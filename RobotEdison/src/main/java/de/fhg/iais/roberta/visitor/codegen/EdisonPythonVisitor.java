package de.fhg.iais.roberta.visitor.codegen;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.components.UsedActor;
import de.fhg.iais.roberta.components.UsedSensor;
import de.fhg.iais.roberta.inter.mode.action.ILanguage;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.light.LightAction;
import de.fhg.iais.roberta.syntax.action.light.LightStatusAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayFileAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayNoteAction;
import de.fhg.iais.roberta.syntax.action.sound.ToneAction;
import de.fhg.iais.roberta.syntax.lang.blocksequence.MainTask;
import de.fhg.iais.roberta.syntax.lang.expr.ConnectConst;
import de.fhg.iais.roberta.syntax.lang.expr.ListCreate;
import de.fhg.iais.roberta.syntax.lang.expr.MathConst;
import de.fhg.iais.roberta.syntax.lang.expr.NumConst;
import de.fhg.iais.roberta.syntax.lang.functions.*;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtList;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitTimeStmt;
import de.fhg.iais.roberta.syntax.sensor.generic.KeysSensor;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.visitor.collect.EdisonUsedHardwareCollectorVisitor;
import de.fhg.iais.roberta.visitor.hardware.IEdisonVisitor;
import de.fhg.iais.roberta.visitor.lang.codegen.prog.AbstractPythonVisitor;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringJoiner;

/**
 * This class visits the Blockly blocks for the Edison robot and translates them into EdPy Python2 code (https://github.com/Bdanilko/EdPy)
 * @author Max Göckel (uzkns)
 */
public class EdisonPythonVisitor extends AbstractPythonVisitor implements IEdisonVisitor<Void> {

    protected final Configuration brickConfig;
    protected final Set<UsedSensor> usedSensors;
    protected final Set<UsedActor> usedActors;
    protected final Set<EdisonUsedHardwareCollectorVisitor.Method> usedMethods;
    protected ILanguage lang;
    private String newLine = System.getProperty("line.separator");


    /**
     * initialize the Python code generator visitor.
     *
     * @param brickConfig    hardware configuration of the robot
     * @param programPhrases to generate the code from
     * @param indentation    to start with. Will be ince/decr depending on block structure
     * @param language       the language
     */
    public EdisonPythonVisitor(Configuration brickConfig, ArrayList<ArrayList<Phrase<Void>>> programPhrases, int indentation, ILanguage language) {
        super(programPhrases, indentation);
        this.brickConfig = brickConfig;

        EdisonUsedHardwareCollectorVisitor checker = new EdisonUsedHardwareCollectorVisitor(programPhrases, brickConfig);
        this.usedSensors = checker.getUsedSensors();
        this.usedActors = checker.getUsedActors();
        this.usedMethods = checker.getUsedMethods();
        this.usedGlobalVarInFunctions = checker.getMarkedVariablesAsGlobal();
        this.lang = language;
    }

    /**
     * Generates the program prefix, i.e. all preparations that need to be executed before Blockly generated code is reached
     *
     * @param withWrapping if the source code should be wrapped by prefix/suffix
     */
    @Override protected void generateProgramPrefix(boolean withWrapping) {
        if ( !withWrapping )
            return;

        this.sb.append("#-------------Setup-------------------" + newLine + newLine);
        this.sb.append("import Ed" + newLine);
        this.sb.append("Ed.EdisonVersion = Ed.V2" + newLine + newLine);
        this.sb.append("Ed.DistanceUnits = Ed.CM" + newLine); //TODO-MAX checken wie es mit time und cm aussieht (anm.: es sieht nicht gut aus)
        this.sb.append("Ed.Tempo = Ed.TEMPO_MEDIUM" + newLine + newLine);
        this.sb.append("#----------Blockly code---------------" + newLine + newLine);
        this.sb.append("Ed.LineTrackerLed(Ed.ON)");
    }

