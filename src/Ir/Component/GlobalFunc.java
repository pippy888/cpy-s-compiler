package Ir.Component;

import Ir.IRInstr.FuncDeclIR;
import Ir.IRInstr.Instr;

import java.util.ArrayList;
import java.util.List;

public class GlobalFunc {
    private FuncDeclIR funcDeclIR;

    private ArrayList<Instr> content;

    public GlobalFunc(FuncDeclIR funcDeclIR, ArrayList<Instr> content) {
        this.funcDeclIR = funcDeclIR;
        this.content = content;
    }

    public String genIr() {
        StringBuilder stringBuilder = new StringBuilder();
        String s = funcDeclIR.genIr();
        stringBuilder.append(s);
        stringBuilder.append(" {");
        stringBuilder.append("\n");
        for (Instr instr : content) {
            stringBuilder.append(instr.genIr());
            stringBuilder.append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

