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

    private ArrayList<Integer> initial;

    public GlobalVarIr(String name, String type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.bracket = 0;
        this.n1 = 0;
        this.n2 = 0;
        this.initial = null;
    }

    public GlobalVarIr(String name, String type,int bracket, int n1, int n2, ArrayList<Integer> initial) {
        this.name = name;
        this.type = type;
        this.value = -999;//没用
        this.bracket = bracket;
        this.n1 = n1;
        this.n2 = n2;
        this.initial = initial;
    }

    public String genIr() {
        if (bracket == 0) {
            return "@" + name + " = " + "dso_local " + type + " i32 " + value;
        } else {
            if (bracket == 1) {
                if (this.initial != null) {
                    String s1 = "@" + name + " = " + "dso_local " + type + " [" + n2 + " x i32] [i32 " + initial.get(0);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 1; i < initial.size(); i++) {
                        stringBuilder.append(", i32 ");
                        stringBuilder.append(initial.get(i));
                    }
                    return s1 + stringBuilder + "]";
                } else {
                    return  "@" + name + " = " + "dso_local " + type + " [" + n2 + " x i32] zeroinitializer";
                }
            } else {
                String s1 = "@" + name + " = " + "dso_local " + type + " ";
                StringBuilder stringBuilder  = new StringBuilder();
                stringBuilder.append("[");
                stringBuilder.append(n1);
                stringBuilder.append(" x [");
                stringBuilder.append(n2);
                stringBuilder.append(" x i32]] ");
                if (initial != null) {
                    stringBuilder.append("[");
                    for (int i = 0; i < n1; i++) {
                        if (i != 0) {
                            stringBuilder.append(", ");
                        }
                        stringBuilder.append("[");
                        stringBuilder.append(n2);
                        stringBuilder.append(" x i32] [");
                        for (int j = 0; j < n2; j++) {
                            if (j != 0) {
                                stringBuilder.append(", ");
                            }
                            stringBuilder.append("i32 ");
                            stringBuilder.append(initial.get(i * n2 + j));
                        }
                        stringBuilder.append("]");
                    }
                    stringBuilder.append("]");
                    return s1 + stringBuilder;
                } else {
                    stringBuilder.append("zeroinitializer");
                    return s1 + stringBuilder;
                }
            }
        }
    }
}
