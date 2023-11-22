package Ir.IRInstr;

public class ReturnIR extends Instr{
    private boolean returnVoid;

    private String tag;

    public ReturnIR(boolean returnVoid, String tag) {
        this.returnVoid = returnVoid;
        this.tag = tag;
    }

    public String genIr() {
        if (returnVoid) {
            return "ret void";
        } else {
            return "ret i32 " + tag;
        }
    }
}
