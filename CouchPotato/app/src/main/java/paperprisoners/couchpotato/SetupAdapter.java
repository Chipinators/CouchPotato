package paperprisoners.couchpotato;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Ian on 7/13/2016.
 */
public class SetupAdapter extends ArrayAdapter<UserData> {

    private boolean isHost;
    private LayoutInflater mInflater;

    public SetupAdapter(Context context, int resource, boolean isHost) {
        super(context, resource);
        this.isHost = isHost;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        //Toggles button
        if (!isHost) {
            RelativeLayout layout;
            layout = (RelativeLayout) view.findViewById(R.id.item_setup_content);
            layout.removeView(view.findViewById(R.id.item_setup_kick));
        }
        return view;
    }
}
