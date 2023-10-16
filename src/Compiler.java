import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        ArrayList<Token> tokens = getLexer();
        getGrammar(tokens);
    }

    public static ArrayList<Token> getLexer() {
        StringBuilder stringBuilder = IoFile.readFileByBytes("testfile.txt");
        if (stringBuilder != null) {
            // source:源程序字符串
            String source = stringBuilder.toString();
            Lexer lexer = new Lexer(source);
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

    public static void getGrammar(ArrayList<Token> tokens) {
        Grammar grammar = new Grammar(tokens);
        grammar.grammarStart();
    }
}
