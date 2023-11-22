package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;
import SymbolTablePackage.BlockSymbolTable;

import java.util.ArrayList;

public class ComputeStmt extends Stmt{

    private AddExpStmt addExpStmt;

    public ComputeStmt(AddExpStmt addExpStmt) {
        this.addExpStmt = addExpStmt;
    }

    public int getValue(BlockSymbolTable fatherTable) {
        return addExpStmt.getConstValue(fatherTable);
    }

    public AddExpStmt getAddExpStmt() {
        return this.addExpStmt;
    }
}
