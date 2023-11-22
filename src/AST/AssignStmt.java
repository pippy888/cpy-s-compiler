package AST;

public class AssignStmt extends Stmt{
    private LVal lVal;

    private boolean isGetInt;
    private ComputeStmt computeStmt;

    public AssignStmt(boolean isGetInt, LVal lVal, ComputeStmt computeStmt) {
        this.lVal = lVal;
        this.computeStmt = computeStmt;
        this.isGetInt = isGetInt;
    }

    public ComputeStmt getComputeStmt() {
        return computeStmt;
    }

    public boolean isGetInt() {
        return isGetInt;
    }

    public LVal getlVal() {
        return lVal;
    }
}
