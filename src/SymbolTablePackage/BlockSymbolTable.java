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
}


