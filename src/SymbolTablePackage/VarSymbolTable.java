package SymbolTablePackage;

import java.util.ArrayList;

public class VarSymbolTable extends SymbolTable {

    private boolean isConst;

    private int n1;

    private int n2;

    private int bracket;

    private boolean isParameter;

    private int value;

    private boolean isGlobal;

    private ArrayList<Integer> arrayValue = new ArrayList<>();

    public VarSymbolTable(int level,SymbolType type,int line,boolean isConst,String name,int n1,int n2,int bracket,boolean isParameter,boolean isGlobal) {
        super(level,type,name,line);
        this.isConst = isConst;
        this.n1 = n1;
        this.n2 = n2;
        this.bracket = bracket;
        this.isParameter = isParameter;
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getBracket() {
        return bracket;
    }

    public boolean isParameter() {
        return isParameter;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public ArrayList<Integer> getArrayValue() {
        return arrayValue;
    }

    public int getN1() {
        return n1;
    }

    public int getN2() {
        return n2;
    }

    public void setN1(int n1) { //错误处理不用，代码生成的时候用
        this.n1 = n1;
    }
    public void setN2(int n2) {
        this.n2 = n2;
    }
}
