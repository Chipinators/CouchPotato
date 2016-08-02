package paperprisoners.couchpotato;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ian on 7/15/2016.
 */
public class PagedGameAdapter extends PagerAdapter {

    private ArrayList<View> dataList = new ArrayList<>();


    //CONSTRUCTOR BELOW

    public PagedGameAdapter(Context context) {
        super();
    }


    //CUSTOM METHODS TO MAKE IT EASIER

    public void addView(View data) {
        dataList.add(data);
        notifyDataSetChanged();
    }

    public void addView(View data, int position) {
        dataList.add(position, data);
        notifyDataSetChanged();
    }

    public View getView(int position) {
        return dataList.get(position);
    }

    public void removeView(View data) {
        dataList.remove(data);
        notifyDataSetChanged();
    }

    public void removeView(int position) {
        dataList.remove(position);
        notifyDataSetChanged();
    }

    public static View generateView(Context context, GameData data) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_game, null);
        //Fill the view
        ImageView img = (ImageView) v.findViewById(R.id.item_game_image);
        img.setImageBitmap(data.logo);
        if (data.maxPlayers <= 0)
        {
            LinearLayout playersInfo = (LinearLayout) v.findViewById(R.id.item_game_players);
            playersInfo.setVisibility(View.INVISIBLE);
        }
        else {
            LinearLayout playersInfo = (LinearLayout) v.findViewById(R.id.item_game_players);
            playersInfo.setVisibility(View.VISIBLE);
            TextView players = (TextView) v.findViewById(R.id.item_game_count);
            players.setText(data.minPlayers+"-"+data.maxPlayers);
        }
        //Then returns
        return v;
    }

    //INHERITED METHODS BELOW

    @Override
    public int getItemPosition (Object object)
    {
        int index = dataList.indexOf (object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View v = dataList.get(position);
        //Then we send it
        collection.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View)view);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }
}
