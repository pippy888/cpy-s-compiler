package SymbolTablePackage;

public class SymbolTable {
    private int level;
    private SymbolType type;
    private String name;

    private int line;

    public SymbolTable(int level, SymbolType type,String name,int line) {
        this.level = level;
        this.type = type;
        this.name = name;
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public int getLine() {
        return this.line;
    }

    public int getLevel() {
        return level;
    }

    public boolean compareType(SymbolType symbolType) {
        return this.type == symbolType;
    }
}
