package AST;

import java.util.ArrayList;

public class ArrDeclStmt extends VarDeclStmt{
    private ArrayList<ComputeStmt> value;//数组初始值

    private int n1;

    private int n2;

    private int bracket;

    private String dimension2pointerType;

    public ArrDeclStmt(boolean isGlobal, boolean isConst, boolean isFuncPara,ArrayList<ComputeStmt> value,int n1, int n2, int bracket, String name) {
        super(isGlobal,isConst,isFuncPara,null, name);
        this.n1 = n1;
        this.n2 = n2;
        this.value = value;
        this.bracket = bracket;
        this.dimension2pointerType = "[" + n2 + " x " + "i32]*";
    }

    public int getBracket() {
        return bracket;
    }

    public int getN1() {
        return n1;
    }

    public int getN2() {
        return n2;
    }

    public ArrayList<ComputeStmt> getValue() {
        return value;
    }

    public String getDimension2pointerType() {
        return dimension2pointerType;
    }
}
