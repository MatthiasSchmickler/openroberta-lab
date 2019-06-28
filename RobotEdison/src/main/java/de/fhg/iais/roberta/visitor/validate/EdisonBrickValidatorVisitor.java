package de.fhg.iais.roberta.visitor.validate;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.visitor.hardware.IEdisonVisitor;

public class EdisonBrickValidatorVisitor extends AbstractBrickValidatorVisitor implements IEdisonVisitor<Void> {

    public EdisonBrickValidatorVisitor(Configuration brickConfiguration) {
        super(brickConfiguration);
    }


    //TODO-MAX fragen was hier noch rein muss
}
