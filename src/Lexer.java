import java.util.HashMap;
import java.util.regex.Pattern;

public class Lexer {
    private String source;
    private int curPos;
    private int line;

    private char c;
    private HashMap<String, LexType> reserveWords;

    public Lexer(String source) {
        this.source = source;
        this.curPos = 0;
        this.line = 1;
        reserveWords = new HashMap<>();
        reserveWordsBook();
    }

    public void reserveWordsBook() {
        reserveWords.put("Ident", LexType.IDENFR);
        reserveWords.put("!", LexType.NOT);
        reserveWords.put("*", LexType.MULT);
        reserveWords.put("=", LexType.ASSIGN);
        reserveWords.put("IntConst", LexType.INTCON);
        reserveWords.put("&&", LexType.AND);
        reserveWords.put("/", LexType.DIV);
        reserveWords.put(";", LexType.SEMICN);
        reserveWords.put("FormatString", LexType.STRCON);
        reserveWords.put("||", LexType.OR);
        reserveWords.put("%", LexType.MOD);
        reserveWords.put(",", LexType.COMMA);
        reserveWords.put("main", LexType.MAINTK);
        reserveWords.put("for", LexType.FORTK);
        reserveWords.put("<", LexType.LSS);
        reserveWords.put("(", LexType.LPARENT);
        reserveWords.put("const", LexType.CONSTTK);
        reserveWords.put("getint", LexType.GETINTTK);
        reserveWords.put("<=", LexType.LEQ);
        reserveWords.put(")", LexType.RPARENT);
        reserveWords.put("int", LexType.INTTK);
        reserveWords.put("printf", LexType.PRINTFTK);
        reserveWords.put(">", LexType.GRE);
        reserveWords.put("[", LexType.LBRACK);
        reserveWords.put("break", LexType.BREAKTK);
        reserveWords.put("return", LexType.RETURNTK);
        reserveWords.put(">=", LexType.GEQ);
        reserveWords.put("]", LexType.RBRACK);
        reserveWords.put("continue", LexType.CONTINUETK);
        reserveWords.put("+", LexType.PLUS);
        reserveWords.put("==", LexType.EQL);
        reserveWords.put("{", LexType.LBRACE);
        reserveWords.put("if", LexType.IFTK);
        reserveWords.put("-", LexType.MINU);
        reserveWords.put("!=", LexType.NEQ);
        reserveWords.put("}", LexType.RBRACE);
        reserveWords.put("else", LexType.ELSETK);
        reserveWords.put("void", LexType.VOIDTK);
    }

    public Token next() {
        while(isSpace()) {
            getChar();
        }
        if (isDigit()) {
            return getDigit();
        }
        if (isLetter() || c =='_') {
            return getIdentOrReserveWord();
        }

        if (c == '!') {
            getChar();
            if (c == '=') {
                getChar();
                return new Token("!=", line, LexType.NEQ);
            } else {
                return new Token("!",line,LexType.NOT);
            }
        }

        if (c == '=') {
            getChar();
            if (c == '=') {
                getChar();
                return new Token("==",line,LexType.EQL);
            } else {
                return new Token("=",line,LexType.ASSIGN);
            }
        }

        if (c == '+') {
            getChar();
            return new Token("+",line,LexType.PLUS);
        }

        if (c =='-') {
            getChar();
            return new Token("-",line,LexType.MINU);
        }

        if(c == '*') {
            getChar();
            return new Token("*",line,LexType.MULT);
        }
        if(c == '/') {
            getChar();
            if (c == '/') {
                solveLineComments();
                return next();
            } else if(c == '*') {
                solveBlockComments();
                return next();
            } else {
                return new Token("/",line,LexType.DIV);
            }
        }

        if (c == '&') {
            getChar();
            if (c == '&') {
                getChar();
                return new Token("&&",line,LexType.AND);
            }
        }

        if (c == '|') {
            getChar();
            if (c == '|') {
                getChar();
                return new Token("||", line,LexType.OR);
            }
        }

        if (c == ';') {
            getChar();
            return new Token(";",line,LexType.SEMICN);
        }

        if (c == '"') {
            return solveFormatString();
        }

        if (c == '%') {
            getChar();
            return new Token("%",line,LexType.MOD);
        }

        if (c == ',') {
            getChar();
            return new Token(",",line,LexType.COMMA);
        }

        if (c == '<') {
            getChar();
            if (c == '=') {
                getChar();
                return new Token("<=",line,LexType.LEQ);
            } else {
                return new Token("<",line,LexType.LSS);
            }
        }

        if (c == '>') {
            getChar();
            if (c == '=') {
                getChar();
                return new Token(">=",line,LexType.GEQ);
            } else {
                return new Token(">",line,LexType.GRE);
            }
        }

        if (c == '(') {
            getChar();
            return new Token("(",line,LexType.LPARENT);
        }

        if (c == ')') {
            getChar();
            return new Token(")",line,LexType.RPARENT);
        }

        if (c == '[') {
            getChar();
            return new Token("[",line,LexType.LBRACK);
        }

        if (c == ']') {
            getChar();
            return new Token("]",line,LexType.RBRACK);
        }

        if (c == '{') {
            getChar();
            return new Token("{",line,LexType.LBRACE);
        }

        if (c == '}') {
            getChar();
            return new Token("}",line,LexType.RBRACE);
        }

        if (c == '\0') {
            return new Token("\0",line,null);
        }
        //debug
        System.out.println("no found!");
        return null;
    }

    public void begin() {
        getChar();
    }

    public boolean isSpace() {
        if (c == '\n') {
            this.line++;
        }
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    public void getChar() {
        c = source.charAt(curPos++);
    }

    public boolean isLetter() {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public boolean isDigit() {
        return c >= '0' && c <= '9';
    }

    public Token getIdentOrReserveWord() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(c);
        getChar();
        while (isLetter() || c == '_' || isDigit()) {
            stringBuilder.append(c);
            getChar();
        }
        String string = stringBuilder.toString();
        LexType lexType = reserveWords.getOrDefault(string,null);
        if (lexType != null) {
            return new Token(string,this.line,lexType);
        } else {
            return new Token(string,this.line,LexType.IDENFR);
        }
    }

    public Token getDigit() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(c);
        getChar();
        while (isDigit()) {
            stringBuilder.append(c);
            getChar();
        }
        String string = stringBuilder.toString();
        return new Token(string,this.line,LexType.INTCON);
    }

    public void solveLineComments() {
        getChar();
        while(c != '\n') {
            getChar();
        }
        line++;
        getChar();
    }

    public void solveBlockComments() {
        getChar();
        while(true) {
            if(c != '*') {
                if (c == '\n') {
                    line++;
                }
                getChar();
            }
            else {
                getChar();
                if(c == '/') {
                    break;
                } else {
                    continue;
                }
            }
        }
        getChar();
    }

    public Token solveFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('"');
        getChar();
        while(c != '"') {
            if(c == '\\') {
                getChar();
                if (c == 'n') {
                    stringBuilder.append('\\');
                    stringBuilder.append('n');
                }
            } else {
                stringBuilder.append(c);
            }
            getChar();
        }
        stringBuilder.append('"');
        String s = stringBuilder.toString();
        getChar();
        return new Token(s,line,LexType.STRCON);
    }
}
