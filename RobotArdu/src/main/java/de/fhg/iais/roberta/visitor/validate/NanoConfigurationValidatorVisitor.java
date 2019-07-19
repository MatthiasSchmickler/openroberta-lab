package de.fhg.iais.roberta.visitor.validate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NanoConfigurationValidatorVisitor extends ArduinoConfigurationValidatorVisitor {
    private final static List<String> freePins =
        Stream.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "A0", "A1", "A2", "A3", "A4", "A5").collect(Collectors.toList());

    public NanoConfigurationValidatorVisitor() {
        super(freePins);
    }
}
