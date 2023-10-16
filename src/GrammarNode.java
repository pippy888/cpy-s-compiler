import java.util.ArrayList;

public class GrammarNode {
    private String nodeName;

    private ArrayList<GrammarNode> nodes;

    public GrammarNode(String nodeName,ArrayList<GrammarNode> nodes) {
        this.nodeName = nodeName;
        this.nodes = nodes;
    }

    public ArrayList<GrammarNode> getNodes() {
        return nodes;
    }

    public String getNodeName() {
        return nodeName;
    }
}
