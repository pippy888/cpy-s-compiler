package AST;

public class ReturnStmt extends Stmt{
    private ComputeStmt returnExp;

    public ReturnStmt(ComputeStmt returnExp) {
        this.returnExp = returnExp;
    }

    public boolean returnVoid() {
        return returnExp == null;
    }

    public ComputeStmt getReturnExp() {
        return returnExp;
    }
}