    /**
     * Generates the program suffix, i.e. everything that will be appended to the very end of the source .py file
     * In the suffix are NEPO helper methods for things like sum, round, min, max, etc
     *
     * @param withWrapping if the source code should be wrapped by prefix/suffix
     */
    @Override protected void generateProgramSuffix(boolean withWrapping) {
        if (!withWrapping) {
            return;
        }

        nlIndent(); nlIndent();
        this.sb.append("#-----NEPO helper methods-------------"); nlIndent();

        this.sb.append("def round(num): return ((num+5)/10)"); nlIndent(); nlIndent(); //This method is used so often that it comes with every program

        for ( EdisonUsedHardwareCollectorVisitor.Method m : this.usedMethods) {
            switch (m) {
                case AVG:
                    this.sb.append("def avg(list): return sum(list) / range(list)");
                    nlIndent();
                    break;
                case SUM:
                    this.sb.append("def sum(list):");
                    incrIndentation();
                    nlIndent();
                    this.sb.append("sum_of_list = 0");
                    nlIndent();
                    this.sb.append("for i in range(list): sum_of_list = (sum_of_list + list[i])");
                    nlIndent();
                    this.sb.append("return sum_of_list");
                    decrIndentation();
                    nlIndent();
                    break;
                case MIN:
                    this.sb.append("def min(list):");
                    incrIndentation();
                    nlIndent();
                    this.sb.append("min_of_list = list[0]");
                    nlIndent();
                    this.sb.append("for i in range(list): if list[i] < min_of_list: min_of_list = list[i]");
                    nlIndent();
                    this.sb.append("return min_of_list");
                    decrIndentation();
                    nlIndent();
                    break;
                case MAX:
                    this.sb.append("def max(list):");
                    incrIndentation();
                    nlIndent();
                    this.sb.append("max_of_list = list[0]");
                    nlIndent();
                    this.sb.append("for i in range(list): if list[i] > max_of_list: max_of_list = list[i]");
                    nlIndent();
                    this.sb.append("return max_of_list");
                    decrIndentation();
                    nlIndent();
                    break;
                case CREATE_REPEAT:
                    this.sb.append("def create_repeat(item, times):");
                    incrIndentation();
                    nlIndent();
                    this.sb.append("list = Ed.List(times)");
                    nlIndent();
                    this.sb.append("for i in range(list): list[i] = item");
                    nlIndent();
                    this.sb.append("return list");
                    decrIndentation();
                    nlIndent();
                    break;
                case PRIME:
                    this.sb.append("def isPrime(number):");
                    incrIndentation();
                    nlIndent();
                    this.sb.append("if number <= 1: return False");
                    nlIndent();
                    this.sb.append("for x in range(number - 2) :");
                    incrIndentation();
                    nlIndent();
                    this.sb.append("y = (x + 2)");
                    nlIndent();
                    this.sb.append("if (number % y) == 0: return False");
                    decrIndentation();
                    nlIndent();
                    this.sb.append("return True");
                    decrIndentation();
                    nlIndent();
                    break;
                default:
                    break;
            }

            this.sb.append(newLine);
        }

        return;
    }

    /**
     * Generates source code from a brick configuration and a Blockly program.
     * This method is needed because {@link AbstractPythonVisitor#generateCode(boolean)} and {AbstractPythonVisitor#generateProgramMainBody()} are protected.
     *
     * @param brickCfg       the brick configuration
     * @param programPhrases the program to generate the code from
     * @param withWrapping   wrap the code with prefix/suffix (yes/no)
     * @param language       the locale
     * @return the source code as a String
     */
    public static String generate(
        Configuration brickCfg, ArrayList<ArrayList<Phrase<Void>>> programPhrases, boolean withWrapping, ILanguage language) {
        Assert.notNull(brickCfg);

        EdisonPythonVisitor visitor = new EdisonPythonVisitor(brickCfg, programPhrases, 0, language);
        visitor.generateCode(withWrapping);

        return visitor.sb.toString();
    }

    /**
     * visit a {@link ConnectConst}.
     * //NOP
     * @param connectConst to be visited
     */
    @Override public Void visitConnectConst(ConnectConst<Void> connectConst) {
        //not needed I guess.. (idk what this does) NOP
        return null;
    }

    /**
     * Function to print() text
     * //NOP not supported by EdPy
     * visit a {@link TextPrintFunct}.
     *
     * @param textPrintFunct to be visited
     */
    @Override public Void visitTextPrintFunct(TextPrintFunct<Void> textPrintFunct) {
        //NOP not needed in Edison
        return null;
    }

    /**
     * Function to get a sublist from a list
     * //NOP
     * visit a {@link GetSubFunct}.
     *
     * @param getSubFunct to be visited
     */
    @Override public Void visitGetSubFunct(GetSubFunct<Void> getSubFunct) {
        //NOP Not supported by Edison
        return null;
    }

