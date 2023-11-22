package Ir.IRInstr;

import java.util.ArrayList;

public class Pair {
    private ArrayList<Instr> instrs;

    private ArrayList<String> place;

    public Pair(ArrayList<Instr> instrs, ArrayList<String> place) {
        this.instrs = instrs;
        this.place = place;
    }

    public ArrayList<Instr> getInstrs() {
        return instrs;
    }

    public ArrayList<String> getPlace() {
        return place;
    }
}
