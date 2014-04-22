package com.offbytes.websphere.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Konrad on 2014-04-22.
 */
public class EARGenerator {

    private String warContextPath;
    private File sourceFile;
    private File destination;
    private String earLevel;

    public String getWarContextPath() {
        return warContextPath;
    }

    public void setWarContextPath(String warContextPath) {
        this.warContextPath = warContextPath;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public File getDestination() {
        return destination;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    public String getEarLevel() {
        return earLevel;
    }

    public void setEarLevel(String earLevel) {
        this.earLevel = earLevel;
    }

    public void generate() {

        byte[] buf = new byte[1024];
        try {
            String warName = sourceFile.getName();
            String context = (getWarContextPath() == null) ? warName.substring(0,warName.lastIndexOf("."))
                    : getWarContextPath();

            if (context.startsWith("/")) {
                context = context.substring(1);
            }

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination));
            FileInputStream in = new FileInputStream(sourceFile);
            out.putNextEntry(new ZipEntry(sourceFile.getName()));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
            out.putNextEntry(new ZipEntry("META-INF/"));
            out.closeEntry();
            out.putNextEntry(new ZipEntry("META-INF/application.xml"));
            out.write(getApplicationXML(warName, context,earLevel).getBytes());
            out.closeEntry();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getApplicationXML(String warName, String context, String earLevel) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<application xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/application_"+earLevel+".xsd\" version=\""+earLevel+"\">\n" +
                "  <description>"+context+"</description>\n" +
                "  <display-name>"+context+"</display-name>\n" +
                "  <module>\n" +
                "    <web>\n" +
                "      <web-uri>"+warName+"</web-uri>\n" +
                "      <context-root>/"+context+"</context-root>\n" +
                "    </web>\n" +
                "  </module>\n" +
                "</application>";
    }
}
