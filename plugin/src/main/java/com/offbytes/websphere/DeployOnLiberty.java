package com.offbytes.websphere;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jenkinsci.plugins.websphere.services.deployment.Artifact;
import org.jenkinsci.plugins.websphere.services.deployment.LibertyDeploymentService;

import java.io.File;
import java.util.HashMap;

import static com.offbytes.websphere.GoalUtils.fileExists;

/**
 * Created by Konrad on 2014-04-13.
 */
@Mojo(name = "deployLiberty")
public class DeployOnLiberty extends AbstractGoal {

    @Parameter(property = "liberty.host", required = true)
    private String ipAddress;

    @Parameter(property = "liberty.port", required = true)
    private String port;

    @Parameter(property = "liberty.userName", required = true)
    private String username;

    @Parameter(property = "liberty.password", required = true)
    private String password;

    @Parameter(property = "liberty.clientTrustFile", required = true)
    private String clientTrustFile;

    @Parameter(property = "liberty.clientTrustPassword", required = true)
    private String clientTrustPassword;

    @Parameter(property = "liberty.warFile", required = false)
    private String warFile;

    @Parameter(property = "liberty.warFile", required = false)
    @Deprecated
    private String warPath;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void executeGoal() throws MojoExecutionException, MojoFailureException {
        LibertyDeploymentService service = new LibertyDeploymentService();
        try {
            connect(service);
            Artifact artifact = createArtifact();
            stopArtifact(artifact.getAppName(), service);
            uninstallArtifact(artifact.getAppName(), service);
            deployArtifact(artifact, service);
            Thread.sleep(2000); //wait 2 seconds for deployment to settle
            startArtifact(artifact.getAppName(), service);
        } catch (Exception e) {
            getLog().error("Error deploying to IBM WebSphere Application Server", e);
            throw new MojoExecutionException("Error deploying to IBM WebSphere Application Server", e);
        } finally {
            try {
                disconnect(service);
            } catch (Exception e) {
                getLog().error("Error deploying to IBM WebSphere Application Server", e);
            }
        }
    }

    @Override
    void handleDeprecated() {
        if (this.warPath != null && this.warFile == null) {
            this.warFile = this.warPath;
        }
    }

    @Override
    void validateParameters() {
        fileExists("clientTrustFile", clientTrustFile);
        fileExists("warFile", warFile);
    }

    private Artifact createArtifact() {
        Artifact artifact = new Artifact();
        File artifactPath = getArtifactPath();
        String artifactExtension = GoalUtils.findExtension(artifactPath);

        if (artifactExtension.equalsIgnoreCase("ear")) {
            artifact.setType(Artifact.TYPE_EAR);
        } else if (artifactExtension.equalsIgnoreCase("war")) {
            artifact.setType(Artifact.TYPE_WAR);
        } else if (artifactExtension.equalsIgnoreCase("rar")) {
            artifact.setType(Artifact.TYPE_RAR);
        } else if (artifactExtension.equalsIgnoreCase("jar")) {
            artifact.setType(Artifact.TYPE_JAR);
        }

        artifact.setSourcePath(artifactPath);
        artifact.setAppName(artifactPath.getName());
        return artifact;
    }

    private void connect(LibertyDeploymentService service) throws Exception {
        getLog().info("Connecting to IBM WebSphere Liberty Profile...");
        service.setHost(ipAddress);
        service.setPort(port);
        service.setUsername(username);
        service.setPassword(password);
        service.setTrustStoreLocation(new File(clientTrustFile));
        service.setTrustStorePassword(clientTrustPassword);
        service.connect();
    }

    private void disconnect(LibertyDeploymentService service) throws Exception {
        getLog().info("Disconnecting from IBM WebSphere Liberty Profile...");
        service.disconnect();
    }

    private void stopArtifact(String appName, LibertyDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            getLog().info("Stopping Old Application '" + appName + "'...");
            service.stopArtifact(appName);
        }
    }

    private void uninstallArtifact(String appName, LibertyDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            getLog().info("Uninstalling Old Application '" + appName + "'...");
            service.uninstallArtifact(appName);
        }
    }

    private void deployArtifact(Artifact artifact, LibertyDeploymentService service) throws Exception {
        getLog().info("Deploying New '" + artifact.getAppName() + "' to IBM WebSphere Liberty Profile");
        service.installArtifact(artifact, new HashMap<String, Object>());
    }

    private void startArtifact(String appName, LibertyDeploymentService service) throws Exception {
        getLog().info("Starting New Application '" + appName + "'...");
        service.startArtifact(appName);
    }

    private File getArtifactPath() {
        if (warFile == null) {
            return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + "." + project.getPackaging());
        } else {
            return new File(warFile);
        }
    }
}
