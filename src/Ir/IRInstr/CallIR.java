package Ir.IRInstr;

import AST.ArrDeclStmt;
import AST.VarDeclStmt;
import SymbolTablePackage.VarSymbolTable;

import java.util.ArrayList;

public class CallIR extends Instr{
    private String name;

    private String type;//返回值类型

    private ArrayList<String> paras;//函数实参

    private String storeReg;//有返回值

    ArrayList<VarDeclStmt> parasType;//自定义函数参数集合，如果没有，就是默认的i32

    public CallIR(String name, String type, ArrayList<String> paras, String storeReg,ArrayList<VarDeclStmt> parasType) {
        this.name = name;
        this.type = type;
        this.paras = paras;
        this.storeReg = storeReg;
        this.parasType = parasType;
    }

    public String genIr() {
        String s1 = storeReg == null ?  "" : storeReg + " = ";
        String s2 = "call ";
        String s3 = type.equals("int") ? "i32 " : "void ";
        String s4 = "@" + name + "(";
        StringBuilder sb = new StringBuilder();
        if (paras.size() >= 1) { //有可能没有参数？
            if (parasType != null) {
                sb.append(getParaType(parasType.get(0)));//说明调用了自己写的函数，不是putch之类
            } else {
                sb.append("i32 ");
            }
            sb.append(paras.get(0));
        }
        for (int i = 1; i < paras.size(); i++) {
            sb.append(", ");
            if (parasType != null) {
                sb.append(getParaType(parasType.get(i)));
            } else {
                sb.append("i32 ");
            }
            sb.append(paras.get(i));
        }
        sb.append(")");
        String s5 = sb.toString();
        return s1 + s2 + s3 + s4 + s5;
    }

    public String getParaType(VarDeclStmt varDeclStmt) {
        if (varDeclStmt instanceof ArrDeclStmt arrDeclStmt) {
            if (arrDeclStmt.getBracket() == 2) {
                return "[" + arrDeclStmt.getN2() + " x i32]* ";
            } else {
                return "i32* ";
            }
        } else {
            return "i32 ";
        }
    }
}
