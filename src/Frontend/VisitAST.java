package Frontend;

import SymbolTablePackage.BlockSymbolTable;
import SymbolTablePackage.FuncSymbolTable;
import SymbolTablePackage.SymbolTable;
import SymbolTablePackage.SymbolType;
import SymbolTablePackage.Value;
import SymbolTablePackage.VarSymbolTable;

import java.util.ArrayList;

public class VisitAST {
    private ExceptionController ec;

    private GrammarNode ast;

    private int nowLevel = 0;

    private BlockSymbolTable stackSymbol;


    public VisitAST(GrammarNode ast, ExceptionController ec) {
        this.ast = ast;
        this.ec = ec;
        this.stackSymbol = new BlockSymbolTable(0,0,"global",null);
        //把整个文件，全局？看作一个代码块
    }

    public BlockSymbolTable getSymbolTableAndHandleError() {
        runAST();
        String output = outputEcError();
        IoFile.outputContentToFile_error(output);
        return stackSymbol;
    }

    public void runAST() {
        for (GrammarNode node : ast.getNodes()) {
            if (node.getNodeName().equals("Decl")) {
                handleDecl(this.stackSymbol,node,true);
            } else if (node.getNodeName().equals("FuncDef")) {
                handleFuncDef(this.stackSymbol,node);
            } else if (node.getNodeName().equals("MainFuncDef")) {
                handleMainFuncDef(this.stackSymbol,node);
            }
        }
    }

    public void handleDecl(BlockSymbolTable fatherTable,GrammarNode node,boolean isGlobal) {
        int line = 0;
        boolean isConst = false;
        SymbolType type = SymbolType.var;
        String name = null;
        int n1 = 0;
        int n2 = 0;
        int bracket = 0;
        if (compareNodeName(node,0,"ConstDecl")) {
            isConst = true;
        }
        else {
            isConst = false;
        }
        //node.getNodes().get(0) 就是ConstDecl
        GrammarNode ConstOrVarDecl = node.getNodes().get(0);
        for (int i = 0; i < ConstOrVarDecl.getNodes().size(); i++) {
            if (compareNodeName(ConstOrVarDecl, i,"ConstDef")
                || compareNodeName(ConstOrVarDecl, i,"VarDef")
            ) {
                GrammarNode nodeConstDef = ConstOrVarDecl.getNodes().get(i);
                for (GrammarNode tmp : nodeConstDef.getNodes()) {
                    Token token;
                    if (tmp instanceof Token) {
                        token = (Token) tmp;
                        if (token.compareLexType(LexType.IDENFR)) {
                            line = token.getLineNum();
                            name = token.getToken();
                        } else if (token.compareLexType(LexType.LBRACK)) {
                            bracket++;
                        }  /* 数字可以是初始化值 可能要结合赋值符号判断
                        else if (token.compareLexType(Frontend.LexType.INTCON)) {
                            if (bracket == 1) {
                                n1 = Integer.parseInt(token.getToken());
                            } else if (bracket == 2) {
                                n2 = Integer.parseInt(token.getToken());
                            }
                        }
                        */
                    }
                }
                VarSymbolTable var = new VarSymbolTable(this.nowLevel,type,line,isConst,name,n1,n2,bracket,false,isGlobal);
                addSymbol(fatherTable,var);
            }
        }
    }

