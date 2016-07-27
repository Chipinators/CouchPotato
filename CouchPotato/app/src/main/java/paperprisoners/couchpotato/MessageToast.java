package paperprisoners.couchpotato;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ian on 7/27/2016.
 */
public class MessageToast extends Toast {

    private Context context;
    private TextView message;

    public MessageToast(Context context, String message) {
        super(context);
        this.context = context;
        //Creates a view to change the Toast's look then assigns it.
        setup();
        //Then assigns the message
        setText(message);
    }

    public MessageToast(Context context, int msgResID) {
        super(context);
        this.context = context;
        //Creates a view to change the Toast's look then assigns it.
        setup();
        //Then assigns the message
        setText(msgResID);
    }

    private void setup() {
        View style = LayoutInflater.from(context).inflate(R.layout.toast_message, null);
        setDuration(Toast.LENGTH_LONG);
        setView(style);
    }

    @Override
    public void setText(CharSequence text) {
        View style = this.getView();
        TextView message = (TextView) style.findViewById(R.id.toast_message);
        message.setTypeface(TypefaceManager.get("Oswald-Light"));
        message.setText(text);
    }

    @Override
    public void setText(int resID) {
        setText(context.getString(resID));
    }
}
