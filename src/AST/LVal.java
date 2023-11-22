package AST;

public class LVal {
    private String LValName;

    private int bracket;

    private int n1;

    private int n2;

    public LVal(String name, int bracket, int n1, int n2) {
        this.LValName = name;
        this.bracket = bracket;
        this.n1 = n1;
        this.n2 = n2;
    }

    public String getLValName() {
        return LValName;
    }

    public int getBracket() {
        return bracket;
    }
}
