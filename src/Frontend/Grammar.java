package Frontend;

import java.util.ArrayList;

public class Grammar {
    private ArrayList<Token> tokens;

    private int nowIndex;

    private int tokenLength;

    private ExceptionController ec;

    public Grammar(ArrayList<Token> tokens, ExceptionController ec) {
        this.tokens = tokens;
        nowIndex = 0;
        tokenLength = tokens.size();
        this.ec = ec;
    }

    public boolean nowTokenTypeCompare(int index,LexType lexType) {
        return tokens.get(index).compareLexType(lexType);
    }

    public GrammarNode getCompUnit() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        //先检测是不是全局声明
        while (tokenLength - nowIndex > 2 &&
                (nowTokenTypeCompare(nowIndex,LexType.CONSTTK) ||
                        (nowTokenTypeCompare(nowIndex,LexType.INTTK) && !nowTokenTypeCompare(nowIndex+1,LexType.MAINTK) && !nowTokenTypeCompare(nowIndex+2,LexType.LPARENT)
                        )
                )
        ) {
            //第一层：确保不越界
            //第二层，如果开头是const，肯定是全局声明
            //第三层，如果是int开头，后面紧跟着不能是main，其次后面第二个不能是左括号
            //这样就可以确保是全局变量声明
            grammarNodes.add(getDecl());
        }

        //检测函数
        while (tokenLength - nowIndex > 2 &&
                (nowTokenTypeCompare(nowIndex,LexType.INTTK) || nowTokenTypeCompare(nowIndex,LexType.VOIDTK))
                    && !nowTokenTypeCompare(nowIndex+1,LexType.MAINTK)
                        && nowTokenTypeCompare(nowIndex+2,LexType.LPARENT)
        ) {
            grammarNodes.add(getFuncDef());
        }

