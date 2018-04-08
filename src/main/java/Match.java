import com.sun.org.apache.xpath.internal.operations.String;

/**
 * Created by 10235 on 2018/3/13.
 */
public class Match {

    public Match(int startInOld, int startInNew, int size)
    {
        StartInOld = startInOld;
        StartInNew = startInNew;
        Size = size;

        EndInOld=StartInOld + Size;
        EndInNew=StartInNew + Size;
    }

    public int StartInOld ;
    public int StartInNew;
    public int Size ;

    public int EndInOld;

    public int getEndInOld() {
        return StartInOld + Size;
    }

    public int EndInNew;

    public int getEndInNew() {
        return StartInNew + Size;
    }

}
