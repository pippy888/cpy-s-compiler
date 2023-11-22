package AST;

import Frontend.GrammarNode;

import java.util.ArrayList;

public class CondStmt {

    private LorExp lorExp;

    public CondStmt() {

    }

    public void setLorExp(GrammarNode node) {//这个node是Cond，提取出唯一个元素lorExp传入
        this.lorExp = new LorExp();
        lorExp.departLorExp(node.getNodes().get(0));
    }

    public LorExp getLorExp() {
        return lorExp;
    }
}
