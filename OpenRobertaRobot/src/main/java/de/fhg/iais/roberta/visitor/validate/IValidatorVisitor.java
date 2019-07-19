package de.fhg.iais.roberta.visitor.validate;

import java.util.Map;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.visitor.IVisitor;

public interface IValidatorVisitor<V> extends IVisitor<Void> {
    void visit(Configuration configuration);

    Map<String, String> getResult();

    Key getResultKey();
}
