package Ir.IRInstr;

public class BrIR extends Instr{
    private boolean unconditionalJump;

    private String unconditionalJump_reg;

    private String value;//有条件跳转的寄存器判断值 1/0

    private String accept;

    private String wrong;

    public BrIR(String unconditionalJump_reg) { //无条件
        this.unconditionalJump = true;
        this.unconditionalJump_reg = unconditionalJump_reg;
    }

    public BrIR(String value,String accept,String wrong) {
        this.unconditionalJump = false;
        this.value = value;
        this.accept = accept;
        this.wrong = wrong;
    }

    public String genIr() {
        if (unconditionalJump) {
            return "br label " + unconditionalJump_reg; // + "\n\n" + unconditionalJump_reg + ":";
        } else {
            return "br i1 " + value + ", label " + accept + ", label " + wrong;// + "\n\n" + accept + ":";
        }
    }

    public void setWrong(String wrong) {
        this.wrong = wrong;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public void setUnconditionalJump_reg(String unconditionalJump_reg) {
        this.unconditionalJump_reg = unconditionalJump_reg;
    }
}
