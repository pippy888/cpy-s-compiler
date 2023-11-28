package AST;

public class ForStmt extends Stmt{
    private AssignStmt assignStmt1;
    //1

    private CondStmt condStmt;
    //2

    private AssignStmt assignStmt2;
    //3

    private Stmt forStmt;

    public ForStmt(LVal lVal1, LVal lVal2, ComputeStmt exp1,ComputeStmt exp2, CondStmt condStmt, Stmt forStmt) {
        this.assignStmt1 = new AssignStmt(false,lVal1,exp1);
        this.assignStmt2 = new AssignStmt(false,lVal2,exp2);
        this.condStmt = condStmt;
        this.forStmt = forStmt;
    }

    public CondStmt getCondStmt() {
        return condStmt;
    }

    public AssignStmt getAssignStmt1() {
        return assignStmt1;
    }

    public AssignStmt getAssignStmt2() {
        return assignStmt2;
    }

    public Stmt getForStmt() {
        return forStmt;
    }
}
