package de.fhg.iais.roberta.visitor.collect;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorSetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.lang.functions.ListRepeat;
import de.fhg.iais.roberta.syntax.lang.functions.MathNumPropFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathOnListFunct;
import de.fhg.iais.roberta.syntax.sensor.generic.*;
import de.fhg.iais.roberta.visitor.hardware.IEdisonVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class visits all the sensors/actors of the Edison brick and collects information about them
 */
public class EdisonUsedHardwareCollectorVisitor extends AbstractUsedHardwareCollectorVisitor implements IEdisonVisitor<Void> {

    public EdisonUsedHardwareCollectorVisitor(ArrayList<ArrayList<Phrase<Void>>> programPhrases, Configuration robotConfiguration) {
        super(robotConfiguration);
        check(programPhrases);
        this.infraredBlocker = Sensor.NONE;
        this.usedMethods = new HashSet<Method>();
    }

    /**
     * The Sensors that can use the IR LEDs
     */
    public enum Sensor {
        OBSTACLE, IRSENDER, IRSEEKER, NONE
    }
    //TODO-MAX IR seeker, IR RC, Obstacle detection share IR LEDs
    private Sensor infraredBlocker; //saves which Sensor uses the IR LEDs

    public enum Method {
        SUM, MIN, MAX, ROUND, AVG, CREATE_REPEAT, PRIME
    }
    private Set<Method> usedMethods;


    @Override public Void visitLightSensor(LightSensor<Void> lightSensor) {
        return null;
    }

    @Override public Void visitInfraredSensor(InfraredSensor<Void> infraredSensor) {
        return null;
    }

    @Override public Void visitIRSeekerSensor(IRSeekerSensor<Void> irSeekerSensor) {
        return null;
    }

    @Override public Void visitDriveAction(DriveAction<Void> driveAction) {
        return null;
    }

    @Override public Void visitCurveAction(CurveAction<Void> curveAction) {
        return null;
    }

    @Override public Void visitTurnAction(TurnAction<Void> turnAction) {
        return null;
    }


    //TODO-MAX JavaDoc zuende
    /**
     *
     * @param listRepeat
     * @return
     */
    @Override public Void visitListRepeat(ListRepeat<Void> listRepeat) {
        this.usedMethods.add(Method.CREATE_REPEAT);
        return null;
    }

    /**
     *
     * @param mathNumPropFunct
     * @return
     */
    @Override public Void visitMathNumPropFunct(MathNumPropFunct<Void> mathNumPropFunct) {
        if (mathNumPropFunct.getFunctName().getOpSymbol().equals("PRIME")) {
            this.usedMethods.add(Method.PRIME);
        }

        return null;
    }

    /**
     *
     * @param mathOnListFunct
     * @return
     */
    @Override public Void visitMathOnListFunct(MathOnListFunct<Void> mathOnListFunct) {
        switch (mathOnListFunct.getFunctName().getOpSymbol()) {
            case "SUM":
                this.usedMethods.add(Method.SUM);
                break;
            case "MIN":
                this.usedMethods.add(Method.MIN);
                break;
            case "MAX":
                this.usedMethods.add(Method.MAX);
                break;
            case "AVERAGE":
                this.usedMethods.add(Method.AVG);
                break;
            default:
                break;
        }

        return null;
    }

    @Override public Void visitMotorDriveStopAction(MotorDriveStopAction<Void> stopAction) {
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

    @Override protected void check(ArrayList<ArrayList<Phrase<Void>>> phrasesSet) {
        super.check(phrasesSet);
    }

    public Set<Method> getUsedMethods() {
        return this.usedMethods;
    }


}