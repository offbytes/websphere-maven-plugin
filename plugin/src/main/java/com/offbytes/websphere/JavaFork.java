package com.offbytes.websphere;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Konrad on 2014-04-14.
 */
public class JavaFork {

    private final File javaFile;
    private CommandLine commandLine;
    private DefaultExecutor defaultExecutor = new DefaultExecutor();
    private String mainClass;
    private List<String> arguments = new ArrayList<String>();
    private ByteArrayOutputStream outAndErr;

    public JavaFork() {
        this.javaFile = findJavaExecutable();
        this.commandLine = new CommandLine(this.javaFile);
    }

    public JavaFork property(String name, String value) {
        commandLine.addArgument("-D" + name + "=" + value);
        return this;
    }

    public JavaFork classpath(String classpath) {
        commandLine.addArgument("-classpath");
        commandLine.addArgument(classpath);
        return this;
    }

    public JavaFork mainClass(String className) {
        this.mainClass = className;
        return this;
    }

    public JavaFork workingDirectory(String path) {
        defaultExecutor.setWorkingDirectory(new File(path));
        return this;
    }

    public JavaFork argument(String argument) {
        arguments.add(argument);
        return this;
    }

    public String getOutput() {
        if (outAndErr != null) {
            return outAndErr.toString();
        } else {
            return null;
        }
    }

    public int execute() throws IOException {
        commandLine.addArgument(mainClass);
        commandLine.addArguments(arguments.toArray(new String[arguments.size()]));
        outAndErr = new ByteArrayOutputStream();
        defaultExecutor.setStreamHandler(new PumpStreamHandler(outAndErr));
        defaultExecutor.setExitValues(null);
        return defaultExecutor.execute(commandLine);
    }

    private File findJavaExecutable() {
        File javaHome = new File(System.getProperty("java.home"));
        if (javaHome.getName().equalsIgnoreCase("jre")) {
            javaHome = javaHome.getParentFile();
        }

        return new File(new File(javaHome, "bin"), "java");
    }
}
