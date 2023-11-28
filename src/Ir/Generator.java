package Ir;

import AST.ASTroot;
import AST.AddExpStmt;
import AST.ArrDeclStmt;
import AST.AssignStmt;
import AST.BasicBlock;
import AST.BreakStmt;
import AST.CallExpr;
import AST.CompoundStmt;
import AST.ComputeStmt;
import AST.CondStmt;
import AST.ContinueStmt;
import AST.EqExp;
import AST.ForStmt;
import AST.FuncDeclStmt;
import AST.IfStmt;
import AST.LAndExp;
import AST.LVal;
import AST.LorExp;
import AST.MulExpStmt;
import AST.Op;
import AST.RelExp;
import AST.ReturnStmt;
import AST.Stmt;
import AST.UnaryExpStmt;
import AST.VarDeclStmt;
import Frontend.LexType;
import Frontend.Token;
import Ir.Component.GlobalFunc;
import Ir.Component.GlobalVar;
import Ir.Component.Model;
import Ir.IRInstr.AddIR;
import Ir.IRInstr.AllocaIR;
import Ir.IRInstr.BrIR;
import Ir.IRInstr.CallIR;
import Ir.IRInstr.FuncDeclIR;
import Ir.IRInstr.GetelementptrIR;
import Ir.IRInstr.GlobalVarIr;
import Ir.IRInstr.IcmpIR;
import Ir.IRInstr.Instr;
import Ir.IRInstr.JumpUnit;
import Ir.IRInstr.LabelLineIR;
import Ir.IRInstr.LoadIR;
import Ir.IRInstr.MulIR;
import Ir.IRInstr.Pair;
import Ir.IRInstr.ReturnIR;
import Ir.IRInstr.SdivIR;
import Ir.IRInstr.SremIR;
import Ir.IRInstr.StoreIR;
import Ir.IRInstr.SubIR;
import Ir.IRInstr.ZextIR;
import SymbolTablePackage.BlockSymbolTable;
import SymbolTablePackage.FuncSymbolTable;
import SymbolTablePackage.SymbolTable;
import SymbolTablePackage.SymbolType;
import SymbolTablePackage.VarSymbolTable;
import java.util.ArrayList;
import java.util.Stack;

import Frontend.IoFile;

public class Generator {
    private BlockSymbolTable mainTable;
    private ASTroot asTroot;
    private int nowReg;
    private BlockSymbolTable nowBlock;

    private SymbolTable nowSymbolTable;

    private JumpUnit nowForStmt;

    private BasicBlock nowBasicBlock;

    private ArrayList<BasicBlock> blocks;


    public Generator(BlockSymbolTable table, ASTroot asTroot) {
        this.mainTable = table;
        this.asTroot = asTroot;
        this.nowReg = 0;
        this.nowBlock = table;
        this.nowForStmt = null;
        this.nowBasicBlock = null;
    }

    public int getReg() {
        return nowReg++;
    }

    public Model run() {
        ArrayList<GlobalVar> globalVars = new ArrayList<>();
        ArrayList<GlobalFunc> globalFunctions = new ArrayList<>();
        FuncDeclStmt funcDeclStmtMain = this.asTroot.getMainStmt();
        GlobalFunc main;
        int index = 0;

        for (VarDeclStmt varDeclStmt : this.asTroot.getVarList()) {
            globalVars.add(handleGlobalVarDeclStmt(varDeclStmt,this.mainTable));
            index++;
        }

        for (FuncDeclStmt funcDeclStmt : this.asTroot.getFunList()) {
            globalFunctions.add(handleGlobalFunctions(funcDeclStmt,nowBlock.getSymbolTables().get(index)));
            index++;
        }

        main = handleMain(funcDeclStmtMain,(BlockSymbolTable) nowBlock.getSymbolTables().get(index));
        Model model = new Model(globalVars,globalFunctions,main);
        IoFile.outputContentToFile_llvm_ir(model.toString());
        return model;
    }

    public GlobalVar handleGlobalVarDeclStmt(VarDeclStmt varDeclStmt, BlockSymbolTable fatherTable) {
        //全局变量和常量直接计算值，不需要输出计算过程
        String name = varDeclStmt.getName();
        String type = varDeclStmt.isConst() ? "constant" : "global";
        //数组
        if (varDeclStmt instanceof ArrDeclStmt arrDeclStmt) {
            VarSymbolTable var = fatherTable.searchVar(name, fatherTable, true);
            var.setTag("@" + name);
            int n1 = arrDeclStmt.getN1();
            int n2 = arrDeclStmt.getN2();
            var.setN1(n1);
            var.setN2(n2);
            int value_arrayElement;
            if (arrDeclStmt.getValue() != null) {
                for (ComputeStmt computeStmt : arrDeclStmt.getValue()) {
                    value_arrayElement = computeStmt.getValue(fatherTable);
                    var.getArrayValue().add(value_arrayElement);
                }
                return new GlobalVar(new GlobalVarIr(name, type, arrDeclStmt.getBracket(), n1, n2, var.getArrayValue()));
            } else {
                return new GlobalVar(new GlobalVarIr(name, type, arrDeclStmt.getBracket(), n1, n2, null));
            }
        }

        //非数组
        else {
            int value = varDeclStmt.getValue(fatherTable);
            VarSymbolTable var = fatherTable.searchVar(name, fatherTable, true);
            var.setTag("@" + name);
            var.setValue(value);
            return new GlobalVar(new GlobalVarIr(name, type, value));
        }
    }


