package com.stroller.stroller.navigationPackage;

import java.io.Serializable;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public class Duration implements Serializable {
    public String text;
    public int value;

    public Duration(String text, int value) {
        this.text = text;
        this.value = value;
    }
}
