package Ir;

import AST.ASTroot;
import AST.AddExpStmt;
import AST.ArrDeclStmt;
import AST.AssignStmt;
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

import Frontend.IoFile;

public class Generator {
    private BlockSymbolTable mainTable;
    private ASTroot asTroot;
    private int nowReg;
    private BlockSymbolTable nowBlock;

    private SymbolTable nowSymbolTable;
    public Generator(BlockSymbolTable table, ASTroot asTroot) {
        this.mainTable = table;
        this.asTroot = asTroot;
        this.nowReg = 0;
        this.nowBlock = table;
    }

    public int getReg() {
        return this.nowReg++;
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

        //

        //非数组
        int value = varDeclStmt.getValue(fatherTable);
        VarSymbolTable var = fatherTable.searchVar(name,fatherTable,true);
        var.setTag("@"+name);
        var.setValue(value);
        return new GlobalVar(new GlobalVarIr(name,type,value));
    }

    public GlobalFunc handleGlobalFunctions(FuncDeclStmt funcDeclStmt,SymbolTable symbolTable) {
        nowReg = 0;//函数从0开始
        String name = funcDeclStmt.getName();
        String type = funcDeclStmt.getReturnType();
        FuncSymbolTable funcSymbolTable = (FuncSymbolTable) symbolTable;
        FuncDeclIR funcDeclIR = funcDeclStmt.getIr();
        ArrayList<Instr> instrs = new ArrayList<>();
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
        getReg();
        if (paras != null) {
            for (SymbolTable para : paras.getSymbolTables()) {
                if (para.compareType(SymbolType.var) && ((VarSymbolTable)para).isParameter()) {
                    String tag = "%" + getReg();
                    //这里的思路使，形参先把tag设置成他的值寄存器（实际上应该要存储地址寄存器但是还没有分配），分配了地址寄存器后，再改回来
                    instrs.add(new AllocaIR(tag));
                    instrs.add(new StoreIR(para.getTag(), tag));
                    para.setTag(tag);//改回来
                }
            }
        }
        instrs.addAll(handleCompoundStmt(funcDeclStmt.getStmts(),paras));
        GlobalFunc globalFunc = new GlobalFunc(funcDeclIR,instrs);
        return globalFunc;
    }

    public GlobalFunc handleMain(FuncDeclStmt funcDeclStmt,BlockSymbolTable paras) {
        nowReg = 0;//函数从0开始
        FuncDeclIR funcDeclIR = funcDeclStmt.getIr();
        ArrayList<Instr> instrs = new ArrayList<>();
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
        getReg();
        if (paras != null) {
            for (SymbolTable para : paras.getSymbolTables()) {
                if (para.compareType(SymbolType.var) && ((VarSymbolTable)para).isParameter()) {
                    String tag = "%" + getReg();
                    instrs.add(new AllocaIR(tag));
                    instrs.add(new StoreIR(para.getTag(), tag));
                    para.setTag(tag);
                }
            }
        }
        instrs.addAll(handleCompoundStmt(funcDeclStmt.getStmts(),paras));
        GlobalFunc globalFunc = new GlobalFunc(funcDeclIR,instrs);
        return globalFunc;
    }

    public ArrayList<Instr> handleCompoundStmt(CompoundStmt compoundStmt,BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<Stmt> stmts = compoundStmt.getStmts();
        int numberOfBlockSymbolTable = 0;
        for (Stmt stmt : stmts) {
            if (stmt instanceof VarDeclStmt) {
                instrs.addAll(handleVarDecl((VarDeclStmt) stmt, fatherTable));
            }  else {
                instrs.addAll(handleStmt(stmt,fatherTable,numberOfBlockSymbolTable));
                if (stmt instanceof CompoundStmt || stmt instanceof IfStmt || stmt instanceof ForStmt) { //顺序记录块符号表在fatherTable顺序中第几位
                    numberOfBlockSymbolTable++;
                }
            }
        }
        return instrs;
    }

