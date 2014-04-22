package com.offbytes.websphere.goals;

import com.offbytes.websphere.DeployOnLiberty;
import com.offbytes.websphere.DeployOnWAS;
import com.offbytes.websphere.utils.JavaFork;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jenkinsci.plugins.websphere.services.deployment.Artifact;
import org.jenkinsci.plugins.websphere.services.deployment.LibertyDeploymentService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Konrad on 2014-04-13.
 */
@Mojo(name = "deployLiberty")
public class DeployOnLibertyGoal extends AbstractGoal {

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

    @Parameter(defaultValue = "${plugin.artifacts}")
    private List<org.apache.maven.artifact.Artifact> artifactList;

    @Override
    public void executeGoal() throws MojoExecutionException, MojoFailureException {
        try {
            File sourceFile = getArtifactPath();

            JavaFork javaFork = new JavaFork();
            javaFork.property("host", ipAddress)
                    .property("port", port)
                    .property("username", username)
                    .property("password", password)
                    .property("clientTrustFile", clientTrustFile)
                    .property("clientTrustPassword", clientTrustPassword)
                    .property("sourceFile", sourceFile.getPath())
                    .mainClass(DeployOnLiberty.class)
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
        for (org.apache.maven.artifact.Artifact artifact : artifactList) {
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
        GoalUtils.fileExists("clientTrustFile", clientTrustFile);
        GoalUtils.fileExists("warFile", warFile);
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

    private File getArtifactPath() {
        if (warFile == null) {
            return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + "." + project.getPackaging());
        } else {
            return new File(warFile);
        }
    }
}