    /**
     * Function to constrain a number (number is between MIN and MAX)
     * visit a {@link MathConstrainFunct}.
     *
     * @param mathConstrainFunct to be visited
     */
    @Override public Void visitMathConstrainFunct(MathConstrainFunct<Void> mathConstrainFunct) {
        //NOP
        return null;
    }

    /**
     * Function to get a random float between 0 and 1
     * visit a {@link MathRandomFloatFunct}.
     *
     * @param mathRandomFloatFunct
     */
    @Override public Void visitMathRandomFloatFunct(MathRandomFloatFunct<Void> mathRandomFloatFunct) {
        //NOP not supported by Edison robot
        return null;
    }

    /**
     * Function to get a random integer between MIN and MAX
     * visit a {@link MathRandomIntFunct}.
     *
     * @param mathRandomIntFunct to be visited
     */
    @Override public Void visitMathRandomIntFunct(MathRandomIntFunct<Void> mathRandomIntFunct) {
        //NOP not supported by Edison robot
        return null;
    }

    /**
     * Function to append text
     * //NOP not needed
     * visit a {@link TextJoinFunct}.
     *
     * @param textJoinFunct to be visited
     */
    @Override public Void visitTextJoinFunct(TextJoinFunct<Void> textJoinFunct) {
        //NOP
        return null;
    }

    /**
     * Function to drive straight forward/backward with given power % and time/distance
     * visit a {@link DriveAction}.
     *
     * @param driveAction to be visited
     */
    @Override public Void visitDriveAction(DriveAction<Void> driveAction) {
        switch (driveAction.getDirection().toString()) {
            case "FOREWARD":
                this.sb.append("Ed.Drive(Ed.FORWARD, ");
                break;
            case "BACKWARD":
                this.sb.append("Ed.Drive(Ed.BACKWARD, ");
                break;
        }

        this.sb.append("round(");
        driveAction.getParam().getSpeed().visit(this);
        this.sb.append("), ");
        driveAction.getParam().getSpeed().visit(this);
        this.sb.append(")");

        return null;
    }

    /**
     * Function to get the index of the first occurrence of an element in a list
     *
     * @param indexOfFunct to be visited
     * @return
     */
    @Override public Void visitIndexOfFunct(IndexOfFunct<Void> indexOfFunct) {
        //NOP
        return null;
    }




    //--------------------- already done --------------------- \\




    /**
     * Function to perform mathematics on a list (sum/average/min/max/...)
     *
     * @param mathOnListFunct to be visited
     */
    @Override public Void visitMathOnListFunct(MathOnListFunct<Void> mathOnListFunct) {
        //TODO-MAX only use helper methods when really needed
        switch (mathOnListFunct.getFunctName().getOpSymbol()) {
            case "SUM":
                this.sb.append("sum(");
                break;
            case "MIN":
                this.sb.append("min(");
                break;
            case "MAX":
                this.sb.append("max(");
                break;
            case "AVERAGE":
                this.sb.append("avg(");
                break;
            default:
                break;
        }

        mathOnListFunct.getParam().get(0).visit(this);
        this.sb.append(")");
        return null;
    }

    /**
     * All Math blocks (Integers and Fractions) are checked here
     * @param numConst
     * @return
     */
    @Override public Void visitNumConst(NumConst<Void> numConst) {
        if ( isInteger(numConst.getValue()) ) {
            super.visitNumConst(numConst);
        } else {
            throw new IllegalArgumentException("Not an integer");
        }
        return null;
    }

    /**
     * Visits the programs main task, i.e. the Blockly-blocks that are (indirectly) connected to the red "Start"-block.
     * In this method, user-generated Blockly-methods are appended also
     *
     * @param mainTask the main task class to be visited
     * @return null
     */
    @Override public Void visitMainTask(MainTask<Void> mainTask) {
        StmtList<Void> variables = mainTask.getVariables();
        variables.visit(this); //fill usedGlobalVarInFunctions with values

        nlIndent();
        generateUserDefinedMethods(); //Functions created by the user will be defined after the main function
        return null;
    }