    public ArrayList<Instr> handleStmt(Stmt stmt, BlockSymbolTable fatherTable,int numberOfBlockSymbolTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        if (stmt instanceof AssignStmt) {
            instrs.addAll(handleAssignStmt((AssignStmt) stmt,fatherTable));
        } else if (stmt instanceof CompoundStmt) { //注意这里的fatherTable会变化
            BlockSymbolTable nowBlock = fatherTable.searchBlockSymbolTable(numberOfBlockSymbolTable,fatherTable);
            //getReg();//块分配
            instrs.addAll(handleCompoundStmt((CompoundStmt) stmt, nowBlock));
        } else if (stmt instanceof ComputeStmt) {
            instrs.addAll(handleComputerStmt((ComputeStmt) stmt,fatherTable));
        } else if (stmt instanceof ReturnStmt) {
            ReturnStmt returnStmt = (ReturnStmt) stmt;
            boolean returnVoid = returnStmt.returnVoid();
            if (!returnVoid) {
                ComputeStmt computeStmt = returnStmt.getReturnExp();
                AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
                if (addExpStmt.isNum()) {
                    String value = String.valueOf(addExpStmt.getValue());
                    instrs.add(new ReturnIR(returnVoid,value));
                } else {
                    instrs.addAll(handleComputerStmt(computeStmt,fatherTable));
                    instrs.add(new ReturnIR(returnVoid,"%" + (nowReg-1)));
                }
            } else {
                instrs.add(new ReturnIR(returnVoid,null));
            }
        } else if (stmt instanceof CallExpr) {
            CallExpr callExpr = (CallExpr) stmt;
            instrs.addAll(handleCallExpr(callExpr,fatherTable));
        } else if (stmt instanceof ForStmt) {
            ; //
        } else if (stmt instanceof IfStmt) {
            instrs.addAll(handleIfStmt((IfStmt) stmt,fatherTable,numberOfBlockSymbolTable));
        } else if (stmt instanceof BreakStmt) {
            ; //
        } else if (stmt instanceof ContinueStmt) {
            ; //
        }
        return instrs;
    }

    public ArrayList<Instr> handleIfStmt(IfStmt ifStmt, BlockSymbolTable fatherTable, int numberOfBlockSymbolTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        CondStmt condStmt = ifStmt.getCondStmt();
        Stmt thenStmt = ifStmt.getThenStmt();
        Stmt elseStmt = ifStmt.getElseStmt();

        String ifCross = "%" + getReg();
        BrIR begin = new BrIR(ifCross);
        JumpUnit ifUnit = new JumpUnit();
        ifUnit.setBegin(begin);
        instrs.add(begin);//两个地方都要加
        instrs.add(new LabelLineIR(ifCross));

        instrs.addAll(handleCondStmt(condStmt,fatherTable,ifUnit));
        //if 开始;
        instrs.addAll(handleStmt(thenStmt,fatherTable,numberOfBlockSymbolTable));
        if (elseStmt == null) {
            BrIR elseBegin = null;

            String endReg = "%" + getReg();
            BrIR end = new BrIR(endReg);//if执行结束的无条件跳转
            instrs.add(end);
            instrs.add(new LabelLineIR(endReg));
            ifUnit.setLorExpFailCrossBrLine(endReg);

            ifUnit.setEnd(end);
            ifUnit.setElseBegin(elseBegin);

        } else {
            String elseBeginLine = "%" + getReg();
            BrIR ifEnd = new BrIR("ifEnd");//尚未确定
            instrs.add(ifEnd);
            instrs.add(new LabelLineIR(elseBeginLine));
            ifUnit.setLorExpFailCrossBrLine(elseBeginLine);//回填之前lorExp失败的跳转
            instrs.addAll(handleStmt(elseStmt,fatherTable,numberOfBlockSymbolTable));

            String End = "%" + getReg();
            ifEnd.setUnconditionalJump_reg(End);//回填之前if结束的跳转
            instrs.add(new BrIR(End));//else结束的跳转
        }

        return instrs;
    }

    public ArrayList<Instr> handleCondStmt(CondStmt condStmt, BlockSymbolTable fatherTable,JumpUnit ifUnit) {
        ArrayList<Instr> instrs = new ArrayList<>();
        LorExp lorExp = condStmt.getLorExp();
        instrs.addAll(handleLorExp(lorExp,fatherTable,ifUnit));
        return instrs;
    }

