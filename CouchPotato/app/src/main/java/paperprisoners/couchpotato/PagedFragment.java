package paperprisoners.couchpotato;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PagedFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private int selected = 0;
    private boolean hidden = false;
    private ViewPager pager;
    private PagedGameAdapter adapter;
    private RelativeLayout wrapper;
    private LinearLayout indicator;
    private ArrayList<ImageView> dots = new ArrayList<>(3);
    private ViewPager.OnPageChangeListener listener;

    private static final float PADDING_MODIFIER = 0.667f;

    //CONSTRUCTOR BELOW

    public PagedFragment() {
    }


    //INHERITED METHODS BELOW

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pager, container, false);
        //Gets elements in fragment
        pager = (ViewPager) v.findViewById(R.id.pager_view);
        wrapper = (RelativeLayout) v.findViewById(R.id.pager_wrapper);
        indicator = (LinearLayout) v.findViewById(R.id.pager_indicator);
        adapter = new PagedGameAdapter(getContext());
        //Applies important stuff
        pager.addOnPageChangeListener(this);
        pager.setAdapter(adapter);
        return v;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (listener != null)
            listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        selected = position;
        adjustIndicator();
        if (listener != null)
            listener.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (listener != null)
            listener.onPageScrollStateChanged(state);
    }


    //CUSTOM METHODS BELOW

    public void addPage(View v) {
        adapter.addView(v);
        addIndicator();
    }

    public void removePage(int index) {
        if (index == selected)
            gotoPage(index, true);
        adapter.removeView(index);
        removeIndicator(index);
    }

    public void setListener(ViewPager.OnPageChangeListener listener){
        this.listener = listener;
    }

    private void addIndicator() {
        ImageView dot = new ImageView(getContext());
        int size = (int) (getResources().getDimension(R.dimen.dots) * getResources().getDisplayMetrics().density);
        dot.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        dot.getAdjustViewBounds();
        dot.setMaxWidth(size);
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pagination_white_48dp);
        dot.setImageBitmap(img);
        dot.setColorFilter(getResources().getColor(R.color.main_black, null));
        dots.add(dot);
        resetIndicator();
    }

    private void removeIndicator(int index) {
        if (index >= 0 && index < indicator.getChildCount()) {
            dots.remove(index);
            if (selected >= dots.size())
                selected = dots.size() - 1;
            gotoPage(selected, false);
            resetIndicator();
        }
    }

    private void resetIndicator() {
        indicator.removeAllViews();
        indicator.invalidate();
        for (ImageView d : dots)
            indicator.addView(d);
        adjustIndicator();
        indicator.invalidate();
    }

    private void adjustIndicator() {
        int p = (int) (getResources().getDimension(R.dimen.padding_xsmall) * getResources().getDisplayMetrics().density);
        //p *= PADDING_MODIFIER;
        if (getPageCount() > 1) {
            if (hidden)
                unhideIndicator();
            for (int i = 0; i < dots.size(); i++) {
                ImageView dot = dots.get(i);
                //Selected dot
                if (i == selected) {
                    dot.setAlpha(1f);
                    dot.setPadding(0, 0, 0, 0);
                }
                //Unselected dot
                else {
                    dot.setAlpha(0.6f);
                    dot.setPadding(p, p, p, p);
                }
            }
            indicator.invalidate();
        } else
            hideIndicator();
    }

    private void hideIndicator() {
        if (!hidden) {
            wrapper.removeView(indicator);
            wrapper.setVisibility(View.INVISIBLE);
        }
        hidden = true;
    }

    private void unhideIndicator() {
        if (hidden) {
            wrapper.addView(indicator);
            wrapper.setVisibility(View.VISIBLE);
        }
        hidden = false;
    }

    public void gotoPage(int index, boolean scroll) {
        if (index >= 0 && index < getPageCount()) {
            pager.setCurrentItem(index, scroll);
            selected = index;
            adjustIndicator();
        }
    }

    public int getPageIndex() {
        return selected;
    }

    public int getPageCount() {
        return indicator.getChildCount();
    }
}
