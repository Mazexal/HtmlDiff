import com.sun.deploy.util.StringUtils;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by 10235 on 2018/3/13.
 */
public class HtmlDiff {

    /// <summary>
    /// This value defines balance between speed and memory utilization. The higher it is the faster it works and more memory consumes.
    /// </summary>
    private final int MatchGranularityMaximum = 4;

    private StringBuilder _content;
    private String _newText;
    private String _oldText;


    private static HashMap<String, Integer> _specialCaseClosingTags = new HashMap<String, Integer>();

    private static final Pattern _specialCaseOpeningTagRegex = Pattern.compile(
            "<((strong)|(b)|(i)|(em)|(big)|(small)|(u)|(sub)|(sup)|(strike)|(s))[\\>\\s]+");

    /// <summary>
    /// Tracks opening and closing formatting tags to ensure that we don't inadvertently generate invalid html during the diff process.
    /// </summary>
    private Stack<String> _specialTagDiffStack;

    private String[] _newWords;
    private String[] _oldWords;
    private int _matchGranularity;
    private List<Pattern> _blockExpressions;

    /// <summary>
    /// Defines how to compare repeating words. Valid values are from 0 to 1.
    /// This value allows to exclude some words from comparison that eventually
    /// reduces the total time of the diff algorithm.
    /// 0 means that all words are excluded so the diff will not find any matching words at all.
    /// 1 (default value) means that all words participate in comparison so this is the most accurate case.
    /// 0.5 means that any word that occurs more than 50% times may be excluded from comparison. This doesn't
    /// mean that such words will definitely be excluded but only gives a permission to exclude them if necessary.
    /// </summary>
    public double RepeatingWordsAccuracy;

    /// <summary>
    /// If true all whitespaces are considered as equal
    /// </summary>
    public boolean IgnoreWhitespaceDifferences;

    /// <summary>
    /// If some match is too small and located far from its neighbors then it is considered as orphan
    /// and removed. For example:
    /// <code>
    /// aaaaa bb ccccccccc dddddd ee
    /// 11111 bb 222222222 dddddd ee
    /// </code>
    /// will find two matches <code>bb</code> and <code>dddddd ee</code> but the first will be considered
    /// as orphan and ignored, as result it will consider texts <code>aaaaa bb ccccccccc</code> and
    /// <code>11111 bb 222222222</code> as single replacement:
    /// <code>
    /// &lt;del&gt;aaaaa bb ccccccccc&lt;/del&gt;&lt;ins&gt;11111 bb 222222222&lt;/ins&gt; dddddd ee
    /// </code>
    /// This property defines relative size of the match to be considered as orphan, from 0 to 1.
    /// 1 means that all matches will be considered as orphans.
    /// 0 (default) means that no match will be considered as orphan.
    /// 0.2 means that if match length is less than 20% of distance between its neighbors it is considered as orphan.
    /// </summary>
    public double OrphanMatchThreshold;


    /// <summary>
    ///     Initializes a new instance of the class.
    /// </summary>
    /// <param name="oldText">The old text.</param>
    /// <param name="newText">The new text.</param>
    public HtmlDiff(String oldText, String newText) {
        RepeatingWordsAccuracy = 1d; //by default all repeating words should be compared

        _oldText = oldText;
        _newText = newText;

        _content = new StringBuilder();
        _specialTagDiffStack = new Stack<String>();
        _blockExpressions = new ArrayList<Pattern>();
    }


    public static String Execute(String oldText, String newText) {
        return new HtmlDiff(oldText, newText).Build();
    }


    /// <summary>
    /// Builds the HTML diff output
    /// </summary>
    /// <returns>HTML diff markup</returns>
    public String Build() {
        // If there is no difference, don't bother checking for differences
        if (_oldText == _newText) {
            return _newText;
        }

        SplitInputsToWords();

        _matchGranularity = Math.min(MatchGranularityMaximum, Math.min(_oldWords.length, _newWords.length));

        List<Operation> operations = Operations();

        for (Operation item : operations) {
            PerformOperation(item);
        }

        return _content.toString();
    }


