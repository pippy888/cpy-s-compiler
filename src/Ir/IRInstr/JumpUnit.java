package Ir.IRInstr;

import java.util.ArrayList;

public class JumpUnit {
    private BrIR begin;//for只用这个和end
    private BrIR end;//for只用这个和begin break也会用这个
    private BrIR continueBrIR;//continue用这个
    private BrIR LorExpFailCrossBr;//如果条件判断错误的br

    private BrIR ifBr = null;//if跳转语句的跳转

    public JumpUnit() {
    }

    public void setLorExpFailCross(BrIR lorExpFailCrossBr) {
        this.LorExpFailCrossBr = lorExpFailCrossBr;
    }

    public void setLorExpFailCrossBrLine(String line) {
        if (LorExpFailCrossBr != null) {
            LorExpFailCrossBr.setUnconditionalJump_reg(line);
        }
    }

    public void setBegin(BrIR begin) {
        this.begin = begin;
    }

    public void setEnd(BrIR end) {
        this.end = end;
    }

    public BrIR getBegin() {
        return begin;
    }

    public BrIR getEnd() {
        return end;
    }

    public void setContinueBrIR(BrIR continueBrIR) { //这个是用来存着continue的入口，就是assignStmt2的入口
        this.continueBrIR = continueBrIR;
    }

    public BrIR getContinueBrIR() {
        return continueBrIR;
    }

    public void setIfBr(BrIR ifBr) {
        this.ifBr = ifBr;
    }

    public BrIR getIfBr() {
        return ifBr;
    }
}
