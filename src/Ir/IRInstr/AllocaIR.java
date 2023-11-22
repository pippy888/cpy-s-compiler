package Ir.IRInstr;

public class AllocaIR extends Instr{
    private String info;

    public AllocaIR(String tag) {
        this.info = tag;
    }

    public String genIr() {
        String s = info + " = "+ "alloca i32";
        return s;
    }
}