    /// <summary>
    /// Uses <paramref name="expression"/> to group text together so that any change detected within the group is treated as a single block
    /// </summary>
    /// <param name="expression"></param>
    public void AddBlockExpression(Pattern expression) {
        _blockExpressions.add(expression);
    }

    private void SplitInputsToWords() {
        _oldWords = WordSplitter.ConvertHtmlToListOfWords(_oldText, _blockExpressions);

        //free memory, allow it for GC
        _oldText = null;

        _newWords = WordSplitter.ConvertHtmlToListOfWords(_newText, _blockExpressions);

        //free memory, allow it for GC
        _newText = null;
    }


    private void PerformOperation(Operation operation) {

        switch (operation.Action) {
            case Equal:
                ProcessEqualOperation(operation);
                break;
            case Delete:
                ProcessDeleteOperation(operation, "diffdel");
                break;
            case Insert:
                ProcessInsertOperation(operation, "diffins");
                break;
            case None:
                break;
            case Replace:
                ProcessReplaceOperation(operation);
                break;
        }
    }


    private void ProcessReplaceOperation(Operation operation) {
        ProcessDeleteOperation(operation, "diffmod");
        ProcessInsertOperation(operation, "diffmod");
    }


    public static List<String> oldString=new ArrayList<String>();
    public static List<String> newString=new ArrayList<String>();

    //zxl返回最终结果
    public static HashMap<String, String> getHashMap() {
        HashMap<String, String> res=new HashMap<String, String>();
        for(int i=0;i<newString.size();i++){
            res.put(newString.get(i).trim(),oldString.get(i));
        }
        return res;
    }

    private void ProcessInsertOperation(Operation operation, String cssClass) {

        List<String> text = new ArrayList<String>();
        for (int i = operation.StartInNew; i < operation.EndInNew; i++) {
            text.add(_newWords[i]);
        }
        newString.add(StringUtil.getString(text.toArray(new String[0])));
        InsertTag("ins", cssClass, text);
    }


    private void ProcessDeleteOperation(Operation operation, String cssClass) {
        List<String> text = new ArrayList<String>();
        for (int i = operation.StartInOld; i < operation.EndInOld; i++) {
            text.add(_oldWords[i]);
        }
        oldString.add(StringUtil.getString(text.toArray(new String[0])));
        InsertTag("del", cssClass, text);
    }

    private void ProcessEqualOperation(Operation operation) {
        List<String> result = new ArrayList<String>();
        for (int i = operation.StartInNew; i < operation.EndInNew; i++) {
            result.add(_newWords[i]);
        }

        _content.append(StringUtils.join(result, ""));
    }


    /// <summary>
    ///     This method encloses words within a specified tag (ins or del), and adds this into "content",
    ///     with a twist: if there are words contain tags, it actually creates multiple ins or del,
    ///     so that they don't include any ins or del. This handles cases like
    ///     old: '<p>a</p>'
    ///     new: '<p>ab</p>
    ///     <p>
    ///         c</b>'
    ///         diff result: '<p>a<ins>b</ins></p>
    ///         <p>
    ///             <ins>c</ins>
    ///         </p>
    ///         '
    ///         this still doesn't guarantee valid HTML (hint: think about diffing a text containing ins or
    ///         del tags), but handles correctly more cases than the earlier version.
    ///         P.S.: Spare a thought for people who write HTML browsers. They live in this ... every day.
    /// </summary>
    /// <param name="tag"></param>
    /// <param name="cssClass"></param>
    /// <param name="words"></param>




