package Ir.IRInstr;

public class LibFunctionIR extends Instr{
    public String genIr() {
        return "declare i32 @getint()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n";
    }
}
