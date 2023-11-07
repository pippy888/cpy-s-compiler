package SymbolTablePackage;

import java.util.ArrayList;

public class Value {
    private int value;

    private ArrayList<Integer> dimensionArray;

    private int valueType;

    public Value(int value, int valueType) {
        this.value = value;
        this.dimensionArray = null;
        this.valueType = valueType;//0or-1 -1是void
    }

    public Value(ArrayList<Integer> dimensionArray, int valueType) {
        this.value = Integer.MIN_VALUE;
        this.dimensionArray = dimensionArray;
        this.valueType = valueType;//1 or 2
    }

    public int getValueType() {
        return this.valueType;
    }
}
