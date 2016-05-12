package assignment.candidate.greenroad.com.emiladjiev.rx_helpers;

import android.widget.TextView;

import rx.functions.Action1;

public class DisplayTextOnViewAction implements Action1<String> {
    private final TextView target;

    public DisplayTextOnViewAction(TextView target) {
        this.target = target;
    }

    @Override
    public void call(String s) {
        target.setText(s);
    }
}