    public GlobalFunc handleGlobalFunctions(FuncDeclStmt funcDeclStmt,SymbolTable symbolTable) {
        this.blocks = funcDeclStmt.getBlocks();
        nowReg = 0;//函数从0开始
        String name = funcDeclStmt.getName();
        String type = funcDeclStmt.getReturnType();
        FuncSymbolTable funcSymbolTable = (FuncSymbolTable) symbolTable;
        FuncDeclIR funcDeclIR = funcDeclStmt.getIr();
        BlockSymbolTable paras = funcSymbolTable.getParams();
        if (paras != null) {
            for (SymbolTable para : paras.getSymbolTables()) {
                if (para.compareType(SymbolType.var) && ((VarSymbolTable)para).isParameter()) {
                    int tag = getReg();
                    para.setTag("%"+tag);
                } else {
                    break;
                }
            }
        }
        String funTag = "%" + getReg();
        BasicBlock funBasicBlock = new BasicBlock(funTag,0);
        this.nowBasicBlock = funBasicBlock;
        /*
        if (paras != null) {
            for (SymbolTable para : paras.getSymbolTables()) {
                if (para.compareType(SymbolType.var) && ((VarSymbolTable)para).isParameter()) {
                    VarSymbolTable varSymbolTable = (VarSymbolTable)para;//屎山。。。。
                    String tag = "%" + getReg();
                    //这里的思路是，形参先把tag设置成他的值寄存器（实际上应该要存储地址寄存器但是还没有分配），分配了地址寄存器后，再改回来
                    if (varSymbolTable.getBracket() == 0) {
                        this.nowBasicBlock.inputInstr(new AllocaIR(tag, "i32"));
                        this.nowBasicBlock.inputInstr(new StoreIR(para.getTag(), tag, "i32"));
                        para.setTag(tag);//改回来
                    } else if (varSymbolTable.getBracket() == 1) {
                        this.nowBasicBlock.inputInstr(new AllocaIR(tag, "i32*"));
                        this.nowBasicBlock.inputInstr(new StoreIR(para.getTag(), tag, "i32*"));
                    } else if (varSymbolTable.getBracket() == 2) {

                    }
                }
            }
        }
         */
        ArrayList<VarDeclStmt> parasOfFuncDeclStmt = funcDeclStmt.getParas();
        if (parasOfFuncDeclStmt != null) {
            for (VarDeclStmt varDeclStmt : parasOfFuncDeclStmt) {
                String tag = "%" + getReg();
                VarSymbolTable para = paras.searchVar(varDeclStmt.getName(), paras, true);
                if (!(varDeclStmt instanceof ArrDeclStmt arrDeclStmt)) {
                    this.nowBasicBlock.inputInstr(new AllocaIR(tag, "i32"));
                    this.nowBasicBlock.inputInstr(new StoreIR(para.getTag(), tag, "i32"));
                } else if (arrDeclStmt.getBracket() == 1) {
                    para.setN2(arrDeclStmt.getN2());
                    this.nowBasicBlock.inputInstr(new AllocaIR(tag, "i32*"));
                    this.nowBasicBlock.inputInstr(new StoreIR(para.getTag(), tag, "i32*"));
                } else if (arrDeclStmt.getBracket() == 2) {
                    para.setN1(arrDeclStmt.getN1());
                    para.setN2(arrDeclStmt.getN2());
                    String arrType = getType(0, arrDeclStmt.getN2()) + "*";
                    this.nowBasicBlock.inputInstr(new AllocaIR(tag, arrType));
                    this.nowBasicBlock.inputInstr(new StoreIR(para.getTag(), tag, arrType));
                }
                para.setTag(tag);//改回来
            }
        }
        handleCompoundStmt(funcDeclStmt.getStmts(),paras);
        this.nowBasicBlock.inputInstr(new ReturnIR(true,null));
        GlobalFunc globalFunc = new GlobalFunc(funcDeclIR,this.blocks);
        blocks.add(this.nowBasicBlock);
        return globalFunc;
    }

    public GlobalFunc handleMain(FuncDeclStmt funcDeclStmt,BlockSymbolTable paras) {
        this.blocks = funcDeclStmt.getBlocks();
        nowReg = 0;//函数从0开始
        FuncDeclIR funcDeclIR = funcDeclStmt.getIr();
        if (paras != null) {
            for (SymbolTable para : paras.getSymbolTables()) {
                if (para.compareType(SymbolType.var) && ((VarSymbolTable)para).isParameter()) {
                    int tag = getReg();
                    para.setTag("%"+tag);
                } else {
                    break;
                }
            }
        }
        String funTag = "%" + getReg();
        BasicBlock funBasicBlock = new BasicBlock(funTag,0);
        this.nowBasicBlock = funBasicBlock;
        /*
        if (paras != null) {
            for (SymbolTable para : paras.getSymbolTables()) {
                if (para.compareType(SymbolType.var) && ((VarSymbolTable)para).isParameter()) {
                    String tag = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new AllocaIR(tag,"i32")); //暂时i32
                    this.nowBasicBlock.inputInstr(new StoreIR(para.getTag(), tag,"i32"));//暂时i32
                    para.setTag(tag);
                }
            }
        }
         */
        handleCompoundStmt(funcDeclStmt.getStmts(),paras);
        GlobalFunc globalFunc = new GlobalFunc(funcDeclIR,this.blocks);
        blocks.add(this.nowBasicBlock);
        return globalFunc;
    }

    public void handleCompoundStmt(CompoundStmt compoundStmt,BlockSymbolTable fatherTable) {
        ArrayList<Stmt> stmts = compoundStmt.getStmts();
        int numberOfBlockSymbolTable = 0;
        for (Stmt stmt : stmts) {
            if (this.nowBasicBlock.getEndOfBlock() != null)
                break;
            if (stmt instanceof VarDeclStmt) {
                handleVarDecl((VarDeclStmt) stmt, fatherTable);
            }  else {
                handleStmt(stmt,fatherTable,numberOfBlockSymbolTable);
                if (stmt instanceof BreakStmt || stmt instanceof ContinueStmt) {
                    break;
                }
                numberOfBlockSymbolTable += checkHasBlock(stmt);
            }
        }
        return ;
    }

    public void handleStmt(Stmt stmt, BlockSymbolTable fatherTable,int numberOfBlockSymbolTable) {
        if (stmt instanceof AssignStmt) {
            handleAssignStmt((AssignStmt) stmt,fatherTable);
        } else if (stmt instanceof CompoundStmt) { //注意这里的fatherTable会变化
            BlockSymbolTable nowBlock = fatherTable.searchBlockSymbolTable(numberOfBlockSymbolTable,fatherTable);
            //getReg();//块分配
            handleCompoundStmt((CompoundStmt) stmt, nowBlock);
        } else if (stmt instanceof ComputeStmt) {
            handleComputerStmt((ComputeStmt) stmt,fatherTable);
        } else if (stmt instanceof ReturnStmt) {
            ReturnStmt returnStmt = (ReturnStmt) stmt;
            boolean returnVoid = returnStmt.returnVoid();
            if (!returnVoid) {
                ComputeStmt computeStmt = returnStmt.getReturnExp();
                AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
                if (addExpStmt.isNum()) {
                    String value = String.valueOf(addExpStmt.getValue());
                    ReturnIR returnIR = new ReturnIR(returnVoid,value);
                    this.nowBasicBlock.inputInstr(returnIR);
                    this.nowBasicBlock.setEndOfBlock(returnIR,"return");
                } else {
                    handleComputerStmt(computeStmt,fatherTable);
                    ReturnIR returnIR = new ReturnIR(returnVoid,"%" + (nowReg-1));
                    this.nowBasicBlock.inputInstr(returnIR);
                    this.nowBasicBlock.setEndOfBlock(returnIR,"return");
                }
            } else {
                ReturnIR returnIR = new ReturnIR(returnVoid,null);
                this.nowBasicBlock.inputInstr(returnIR);
                this.nowBasicBlock.setEndOfBlock(returnIR,"return");
            }

        } else if (stmt instanceof CallExpr) {
            CallExpr callExpr = (CallExpr) stmt;
            handleCallExpr(callExpr,fatherTable);
        } else if (stmt instanceof ForStmt forStmt) {
            handleForStmt((ForStmt) stmt, fatherTable, numberOfBlockSymbolTable);
        } else if (stmt instanceof IfStmt ifStmt) {
            handleIfStmt((IfStmt) stmt,fatherTable,numberOfBlockSymbolTable);
        } else if (stmt instanceof BreakStmt breakStmt) {
            if (this.nowForStmt != null) {
                BrIR breakBrIR = this.nowForStmt.getEnd();//注意这是共用了同一个指针！
                this.nowBasicBlock.inputInstr(breakBrIR);
                this.nowBasicBlock.setEndOfBlock(breakBrIR,"break");
                //注意这里没有新建块
            }
        } else if (stmt instanceof ContinueStmt) {
            if (this.nowForStmt != null) {
                BrIR continueBrIR = this.nowForStmt.getContinueBrIR();
                this.nowBasicBlock.inputInstr(continueBrIR);
                this.nowBasicBlock.setEndOfBlock(continueBrIR,"continue");
                //注意这里没有新建块
            }
        }
        return ;
    }

