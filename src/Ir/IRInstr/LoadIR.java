package Ir.IRInstr;

public class LoadIR extends Instr{
    private String tag;

    private String pointer;

    public LoadIR(String tag, String pointer) {
        this.tag = tag;
        this.pointer = pointer;
    }

    public String genIr() {
        return tag + " = load i32, i32* " + pointer;
    }
}