    /**
     * Function to check if a number is odd/even/positive/negative/...
     * visit a {@link MathNumPropFunct}.
     *
     * @param mathNumPropFunct to be visited
     */
    @Override public Void visitMathNumPropFunct(MathNumPropFunct<Void> mathNumPropFunct) {
        switch (mathNumPropFunct.getFunctName()) {
            case EVEN:
                this.sb.append("((");
                mathNumPropFunct.getParam().get(0).visit(this);
                this.sb.append(" % 2) == 0)");
                break;
            case ODD:
                this.sb.append("((");
                mathNumPropFunct.getParam().get(0).visit(this);
                this.sb.append(" % 2) != 0)");
                break;
            case PRIME:
                this.sb.append("BlocklyMethods.isPrime(");
                mathNumPropFunct.getParam().get(0).visit(this);
                this.sb.append(")");
                break;
            case WHOLE: //TODO-MAX entfernen, jeder Integer is Whole
                this.sb.append("(True)");
                break;
            case POSITIVE:
                this.sb.append("(");
                mathNumPropFunct.getParam().get(0).visit(this);
                this.sb.append(" >= 0)");
            case NEGATIVE:
                this.sb.append("(");
                mathNumPropFunct.getParam().get(0).visit(this);
                this.sb.append(" < 0)");
                break;
            case DIVISIBLE_BY:
                this.sb.append("((");
                mathNumPropFunct.getParam().get(0).visit(this);
                this.sb.append(" % ");
                mathNumPropFunct.getParam().get(1).visit(this);
                this.sb.append(") == 0)");
                break;
            default:
                break;
        }

        return null;
    }

    /**
     * Function to create a list
     * visit a {@link ListCreate}.
     *
     * @param listCreate to be visited
     */
    @Override public Void visitListCreate(ListCreate<Void> listCreate) {
        int listSize = listCreate.getValue().get().size();

        this.sb.append("Ed.List(").append(listSize).append((", ["));
        for (int i = 0; i < listSize; i++) {
            listCreate.getValue().get().get(i).visit(this);
            if (i < (listSize -1)) {
                this.sb.append((","));
            } else {
                this.sb.append("]");
            }
        }
        this.sb.append(")");

        return null;
    }

    /**
     * Function to create a List with repeated item
     * visit a {@link ListRepeat}.
     *
     * @param listRepeat to be visited
     */
    @Override public Void visitListRepeat(ListRepeat<Void> listRepeat) {
        this.sb.append("create_repeat(");
        listRepeat.getParam().get(0).visit(this);
        this.sb.append(", ");
        listRepeat.getParam().get(1).visit(this);
        this.sb.append(")");

        return null;
    }

    /**
     * Function to execute code when a key is pressed
     * visit a {@link KeysSensor}.
     *
     * @param keysSensor to be visited
     */
    @Override public Void visitKeysSensor(KeysSensor<Void> keysSensor) {
        switch (keysSensor.getPort()) {
            case "REC":
                this.sb.append("Ed.ReadKeypad(Ed.KEYPAD_ROUND)");
                break;
            case "PLAY":
                this.sb.append("Ed.ReadKeypad(Ed.KEYPAD_TRIANGLE)");
                break;
            default:
        }

        return null;
    }

    /**
     * Function to drive a curve forard/backward
     * visit a {@link CurveAction}.
     *
     * @param curveAction
     */
    @Override public Void visitCurveAction(CurveAction<Void> curveAction) {

        String direction;

        switch (curveAction.getDirection().toString()) {
            default:
            case "FOREWARD":
                direction = "Ed.FORWARD";
                break;
            case "BACKWARD":
                direction = "Ed.BACKWARD";
                break;
        }
        this.sb.append("Ed.DriveLeftMotor(" + direction + ", round(");
        curveAction.getParamLeft().getSpeed().visit(this);
        this.sb.append("), round(");
        curveAction.getParamLeft().getDuration().getValue().visit(this);
        this.sb.append("))");

        nlIndent();

        this.sb.append("Ed.DriveRightMotor(" + direction + ", round(");
        curveAction.getParamRight().getSpeed().visit(this);
        this.sb.append("), round(");
        curveAction.getParamRight().getDuration().getValue().visit(this);
        this.sb.append("))");

        return null;
    }

