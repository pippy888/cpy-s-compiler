package Ir.IRInstr;

public class StoreIR extends Instr{
    private String type;
    private String para;//value
    private String pointer;

    public StoreIR(String para, String pointer, String type) {
        this.para = para;
        this.pointer = pointer;
        this.type = type;
    }

    public String genIr() {
        return "store " + type + " " + para + ", " + type + " * " + pointer;
    }
}
