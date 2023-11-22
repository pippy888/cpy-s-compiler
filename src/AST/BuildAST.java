package AST;
import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;

import java.util.ArrayList;
/*
注意！这里仅仅对于非数组仅仅保留了计算式未求值，而对于数组元素则需要可能出现的常量求值，这里还未处理，特征10处，.getValue(null);
 */
public class BuildAST {
    private GrammarNode main;

    public BuildAST(GrammarNode main) {
        this.main = main;
    }

    public ASTroot getRoot() {
        ArrayList<VarDeclStmt> globalVarDeclStmt = new ArrayList<>();
        ArrayList<FuncDeclStmt> funcDeclStmts = new ArrayList<>();
        FuncDeclStmt mainFuncDef = null;
        for (GrammarNode rootNode : main.getNodes()) {
            if (compareNode(rootNode,"Decl")) {
                getVarDeclStmt(true,false,globalVarDeclStmt,rootNode.getNodes().get(0));
            } else if (compareNode(rootNode,"FuncDef")){
                getFuncDef(funcDeclStmts, rootNode);//最后一个定义的函数是main
            } else if (compareNode(rootNode,"MainFuncDef")) {
                mainFuncDef = getMainFuncDef(rootNode);
            }
        }

        return new ASTroot(globalVarDeclStmt,funcDeclStmts,mainFuncDef);
    }

    public void getVarDeclStmt(boolean isGlobal, boolean isFuncPara, ArrayList<VarDeclStmt> list, GrammarNode node) {
        if (compareNode(node,"ConstDecl")) {
            for (GrammarNode constNode : node.getNodes()) {
                if (compareNode(constNode,"ConstDef")) {
                    list.add(getVarDef(constNode,isGlobal,true,isFuncPara));
                }
            }
        } else {
            for (GrammarNode varNode : node.getNodes()) {
                if (compareNode(varNode,"VarDef")) {
                    list.add(getVarDef(varNode,isGlobal,false,isFuncPara));
                }
            }
        }
    }

    public VarDeclStmt getVarDef(GrammarNode node,boolean isGlobal, boolean isConst, boolean isFuncPara) { //const 和 var 后面的def都一样
        String name = node.getNodes().get(0).getNodeName();//token 和 getNodeName有一样的值
        if (node.getNodes().size() == 1) {
            return new VarDeclStmt(isGlobal,isConst,isFuncPara,null,name);
        }
        else if (!assertNodeIsTokenAndCompare(node.getNodes().get(node.getNodes().size()-2),LexType.ASSIGN)) {
            //倒二个不是=
            if (assertNodeIsTokenAndCompare(node.getNodes().get(1),LexType.LBRACK )) {
                //数组
                if (node.getNodes().size() == 4) {
                    //一维数组
                    GrammarNode constExp = node.getNodes().get(2);
                    ComputeStmt constStmt = getComputerStmt(constExp);
                    int constValue = constStmt.getValue(null);

                    return new ArrDeclStmt(isGlobal,isConst,isFuncPara,null,0,constValue,1,name);
                } else if (node.getNodes().size() == 7) {
                    GrammarNode constExp1 = node.getNodes().get(2);
                    ComputeStmt constStmt1 = getComputerStmt(constExp1);
                    int constValue1 = constStmt1.getValue(null);

                    GrammarNode constExp2 = node.getNodes().get(5);
                    ComputeStmt constStmt2 = getComputerStmt(constExp2);
                    int constValue2 = constStmt2.getValue(null);

                    return new ArrDeclStmt(isGlobal,isConst,isFuncPara,null,constValue1,constValue2,2,name);
                }
            }
        } else {
            if (node.getNodes().size() == 3) {
                //ident = initial
                ComputeStmt initial = getComputerStmt(node.getNodes().get(node.getNodes().size()-1).getNodes().get(0));
                //initial->Exp
                return new VarDeclStmt(isGlobal,isConst,isFuncPara,initial,name);
            } else if (node.getNodes().size() == 6) {
                GrammarNode constExp = node.getNodes().get(2);
                ComputeStmt constStmt = getComputerStmt(constExp);
                int constValue = constStmt.getValue(null);

                GrammarNode initial = node.getNodes().get(5);
                ArrayList<ComputeStmt> list = getInitialValue(initial);

                return new ArrDeclStmt(isGlobal,isConst,isFuncPara,list,0,constValue,1,name);
            } else if (node.getNodes().size() == 9) {
                GrammarNode constExp1 = node.getNodes().get(2);
                ComputeStmt constStmt1 = getComputerStmt(constExp1);
                int constValue1 = constStmt1.getValue(null);

                GrammarNode constExp2 = node.getNodes().get(5);
                ComputeStmt constStmt2 = getComputerStmt(constExp2);
                int constValue2 = constStmt2.getValue(null);

                GrammarNode initial = node.getNodes().get(9);
                ArrayList<ComputeStmt> list = getInitialValue(initial);

                return new ArrDeclStmt(isGlobal,isConst,isFuncPara,list,constValue1,constValue2,2,name);
            }
        }
        return null;
    }

