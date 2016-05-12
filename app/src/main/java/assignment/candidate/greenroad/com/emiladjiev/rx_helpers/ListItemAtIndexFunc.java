package assignment.candidate.greenroad.com.emiladjiev.rx_helpers;

import java.util.ArrayList;

import rx.functions.Func1;

/**
 * Created by Emil on 12/05/2016.
 */
public class ListItemAtIndexFunc implements Func1<ArrayList<String>, String> {

    private final int index;

    public ListItemAtIndexFunc(int index) {
        this.index = index;
    }

    @Override
    public String call(ArrayList<String> strings) {
        return strings.get(index);
    }
}