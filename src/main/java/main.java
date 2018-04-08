import java.io.*;
import java.util.HashMap;

/**
 * Created by 10235 on 2018/3/26.
 */
public class main {
    public static void main(String[] args) {
        String oldText=FileUtil.readAllText("test1.html");
        String newText=FileUtil.readAllText("test2.html");
        HtmlDiff diff=new HtmlDiff(oldText, newText);
       // FileUtil.writeToFile1( );
        diff.Build();
        HashMap<String,String> res=HtmlDiff.getHashMap();

        System.out.println("success");
    }


}