    public void handleFuncDef(BlockSymbolTable fatherTable, GrammarNode node) {
        int funcLine = 0;
        String returnType = null;
        String funcName = null;
        FuncSymbolTable funcSymbolTable = null;
        BlockSymbolTable paras = null; //函数块表
        Token token = null;
        for (GrammarNode funcDefNode : node.getNodes()) {
            if (funcDefNode.getNodeName().equals("FuncType")) {
                token = (Token) funcDefNode.getNodes().get(0);
                returnType = token.getToken();
            } else if (funcDefNode instanceof Token && ((Token)funcDefNode).compareLexType(LexType.IDENFR)) {
                token = (Token) funcDefNode;
                funcName = token.getToken();
                funcLine = token.getLineNum();
                paras = new BlockSymbolTable(nowLevel,funcLine,"paras",fatherTable);
                funcSymbolTable = new FuncSymbolTable(nowLevel,SymbolType.function,funcLine,funcName,returnType,paras);
                addSymbol(fatherTable,funcSymbolTable); //先加入符号表，防止定义递归调用，后面只有para，调用setpara方法设置
                nowLevel++; //
            } else if (funcDefNode.getNodeName().equals("FuncFParams")) {
                boolean hasFuncPara = false; //最后一个函数参数录入符号表
                String paraName = null;
                int paraLine = 0;
                int bracket = 0;
                int n1 = 0;
                int n2 = 0;
                for (GrammarNode para : funcDefNode.getNodes()) {
                    if (para.getNodeName().equals("FuncFParam")) {
                        bracket = 0; //更新brack
                        n1 = 0;
                        n2 = 0;
                        for (GrammarNode paraDetail : para.getNodes()) {
                            if (!(paraDetail instanceof Token)) {
                                continue;
                            }
                            token = (Token) paraDetail;
                            if (token.compareLexType(LexType.IDENFR)) {
                                paraName = token.getToken();
                                paraLine = token.getLineNum();
                                hasFuncPara = true;
                            } else if (token.compareLexType(LexType.LBRACK)) {
                                bracket++;
                            }/* else if (token.compareLexType(Frontend.LexType.INTCON)) { //不管数组值
                                if (bracket == 1) {
                                    n1 = Integer.parseInt(token.getToken());
                                } else if(bracket == 2){
                                    n2 = Integer.parseInt(token.getToken());
                                }
                            }*/
                        }
                    }  else if (para instanceof Token && ((Token) para).compareLexType(LexType.COMMA)) {
                        // 检测到逗号的时候
                        VarSymbolTable paraVar = new VarSymbolTable(nowLevel,SymbolType.var,paraLine,false,paraName,n1,n2,bracket,true,false);
                        addSymbol(paras,paraVar);
                    }
                }
                if (hasFuncPara) { // 最后一个参数
                    VarSymbolTable paraVar = new VarSymbolTable(nowLevel,SymbolType.var,paraLine,false,paraName,n1,n2,bracket,true,false);
                    addSymbol(paras,paraVar);
                }
                funcSymbolTable.setParams(paras);
            } else if (funcDefNode.getNodeName().equals("Block")) {
                handleBlock(funcDefNode,returnType,paras);
            }
        } // for遍历funcDef
    }// 函数主体

    public void handleMainFuncDef(BlockSymbolTable fatherTable, GrammarNode node) {
        GrammarNode mainBlock = node.getNodes().get(node.getNodes().size()-1);
        nowLevel++;
        BlockSymbolTable mainFuncSymbolTable = new BlockSymbolTable(nowLevel,((Token) node.getNodes().get(1)).getLineNum(),"main",fatherTable);
        //main函数没参数
        handleBlock(mainBlock,"int",mainFuncSymbolTable);
        addSymbol(fatherTable,mainFuncSymbolTable);
    }

    // 调用其他函数使用handleBlock的时候，记得returnType应该要继承父returnType
    public void handleBlock(GrammarNode block,String returnType,BlockSymbolTable fatherTable) {
        boolean hasReturn = false;
        Token RBRACK = (Token) block.getNodes().get(block.getNodes().size()-1);
        for (int i = 1; i < block.getNodes().size()-1; i++) {
            // 头尾是{} 中间是blockItem保证存在getNodes
            GrammarNode node = block.getNodes().get(i). getNodes().get(0);
            //blockItem中是Decl或者Stmt
            if (node.getNodeName().equals("Decl")) {
                handleDecl(fatherTable,node,false);
            } else { //Stmt
                handleStmt(node,returnType,fatherTable);
                if (i == block.getNodes().size()-2){
                    hasReturn = handleFuncLastTokenWhetherToken(node,returnType,fatherTable);
                }
            }
        }
        //未发现return && int是返回值 returnType已经继承
        if (!hasReturn && returnType.equals("int")) {
            ec.addException(HandleException.makeNoReturnException(RBRACK));
        }
        nowLevel--;
    }

