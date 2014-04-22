package com.offbytes.websphere;

import org.jenkinsci.plugins.websphere.services.deployment.Artifact;
import org.jenkinsci.plugins.websphere.services.deployment.LibertyDeploymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Konrad on 2014-04-22.
 */
public class DeployOnLiberty {

    private static Logger logger = LoggerFactory.getLogger(DeployOnLiberty.class);

    private String ipAddress;
    private String port;
    private String username;
    private String password;
    private String clientTrustFile;
    private String clientTrustPassword;
    private String sourceFile;

    public void deploy() throws Exception {
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
            logger.error("Error deploying to IBM WebSphere Application Server", e);
            throw e;
        } finally {
            try {
                disconnect(service);
            } catch (Exception e) {
                logger.error("Error deploying to IBM WebSphere Application Server", e);
                throw e;
            }
        }
    }

    private Artifact createArtifact() {
        Artifact artifact = new Artifact();
        File artifactPath = new File(sourceFile);
        String artifactExtension = findExtension(artifactPath);

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
        logger.info("Connecting to IBM WebSphere Liberty Profile...");
        service.setHost(ipAddress);
        service.setPort(port);
        service.setUsername(username);
        service.setPassword(password);
        service.setTrustStoreLocation(new File(clientTrustFile));
        service.setTrustStorePassword(clientTrustPassword);
        service.connect();
    }

    private void disconnect(LibertyDeploymentService service) throws Exception {
        logger.info("Disconnecting from IBM WebSphere Liberty Profile...");
        service.disconnect();
    }

    private void stopArtifact(String appName, LibertyDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            logger.info("Stopping Old Application '" + appName + "'...");
            service.stopArtifact(appName);
        }
    }

    private void uninstallArtifact(String appName, LibertyDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            logger.info("Uninstalling Old Application '" + appName + "'...");
            service.uninstallArtifact(appName);
        }
    }

    private void deployArtifact(Artifact artifact, LibertyDeploymentService service) throws Exception {
        logger.info("Deploying New '" + artifact.getAppName() + "' to IBM WebSphere Liberty Profile");
        service.installArtifact(artifact, new HashMap<String, Object>());
    }

    private void startArtifact(String appName, LibertyDeploymentService service) throws Exception {
        logger.info("Starting New Application '" + appName + "'...");
        service.startArtifact(appName);
    }

    private String findExtension(File file) {
        return file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
    }

    public static void main(String[] args) {
        DeployOnLiberty deployOnLiberty = new DeployOnLiberty();
        deployOnLiberty.ipAddress = System.getProperty("host");
        deployOnLiberty.port = System.getProperty("port");
        deployOnLiberty.username = System.getProperty("username");
        deployOnLiberty.password = System.getProperty("password");
        deployOnLiberty.clientTrustFile = System.getProperty("clientTrustFile");
        deployOnLiberty.clientTrustPassword = System.getProperty("clientTrustPassword");
        deployOnLiberty.sourceFile = System.getProperty("sourceFile");

        try {
            deployOnLiberty.deploy();
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
