package Ir.IRInstr;

public class StoreIR extends Instr{
    private String para;//value

    private String pointer;

    public StoreIR(String para, String pointer) {
        this.para = para;
        this.pointer = pointer;
    }

    public String genIr() {
        return "store i32 " + para + ", i32* " + pointer;
    }
}