    public void handleIfStmt(IfStmt ifStmt, BlockSymbolTable fatherTable, int numberOfBlockSymbolTable) {
        CondStmt condStmt = ifStmt.getCondStmt();
        Stmt thenStmt = ifStmt.getThenStmt();
        Stmt elseStmt = ifStmt.getElseStmt();

        JumpUnit ifUnit = new JumpUnit();

        String ifCross = handleBlockEnd();
        handleBlockBegin(ifCross);
        handleCondStmt(condStmt,fatherTable,ifUnit);
        //处理if块语句;
        handleStmt(thenStmt,fatherTable,numberOfBlockSymbolTable);
        //if块收尾
        String endReg = "%" + getReg();
        //回填之前失败的跳转
        ifUnit.setLorExpFailCrossBrLine(endReg);
        if (this.nowBasicBlock.getEndOfBlock() == null) { //没有continue和break、return
            BrIR end = new BrIR("if's end br");//if执行结束的无条件跳转
            this.nowBasicBlock.inputInstr(end);
            this.nowBasicBlock.setEndOfBlock(end,"blockBr");
            ifUnit.setIfBr(end);//如果有continue和break、return,这个地方就是空的不需要回填
        }

        //
        int newBlockNo = this.nowBasicBlock.getBlockNo() + 1;
        BasicBlock basicBlock = new BasicBlock(endReg,newBlockNo);
        this.blocks.add(this.nowBasicBlock);
        this.nowBasicBlock = basicBlock;
        LabelLineIR beginLabel = new LabelLineIR(endReg);
        this.nowBasicBlock.inputInstr(beginLabel);

        if (elseStmt == null) {
            if (ifUnit.getIfBr() != null) { //ifUnit.getIfBr是空，说明因为由continue和break导致没填
                ifUnit.getIfBr().setUnconditionalJump_reg(endReg);//回填之前if结束的跳转
            }
        } else if (elseStmt != null) {
//            if (thenStmt instanceof CompoundStmt || thenStmt instanceof IfStmt || thenStmt instanceof ForStmt) { //if块是 块
//                handleStmt(elseStmt,fatherTable,numberOfBlockSymbolTable+1);
//            }
//            else { // if块只是语句
//                handleStmt(elseStmt, fatherTable, numberOfBlockSymbolTable);
//            }
            //else结束
            int numOfIfBlocks = checkHasBlock(thenStmt);
            handleStmt(elseStmt, fatherTable, numberOfBlockSymbolTable + numOfIfBlocks);
            String elseEnd = "%" + getReg();
            if (ifUnit.getIfBr() != null) {
                ifUnit.getIfBr().setUnconditionalJump_reg(elseEnd);//回填之前if结束的跳转
            }
            if (this.nowBasicBlock.getEndOfBlock() == null) {
                BrIR end = new BrIR(elseEnd);
                this.nowBasicBlock.inputInstr(end);
                this.nowBasicBlock.setEndOfBlock(end,"blockBr");
                blocks.add(this.nowBasicBlock);
            }

            //新块
            this.nowBasicBlock = new BasicBlock(elseEnd,this.nowBasicBlock.getBlockNo() + 1);
            this.nowBasicBlock.inputInstr(new LabelLineIR(elseEnd));
        }
    }

    public void handleForStmt (ForStmt forStmt, BlockSymbolTable fatherTable, int numberOfBlockSymbolTable) {
        CondStmt condStmt = forStmt.getCondStmt();
        AssignStmt assignStmt1 = forStmt.getAssignStmt1();
        AssignStmt assignStmt2 = forStmt.getAssignStmt2();
        Stmt stmt = forStmt.getForStmt();
        handleAssignStmt(assignStmt1,fatherTable);

        String forCross = handleBlockEnd();
        BrIR before = (BrIR) this.nowBasicBlock.getEndOfBlock();
        handleBlockBegin(forCross);

        JumpUnit forUnit = new JumpUnit();
        forUnit.setBegin(before);
        forUnit.setEnd(new BrIR("for exit")); //先设置！这样break也可以用这个end
        forUnit.setContinueBrIR(new BrIR("for continue"));//continue使用的入口

        //保存之前的forUnit
        JumpUnit forStmtBefore = this.nowForStmt;
        //修改全局JumpUnit
        this.nowForStmt = forUnit;

        handleCondStmt(condStmt,fatherTable,forUnit);
        //处理for块语句
        handleStmt(stmt,fatherTable,numberOfBlockSymbolTable);

        //处理assignStmt2
        //continue入口
        String continueCross = handleBlockEnd();
        //回填continue
        forUnit.getContinueBrIR().setUnconditionalJump_reg(continueCross);
        handleBlockBegin(continueCross);
        handleAssignStmt(assignStmt2,fatherTable);

        //跳回
        //instrs.add(forUnit.getBegin());
        this.nowBasicBlock.inputInstr(forUnit.getBegin());
        //跳出
        String endReg = "%" + getReg();
        handleBlockBegin(endReg);
        forUnit.setLorExpFailCrossBrLine(endReg);
        //把end回填
        forUnit.getEnd().setUnconditionalJump_reg(endReg);

        //全局JumpUnit改回来
        this.nowForStmt = forStmtBefore;
    }

    public void handleCondStmt(CondStmt condStmt, BlockSymbolTable fatherTable,JumpUnit ifUnit) {
        if (condStmt == null)
            return;
        LorExp lorExp = condStmt.getLorExp();
        handleLorExp(lorExp,fatherTable,ifUnit);
        return ;
    }

