package org.jenkinsci.plugins.websphere.services.deployment;


public class Server {

    private String cellName;
    private String nodeName;
    private String serverName;
    private String serverVendor;
    private String serverVersion;
    private String processId;

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerVendor() {
        return serverVendor;
    }

    public void setServerVendor(String serverVendor) {
        this.serverVendor = serverVendor;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
