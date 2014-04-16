package com.offbytes.websphere;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Konrad on 2014-04-13.
 */
@Mojo(name = "testMojo")
public class TestPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${plugin.artifacts}")
    private List<Artifact> classpath;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File javaHome = new File(System.getProperty("java.home"));
        if (javaHome.getName().equalsIgnoreCase("jre")) {
            javaHome = javaHome.getParentFile();
        }

        List<String> classPathFiles = new ArrayList<String>();
        for (Artifact artifact : classpath) {
            classPathFiles.add(artifact.getFile().getPath());
        }

        try {
            DefaultExecutor defaultExecutor = new DefaultExecutor();
            CommandLine commandLine = new CommandLine(new File(new File(javaHome, "bin"), "java"));
            commandLine.addArgument("-classpath");
            commandLine.addArgument(StringUtils.join(classPathFiles, File.pathSeparator));
            commandLine.addArgument("com.offbytes.websphere.MainClass");
            System.out.println(StringUtils.join(classPathFiles, File.pathSeparator));
            defaultExecutor.execute(commandLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