    public void handleLorExp(LorExp lorExp, BlockSymbolTable fatherTable,JumpUnit ifUnit) {
        ArrayList<LAndExp> lAndExps = lorExp.getlAndExps();
        //instrs.addAll(handleLAndExp(lAndExps.get(0),fatherTable,ifUnit));
        String tag1;
        ArrayList<BrIR> acceptBrIR = new ArrayList<>();
        ArrayList<BrIR> wrongBrIR = new ArrayList<>();
        for (int i = 0; i < lAndExps.size(); i++) {
            handleLAndExp(lAndExps.get(i),fatherTable,wrongBrIR);

            if (lAndExps.get(i).isNum()) {
                tag1 = String.valueOf(lAndExps.get(i).getValue());
            } else {
                int preReg = nowReg - 1;
                tag1 = "%" +preReg;
            }

            //如果 && 全对， 那么应该无条件跳转到if入口，但是这个入口现在还不知道在哪，先收集着
            //当前的基本块不再是LAnd块，而是Lor块
            BrIR accept = new BrIR("null");
            //cond应该不会出现continue break 和 return
            this.nowBasicBlock.inputInstr(accept);
            this.nowBasicBlock.setEndOfBlock(accept,"blockBr");
            acceptBrIR.add(accept);//现在还不知道入口，结束的时候统一设置 最后的land块的br

            //如果不对，设置下一个||的入口点！并为之前的wrong设置入口
            // 新块
            String wrongLine = "%" + getReg();
            int newBlockNo = this.nowBasicBlock.getBlockNo() + 1;
            BasicBlock basicBlock = new BasicBlock(wrongLine,newBlockNo);
            this.blocks.add(this.nowBasicBlock);
            this.nowBasicBlock = basicBlock;
            this.nowBasicBlock.inputInstr(new LabelLineIR(wrongLine));
            //
            for (BrIR wrong : wrongBrIR) {
                wrong.setWrong(wrongLine);
            }
            // wrong要清空
            wrongBrIR.clear();
        }
        // 注意，最后一个||如果没有成功，那么要跳到if后的地方！这个还没有处理，所以应该先记录下来
        BrIR lorExpFailCross = new BrIR("LorExpWrong");
        ifUnit.setLorExpFailCross(lorExpFailCross);
        this.nowBasicBlock.inputInstr(lorExpFailCross);
        this.nowBasicBlock.setEndOfBlock(lorExpFailCross,"blockBr");
        //if 开始 或者 for 开始 之前按if写的，问题不大
        //if块
        String ifBeginLine = "%" + getReg();
        int newBlockNo = this.nowBasicBlock.getBlockNo() + 1;
        BasicBlock basicBlock = new BasicBlock(ifBeginLine,newBlockNo);
        this.blocks.add(this.nowBasicBlock);
        this.nowBasicBlock = basicBlock;
        this.nowBasicBlock.inputInstr(new LabelLineIR(ifBeginLine));
        //回填
        for (BrIR accept : acceptBrIR) {
            accept.setUnconditionalJump_reg(ifBeginLine);
        }
        //回填结束
        return ;
    }

    public void handleLAndExp(LAndExp lAndExp, BlockSymbolTable fatherTable,ArrayList<BrIR> wrongBrIR) {
        ArrayList<EqExp> eqExps = lAndExp.getEqExps();

        //instrs.addAll(handleEqExp(eqExps.get(0),fatherTable));
        String tag1;
        for (int i = 0; i < eqExps.size(); i++) {
            handleEqExp(eqExps.get(i),fatherTable);
            if (eqExps.get(i).isNum()) {
                tag1 = String.valueOf(eqExps.get(i).getValue());
            } else {
                int preReg = nowReg - 1;
                tag1 = "%" + preReg;//已经zext过
            }

            //最后的结果需要和0比较
            String cond = "%" + getReg();
            this.nowBasicBlock.inputInstr(new IcmpIR(cond,"ne","0",tag1));
            String accept = "%" + getReg();
            BrIR brIR = new BrIR(cond,accept,"lAndExpWrong");
            wrongBrIR.add(brIR);
            this.nowBasicBlock.inputInstr(brIR);
            this.nowBasicBlock.setEndOfBlock(brIR,"blockBr");
            //新块
            int newBlockNo = this.nowBasicBlock.getBlockNo() + 1;
            BasicBlock basicBlock = new BasicBlock(accept,newBlockNo);
            this.blocks.add(this.nowBasicBlock);
            this.nowBasicBlock = basicBlock;
            LabelLineIR beginLabel = new LabelLineIR(accept);
            this.nowBasicBlock.inputInstr(beginLabel);
            //注意都是指针！以后ifUnit改，instr也会改
        }
        //&&的最后accept,就可以直接跳转到if入口

        return ;
    }

    public void handleEqExp(EqExp eqExp, BlockSymbolTable fatherTable) {
        ArrayList<RelExp> relExps = eqExp.getRelExps();

        handleRelExp(relExps.get(0),fatherTable);
        String tag1;
        String tag2;
        if (relExps.get(0).isNum()) {
            tag1 = String.valueOf(relExps.get(0).getValue());
        } else {
            int preReg = nowReg - 1;
            tag1 = "%" + preReg;
        }

        for (int i = 2; i < relExps.size(); i += 2) {
            handleRelExp(relExps.get(i),fatherTable);
            if (relExps.get(i).isNum()) {
                tag2 = String.valueOf(relExps.get(i).getValue());
            } else {
                int preReg = nowReg - 1;
                tag2 = "%" + preReg;
            }

            //1 : ==
            //2 : !=
            RelExp op = relExps.get(i-1);
            String transferReg = "%" + getReg();
            if (op.isOp()) {
                if (op.getType() == 1) {
                    this.nowBasicBlock.inputInstr(new IcmpIR(transferReg,"eq",tag1,tag2));
                } else if (op.getType() == 2) {
                    this.nowBasicBlock.inputInstr(new IcmpIR(transferReg,"ne",tag1,tag2));
                }
            }

            String result = "%" + getReg();
            this.nowBasicBlock.inputInstr(new ZextIR(result, transferReg));

            tag1 = result; //上一次的结果下一次用，如果还有的话
        }

        return ;
    }

    public void handleRelExp(RelExp relExp, BlockSymbolTable fatherTable) {
        ArrayList<AddExpStmt> addExpStmts = relExp.getAddExpStmts();

        String tag1;
        String tag2;
        handleAddExpStmt(addExpStmts.get(0),fatherTable);
        if (addExpStmts.get(0).isNum()) {
            tag1 = String.valueOf(addExpStmts.get(0).getValue());
        } else {
            int preReg = nowReg - 1;
            tag1 = "%" + preReg;
        }

//        if (addExpStmts.size() == 1) {
//            //最后的结果需要和0比较
//            String cond = "%" + getReg();
//            instrs.add(new IcmpIR(cond,"ne","0",tag1));
//            //最后的结果tag1进行br
//            instrs.add(new BrIR(cond,"%" + getReg(),null));
//        } else {
            for (int i = 2; i < addExpStmts.size(); i += 2) {
                handleAddExpStmt(addExpStmts.get(i),fatherTable);
                if (addExpStmts.get(i).isNum()) {
                    tag2 = String.valueOf(addExpStmts.get(i).getValue());
                } else {
                    int preReg = nowReg - 1;
                    tag2 = "%" + preReg;
                }
                //1: LSS <
                //2: LEQ <=
                //3: GRE >
                //4: GEQ >=
                AddExpStmt op = addExpStmts.get(i-1);
                String transferReg = "%" + getReg();
                if (op.isOp()) {
                    if (op.getType() == 1) {
                        this.nowBasicBlock.inputInstr(new IcmpIR(transferReg, "slt", tag1, tag2));
                    } else if (op.getType() == 2) {
                        this.nowBasicBlock.inputInstr(new IcmpIR(transferReg, "sle", tag1, tag2));
                    } else if (op.getType() == 3) {
                        this.nowBasicBlock.inputInstr(new IcmpIR(transferReg, "sgt", tag1, tag2));
                    } else if (op.getType() == 4) {
                        this.nowBasicBlock.inputInstr(new IcmpIR(transferReg, "sge", tag1, tag2));
                    }
                }
                String result = "%" + getReg();
                this.nowBasicBlock.inputInstr(new ZextIR(result, transferReg));

                tag1 = result; //上一次的结果下一次用，如果还有的话
            }
            //最后的结果需要和0比较
            //String cond = "%" + getReg();
            //instrs.add(new IcmpIR(cond,"ne","0",tag1));
            //最后的结果tag1进行br
            //instrs.add(new BrIR(cond,"%" + getReg(),null));
            //这个应该是land和lor用的
        // }

        return ;
    }

