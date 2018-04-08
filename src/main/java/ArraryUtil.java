import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10235 on 2018/3/24.
 */
public class ArraryUtil<T> {

    public  List<T>  removeRange(int index,int count,List<T> input){
        if(index+count<=input.size()){
            for(int i=0;i<count;i++){
                input.remove(index);
            }
        }
        return input;
    }




}
