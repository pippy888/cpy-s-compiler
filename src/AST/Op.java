package AST;


import Frontend.LexType;

public class Op extends UnaryExpStmt{
    private LexType lexType;

    public Op(LexType lexType) {
        super();
        this.lexType = lexType;
    }

    public LexType getLexType() {
        return lexType;
    }
}
