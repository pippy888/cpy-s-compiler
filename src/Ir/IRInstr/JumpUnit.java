package Ir.IRInstr;

import java.util.ArrayList;

public class JumpUnit {
    private ArrayList<BrIR> brIRS = new ArrayList<>();//似乎没什么用了。。。。

    private BrIR begin;//for只用这个和end

    private BrIR ifBegin;
    private BrIR elseBegin;

    private BrIR end;//for只用这个和begin

    private int LorExpFailCrossLine;

    private BrIR LorExpFailCrossBr;

    public JumpUnit() {
    }

    public void setLorExpFailCross(int lorExpFailCrossLine,BrIR lorExpFailCrossBr) {
        this.LorExpFailCrossLine = lorExpFailCrossLine;
        this.LorExpFailCrossBr = lorExpFailCrossBr;
    }

    public void setLorExpFailCrossBrLine(String line) {
        LorExpFailCrossBr.setUnconditionalJump_reg(line);
    }

    public void addBrIR(BrIR brIR) {
        this.brIRS.add(brIR);
    }

    public void setBegin(BrIR begin) {
        this.begin = begin;
    }

    public void setIfBegin(BrIR ifBegin) {
        this.ifBegin = ifBegin;
    }

    public void setElseBegin(BrIR elseBegin) {
        this.elseBegin = elseBegin;
    }
    public void setEnd(BrIR end) {
        this.end = end;
    }
    public void setAllBrEndReg(String endReg) {
        for (BrIR brIR : brIRS) {
            brIR.setWrong(endReg);
        }
    }
}
