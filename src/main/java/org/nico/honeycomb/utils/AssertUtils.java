package org.nico.honeycomb.utils;

import org.apache.commons.lang3.StringUtils;

public class AssertUtils {

    public static void assertBlank(String key, String str) {
        if(StringUtils.isBlank(str)) {
            throw new AssertionError(key + " is blank.");
        }
    }
}