    private void InsertTag(String tag, String cssClass, List<String> words) {
        System.out.println(StringUtil.getString(words.toArray(new String[0])));
        System.out.println(tag +"=====================");
        while (true) {
            if (words.size() == 0) {
                break;
            }

            List<String> nonTags = ExtractConsecutiveWords(words,0);

            String specialCaseTagInjection = "";
            boolean specialCaseTagInjectionIsBefore = false;

            if (nonTags.size() != 0) {
                String text = Utils.WrapText(StringUtils.join(nonTags, ""), tag, cssClass);

                _content.append(text);
            } else {
                // Check if the tag is a special case
                if (_specialCaseOpeningTagRegex.matcher(words.get(0)).matches()) {
                    _specialTagDiffStack.push(words.get(0));
                    specialCaseTagInjection = "<ins class='mod'>";
                    if (tag == "del") {
                        words.remove(0);

                        // following tags may be formatting tags as well, follow through
                        while (words.size() > 0 && _specialCaseOpeningTagRegex.matcher(words.get(0)).matches()) {
                            words.remove(0);
                        }
                    }
                } else if (_specialCaseClosingTags.containsKey(words.get(0))) {
                    String openingTag = _specialTagDiffStack.size() == 0 ? null : _specialTagDiffStack.pop();

                    // If we didn't have an opening tag, and we don't have a match with the previous tag used
                    if (openingTag == null || openingTag != words.get(words.size() - 1).replace("/", "")) {
                        // do nothing
                    } else {
                        specialCaseTagInjection = "</ins>";
                        specialCaseTagInjectionIsBefore = true;
                    }

                    if (tag == "del") {
                        words.remove(0);

                        // following tags may be formatting tags as well, follow through
                        while (words.size() > 0 && _specialCaseClosingTags.containsKey(words.get(0))) {
                            words.remove(0);
                        }
                    }
                }
            }

            if (words.size() == 0 && specialCaseTagInjection.length() == 0) {
                break;
            }

            if (specialCaseTagInjectionIsBefore) {
                _content.append(specialCaseTagInjection + StringUtils.join(ExtractConsecutiveWords(words,1), ""));
            } else {
                List<String> arr=  ExtractConsecutiveWords(words,1);
                arr.add(specialCaseTagInjection);
                _content.append(StringUtils.join(arr, ""));
            }
        }
    }

    private List<String> ExtractConsecutiveWords(List<String> words,int type) {
        Integer indexOfFirstTag = null;

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            if (i == 0 && word == " ") {
                words.set(i, "&nbsp;");
            }

            if(type==1){
                if (!Utils.IsTag(word)) {
                    indexOfFirstTag = i;
                    break;
                }
            }else{
                if (Utils.IsTag(word)) {
                    indexOfFirstTag = i;
                    break;
                }
            }

        }

