package Ir.IRInstr;

public class IcmpIR extends Instr{
    private String cond;//ne eq

    private String op1;

    private String op2;

    private String storeReg;

    public IcmpIR(String storeReg, String cond, String op1, String op2) {
        this.cond = cond;
        this.storeReg = storeReg;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String genIr() {
        return storeReg + " = icmp " + cond + " i32 " + op1 + ", " + op2;
    }
}
