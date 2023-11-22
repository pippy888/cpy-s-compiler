package Frontend;

import java.util.ArrayList;
import java.util.Collections;

public class ExceptionController {

    private ArrayList<MyException> myExceptions;

    public ExceptionController() {
        myExceptions = new ArrayList<>();
    }

    public void addException(MyException myException) {
        myExceptions.add(myException);
    }

    public ArrayList<MyException> sortArray() {
        Collections.sort(this.myExceptions,MyException::compareTo);
        return this.myExceptions;
    }
}
