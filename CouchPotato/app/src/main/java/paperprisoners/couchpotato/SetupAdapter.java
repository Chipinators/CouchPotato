package paperprisoners.couchpotato;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Ian on 7/13/2016.
 */
public class SetupAdapter extends ArrayAdapter<UserData> {

    private boolean isHost;
    private LayoutInflater mInflater;
    private SetupDialog dialog;

    public SetupAdapter(SetupDialog dialog, int resource, boolean isHost) {
        super(dialog.getContext(), resource);
        this.isHost = isHost;
        this.dialog = dialog;
        mInflater = (LayoutInflater) dialog.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Retrieve data
        UserData item = getItem(position);
        // Use layout file to generate View
        View view = mInflater.inflate(R.layout.item_setup, null);
        // Set user name
        TextView name;
        name = (TextView) view.findViewById(R.id.item_setup_name);
        name.setText(item.username);
        name.setTypeface(TypefaceManager.get("Oswald-Bold"));
        // Set comment
        TextView device;
        device = (TextView) view.findViewById(R.id.item_setup_device);
        device.setTypeface(TypefaceManager.get("Oswald-Regular"));
        if (item.device != null)
            device.setText(item.device.getName());
        else
            device.setText( getContext().getString(R.string.na_device) );
        //Button stuff
        Button button;
        button = (Button) view.findViewById(R.id.item_setup_kick);
        button.setTypeface(TypefaceManager.get("Oswald-Regular"));
        //Toggles button
        if (isHost) {
            button.setOnClickListener(dialog);
        }
        else {
            RelativeLayout layout;
            layout = (RelativeLayout) view.findViewById(R.id.item_setup_content);
            layout.removeView(button);
        }
        return view;
    }
}
