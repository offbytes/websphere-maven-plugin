# websphere-maven-plugin

This plugin is a fork of Jenkins plugin - [websphere-deployer-plugin](https://github.com/jenkinsci/websphere-deployer-plugin).
It uses all deploying code from Jenkins plugin.

## Dependencies

Plugin depends on some IBM libraries. They must be installed in your local repository to compile the plugin:

1. WebSphere Application Server deployer
    * com.ibm.ws.admin.client\_\*.jar - ($WAS\_INSTALL\_ROOT/runtimes)
    ```bash 
    mvn install:install-file -Dfile=com.ibm.ws.admin.client_*.jar \
         -DgroupId=com.ibm.ws -DartifactId=admin -Dversion=8.5.0 \
         -Dpackaging=jar
    ```
    * com.ibm.ws.orb\_\*.jar - ($WAS\_INSTALL\_ROOT/runtimes)
    ```bash 
    mvn install:install-file -Dfile=com.ibm.ws.orb_*.jar \
         -DgroupId=com.ibm.ws -DartifactId=orb -Dversion=8.5.0 \
         -Dpackaging=jar
    ```
1. WebSphere Liberty Profile
    * com.ibm.websphere.appserver.api.basics\_\*.jar - ($LIBERTY\_INSTALL\_ROOT/dev/api/ibm)
    ```bash 
    mvn install:install-file -Dfile=com.ibm.websphere.appserver.api.basics_*.jar \
         -DgroupId=com.ibm.ws -DartifactId=liberty-basic -Dversion=8.5.5 \
         -Dpackaging=jar
    ```
    * com.ibm.websphere.appserver.api.endpoint\_\*.jar - ($LIBERTY\_INSTALL\_ROOT/dev/api/ibm)
    ```bash 
    mvn install:install-file -Dfile=com.ibm.websphere.appserver.api.endpoint_*.jar \
         -DgroupId=com.ibm.ws -DartifactId=liberty-endpoint -Dversion=8.5.5 \
         -Dpackaging=jar
    ```
    * com.ibm.websphere.appserver.api.restConnector\_\*.jar - ($LIBERTY\_INSTALL\_ROOT/dev/api/ibm)
    ```bash 
    mvn install:install-file -Dfile=com.ibm.websphere.appserver.api.restConnector_*.jar \
         -DgroupId=com.ibm.ws -DartifactId=liberty-connector -Dversion=8.5.5 \ 
         -Dpackaging=jar
    ```
    * restConnector.jar - ($LIBERTY\_INSTALL\_ROOT/clients)
    ```bash 
    mvn install:install-file -Dfile=restConnector.jar \
         -DgroupId=com.ibm.ws -DartifactId=liberty-rest-connector -Dversion=8.5.5 \
         -Dpackaging=jar
    ```

## Use Maven plugin

To use plugin you have to append dependencies to plugin definition in pom.xml. E.g.

```XML
<plugin>
    <groupId>com.offbytes.websphere</groupId>
    <artifactId>websphere-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency> <!-- WebSphere AS dependencies -->
            <groupId>com.ibm.ws</groupId>
            <artifactId>admin</artifactId>
            <version>8.5.0</version>
        </dependency>
        <dependency>
            <groupId>com.ibm.ws</groupId>
            <artifactId>orb</artifactId>
            <version>8.5.0</version>
        </dependency>
    </dependencies>
</plugin>
```

### WebSphere Application Server deployment

Deployment is triggered using `websphere:deployWAS` goal.

Configuration: (__property__ - required)

* __host__ (property: was.host) - Application server host name (_example:_ 10.0.0.1)
* __port__ (property: was.port) - Server access port for SOAP (_example:_ 8880)
* __node__ (property: was.node) - Node name (_example:_ AppSrvNode01)
* __cell__ (property: was.cell) - Cell name (_example:_ AppSrvNode01Cell)
* __server__ (property: was.server) - Server name (_example:_ server1)
* username (property: was.username) - (_example:_ admin)
* password (property: was.password) - (_example:_ password)
* clientKeyFile (property: was.clientKeyFile) - Absolute path to copy of Client Key file (_example:_ /home/bob/secret/DummyClientKeyFile.jks)
* clientKeyPassword (property: was.clientKeyPassword) - Key file password (_example:_ WebAS)
* clientTrustFile (property: was.clientTrustFile) - Absolute path to copy of Client Trust file (_example:_ /home/bob/secret/DummyClientTrustFile.jks)
* clientTrustPassword (property: was.clientTrustPassword) - Trust file password (_example:_ WebAS)
* earLevel (property: was.earLevel) - Generated EAR api level (5 for JavaEE 5, 6 for JavaEE 6) - used when deploying WAR file
* precompile (property: was.precompile) - Precompile JSP pages
* reloading (property: was.reloading) - JSP reloading
* warContextPath (property: was.warContextPath) - allows to set context path of deployed WAR file
* warPath (property: was.warPath) - allows to deploy any WAR file (not only target/finalName.war)

