package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;
import SymbolTablePackage.BlockSymbolTable;

import java.util.ArrayList;

public class MulExpStmt extends AddExpStmt{
    private ArrayList<UnaryExpStmt> unaryExpStmts;
    public MulExpStmt() {
        super(false,0);
    }

    private int value = 0;

    private boolean isNum = false;

    public ArrayList<UnaryExpStmt> departMulExp(GrammarNode mulExp) {
        this.unaryExpStmts = new ArrayList<>();
        for (GrammarNode node : mulExp.getNodes()) {
            if (node.getNodeName().equals("MulExp")) {
                MulExpStmt mulExpStmt = new MulExpStmt();
                ArrayList<UnaryExpStmt> tmp = mulExpStmt.departMulExp(node);
                unaryExpStmts.addAll(tmp);
            } else if (node instanceof Token token && token.compareLexType(LexType.MULT)){
                unaryExpStmts.add(new Op(LexType.MULT));
            } else if (node instanceof Token token && token.compareLexType(LexType.DIV)) {
                unaryExpStmts.add(new Op(LexType.DIV));
            } else if (node instanceof Token token && token.compareLexType(LexType.MOD)) {
                unaryExpStmts.add(new Op(LexType.MOD));
            }else if (node.getNodeName().equals("UnaryExp")) {
                UnaryExpStmt unaryExpStmt = new UnaryExpStmt(node);
                unaryExpStmts.add(unaryExpStmt);
            }
        }
        if (unaryExpStmts.size() == 1 && unaryExpStmts.get(0).getType() == 3) { //如果只是一个数，为了后面的llvm代码生成
            isNum = true;
            value = unaryExpStmts.get(0).getNumber();
        }
        return unaryExpStmts;
    }

    public int getConstValue(BlockSymbolTable fatherTable) {
        int sum = 1;//乘除运算
        Op last = new Op(LexType.MULT);//默认
        for (UnaryExpStmt unaryExpStmt : unaryExpStmts) {
            if (unaryExpStmt instanceof Op op) {
                last = op;
            } else {
                if (last.getLexType() == LexType.MULT) {
                    sum = sum * unaryExpStmt.getConstValue(fatherTable);
                } else if (last.getLexType() == LexType.DIV) {
                    sum = sum / unaryExpStmt.getConstValue(fatherTable);
                } else if (last.getLexType() == LexType.MOD) {
                    sum = sum % unaryExpStmt.getConstValue(fatherTable);
                }
            }
        }
        return sum;
    }

    public void setUnaryExpStmts(ArrayList<UnaryExpStmt> unaryExpStmts) {
        this.unaryExpStmts = unaryExpStmts;
    }

    public ArrayList<UnaryExpStmt> getUnaryExpStmts() {
        return unaryExpStmts;
    }

    public boolean isNum() {
        return isNum;
    }

    public int getValue() {
        return value;
    }
}
