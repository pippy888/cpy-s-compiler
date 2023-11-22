package Frontend;

public class MyException implements Comparable<MyException> {
    private int line;

    private String errorType;

    private String errorInfo;

    public MyException(int line,String errorType, String errorInfo) {
        this.errorType = errorType;
        this.line = line;
        this.errorInfo = errorInfo;
    }

    public int getLine() {
        return line;
    }

    @Override
    public int compareTo(MyException o) {
        return this.line - o.getLine();
    }

    public String getErrorType() {
        return errorType;
    }
}
