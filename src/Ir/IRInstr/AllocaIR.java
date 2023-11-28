package Ir.IRInstr;

public class AllocaIR extends Instr{
    private String info;

    private String type;

    public AllocaIR(String tag,String type) {
        this.info = tag;
        this.type = type;
    }

    public String genIr() {
        String s = info + " = "+ "alloca " + type;
        return s;
    }
}
