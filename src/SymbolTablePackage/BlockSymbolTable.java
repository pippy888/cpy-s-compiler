package SymbolTablePackage;

import java.util.ArrayList;

public class BlockSymbolTable extends SymbolTable{
    private ArrayList<SymbolTable> symbolTables;

    private BlockSymbolTable fatherTable;
    public BlockSymbolTable (int level, int line, String name, BlockSymbolTable fatherTable) {
        super(level,SymbolType.blockSymbolTable,name,line);
        symbolTables = new ArrayList<>();
        this.fatherTable = fatherTable;
    }

    public ArrayList<SymbolTable> getSymbolTables() {
        return this.symbolTables;
    }

    public void add(SymbolTable symbolTable) {
        this.symbolTables.add(symbolTable);
    }

    public BlockSymbolTable getFatherTable() {
        return fatherTable;
    }

    public void setTagForVar(String name, String tag) {
        for (SymbolTable symbolTable : symbolTables) {
            if (symbolTable instanceof  VarSymbolTable && symbolTable.getName().equals(name)) {
                symbolTable.setTag(tag);
            }
        }
    }

    public VarSymbolTable searchVar(String name,BlockSymbolTable fatherTable,boolean define) {
        for (SymbolTable symbolTable : fatherTable.getSymbolTables()) {
            if (symbolTable instanceof  VarSymbolTable && symbolTable.getName().equals(name)) {
                if (!define && symbolTable.getTag() == null) {
                    //不是定义语句，是引用语句，而且引用了未定义的变量（type==null) 原因是当前块中可能新定义了什么一样的名字，但要去父块找，
                    // symbolTable.getTag() == null 约束了未定义变量
                    break;
                }
                return (VarSymbolTable) symbolTable;
            }
        }
        if (fatherTable.getFatherTable() != null) {
            return searchVar(name,fatherTable.getFatherTable(),define);
        } else {
            return null;
        }
    }

    public FuncSymbolTable searchFunction(String name,BlockSymbolTable mainTable) {
        for (SymbolTable symbolTable : mainTable.getSymbolTables()) {
            if (symbolTable instanceof  FuncSymbolTable && symbolTable.getName().equals(name)) {
                return (FuncSymbolTable) symbolTable;
            }
        }
        System.err.println("searchFunctionError!");
        return null;
    }

    public BlockSymbolTable searchBlockSymbolTable(int numberOfBlockSymbolTable, BlockSymbolTable fatherTable) {
        int hadFound = 0;
        for (SymbolTable symbolTable : fatherTable.getSymbolTables()) {
            if (symbolTable instanceof  BlockSymbolTable ) {
                if (hadFound == numberOfBlockSymbolTable) {
                    return (BlockSymbolTable) symbolTable;
                }
                else {
                    hadFound++;
                }
            }
        }
        return null;
    }
}


