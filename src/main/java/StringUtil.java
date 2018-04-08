import java.util.List;

public class StringUtil {

    public static String getString(List<Character> input){
        StringBuilder sb=new StringBuilder();

        for(Character c:input){
            sb.append(c);
        }

        return sb.toString();
    }


    public static String getString(String[] input){
        StringBuilder sb=new StringBuilder();

        for(String c:input){
            sb.append(c);
        }
        return sb.toString();

    }

}
