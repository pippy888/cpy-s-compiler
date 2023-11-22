package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;
import SymbolTablePackage.BlockSymbolTable;

import java.util.ArrayList;

public class AddExpStmt {
    private ArrayList<MulExpStmt> mulExpStmts;

    private boolean isOp;

    private int type;
    /*
    1: LSS <
    2: LEQ <=
    3: GRE >
    4: GEQ >=
     */

    private boolean isNum = false;

    private int value = 0;

    public AddExpStmt(boolean isOp, int type) {
        this.isOp = isOp;
        this.type = type;
    }

    public ArrayList<MulExpStmt> departAddExp(GrammarNode addExp) {
        this.mulExpStmts = new ArrayList<>();
        for (GrammarNode node : addExp.getNodes()) {
            if (node.getNodeName().equals("AddExp")) {
                AddExpStmt addExpStmt = new AddExpStmt(false,0);
                ArrayList<MulExpStmt> tmp = addExpStmt.departAddExp(node);
                mulExpStmts.addAll(tmp);
            } else if (node instanceof Token token && token.compareLexType(LexType.PLUS)){
                mulExpStmts.add(new Op(LexType.PLUS));
            } else if (node instanceof Token token && token.compareLexType(LexType.MINU)) {
                mulExpStmts.add(new Op(LexType.MINU));
            } else if (node.getNodeName().equals("MulExp")) {
                MulExpStmt mulExpStmt = new MulExpStmt();
                mulExpStmt.departMulExp(node);
                mulExpStmts.add(mulExpStmt);
            }
        }
        if (mulExpStmts.size() == 1 && mulExpStmts.get(0).isNum()) {
            this.isNum = true;
            this.value = mulExpStmts.get(0).getValue();
        }
        return mulExpStmts;
    }

    public int getConstValue(BlockSymbolTable fatherTable) {//mul unary重写
        int sum = 0;//加减运算
        Op last = new Op(LexType.PLUS);
        for (MulExpStmt mulExpStmt : this.mulExpStmts) {
            if (mulExpStmt instanceof Op op) {
                last = op;
            } else {
                if (last.getLexType() == LexType.PLUS) {
                    sum = sum + mulExpStmt.getConstValue(fatherTable);
                } else if (last.getLexType() == LexType.MINU) {
                    sum = sum - mulExpStmt.getConstValue(fatherTable);
                }
            }
        }
        return sum;
    }

    public void setMulExpStmts(ArrayList<MulExpStmt> mulExpStmts) {
        this.mulExpStmts = mulExpStmts;
    }

    public ArrayList<MulExpStmt> getMulExpStmts() {
        return mulExpStmts;
    }

    public boolean isNum() { //mul重写了
        return isNum;
    }

    public int getValue() {
        return value;
    }

    public boolean isOp() {
        return isOp;
    }

    public int getType() {
        return type;
    }
}
