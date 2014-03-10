package org.jenkinsci.plugins.websphere.services.deployment;

import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasXPath;
import static org.junit.Assert.assertThat;

/**
 * Created by kmrozek on 3/7/14.
 */
public class WebSphereDeploymentServiceTest {

    public static final String APP_NAME = "app";
    public static final String EAR_LEVEL = "6";

    private WebSphereDeploymentService service;
    private File earFile;
    private File warFile;
    private Artifact artifact;

    @Before
    public void initFiles() throws IOException {
        earFile = File.createTempFile(WebSphereDeploymentServiceTest.class.getSimpleName(), ".ear");
        earFile.deleteOnExit();
        warFile = File.createTempFile(WebSphereDeploymentServiceTest.class.getSimpleName(), ".war");
        warFile.deleteOnExit();

        service = new WebSphereDeploymentService();
        createArtifact();
        touchFile(warFile);
    }

    @Test
    public void shouldGenerateCorrectEAR() throws IOException {
        Node applicationXml = generateEarAndReturnApplicationXml();
        String warFileName = warFile.getName();
        String appName = warFileName.substring(0, warFileName.indexOf(".war"));

        assertThat(applicationXml,
                hasXPath("//context-root", equalTo("/" + appName)));
        assertThat(applicationXml,
                hasXPath("//display-name", equalTo(appName)));
        assertThat(applicationXml,
                hasXPath("//description", equalTo(appName)));
        assertApplictionXml(applicationXml, warFileName);
    }

    @Test
    public void shouldGenerateCorrectEARWithSpecifiedContextRoot() throws IOException {
        service.setWarContextPath("/test");
        Node applicationXml = generateEarAndReturnApplicationXml();

        String warFileName = warFile.getName();
        assertThat(applicationXml,
                hasXPath("//context-root", equalTo("/test")));
        assertThat(applicationXml,
                hasXPath("//display-name", equalTo("test")));
        assertThat(applicationXml,
                hasXPath("//description", equalTo("test")));
        assertApplictionXml(applicationXml, warFileName);
    }

    private void assertApplictionXml(Node applicationXml, String warFileName) {
        assertThat(applicationXml,
                hasXPath("//web-uri", equalTo(warFileName)));
        assertThat(applicationXml,
                hasXPath("//application/@version", equalTo(EAR_LEVEL)));
    }

    private Node generateEarAndReturnApplicationXml() throws IOException {
        // when
        service.generateEAR(artifact, earFile, EAR_LEVEL);

        // then
        ZipFile zipFile = new ZipFile(earFile);
        return extractFileToXml(zipFile, "META-INF/application.xml");
    }

    private Node extractFileToXml(ZipFile zipFile, String name) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(zipFile.getInputStream(zipFile.getEntry(name)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractFileToString(ZipFile zipFile, String name) throws IOException {
        return IOUtils.toString(zipFile.getInputStream(zipFile.getEntry(name)));
    }

    private void createArtifact() {
        artifact = new Artifact();
        artifact.setAppName(APP_NAME);
        artifact.setPrecompile(false);
        artifact.setSourcePath(warFile);
        artifact.setType(Artifact.TYPE_WAR);
    }

    private void touchFile(File file) {
        file.setLastModified(0);
    }

}
