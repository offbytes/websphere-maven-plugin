package com.offbytes.websphere;

import org.jenkinsci.plugins.websphere.services.deployment.Artifact;
import org.jenkinsci.plugins.websphere.services.deployment.WebSphereDeploymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Konrad on 2014-04-22.
 */
public class DeployOnWAS {

    private static Logger logger = LoggerFactory.getLogger(DeployOnWAS.class);

    // WAS connection settings
    private String host;
    private String port;
    private String connectorType;

    // WAS authorization settings
    private String username;
    private String password;
    private String clientKeyFile;
    private String clientKeyPassword;
    private String clientTrustFile;
    private String clientTrustPassword;

    // WAS deployment settings

    private String node;
    private String cell;
    private String server;
    private boolean autoStart;
    private boolean precompile;
    private boolean reloading;
    private String sourceFile;

    public void deploy() throws Exception {
        WebSphereDeploymentService service = new WebSphereDeploymentService();

        try {
            connect(service);
            Artifact artifact = createArtifact(service);
            stopArtifact(artifact.getAppName(), service);
            uninstallArtifact(artifact.getAppName(), service);
            deployArtifact(artifact, service);
            startArtifact(artifact.getAppName(), service);
        } catch (Exception e) {
            logger.error("Error deploying to IBM WebSphere Application Server", e);
            throw e;
        } finally {
            service.disconnect();
        }
    }

    private void deployArtifact(Artifact artifact, WebSphereDeploymentService service) throws Exception {
        logger.info("Deploying New '" + artifact.getAppName() + "' to IBM WebSphere Application Server");
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(WebSphereDeploymentService.APPDEPL_JSP_RELOADENABLED, reloading);
        options.put(WebSphereDeploymentService.APPDEPL_PRECOMPILE_JSP, precompile);
        service.installArtifact(artifact, options);
    }

    private void uninstallArtifact(String appName, WebSphereDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            logger.info("Uninstalling Old Application '" + appName + "'...");
            service.uninstallArtifact(appName);
        }
    }

    private void startArtifact(String appName, WebSphereDeploymentService service) throws Exception {
        if (autoStart) {
            logger.info("Starting New Application '" + appName + "'...");
            service.startArtifact(appName);
        }
    }

    private void stopArtifact(String appName, WebSphereDeploymentService service) throws Exception {
        if (service.isArtifactInstalled(appName)) {
            logger.info("Stopping Old Application '" + appName + "'...");
            service.stopArtifact(appName);
        }
    }

    private Artifact createArtifact(WebSphereDeploymentService service) {
        Artifact artifact = new Artifact();
        File artifactPath = new File(sourceFile);

        artifact.setType(Artifact.TYPE_EAR);
        artifact.setPrecompile(precompile);
        artifact.setSourcePath(artifactPath);
        artifact.setAppName(getAppName(artifact, service));
        return artifact;
    }

    private void connect(WebSphereDeploymentService service) throws Exception {
        logger.info("Connecting to IBM WebSphere Application Server...");
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
        service.connect();
    }

    private String getAppName(Artifact artifact, WebSphereDeploymentService service) {
        return service.getAppName(artifact.getSourcePath().getAbsolutePath());
    }

    public static void main(String[] args) {
        DeployOnWAS deployOnWAS = new DeployOnWAS();
        deployOnWAS.host = System.getProperty("host");
        deployOnWAS.port = System.getProperty("port");
        deployOnWAS.connectorType = System.getProperty("connectorType");
        deployOnWAS.username = System.getProperty("username");
        deployOnWAS.password = System.getProperty("password");
        deployOnWAS.clientKeyFile = System.getProperty("clientKeyFile");
        deployOnWAS.clientKeyPassword = System.getProperty("clientKeyPassword");
        deployOnWAS.clientTrustFile = System.getProperty("clientTrustFile");
        deployOnWAS.clientTrustPassword = System.getProperty("clientTrustPassword");
        deployOnWAS.node = System.getProperty("node");
        deployOnWAS.cell = System.getProperty("cell");
        deployOnWAS.server = System.getProperty("server");
        deployOnWAS.autoStart = Boolean.getBoolean("autoStart");
        deployOnWAS.precompile = Boolean.getBoolean("precompile");
        deployOnWAS.reloading = Boolean.getBoolean("reloading");
        deployOnWAS.sourceFile = System.getProperty("sourceFile");

        try {
            deployOnWAS.deploy();
        } catch (Exception e) {
            System.exit(1);
        }
    }

}
