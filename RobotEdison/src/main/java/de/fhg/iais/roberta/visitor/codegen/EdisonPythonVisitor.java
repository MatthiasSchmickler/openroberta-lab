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
 */
public class EdisonPythonVisitor extends AbstractPythonVisitor implements IEdisonVisitor<Void> {

    protected final Configuration brickConfig;
    protected final Set<UsedSensor> usedSensors;
    protected final Set<UsedActor> usedActors;
    protected ILanguage lang;
    private String newLine = System.getProperty("line.separator");

    /**
     * visit a {@link KeysSensor}.
     *
     * @param keysSensor to be visited
     */
    @Override public Void visitKeysSensor(KeysSensor<Void> keysSensor) {
        switch (keysSensor.getPort().toLowerCase()) {
            case "rec":
                this.sb.append("Ed.ReadKeypad(Ed.KEYPAD_ROUND)");
            case "play":
                this.sb.append("Ed.ReadKeypad(Ed.KEYPAD_TRIANGLE)");
            default:
                System.out.println("Yo");
                System.out.println(keysSensor.getPort().toLowerCase());
        }

        return null;
    }

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
        this.usedGlobalVarInFunctions = checker.getMarkedVariablesAsGlobal();
        this.lang = language;
    }

    /**
     * Generates the program prefix, i.e. all preparations that need to be executed before the __main__ method is called
     *
     * @param withWrapping
     */
    @Override protected void generateProgramPrefix(boolean withWrapping) {
        if ( !withWrapping )
            return;

        this.sb.append("#-------------Setup-------------------" + newLine + newLine);
        this.sb.append("import Ed" + newLine);
        this.sb.append("Ed.EdisonVersion = Ed.V1" + newLine + newLine);
        this.sb.append("Ed.DistanceUnits = Ed.TIME" + newLine); //TODO-MAX checken wie es mit time und cm aussieht (anm.: es sieht nicht gut aus)
        this.sb.append("Ed.Tempo = Ed.TEMPO_MEDIUM" + newLine + newLine);
        this.sb.append("#--------Blockly code below-----------" + newLine + newLine);
    }

    /**
     * Generates the program suffix, i.e. everything that will be appended to the very end of the source .py file
     *
     * @param withWrapping
     */
    @Override protected void generateProgramSuffix(boolean withWrapping) {
        return;
    }

    /**
     * Generates source code from a brick configuration and a Blockly program.
     * This method is needed because {@link AbstractPythonVisitor#generateCode(boolean)} and {AbstractPythonVisitor#generateProgramMainBody()} are protected.
     *
     * @param brickCfg       the brick configuration
     * @param programPhrases the program to generate the code from
     * @param withWrapping   is wrapping wanted?
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
     *
     * @param connectConst to be visited
     */
    @Override public Void visitConnectConst(ConnectConst<Void> connectConst) {
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

        return null;
    }

    /**
     * Function to find the first (or last) occurrence of an element in a list
     * //NOP
     * visit a {@link IndexOfFunct}.
     *
     * @param indexOfFunct to be visited
     */

    /**
     * Function to create a list
     * visit a {@link ListCreate}.
     *
     * @param listCreate to be visited
     */
    @Override public Void visitListCreate(ListCreate<Void> listCreate) {
        return null;
    }

    /**
     * Function to create a List with repeated item
     * visit a {@link ListRepeat}.
     *
     * @param listRepeat to be visited
     */
    @Override public Void visitListRepeat(ListRepeat<Void> listRepeat) {
        this.sb.append(listRepeat.getParam().get(0).toString());

        return null;
    }

    /**
     * Function to constrain a number (number is between MIN and MAX)
     * visit a {@link MathConstrainFunct}.
     *
     * @param mathConstrainFunct to be visited
     */
    @Override public Void visitMathConstrainFunct(MathConstrainFunct<Void> mathConstrainFunct) {
        return null;
    }

    /** Function to check if a number is odd/even/positive/negative/...
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
            case PRIME: //TODO-MAX eigene implementierung?
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
     * visit a {@link MathOnListFunct}.
     *
     * @param mathOnListFunct to be visited
     */
    @Override public Void visitMathOnListFunct(MathOnListFunct<Void> mathOnListFunct) {
        return null;
    }

    /**
     * Function to get a random fraction/float
     * visit a {@link MathRandomFloatFunct}.
     *
     * @param mathRandomFloatFunct
     */
    @Override public Void visitMathRandomFloatFunct(MathRandomFloatFunct<Void> mathRandomFloatFunct) {
        return null;
    }

    /**
     * Function to get a random integer between MIN and MAX
     * visit a {@link MathRandomIntFunct}.
     *
     * @param mathRandomIntFunct to be visited
     */
    @Override public Void visitMathRandomIntFunct(MathRandomIntFunct<Void> mathRandomIntFunct) {
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
        return null;
    }

    /**
     * Function to drive straight forward/backward with given power % and time/distance
     * visit a {@link DriveAction}.
     *
     * @param driveAction to be visited
     */
    @Override public Void visitDriveAction(DriveAction<Void> driveAction) {
        return null;
    }

    /**
     * Function to drive a curve forard/backward
     * visit a {@link CurveAction}.
     *
     * @param curveAction
     */
    @Override public Void visitCurveAction(CurveAction<Void> curveAction) {
        return null;
    }

    /**
     * Function to turn the robot
     * visit a {@link TurnAction}.
     *
     * @param turnAction to be visited
     */
    @Override public Void visitTurnAction(TurnAction<Void> turnAction) {
        return null;
    }

    /**
     * Function to turn the motors to a set power%
     * visit a {@link MotorOnAction}.
     *
     * @param motorOnAction
     */
    @Override public Void visitMotorOnAction(MotorOnAction<Void> motorOnAction) {
        return null;
    }

    /**
     * Function to stop the motor
     * visit a {@link MotorStopAction}.
     *
     * @param motorStopAction
     */
    @Override public Void visitMotorStopAction(MotorStopAction<Void> motorStopAction) {
        this.sb.append(motorStopAction.getUserDefinedPort().toLowerCase());
        return null;
    }

    /**
     * Function to stop the motors
     * visit a {@link MotorDriveStopAction}.
     *
     * @param stopAction
     */
    @Override public Void visitMotorDriveStopAction(MotorDriveStopAction<Void> stopAction) {
        this.sb.append("chi zhang");
        return null;
    }

    /**
     * Function to get the index of the first occurrence of an element in a list
     *
     * @param indexOfFunct to be visited
     * @return
     */
    @Override public Void visitIndexOfFunct(IndexOfFunct<Void> indexOfFunct) {
        return null;
    }




    //--------------------- already done --------------------- \\




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
     * Visits the programs main task, i.e. the Blockly-blocks that are (indirectly) connected to the red "Start"-block.
     * In this method, user-generated Blockly-methods are appended also
     *
     * @param mainTask the main task class to be visited
     * @return null
     */
    @Override public Void visitMainTask(MainTask<Void> mainTask) {
        StmtList<Void> variables = mainTask.getVariables();
        variables.visit(this); //fill usedGlobalVarInFunctions with values

        this.sb.append(newLine);
        generateUserDefinedMethods(); //Functions created by the user will be defined after the main function
        return null;
    }

    /**
     * Visits the Blockly wait-block (without user-defined waiting time)
     *
     * @param waitStmt to be visited
     * @return null
     */
    @Override public Void visitWaitStmt(WaitStmt<Void> waitStmt) {
        this.sb.append("Ed.TimeWait(1, Ed.TIME_SECONDS)"); //TODO-MAX richtige länge
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
                this.sb.append("3 //Pi");
                break;
            case E:
                this.sb.append("3 //e");
                break;
            case GOLDEN_RATIO:
                this.sb.append("2 //Golden Ratio");
                break;
            case SQRT2:
                this.sb.append("1 //sqrt(2)");
                break;
            case SQRT1_2:
                this.sb.append("1 //sqrt(0.5)");
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
        switch (lightAction.getPort().toLowerCase()) {
            case "1":
                this.sb.append("Ed.RightLed(Ed.ON)");
                break;
            case "2":
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
            case "1":
                this.sb.append("Ed.RightLed(Ed.OFF)");
                break;
            case "2":
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
