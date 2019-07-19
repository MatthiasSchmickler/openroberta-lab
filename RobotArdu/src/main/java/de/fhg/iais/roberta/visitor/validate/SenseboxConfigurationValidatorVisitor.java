package de.fhg.iais.roberta.visitor.validate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SenseboxConfigurationValidatorVisitor extends ArduinoConfigurationValidatorVisitor {
    private final static List<String> freePins = Stream.of("0", "1", "2", "3", "4", "5", "6").collect(Collectors.toList());

    public SenseboxConfigurationValidatorVisitor() {
        super(freePins);
    }
}
