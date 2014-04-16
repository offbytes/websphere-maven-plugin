package com.offbytes.websphere;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Created by Konrad on 2014-04-13.
 */
public abstract class AbstractGoal extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        handleDeprecated();
        validateParameters();
        executeGoal();
    }

    public abstract void executeGoal() throws MojoExecutionException, MojoFailureException;

    void handleDeprecated() {
    }

    void validateParameters() {
    }

}