    public void handleCallExpr(CallExpr callExpr,BlockSymbolTable fatherTable) {
        String name = callExpr.getFunctionName();
        if (name.equals("printf")) {
            ArrayList<ComputeStmt> paras = callExpr.getParas();

            ArrayList<String> place = handleCallParas(paras,fatherTable);
            //this.nowBasicBlock.inputInstr(pair.getInstrs());
            int realPara = 0;//index of printf
            String format = callExpr.getFormat();
            char[] format_char = format.toCharArray();
            for (int i = 1; i < format_char.length-1; i++) { //去掉头尾的“ ”
                if (format_char[i] == '%' && i + 1 < format_char.length-1 && format_char[i+1] == 'd') {
                    //输出值
                    ArrayList<String> tmp = new ArrayList<>();//para 因为putint只有一个参数，所以每次都新建一个，加入place的元素
                    tmp.add(place.get(realPara++));
                    this.nowBasicBlock.inputInstr(new CallIR("putint","void", tmp, null,null));
                    i++;//d
                } else if (format_char[i] == '\\' && i + 1 < format_char.length-1 && format_char[i+1] == 'n') {
                    ArrayList<String> tmp = new ArrayList<>();
                    tmp.add("10");
                    this.nowBasicBlock.inputInstr(new CallIR("putch","void", tmp, null,null));
                    i++;//n
                }
                else {
                    ArrayList<String> tmp = new ArrayList<>();
                    int c = format_char[i];
                    tmp.add(String.valueOf(c));
                    this.nowBasicBlock.inputInstr(new CallIR("putch","void", tmp, null,null));
                }
            }
        } else {
            FuncSymbolTable funcSymbolTable = fatherTable.searchFunction(name,mainTable);
            FuncDeclStmt funcDeclStmt = null;
            for (FuncDeclStmt tmp : this.asTroot.getFunList()) {
                if (tmp.getName().equals(name)) {
                    funcDeclStmt = tmp;
                    break;
                }
            }
            ArrayList<VarDeclStmt> parasType = funcDeclStmt.getParas();
            String returnType = funcSymbolTable.getReturnType();
            ArrayList<ComputeStmt> paras = callExpr.getParas();

            ArrayList<String> place = handleCallParas(paras,fatherTable);
            //this.nowBasicBlock.inputInstr(pair.getInstrs());
            String storeReg;
            if (returnType.equals("void")) {
                storeReg = null;
            } else {
                storeReg = "%" + getReg();
            }

            this.nowBasicBlock.inputInstr(new CallIR(name,returnType,place,storeReg,parasType));
        }
    }

