package com.offbytes.websphere;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jenkinsci.plugins.websphere.services.deployment.Artifact;
import org.jenkinsci.plugins.websphere.services.deployment.WebSphereDeploymentService;

import java.io.File;
import java.util.HashMap;

/**
 * Goal which deploys WAR or EAR to WebSphere application server.
 *
 */
@Mojo(name = "deployWAS")
public class DeployOnWAS extends AbstractMojo {

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

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(property = "was.warContextPath")
    private String warContextPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        WebSphereDeploymentService service = new WebSphereDeploymentService();

        try {
            connect(service);
            Artifact artifact = createArtifact(service);
            stopArtifact(artifact.getAppName(), service);
            uninstallArtifact(artifact.getAppName(), service);
            deployArtifact(artifact, service);
            startArtifact(artifact.getAppName(), service);
        } catch (Exception e) {
            getLog().error("Error deploying to IBM WebSphere Application Server", e);
            throw new MojoExecutionException("Error deploying to IBM WebSphere Application Server", e);
        } finally {
            service.disconnect();
        }
    }

    private void deployArtifact(Artifact artifact, WebSphereDeploymentService service) throws Exception {
        getLog().info("Deploying New '" + artifact.getAppName() + "' to IBM WebSphere Application Server");
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(WebSphereDeploymentService.APPDEPL_JSP_RELOADENABLED, reloading);
        options.put(WebSphereDeploymentService.APPDEPL_PRECOMPILE_JSP, precompile);
        service.installArtifact(artifact, options);
    }

    private void uninstallArtifact(String appName, WebSphereDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            getLog().info("Uninstalling Old Application '" + appName + "'...");
            service.uninstallArtifact(appName);
        }
    }

    private void startArtifact(String appName, WebSphereDeploymentService service) throws Exception {
        if (autoStart) {
            getLog().info("Starting New Application '" + appName + "'...");
            service.startArtifact(appName);
        }
    }

    private void stopArtifact(String appName, WebSphereDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            getLog().info("Stopping Old Application '" + appName + "'...");
            service.stopArtifact(appName);
        }
    }

    private Artifact createArtifact(WebSphereDeploymentService service) {
        Artifact artifact = new Artifact();
        File artifactPath = getArtifactPath();

        if (project.getPackaging().equalsIgnoreCase("ear")) {
            artifact.setType(Artifact.TYPE_EAR);
        } else if (project.getPackaging().equalsIgnoreCase("war")) {
            artifact.setType(Artifact.TYPE_WAR);
        }
        artifact.setPrecompile(precompile);
        artifact.setSourcePath(artifactPath);
        artifact.setAppName(getAppName(artifact, service));
        if (artifact.getType() == Artifact.TYPE_WAR) {
            generateEAR(artifact, service);
        }
        return artifact;
    }

    private void connect(WebSphereDeploymentService service) throws Exception {
        getLog().info("Connecting to IBM WebSphere Application Server...");
        service.setConnectorType(connectorType);
        service.setHost(host);
        service.setPort(port);
        service.setUsername(username);
        service.setPassword(password);
        service.setKeyStoreLocation(new File(clientKeyFile));
        service.setKeyStorePassword(clientKeyPassword);
        service.setTrustStoreLocation(new File(clientTrustFile));
        service.setTrustStorePassword(clientTrustPassword);
        service.setTargetCell(cell);
        service.setTargetNode(node);
        service.setTargetServer(server);
        service.setWarContextPath(warContextPath);
        service.connect();
    }

    private String getAppName(Artifact artifact, WebSphereDeploymentService service) {
        if (artifact.getType() == Artifact.TYPE_EAR) {
            return service.getAppName(artifact.getSourcePath().getAbsolutePath());
        } else {
            String filename = artifact.getSourcePath().getName();
            return filename.substring(0, filename.lastIndexOf("."));
        }
    }

    private void generateEAR(Artifact artifact, WebSphereDeploymentService service) {
        getLog().info("Generating EAR For New Artifact: " + artifact.getAppName());
        File modified = new File(artifact.getSourcePath().getParent(), artifact.getAppName() + ".ear");
        service.generateEAR(artifact, modified, earLevel);
        artifact.setSourcePath(modified);
    }

    private File getArtifactPath() {
        return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + "." + project.getPackaging());
    }

}
