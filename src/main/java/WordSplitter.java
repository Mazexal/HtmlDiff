import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.util.Arrays;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 10235 on 2018/3/13.
 */
public class WordSplitter {
    /// <summary>
    /// Converts Html text into a list of words
    /// </summary>
    /// <param name="text"></param>
    /// <param name="blockExpressions"></param>
    /// <returns></returns>
    public static String[] ConvertHtmlToListOfWords(String text, List<Pattern> blockExpressions)
    {
        Mode mode = Mode.Character;
        List<Character> currentWord = new ArrayList<Character>();
        List<String> words = new ArrayList<String>();

        Dictionary<Integer, Integer> blockLocations = FindBlocks(text, blockExpressions);

        boolean isBlockCheckRequired = blockLocations.size()>0;
        boolean isGrouping = false;
        int groupingUntil = -1;

        for (int index = 0; index < text.length(); index++)
        {
            char character = text.charAt(index);

            // Don't bother executing block checks if we don't have any blocks to check for!
            if (isBlockCheckRequired)
            {
                // Check if we have completed grouping a text sequence/block
                if (groupingUntil == index)
                {
                    groupingUntil = -1;
                    isGrouping = false;
                }

                // Check if we need to group the next text sequence/block
                int until = 0;
                if (blockLocations.get(index)!=null)
                {
                    isGrouping = true;
                    groupingUntil = until;
                }

                // if we are grouping, then we don't care about what type of character we have, it's going to be treated as a word
                if (isGrouping)
                {
                    currentWord.add(character);
                    mode = Mode.Character;
                    continue;
                }
            }

            switch (mode)
            {
                case Character:

                    if (Utils.IsStartOfTag(character))
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }

                        currentWord.clear();
                        currentWord.add('<');
                        mode = Mode.Tag;
                    }
                    else if (Utils.IsStartOfEntity(character))
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }

                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Entity;
                    }
                    else if (Utils.IsWhiteSpace(character))
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }
                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Whitespace;
                    }
                    else if (Utils.IsWord(character)
                            && (currentWord.size() == 0 || Utils.IsWord(currentWord.get(currentWord.size()-1))))
                    {
                        currentWord.add(character);
                    }
                    else
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }
                        currentWord.clear();
                        currentWord.add(character);
                    }

                    break;
                case Tag:

                    if (Utils.IsEndOfTag(character))
                    {
                        currentWord.add(character);
                        words.add(StringUtil.getString(currentWord));
                        currentWord.clear();

                        mode = Utils.IsWhiteSpace(character) ? Mode.Whitespace : Mode.Character;
                    }
                    else
                    {
                        currentWord.add(character);
                    }

                    break;
                case Whitespace:

                    if (Utils.IsStartOfTag(character))
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }
                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Tag;
                    }
                    else if (Utils.IsStartOfEntity(character))
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }

                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Entity;
                    }
                    else if (Utils.IsWhiteSpace(character))
                    {
                        currentWord.add(character);
                    }
                    else
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }

                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Character;
                    }

                    break;
                case Entity:
                    if (Utils.IsStartOfTag(character))
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }

                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Tag;
                    }
                    else if (Character.isWhitespace(character))
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add(StringUtil.getString(currentWord));
                        }
                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Whitespace;
                    }
                    else if (Utils.IsEndOfEntity(character))
                    {
                        boolean switchToNextMode = true;
                        if (currentWord.size() != 0)
                        {
                            currentWord.add(character);
                            if(words.size()>637){
                                int i=0;
                            }
                            words.add( StringUtil.getString(currentWord));

                            //join &nbsp; entity with last whitespace
                            if (words.size() > 2
                                    && Utils.IsWhiteSpace(words.get(words.size() - 2))
                                    && Utils.IsWhiteSpace(words.get(words.size() - 1)))
                            {
                                String w1 = words.get(words.size() - 2);
                                String w2 = words.get(words.size() - 1);
                                ArraryUtil<String> arraryUtil=new ArraryUtil<String>();
                                arraryUtil.removeRange(words.size() - 2, 2,words);
                              //  words.removeRange(words.size() - 2, 2);
                                currentWord.clear();
                                for(char tmp:w1.toCharArray()){
                                    currentWord.add(tmp);
                                }
                                for(char tmp:w2.toCharArray()){
                                    currentWord.add(tmp);
                                }
                                mode = Mode.Whitespace;
                                switchToNextMode = false;
                            }
                        }
                        if (switchToNextMode)
                        {
                            currentWord.clear();
                            mode = Mode.Character;
                        }
                    }
                    else if (Utils.IsWord(character))
                    {
                        currentWord.add(character);
                    }
                    else
                    {
                        if (currentWord.size() != 0)
                        {
                            words.add( StringUtil.getString(currentWord));
                        }
                        currentWord.clear();
                        currentWord.add(character);
                        mode = Mode.Character;
                    }
                    break;
            }
        }
        if (currentWord.size() != 0)
        {
            words.add( StringUtil.getString(currentWord));
        }

        return words.toArray(new String[0]);
    }

    /// <summary>
    /// Finds any blocks that need to be grouped
    /// </summary>
    /// <param name="text"></param>
    /// <param name="blockExpressions"></param>
    private static Dictionary<Integer, Integer> FindBlocks(String text, List<Pattern> blockExpressions)
    {
        Dictionary<Integer, Integer> blockLocations = new Hashtable<Integer, Integer>();

        if (blockExpressions == null)
        {
            return blockLocations;
        }

        for (Pattern exp : blockExpressions)
        {
            try
            {
            Matcher match = exp.matcher(text);
            while(match.find()){
                    blockLocations.put(match.start(), match.end());
                }
            }
            catch (Exception e)
            {

            }
        }
        return blockLocations;
    }



}