    public static ComputeStmt getComputerStmt(GrammarNode node) { //Exp
        GrammarNode addExp = node.getNodes().get(0);// Exp->addExp
        AddExpStmt addExpStmt = new AddExpStmt(false,0);
        addExpStmt.departAddExp(addExp);
        ComputeStmt computeStmt = new ComputeStmt(addExpStmt);
        return computeStmt;
    }

    public ArrayList<ComputeStmt> getInitialValue(GrammarNode node) {
        ArrayList<ComputeStmt> result = new ArrayList<>();
        if (node.getNodes().size() == 1) {
            result.add(getComputerStmt(node.getNodes().get(0)));
        } else {
            for (GrammarNode initialNode : node.getNodes()) {
                if (compareNode(initialNode,"InitVal") ||
                compareNode(initialNode,"ConstInitVal")) {
                    result.addAll(getInitialValue(initialNode));
                }
            }
        }
        return result;
    }

    public void getFuncDef(ArrayList<FuncDeclStmt> funcDeclStmts,GrammarNode node) {
        String returnType = node.getNodes().get(0).getNodes().get(0).getNodeName();
        String name = node.getNodes().get(1).getNodeName();
        ArrayList<VarDeclStmt> paras = null;
        CompoundStmt compoundStmt = getCompoundStmt(returnLastNode(node));
        //block总是在最后一个
        if (node.getNodes().size() == 6) {
            //有参数
             paras = getParas(node.getNodes().get(3));
        }
        funcDeclStmts.add(new FuncDeclStmt(name,returnType,paras,compoundStmt));
    }

    public FuncDeclStmt getMainFuncDef(GrammarNode node) {
        CompoundStmt compoundStmt = getCompoundStmt(returnLastNode(node));
        return new FuncDeclStmt("main","int",null,compoundStmt);
    }

    public CompoundStmt getCompoundStmt(GrammarNode node) {
        ArrayList<Stmt> stmts = new ArrayList<>();
        for (GrammarNode blockItem : node.getNodes()) {
            if (blockItem instanceof Token) {
                continue;//{}
            }
            GrammarNode inner = blockItem.getNodes().get(0);
            if (inner.getNodeName().equals("Stmt")) {
                Stmt tmp = getStmt(inner);
                if (tmp == null) {
                    continue;
                }
                stmts.add(tmp);
            } else if (inner.getNodeName().equals("Decl")){
                //Decl
                ArrayList<VarDeclStmt> tmp = new ArrayList<>();
                getVarDeclStmt(false,false,tmp,inner.getNodes().get(0));
                stmts.addAll(tmp);
            }
        }
        CompoundStmt compoundStmt = new CompoundStmt(stmts);
        return compoundStmt;
    }

    public Stmt getStmt(GrammarNode node) {
        ArrayList<GrammarNode> stmts = node.getNodes();
        GrammarNode head = stmts.get(0);
        if (head instanceof Token token) {
            if (token.compareLexType(LexType.IFTK)) {
                CondStmt condStmt = new CondStmt();
                GrammarNode condNode = stmts.get(2);
                condStmt.setLorExp(condNode);
                Stmt thenStmt = getStmt(stmts.get(4));
                Stmt elseStmt = null;
                if (node.getNodes().size() > 5) {
                    elseStmt = getStmt(stmts.get(6));
                }
                return new IfStmt(condStmt,thenStmt,elseStmt);
            } else if (token.compareLexType(LexType.FORTK)) {
                //寻找封号
                int semicn1 = 0;
                int semicn2 = 0;
                for (GrammarNode tmpNode : stmts) {
                    if (tmpNode instanceof Token tokenFor && tokenFor.compareLexType(LexType.SEMICN)) {
                        if (semicn1 == 0) {
                            semicn1 = stmts.indexOf(tmpNode);
                        } else {
                            semicn2 = stmts.indexOf(tmpNode);
                        }
                    }
                }
                LVal lVal1;
                LVal lVal2;
                ComputeStmt computeStmt1;
                ComputeStmt computeStmt2;
                CondStmt condStmt;
                if (stmts.get(semicn1-1) instanceof Token) {
                    //第一个封号前是（，没有forstmt
                    lVal1 = null;
                    computeStmt1 = null;
                } else {
                    lVal1 = getLVal(stmts.get(semicn1-1).getNodes().get(0));
                    computeStmt1 = getComputerStmt(stmts.get(semicn1-1).getNodes().get(2));

                    //第一个赋值语句
                }
                //1
                if (stmts.get(semicn1+1) instanceof Token) {
                    //第一个封号后是;
                    condStmt = null;
                } else {
                    condStmt = new CondStmt();
                    condStmt.setLorExp(stmts.get(semicn1+1));
                }
                //2
                if (stmts.get(semicn2+1) instanceof Token) {
                    lVal2 = null;
                    computeStmt2 = null;
                } else {
                    lVal2 = getLVal(stmts.get(semicn2+1).getNodes().get(0));
                    computeStmt2 = getComputerStmt(stmts.get(semicn2+1).getNodes().get(2));
                }
                //3
                ForStmt forStmt = new ForStmt(lVal1,lVal2,computeStmt1,computeStmt2,condStmt);
                return forStmt;
            } else if (token.compareLexType(LexType.BREAKTK)) {
                return new BreakStmt();
            } else if (token.compareLexType(LexType.CONTINUETK)) {
                return new ContinueStmt();
            } else if (token.compareLexType(LexType.PRINTFTK)) {
                String format = ((Token)stmts.get(2)).getToken();
                ArrayList<ComputeStmt> paras = new ArrayList<>();
                if (stmts.get(3) instanceof Token printfToken && printfToken.compareLexType(LexType.COMMA)) {
                    //format后面是逗号，说明有参数
                    for (int i = 3; i < stmts.size(); i++) {
                        if (stmts.get(i).getNodeName().equals("Exp")) {
                            paras.add(getComputerStmt(stmts.get(i)));
                        }
                    }
                }
                return new CallExpr("printf",paras,format);
            } else if (token.compareLexType(LexType.RETURNTK)) {
                if (stmts.size() == 2) {
                    return new ReturnStmt(null);
                } else {
                    ComputeStmt computeStmt = getComputerStmt(stmts.get(1));
                    return new ReturnStmt(computeStmt);
                }
            } else if (token.compareLexType(LexType.SEMICN)) {
                return null;
            } else {
                //未能检测出来
                System.err.println("getStmtError!");
                return null;
            }
        } //首node是token
        else {
            if (compareNode(head,"LVal")) {
                LVal lVal = getLVal(stmts.get(0));
                AssignStmt assignStmt;
                if (stmts.get(2) instanceof Token tokenGetInt && tokenGetInt.compareLexType(LexType.GETINTTK)) {
                    assignStmt = new AssignStmt(true,lVal,null);
                } else {
                    ComputeStmt computeStmt = getComputerStmt(stmts.get(2));
                    assignStmt = new AssignStmt(false, lVal,computeStmt);
                }
                return assignStmt;
            } else if(compareNode(head,"Block")) {
                 return getCompoundStmt(head);
            } else if (compareNode(head,"Exp")) {
                return getComputerStmt(head);
            } else {
                System.err.println("getStmtError!");
                return null;
            }
        }
    }

