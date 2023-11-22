package Ir.IRInstr;

public class ZextIR extends Instr{
    //%32 = zext i1 %31 to i32

    private String storeReg;

    private String transferReg;

    public ZextIR(String storeReg, String transferReg) {
        this.storeReg = storeReg;
        this.transferReg = transferReg;
    }

    @Override
    public String genIr() {
        return storeReg + " = zext i1 " + transferReg + " to i32";
    }
}
