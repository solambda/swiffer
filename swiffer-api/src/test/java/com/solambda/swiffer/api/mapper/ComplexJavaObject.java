package com.solambda.swiffer.api.mapper;

import java.util.List;

public class ComplexJavaObject {
    private final String finalStringValue;
    private final Number finalNumberValue;
    private List<ComplexJavaObject> complexList;
    private int[] intArray;
    final Object alwaysNull = null;

    public ComplexJavaObject() {
        finalStringValue = "";
        finalNumberValue = null;
    }

    public ComplexJavaObject(String finalStringValue, Number finalNumberValue) {
        this.finalStringValue = finalStringValue;
        this.finalNumberValue = finalNumberValue;
    }

    public String getFinalStringValue() {
        return finalStringValue;
    }

    public Number getFinalNumberValue() {
        return finalNumberValue;
    }

    public List<ComplexJavaObject> getComplexList() {
        return complexList;
    }

    public void setComplexList(List<ComplexJavaObject> complexList) {
        this.complexList = complexList;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public Object getAlwaysNull() {
        return alwaysNull;
    }
}
