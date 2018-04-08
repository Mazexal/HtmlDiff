/**
 * Created by 10235 on 2018/3/13.
 */
public class Operation {
    public Operation(Action action, int startInOld, int endInOld, int startInNew, int endInNew)
    {
        Action = action;
        StartInOld = startInOld;
        EndInOld = endInOld;
        StartInNew = startInNew;
        EndInNew = endInNew;
    }

    public Action Action ;
    public int StartInOld ;
    public int EndInOld ;
    public int StartInNew ;
    public int EndInNew ;

    public void setAction(Action action) {
        Action = action;
    }

    public void setStartInOld(int startInOld) {
        StartInOld = startInOld;
    }

    public void setEndInOld(int endInOld) {
        EndInOld = endInOld;
    }

    public void setStartInNew(int startInNew) {
        StartInNew = startInNew;
    }

    public void setEndInNew(int endInNew) {
        EndInNew = endInNew;
    }

    public Action getAction() {

        return Action;
    }

    public int getStartInOld() {
        return StartInOld;
    }

    public int getEndInOld() {
        return EndInOld;
    }

    public int getStartInNew() {
        return StartInNew;
    }

    public int getEndInNew() {
        return EndInNew;
    }



}
