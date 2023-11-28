package Ir.Component;

import AST.BasicBlock;
import Ir.IRInstr.FuncDeclIR;
import Ir.IRInstr.Instr;

import java.util.ArrayList;
import java.util.List;

public class GlobalFunc {
    private FuncDeclIR funcDeclIR;

    private ArrayList<BasicBlock> blocks;

    public GlobalFunc(FuncDeclIR funcDeclIR, ArrayList<BasicBlock> blocks) {
        this.funcDeclIR = funcDeclIR;
        this.blocks = blocks;
    }

    public String genIr() {
        StringBuilder stringBuilder = new StringBuilder();
        String s = funcDeclIR.genIr();
        stringBuilder.append(s);
        stringBuilder.append(" {");
        stringBuilder.append("\n");

        for (BasicBlock basicBlock : blocks) {
            ArrayList<Instr> instrs = basicBlock.getInstrs();
            for (Instr instr : instrs) {
                stringBuilder.append(instr.genIr());
                stringBuilder.append('\n');
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

