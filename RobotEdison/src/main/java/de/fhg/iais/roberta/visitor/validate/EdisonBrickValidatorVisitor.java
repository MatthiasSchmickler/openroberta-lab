package de.fhg.iais.roberta.visitor.validate;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.syntax.action.MoveAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorGetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorSetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.sensor.generic.IRSeekerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.InfraredSensor;
import de.fhg.iais.roberta.visitor.hardware.IEdisonVisitor;

public class EdisonBrickValidatorVisitor extends AbstractBrickValidatorVisitor implements IEdisonVisitor<Void> {

    public EdisonBrickValidatorVisitor(Configuration brickConfiguration) {
        super(brickConfiguration);
        this.infraredBlocker = Sensor.NONE;
    }

    private enum Sensor { //The Sensor that uses the IR LEDs
        OBSTACLE, RC, NONE
    }
    private Sensor infraredBlocker;


    @Override public Void visitDriveAction(DriveAction<Void> driveAction) {
        return null;
    }

    @Override public Void visitTurnAction(TurnAction<Void> turnAction) {
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

    @Override public Void visitMotorDriveStopAction(MotorDriveStopAction<Void> stopAction) {
        return null;
    }

    @Override public Void visitCurveAction(CurveAction<Void> driveAction) {
        return null;
    }

    @Override public Void visitInfraredSensor(InfraredSensor<Void> infraredSensor) {
        return null;
    }

    @Override public Void visitIRSeekerSensor(IRSeekerSensor<Void> irSeekerSensor) {
        return null;
    }

    @Override protected void checkMotorPort(MoveAction<Void> action) {
        super.checkMotorPort(action);
    }
}
