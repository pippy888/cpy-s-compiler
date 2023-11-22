package Ir.IRInstr;

import java.util.ArrayList;

public class CallIR extends Instr{
    private String name;

    private String type;//返回值类型

    private ArrayList<String> paras;//函数实参

    private String storeReg;//有返回值

    public CallIR(String name, String type, ArrayList<String> paras, String storeReg) {
        this.name = name;
        this.type = type;
        this.paras = paras;
        this.storeReg = storeReg;
    }

    public String genIr() {
        String s1 = storeReg == null ?  "" : storeReg + " = ";
        String s2 = "call ";
        String s3 = type.equals("int") ? "i32 " : "void ";
        String s4 = "@" + name + "(";
        StringBuilder sb = new StringBuilder();
        if (paras.size() >= 1) {
            sb.append("i32 ").append(paras.get(0));
        }
        for (int i = 1; i < paras.size(); i++) {
            sb.append(", ");
            sb.append("i32 ").append(paras.get(i));
        }
        sb.append(")");
        String s5 = sb.toString();
        return s1 + s2 + s3 + s4 + s5;
    }
}
