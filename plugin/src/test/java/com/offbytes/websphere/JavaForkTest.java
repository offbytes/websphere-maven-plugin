package com.offbytes.websphere;

import com.offbytes.websphere.utils.JavaFork;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Konrad on 2014-04-16.
 */
public class JavaForkTest {

    @Test
    public void testExecute() throws Exception {
        int expected = new Random().nextInt();
        String expectedOutput = UUID.randomUUID().toString();
        JavaFork fork = new JavaFork()
                .classpath(System.getProperty("java.class.path"))
                .mainClass(JavaForkTest.class.getName())
                .property("testCode", Integer.toString(expected))
                .stealsOutput()
                .argument(expectedOutput);
        int actual = fork.execute();

        Assert.assertEquals(expected, actual);
        Assert.assertTrue(fork.getOutput().contains(expectedOutput));
    }

    public static void main(String[] args) {
        System.out.println(args[0]);
        System.exit(Integer.getInteger("testCode"));
    }
}
