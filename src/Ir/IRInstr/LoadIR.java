package Ir.IRInstr;

public class LoadIR extends Instr{
    private String store;

    private String pointer;

    private String type;

    public LoadIR(String store, String pointer,String type) {
        this.store = store;
        this.pointer = pointer;
        this.type = type;
    }

    public String genIr() {
        return store + " = load " + type + "," + type + " * " + pointer;
    }
}
