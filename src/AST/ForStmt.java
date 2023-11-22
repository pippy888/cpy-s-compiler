package AST;

public class ForStmt extends Stmt{
    private AssignStmt assignStmt1;
    //1

    private CondStmt condStmt;
    //2

    private AssignStmt assignStmt2;
    //3

    public ForStmt(LVal lVal1, LVal lVal2, ComputeStmt exp1,ComputeStmt exp2, CondStmt condStmt) {
        this.assignStmt1 = new AssignStmt(false,lVal1,exp1);
        this.assignStmt2 = new AssignStmt(false,lVal2,exp2);
        this.condStmt = condStmt;
    }

}
