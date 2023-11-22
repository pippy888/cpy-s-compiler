package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;

import java.util.ArrayList;

public class EqExp {

    private ArrayList<RelExp> relExps;

    private boolean isNum = false;

    private int value = 0;

    public ArrayList<RelExp> departEqExp(GrammarNode node) {
        this.relExps = new ArrayList<>();
        for (GrammarNode eqExpNode : node.getNodes()) {
            if (eqExpNode.getNodeName().equals("EqExp")) {
                EqExp eqExp = new EqExp();
                ArrayList<RelExp> tmp = eqExp.departEqExp(eqExpNode);
                relExps.addAll(tmp);
            } else if (eqExpNode instanceof Token token && token.compareLexType(LexType.EQL)){
                relExps.add(new RelExp(true,1));
            } else if (eqExpNode instanceof Token token && token.compareLexType(LexType.NEQ)){
                relExps.add(new RelExp(true,2));
            }else if (eqExpNode.getNodeName().equals("RelExp")) {
                RelExp relExp = new RelExp(false,0);
                relExp.departRelExp(eqExpNode);
                relExps.add(relExp);
            }
        }
        if (relExps.size() == 1 && relExps.get(0).isNum()) {
            isNum = true;
            value = relExps.get(0).getValue();
        }
        return relExps;
    }

    public ArrayList<RelExp> getRelExps() {
        return relExps;
    }

    public int getValue() {
        return value;
    }

    public boolean isNum() {
        return isNum;
    }
}
