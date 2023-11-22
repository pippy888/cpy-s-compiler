package AST;

import java.util.ArrayList;

public class ArrDeclStmt extends VarDeclStmt{
    private ArrayList<ComputeStmt> value;

    private int n1;

    private int n2;

    private int bracket;

    public ArrDeclStmt(boolean isGlobal, boolean isConst, boolean isFuncPara,ArrayList<ComputeStmt> value,int n1, int n2, int bracket, String name) {
        super(isGlobal,isConst,isFuncPara,null, name);
        this.n1 = n1;
        this.n2 = n2;
        this.value = value;
        this.bracket = bracket;
    }
}
