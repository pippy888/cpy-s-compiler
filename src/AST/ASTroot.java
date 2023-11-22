package AST;

import java.util.ArrayList;

public class ASTroot {
    private ArrayList<VarDeclStmt> varList;

    private ArrayList<FuncDeclStmt> funList;

    private FuncDeclStmt mainStmt;

    public ASTroot(ArrayList<VarDeclStmt> varList, ArrayList<FuncDeclStmt> funList, FuncDeclStmt mainStmt) {
        this.varList = varList;
        this.funList = funList;
        this.mainStmt = mainStmt;
    }

    public ArrayList<FuncDeclStmt> getFunList() {
        return funList;
    }

    public ArrayList<VarDeclStmt> getVarList() {
        return varList;
    }

    public FuncDeclStmt getMainStmt() {
        return mainStmt;
    }
}
