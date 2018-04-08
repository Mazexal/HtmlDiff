import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 10235 on 2018/3/13.
 */
public class Utils {

    private static Pattern openingTagRegex =  Pattern.compile("(^\\s*<[^>]+>\\s*$)");
    private static Pattern closingTagTexRegex =  Pattern.compile("(^\\s*</[^>]+>\\s*$)");
    private static Pattern tagWordRegex =  Pattern.compile("(<[^\\s>]+)");
    private static Pattern whitespaceRegex =  Pattern.compile("^(\\s|&nbsp;)+$");
    private static Pattern wordRegex =  Pattern.compile("([\\w#@]+)");

    private static  String[] SpecialCaseWordTags = { "<img" };

    public static boolean IsTag(String item)
    {
        if (item != null && item.startsWith(SpecialCaseWordTags[0])){
            return false;
        }
        return IsOpeningTag(item) || IsClosingTag(item);
    }

    public static int i=0;
    private static boolean IsOpeningTag(String item)
    {
        i++;
        try{
            return openingTagRegex.matcher(item).find();
        }catch (Exception e){
            System.out.println(i+":"+item);
            return false;
        }

    }

    private static boolean IsClosingTag(String item)
    {
        return closingTagTexRegex.matcher(item).find();
    }

    public static String StripTagAttributes(String word)
    {
        Matcher matcher=tagWordRegex.matcher(word);
        if(matcher.find()){
            String tag = matcher.group(1);
            word = tag + (word.endsWith("/>") ? "/>" : ">");
        }
        return word;
    }

    public static String WrapText(String text, String tagName, String cssClass)
    {
        return "<"+tagName+" class='"+cssClass+"'>"+text+"</"+tagName+">";
    }

    public static boolean IsStartOfTag(char val)
    {
        return val == '<';
    }

    public static boolean IsEndOfTag(char val)
    {
        return val == '>';
    }

    public static boolean IsStartOfEntity(char val)
    {
        return val == '&';
    }

    public static boolean IsEndOfEntity(char val)
    {
        return val == ';';
    }

    public static boolean IsWhiteSpace(String value)
    {
        return whitespaceRegex.matcher(value).find();
    }

    public static boolean IsWhiteSpace(char value)
    {
        return Character.isWhitespace(value);
    }

    public static String StripAnyAttributes(String word)
    {
        if (IsTag(word))
        {
            return StripTagAttributes(word);
        }
        return word;
    }

    public static boolean IsWord(char text)
    {
        return wordRegex.matcher(new Character(text).toString()).find();
    }
}
