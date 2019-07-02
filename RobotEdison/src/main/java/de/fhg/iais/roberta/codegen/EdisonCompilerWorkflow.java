package de.fhg.iais.roberta.codegen;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.factory.IRobotFactory;
import de.fhg.iais.roberta.inter.mode.action.ILanguage;
import de.fhg.iais.roberta.transformer.BlocklyProgramAndConfigTransformer;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.util.PluginProperties;
import de.fhg.iais.roberta.visitor.codegen.EdisonPythonVisitor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

/**
 * The workflow for the Edison compiler. Blockly blocks are first converted into EdPy Python2 code and then the code is converted into a WAV audio file.
 * See also: https://github.com/Bdanilko/EdPy
 */
public class EdisonCompilerWorkflow extends AbstractCompilerWorkflow {

    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(EdisonCompilerWorkflow.class);
    private String compiledWav = null;

    /**
     * Constructor, calls super() constructor
     *
     * @param pluginProperties the Edison's plugins properties, from edison.properties //TODO-MAX Fact check
     */
    public EdisonCompilerWorkflow(PluginProperties pluginProperties) {
        super(pluginProperties);
    }

    /**
     * Generates the Python source code of the Blockly program
     *
     * @param token       the credential supplied by the user. Needed to provide a unique directory name for crosscompilation
     * @param programName name of the program
     * @param transformer holding program and configuration. Require, that <code>transformer.getErrorMessage() == null</code>
     * @param language    locale to be used for messages
     */
    @Override public void generateSourceCode(
        String token, String programName, BlocklyProgramAndConfigTransformer transformer, ILanguage language) {
        if ( transformer.getErrorMessage() != null ) {
            this.workflowResult = Key.COMPILERWORKFLOW_ERROR_PROGRAM_TRANSFORM_FAILED;
        }

        try {
            this.generatedSourceCode =
                EdisonPythonVisitor.generate(transformer.getRobotConfiguration(), transformer.getProgramTransformer().getTree(), true, language);

            LOG.info("Edison code generated.");

        } catch ( Exception e ) {
            LOG.error("Edison code generation failed", e);
            this.workflowResult = Key.COMPILERWORKFLOW_ERROR_CONFIGURATION_TRANSFORM_FAILED;
        }

    }

    /**
     * Stores the generated program into a directory
     *
     * @param token        the credential supplied by the user. Needed to provide a unique directory name for crosscompilation
     * @param programName  name of the program
     * @param language     locale for messages
     * @param flagProvider TODO
     */
    @Override public void compileSourceCode(String token, String programName, ILanguage language, Object flagProvider) {
        try {
            storeGeneratedProgram(token, programName, ".py");
        } catch ( Exception e ) {
            LOG.error("Storing the generated program into directory " + token + " failed", e);
            return;
        }

        this.workflowResult = this.runBuild(token, programName);
    }

    /**
     * Generates the brick configuration. Since the Edison cannot be configured (all sensors/motors are fixed) we only need one standard configuration.
     *
     * @param factory    the generator factory (ignored)
     * @param blocklyXml the configuration XML as String (ignored)
     * @return the one standard configuration
     * @throws Exception if the Builder fails for whatever reason
     */
    @Override public Configuration generateConfiguration(IRobotFactory factory, String blocklyXml) throws Exception {
        return new Configuration.Builder().build();
        //TODO-MAX wenn V1 und V2 beide unterschiedlich sind: bei Calliope gucken was passiert
    }

    /**
     * Gets the WAV file as a Base64 encoded String
     *
     * @return the file as a String
     */
    @Override public String getCompiledCode() {
        return this.compiledWav;
    }

    /**
     * Builds the WAV file from the .py source file using the EdPy Python2 compiler (https://github.com/Bdanilko/EdPy) by starting an external Python2 process.
     * The file will be stored as {@link PluginProperties#getTempDir()}/token/source/tokXXXXX.wav and also in {@link #compiledWav} as a Base64 String.
     *
     * @param token  the credential supplied by the user. Needed to provide a unique temporary directory name
     * @param pyFile the source file name
     * @return a Key that gives information about the building process (success, failure, interrupted,...)
     */
    private Key runBuild(String token, String pyFile) {

        //get all directories
        String compilerDir = this.pluginProperties.getCompilerResourceDir();
        String sourceFilePath = this.pluginProperties.getTempDir() + "/" + token + "/" + pyFile + "/source/";
        String wavFileName = null;

        //build and start the Python process
        ProcessBuilder
            processBuilder =
            new ProcessBuilder("/usr/bin/python2", compilerDir + "EdPy.py", compilerDir + "en_lang.json", sourceFilePath + pyFile + ".py");

        try {
            Process p = processBuilder.start();
            int exitCode = p.waitFor();


            //get the correct WAV file after the Python process finished successfully
            System.err.println("/usr/bin/python2: Exit code " + exitCode);

            if ( exitCode != 0 ) {
                return Key.COMPILERWORKFLOW_ERROR_PROGRAM_COMPILE_FAILED;
            }

            File tempDir = new File(sourceFilePath);
            File[] list = tempDir.listFiles();

            if ( list.length <= 0 ) {
                LOG.error("Cannot find WAV target file");
                return Key.COMPILERWORKFLOW_ERROR_PROGRAM_COMPILE_FAILED;
            }

            for ( File f : list ) { //WAV target files have a random name inside the temp directory
                if ( f.getName().startsWith("tok") && f.getName().endsWith(".wav") ) {
                    wavFileName = f.getName();
                    break;
                }
            }

            //cast the file to a String using the Base64 encoder
            byte[] wavBytes = FileUtils.readFileToByteArray(new File(sourceFilePath + "/" + wavFileName));
            this.compiledWav = Base64.getEncoder().encodeToString(wavBytes);

        } catch ( IOException e ) { //if process cannot be started
            LOG.error("Exception when preparing the build", e);
            return Key.COMPILERWORKFLOW_ERROR_PROGRAM_COMPILE_FAILED;
        } catch ( InterruptedException e ) { //if process is interrupted
            LOG.error("Build interrupted", e);
            return Key.COMPILERWORKFLOW_ERROR_PROGRAM_COMPILE_FAILED;
        }

        return Key.COMPILERWORKFLOW_SUCCESS;
    }
}