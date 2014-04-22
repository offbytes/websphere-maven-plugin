package com.offbytes.websphere.goals;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.offbytes.websphere.DeployOnWAS;
import com.offbytes.websphere.utils.EARGenerator;
import com.offbytes.websphere.utils.JavaFork;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jenkinsci.plugins.websphere.services.deployment.WebSphereDeploymentService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Goal which deploys WAR or EAR to WebSphere application server.
 *
 */
@Mojo(name = "deployWAS")
public class DeployOnWASGoal extends AbstractGoal {

    // WAS connection settings

    @Parameter(property = "was.host", required = true)
    private String host;

    @Parameter(property = "was.port", required = true, defaultValue = "8880")
    private String port;

    @Parameter(property = "connectorType", required = true, defaultValue = WebSphereDeploymentService.CONNECTOR_TYPE_SOAP)
    private String connectorType;

    // WAS authorization settings

    @Parameter(property = "was.username", required = true)
    private String username;

    @Parameter(property = "was.password", required = true)
    private String password;

    @Parameter(property = "was.clientKeyFile", required = true)
    private String clientKeyFile;

    @Parameter(property = "was.clientKeyPassword")
    private String clientKeyPassword;

    @Parameter(property = "was.clientTrustFile", required = true)
    private String clientTrustFile;

    @Parameter(property = "was.clientTrustPassword")
    private String clientTrustPassword;

    // WAS deployment settings

    @Parameter(property = "was.node", required = true)
    private String node;

    @Parameter(property = "was.cell", required = true)
    private String cell;

    @Parameter(property = "was.server", required = true)
    private String server;

    @Parameter(property = "was.earLevel", defaultValue = "5", required = true)
    private String earLevel;

    @Parameter(property = "was.autoStart", required = true, defaultValue = "true")
    private boolean autoStart;

    @Parameter(property = "was.precompile", required = true, defaultValue = "true")
    private boolean precompile;

    @Parameter(property = "was.reloading", required = true, defaultValue = "true")
    private boolean reloading;

    @Parameter(property = "was.warContextPath")
    private String warContextPath;

    @Parameter(property = "was.warPath")
    @Deprecated
    private String warPath;

    @Parameter(property = "was.warFile")
    private String warFile;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "${plugin.artifacts}")
    private List<Artifact> artifactList;

    @Override
    public void executeGoal() throws MojoExecutionException, MojoFailureException {
        try {
            File sourceFile = getArtifactPath();

            if (GoalUtils.findExtension(sourceFile).equals("war")) {
                sourceFile = generateEAR(sourceFile);
            }

            JavaFork javaFork = new JavaFork();
            javaFork.property("host", host)
                    .property("port", port)
                    .property("connectorType", connectorType)
                    .property("username", username)
                    .property("password", password)
                    .property("clientKeyFile", clientKeyFile)
                    .property("clientKeyPassword", clientKeyPassword)
                    .property("clientTrustFile", clientTrustFile)
                    .property("clientTrustPassword", clientTrustPassword)
                    .property("node", node)
                    .property("cell", cell)
                    .property("server", server)
                    .property("earLevel", earLevel)
                    .property("autoStart", Boolean.toString(autoStart))
                    .property("precompile", Boolean.toString(precompile))
                    .property("reloading", Boolean.toString(reloading))
                    .property("sourceFile", sourceFile.getPath())
                    .mainClass(DeployOnWAS.class)
                    .option("mx128m")
                    .option("X:MaxPermSize=128m")
                    .classpath(createPluginClassPath());

            if (javaFork.execute() != 0) {
                throw new MojoFailureException("Websphere Deployer error");
            }
        } catch (IOException e) {
            getLog().error("Unexpected error", e);
            throw new MojoExecutionException("Unexpected error", e);
        }
    }

    private String createPluginClassPath() {
        List<String> artifactPaths = new ArrayList<String>();
        for (Artifact artifact : artifactList) {
            artifactPaths.add(artifact.getFile().getPath());
        }

        return StringUtils.join(artifactPaths, File.pathSeparator);
    }

    @Override
    protected void handleDeprecated() {
        if (this.warPath != null && this.warFile == null) {
            this.warFile = this.warPath;
        }
    }

    @Override
    protected void validateParameters() {
        GoalUtils.fileExists("clientKeyFile", clientKeyFile);
        GoalUtils.fileExists("clientTrustFile", clientTrustFile);
        GoalUtils.fileExists("warFile", warFile);
    }

    private File generateEAR(File sourceFile) {
        String appName = getAppName(sourceFile);
        getLog().info("Generating EAR From " + sourceFile.getAbsolutePath() + " For New Artifact: " + appName);

        File modified = new File(sourceFile.getParent(), appName + ".ear");

        EARGenerator earGenerator = new EARGenerator();
        earGenerator.setSourceFile(sourceFile);
        earGenerator.setDestination(modified);
        earGenerator.setWarContextPath(warContextPath);
        earGenerator.setEarLevel(earLevel);
        earGenerator.generate();

        return modified;
    }

    private String getAppName(File sourceFile) {
        String filename = sourceFile.getName();
        return filename.substring(0, filename.lastIndexOf("."));
    }

    private File getArtifactPath() {
        if (warFile == null) {
            return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + "." + project.getPackaging());
        } else {
            return new File(warFile);
        }
    }

 }
