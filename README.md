# websphere-maven-plugin

This plugin is a fork of Jenkins plugin - [websphere-deployer-plugin](https://github.com/jenkinsci/websphere-deployer-plugin).
It uses all deploying code from Jenkins plugin.

## Dependencies

Plugin depends on some IBM libraries. They must be installed in your local repository to compile the plugin:

### WebSphere Application Server deployer

* com.ibm.ws.admin.client\_\*.jar - ($WAS\_INSTALL\_ROOT/runtimes)
    ```
    mvn install:install-file -Dfile=com.ibm.ws.admin.client\_\*.jar -DgroupId=com.ibm.ws -DartifactId=admin -Dversion=8.5.0 -Dpackaging=jar
    ```
* com.ibm.ws.orb\_\*.jar - ($WAS\_INSTALL\_ROOT/runtimes)
    ```
    mvn install:install-file -Dfile=com.ibm.ws.orb\_\*.jar -DgroupId=com.ibm.ws -DartifactId=orb -Dversion=8.5.0 -Dpackaging=jar
    ```

### WebSphere Liberty Profile

* com.ibm.websphere.appserver.api.basics\_\*.jar - ($LIBERTY\_INSTALL\_ROOT/dev/api/ibm)
    ```
    mvn install:install-file -Dfile=com.ibm.websphere.appserver.api.basics\_\*.jar -DgroupId=com.ibm.ws -DartifactId=liberty-basic -Dversion=8.5.5 -Dpackaging=jar
    ```
* com.ibm.websphere.appserver.api.endpoint\_\*.jar - ($LIBERTY\_INSTALL\_ROOT/dev/api/ibm)
    ```
    mvn install:install-file -Dfile=com.ibm.websphere.appserver.api.endpoint\_\*.jar -DgroupId=com.ibm.ws -DartifactId=liberty-endpoint -Dversion=8.5.5 -Dpackaging=jar
    ```
* com.ibm.websphere.appserver.api.restConnector\_\*.jar - ($LIBERTY\_INSTALL\_ROOT/dev/api/ibm)
    ```
    mvn install:install-file -Dfile=com.ibm.websphere.appserver.api.restConnector\_\*.jar -DgroupId=com.ibm.ws -DartifactId=liberty-connector -Dversion=8.5.5 -Dpackaging=jar
    ```
* restConnector.jar - ($LIBERTY\_INSTALL\_ROOT/clients)
    ```
    mvn install:install-file -Dfile=restConnector.jar -DgroupId=com.ibm.ws -DartifactId=liberty-rest-connector -Dversion=8.5.5 -Dpackaging=jar
    ```