    public void handleAssignStmt(AssignStmt assignStmt, BlockSymbolTable fatherTable) {
        if (assignStmt.getlVal() == null)
            return;
        LVal lVal = assignStmt.getlVal();
        ComputeStmt computeStmt = assignStmt.getComputeStmt();
        boolean isGetInt = assignStmt.isGetInt();
        int bracket = lVal.getBracket();
        String name = lVal.getLValName();
        VarSymbolTable var = fatherTable.searchVar(name,fatherTable,false);
        String pointer = var.getTag();
        if (bracket == 0 && isGetInt) {
            this.nowBasicBlock.inputInstr(new CallIR("getint","int",new ArrayList<>(),"%" + getReg(),null));
            this.nowBasicBlock.inputInstr(new StoreIR("%"+(nowReg-1),pointer,"i32"));
        } else {
            handleComputerStmt(computeStmt,fatherTable);
            String storeValueReg;
            AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
            String storeReg;
            String arrPointer;
            if (addExpStmt.isNum()) {
                storeValueReg = String.valueOf(addExpStmt.getValue());
            } else {
                storeValueReg = "%" + (nowReg-1); //如果不是全局变量，值保存在nowReg前一个寄存器里
            }
            if (bracket == 0) {
                this.nowBasicBlock.inputInstr(new StoreIR(storeValueReg,pointer,"i32"));
            } else if (bracket == 1) { //注意，在赋值语句中，在左边的只能是整数
                if (var.isParameter())  {
                    storeReg = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new LoadIR(storeReg, pointer, "i32*"));
                } else {
                    storeReg = pointer;
                }
                ComputeStmt n2 = lVal.getN2();
                handleComputerStmt(n2,fatherTable);
                AddExpStmt addExpStmtN2 = n2.getAddExpStmt();
                String indexN2;
                if (addExpStmtN2.isNum()) {
                    indexN2 = String.valueOf(addExpStmtN2.getValue());
                } else {
                    indexN2 = "%" + (nowReg - 1);
                }
                ArrayList<String> indexs = new ArrayList<>();
                String arrType = "i32";
                if (!var.isParameter()) {//如果不是指针，不需要前面带0，type就是i32
                    indexs.add(String.valueOf(0));
                    arrType = getType(0, var.getN2());
                    //不是指针会变大一点。。所以前面有个0
                }
                indexs.add(indexN2);
                arrPointer = "%" + getReg();
                this.nowBasicBlock.inputInstr(new GetelementptrIR(arrPointer,arrType,storeReg,indexs));
                this.nowBasicBlock.inputInstr(new StoreIR(storeValueReg,arrPointer,"i32"));
            } else {
                String arrType;
                if (var.isParameter()) {
                    arrType = getType(0, var.getN2());
                    storeReg = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new LoadIR(storeReg,pointer,arrType + "*"));
                } else {
                    storeReg = pointer;
                }
                ComputeStmt n1 = lVal.getN1();
                handleComputerStmt(n1,fatherTable);
                AddExpStmt addExpStmtN1 = n1.getAddExpStmt();
                String indexN1;
                if (addExpStmtN1.isNum()) {
                    indexN1 = String.valueOf(addExpStmtN1.getValue());
                } else {
                    indexN1 = "%" + (nowReg - 1);
                }

                ComputeStmt n2 = lVal.getN2();
                handleComputerStmt(n2,fatherTable);
                AddExpStmt addExpStmtN2 = n2.getAddExpStmt();
                String indexN2;
                if (addExpStmtN2.isNum()) {
                    indexN2 = String.valueOf(addExpStmtN2.getValue());
                } else {
                    indexN2 = "%" + (nowReg - 1);
                }

                ArrayList<String> indexs = new ArrayList<>();
                arrType = getType(0, var.getN2());
                if (!var.isParameter()) {//如果前面不是指针，前面不用带0，类型就是
                    arrType = getType(var.getN1(), var.getN2());
                    indexs.add(String.valueOf(0));
                }
                indexs.add(indexN1);
                indexs.add(indexN2);
                arrPointer = "%" + getReg();
                this.nowBasicBlock.inputInstr(new GetelementptrIR(arrPointer,arrType,storeReg,indexs));
                this.nowBasicBlock.inputInstr(new StoreIR(storeValueReg,arrPointer,"i32"));
            }
        }
        return ;
    }

    public void handleVarDecl(VarDeclStmt varDeclStmt, BlockSymbolTable fatherTable) {//函数块内临时变量
        //Const，全局和与普通声明区别在于，前者明确是常量表达式，可以直接计算出来。后者要用handle
        if (varDeclStmt instanceof ArrDeclStmt arrDeclStmt) {
            //看看这是什么类型的数组
            String arrPointer = "%" + getReg();
            String integerStore;
            String type;
            String name = arrDeclStmt.getName();
            int n1 = arrDeclStmt.getN1();
            int n2 = arrDeclStmt.getN2();
            int bracket = arrDeclStmt.getBracket();
            if (bracket == 1) {
                type = "[" + n2 + " x i32]";
                this.nowBasicBlock.inputInstr(new AllocaIR(arrPointer,type));
            } else {
                type = "[" + n1 + " x " + "[" + n2 + " x i32]]";
                this.nowBasicBlock.inputInstr(new AllocaIR(arrPointer,type));
            }
            AddExpStmt addExpStmt;
            ArrayList<String> indexs;
            ArrayList<ComputeStmt> values = arrDeclStmt.getValue();
            if (values != null) {
                if (arrDeclStmt.isConst()) {
                    for (int i = 0; i < values.size(); i++) {
                        ComputeStmt computeStmt = values.get(i);
                        int value = computeStmt.getValue(fatherTable);
                        indexs = new ArrayList<>();
                        indexs.add("0");
                        if (bracket == 1) {
                            indexs.add(String.valueOf(i));
                        } else if (bracket == 2){
                            indexs.add(String.valueOf(i / n2));
                            indexs.add(String.valueOf(i % n2));
                        }
                        integerStore = "%" + getReg();
                        this.nowBasicBlock.inputInstr(new GetelementptrIR(integerStore,type,arrPointer,indexs));
                        this.nowBasicBlock.inputInstr(new StoreIR(String.valueOf(value),integerStore,"i32"));
                    }
                } else {
                    for (int i = 0; i < values.size(); i++) {
                        ComputeStmt computeStmt = values.get(i);
                        handleComputerStmt(computeStmt, fatherTable);
                        addExpStmt = computeStmt.getAddExpStmt();
                        indexs = new ArrayList<>();
                        indexs.add("0");
                        if (bracket == 1) {
                            indexs.add(String.valueOf(i));
                        } else if (bracket == 2){
                            indexs.add(String.valueOf(i / n2));
                            indexs.add(String.valueOf(i % n2));
                        }
                        integerStore = "%" + getReg();
                        this.nowBasicBlock.inputInstr(new GetelementptrIR(integerStore,type,arrPointer,indexs));
                        if (addExpStmt.isNum()) {
                            this.nowBasicBlock.inputInstr(new StoreIR(String.valueOf(addExpStmt.getValue()),integerStore,"i32"));
                        } else {
                            this.nowBasicBlock.inputInstr(new StoreIR("%" + (nowReg-2), integerStore,"i32"));
                            //注意！这里是nowReg-2,因为前面的nowReg-1被取出现的指针占据了，跟普通声明不一样，普通声明不需要取指针，直接alloc的就行，数组不一样
                        }
                    }
                }
                VarSymbolTable varSymbolTable = fatherTable.setTagForVar(name,arrPointer);
                varSymbolTable.setN1(n1);
                varSymbolTable.setN2(n2);
            }
            return ;
        } else { //非数组
            String tag = "%" + getReg();
            this.nowBasicBlock.inputInstr(new AllocaIR(tag,"i32"));
            String name = varDeclStmt.getName();
            if (varDeclStmt.isConst()) { //常量直接求值
                int value = varDeclStmt.getValue(fatherTable);
                this.nowBasicBlock.inputInstr(new StoreIR(String.valueOf(value),tag,"i32"));
            } else {
                if (varDeclStmt.hasInitialValue()) { //需要详细求值
                    ComputeStmt computeStmt = varDeclStmt.getInitialValue();
                    handleComputerStmt(computeStmt,fatherTable);

                    AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
                    String place;
                    if (addExpStmt.isNum()) {
                        place = String.valueOf(addExpStmt.getValue());
                    } else {
                        place = "%" + (nowReg-1);
                    }

                    this.nowBasicBlock.inputInstr(new StoreIR(place,tag,"i32"));
                }
            }
            fatherTable.setTagForVar(name,tag);//setTag放到最后！因为怕int c = c * 2这种情况，如果setTag在前面，就先分配空间地址了，这样就默认已经存在了
            return ;
        }
    }

    public void handleComputerStmt(ComputeStmt computeStmt, BlockSymbolTable fatherTable) {
        AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
        handleAddExpStmt(addExpStmt,fatherTable);
    }

    public void handleAddExpStmt(AddExpStmt addExpStmt, BlockSymbolTable fatherTable) {//tag的分配是严格地从左往右，从下到上，这样可以保证tag值连续
        ArrayList<MulExpStmt> mulExpStmts = addExpStmt.getMulExpStmts();

        //first
        String tag1;
        String tag2;
        String tag3;
        handleMulExpStmt(mulExpStmts.get(0),fatherTable);
        if (mulExpStmts.get(0).isNum()) {
            tag1 = String.valueOf(mulExpStmts.get(0).getValue());
        } else {
            int preReg = nowReg-1;
            tag1 = "%" + preReg;
        }
        for (int i = 2;i < mulExpStmts.size();i += 2) { //只可能奇数个
            handleMulExpStmt(mulExpStmts.get(i),fatherTable);

            if (mulExpStmts.get(i).isNum()) {
                tag2 = String.valueOf(mulExpStmts.get(i).getValue());
            } else {
                int preReg = nowReg-1;
                tag2 = "%" + preReg;
            }

            tag3 = "%" + getReg();//最后的值
            if (mulExpStmts.get(i-1) instanceof Op op && op.getLexType() == LexType.PLUS) {
                this.nowBasicBlock.inputInstr(new AddIR(tag1,tag2,tag3));
            } else if (mulExpStmts.get(i-1) instanceof Op op && op.getLexType() == LexType.MINU){
                this.nowBasicBlock.inputInstr(new SubIR(tag1,tag2,tag3));
            } else {
                System.err.println("handleAddExpStmtError!");
            }
            tag1 = tag3;//前值
        }
        return ;
    }

    public void handleMulExpStmt(MulExpStmt mulExpStmt,BlockSymbolTable fatherTable) {
        ArrayList<UnaryExpStmt> unaryExpStmts = mulExpStmt.getUnaryExpStmts();

        //first
        String tag1;
        String tag2;
        String tag3;
        handleUnaryExpStmt(unaryExpStmts.get(0),fatherTable);
        if (unaryExpStmts.get(0).getType() == 3) { //只是一个数字
            tag1 = String.valueOf(unaryExpStmts.get(0).getNumber());
        } else {
            int preReg = nowReg-1;
            tag1 = "%" + preReg;
        }
        for (int i = 2; i< unaryExpStmts.size(); i += 2) {
            handleUnaryExpStmt(unaryExpStmts.get(i),fatherTable);
            if (unaryExpStmts.get(i).getType() == 3) { //只是一个数字
                tag2 = String.valueOf(unaryExpStmts.get(i).getNumber());
            } else {
                int preReg = nowReg-1;
                tag2 = "%" + preReg;
            }
            tag3 = "%" + getReg();
            //('*' | '/' | '%')
            if (unaryExpStmts.get(i-1) instanceof  Op op && op.getLexType() == LexType.MULT) {
                this.nowBasicBlock.inputInstr(new MulIR(tag1,tag2,tag3));
            } else if (unaryExpStmts.get(i-1) instanceof  Op op && op.getLexType() == LexType.DIV) {
                this.nowBasicBlock.inputInstr(new SdivIR(tag1,tag2,tag3));
            } else if (unaryExpStmts.get(i-1) instanceof  Op op && op.getLexType() == LexType.MOD) {
                this.nowBasicBlock.inputInstr(new SremIR(tag1,tag2,tag3));
            } else {
                System.err.println("handleMulExpStmtError!");
            }
            tag1 = tag3;
        }
        return ;
    }

    public void handleUnaryExpStmt(UnaryExpStmt unaryExpStmt, BlockSymbolTable fatherTable) {
        int type = unaryExpStmt.getType();
        if (type == 1) { //(Exp)
            handleComputerStmt(unaryExpStmt.getComputeStmt(),fatherTable);
        } else if (type == 2) { //LVal
            LVal lVal = unaryExpStmt.getlVal();
            String name = lVal.getLValName();
            VarSymbolTable var = fatherTable.searchVar(name,fatherTable,false);
            String tag = var.getTag();
            /*
                2维
                a 0 0
                a[1] 0 0    0 1
                a[1][1] 0 1   0 1
                1维
                b 0 0
                b[1] 0 1
                根本原因在于，一维数组和取一维数组中的整数返回的类型是一样的，因此出现了这样的情况
             */
            if (var.getBracket() == 0) { // 非数组
                this.nowBasicBlock.inputInstr(new LoadIR("%" + getReg(), tag, "i32"));
            } else { //数组
                String storeReg;
                String arrType;
                String arrPointer = var.getTag();
                ArrayList<String> indexs;
                if (var.isParameter()) { //因为原来是指针！所以必须load一次才可以用，这样变成数组指针！注意变为数组指针后，与下面的区别
                    if (var.getBracket() == 1) {
                        storeReg = "%" + getReg();
                        this.nowBasicBlock.inputInstr(new LoadIR(storeReg, arrPointer, "i32*"));
                        //之后就是一维数组
                        if (lVal.getBracket() == 1) {
                            indexs = new ArrayList<>();
                            ComputeStmt n2 = lVal.getN2();
                            handleComputerStmt(n2,fatherTable);
                            AddExpStmt addExpStmt = n2.getAddExpStmt();
                            if (addExpStmt.isNum()) {
                                indexs.add(String.valueOf(addExpStmt.getValue()));
                            } else {
                                indexs.add("%" + (nowReg - 1));
                            }
                            String integerPointer = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(integerPointer,"i32",storeReg,indexs));
                            this.nowBasicBlock.inputInstr(new LoadIR("%" + getReg(), integerPointer, "i32"));
                        } else { //0
                            indexs = new ArrayList<>();
                            indexs.add(String.valueOf(0));
                            String integerPointer = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(integerPointer,"i32*",storeReg,indexs));
                        }
                    } else { // 2
                        storeReg = "%" + getReg();
                        arrType = getType(0, var.getN2());
                        this.nowBasicBlock.inputInstr(new LoadIR(storeReg,arrPointer,arrType + "*"));//注意这里，即使是二维数组，但是是一个指针
                        if (lVal.getBracket() == 2) {
                            indexs = new ArrayList<>();
                            ComputeStmt n1 = lVal.getN1();
                            handleComputerStmt(n1,fatherTable);
                            AddExpStmt addExpStmt = n1.getAddExpStmt();
                            if (addExpStmt.isNum()) {
                                indexs.add(String.valueOf(addExpStmt.getValue()));
                            } else {
                                indexs.add("%" + (nowReg - 1));
                            }
                            ComputeStmt n2 = lVal.getN2();
                            handleComputerStmt(n2,fatherTable);
                            addExpStmt = n2.getAddExpStmt();
                            if (addExpStmt.isNum()) {
                                indexs.add(String.valueOf(addExpStmt.getValue()));
                            } else {
                                indexs.add("%" + (nowReg - 1));
                            }
                            String integerPointer = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(integerPointer,arrType,storeReg,indexs));
                            this.nowBasicBlock.inputInstr(new LoadIR("%" + getReg(), integerPointer, "i32"));
                        } else if (lVal.getBracket() == 1) {
                            indexs = new ArrayList<>();
                            ComputeStmt n2 = lVal.getN2();
                            handleComputerStmt(n2,fatherTable);
                            AddExpStmt addExpStmt = n2.getAddExpStmt();
                            if (addExpStmt.isNum()) {
                                indexs.add(String.valueOf(addExpStmt.getValue()));
                            } else {
                                indexs.add("%" + (nowReg - 1));
                            }
                            indexs.add(String.valueOf(0));
                            String integerPointer = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(integerPointer,arrType,storeReg,indexs));
                        } else if (lVal.getBracket() == 0) {
                            ;//只要load就行
                        }
                    }
                } else {
                    if (var.getBracket() == 2) { //原本是二维数组(不是形参指针）
                        arrType = getType(var.getN1(), var.getN2());
                        if (lVal.getBracket() == 0) { //a
                            indexs = new ArrayList<>();
                            indexs.add(String.valueOf(0));
                            indexs.add(String.valueOf(0));
                            storeReg = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(storeReg,arrType,arrPointer,indexs));
                        } else if (lVal.getBracket() == 1) { //a[1]
                            indexs = new ArrayList<>();
                            ComputeStmt n2 = lVal.getN2();
                            handleComputerStmt(n2,fatherTable);
                            AddExpStmt addExpStmt = n2.getAddExpStmt();
                            if (addExpStmt.isNum()) {
                                indexs.add(String.valueOf(0));
                                indexs.add(String.valueOf(addExpStmt.getValue()));
                            } else {
                                indexs.add(String.valueOf(0));
                                indexs.add("%" + (nowReg - 1));
                            }
                            storeReg = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(storeReg,arrType,arrPointer,indexs));
                            arrPointer = storeReg;
                            storeReg = "%" + getReg();
                            arrType = getType(0, var.getN2());
                            indexs = new ArrayList<>();
                            indexs.add(String.valueOf(0));
                            indexs.add(String.valueOf(0));
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(storeReg,arrType,arrPointer,indexs));
                        } else if (lVal.getBracket() == 2) { //a[1][1]
                            indexs = new ArrayList<>();
                            ComputeStmt n1 = lVal.getN1();
                            handleComputerStmt(n1,fatherTable);
                            AddExpStmt addExpStmt1 = n1.getAddExpStmt();
                            if (addExpStmt1.isNum()) {
                                indexs.add(String.valueOf(0));
                                indexs.add(String.valueOf(addExpStmt1.getValue()));
                            } else {
                                indexs.add(String.valueOf(0));
                                indexs.add("%" + (nowReg - 1));
                            }
                            storeReg = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(storeReg,arrType,arrPointer,indexs));
                            arrPointer = storeReg;
                            storeReg = "%" + getReg();
                            arrType = getType(0, var.getN2());
                            indexs = new ArrayList<>();
                            ComputeStmt n2 = lVal.getN2();
                            handleComputerStmt(n2,fatherTable);
                            AddExpStmt addExpStmt2 = n2.getAddExpStmt();
                            if (addExpStmt2.isNum()) {
                                indexs.add(String.valueOf(0));
                                indexs.add(String.valueOf(addExpStmt2.getValue()));
                            } else {
                                indexs.add(String.valueOf(0));
                                indexs.add("%" + (nowReg - 1));
                            }
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(storeReg,arrType,arrPointer,indexs));
                            String integerPointer = "%" + (nowReg-1);
                            String loadStore = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new LoadIR(loadStore, integerPointer, "i32"));
                        }
                    } else { //原本是一维数组
                        arrType = getType(0, var.getN2());
                        if (lVal.getBracket() == 0) {
                            indexs = new ArrayList<>();
                            indexs.add(String.valueOf(0));
                            indexs.add(String.valueOf(0));
                            storeReg = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(storeReg, arrType, arrPointer, indexs));
                        } else {
                            ComputeStmt n2 = lVal.getN2();
                            handleComputerStmt(n2, fatherTable);
                            AddExpStmt addExpStmt = n2.getAddExpStmt();
                            indexs = new ArrayList<>();
                            if (addExpStmt.isNum()) {
                                indexs.add(String.valueOf(0));
                                indexs.add(String.valueOf(addExpStmt.getValue()));
                            } else {
                                indexs.add(String.valueOf(0));
                                indexs.add("%" + (nowReg - 1));
                            }
                            storeReg = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new GetelementptrIR(storeReg, arrType, arrPointer, indexs));
                            String integerPointer = "%" + (nowReg - 1);
                            String loadStore = "%" + getReg();
                            this.nowBasicBlock.inputInstr(new LoadIR(loadStore, integerPointer, "i32"));
                        }
                    }
                }
