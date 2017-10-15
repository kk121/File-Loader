package com.krishna.fileloadersample;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by krishna on 15/10/17.
 */

public class JsonTest implements Serializable{
    @SerializedName("key2")
    public String key2;
    @SerializedName("key1")
    public String key1;

    @Override
    public String toString() {
        return "JsonTest{" +
                "key2='" + key2 + '\'' +
                ", key1='" + key1 + '\'' +
                '}';
    }
}
