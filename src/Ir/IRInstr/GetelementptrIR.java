package Ir.IRInstr;

import java.util.ArrayList;

public class GetelementptrIR extends Instr{
    private String store;

    private String type;

    private String pointer;

    private ArrayList<String> indexs;

    public GetelementptrIR(String store, String type, String pointer,ArrayList<String> indexs) {
        this.store = store;
        this.type = type;
        this.pointer = pointer;
        this.indexs = indexs;
    }

    @Override
    public String genIr() {
        String s =  store + " = getelementptr " + type + ", " + type + "*" +
                pointer;
        StringBuilder stringBuilder = new StringBuilder();
        for (String index : indexs) {
            stringBuilder.append(", i32 ");
            stringBuilder.append(index);
        }
        return s + stringBuilder;
    }
}
