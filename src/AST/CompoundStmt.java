package AST;

import java.util.ArrayList;

public class CompoundStmt extends Stmt{
    private ArrayList<Stmt> stmts;

    public CompoundStmt(ArrayList<Stmt> stmts) {
        this.stmts = stmts;
    }

    public ArrayList<Stmt> getStmts() {
        return stmts;
    }
}

