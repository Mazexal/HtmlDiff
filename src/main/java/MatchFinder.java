
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 10235 on 2018/3/13.
 */
public class MatchFinder {

    private String[] _oldWords;
    private String[] _newWords;
    private int _startInOld;
    private int _endInOld;
    private int _startInNew;
    private int _endInNew;
    private HashMap<String, List<Integer>> _wordIndices;
    private MatchOptions _options;

    /// <summary>
    /// </summary>
    /// <param name="oldWords"></param>
    /// <param name="newWords"></param>
    /// <param name="startInOld"></param>
    /// <param name="endInOld"></param>
    /// <param name="startInNew"></param>
    /// <param name="endInNew"></param>
    /// <param name="options"></param>
    public MatchFinder(String[] oldWords, String[] newWords, int startInOld, int endInOld, int startInNew, int endInNew, MatchOptions options) {
        _oldWords = oldWords;
        _newWords = newWords;
        _startInOld = startInOld;
        _endInOld = endInOld;
        _startInNew = startInNew;
        _endInNew = endInNew;
        _options = options;
    }

    private void IndexNewWords() {
        _wordIndices = new HashMap<String, List<Integer>>();
        List<String> block = new ArrayList<String>(_options.BlockSize);
        for (int i = _startInNew; i < _endInNew; i++) {
            // if word is a tag, we should ignore attributes as attribute changes are not supported (yet)
            String word = NormalizeForIndex(_newWords[i]);
            String key = PutNewWord(block, word, _options.BlockSize); //TODO check

            if (key == null)
                continue;


            try{
                if (_wordIndices.get(key)!=null) {
                    List<Integer> indicies=_wordIndices.get(key);
                    indicies.add(i);
                } else {
                    List<Integer> tmp=new ArrayList<Integer>() ;
                    tmp.add(i);
                    _wordIndices.put(key, tmp);
                }
            }
            catch (Exception e){

            }

        }


    }

    private static int i=0;

    private static String PutNewWord(List<String> block, String word, int blockSize) {
        block.add(word);
        if (block.size() > blockSize){
            block.remove(0);
        }


        if (block.size() != blockSize)
            return null;

        StringBuilder result = new StringBuilder(blockSize);
        for (String s : block) {
            result.append(s);
        }
        return result.toString();
    }

    /// <summary>
    /// Converts the word to index-friendly value so it can be compared with other similar words
    /// </summary>
    /// <param name="word"></param>
    /// <returns></returns>
    private String NormalizeForIndex(String word) {
        word = Utils.StripAnyAttributes(word);
        if (_options.IgnoreWhitespaceDifferences && Utils.IsWhiteSpace(word))
            return " ";

        return word;
    }

    public static int findmatchcalled=0;

    public Match FindMatch() {
        IndexNewWords();
        RemoveRepeatingWords();
        findmatchcalled++;
        if (_wordIndices.size() == 0)
            return null;

        int bestMatchInOld = _startInOld;
        int bestMatchInNew = _startInNew;
        int bestMatchSize = 0;

        HashMap<Integer, Integer> matchLengthAt = new HashMap<Integer, Integer>();
        List<String> block = new ArrayList<String>(_options.BlockSize);

        for (int indexInOld = _startInOld; indexInOld < _endInOld; indexInOld++) {
            String word = NormalizeForIndex(_oldWords[indexInOld]);

            String index = PutNewWord(block, word, _options.BlockSize);//TODO check

            if (index == null)
                continue;

            HashMap<Integer, Integer> newMatchLengthAt = new HashMap<Integer, Integer>();

            if (!_wordIndices.containsKey(index)) {
                matchLengthAt = newMatchLengthAt;
                continue;
            }

            for( int indexInNew : _wordIndices.get(index))
            {
                int newMatchLength = (matchLengthAt.containsKey(indexInNew - 1) ? matchLengthAt.get(indexInNew - 1) : 0) +
                        1;
                newMatchLengthAt.put(indexInNew,newMatchLength)  ;
                if (newMatchLength > bestMatchSize) {
                    bestMatchInOld = indexInOld - newMatchLength + 1 - _options.BlockSize + 1;
                    bestMatchInNew = indexInNew - newMatchLength + 1 - _options.BlockSize + 1;
                    bestMatchSize = newMatchLength;
                }
            }

            matchLengthAt = newMatchLengthAt;
        }

        return bestMatchSize != 0 ? new Match(bestMatchInOld, bestMatchInNew, bestMatchSize + _options.BlockSize - 1) : null;
    }

    /// <summary>
    /// This method removes words that occur too many times. This way it reduces total count of comparison operations
    /// and as result the diff algoritm takes less time. But the side effect is that it may detect false differences of
    /// the repeating words.
    /// </summary>
    private void RemoveRepeatingWords() {
        double threshold = _newWords.length * _options.RepeatingWordsAccuracy;
        for (Map.Entry<String, List<Integer>> entry : _wordIndices.entrySet()) {
            if (entry.getValue().size() > threshold) {
                _wordIndices.remove(entry);
            }
        }

    }

}
