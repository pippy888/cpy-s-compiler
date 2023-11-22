package Ir.IRInstr;

public class SremIR extends Instr{
    private String tag1;

    private String tag2;

    private String tag3;

    public SremIR(String tag1, String tag2, String tag3) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
    }

    public String genIr() {
        return tag3 + " = srem i32 " + tag1 + ", " + tag2;
    }
}
