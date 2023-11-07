public class Token extends GrammarNode{
    private String token;
    private int lineNum;
    private LexType lexType;

    public Token(String token,int lineNum,LexType lexType) {
        super(token,null);//叶节点nodes是空
        this.token = token;
        this.lineNum = lineNum;
        this.lexType = lexType;
    }

    public String getToken() {
        return token;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getLexType() {
        return lexType.toString();
    }

    public boolean compareLexType(LexType lexType) {
        return this.lexType == lexType;
    }
}
