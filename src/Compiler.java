import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        ExceptionController ec = new ExceptionController();
        ArrayList<Token> tokens = getLexer(ec);
        GrammarNode ast = getGrammar(tokens,ec);
        VisitAST visitAST = new VisitAST(ast,ec);
        visitAST.getSymbolTableAndHandleError();
    }

    public static ArrayList<Token> getLexer(ExceptionController ec) {
        StringBuilder stringBuilder = IoFile.readFileByBytes("testfile.txt");
        if (stringBuilder != null) {
            // source:源程序字符串
            String source = stringBuilder.toString();
            Lexer lexer = new Lexer(source,ec);
            lexer.begin();
            Token token = lexer.next();
            StringBuilder output = new StringBuilder();
            ArrayList<Token> tokens = new ArrayList<>();
            while (!token.getToken().equals("\0")) {
                // System.out.println(token.getLexType() + " " + token.getToken());
                output.append(token.getLexType()).append(" ").append(token.getToken()).append('\n');
                tokens.add(token);
                token = lexer.next();
            }
            IoFile.outputContentToFile_testTokens(output.toString());
            return tokens;
        }
        return null;
    }

    public static GrammarNode getGrammar(ArrayList<Token> tokens,ExceptionController ec) {
        Grammar grammar = new Grammar(tokens,ec);
        GrammarNode ast = grammar.grammarStart();
        return ast;
    }
}
