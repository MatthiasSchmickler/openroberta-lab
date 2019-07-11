package de.fhg.iais.roberta.visitor.collect;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.motor.MotorGetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorSetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.lang.expr.VarDeclaration;
import de.fhg.iais.roberta.syntax.sensor.generic.*;
import de.fhg.iais.roberta.visitor.hardware.IEdisonVisitor;

import java.util.ArrayList;

/**
 * This class visits all the sensors/actors of the Edison brick and collects information about them
 */
public class EdisonUsedHardwareCollectorVisitor extends AbstractUsedHardwareCollectorVisitor implements IEdisonVisitor<Void> {

    public EdisonUsedHardwareCollectorVisitor(ArrayList<ArrayList<Phrase<Void>>> programPhrases, Configuration robotConfiguration) {
        super(robotConfiguration);
        check(programPhrases);
        this.infraredBlocker = Sensor.NONE;
    }

    /**
     * The Sensors that can use the IR LEDs
     */
    private enum Sensor {
        OBSTACLE, IRSENDER, IRSEEKER, NONE
    }

    //TODO-MAX IR seeker, IR RC, Obstacle detection share IR LEDs
    private Sensor infraredBlocker; //saves which Sensor uses the IR LEDs


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
}