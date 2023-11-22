package AST;

import java.util.ArrayList;

public class CallExpr extends Stmt{
    private String functionName;

    private ArrayList<ComputeStmt> paras;

    private String format;

    public CallExpr(String name, ArrayList<ComputeStmt> paras, String format) {
        this.functionName = name;
        this.paras = paras;
        this.format = format;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getFormat() {
        return format;
    }

    public ArrayList<ComputeStmt> getParas() {
        return paras;
    }
}