    public boolean handleStmt(GrammarNode node,String returnType,BlockSymbolTable fatherTable) {
        boolean hasReturn = false; //hasReturn没用了，原来已经规定了return语句只能在函数末尾 草
        boolean tmpHasReturn = false;
        GrammarNode stmtDetailNode = node.getNodes().get(0);
        if (stmtDetailNode.getNodeName().equals("LVal")) { //似乎只要第一个是LVal就一定会被赋值？
            Token tokenLVal =  (Token) stmtDetailNode.getNodes().get(0);
            String name = tokenLVal.getToken();
            int line = tokenLVal.getLineNum();
            VarSymbolTable var = (VarSymbolTable) findAllSymbolTable(name,line,fatherTable,SymbolType.var);
            //Frontend.Token tokenAssign = (Frontend.Token) stmtDetailNode.getNodes().get(1);
            if (var != null && var.isConst()) { //改const
                ec.addException(HandleException.makeReviseConstException(tokenLVal));
            }

            if (node.getNodes().get(node.getNodes().size() - 1).getNodeName().equals("Exp")) {
                handleExp(node.getNodes().get(node.getNodes().size() - 1),fatherTable);
            }
        } else if (stmtDetailNode.getNodeName().equals("Exp")) {
            handleExp(stmtDetailNode,fatherTable);
        } else if (stmtDetailNode instanceof Token token && token.compareLexType(LexType.FORTK)) {
            nowLevel++;
            BlockSymbolTable forBlock = new BlockSymbolTable(nowLevel, token.getLineNum(), "for",fatherTable);
            for (GrammarNode forNodeDetail : node.getNodes()) {
                if (forNodeDetail.getNodeName().equals("ForStmt")) {
                    Token LValNode = (Token) forNodeDetail.getNodes().get(0).getNodes().get(0);//forstmt -> lval -> idenfr
                    String name = LValNode.getToken();
                    int line = LValNode.getLineNum();
                    VarSymbolTable var = (VarSymbolTable) findAllSymbolTable(name,line,fatherTable,SymbolType.var);
                    //Frontend.Token tokenAssign = (Frontend.Token) stmtDetailNode.getNodes().get(1);
                    if (var != null && var.isConst()) { //改const
                        ec.addException(HandleException.makeReviseConstException(LValNode));
                    }
                } else if (forNodeDetail.getNodeName().equals("Stmt")) {
                    tmpHasReturn = handleStmt(forNodeDetail,returnType,forBlock);
                    if (tmpHasReturn) {
                        hasReturn = true;
                    }
                }
            }
            nowLevel--;
        }  else if (stmtDetailNode instanceof Token && ( ((Token) stmtDetailNode) .compareLexType(LexType.CONTINUETK)) ||
                stmtDetailNode instanceof Token && ( ((Token) stmtDetailNode) .compareLexType(LexType.BREAKTK))
        ) {
            if (!whetherInForBlock(fatherTable)) {
                ec.addException(HandleException.makeNotFunBlockHasContinueOrBreak((Token) stmtDetailNode));
            }
        } else if (stmtDetailNode.getNodeName().equals("Block")) {
            nowLevel++;
            BlockSymbolTable blockSymbolTable = new BlockSymbolTable(nowLevel,((Token)stmtDetailNode.getNodes().get(0)).getLineNum(),"commonBlock",fatherTable);
            handleBlock(stmtDetailNode,"",blockSymbolTable); //不是函数块returnType不需要继承
            addSymbol(fatherTable,blockSymbolTable);
        } else if (stmtDetailNode instanceof Token && ( ((Token) stmtDetailNode) .compareLexType(LexType.IFTK))) {
            if (node.getNodes().size() == 5) {
                //没有else
                tmpHasReturn = handleStmt(node.getNodes().get(4), returnType, fatherTable);
                if (tmpHasReturn) {
                    hasReturn = true;
                }
            } else {
                // 7
                tmpHasReturn = handleStmt(node.getNodes().get(4), returnType, fatherTable);
                if (tmpHasReturn) {
                    hasReturn = true;
                }
                tmpHasReturn = handleStmt(node.getNodes().get(6), returnType, fatherTable);
                if (tmpHasReturn) {
                    hasReturn = true;
                }
            }
        } else if (stmtDetailNode instanceof Token && ( ((Token) stmtDetailNode) .compareLexType(LexType.PRINTFTK))) {
            int count = 0;
            int exp = 0;
            for (GrammarNode printfNode : node.getNodes()) {
                if (printfNode instanceof Token && ( ((Token) printfNode) .compareLexType(LexType.STRCON))) {
                    String str = ((Token) printfNode).getToken();
                    int index = 0;
                    String child = "%d";
                    while ((index = str.indexOf(child,index)) != -1) {
                        index += child.length();
                        count++;
                    }
                } else if (printfNode.getNodeName().equals("Exp")) {
                    exp++;
                }
            }

            if (count != exp) {
                ec.addException(HandleException.makePrintfParasNumNotMatch((Token) stmtDetailNode));
            }
        }

        return hasReturn;
    }

