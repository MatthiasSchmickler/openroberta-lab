package de.fhg.iais.roberta.visitor.collect;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.syntax.Phrase;

import java.util.ArrayList;

/**
 * This class visits all the sensors/actors of the Edison brick and collects information about them
 */
public class EdisonUsedHardwareCollectorVisitor extends AbstractUsedHardwareCollectorVisitor {

    public EdisonUsedHardwareCollectorVisitor(ArrayList<ArrayList<Phrase<Void>>> programPhrases, Configuration robotConfiguration) {
        super(robotConfiguration);
        check(programPhrases);
    }

    //TODO-MAX implement

}
