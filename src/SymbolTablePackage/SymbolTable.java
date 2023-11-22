package SymbolTablePackage;

public class SymbolTable {
    private int level;
    private SymbolType type;
    private String name;

    private int line;

    private String tag;

    public SymbolTable(int level, SymbolType type,String name,int line) {
        this.level = level;
        this.type = type;
        this.name = name;
        this.line = line;
        this.tag = null;//这里千万注意，符号表是之前就创建过的，
        // 为了避免后来的语句访问了还没有声明的变量，这里的type默认值是null，只有分配空间了，即声明过了，type才有值
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

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
