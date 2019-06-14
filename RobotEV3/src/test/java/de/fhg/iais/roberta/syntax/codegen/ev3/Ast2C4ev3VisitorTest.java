package de.fhg.iais.roberta.syntax.codegen.ev3;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.util.test.ev3.HelperEv3ForXmlTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Ast2C4ev3VisitorTest {


    private static final String CONSTANTS_AND_IMPORTS =
        "" //
            + "#define WHEEL_DIAMETER 5.6\n"
            + "#define TRACK_WIDTH 18.0\n"
            + "#include <ev3.h>\n"
            + "#include <math.h>\n"
            + "#include <list>\n"
            + "#include \"NEPODefs.h\"\n";

    private static final String CONSTANTS_AND_IMPORTS__WITH_SMALLER_TRACK_WIDTH =
        "" //
            + "#define WHEEL_DIAMETER 5.6\n"
            + "#define TRACK_WIDTH 17.0\n"
            + "#include <ev3.h>\n"
            + "#include <math.h>\n"
            + "#include <list>\n"
            + "#include \"NEPODefs.h\"\n";

    private static final String MAIN_INIT_EV3 =
        "" //
            + "int main() {\n"
            + "    InitEV3();\n";

    private static final String BEGIN_MAIN_DEFAULT =
        "" //
            + MAIN_INIT_EV3
            + "    setAllSensorMode(DEFAULT_MODE_TOUCH, DEFAULT_MODE_GYRO, DEFAULT_MODE_COLOR, DEFAULT_MODE_ULTRASONIC);\n\n";

    private static final String BEGIN_MAIN__TOUCH_ULTRASONIC_COLOR =
        "" //
            + MAIN_INIT_EV3
            + "    setAllSensorMode(DEFAULT_MODE_TOUCH, DEFAULT_MODE_ULTRASONIC, DEFAULT_MODE_COLOR, NO_SEN);\n\n";

    private static final String BEGIN_MAIN__TOUCH_ULTRASONIC_COLOR_ULTRASONIC =
        "" //
            + MAIN_INIT_EV3
            + "    setAllSensorMode(DEFAULT_MODE_TOUCH, DEFAULT_MODE_ULTRASONIC, DEFAULT_MODE_COLOR, DEFAULT_MODE_ULTRASONIC);\n\n";

    private static final String BEGIN_MAIN__TOUCH_GYRO_INFRARED_ULTRASONIC =
        "" //
            + MAIN_INIT_EV3
            + "    setAllSensorMode(DEFAULT_MODE_TOUCH, DEFAULT_MODE_GYRO, DEFAULT_MODE_INFRARED, DEFAULT_MODE_ULTRASONIC);\n\n";


    private static final String END_MAIN =
        "" //
            + "    \n"
            + "    FreeEV3();\n"
            + "    return 0;\n"
            + "}\n";

    private static Configuration standardBrickConfiguration = HelperEv3ForXmlTest.makeStandardEv3DevConfiguration();

    private final HelperEv3ForXmlTest helper = new HelperEv3ForXmlTest();


    @Test
    public void testLcdText() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + BEGIN_MAIN_DEFAULT
                + "DrawString(\"Hallo\", 0, 3);\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator.xml", expectedCode);
    }

    @Test
    public void testLcdTextInForLoop() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + BEGIN_MAIN_DEFAULT
                + "for (float ___k0 = 0; ___k0 < 10; ___k0 += 1) {\n"
                + "    DrawString(\"Hallo\", 0, 3);\n"
                + "}\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator1.xml", expectedCode);
    }

    @Test
    public void testIfPressedLedElseLcdPictureThenPlaySoundAndMove() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS__WITH_SMALLER_TRACK_WIDTH
                + BEGIN_MAIN__TOUCH_ULTRASONIC_COLOR
                + "if ( readSensor(IN_1) ) {\n"
                + "    SetLedPattern(LED_GREEN);\n"
                + "} else if ( INPUT_REDCOLOR == ReadSensorInMode(IN_3, COL_COLOR) ) {\n"
                + "    while ( true ) {\n"
                + "        LcdPicture(LCD_COLOR_BLACK, 0, 0, EYESOPEN);\n"
                + "        OnFwdReg(OUT_B, Speed(30));\n"
                + "    }\n"
                + "}\n"
                + "PlaySystemSound(SOUND_DOUBLE_BEEP);\n"
                + "SetVolume(50);\n"
                + "for ( float ___i = 1; ___i < 10; ___i += 1 ) {\n"
                + "    RotateMotorForAngle(OUT_B, Speed(30), 360 * 1);\n"
                + "}\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator2.xml", expectedCode, HelperEv3ForXmlTest.makeTouchUltrasonicColorConfiguration());
    }


    @Test
    public void testIfPressedLedElseIfPressedLedElseIfNearLcdPictureElseMove () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS__WITH_SMALLER_TRACK_WIDTH
                + BEGIN_MAIN__TOUCH_ULTRASONIC_COLOR_ULTRASONIC
                + "if ( readSensor(IN_1) ) {\n"
                + "    SetLedPattern(LED_GREEN);\n"
                + "} else {\n"
                + "    if ( readSensor(IN_1) ) {\n"
                + "        SetLedPattern(LED_GREEN);\n"
                + "    } else if ( 0 == ReadSensorInMode(IN_4, US_DIST_CM) ) {\n"
                + "        LcdPicture(LCD_COLOR_BLACK, 0, 0, FLOWERS);\n"
                + "    } else {\n"
                + "        while ( !ButtonIsDown(BTNUP) ) {\n"
                + "            OnFwdReg(OUT_B, Speed(30));\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator3.xml", expectedCode, HelperEv3ForXmlTest.makeTouchUltrasonicColorUltrasonicConfiguration());
    }

    @Test
    public void testIfNearRotateThenIfTachoThenLedElsePictureThenLed () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS__WITH_SMALLER_TRACK_WIDTH
                + BEGIN_MAIN__TOUCH_GYRO_INFRARED_ULTRASONIC
                + "if ( 5 < MotorPower(OUT_B) ) {\n"
                + "    OnFwdReg(OUT_B, Speed(30));\n"
                + "    RotateMotorForAngle(OUT_B, Speed(30), 360 * 1);\n"
                + "    OnFwdSyncEx(OUT_AB, Speed(50), 100, RESET_NONE);"
                + "}\n"
                + "if ( (((MotorRotationCount(OUT_A) / 360.0) + ReadSensorInMode(IN_3, IR_PROX))) == ReadSensorInMode(IN_4, US_DIST_CM) ) {\n"
                + "    SetLedPattern(LED_BLACK);\n"
                + "} else {\n"
                + "    ResetGyroSensor(IN_2);\n"
                + "    while ( readSensor(IN_1) ) {\n"
                + "        LcdPicture(LCD_COLOR_BLACK, 0, 0, OLDGLASSES);\n"
                + "        LcdClean();\n"
                + "    }\n"
                + "    SetLedPattern(LED_GREEN);\n"
                + "}\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator4.xml", expectedCode, HelperEv3ForXmlTest.makeTouchGyroInfraredUltrasonic());
    }


    @Test
    public void testMotorsAndSound () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + BEGIN_MAIN_DEFAULT
                + "OnFwdReg(OUT_B, Speed(0));\n"
                + "RotateMotorForAngle(OUT_B, Speed(30), 360 * 0);\n"
                + "OnFwdSyncEx(OUT_BC, Speed(0), 100, RESET_NONE);\n"
                + "SetVolume(50);\n"
                + "PlayToneEx(0, 0, GetVolume());\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator5.xml", expectedCode);
    }

    @Test
    public void testLcdTextAndSound() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + BEGIN_MAIN_DEFAULT
                + "DrawString(\"Hallo\", 0, 0);\n"
                + "PlayToneEx(300, 3000, GetVolume());"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator6.xml", expectedCode);
    }


    @Test
    public void testMotorAction() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + BEGIN_MAIN_DEFAULT
                + "OnFwdReg(OUT_B, Speed(30));\n"
                + "RotateMotorForAngle(OUT_B, Speed(30), 360 * 1);\n"
                + END_MAIN;

        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator7.xml", expectedCode);
    }


    @Test
    public void testVariablesAndLcdText() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "double ___item = 10;\n"
                + "String ___item2 = \"TTTT\";\n"
                + "bool ___item3 = true;\n"
                + BEGIN_MAIN_DEFAULT
                + "DrawNumber(___item, 0, 0);\n"
                + "DrawString(___item2, 0, 0);\n"
                + "DrawBool(___item3, 0, 0);\n"
                + "___item3 = false;\n"
                + END_MAIN;

        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator8.xml", expectedCode);
    }

    // TODO: Test drawColorName

    @Test
    public void testMoveAndLcdPicture() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "double ___variablenName = 0;\n"
                + BEGIN_MAIN_DEFAULT
                + "OnFwdSync(OUT_BC, Speed(50));\n"
                + "LcdPicture(LCD_COLOR_BLACK,0,0,OLDGLASSES);\n"
                + END_MAIN;

        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator9.xml", expectedCode);
    }


    @Test
    public void testVariables() throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "double ___item = 0;\n"
                + "String ___item2 = \"ss\";\n"
                + "bool ___item3 = true;\n"
                + "std::list<double> ___item4 = {1, 2, 3};\n"
                + "std::list<String> ___item5 = {\"a\", \"b\"};\n"
                + "std::list<bool> ___item6 = {true, false};\n"
                + "std::list<unsigned int> ___item7 = {INPUT_REDCOLOR, INPUT_BLACKCOLOR, INPUT_NULLCOLOR};\n"
                + "unsigned int ___item8 = INPUT_NULLCOLOR;\n"
                + BEGIN_MAIN_DEFAULT
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/task/task_mainTask.xml", expectedCode);
    }

    @Test
    public void testMoveAndDefineAndCallFunction () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "void macheEtwas(double ___x, double ___x2) {\n"
                + "    LcdPicture(LCD_COLOR_BLACK, 0, 0, OLDGLASSES);\n"
                + "}\n"
                + BEGIN_MAIN_DEFAULT
                + "RotateMotorForAngle(OUT_B, Speed(30), 360 * 1);\n"
                + "macheEtwas(10, 10);\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/methods/method_void_1.xml", expectedCode);
    }


    @Test
    public void testVoidFunction () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "void test() {\n"
                + "    SetLedPattern(LED_GREEN);\n"
                + "}\n"
                + BEGIN_MAIN_DEFAULT
                + "test();\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/methods/method_void_2.xml", expectedCode);
    }


    @Test
    public void testVoidFunctionIfTrueReturn () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "void test(bool ___x) {\n"
                + "    if (___x) return;"
                + "    SetLedPattern(LED_GREEN);\n"
                + "}\n"
                + BEGIN_MAIN_DEFAULT
                + "test(true);\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/methods/method_if_return_1.xml", expectedCode);
    }

    @Test
    public void testMultipleVoidFunctions () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "double ___variablenName = 0;\n"
                + "bool ___variablenName2 = true;\n"
                + "void test1(double ___x, double ___x2) {\n"
                + "    DrawString(\"Hallo\", ___x, ___x2);\n"
                + "}\n"
                + "void test2 () {\n"
                + "    if (___variablenName2) return;\n"
                + "    SetLedPattern(LED_GREEN);\n"
                + "}\n"
                + BEGIN_MAIN_DEFAULT
                + "test1(0, 0);\n"
                + "test2();"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/methods/method_void_3.xml", expectedCode);
    }

    @Test
    public void testFunctionReturnsDouble () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "std::list<String> ___variablenName = {\"a\", \"b\", \"c\"};\n"
                + "double test(double ___x, std::list<String> ___x2) {\n"
                + "    DrawString(___x2, ___x, 0);"
                + "    return ___x;\n"
                + "}\n"
                + BEGIN_MAIN_DEFAULT
                + "DrawString(test(0, ___variablenName), 0, 0);\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/methods/method_return_1.xml", expectedCode);
    }

    @Test
    public void testFunctionReturnsColor () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "std::list<String> ___variablenName = {\"a\", \"b\", \"c\"};\n"
                + "unsigned int test() {\n"
                + "    DrawString(___variablenName, 0, 0);"
                + "    return INPUT_NULLCOLOR;\n"
                + "}\n"
                + BEGIN_MAIN_DEFAULT
                + "DrawString(test(), 0, 0);\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/methods/method_return_2.xml", expectedCode);
    }


    @Test
    public void testFunctionReturnsRedOrNullColor () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "std::list<String> ___variablenName = {\"a\", \"b\", \"c\"};\n"
                + "unsigned int test() {\n"
                + "    if (true) return INPUT_REDCOLOR;\n"
                + "    DrawString(___variablenName, 0, 0);"
                + "    return INPUT_NULLCOLOR;\n"
                + "}\n"
                + BEGIN_MAIN_DEFAULT
                + "DrawString(test(), 0, 0);\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/methods/method_if_return_2.xml", expectedCode);
    }


    @Test
    public void testCompareString () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "String ___message = \"exit\";\n"
                + BEGIN_MAIN_DEFAULT
                + "if (___message == \"exit\") {\n"
                + "    DrawString(\"done\", 0, 0);\n"
                + "}\n"
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/stmt/if_stmt4.xml", expectedCode);
    }

    @Test
    public void testDeclareTwoVariables () throws Exception {
        String expectedCode =
            "" //
                + CONSTANTS_AND_IMPORTS
                + "double ___item;\n"
                + "String ___item2 = \"cc\";\n"
                + BEGIN_MAIN_DEFAULT
                + END_MAIN;
        checkCodeGeneratorForInput("/syntax/code_generator/java/java_code_generator11.xml", expectedCode);
    }

    private void checkCodeGeneratorForInput(String fileName, String expectedSourceCode) throws Exception {
        checkCodeGeneratorForInput(fileName, expectedSourceCode, standardBrickConfiguration);
    }

    private void checkCodeGeneratorForInput(String fileName, String expectedSourceCode, Configuration configuration) throws Exception {
        String generatedSourceCode = helper.generateC4ev3(fileName, configuration);
        assertEquals(removeSpaces(expectedSourceCode), removeSpaces(generatedSourceCode));
    }

    private static String removeSpaces (String string) {
        return string.replaceAll("\\s+", "");
    }

}