        if (indexOfFirstTag != null) {
            List<String> items = new ArrayList<String>();
            for (int i = 0; i < indexOfFirstTag; i++) {
                items.add(words.get(i));
            }
            if (indexOfFirstTag > 0) {
                ArraryUtil<String> arraryUtil = new ArraryUtil<String>();
                arraryUtil.removeRange(0, indexOfFirstTag, words);
            }
            return items;
        } else {
            List<String> items = new ArrayList<String>();
            for (int i = 0; i < words.size(); i++) {
                items.add(words.get(i));
            }
            ArraryUtil<String> arraryUtil = new ArraryUtil<String>();
            arraryUtil.removeRange(0, words.size(), words);
            return items;
        }
    }

    private List<Operation> Operations() {
        int positionInOld = 0, positionInNew = 0;
        List<Operation> operations = new ArrayList<Operation>();

        List<Match> matches = MatchingBlocks();

        matches.add(new Match(_oldWords.length, _newWords.length, 0));

        //Remove orphans from matches.
        //If distance between left and right matches is 4 times longer than length of current match then it is considered as orphan
        List<Match> mathesWithoutOrphans = RemoveOrphans(matches);

        for (Match match : mathesWithoutOrphans) {
            boolean matchStartsAtCurrentPositionInOld = (positionInOld == match.StartInOld);
            boolean matchStartsAtCurrentPositionInNew = (positionInNew == match.StartInNew);

            Action action;

            if (matchStartsAtCurrentPositionInOld == false
                    && matchStartsAtCurrentPositionInNew == false) {
                action = Action.Replace;
            } else if (matchStartsAtCurrentPositionInOld
                    && matchStartsAtCurrentPositionInNew == false) {
                action = Action.Insert;
            } else if (matchStartsAtCurrentPositionInOld == false) {
                action = Action.Delete;
            } else // This occurs if the first few words are the same in both versions
            {
                action = Action.None;
            }

            if (action != Action.None) {
                operations.add(
                        new Operation(action,
                                positionInOld,
                                match.StartInOld,
                                positionInNew,
                                match.StartInNew));
            }

            if (match.Size != 0) {
                operations.add(new Operation(
                        Action.Equal,
                        match.StartInOld,
                        match.EndInOld,
                        match.StartInNew,
                        match.EndInNew));
            }

            positionInOld = match.EndInOld;
            positionInNew = match.EndInNew;
        }

        return operations;
    }

    private List<Match> RemoveOrphans(List<Match> matches) {
        Match prev = null;
        Match curr = null;
        List<Match> tmp = new ArrayList<Match>();
        for (Match next : matches) {
            if (curr == null) {
                prev = new Match(0, 0, 0);
                curr = next;
                continue;
            }


            if (prev.EndInOld == curr.StartInOld && prev.EndInNew == curr.StartInNew
                    || curr.EndInOld == next.StartInOld && curr.EndInNew == next.StartInNew)
            //if match has no diff on the left or on the right
            {
                tmp.add(curr);
                prev = curr;
                curr = next;
                continue;
            }

//            int oldDistanceInChars = Enumerable.Range(prev.EndInOld, next.StartInOld - prev.EndInOld)
//                    .Sum(i => _oldWords[i].length);
            int oldDistanceInChars = 0;
            int temp = prev.EndInOld;
            for (int i = 0; i <= next.StartInOld - prev.EndInOld; i++) {
                oldDistanceInChars = oldDistanceInChars + _oldWords[temp].length();
                temp++;
            }

//            int newDistanceInChars = Enumerable.Range(prev.EndInNew, next.StartInNew - prev.EndInNew)
//                    .Sum(i => _newWords[i].length);
            int newDistanceInChars = 0;
            int temp2 = prev.EndInNew;
            for (int i = 0; i <= next.StartInNew - prev.EndInNew; i++) {
                newDistanceInChars = newDistanceInChars + _newWords[temp2].length();
                temp++;
            }
            int currMatchLengthInChars = 0;
            int temp3=curr.StartInNew;
            for (int i = 0; i <= curr.EndInNew - curr.StartInNew; i++) {
                currMatchLengthInChars = currMatchLengthInChars + _newWords[temp3].length();
                temp++;
            }
            if (currMatchLengthInChars > Math.max(oldDistanceInChars, newDistanceInChars) * OrphanMatchThreshold) {
                tmp.add(curr);
            }
            prev = curr;
            curr = next;
        }

        tmp.add(curr);
        return tmp;
    }

    private List<Match> MatchingBlocks() {
        List<Match> matchingBlocks = new ArrayList<Match>();
        FindMatchingBlocks(0, _oldWords.length, 0, _newWords.length, matchingBlocks);
        return matchingBlocks;
    }


    private void FindMatchingBlocks(
            int startInOld,
            int endInOld,
            int startInNew,
            int endInNew,
            List<Match> matchingBlocks) {
        Match match = FindMatch(startInOld, endInOld, startInNew, endInNew);

        if (match != null) {
            if (startInOld < match.StartInOld && startInNew < match.StartInNew) {
                FindMatchingBlocks(startInOld, match.StartInOld, startInNew, match.StartInNew, matchingBlocks);
            }

            matchingBlocks.add(match);

            if (match.EndInOld < endInOld && match.EndInNew < endInNew) {
                FindMatchingBlocks(match.EndInOld, endInOld, match.EndInNew, endInNew, matchingBlocks);
            }
        }
    }

    private Match FindMatch(int startInOld, int endInOld, int startInNew, int endInNew) {
        // For large texts it is more likely that there is a Match of size bigger than maximum granularity.
        // If not then go down and try to find it with smaller granularity.
        for (int i = _matchGranularity; i > 0; i--) {
            MatchOptions options = new MatchOptions();

            options.BlockSize = i;
            options.RepeatingWordsAccuracy = RepeatingWordsAccuracy;
            options.IgnoreWhitespaceDifferences = IgnoreWhitespaceDifferences;

            MatchFinder finder = new MatchFinder(_oldWords, _newWords, startInOld, endInOld, startInNew, endInNew, options);
            Match match = finder.FindMatch();
            if (match != null)
                return match;
        }
        return null;
    }
}
