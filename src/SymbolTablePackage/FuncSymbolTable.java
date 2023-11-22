package SymbolTablePackage;

import java.util.ArrayList;

public class FuncSymbolTable extends SymbolTable{

    private String returnType;

    private BlockSymbolTable params;

    public FuncSymbolTable(int level,SymbolType type, int line,
                           String name, String returnType, BlockSymbolTable params) {
        super(level,type,name,line);
        this.returnType = returnType;
        this.params = params;
    }

    public int getParasCount() {
        int count = 0;
        if (params == null) {
            return 0;
        }
        for (SymbolTable var : params.getSymbolTables()) {
            if (var instanceof VarSymbolTable) {
                if (((VarSymbolTable) var).isParameter()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getParaIndexOfType(int index) {
        return ((VarSymbolTable)params.getSymbolTables().get(index)).getBracket(); //一定是var型 而且函数参数一定是在最前面，个数匹配正确就没问题
    }

    public String getReturnType() {
        return returnType;
    }

    public void setParams(BlockSymbolTable blockSymbolTable) {
        params = blockSymbolTable;
    }

    public BlockSymbolTable getParams() {
        return params;
    }
}
