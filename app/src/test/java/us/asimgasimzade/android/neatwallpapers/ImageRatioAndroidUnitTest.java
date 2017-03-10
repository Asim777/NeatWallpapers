package us.asimgasimzade.android.neatwallpapers;


import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the testing ImageRatio
 */

public class ImageRatioAndroidUnitTest {

    private double width;
    private double height;

    @Before
    public void setupDimensions() {
        width = 4001.0;
        height = 2000.0;
    }

    @Test
    public void imageMeetsRequirements() {
        boolean result = width / height <= 2.0;
        Assert.assertTrue("Aspect ratio is more than 2, it is ", result);
    }

}
