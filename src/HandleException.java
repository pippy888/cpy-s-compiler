import SymbolTablePackage.SymbolTable;

public class HandleException {

    public static MyException makeStrFormatErr(Token tokenOfFormatStr) {
        String errorInfo = "format str invalid code error in file " + tokenOfFormatStr.getLineNum() + "\n";
        return new MyException(tokenOfFormatStr.getLineNum(),"a",errorInfo);
    }
    public static MyException makeRedeclarationException(SymbolTable sym, int preLine) {
        String errorInfo = "The var or function define in file " + sym.getLine() + " "
                + sym.getName() + " had declared. The previous declaration is in file " + preLine + "\n";
        return new MyException(sym.getLine(), "b", errorInfo);
    }

    public static MyException makeNoFoundNameException(String name, int line) {
        String errorInfo = name + " in file " + line + " did not be recorded.\n";
        return new MyException(line,"c",errorInfo);
    }
    public static MyException makeFuncParasCountNoMatch(Token tokenOfFunc) {
        String errorInfo = "The number of function's real parameters doesn't match the define in file " + tokenOfFunc.getLineNum() + "\n";
        return new MyException(tokenOfFunc.getLineNum(), "d", errorInfo);
    }

    public static MyException makeFuncParasTypeNoMatch(Token tokenOfFunc) {
        String errorInfo = "The function real parameter doesn't match the define in file " + tokenOfFunc.getLineNum() + "\n";
        return new MyException(tokenOfFunc.getLineNum(),"e",errorInfo);
    }
    //void函数返回非null
    public static MyException makeReturnValueException(Token tokenOfReturn) {
        String errorInfo = "This function should not return value but it did in file " + tokenOfReturn.getLineNum() + "\n";
        return new MyException(tokenOfReturn.getLineNum(),"f",errorInfo);
    }

    //int函数返回null
    public static MyException makeNoReturnException(Token tokenOfRbrack) {
        String errorInfo = "This function should return some values but it return null in file" + tokenOfRbrack.getLineNum() +"\n";
        return new MyException(tokenOfRbrack.getLineNum(),"g",errorInfo);
    }


    public static MyException makeReviseConstException(Token tokeOfLVal) {
        String errorInfo = tokeOfLVal.getNodeName() + " is a const var, but in file " + tokeOfLVal.getLineNum() + " it was revised\n";
        return new MyException(tokeOfLVal.getLineNum(),"h",errorInfo);
    }

    public static MyException makePrintfParasNumNotMatch(Token tokenOfPrintf) {
        String errorInfo = "number of printf parameters doesn't match exp in file " + tokenOfPrintf.getLineNum() + "\n";
        return new MyException(tokenOfPrintf.getLineNum(),"l",errorInfo);
    }

    public static MyException makeNotFunBlockHasContinueOrBreak(Token continueOrBreak) {
        String errorInfo = "not for block should not have stmt continue or break, but it exist in file " + continueOrBreak.getLineNum() + "\n";
        return new MyException(continueOrBreak.getLineNum(),"m",errorInfo);
    }



}
