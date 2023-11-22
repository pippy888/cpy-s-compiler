package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;

import java.util.ArrayList;

public class LorExp {
    private ArrayList<LAndExp> lAndExps;

    public LorExp() {

    }

    public ArrayList<LAndExp> departLorExp(GrammarNode node) {
        //LorExp
        this.lAndExps = new ArrayList<>();
        for (GrammarNode lAndExpNode : node.getNodes()) {
            if (lAndExpNode.getNodeName().equals("LOrExp")) {
                LorExp lorExp = new LorExp();
                ArrayList<LAndExp> tmp = lorExp.departLorExp(lAndExpNode);
                lAndExps.addAll(tmp);
            } else if (lAndExpNode instanceof Token token && token.compareLexType(LexType.OR)) {
                continue;//总是||
            } else if (lAndExpNode.getNodeName().equals("LAndExp")) {
                LAndExp lAndExp = new LAndExp();
                lAndExp.departLAndExp(lAndExpNode);
                this.lAndExps.add(lAndExp);
            }
        }
        return lAndExps;
    }

    public ArrayList<LAndExp> getlAndExps() {
        return this.lAndExps;
    }
}