    public LVal getLVal(GrammarNode priNode) { //priNode是LVal节点
        String name = priNode.getNodes().get(0).getNodeName();
        if (priNode.getNodes().size() == 1) {
            return new LVal(name,0,0,0);
        }
        else if (priNode.getNodes().size() == 4) {
            //一维数组
            GrammarNode constExp = priNode.getNodes().get(2);
            ComputeStmt constStmt = BuildAST.getComputerStmt(constExp);
            int constValue = constStmt.getValue(null);
            return new LVal(name,1,0,constValue);

        } else if (priNode.getNodes().size() == 7) {
            GrammarNode constExp1 = priNode.getNodes().get(2);
            ComputeStmt constStmt1 = BuildAST.getComputerStmt(constExp1);
            int constValue1 = constStmt1.getValue(null);

            GrammarNode constExp2 = priNode.getNodes().get(5);
            ComputeStmt constStmt2 = BuildAST.getComputerStmt(constExp2);
            int constValue2 = constStmt2.getValue(null);

            return  new LVal(name,2,constValue1,constValue2);
        } else {
            System.err.println("getLValError!");
        }
        return null;
    }

    public ArrayList<VarDeclStmt> getParas(GrammarNode node) {
        ArrayList<VarDeclStmt> paras = new ArrayList<>();
        for (GrammarNode funcFParam : node.getNodes()) {
            if (!(funcFParam instanceof Token)) { //不是逗号，是参数
                ArrayList<GrammarNode> parasInfo = funcFParam.getNodes();
                String name = parasInfo.get(1).getNodeName();
                if (parasInfo.size() == 2) {
                    VarDeclStmt var = new VarDeclStmt(false,false,true,null,name);
                    paras.add(var);
                } else if (parasInfo.size() == 4) {
                    ArrDeclStmt arr = new ArrDeclStmt(false,false,true,null,0,0,1,name);
                    paras.add(arr);
                } else if (parasInfo.size() == 7) {
                    int n2;
                    ComputeStmt constN2 = getComputerStmt(parasInfo.get(5));
                    n2 = constN2.getValue(null);
                    ArrDeclStmt arr = new ArrDeclStmt(false,false,true,null,0,n2,2,name);
                    paras.add(arr);
                }
            }
        }
        return paras;
    }

    public boolean compareNode(GrammarNode node, String s) {
        return node.getNodeName().equals(s);
    }

    public boolean assertNodeIsTokenAndCompare(GrammarNode node, LexType lexType) {
        Token token = (Token) node;
        return token.compareLexType(lexType);
    }

    public Token assertNodeIsTokenAndTransfer(GrammarNode node) {
        return (Token) node;
    }


    public GrammarNode returnLastNode(GrammarNode node) {
        return node.getNodes().get(node.getNodes().size()-1);
    }
}
