package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;

import java.util.ArrayList;

public class RelExp {
    private boolean isOp;

    private int type;
    //1 : ==
    //2 : !=

    private ArrayList<AddExpStmt> addExpStmts;

    private boolean isNum = false;

    private int value = 0;
    public RelExp(boolean isOp, int type) {
        this.isOp = isOp;
        this.type = type;
    }


    public ArrayList<AddExpStmt> departRelExp(GrammarNode node) {
        this.addExpStmts = new ArrayList<>();
        for (GrammarNode addExpNode : node.getNodes()) {
            if (addExpNode.getNodeName().equals("RelExp")) {
                RelExp relExp = new RelExp(false,0);
                ArrayList<AddExpStmt> tmp = relExp.departRelExp(addExpNode);
                addExpStmts.addAll(tmp);
            } else if (addExpNode instanceof Token token && token.compareLexType(LexType.LSS)) {
                addExpStmts.add(new AddExpStmt(true,1));
            } else if (addExpNode instanceof Token token && token.compareLexType(LexType.LEQ)) {
                addExpStmts.add((new AddExpStmt(true,2)));
            } else if (addExpNode instanceof Token token && token.compareLexType(LexType.GRE)) {
                addExpStmts.add((new AddExpStmt(true,3)));
            } else if (addExpNode instanceof Token token && token.compareLexType(LexType.GEQ)) {
                addExpStmts.add(new AddExpStmt(true,4));
            } else if (addExpNode.getNodeName().equals("AddExp")) {
                AddExpStmt addExpStmt = new AddExpStmt(false,0);
                addExpStmt.departAddExp(addExpNode);
                addExpStmts.add(addExpStmt);
            }
        }

        if (addExpStmts.size() == 1 && addExpStmts.get(0).isNum()) {
            this.isNum = true;
            this.value = addExpStmts.get(0).getValue();
        }
        return addExpStmts;
    }

    public ArrayList<AddExpStmt> getAddExpStmts() {
        return addExpStmts;
    }

    public boolean isNum() {
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