    /**
     * Function to turn the robot
     * visit a {@link TurnAction}.
     *
     * @param turnAction to be visited
     */
    @Override public Void visitTurnAction(TurnAction<Void> turnAction) {
        switch (turnAction.getDirection().toString()) {
            case "RIGHT":
                this.sb.append("Ed.Drive(Ed.FORWARD_RIGHT, ");
                break;
            case "LEFT":
                this.sb.append("Ed.Drive(Ed.FORWARD_LEFT, ");
                break;
            default:
                break;
        }

        this.sb.append("round(");
        turnAction.getParam().getSpeed().visit(this);
        this.sb.append("), ");
        turnAction.getParam().getDuration().getValue().visit(this); //TODO-MAX degree != distance
        this.sb.append(")");

        return null;
    }

    /**
     * Function to turn the motors to a set power%
     * visit a {@link MotorOnAction}.
     *
     * @param motorOnAction
     */
    @Override public Void visitMotorOnAction(MotorOnAction<Void> motorOnAction) {
        switch (motorOnAction.getUserDefinedPort()) {
            case "LMOTOR":
                this.sb.append("Ed.DriveLeftMotor(Ed.FORWARD, round(");
                motorOnAction.getParam().getSpeed().visit(this);
                this.sb.append("), Ed.DISTANCE_UNLIMITED)");
                break;
            case "RMOTOR":
                this.sb.append("Ed.DriveRightMotor(Ed.FORWARD, round(");
                motorOnAction.getParam().getSpeed().visit(this);
                this.sb.append("), Ed.DISTANCE_UNLIMITED)");
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * Function to stop the motor
     * visit a {@link MotorStopAction}.
     *
     * @param motorStopAction
     */
    @Override public Void visitMotorStopAction(MotorStopAction<Void> motorStopAction) {
        switch (motorStopAction.getUserDefinedPort()) {
            case "LMOTOR":
                this.sb.append("Ed.DriveLeftMotor(Ed.STOP, Ed.SPEED_1, 0)");
                break;
            case "RMOTOR":
                this.sb.append("Ed.DriveRightMotor(Ed.STOP, Ed.SPEED_1, 0)");
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * Function to stop the motors
     * visit a {@link MotorDriveStopAction}.
     *
     * @param stopAction
     */
    @Override public Void visitMotorDriveStopAction(MotorDriveStopAction<Void> stopAction) {
        this.sb.append("Ed.Drive(Ed.STOP, Ed.SPEED_1, 0)");
        return null;
    }

    /**
     * Function to find out the length of list or if it is empty
     * //NOP is empty not supported
     * visit a {@link LengthOfIsEmptyFunct}.
     *
     * @param lengthOfIsEmptyFunct to be visited
     */
    @Override public Void visitLengthOfIsEmptyFunct(LengthOfIsEmptyFunct<Void> lengthOfIsEmptyFunct) {
        this.sb.append("len( ");
        lengthOfIsEmptyFunct.getParam().get(0).visit(this);this.sb.append(")");
        return null;
    }

    /**
     * Visits the Blockly wait-block (without user-defined waiting time)
     *
     * @param waitStmt to be visited
     * @return null
     */
    @Override public Void visitWaitStmt(WaitStmt<Void> waitStmt) {
        this.sb.append("Ed.TimeWait(1, Ed.TIME_SECONDS)");
        return null;
    }

    /**
     * Visits the Blockly wait-block (with user-defined waiting time)
     *
     * @param waitTimeStmt
     * @return null
     */
    @Override public Void visitWaitTimeStmt(WaitTimeStmt<Void> waitTimeStmt) {
        this.sb.append("Ed.WaitTime(");
        waitTimeStmt.getTime().visit(this);
        this.sb.append(", Ed.TIME_MILLISECONDS)");
        return null;
    }

    /**
     * Function to get the n-th element of a list
     * visit a {@link ListGetIndex}.
     *
     * @param listGetIndex to be visited
     */
    @Override public Void visitListGetIndex(ListGetIndex<Void> listGetIndex) {
        listGetIndex.getParam().get(0).visit(this); //Name of list
        this.sb.append("[");
        listGetIndex.getParam().get(1).visit(this); //index (from 0)
        this.sb.append("]");
        return null;
    }

    /**
     * Function to set the n-th element of a List or insert the element at the n-th place (if supported)
     * visit a {@link ListSetIndex}.
     *
     * @param listSetIndex to be visited
     */
    @Override public Void visitListSetIndex(ListSetIndex<Void> listSetIndex) {
        listSetIndex.getParam().get(0).visit(this); //Name of list
        this.sb.append("[");
        listSetIndex.getParam().get(2).visit(this);
        this.sb.append("] = ");
        listSetIndex.getParam().get(1).visit(this);
        return null;
    }

    /**
     * Function to get a math constant
     * visit a {@link MathConst}.
     *
     * @param mathConst to be visited
     */
    @Override public Void visitMathConst(MathConst<Void> mathConst) {

        //EdPy does not support importing the math module so every math constant has to be rounded.
        //EdPy does not support floats
        switch ( mathConst.getMathConst() ) {
            case PI:
                this.sb.append("3");
                break;
            case E:
                this.sb.append("3");
                break;
            case GOLDEN_RATIO:
                this.sb.append("2");
                break;
            case SQRT2:
                this.sb.append("1");
                break;
            case SQRT1_2:
                this.sb.append("1");
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * Function to turn on the LEDs
     * visit a {@link LightAction}.
     *
     * @param lightAction to be visited
     */
    @Override public Void visitLightAction(LightAction<Void> lightAction) {
        switch (lightAction.getPort()) {
            case "RLED":
                this.sb.append("Ed.RightLed(Ed.ON)");
                break;
            case "LLED":
                this.sb.append("Ed.LeftLed(Ed.ON)");
                break;
            default:
                break;
        }

        return null;
    }

    /**
     * Function to turn off the LEDs
     * visit a {@link LightStatusAction}.
     *
     * @param lightStatusAction to be visited
     */
    @Override public Void visitLightStatusAction(LightStatusAction<Void> lightStatusAction) {
        switch (lightStatusAction.getPort()) {
            case "RLED":
                this.sb.append("Ed.RightLed(Ed.OFF)");
                break;
            case "LLED":
                this.sb.append("Ed.LeftLed(Ed.OFF)");
                break;
            default:
                break;
        }

        return null;
    }

    /**
     * Function to play a tone
     * visit a {@link ToneAction}.
     *
     * @param toneAction to be visited
     */
    @Override public Void visitToneAction(ToneAction<Void> toneAction) {
        this.sb.append("Ed.PlayMyBeep(");
        toneAction.getFrequency().visit(this); //TODO-MAX-JS nur Frequency einstellen, nicht auch noch duration
        this.sb.append(")");

        return null;
    }

    /**
     * Function to play a note
     * visit a {@link PlayNoteAction}.
     *
     * @param playNoteAction
     */
    @Override public Void visitPlayNoteAction(PlayNoteAction<Void> playNoteAction) {

        this.sb.append("Ed.PlayTone(");
        this.sb.append(playNoteAction.getFrequency()); //TODO-MAX-JS Note auswählen
        this.sb.append(", ");
        switch (playNoteAction.getDuration()) {
            case "2000":
                this.sb.append("Ed.NOTE_WHOLE");
                break;
            case "1000":
                this.sb.append("Ed.NOTE_HALF");
                break;
            case "500":
                this.sb.append("Ed.NOTE_QUARTER");
                break;
            case "250":
                this.sb.append("Ed.NOTE_EIGHTH");
                break;
            case "125":
                this.sb.append("Ed.NOTE_SIXTEENTH");
                break;
        }
        this.sb.append(")");

        return null;
    }

    /**
     * Function to play a sound file/note file
     * visit a {@link PlayFileAction}.
     *
     * @param playFileAction
     */
    @Override public Void visitPlayFileAction(PlayFileAction<Void> playFileAction) {
        this.sb.append("Ed.PlayTune(");
        switch (playFileAction.getFileName().toLowerCase()) {
            case "0":
                this.sb.append("Ed.TuneString(25, \"d4e4f4e4d4c4n2d4e4f4e4d1z\")");
                break;
            case "1":
                this.sb.append("Ed.TuneString(25, \"d4e4f4e4d4c4n2d4e4f4e4d1z\")");
                break;
            case "2":
                this.sb.append("Ed.TuneString(25, \"d4e4f4e4d4c4n2d4e4f4e4d1z\")");
                break;
            case "3":
                this.sb.append("Ed.TuneString(25, \"d4e4f4e4d4c4n2d4e4f4e4d1z\")");
                break;
            case "4":
                this.sb.append("Ed.TuneString(25, \"d4e4f4e4d4c4n2d4e4f4e4d1z\")");
                break;
        }
        this.sb.append(")");
        return null;
    }
}
