package AST;

import SymbolTablePackage.BlockSymbolTable;

public class VarDeclStmt extends Stmt{

    private boolean isGlobal;

    private boolean isConst;

    private boolean isFuncPara;

    private ComputeStmt initialValue; //非数组初始值

    private String name;

    public VarDeclStmt(boolean isGlobal, boolean isConst, boolean isFuncPara, ComputeStmt initial,String name) {
        this.isGlobal = isGlobal;
        this.isConst = isConst;
        this.isFuncPara = isFuncPara;
        this.initialValue = initial;
        this.name = name;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public String getName() {
        return this.name;
    }

    public boolean isConst() {
        return this.isConst;
    }

    public int getValue(BlockSymbolTable fatherTable) { //非数组
        return initialValue.getValue(fatherTable);
    }

    public boolean hasInitialValue() { //有的变量没有初始值
        return initialValue != null;
    }

    public ComputeStmt getInitialValue() {
        return initialValue;
    }
}
