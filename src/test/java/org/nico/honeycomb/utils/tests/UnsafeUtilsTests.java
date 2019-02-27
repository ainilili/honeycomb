package org.nico.honeycomb.utils.tests;

import org.junit.Assert;
import org.junit.Test;
import org.nico.honeycomb.utils.UnsafeUtils;

public class UnsafeUtilsTests {

    @Test
    public void getUnsafeTest() {
        Assert.assertNotNull(UnsafeUtils.getUnsafe());
    }
}
