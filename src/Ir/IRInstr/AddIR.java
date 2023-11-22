package Ir.IRInstr;

public class AddIR extends Instr{
    private String reg1;

    private String reg2;

    private String reg3;

    public AddIR(String reg1, String reg2, String reg3) {
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.reg3 = reg3;

    }

    public String genIr() {
        return reg3 + " = add i32 " + reg1 + ", " + reg2;
    }
}