//                String pointer = "%" + (nowReg - 1);
//                this.nowBasicBlock.inputInstr(new LoadIR("%" + getReg(),pointer));
            }
        } else if (type == 3) {
            ;
        } else if (type == 4) {
            CallExpr callExpr = unaryExpStmt.getCallExpr();
            handleCallExpr(callExpr,fatherTable);
        } else if (type == 5) {
            handleUnaryExpStmt(unaryExpStmt.getUnaryExpStmt(),fatherTable);
            String reg1;
            String reg2;
            String reg3;
            UnaryExpStmt subUnaryExpStmt = unaryExpStmt.getUnaryExpStmt();
            if (subUnaryExpStmt.getType() == 3) {
                if (unaryExpStmt.getOp().getLexType() == LexType.PLUS) {
                    reg1 = "0";
                    reg2 = String.valueOf(subUnaryExpStmt.getNumber());
                    reg3 = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new AddIR(reg1,reg2,reg3));
                } else if (unaryExpStmt.getOp().getLexType() == LexType.MINU) {
                    reg1 = "0";
                    reg2 = String.valueOf(subUnaryExpStmt.getNumber());
                    reg3 = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new SubIR(reg1,reg2,reg3));
                } else if (unaryExpStmt.getOp().getLexType() == LexType.NOT) {
                    String num = String.valueOf(subUnaryExpStmt.getNumber());
                    String compareReg = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new IcmpIR(compareReg,"ne",num,"0"));
                    this.nowBasicBlock.inputInstr(new ZextIR("%" + getReg(),compareReg));
                }
            } else {
                if (unaryExpStmt.getOp().getLexType() == LexType.PLUS) {
                    reg1 = "0";
                    reg2 = "%" + (nowReg-1);
                    reg3 = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new AddIR(reg1,reg2,reg3));
                } else if (unaryExpStmt.getOp().getLexType() == LexType.MINU) {
                    reg1 = "0";
                    reg2 = "%" + (nowReg-1);
                    reg3 = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new SubIR(reg1,reg2,reg3));
                } else if (unaryExpStmt.getOp().getLexType() == LexType.NOT) {
                    String beforeAns = "%" + (nowReg-1);
                    String compareReg = "%" + getReg();
                    this.nowBasicBlock.inputInstr(new IcmpIR(compareReg,"ne",beforeAns,"0"));
                    this.nowBasicBlock.inputInstr(new ZextIR("%" + getReg(),compareReg));
                }
            }
        }
        return ;
    }

    public ArrayList<String> handleCallParas(ArrayList<ComputeStmt> paras, BlockSymbolTable fatherTable) {
        ArrayList<String> place = new ArrayList<>();
        for (ComputeStmt computeStmt : paras) {
            handleComputerStmt(computeStmt,fatherTable);
            AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
            if (addExpStmt.isNum()) {
                int value = addExpStmt.getValue();
                String s = String.valueOf(value);
                place.add(s);
            } else { //如果不是直接数，那么保存答案的最后寄存器就是nowReg-1
                int reg = nowReg-1;
                place.add("%" + reg);
            }
        }

        return place;
    }

    public String getType(int n1, int n2) {
        if (n1 == 0) {
            return "[" + n2 + " x i32]";
        } else {
            return "[" + n1 + " x " + "[" + n2 + " x i32]]";
        }
    }

    public String handleBlockEnd() { //块的结束
        String newBlockLabel = "%" + getReg();
        BrIR oldBlockBr = new BrIR(newBlockLabel);
        this.nowBasicBlock.inputInstr(oldBlockBr);
        //这个就是基本块的末尾
        this.nowBasicBlock.setEndOfBlock(oldBlockBr,"blockBr");//set在后面
        return newBlockLabel;
    }

    public void handleBlockBegin(String newBlockLabel) {
        //前基本块结束
        int newBlockNo = this.nowBasicBlock.getBlockNo() + 1;
        BasicBlock basicBlock = new BasicBlock(newBlockLabel,newBlockNo);
        this.blocks.add(this.nowBasicBlock);
        this.nowBasicBlock = basicBlock;
        LabelLineIR beginLabel = new LabelLineIR(newBlockLabel);
        this.nowBasicBlock.inputInstr(beginLabel);
    }

    public int checkHasBlock(Stmt stmt) {
        if (stmt == null) {
            return 0;
        } else {
            if (stmt instanceof CompoundStmt) {
                return 1;
            } else if (stmt instanceof IfStmt ifStmt) {
                return checkHasBlock(ifStmt.getThenStmt()) + checkHasBlock(ifStmt.getElseStmt());
            } else if (stmt instanceof ForStmt forStmt) {
                return checkHasBlock(forStmt.getForStmt());
            } else {
                return 0;
            }
        }
    }
}
