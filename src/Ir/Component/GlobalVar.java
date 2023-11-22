package Ir.Component;

import Ir.IRInstr.GlobalVarIr;

public class GlobalVar {
    private GlobalVarIr ir;

    public GlobalVar(GlobalVarIr ir) {
        this.ir = ir;
    }

    public String genIr() {
        return this.ir.genIr();
    }
}
