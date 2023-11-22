package Frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class IoFile {
    public static StringBuilder readFileByBytes(String fileName) {
        File file = new File(fileName);
        FileInputStream in = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            in = new FileInputStream(file);
            int tmp;
            while ((tmp = in.read()) != -1) {
                // System.out.print((char)tmp);
                stringBuilder.append((char)tmp);
            }
            stringBuilder.append('\0');
            return stringBuilder;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void outputContentToFile(String s) {
        try {
            File file = new File("output.txt");
            FileWriter fileWriter = new FileWriter(file.getName());
            fileWriter.write(s);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void outputContentToFile_testTokens(String s) {
        try {
            File file = new File("tokens.txt");
            FileWriter fileWriter = new FileWriter(file.getName());
            fileWriter.write(s);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void outputContentToFile_error(String s) {
        try {
            File file = new File("error.txt");
            FileWriter fileWriter = new FileWriter(file.getName());
            fileWriter.write(s);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void outputContentToFile_llvm_ir(String s) {
        try {
            File file = new File("llvm_ir.txt");
            FileWriter fileWriter = new FileWriter(file.getName());
            fileWriter.write(s);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
