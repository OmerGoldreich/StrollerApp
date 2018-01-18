package com.stroller.stroller.navigationPackage;

import java.io.Serializable;

public class Distance implements Serializable{
    public String text;
    public int value;

    public Distance(String text, int value) {
        this.text = text;
        this.value = value;
    }
}
