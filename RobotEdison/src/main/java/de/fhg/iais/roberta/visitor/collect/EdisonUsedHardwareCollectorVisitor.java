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
    }


    /**
     * Blockly Blocks that need an extra helper method in the source code
     */
    public enum Method {
        AVG, //Average of a list
        CREATE_REPEAT, //Create a list with an integer x repeated n times
        MAX, //maximum of a given list
        MIN, //minimum of a list
        PRIME, //check if the given number is a prime number
        ROUND, //round a number and divide it by 10 for Edisons Drive() method
        SUM //Sum of a list

    }

    private HashSet<Method> usedMethods; //All needed helper methods as a Set


    /**
     * Visit the "Create List with repeated item" function so that their helper methods can be appended to the end of the source code
     *
     * @param listRepeat
     * @return
     */
    @Override public Void visitListRepeat(ListRepeat<Void> listRepeat) {
        setListsUsed(true);
        usedMethod(Method.CREATE_REPEAT);
        return null;
    }

    /**
     * Visit the Number Property function (number is even/odd/prime/...) so that their helper methods can be appended to the end of the source code
     *
     * @param mathNumPropFunct
     * @return
     */
    @Override public Void visitMathNumPropFunct(MathNumPropFunct<Void> mathNumPropFunct) {
        if (mathNumPropFunct.getFunctName().getOpSymbol().equals("PRIME")) {
            usedMethod(Method.PRIME);
        }

        return null;
    }

    /**
     * Visit the Math on list function (sum/average/min/max) so that their helper methods can be appended to the end of the source code
     *
     * @param mathOnListFunct
     * @return
     */
    @Override public Void visitMathOnListFunct(MathOnListFunct<Void> mathOnListFunct) {
        switch (mathOnListFunct.getFunctName().getOpSymbol()) {
            case "SUM":
                this.usedMethod(Method.SUM);
                break;
            case "MIN":
                this.usedMethod(Method.MIN);
                break;
            case "MAX":
                this.usedMethod(Method.MAX);
                break;
            case "AVERAGE":
                this.usedMethod(Method.AVG);
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

    @Override public Void visitLightSensor(LightSensor<Void> lightSensor) {
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

    /**
     * Returns all used helper methods (see {@link EdisonUsedHardwareCollectorVisitor#usedMethod(Method)}). If no methods are used,
     * this method returns a new empty Set
     *
     * @return
     */
    public Set<Method> getUsedMethods() {
        if (this.usedMethods == null) {
            //If no helper methods have been used, an empty set will be returned
            return new HashSet<>();
        }
        return this.usedMethods;
    }

    /**
     * Helper method to list all used "NEPO helper methods" so that they can be appended to the end of the source code
     *
     * @param m the helper method that was called
     */
    private void usedMethod(Method m) {
        if (this.usedMethods == null) {
            this.usedMethods = new HashSet<>();
            this.usedMethods.add(m);
        } else {
            this.usedMethods.add(m);
        }
    }
}