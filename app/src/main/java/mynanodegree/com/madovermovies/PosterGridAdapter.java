package mynanodegree.com.madovermovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created ba Arjun Chouhan on 29-02-2016.
 */

public class PosterGridAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<MovieData> posterData;

    public PosterGridAdapter(Activity activity, ArrayList<MovieData> posterData) {
        this.activity = activity;
        this.posterData = posterData;
    }

    @Override
    public int getCount() {
        return posterData != null ? posterData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return posterData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.grid_poster, parent, false);
        }

        ImageView poster = (ImageView) convertView;
        Picasso.with(activity)
                .load(AppConstants.BASE_IMAGE_PATH + posterData.get(position).getPoster_path())
                .placeholder(R.drawable.placeholder)
                .into(poster);
        return poster;
    }
}
