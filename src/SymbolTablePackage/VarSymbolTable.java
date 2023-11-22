package SymbolTablePackage;

public class VarSymbolTable extends SymbolTable {

    private boolean isConst;

    private int n1;

    private int n2;

    private int bracket;

    private boolean isParameter;

    private int value;

    private boolean isGlobal;

    public VarSymbolTable(int level,SymbolType type,int line,boolean isConst,String name,int n1,int n2,int bracket,boolean isParameter,boolean isGlobal) {
        super(level,type,name,line);
        this.isConst = isConst;
        this.n1 = n1;
        this.n2 = n2;
        this.bracket = bracket;
        this.isParameter = isParameter;
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getBracket() {
        return bracket;
    }

    public boolean isParameter() {
        return isParameter;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
