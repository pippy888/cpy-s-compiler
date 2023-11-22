package AST;

import Frontend.GrammarNode;
import Frontend.LexType;
import Frontend.Token;
import SymbolTablePackage.BlockSymbolTable;
import SymbolTablePackage.VarSymbolTable;

import java.util.ArrayList;

public class UnaryExpStmt extends MulExpStmt{
    int type;
    /*
    1: (Exp)
    2: LVal
    3: Number
    4: 函数
    5：op UnaryExpStmt
     */
    private ComputeStmt computeStmt; // 1

    //2
    private LVal lVal;
    //2

    //3
    private int number;
    //3

    //4
    private CallExpr callExpr;
    //4

    //5
    private Op op;

    private UnaryExpStmt unaryExpStmt;
    //5
    public UnaryExpStmt() { //Op

    }
    public UnaryExpStmt(GrammarNode unaryExpNode) {
        GrammarNode firstNode = unaryExpNode.getNodes().get(0);
        if (firstNode.getNodeName().equals("PrimaryExp")) {
            GrammarNode priNode = firstNode.getNodes().get(0);
            if (priNode instanceof Token token && token.compareLexType(LexType.LPARENT)) {
                //Exp
                this.type = 1;
                GrammarNode addExp = firstNode.getNodes().get(1).getNodes().get(0);
                AddExpStmt addExpStmt = new AddExpStmt(false,0);
                addExpStmt.departAddExp(addExp);
                this.computeStmt = new ComputeStmt(addExpStmt);
            } else if (priNode.getNodeName().equals("LVal")) {
                this.type = 2;
                String name = priNode.getNodes().get(0).getNodeName();
                if (priNode.getNodes().size() == 1) {
                    this.lVal = new LVal(name,0,0,0);
                }
                else if (priNode.getNodes().size() == 4) {
                    //一维数组
                    GrammarNode constExp = priNode.getNodes().get(2);
                    ComputeStmt constStmt = BuildAST.getComputerStmt(constExp);
                    int constValue = constStmt.getValue(null);//因为这时候不会用常量
                    this.lVal = new LVal(name,1,0,constValue);

                } else if (priNode.getNodes().size() == 7) {
                    GrammarNode constExp1 = priNode.getNodes().get(2);
                    ComputeStmt constStmt1 = BuildAST.getComputerStmt(constExp1);
                    int constValue1 = constStmt1.getValue(null);

                    GrammarNode constExp2 = priNode.getNodes().get(5);
                    ComputeStmt constStmt2 = BuildAST.getComputerStmt(constExp2);
                    int constValue2 = constStmt2.getValue(null);

                    this.lVal = new LVal(name,2,constValue1,constValue2);
                }
            } else {
                this.type = 3;
                this.number = Integer.parseInt(priNode.getNodes().get(0).getNodeName());
            }
        } else if (firstNode instanceof Token token && token.compareLexType(LexType.IDENFR)) {
            //4
            this.type = 4;
            String name = token.getToken();
            GrammarNode paras = unaryExpNode.getNodes().get(unaryExpNode.getNodes().size()-2);
            ArrayList<ComputeStmt> parasArray = new ArrayList<>();
            //倒数第二个node是函数参数
            if (unaryExpNode.getNodes().size() > 3) {
                for (GrammarNode expNode : paras.getNodes()) {
                    if (expNode.getNodeName().equals("Exp")) {
                        ComputeStmt tmp = BuildAST.getComputerStmt(expNode);
                        parasArray.add(tmp);
                    }
                }
            }

            this.callExpr = new CallExpr(name,parasArray,null);
        } else if(firstNode.getNodeName().equals("UnaryOp")) {
            this.type = 5;
            GrammarNode op = firstNode.getNodes().get(0);
            String s = ((Token)op).getLexType();
            LexType lexType = s.equals("PLUS") ? LexType.PLUS :
                                    s.equals("MINU") ? LexType.MINU :
                                        s.equals("NOT") ? LexType.NOT : null;
            this.op = new Op(lexType);
            this.unaryExpStmt = new UnaryExpStmt(unaryExpNode.getNodes().get(1));
        }
    }

    public int getConstValue(BlockSymbolTable fatherTable) { //好像是能直接算的才用这个
        if (type == 1) {
            return this.computeStmt.getValue(fatherTable);
        } else if (type == 2) {
            String name = this.lVal.getLValName();
            VarSymbolTable var = fatherTable.searchVar(name,fatherTable,false);
            return var.getValue();
        } else if (this.type == 3) {
            return this.number;
        } else if (this.type == 5) {
            if (op.getLexType() == LexType.PLUS) {
                return this.unaryExpStmt.getConstValue(fatherTable);
            } else if (op.getLexType() == LexType.MINU) {
                return -this.unaryExpStmt.getConstValue(fatherTable);
            } else {
                if (unaryExpStmt.getConstValue(fatherTable) == 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }

    public int getType() {
        return type;
    }

    public ComputeStmt getComputeStmt() {
        return computeStmt;
    }

    public LVal getlVal() {
        return this.lVal;
    }

    public int getNumber() {
        return this.number;
    }

    public CallExpr getCallExpr() {
        return callExpr;
    }

    public Op getOp() {
        return op;
    }

    public UnaryExpStmt getUnaryExpStmt() {
        return unaryExpStmt;
    }
}
