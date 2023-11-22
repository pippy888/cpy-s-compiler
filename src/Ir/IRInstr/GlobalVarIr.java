package Ir.IRInstr;

import AST.ComputeStmt;

import java.util.ArrayList;

public class GlobalVarIr {
    private String name;

    private String type;

    private int value;

    private int bracket;

    private int n1;

    private int n2;

    private ArrayList<ComputeStmt> initial;

    public GlobalVarIr(String name, String type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.bracket = 0;
        this.n1 = 0;
        this.n2 = 0;
        this.initial = null;
    }

    public String genIr() {
        if (bracket == 0) {
            return "@" + name + " = " + "dso_local " + type + " i32 " + value;
        } else {
            return null;
        }
    }
}