        grammarNodes.add(getMainFuncDef());
        return new GrammarNode("CompUnit",grammarNodes);
    }

    public GrammarNode getDecl() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (nowTokenTypeCompare(nowIndex,LexType.CONSTTK)) {
            grammarNodes.add(getConstDecl());
        } else if (nowTokenTypeCompare(nowIndex,LexType.INTTK)){
            grammarNodes.add(getVarDecl());
        } else {
            System.err.println();
        }
        return new GrammarNode("Decl",grammarNodes);
    }

    public GrammarNode getConstDecl() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.CONSTTK));
        grammarNodes.add(getBType());
        grammarNodes.add(getConstDef());
        while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.COMMA)) {
            grammarNodes.add(getLeaf(LexType.COMMA));
            grammarNodes.add(getConstDef());
        }
        GrammarNode tmpNode;
        if ((tmpNode = getLeaf(LexType.SEMICN)) != null) {
            grammarNodes.add(tmpNode);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
        }
        return new GrammarNode("ConstDecl",grammarNodes);
    }

    public GrammarNode getVarDecl() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getBType());
        grammarNodes.add(getVarDef());
        while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.COMMA)) {
            grammarNodes.add(getLeaf(LexType.COMMA));
            grammarNodes.add(getVarDef());
        }
        GrammarNode tmpNode;
        if ((tmpNode = getLeaf(LexType.SEMICN)) != null) {
            grammarNodes.add(tmpNode);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
        }
        return new GrammarNode("VarDecl",grammarNodes);
    }

    public GrammarNode getLeaf(LexType lexType){
        Token token = tokens.get(nowIndex);
        if (!token.compareLexType(lexType)) {
            // System.err.println("getLeafErr,should be type--" + lexType+ " nowIndex: " + nowIndex + "\n"+errTokenMessage());
            return null;
        } else {
            nowIndex++; // 应该要假设没有丢失右括号或者封号 所以上面的不能++index
            return token;
        }
    }

    public GrammarNode getBType() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.INTTK));
        return new GrammarNode("BType",grammarNodes);
    }

    public GrammarNode getConstDef() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.IDENFR));
        while(whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LBRACK)) {
            grammarNodes.add(getLeaf(LexType.LBRACK));
            grammarNodes.add(getConstExp());
            GrammarNode tmpNode;
            if ((tmpNode = getLeaf(LexType.RBRACK)) != null) {
                grammarNodes.add(tmpNode);
            } else {
                ec.addException(returnException(tokens.get(nowIndex-1),"k",LexType.RBRACK));
            }
        }
        grammarNodes.add(getLeaf(LexType.ASSIGN));
        grammarNodes.add(getConstInitVal());
        return new GrammarNode("ConstDef",grammarNodes);
    }

    public GrammarNode getConstExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getAddExp());
        return new GrammarNode("ConstExp",grammarNodes);
    }

    public GrammarNode getConstInitVal() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LBRACE)) {
            grammarNodes.add(getLeaf(LexType.LBRACE));
            if (whetherOutOfBound() && !nowTokenTypeCompare(nowIndex,LexType.RBRACE)) {
               grammarNodes.add(getConstInitVal());
               while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.COMMA)) {
                   grammarNodes.add(getLeaf(LexType.COMMA));
                   grammarNodes.add(getConstInitVal());
               }
               grammarNodes.add(getLeaf(LexType.RBRACE));
            }
        }
        else {
            grammarNodes.add(getConstExp());
        }
        return new GrammarNode("ConstInitVal",grammarNodes);
    }

    public GrammarNode getVarDef() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.IDENFR));
        while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LBRACK)) {
            grammarNodes.add(getLeaf(LexType.LBRACK));
            grammarNodes.add(getConstExp());
            GrammarNode tmpNode;
            if ((tmpNode = getLeaf(LexType.RBRACK)) != null) {
                grammarNodes.add(tmpNode);
            } else {
                ec.addException(returnException(tokens.get(nowIndex-1),"k",LexType.RBRACK));
            }
        }
        if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.ASSIGN)) {
            grammarNodes.add(getLeaf(LexType.ASSIGN));
            grammarNodes.add(getInitVal());
        }
        return new GrammarNode("VarDef",grammarNodes);
    }

    public GrammarNode getInitVal() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LBRACE)) {
            grammarNodes.add(getLeaf(LexType.LBRACE));
            if (whetherOutOfBound() && !nowTokenTypeCompare(nowIndex,LexType.RBRACE)) {
                grammarNodes.add(getInitVal());
                while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.COMMA)) {
                    grammarNodes.add(getLeaf(LexType.COMMA));
                    grammarNodes.add(getInitVal());
                }
            }
            grammarNodes.add(getLeaf(LexType.RBRACE));
        } else {
            grammarNodes.add(getExp());
        }
        return new GrammarNode("InitVal",grammarNodes);
    }

    public GrammarNode getFuncDef() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getFuncType());
        grammarNodes.add(getLeaf(LexType.IDENFR));
        grammarNodes.add(getLeaf(LexType.LPARENT));
        if (whetherOutOfBound() && !(nowTokenTypeCompare(nowIndex,LexType.RPARENT) || nowTokenTypeCompare(nowIndex,LexType.LBRACE))) { //无参少右括号情况
            grammarNodes.add(getFuncFParams());
        }
        GrammarNode tmpNode;
        if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
            grammarNodes.add(tmpNode);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
        }
        grammarNodes.add(getBlock());
        return new GrammarNode("FuncDef",grammarNodes);
    }

    public GrammarNode getFuncType() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (nowTokenTypeCompare(nowIndex,LexType.INTTK)) {
            grammarNodes.add(getLeaf(LexType.INTTK));
        } else if (nowTokenTypeCompare(nowIndex,LexType.VOIDTK)) {
            grammarNodes.add(getLeaf(LexType.VOIDTK));
        } else {
            System.err.println("getFuncTypeErr " + errTokenMessage());
        }
        return new GrammarNode("FuncType",grammarNodes);
    }

    public GrammarNode getFuncFParams() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getFuncFParam());
        while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.COMMA)) {
            grammarNodes.add(getLeaf(LexType.COMMA));
            grammarNodes.add(getFuncFParam());
        }
        return new GrammarNode("FuncFParams",grammarNodes);
    }

    public GrammarNode getFuncFParam() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getBType());
        grammarNodes.add(getLeaf(LexType.IDENFR));
        if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LBRACK)) {
            grammarNodes.add(getLeaf(LexType.LBRACK));
            GrammarNode tmpNode;
            if ((tmpNode = getLeaf(LexType.RBRACK)) != null) {
                grammarNodes.add(tmpNode);
            } else {
                ec.addException(returnException(tokens.get(nowIndex-1),"k",LexType.RBRACK));
            }
            while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LBRACK)) {
                grammarNodes.add(getLeaf(LexType.LBRACK));
                grammarNodes.add(getConstExp());
                GrammarNode tmpNode2;
                if ((tmpNode2 = getLeaf(LexType.RBRACK)) != null) {
                    grammarNodes.add(tmpNode2);
                } else {
                    ec.addException(returnException(tokens.get(nowIndex-1),"k",LexType.RBRACK));
                }
            }
        }
        return new GrammarNode("FuncFParam",grammarNodes);
    }

    public GrammarNode getMainFuncDef() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.INTTK));
        grammarNodes.add(getLeaf(LexType.MAINTK));
        grammarNodes.add(getLeaf(LexType.LPARENT));
        GrammarNode tmpNode;
        if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
            grammarNodes.add(tmpNode);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
        }
        grammarNodes.add(getBlock());
        return new GrammarNode("MainFuncDef",grammarNodes);
    }

    public GrammarNode getBlock() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.LBRACE));
        while (whetherOutOfBound() && !nowTokenTypeCompare(nowIndex,LexType.RBRACE)) {
            grammarNodes.add(getBlockItem());
        }
        try {
            grammarNodes.add(getLeaf(LexType.RBRACE));
        } catch (Exception e) {
            System.err.println();
        }
        return new GrammarNode("Block",grammarNodes);
    }

    public GrammarNode getBlockItem() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (whetherOutOfBound() &&
                (nowTokenTypeCompare(nowIndex,LexType.CONSTTK) || nowTokenTypeCompare(nowIndex,LexType.INTTK))
        ) {
            grammarNodes.add(getDecl());
        } else {
            grammarNodes.add(getStmt());
        }
        return new GrammarNode("BlockItem",grammarNodes);
    }

    public GrammarNode getStmt() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (whetherOutOfBound()) {
            if (nowTokenTypeCompare(nowIndex,LexType.SEMICN)) {
                grammarNodes.add(getLeaf(LexType.SEMICN));
            } else if (nowTokenTypeCompare(nowIndex,LexType.LBRACE)) {
                grammarNodes.add(getBlock());
            } else if (nowTokenTypeCompare(nowIndex,LexType.IFTK)) {
                solveIfStmt(grammarNodes);
            } else if (nowTokenTypeCompare(nowIndex,LexType.FORTK)) {
                solveForStmt(grammarNodes);
            } else if (nowTokenTypeCompare(nowIndex,LexType.BREAKTK)) {
                grammarNodes.add(getLeaf(LexType.BREAKTK));
                GrammarNode tmpNode;
                if ((tmpNode = getLeaf(LexType.SEMICN)) != null) {
                    grammarNodes.add(tmpNode);
                } else {
                    ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
                }
            } else if (nowTokenTypeCompare(nowIndex,LexType.CONTINUETK)) {
                grammarNodes.add(getLeaf(LexType.CONTINUETK));
                GrammarNode tmpNode;
                if ((tmpNode = getLeaf(LexType.SEMICN)) != null) {
                    grammarNodes.add(tmpNode);
                } else {
                    ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
                }
            } else if (nowTokenTypeCompare(nowIndex,LexType.RETURNTK)) {
                grammarNodes.add(getLeaf(LexType.RETURNTK));
                if (whetherOutOfBound() && !(nowTokenTypeCompare(nowIndex,LexType.SEMICN) || nowTokenTypeCompare(nowIndex,LexType.RBRACE))){
                    grammarNodes.add(getExp());
                }
                GrammarNode tmpNode;
                if ((tmpNode = getLeaf(LexType.SEMICN)) != null) {
                    grammarNodes.add(tmpNode);
                } else {
                    ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
                }
            } else if (nowTokenTypeCompare(nowIndex,LexType.PRINTFTK)) {
                solvePrintfStmt(grammarNodes);
            } else {
                //LVal '=' Exp ';'
                //Exp ';' //有Exp的情况 只有一个;的情况前面写了
                // LVal '=' 'getint''('')'';'
                if (tokenLength - nowIndex > 1 && searchAssignForStmt()) {
                    //说明是LVal的情况
                    grammarNodes.add(getLVal());
                    grammarNodes.add(getLeaf(LexType.ASSIGN));
                    if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.GETINTTK)) {
                        grammarNodes.add(getLeaf(LexType.GETINTTK));
                        grammarNodes.add(getLeaf(LexType.LPARENT));

                        GrammarNode tmpNode;
                        if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
                            grammarNodes.add(tmpNode);
                        } else {
                            ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
                        }

                        GrammarNode tmpNode2;
                        if ((tmpNode2 = getLeaf(LexType.SEMICN)) != null) {
                            grammarNodes.add(tmpNode2);
                        } else {
                            ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
                        }

                    } else {
                        grammarNodes.add(getExp());
                        GrammarNode tmpNode;
                        if ((tmpNode = getLeaf(LexType.SEMICN)) != null) {
                            grammarNodes.add(tmpNode);
                        } else {
                            ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
                        }
                    }
                } else {
                    // 说明是Exp的情况
                    grammarNodes.add(getExp());
                    GrammarNode tmpNode;
                    if ((tmpNode = getLeaf(LexType.SEMICN)) != null) {
                        grammarNodes.add(tmpNode);
                    } else {
                        ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
                    }
                }
            }
        }
        return new GrammarNode("Stmt",grammarNodes);
    }

    public void solveIfStmt(ArrayList<GrammarNode> grammarNodes) {
        grammarNodes.add(getLeaf(LexType.IFTK));
        grammarNodes.add(getLeaf(LexType.LPARENT));
        grammarNodes.add(getCond());
        GrammarNode tmpNode;
        if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
            grammarNodes.add(tmpNode);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
        }
        grammarNodes.add(getStmt());
        if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.ELSETK)) {
            grammarNodes.add(getLeaf(LexType.ELSETK));
            grammarNodes.add(getStmt());
        }
    }

    public GrammarNode getCond() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLOrExp());
        return new GrammarNode("Cond",grammarNodes);
    }

    public void solveForStmt(ArrayList<GrammarNode> grammarNodes) {
        grammarNodes.add(getLeaf(LexType.FORTK));
        grammarNodes.add(getLeaf(LexType.LPARENT));
        if (whetherOutOfBound() && !nowTokenTypeCompare(nowIndex,LexType.SEMICN)) {
            grammarNodes.add(getForStmt());
        }
        grammarNodes.add(getLeaf(LexType.SEMICN));
        if (whetherOutOfBound() && !nowTokenTypeCompare(nowIndex,LexType.SEMICN)) {
            grammarNodes.add(getCond());
        }
        grammarNodes.add(getLeaf(LexType.SEMICN));
        if (whetherOutOfBound() && !nowTokenTypeCompare(nowIndex,LexType.RPARENT)) {
            grammarNodes.add(getForStmt());
        }
        GrammarNode tmpNode;
        if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
            grammarNodes.add(tmpNode);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
        }
        grammarNodes.add(getStmt());
    }

    public GrammarNode getForStmt() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLVal());
        grammarNodes.add(getLeaf(LexType.ASSIGN));
        grammarNodes.add(getExp());
        return new GrammarNode("ForStmt",grammarNodes);
    }

    public void solvePrintfStmt(ArrayList<GrammarNode> grammarNodes) {
        grammarNodes.add(getLeaf(LexType.PRINTFTK));
        grammarNodes.add(getLeaf(LexType.LPARENT));
        grammarNodes.add(getLeaf(LexType.STRCON));
        while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.COMMA)) {
            grammarNodes.add(getLeaf(LexType.COMMA));
            grammarNodes.add(getExp());
        }
        GrammarNode tmpNode;
        if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
            grammarNodes.add(tmpNode);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
        }
        GrammarNode tmpNode2;
        if ((tmpNode2 = getLeaf(LexType.SEMICN)) != null) {
            grammarNodes.add(tmpNode2);
        } else {
            ec.addException(returnException(tokens.get(nowIndex-1),"i",LexType.SEMICN));
        }
    }

    public GrammarNode getLVal() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.IDENFR));
        while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LBRACK)) {
            grammarNodes.add(getLeaf(LexType.LBRACK));
            grammarNodes.add(getExp());
            GrammarNode tmpNode;
            if ((tmpNode = getLeaf(LexType.RBRACK)) != null) {
                grammarNodes.add(tmpNode);
            } else {
                ec.addException(returnException(tokens.get(nowIndex-1),"k",LexType.RBRACK));
            }
        }
        return new GrammarNode("LVal",grammarNodes);
    }

    public GrammarNode getExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getAddExp());
        return new GrammarNode("Exp",grammarNodes);
    }

    //基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number
    public GrammarNode getPrimaryExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LPARENT)) {
            grammarNodes.add(getLeaf(LexType.LPARENT));
            grammarNodes.add(getExp());
            GrammarNode tmpNode;
            if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
                grammarNodes.add(tmpNode);
            } else {
                ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
            }
        } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.INTCON)) {
            grammarNodes.add(getNumber());
        } else {
            grammarNodes.add(getLVal());
        }
        return new GrammarNode("PrimaryExp",grammarNodes);
    }

    public GrammarNode getNumber() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLeaf(LexType.INTCON));
        return new GrammarNode("Number",grammarNodes);
    }

    //一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'| UnaryOp UnaryExp
    public GrammarNode getUnaryExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (nowIndex + 1 < tokenLength &&
                nowTokenTypeCompare(nowIndex,LexType.IDENFR) &&
                    nowTokenTypeCompare(nowIndex + 1, LexType.LPARENT)
        ) {
            grammarNodes.add(getLeaf(LexType.IDENFR));
            grammarNodes.add(getLeaf(LexType.LPARENT));
            if (whetherOutOfBound() && !(nowTokenTypeCompare(nowIndex,LexType.RPARENT) || nowTokenTypeCompare(nowIndex,LexType.SEMICN))) {
                grammarNodes.add(getFuncRParams());
            }
            GrammarNode tmpNode;
            if ((tmpNode = getLeaf(LexType.RPARENT)) != null) {
                grammarNodes.add(tmpNode);
            } else {
                ec.addException(returnException(tokens.get(nowIndex-1),"j",LexType.RPARENT));
            }
        } else if (whetherOutOfBound() &&
                (nowTokenTypeCompare(nowIndex,LexType.PLUS) ||
                        nowTokenTypeCompare(nowIndex,LexType.MINU) ||
                            nowTokenTypeCompare(nowIndex,LexType.NOT))
        ){
            grammarNodes.add(getUnaryOp());
            grammarNodes.add(getUnaryExp());
        } else {
            grammarNodes.add(getPrimaryExp());
        }
        return new GrammarNode("UnaryExp",grammarNodes);
    }

    public GrammarNode getFuncRParams() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getExp());
        while (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.COMMA)) {
            grammarNodes.add(getLeaf(LexType.COMMA));
            grammarNodes.add(getExp());
        }
        return new GrammarNode("FuncRParams",grammarNodes);
    }

    public GrammarNode getUnaryOp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.PLUS)) {
            grammarNodes.add(getLeaf(LexType.PLUS));
        } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.MINU)) {
            grammarNodes.add(getLeaf(LexType.MINU));
        } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.NOT)) {
            grammarNodes.add(getLeaf(LexType.NOT));
        } else {
            System.err.println(errTokenMessage());
        }
        return new GrammarNode("UnaryOp",grammarNodes);
    }

    //加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
    public GrammarNode getAddExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getMulExp());
        while (whetherOutOfBound() &&
                (nowTokenTypeCompare(nowIndex,LexType.PLUS) ||
                    nowTokenTypeCompare(nowIndex,LexType.MINU)
                )
        ) {
            ArrayList<GrammarNode> tmp = (ArrayList<GrammarNode>)grammarNodes.clone();
            GrammarNode tmpNode = new GrammarNode("AddExp",tmp);
            grammarNodes.clear();
            grammarNodes.add(tmpNode);
            if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.PLUS)) {
                grammarNodes.add(getLeaf(LexType.PLUS));
            } else {
                grammarNodes.add(getLeaf(LexType.MINU));
            }
            grammarNodes.add(getMulExp());
        }
        return new GrammarNode("AddExp",grammarNodes);
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public GrammarNode getMulExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getUnaryExp());
        while (whetherOutOfBound() &&
                (nowTokenTypeCompare(nowIndex,LexType.MULT) ||
                        nowTokenTypeCompare(nowIndex,LexType.DIV)||
                                nowTokenTypeCompare(nowIndex,LexType.MOD)
            )
        ) {
            ArrayList<GrammarNode> tmp = (ArrayList<GrammarNode>)grammarNodes.clone();
            GrammarNode tmpNode = new GrammarNode("MulExp",tmp);
            grammarNodes.clear();
            grammarNodes.add(tmpNode);
            if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.MULT)) {
                grammarNodes.add(getLeaf(LexType.MULT));
            } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.DIV)) {
                grammarNodes.add(getLeaf(LexType.DIV));
            } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.MOD)) {
                grammarNodes.add(getLeaf(LexType.MOD));
            }
            grammarNodes.add(getUnaryExp());
        }
        return new GrammarNode("MulExp",grammarNodes);
    }

    //关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    public GrammarNode getRelExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getAddExp());
        while (whetherOutOfBound() &&
                (nowTokenTypeCompare(nowIndex,LexType.LSS) ||
                        nowTokenTypeCompare(nowIndex,LexType.LEQ) ||
                        nowTokenTypeCompare(nowIndex,LexType.GRE) ||
                        nowTokenTypeCompare(nowIndex,LexType.GEQ)
                )
        ) {
            ArrayList<GrammarNode> tmp = (ArrayList<GrammarNode>)grammarNodes.clone();
            GrammarNode tmpNode = new GrammarNode("RelExp",tmp);
            grammarNodes.clear();
            grammarNodes.add(tmpNode);
            if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LSS)) {
                grammarNodes.add(getLeaf(LexType.LSS));
            } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.LEQ)) {
                grammarNodes.add(getLeaf(LexType.LEQ));
            } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.GRE)) {
                grammarNodes.add(getLeaf(LexType.GRE));
            } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.GEQ)) {
                grammarNodes.add(getLeaf(LexType.GEQ));
            }
            grammarNodes.add(getAddExp());
        }
        return new GrammarNode("RelExp",grammarNodes);
    }
    // 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
    public GrammarNode getEqExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getRelExp());
        while (whetherOutOfBound() &&
                (nowTokenTypeCompare(nowIndex,LexType.EQL) ||
                        nowTokenTypeCompare(nowIndex,LexType.NEQ)
                )
        ) {
            ArrayList<GrammarNode> tmp = (ArrayList<GrammarNode>)grammarNodes.clone();
            GrammarNode tmpNode = new GrammarNode("EqExp",tmp);
            grammarNodes.clear();
            grammarNodes.add(tmpNode);
            if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.EQL)) {
                grammarNodes.add(getLeaf(LexType.EQL));
            } else if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.NEQ)) {
                grammarNodes.add(getLeaf(LexType.NEQ));
            }
            grammarNodes.add(getRelExp());
        }
        return new GrammarNode("EqExp",grammarNodes);
    }

    //逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
    public GrammarNode getLAndExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getEqExp());
        while (whetherOutOfBound() &&
                nowTokenTypeCompare(nowIndex,LexType.AND)
        ) {
            ArrayList<GrammarNode> tmp = (ArrayList<GrammarNode>)grammarNodes.clone();
            GrammarNode tmpNode = new GrammarNode("LAndExp",tmp);
            grammarNodes.clear();
            grammarNodes.add(tmpNode);
            if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.AND)) {
                grammarNodes.add(getLeaf(LexType.AND));
            }
            grammarNodes.add(getEqExp());
        }
        return new GrammarNode("LAndExp",grammarNodes);
    }

    //逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
    public GrammarNode getLOrExp() {
        ArrayList<GrammarNode> grammarNodes = new ArrayList<>();
        grammarNodes.add(getLAndExp());
        while (whetherOutOfBound() &&
                nowTokenTypeCompare(nowIndex,LexType.OR)
        ) {
            ArrayList<GrammarNode> tmp = (ArrayList<GrammarNode>)grammarNodes.clone();
            GrammarNode tmpNode = new GrammarNode("LOrExp",tmp);
            grammarNodes.clear();
            grammarNodes.add(tmpNode);
            if (whetherOutOfBound() && nowTokenTypeCompare(nowIndex,LexType.OR)) {
                grammarNodes.add(getLeaf(LexType.OR));
            }
            grammarNodes.add(getLAndExp());
        }
        return new GrammarNode("LOrExp",grammarNodes);
    }
    public String errTokenMessage() {
        return "errorIndexInfo:\ntoken name is: " + tokens.get(nowIndex).getToken() + ", errorToken in file's line: "+tokens.get(nowIndex).getLineNum();
    }

    public boolean whetherOutOfBound() {
        if (nowIndex >= tokenLength) {
            System.err.println("lost more tokens!");
        }
        return nowIndex < tokenLength;
    }

    public GrammarNode grammarStart() {
        GrammarNode main = getCompUnit();
        StringBuilder output = new StringBuilder();
        outputParser(main,output);
        IoFile.outputContentToFile(output.toString());
        return main;
    }

    public void outputParser(GrammarNode main,StringBuilder output) {
        ArrayList<GrammarNode> nodes = main.getNodes();
        for (GrammarNode node : nodes) {
            if (node.getNodes() == null) { //查询到叶节点
                Token token = (Token)node;
                output.append(token.getLexType()).append(" ").append(token.getToken()).append('\n');
                //System.out.println(token.getLexType() + " " + token.getToken());
            }
            else {
                outputParser(node,output);
            }
        }
        if (main.getNodeName().equals("BlockItem") ||
                main.getNodeName().equals("Decl") ||
                    main.getNodeName().equals("BType")
        ) {
            return;
        } else {
            //System.out.println();
            //Frontend.IoFile.outputContentToFile();
            output.append('<').append(main.getNodeName()).append('>').append('\n');
        }
    }

    public boolean searchAssignForStmt() {
        boolean hasAssign = false;
        for (int i = 0; nowIndex + i < tokenLength; i++) {
            if (nowTokenTypeCompare(nowIndex + i,LexType.ASSIGN)) {
                return true;
            } else if (nowTokenTypeCompare(nowIndex + i, LexType.SEMICN)){
                return false;
            }
        }
        return hasAssign;
    }

    public MyException returnException(Token token,String errorType, LexType lexType) {
        String errorInfo = "getLeafErr,should be type--" + lexType +
                " nowIndex and errorIndexInfo: " + nowIndex + "\n"+errTokenMessage();
        return new MyException(token.getLineNum(), errorType, errorInfo);
    }
}
