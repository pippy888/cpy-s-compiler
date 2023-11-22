package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;

import java.util.ArrayList;

public class LAndExp {

    private ArrayList<EqExp> eqExps;

    private boolean isNum = false;

    private int value = 0;

    public LAndExp() {

    }

    public ArrayList<EqExp> departLAndExp(GrammarNode node) {
        this.eqExps = new ArrayList<>();
        for (GrammarNode lAndExpNode : node.getNodes()) {
            if (lAndExpNode.getNodeName().equals("LAndExp")) {
                LAndExp lAndExp = new LAndExp();
                ArrayList<EqExp> tmp = lAndExp.departLAndExp(lAndExpNode);
                eqExps.addAll(tmp);
            } else if (lAndExpNode instanceof Token token && token.compareLexType(LexType.AND)){
                continue;
            } else if (lAndExpNode.getNodeName().equals("EqExp")) {
                EqExp eqExp = new EqExp();
                eqExp.departEqExp(lAndExpNode);
                this.eqExps.add(eqExp);
            }
        }
        if (eqExps.size() == 1 && eqExps.get(0).isNum()) {
            this.isNum = true;
            this.value = eqExps.get(0).getValue();
        }
        return this.eqExps;
    }

    public ArrayList<EqExp> getEqExps() {
        return eqExps;
    }

    public boolean isNum() {
        return isNum;
    }

    public int getValue() {
        return value;
    }
}