    public boolean handleFuncLastTokenWhetherToken(GrammarNode node, String returnType, BlockSymbolTable fatherTable) {
        GrammarNode stmtDetailNode = node.getNodes().get(0);
        boolean hasReturn = false;
        if (stmtDetailNode instanceof Token && ( ((Token) stmtDetailNode) .compareLexType(LexType.RETURNTK))) {
            hasReturn = true;
            if (returnType.equals("void") ) {
                // return后面不是封号 有返回值
                if (node.getNodes().size() > 1 && !(node.getNodes().get(1) instanceof Token && ((Token)node.getNodes().get(1)).compareLexType(LexType.SEMICN))) {
                    ec.addException(HandleException.makeReturnValueException((Token) stmtDetailNode));
                }
            } else {
                //int 后面的exp要解析
                if (node.getNodes().size() > 1 && node.getNodes().get(1).getNodeName().equals("Exp")) {
                    handleExp(node.getNodes().get(1),fatherTable);
                }
            }
        }
        return hasReturn;
    }

    public Value handleExp(GrammarNode node, BlockSymbolTable fatherTable)  {
        Value value = null;
        for (GrammarNode addNode : node.getNodes()) {
            if (addNode.getNodeName().equals("AddExp")) {
                value = handleAddExp(addNode,fatherTable);
            }
        }
        return value;
    }

    public Value handleAddExp(GrammarNode node, BlockSymbolTable fatherTable) {
        Value value = null;
        for (GrammarNode mulExp : node.getNodes()) {
            if (mulExp.getNodeName().equals("MulExp")) {
                value = handleMulExp(mulExp,fatherTable);
            } else if (mulExp.getNodeName().equals("AddExp")) { //注意这里
                value = handleAddExp(mulExp,fatherTable);
            }
        }
        return value;
    }

    public Value handleMulExp(GrammarNode node, BlockSymbolTable fatherTable) {
        Value value = null;
        for (GrammarNode unaryExpNode : node.getNodes()) {
            if (unaryExpNode.getNodeName().equals("UnaryExp")) {
                value = handleUnaryExp(unaryExpNode,fatherTable);
            } else if (unaryExpNode.getNodeName().equals("MulExp")) {
                value = handleMulExp(unaryExpNode,fatherTable);
            }
        }
        return value;
    }

