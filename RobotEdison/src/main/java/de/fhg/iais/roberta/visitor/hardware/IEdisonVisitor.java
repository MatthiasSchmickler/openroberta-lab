package de.fhg.iais.roberta.visitor.hardware;

import de.fhg.iais.roberta.visitor.hardware.actor.ILightVisitor;
import de.fhg.iais.roberta.visitor.hardware.actor.IMotorVisitor;
import de.fhg.iais.roberta.visitor.hardware.actor.ISoundVisitor;
import de.fhg.iais.roberta.visitor.hardware.sensor.ISensorVisitor;

public interface IEdisonVisitor<V> extends ILightVisitor<V>, IMotorVisitor<V>, ISoundVisitor<V>, ISensorVisitor<V> {

    //TODO-MAX look @ NXT visitors
    //TODO-MAX look @ Mbed visitors

}