    public ArrayList<Instr> handleLorExp(LorExp lorExp, BlockSymbolTable fatherTable,JumpUnit ifUnit) {
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<LAndExp> lAndExps = lorExp.getlAndExps();
        //instrs.addAll(handleLAndExp(lAndExps.get(0),fatherTable,ifUnit));
        String tag1;
        ArrayList<BrIR> acceptBrIR = new ArrayList<>();
        ArrayList<BrIR> wrongBrIR = new ArrayList<>();
        for (int i = 0; i < lAndExps.size(); i++) {
            instrs.addAll(handleLAndExp(lAndExps.get(i),fatherTable,wrongBrIR));

            if (lAndExps.get(i).isNum()) {
                tag1 = String.valueOf(lAndExps.get(i).getValue());
            } else {
                int preReg = nowReg - 1;
                tag1 = "%" +preReg;
            }

            //如果 && 全对， 那么应该无条件跳转到if入口，但是这个入口现在还不知道在哪，先收集着
            BrIR accept = new BrIR("null");
            acceptBrIR.add(accept);//现在还不知道入口，结束的时候统一设置
            instrs.add(accept);

            //如果不对，设置下一个||的入口点！并为之前的wrong设置入口
            String wrongLine = "%" + getReg();
            instrs.add(new LabelLineIR(wrongLine));
            for (BrIR wrong : wrongBrIR) {
                wrong.setWrong(wrongLine);
            }
            // wrong要清空
            wrongBrIR.clear();
        }
        // 注意，最后一个||如果没有成功，那么要跳到if后的地方！这个还没有处理，所以应该先记录下来
        BrIR lorExpFailCross = new BrIR("LorExpWrong");
        ifUnit.setLorExpFailCross(nowReg-1,lorExpFailCross);
        instrs.add(lorExpFailCross);
        //if 开始
        int ifBeginLine = getReg();
        BrIR ifBegin = new BrIR("%" + ifBeginLine);
        ifUnit.setIfBegin(ifBegin);
        instrs.add(new LabelLineIR("%" + ifBeginLine));
        for (BrIR accept : acceptBrIR) {
            accept.setUnconditionalJump_reg("%" + ifBeginLine);
        }

        return instrs;
    }

    public ArrayList<Instr> handleLAndExp(LAndExp lAndExp, BlockSymbolTable fatherTable,ArrayList<BrIR> wrongBrIR) {
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<EqExp> eqExps = lAndExp.getEqExps();

        //instrs.addAll(handleEqExp(eqExps.get(0),fatherTable));
        String tag1;
        for (int i = 0; i < eqExps.size(); i++) {
            instrs.addAll(handleEqExp(eqExps.get(i),fatherTable));
            if (eqExps.get(i).isNum()) {
                tag1 = String.valueOf(eqExps.get(i).getValue());
            } else {
                int preReg = nowReg - 1;
                tag1 = "%" + preReg;//已经zext过
            }

            //最后的结果需要和0比较
            String cond = "%" + getReg();
            instrs.add(new IcmpIR(cond,"ne","0",tag1));
            String accept = "%" + getReg();
            BrIR brIR = new BrIR(cond,accept,"lAndExpWrong");
            wrongBrIR.add(brIR);
            instrs.add(brIR);
            instrs.add(new LabelLineIR(accept)); //条件语句对了的话，跳转label
            //注意都是指针！以后ifUnit改，instr也会改
        }
        //&&的最后accept,就可以直接跳转到if入口

        return instrs;
    }

    public ArrayList<Instr> handleEqExp(EqExp eqExp, BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<RelExp> relExps = eqExp.getRelExps();

        instrs.addAll(handleRelExp(relExps.get(0),fatherTable));
        String tag1;
        String tag2;
        if (relExps.get(0).isNum()) {
            tag1 = String.valueOf(relExps.get(0).getValue());
        } else {
            int preReg = nowReg - 1;
            tag1 = "%" + preReg;
        }

        for (int i = 2; i < relExps.size(); i += 2) {
            instrs.addAll(handleRelExp(relExps.get(i),fatherTable));
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
                    instrs.add(new IcmpIR(transferReg,"eq",tag1,tag2));
                } else if (op.getType() == 2) {
                    instrs.add(new IcmpIR(transferReg,"ne",tag1,tag2));
                }
            }

            String result = "%" + getReg();
            instrs.add(new ZextIR(result, transferReg));

