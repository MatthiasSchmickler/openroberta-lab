package de.fhg.iais.roberta.components;

import com.google.common.collect.Lists;
import de.fhg.iais.roberta.util.dbc.DbcException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EdisonConfiguration extends Configuration {

    private static final Configuration config;

    static {

        ConfigurationComponent leftMotor = new ConfigurationComponent("leftmotor", true, "LMOTOR", "Left Motor", Collections.emptyMap());
        ConfigurationComponent rightMotor = new ConfigurationComponent("rightmotor", true, "RMOTOR", "Right Motor", Collections.emptyMap());
        ConfigurationComponent leftLED = new ConfigurationComponent("leftled", true, "LLED", "Left LED", Collections.emptyMap());
        ConfigurationComponent rightLED = new ConfigurationComponent("rightled", true, "RLED", "Right LED", Collections.emptyMap());
        ConfigurationComponent irLED = new ConfigurationComponent("infraredled", false, "IRLED", "IR LED", Collections.emptyMap());
        ConfigurationComponent obstacleDetector = new ConfigurationComponent("OBSTACLEDETECTOR", false, "OBSTACLEDETECTOR", "OBSTACLEDETECTOR", Collections.emptyMap());
        ConfigurationComponent lineTracker = new ConfigurationComponent("linetracker", false, "LINETRACKER", "Line Tracker", Collections.emptyMap());
        ConfigurationComponent leftLight = new ConfigurationComponent("LLIGHT", false, "LLIGHT", "LLIGHT", Collections.emptyMap());
        ConfigurationComponent rightLight = new ConfigurationComponent("rightlight", false, "RLIGHT", "Right Light Sensor", Collections.emptyMap());
        ConfigurationComponent sound = new ConfigurationComponent("sound", true, "SOUND", "Sound Sensor", Collections.emptyMap());
        ConfigurationComponent playButton = new ConfigurationComponent("play", true, "PLAY", "Play Button", Collections.emptyMap());
        ConfigurationComponent recordButton = new ConfigurationComponent("record", true, "REC", "Record Button", Collections.emptyMap());

        ArrayList<ConfigurationComponent> components = Lists.newArrayList(
            leftMotor,
            rightMotor,
            leftLED,
            rightLED,
            irLED,
            obstacleDetector,
            lineTracker,
            leftLight,
            rightLight,
            sound,
            playButton,
            recordButton);

        config = new Configuration.Builder().addComponents(components).build();
    }

    public EdisonConfiguration(Collection<ConfigurationComponent> configurationComponents, float wheelDiameterCM, float trackWidthCM) {
        super(configurationComponents, wheelDiameterCM, trackWidthCM);
    }

    public static class Builder extends Configuration.Builder {

        @Override
        public Builder addComponents(List<ConfigurationComponent> components) {
            throw new DbcException("Unsupported operation!");
        }

        @Override
        public Builder setWheelDiameter(float wheelDiameter) {
            throw new DbcException("Unsupported operation!");
        }

        @Override
        public Builder setTrackWidth(float trackWidth) {
            throw new DbcException("Unsupported operation!");
        }

        @Override
        public Configuration build() {
            return config;
        }
    }
}
