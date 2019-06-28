package de.fhg.iais.roberta.visitor.codegen;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.components.UsedActor;
import de.fhg.iais.roberta.components.UsedSensor;
import de.fhg.iais.roberta.inter.mode.action.ILanguage;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.light.LightAction;
import de.fhg.iais.roberta.syntax.action.light.LightStatusAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorGetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorSetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayFileAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayNoteAction;
import de.fhg.iais.roberta.syntax.action.sound.ToneAction;
import de.fhg.iais.roberta.syntax.action.sound.VolumeAction;
import de.fhg.iais.roberta.syntax.lang.blocksequence.MainTask;
import de.fhg.iais.roberta.syntax.lang.expr.ConnectConst;
import de.fhg.iais.roberta.syntax.lang.expr.ListCreate;
import de.fhg.iais.roberta.syntax.lang.functions.*;
import de.fhg.iais.roberta.syntax.lang.stmt.StmtList;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.WaitTimeStmt;
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
     * This method is needed because {@link AbstractPythonVisitor#generateCode(boolean)} and {@link AbstractPythonVisitor#generateProgramMainBody()} are protected.
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

    @Override public Void visitConnectConst(ConnectConst<Void> connectConst) {
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

        if ( !this.usedGlobalVarInFunctions.isEmpty() ) {
            StringJoiner joiner = new StringJoiner(", "); //concatenate every variable
            for ( String var : usedGlobalVarInFunctions ) {
                joiner.add(var);
            }

            this.sb.append("global " + joiner.toString());
        }

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

    //TODO-MAX alle visitors zu ende machen

    @Override public Void visitTextPrintFunct(TextPrintFunct<Void> textPrintFunct) {
        return null;
    }

    @Override public Void visitGetSubFunct(GetSubFunct<Void> getSubFunct) {
        return null;
    }

    @Override public Void visitIndexOfFunct(IndexOfFunct<Void> indexOfFunct) {
        return null;
    }

    @Override public Void visitLengthOfIsEmptyFunct(LengthOfIsEmptyFunct<Void> lengthOfIsEmptyFunct) {
        return null;
    }

    @Override public Void visitListCreate(ListCreate<Void> listCreate) {
        return null;
    }

    @Override public Void visitListGetIndex(ListGetIndex<Void> listGetIndex) {
        return null;
    }

    @Override public Void visitListRepeat(ListRepeat<Void> listRepeat) {
        return null;
    }

    @Override public Void visitListSetIndex(ListSetIndex<Void> listSetIndex) {
        return null;
    }

    @Override public Void visitMathConstrainFunct(MathConstrainFunct<Void> mathConstrainFunct) {
        return null;
    }

    @Override public Void visitMathNumPropFunct(MathNumPropFunct<Void> mathNumPropFunct) {
        return null;
    }

    @Override public Void visitMathOnListFunct(MathOnListFunct<Void> mathOnListFunct) {
        return null;
    }

    @Override public Void visitMathRandomFloatFunct(MathRandomFloatFunct<Void> mathRandomFloatFunct) {
        return null;
    }

    @Override public Void visitMathRandomIntFunct(MathRandomIntFunct<Void> mathRandomIntFunct) {
        return null;
    }

    @Override public Void visitTextJoinFunct(TextJoinFunct<Void> textJoinFunct) {
        return null;
    }

    @Override public Void visitLightAction(LightAction<Void> lightAction) {
        return null;
    }

    @Override public Void visitLightStatusAction(LightStatusAction<Void> lightStatusAction) {
        return null;
    }

    @Override public Void visitMotorGetPowerAction(MotorGetPowerAction<Void> motorGetPowerAction) {
        return null;
    }

    @Override public Void visitMotorOnAction(MotorOnAction<Void> motorOnAction) {
        return null;
    }

    @Override public Void visitMotorSetPowerAction(MotorSetPowerAction<Void> motorSetPowerAction) {
        return null;
    }

    @Override public Void visitMotorStopAction(MotorStopAction<Void> motorStopAction) {
        return null;
    }

    @Override public Void visitToneAction(ToneAction<Void> toneAction) {
        return null;
    }

    @Override public Void visitPlayNoteAction(PlayNoteAction<Void> playNoteAction) {
        return null;
    }

    @Override public Void visitVolumeAction(VolumeAction<Void> volumeAction) {
        return null;
    }

    @Override public Void visitPlayFileAction(PlayFileAction<Void> playFileAction) {
        return null;
    }
}
