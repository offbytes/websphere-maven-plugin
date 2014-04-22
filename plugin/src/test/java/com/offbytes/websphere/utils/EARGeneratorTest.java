package com.offbytes.websphere.utils;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.websphere.services.deployment.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasXPath;
import static org.junit.Assert.*;

public class EARGeneratorTest {

    public static final String APP_NAME = "app";
    public static final String EAR_LEVEL = "6";

    private EARGenerator earGenerator;
    private File earFile;
    private File warFile;
    private Artifact artifact;

    @Before
    public void initFiles() throws IOException {
        earFile = File.createTempFile(EARGenerator.class.getSimpleName(), ".ear");
        earFile.deleteOnExit();
        warFile = File.createTempFile(EARGenerator.class.getSimpleName(), ".war");
        warFile.deleteOnExit();

        earGenerator = new EARGenerator();
        earGenerator.setSourceFile(warFile);
        earGenerator.setDestination(earFile);
        earGenerator.setEarLevel(EAR_LEVEL);
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
        earGenerator.setWarContextPath("/test");
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
        earGenerator.generate();

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

    private void touchFile(File file) {
        file.setLastModified(0);
    }

}