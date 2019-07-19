package de.fhg.iais.roberta.factory;

import de.fhg.iais.roberta.util.PluginProperties;
import de.fhg.iais.roberta.visitor.validate.AbstractConfigurationValidatorVisitor;
import de.fhg.iais.roberta.visitor.validate.UnoConfigurationValidatorVisitor;

public class UnoFactory extends AbstractArduinoFactory {

    public UnoFactory(PluginProperties pluginProperties) {
        super(pluginProperties);
    }

    @Override
    public AbstractConfigurationValidatorVisitor getRobotConfigurationCheckVisitor() {
        return new UnoConfigurationValidatorVisitor();
    }
}