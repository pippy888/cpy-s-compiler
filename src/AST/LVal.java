package AST;

public class LVal {
    private String LValName;

    private int bracket;

    private ComputeStmt n1;
    private int n1_const;//因为全局数组和常量数组要直接计算，其中数组下标是常数
    private ComputeStmt n2;
    private int n2_const;

    public LVal(String name, int bracket, ComputeStmt n1, ComputeStmt n2) {
        this.LValName = name;
        this.bracket = bracket;
        this.n1 = n1;
        this.n2 = n2;
//        this.n1_const = n1_const;
//        this.n2_const = n2_const;
    }

    public String getLValName() {
        return LValName;
    }

    public int getBracket() {
        return bracket;
    }

    public ComputeStmt getN1() {
        return n1;
    }

    public ComputeStmt getN2() {
        return n2;
    }
}
