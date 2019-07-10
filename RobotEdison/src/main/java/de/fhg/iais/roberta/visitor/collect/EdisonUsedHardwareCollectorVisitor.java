package de.fhg.iais.roberta.visitor.collect;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.syntax.Phrase;
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
    }
}
