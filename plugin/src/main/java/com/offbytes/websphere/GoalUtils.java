package com.offbytes.websphere;

import java.io.File;

/**
 * Created by Konrad on 2014-04-13.
 */
class GoalUtils {

    static void fileExists(String property, String fileName) {
        if (fileName != null && !new File(fileName).exists()) {
            throw new IllegalArgumentException("Property " + property + ": file doesn't exist " + fileName);
        }
    }

    static String findExtension(File file) {
        return file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
    }
}
