package mynanodegree.com.madovermovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private GridView moviePosters;
    private ArrayList<MovieData> data;
    private int page = 2, pos;
    private boolean isLoading = false, stateRestored = false;
    private String sortCriteria = "popularity.desc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviePosters = (GridView) findViewById(R.id.gridPosters);
        data = new ArrayList<>();

        if(savedInstanceState != null) {
            data = savedInstanceState.getParcelableArrayList("movieList");
            pos = savedInstanceState.getInt("posToScroll");
            sortCriteria = savedInstanceState.getString("sortBy");
            page = savedInstanceState.getInt("numPages");

            moviePosters.setAdapter(new PosterGridAdapter(MainActivity.this, data));
            moviePosters.smoothScrollToPosition(pos);
            stateRestored = true;
            moviePosters.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    pos = view.getLastVisiblePosition();
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount >= totalItemCount && !isLoading && view.getAdapter().getCount() != 0) {
                        // Till end of this view has been scrolled
                        if (CheckNetworkConnection.isNetworkAvailable(MainActivity.this)) {
                            new DiscoverMovies().execute(AppConstants.BASE_PATH_DISCOVER + "&sort_by=" + sortCriteria + "&page=" + page++);
                        } else {
                            Snackbar.make(moviePosters, "Cannot load more movies...", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            });
        } else {
            if (CheckNetworkConnection.isNetworkAvailable(this)) {
                new DiscoverMovies().execute(AppConstants.BASE_PATH_DISCOVER + "&sort_by=" + sortCriteria);
            } else {
                Snackbar.make(moviePosters, "No Internet Connection", Snackbar.LENGTH_LONG).show();
            }
        }

        moviePosters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(MainActivity.this, MovieDetailsActivity.class).putExtra("MovieData", data.get(position)));
            }
        });
    }

    private class DiscoverMovies extends AsyncTask<String, Void, ArrayList<MovieData>> {
        @Override
        protected ArrayList<MovieData> doInBackground(String... params) {
            try {
                isLoading = true;
                InputStream is = new URL(params[0]).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                int cp;

                while ((cp = rd.read()) != -1) {
                    sb.append((char) cp);
                }
                is.close();

                JSONObject object = new JSONObject(sb.toString());
                JSONArray array = object.getJSONArray("results");
                ArrayList<MovieData> list = new ArrayList<>();

                for(int i = 0; i < array.length(); i++) {
                    MovieData movieData = new MovieData();
                    JSONObject data = (JSONObject) array.get(i);

                    movieData.setPoster_path(data.getString("poster_path"));
                    movieData.setAdult(data.getString("adult"));
                    movieData.setOverview(data.getString("overview"));
                    movieData.setRelease_date(data.getString("release_date"));
                    movieData.setGenre_ids(data.getString("genre_ids"));
                    movieData.setId(data.getString("id"));
                    movieData.setOriginal_title(data.getString("original_title"));
                    movieData.setOriginal_language(data.getString("original_language"));
                    movieData.setTitle(data.getString("title"));
                    movieData.setBackdrop_path(data.getString("backdrop_path"));
                    movieData.setPopularity(data.getString("popularity"));
                    movieData.setVote_count(data.getString("vote_count"));
                    movieData.setVideo(data.getString("video"));
                    movieData.setVote_average(data.getString("vote_average"));

                    list.add(movieData);
                }
                return list;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(ArrayList<MovieData> result) {
            if(moviePosters.getAdapter() == null) {
                data = result;
                moviePosters.setAdapter(new PosterGridAdapter(MainActivity.this, data));
            } else {
                data.addAll(result);
                ((BaseAdapter) moviePosters.getAdapter()).notifyDataSetChanged();
            }
            isLoading = false;
            deleteCache(MainActivity.this);

            if(!stateRestored) {
                moviePosters.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        pos = view.getLastVisiblePosition();
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (firstVisibleItem + visibleItemCount >= totalItemCount && !isLoading && view.getAdapter().getCount() != 0) {
                            // Till end of this view has been scrolled
                            if (CheckNetworkConnection.isNetworkAvailable(MainActivity.this)) {
                                new DiscoverMovies().execute(AppConstants.BASE_PATH_DISCOVER + "&sort_by=" + sortCriteria + "&page=" + page++);
                            } else {
                                Snackbar.make(moviePosters, "Cannot load more movies...", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        }
    }

    public static void deleteCache(Context context) {
        Log.d("CACHE", "DELETING...");
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        try {
            Log.d("POS", "" + pos);
            savedInstanceState.putString("sortBy", sortCriteria);
            savedInstanceState.putInt("posToScroll", pos);
            savedInstanceState.putParcelableArrayList("movieList", data);
            savedInstanceState.putInt("numPages", page);

            super.onSaveInstanceState(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(data != null && CheckNetworkConnection.isNetworkAvailable(MainActivity.this)) {
            data.clear();
            page = 1;
            switch (menuItem.getItemId()) {
                case R.id.most_popular:
                    sortCriteria = "popularity.desc";
                    break;

                case R.id.highest_rated:
                    sortCriteria = "vote_average.desc";
                    break;
            }
            new DiscoverMovies().execute(AppConstants.BASE_PATH_DISCOVER + "&sort_by=" + sortCriteria + "&page=" + page++);
            deleteCache(MainActivity.this);
            if(moviePosters.getAdapter() != null)
                ((BaseAdapter) moviePosters.getAdapter()).notifyDataSetChanged();

            return true;
        }
        return false;
    }
}