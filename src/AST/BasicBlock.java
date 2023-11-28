package AST;

import Ir.IRInstr.BrIR;
import Ir.IRInstr.Instr;
import Ir.IRInstr.ReturnIR;

import java.util.ArrayList;

public class BasicBlock {
    private String tag;

    private Instr endOfBlock;

    private String endOfBlockClass;

    private int blockNo;

    private ArrayList<Instr> instrs = new ArrayList<>();

    public BasicBlock(String tag, int blockNo) {
        this.tag = tag;
        this.blockNo = blockNo;
    }

    public void inputInstr(Instr instr) {
        if (endOfBlock == null) {
            this.instrs.add(instr);
        } else {
            System.out.println("because of continue or break!, these codes add error!");
        }
    }

    public String getTag() {
        return tag;
    }

    public void setEndOfBlock(Instr instr,String endOfBlockClass) {
        if (endOfBlock == null) {
            this.endOfBlock = instr;
            this.endOfBlockClass = endOfBlockClass;
        }
    }

    public int getBlockNo() {
        return blockNo;
    }

    public ArrayList<Instr> getInstrs() {
        return instrs;
    }

    public Instr getEndOfBlock() {
        return this.endOfBlock;
    }
}