    public Value handleUnaryExp(GrammarNode node, BlockSymbolTable fatherTable) {
        if (node.getNodes().get(0).getNodeName().equals("PrimaryExp")) { //直接数，变量，（exp）
            GrammarNode primaryExpNode = node.getNodes().get(0).getNodes().get(0); //UnaryExp -> PrimaryExp -> detailFirst
            if (primaryExpNode instanceof Token toke && ((Token)primaryExpNode).compareLexType(LexType.LPARENT)) {
                return handleExp(node.getNodes().get(0).getNodes().get(1),fatherTable);
            } else if (primaryExpNode.getNodeName().equals("LVal")) {
                if (primaryExpNode.getNodes().get(0) instanceof Token tokenOfLVal && tokenOfLVal.compareLexType(LexType.IDENFR)) {
                    Token tokenLVal = (Token) primaryExpNode.getNodes().get(0);
                    String name = tokenLVal.getToken();
                    int line = tokenLVal.getLineNum();
                    VarSymbolTable var = (VarSymbolTable) findAllSymbolTable(name, line, fatherTable,SymbolType.var);
                    int subDimension = 0;
                    for (GrammarNode searchLBARCK : primaryExpNode.getNodes()) {
                        if (searchLBARCK instanceof Token token && token.compareLexType(LexType.LBRACK)) {
                            subDimension++;
                        }
                    }
                    if (var != null) {
                        return new Value(0, var.getBracket() - subDimension);
                    }
                    return null;
                } else {
                    System.err.println("handleUnaryExp LVal error!\n");
                    return null;
                }
            } else { //Number
                if (primaryExpNode.getNodes().get(0) instanceof Token tokenOfLVal && tokenOfLVal.compareLexType(LexType.INTCON)) {
                    return new Value(0,0);
                } else {
                    System.err.println("handleUnaryExp Number error!\n");
                    return null;
                }
            }
            // return;
        } else if (node.getNodes().get(0).getNodeName().equals("UnaryOp")) {
            return handleUnaryExp(node.getNodes().get(1),fatherTable);
        } else { //函数调用 返回值一定是int型、void型
            FuncSymbolTable func = null;
            Token tokenOfFunc = null;
            for (GrammarNode unaryExpNode : node.getNodes()) {
                SymbolTable tmp;
                if (unaryExpNode instanceof Token && ((Token) unaryExpNode).compareLexType(LexType.IDENFR)) {
                    tokenOfFunc = (Token) unaryExpNode;
                    if ((tmp = findAllSymbolTable(tokenOfFunc.getToken(), tokenOfFunc.getLineNum(), fatherTable,SymbolType.function)) != null) {
                        func = (FuncSymbolTable) tmp;
                    }
                } else if(unaryExpNode.getNodeName().equals("FuncRParams")) {
                    ArrayList<GrammarNode> realParas = new ArrayList<>();
                    for (GrammarNode realParaNode : unaryExpNode.getNodes()) {
                        if (realParaNode.getNodeName().equals("Exp")) {
                            realParas.add(realParaNode);
                        }
                    }
                    // 参数数量匹配
                    if (func != null) {
                        if (func.getParasCount() != realParas.size()) {
                            ec.addException(HandleException.makeFuncParasCountNoMatch(tokenOfFunc));
                        } else {
                            // 参数类型匹配建立在个数匹配的基础上
                            for (int i = 0; i < realParas.size(); i++) {
                                Value realParaValue = handleExp(realParas.get(i), fatherTable);
                                if (realParaValue != null && realParaValue.getValueType() != func.getParaIndexOfType(i)) {
                                    // 有可能该值不存在 但是确实有占位符？
                                    ec.addException(HandleException.makeFuncParasTypeNoMatch(tokenOfFunc));
                                }
                            }
                        }
                    }
                }
            }
            if (func != null) {
                if (func.getReturnType().equals("int")) {
                    return new Value(0,0);
                } else {
                    return new Value(0,-1);
                } //注意这里的value未处理，只是为了错误处理用0填入
            }  else {
                return null;
            }
        } //else
    }


    public boolean compareNodeName(GrammarNode node, int index, String compareName) {
        String name = node.getNodes().get(index).getNodeName();
        return name.equals(compareName);
    }

    public void addSymbol(BlockSymbolTable fatherTable,SymbolTable sym) { //这里的sym一定要是三大类之一
        //务必确保fatherTable已经在总符号表中
        if (sym instanceof BlockSymbolTable) { //代码块直接加入
            fatherTable.add(sym);
            return;
        } else {
            boolean flag = checkRedeclaration(fatherTable,sym);
            if (flag) {
                fatherTable.add(sym);
            }
        }
    }

    public boolean checkRedeclaration(BlockSymbolTable fatherTable, SymbolTable sym) {
        if (sym instanceof BlockSymbolTable) {
            return true;
        }
        for (SymbolTable exist : fatherTable.getSymbolTables()) {
            if (exist.getName().equals(sym.getName())) {
                // 重定义
                MyException my = HandleException.makeRedeclarationException(sym, exist.getLine());
                ec.addException(my);
                return false;
            }
        }
        // 没有重定义
        return true;
    }

    public SymbolTable findAllSymbolTable(String name, int line, BlockSymbolTable fatherTable,SymbolType symbolType) {
        // 先检查本级
        for (SymbolTable exist : fatherTable.getSymbolTables()) {
            if (exist.getName().equals(name) && exist.compareType(symbolType)) {
                 return exist;
            }
        }

        //找之前的
        if (fatherTable.getFatherTable() != null) {
            return findAllSymbolTable(name,line, fatherTable.getFatherTable(),symbolType);
        } else { //没找到 报异常
            ec.addException(HandleException.makeNoFoundNameException(name,line));
            return null;
        }
    }

    public boolean whetherInForBlock(BlockSymbolTable blockSymbolTable) {
        if (blockSymbolTable == null) {
            return false;
        }
        if (blockSymbolTable.getName().equals("for")) {
            return true;
        } else {
            return whetherInForBlock(blockSymbolTable.getFatherTable());
        }
    }

    public String outputEcError() {
        ArrayList<MyException> sortArray = ec.sortArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (MyException myException : sortArray) {
            stringBuilder.append(myException.getLine()).append(" ").append(myException.getErrorType()).append("\n");
        }
        return stringBuilder.toString();
    }
}
