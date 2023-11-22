package Ir.IRInstr;

public class LabelLineIR extends Instr{
    private String line;

    public LabelLineIR(String line) {
        this.line = line;
    }

    @Override
    public String genIr() {
        return "\n" + this.line.substring(1) + ":";
    }
}
