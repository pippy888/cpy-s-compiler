package AST;

public class IfStmt extends Stmt{
    private CondStmt condStmt;

    private Stmt thenStmt;

    private Stmt elseStmt;

    public IfStmt(CondStmt condStmt, Stmt thenStmt, Stmt elseStmt) {
        this.condStmt = condStmt;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    public Stmt getElseStmt() {
        return elseStmt;
    }

    public Stmt getThenStmt() {
        return thenStmt;
    }

    public CondStmt getCondStmt() {
        return condStmt;
    }
}
