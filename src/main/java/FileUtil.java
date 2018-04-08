import java.io.*;

public class FileUtil {


    public static String readAllText(String fileName) {
        String encoding = "utf-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }

    }

    public static void writeToFile1(String input) {

        try {
            String content = input;
            File file = new File("res.html");
            if (!file.exists()) {
                file.createNewFile();
            }
            if (file.exists()) {
                FileWriter fw = new FileWriter(file, false);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
                fw.close();
                System.out.println("test1 done!");
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    public static void appendToFile(String str) {
        String filename = "debug.txt";
        FileOutputStream stream;
        OutputStreamWriter writer;
        try {
            stream = new FileOutputStream(filename, true);
            writer = new OutputStreamWriter(stream);
            writer.write(str);
            writer.close();
            stream.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

}
