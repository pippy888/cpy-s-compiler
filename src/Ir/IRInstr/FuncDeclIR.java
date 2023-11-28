package Ir.IRInstr;

import AST.ArrDeclStmt;
import AST.VarDeclStmt;

import java.util.ArrayList;

public class FuncDeclIR extends Instr{
    private String name;

    private String type;

    private ArrayList<VarDeclStmt> paras;


    public FuncDeclIR(String name, String type, ArrayList<VarDeclStmt> paras) {
        this.name = name;
        this.type = type;
        this.paras = paras;
    }

    public String genIr() {
        StringBuilder stringBuilder = new StringBuilder();
        String s = type.equals("int") ? "i32" : "void";
        stringBuilder.append("define dso_local ").append(s).append(" @").append(name);
        stringBuilder.append("(");
        if (paras != null) {
            if (paras.size() > 0) {
                stringBuilder.append("i32 %").append(0);
            }
            for (int i = 1; i < paras.size(); i++) {
                stringBuilder.append(", ");
                //stringBuilder.append("i32 %").append(i);
                if (paras.get(i) instanceof ArrDeclStmt arrDeclStmt) {
                    if (arrDeclStmt.getBracket() == 1) {
                        stringBuilder.append("i32* %").append(i);
                    } else if (arrDeclStmt.getBracket() == 2){
                        stringBuilder.append(arrDeclStmt.getDimension2pointerType());
                        stringBuilder.append(" %").append(i);
                    }
                } else {
                    stringBuilder.append("i32 %").append(i);
                }
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
