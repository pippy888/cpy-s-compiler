package AST;

import Ir.IRInstr.FuncDeclIR;

import java.util.ArrayList;

public class FuncDeclStmt extends Stmt{

    private String name;

    private String returnType;

    private ArrayList<VarDeclStmt> paras;

    private CompoundStmt stmts;

    public FuncDeclStmt(String name, String returnType, ArrayList<VarDeclStmt> paras, CompoundStmt stmts) {
        super();
        this.name = name;
        this.returnType = returnType;
        this.paras = paras;
        this.stmts = stmts;
    }

    public String getName() {
        return this.name;
    }

    public CompoundStmt getStmts() {
        return stmts;
    }

    public String getReturnType() {
        return returnType;
    }

    public ArrayList<VarDeclStmt> getParas() {
        return paras;
    }

    public FuncDeclIR getIr() {
        return new FuncDeclIR(name,returnType,paras);
    }

}
