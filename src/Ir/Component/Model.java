package Ir.Component;


import Ir.IRInstr.LibFunctionIR;

import java.util.ArrayList;

public class Model {
    private ArrayList<GlobalVar> globalVars;

    private ArrayList<GlobalFunc> globalFuncs;

    private GlobalFunc main;

    public Model(ArrayList<GlobalVar> globalVars, ArrayList<GlobalFunc> globalFuncs, GlobalFunc main) {
        this.globalVars = globalVars;
        this.globalFuncs = globalFuncs;
        this.main = main;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        LibFunctionIR libFunctionIR = new LibFunctionIR();

        stringBuilder.append(libFunctionIR.genIr());
        for (GlobalVar globalVar : globalVars) {
            stringBuilder.append(globalVar.genIr()).append("\n");
        }

        for (GlobalFunc globalFunc : globalFuncs) {
            stringBuilder.append(globalFunc.genIr()).append("\n");
        }
        stringBuilder.append(main.genIr());

        String s = stringBuilder.toString();
        System.out.println(s);
        return s;
    }
}
