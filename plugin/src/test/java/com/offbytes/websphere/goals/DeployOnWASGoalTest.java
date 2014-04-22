package com.offbytes.websphere.goals;

import com.offbytes.websphere.goals.DeployOnWASGoal;
import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by Konrad on 2014-03-23.
 */
public class DeployOnWASGoalTest {

    public final DeployOnWASGoal deployOnWASGoal = new DeployOnWASGoal();

    @Before
    public void setUpMocks() {
        deployOnWASGoal.setLog(Mockito.mock(Log.class));
    }

    @Test
    public void shouldUseDeprecatedWarPathWhenWarFileNotSet() throws Exception {
        // given
        option("warPath", "test");

        // when
        deployOnWASGoal.handleDeprecated();

        // then
        Assert.assertEquals("test", option("warFile"));
    }

    @Test
    public void shouldUseWarFileEvenWhenDeprecatedWarPathWasSet() throws Exception {
        // given
        option("warPath", "test");
        option("warPath", "test2");

        // when
        deployOnWASGoal.handleDeprecated();

        // then
        Assert.assertEquals("test2", option("warFile"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfClientKeyFileNotExists() throws Exception {
        // given
        option("clientKeyFile", Long.toHexString(System.nanoTime()));

        // when
        deployOnWASGoal.validateParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfClientTrustFileFileNotExists() throws Exception {
        // given
        option("clientTrustFile", Long.toHexString(System.nanoTime()));

        // when
        deployOnWASGoal.validateParameters();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfWarFileNotExists() throws Exception {
        // given
        option("warFile", Long.toHexString(System.nanoTime()));

        // when
        deployOnWASGoal.validateParameters();
    }

    @Test
    public void shouldPassIfAllFilesSetExist() throws Exception {
        // given
        String tempFilePath = createTempFile().getPath();
        option("clientKeyFile", tempFilePath);
        option("clientTrustFile", tempFilePath);
        option("warFile", tempFilePath);

        // when
        deployOnWASGoal.validateParameters();

        // then
    }

    private File createTempFile() throws IOException {
        File tempFile = File.createTempFile("test_" + this.getClass().getSimpleName(), ".dat");
        tempFile.setLastModified(new Date().getTime());
        tempFile.deleteOnExit();
        return tempFile;
    }

    private String option(String name) {
        try {
            Field field = DeployOnWASGoal.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(deployOnWASGoal).toString();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void option(String name, String value) {
        try {
            Field field = DeployOnWASGoal.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(deployOnWASGoal, value);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
