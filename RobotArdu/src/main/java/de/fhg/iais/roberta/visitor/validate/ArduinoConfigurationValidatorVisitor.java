package de.fhg.iais.roberta.visitor.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.util.dbc.DbcException;

public class ArduinoConfigurationValidatorVisitor extends AbstractConfigurationValidatorVisitor {

    private final List<String> freePins;
    private String incorrectPin;
    private String failingBlock;
    private Key resultKey;

    private boolean checkSuccess = false;

    int errorCount;

    public ArduinoConfigurationValidatorVisitor(List<String> pins) {
        this.freePins = pins;
    }

    public void checkConfigurationBlock(Map<String, String> componentProperties, /*Map<String, List<String>> inputToPinsMapping,*/ String blockType) {
        List<String> blockPins = new ArrayList<>();
        componentProperties
            .forEach(
                (k, v) -> {
                    //if ( inputToPinsMapping.containsKey(k) ) {
                    //List<String> allowedPins = inputToPinsMapping.get(k);
                    if ( /*!(allowedPins.contains(v) &&*/ !this.freePins.contains(v) ) {
                        //System.err.println("Pin " + v + " is not allowed for " + k + " input/output");
                        //block.addInfo(NepoInfo.error("CONFIGURATION_ERROR_ACTOR_MISSING"));
                        this.errorCount++;
                        this.incorrectPin = v;
                        this.failingBlock = blockType;
                        this.resultKey = Key.COMPILERWORKFLOW_ERROR_PROGRAM_GENERATION_FAILED_WITH_PARAMETERS;
                        throw new DbcException("Pin " + v + " is not allowed for " + k + " input/output");
                    } else {
                        blockPins.add(v);
                        this.freePins.removeIf(s -> s.equals(v));
                    }
                    /*} else {
                        System.err.println("Input not allowed " + k);
                        block.addInfo(NepoInfo.error("CONFIGURATION_ERROR_ACTOR_MISSING"));
                        errorCount++;
                    }*/
                });
        if ( blockPins.stream().distinct().count() != blockPins.size() ) {
            //System.err.println("Pins must be unique");
            //block.addInfo(NepoInfo.error("CONFIGURATION_ERROR_ACTOR_MISSING"));
            this.errorCount++;
            this.incorrectPin = "NON_UNIQUE";
            this.resultKey = Key.COMPILERWORKFLOW_ERROR_PROGRAM_GENERATION_FAILED_WITH_PARAMETERS;
            throw new DbcException("Pins must be unique");
        }
        this.checkSuccess = true;
    }

    @Override
    public void visit(Configuration configuration) {
        configuration.getConfigurationComponentsValues().forEach(v -> {
            checkConfigurationBlock(v.getComponentProperties(), /*inputToPinsMapping,*/ v.getComponentType());
        });
    }

    @Override
    public Map<String, String> getResult() {
        Map<String, String> result = new HashMap<>();
        result.put("BLOCK", this.failingBlock);
        result.put("PIN", this.incorrectPin);
        return result;
    }

    @Override
    public Key getResultKey() {
        return this.resultKey;
    }
}