            tag1 = result; //上一次的结果下一次用，如果还有的话
        }

        return instrs;
    }

    public ArrayList<Instr> handleRelExp(RelExp relExp, BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<AddExpStmt> addExpStmts = relExp.getAddExpStmts();

        String tag1;
        String tag2;
        ArrayList<Instr> tmp = handleAddExpStmt(addExpStmts.get(0),fatherTable);
        instrs.addAll(tmp);
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
                instrs.addAll(handleAddExpStmt(addExpStmts.get(i),fatherTable));
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
                        instrs.add(new IcmpIR(transferReg, "slt", tag1, tag2));
                    } else if (op.getType() == 2) {
                        instrs.add(new IcmpIR(transferReg, "sle", tag1, tag2));
                    } else if (op.getType() == 3) {
                        instrs.add(new IcmpIR(transferReg, "sgt", tag1, tag2));
                    } else if (op.getType() == 4) {
                        instrs.add(new IcmpIR(transferReg, "sge", tag1, tag2));
                    }
                }
                String result = "%" + getReg();
                instrs.add(new ZextIR(result, transferReg));

                tag1 = result; //上一次的结果下一次用，如果还有的话
            }
            //最后的结果需要和0比较
            //String cond = "%" + getReg();
            //instrs.add(new IcmpIR(cond,"ne","0",tag1));
            //最后的结果tag1进行br
            //instrs.add(new BrIR(cond,"%" + getReg(),null));
            //这个应该是land和lor用的
        // }

        return instrs;
    }

    public ArrayList<Instr> handleCallExpr(CallExpr callExpr,BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        String name = callExpr.getFunctionName();
        if (name.equals("printf")) {
            ArrayList<ComputeStmt> paras = callExpr.getParas();

            Pair pair = handleCallParas(paras,fatherTable);
            instrs.addAll(pair.getInstrs());
            ArrayList<String> place = pair.getPlace();
            int realPara = 0;//index of printf
            String format = callExpr.getFormat();
            char[] format_char = format.toCharArray();
            for (int i = 1; i < format_char.length-1; i++) { //去掉头尾的“ ”
                if (format_char[i] == '%' && i + 1 < format_char.length-1 && format_char[i+1] == 'd') {
                    //输出值
                    ArrayList<String> tmp = new ArrayList<>();//para 因为putint只有一个参数，所以每次都新建一个，加入place的元素
                    tmp.add(place.get(realPara++));
                    instrs.add(new CallIR("putint","void", tmp, null));
                    i++;//d
                } else if (format_char[i] == '\\' && i + 1 < format_char.length-1 && format_char[i+1] == 'n') {
                    ArrayList<String> tmp = new ArrayList<>();
                    tmp.add("10");
                    instrs.add(new CallIR("putch","void", tmp, null));
                    i++;//n
                }
                else {
                    ArrayList<String> tmp = new ArrayList<>();
                    int c = format_char[i];
                    tmp.add(String.valueOf(c));
                    instrs.add(new CallIR("putch","void", tmp, null));
                }
            }
        } else {
            FuncSymbolTable funcSymbolTable = fatherTable.searchFunction(name,mainTable);
            String returnType = funcSymbolTable.getReturnType();
            ArrayList<ComputeStmt> paras = callExpr.getParas();

            Pair pair = handleCallParas(paras,fatherTable);
            instrs.addAll(pair.getInstrs());
            ArrayList<String> place = pair.getPlace();
            String storeReg;
            if (returnType.equals("void")) {
                storeReg = null;
            } else {
                storeReg = "%" + getReg();
            }

            instrs.add(new CallIR(name,returnType,place,storeReg));
        }
        return instrs;
    }

    public ArrayList<Instr> handleAssignStmt(AssignStmt assignStmt, BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        LVal lVal = assignStmt.getlVal();
        ComputeStmt computeStmt = assignStmt.getComputeStmt();
        boolean isGetInt = assignStmt.isGetInt();
        int bracket = lVal.getBracket();
        if (bracket == 0) { //非数组
            String name = lVal.getLValName();
            VarSymbolTable var = fatherTable.searchVar(name,fatherTable,false);
            String pointer = var.getTag();
            if (isGetInt) {
                instrs.add(new CallIR("getint","int",new ArrayList<>(),"%" + getReg()));
                instrs.add(new StoreIR("%"+(nowReg-1),pointer));
            } else {
                instrs.addAll(handleComputerStmt(computeStmt,fatherTable));
                String storeValueReg;
                AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
                if (addExpStmt.isNum()) {
                    storeValueReg = String.valueOf(addExpStmt.getValue());
                } else {
                    storeValueReg = "%" + (nowReg-1); //如果不是全局变量，值保存在nowReg前一个寄存器里
                }
                instrs.add(new StoreIR(storeValueReg,pointer));
            }
        } else { //
            ;
        }
        return instrs;
    }

    public ArrayList<Instr> handleVarDecl(VarDeclStmt varDeclStmt, BlockSymbolTable fatherTable) {//函数块内临时变量
        ArrayList<Instr> instrs = new ArrayList<>();
        if (varDeclStmt instanceof ArrDeclStmt) {
            return null;//未处理
        } else { //非数组
            String tag = "%" + getReg();
            instrs.add(new AllocaIR(tag));
            String name = varDeclStmt.getName();
            if (varDeclStmt.isConst()) { //常量直接求值
                int value = varDeclStmt.getValue(fatherTable);
                instrs.add(new StoreIR("%"+value,tag));
            } else {
                if (varDeclStmt.hasInitialValue()) { //需要详细求值
                    ComputeStmt computeStmt = varDeclStmt.getInitialValue();
                    ArrayList<Instr> tmp = handleComputerStmt(computeStmt,fatherTable);
                    instrs.addAll(tmp);

                    AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
                    String place;
                    if (addExpStmt.isNum()) {
                        place = String.valueOf(addExpStmt.getValue());
                    } else {
                        place = "%" + (nowReg-1);
                    }

                    instrs.add(new StoreIR(place,tag));
                }
            }
            fatherTable.setTagForVar(name,tag);//setTag放到最后！因为怕int c = c * 2这种情况，如果setTag在前面，就先分配空间地址了，这样就默认已经存在了
            return instrs;
        }
    }

    public ArrayList<Instr> handleComputerStmt(ComputeStmt computeStmt, BlockSymbolTable fatherTable) {
        AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
        return handleAddExpStmt(addExpStmt,fatherTable);
    }

    public ArrayList<Instr> handleAddExpStmt(AddExpStmt addExpStmt, BlockSymbolTable fatherTable) {//tag的分配是严格地从左往右，从下到上，这样可以保证tag值连续
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<MulExpStmt> mulExpStmts = addExpStmt.getMulExpStmts();

        //first
        String tag1;
        String tag2;
        String tag3;
        ArrayList<Instr> tmp1 = handleMulExpStmt(mulExpStmts.get(0),fatherTable);
        instrs.addAll(tmp1);
        if (mulExpStmts.get(0).isNum()) {
            tag1 = String.valueOf(mulExpStmts.get(0).getValue());
        } else {
            int preReg = nowReg-1;
            tag1 = "%" + preReg;
        }
        for (int i = 2;i < mulExpStmts.size();i += 2) { //只可能奇数个
            ArrayList<Instr> tmp2 = handleMulExpStmt(mulExpStmts.get(i),fatherTable);
            instrs.addAll(tmp2);

            if (mulExpStmts.get(i).isNum()) {
                tag2 = String.valueOf(mulExpStmts.get(i).getValue());
            } else {
                int preReg = nowReg-1;
                tag2 = "%" + preReg;
            }

            tag3 = "%" + getReg();//最后的值
            if (mulExpStmts.get(i-1) instanceof Op op && op.getLexType() == LexType.PLUS) {
                instrs.add(new AddIR(tag1,tag2,tag3));
            } else if (mulExpStmts.get(i-1) instanceof Op op && op.getLexType() == LexType.MINU){
                instrs.add(new SubIR(tag1,tag2,tag3));
            } else {
                System.err.println("handleAddExpStmtError!");
            }
            tag1 = tag3;//前值
        }
        return instrs;
    }

    public ArrayList<Instr> handleMulExpStmt(MulExpStmt mulExpStmt,BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<UnaryExpStmt> unaryExpStmts = mulExpStmt.getUnaryExpStmts();

        //first
        String tag1;
        String tag2;
        String tag3;
        ArrayList<Instr> tmp1 = handleUnaryExpStmt(unaryExpStmts.get(0),fatherTable);
        instrs.addAll(tmp1);
        if (unaryExpStmts.get(0).getType() == 3) { //只是一个数字
            tag1 = String.valueOf(unaryExpStmts.get(0).getNumber());
        } else {
            int preReg = nowReg-1;
            tag1 = "%" + preReg;
        }
        for (int i = 2; i< unaryExpStmts.size(); i += 2) {
            ArrayList<Instr> tmp2 = handleUnaryExpStmt(unaryExpStmts.get(i),fatherTable);
            instrs.addAll(tmp2);
            if (unaryExpStmts.get(i).getType() == 3) { //只是一个数字
                tag2 = String.valueOf(unaryExpStmts.get(i).getNumber());
            } else {
                int preReg = nowReg-1;
                tag2 = "%" + preReg;
            }
            tag3 = "%" + getReg();
            //('*' | '/' | '%')
            if (unaryExpStmts.get(i-1) instanceof  Op op && op.getLexType() == LexType.MULT) {
                instrs.add(new MulIR(tag1,tag2,tag3));
            } else if (unaryExpStmts.get(i-1) instanceof  Op op && op.getLexType() == LexType.DIV) {
                instrs.add(new SdivIR(tag1,tag2,tag3));
            } else if (unaryExpStmts.get(i-1) instanceof  Op op && op.getLexType() == LexType.MOD) {
                instrs.add(new SremIR(tag1,tag2,tag3));
            } else {
                System.err.println("handleMulExpStmtError!");
            }
            tag1 = tag3;
        }
        return instrs;
    }

    public ArrayList<Instr> handleUnaryExpStmt(UnaryExpStmt unaryExpStmt, BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        int type = unaryExpStmt.getType();
        if (type == 1) { //(Exp)
            instrs.addAll(handleComputerStmt(unaryExpStmt.getComputeStmt(),fatherTable));
            return instrs;
        } else if (type == 2) { //LVal
            LVal lVal = unaryExpStmt.getlVal();
            String name = lVal.getLValName();
            VarSymbolTable var = fatherTable.searchVar(name,fatherTable,false);
            String tag = var.getTag();
            int storeReg = getReg();
            instrs.add(new LoadIR("%"+storeReg, tag));
            return instrs;
        } else if (type == 3) {
            return instrs;
        } else if (type == 4) {
            CallExpr callExpr = unaryExpStmt.getCallExpr();
            instrs.addAll(handleCallExpr(callExpr,fatherTable));
        } else if (type == 5) {
            instrs.addAll(handleUnaryExpStmt(unaryExpStmt.getUnaryExpStmt(),fatherTable));
            String reg1;
            String reg2;
            String reg3;
            UnaryExpStmt subUnaryExpStmt = unaryExpStmt.getUnaryExpStmt();
            if (subUnaryExpStmt.getType() == 3) {
                if (unaryExpStmt.getOp().getLexType() == LexType.PLUS) {
                    reg1 = "0";
                    reg2 = String.valueOf(subUnaryExpStmt.getNumber());
                    reg3 = "%" + getReg();
                    instrs.add(new AddIR(reg1,reg2,reg3));
                } else if (unaryExpStmt.getOp().getLexType() == LexType.MINU) {
                    reg1 = "0";
                    reg2 = String.valueOf(subUnaryExpStmt.getNumber());
                    reg3 = "%" + getReg();
                    instrs.add(new SubIR(reg1,reg2,reg3));
                }
            } else {
                if (unaryExpStmt.getOp().getLexType() == LexType.PLUS) {
                    reg1 = "0";
                    reg2 = "%" + (nowReg-1);
                    reg3 = "%" + getReg();
                    instrs.add(new AddIR(reg1,reg2,reg3));
                } else if (unaryExpStmt.getOp().getLexType() == LexType.MINU) {
                    reg1 = "0";
                    reg2 = "%" + (nowReg-1);
                    reg3 = "%" + getReg();
                    instrs.add(new SubIR(reg1,reg2,reg3));
                }
            }
        }
        return instrs;
    }

    public Pair handleCallParas(ArrayList<ComputeStmt> paras, BlockSymbolTable fatherTable) {
        ArrayList<Instr> instrs = new ArrayList<>();
        ArrayList<String> place = new ArrayList<>();
        for (ComputeStmt computeStmt : paras) {
            instrs.addAll(handleComputerStmt(computeStmt,fatherTable));
            AddExpStmt addExpStmt = computeStmt.getAddExpStmt();
            if (addExpStmt.isNum()) {
                int value = addExpStmt.getValue();
                String s = String.valueOf(value);
                place.add(s);
            } else { //如果不是直接数，那么保存答案的最后寄存器就是nowReg-1
                int reg = nowReg-1;
                place.add("%" + String.valueOf(reg));
            }
        }

        return new Pair(instrs,place);
    }
}
