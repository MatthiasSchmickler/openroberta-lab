package de.fhg.iais.roberta.codegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.inter.mode.action.ILanguage;
import de.fhg.iais.roberta.transformer.BlocklyProgramAndConfigTransformer;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.util.PluginProperties;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.util.dbc.DbcException;
import de.fhg.iais.roberta.visitor.validate.IValidatorVisitor;

public abstract class AbstractCompilerWorkflow implements ICompilerWorkflow {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCompilerWorkflow.class);

    protected final PluginProperties pluginProperties;

    protected Key workflowResult = Key.COMPILERWORKFLOW_SUCCESS;
    protected String crosscompilerResponse = "";
    protected String generatedSourceCode = null;
    protected List<IValidatorVisitor<Void>> validators;

    public AbstractCompilerWorkflow(PluginProperties pluginProperties) {
        this.pluginProperties = pluginProperties;
    }

    @Override
    public void loadValidatorVisitors(Configuration configuration) {
        LOG.debug("Loading validators...");
        String validatorsPropertyEntry = this.pluginProperties.getStringProperty("robot.plugin.validators");
        if ( validatorsPropertyEntry == null || validatorsPropertyEntry.equals("") ) {
            // throw new DbcException("Program/Configuration validators not configured");
            LOG.debug("No validators present.");
            this.validators = null;
            return;
        }
        List<String> validatorNames = Stream.of(this.pluginProperties.getStringProperty("robot.plugin.validators").split(",")).collect(Collectors.toList());
        List<IValidatorVisitor<Void>> validators = new ArrayList<>();
        validatorNames.forEach(validatorName -> {
            LOG.debug("Loading validator " + validatorName);
            try {
                validators.add((IValidatorVisitor<Void>) Class.forName(validatorName).newInstance());
            } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | SecurityException e ) {
                e.printStackTrace();
                throw new DbcException(
                    "Provided validator is not a validator, please validate that your provided validator is a validator that can perform validation.");
            }
        });
        boolean methodFound = false;
        for ( IValidatorVisitor<Void> validator : validators ) {
            Method[] methods = validator.getClass().getDeclaredMethods();
            for ( Method method : methods ) {
                if ( method.getName().equals("visit") ) {
                    LOG.debug("Visit method found for " + validator.getClass().getName());
                    methodFound = true;
                }
            }
            if ( !methodFound ) {
                throw new DbcException("Visit method not found for validator " + validator.getClass().getName());
            }
        }
        this.validators = validators;
    }

    @Override
    public void generateSourceAndCompile(String token, String programName, BlocklyProgramAndConfigTransformer transformer, ILanguage language) {
        generateSourceCode(token, programName, transformer, language);
        if ( this.workflowResult == Key.COMPILERWORKFLOW_SUCCESS ) {
            compileSourceCode(token, programName, language, null);
        }
    }

    @Override
    public final Key getWorkflowResult() {
        return this.workflowResult;
    }

    @Override
    public final void setSourceCode(String sourceCode) {
        this.generatedSourceCode = sourceCode;
    }

    @Override
    public final String getGeneratedSourceCode() {
        return this.generatedSourceCode;
    }

    @Override
    public final String getCrosscompilerResponse() {
        return this.crosscompilerResponse;
    }

    /**
     * run a crosscompiler in a process of its own, store the compiler response in field crosscompilerResponse
     *
     * @param executableWithParameters
     * @return true, when the crosscompiler succeeds; false, otherwise
     */
    protected final boolean runCrossCompiler(String[] executableWithParameters) {
        int ecode = -1;
        try {
            ProcessBuilder procBuilder = new ProcessBuilder(executableWithParameters);
            procBuilder.redirectErrorStream(true);
            procBuilder.redirectInput(Redirect.INHERIT);
            procBuilder.redirectOutput(Redirect.PIPE);
            Process p = procBuilder.start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
            reader.lines().iterator().forEachRemaining(sj::add);
            this.crosscompilerResponse = sj.toString();
            ecode = p.waitFor();
            p.destroy();
        } catch ( Exception e ) {
            this.crosscompilerResponse = "exception when calling the cross compiler";
            LOG.error(this.crosscompilerResponse, e);
            ecode = -1;
        }
        LOG.error("DEBUG INFO: " + this.crosscompilerResponse);
        if ( ecode == 0 ) {
            return true;
        } else {
            LOG.error("compilation of program failed with message: \n" + this.crosscompilerResponse);
            return false;
        }
    }

    /**
     * run a crosscompiler in a process of its own, store the compiler response in field crosscompilerResponse
     *
     * @param executableWithParameters
     * @return the binary generated by the compiler; return null, if the compiler fails
     */
    protected final String getBinaryFromCrossCompiler(String[] executableWithParameters) {
        try {
            ProcessBuilder procBuilder = new ProcessBuilder(executableWithParameters);
            procBuilder.redirectErrorStream(true);
            procBuilder.redirectInput(Redirect.INHERIT);
            procBuilder.redirectOutput(Redirect.PIPE);
            Process p = procBuilder.start();
            String compiledHex = IOUtils.toString(p.getInputStream(), "US-ASCII");
            p.waitFor();
            p.destroy();
            this.crosscompilerResponse = "cross compilation successful";
            return compiledHex;
        } catch ( Exception e ) {
            this.crosscompilerResponse = "cross compiler could not be called: " + e.getMessage();
            LOG.error("exception when calling the cross compiler", e);
            return null;
        }
    }

    protected final void storeGeneratedProgram(String token, String programName, String ext) {
        try {
            String tempDir = this.pluginProperties.getTempDir();
            Assert.isTrue(token != null && programName != null && this.generatedSourceCode != null && this.workflowResult == Key.COMPILERWORKFLOW_SUCCESS);
            File sourceFile = new File(tempDir + token + "/" + programName + "/source/" + programName + ext);
            Path path = Paths.get(tempDir + token + "/" + programName + "/target/");
            try {
                Files.createDirectories(path);
                FileUtils.writeStringToFile(sourceFile, this.generatedSourceCode, StandardCharsets.UTF_8.displayName());
            } catch ( IOException e ) {
                String msg = "could not write source code to file system";
                LOG.error(msg, e);
                throw new DbcException(msg, e);
            }
            LOG.info("stored under: " + sourceFile.getPath());
        } catch ( Exception e ) {
            LOG.error("Storing the generated program " + programName + " into directory " + token + " failed", e);
            this.workflowResult = Key.COMPILERWORKFLOW_ERROR_PROGRAM_STORE_FAILED;
        }
    }

    protected final String getBase64Encoded(String path) {
        try {
            String compiledHex = FileUtils.readFileToString(new File(path), "UTF-8");
            final Base64.Encoder urec = Base64.getEncoder();
            compiledHex = urec.encodeToString(compiledHex.getBytes());
            return compiledHex;
        } catch ( IOException e ) {
            LOG.error("Exception when reading the compiled code from " + path, e);
            return null;
        }
    }
}
