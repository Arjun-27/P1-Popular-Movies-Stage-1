package mynanodegree.com.madovermovies;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MovieDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    ImageView imageDrop, imageScrim;
    Toolbar toolbar;
    TextView releaseDate, rating, synopsis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        releaseDate = (TextView) findViewById(R.id.tvDate);
        rating = (TextView) findViewById(R.id.tvRate);
        synopsis = (TextView) findViewById(R.id.textSynopsis);

        Typeface font = Typeface.createFromAsset(getAssets(), "OpenSans.ttf");
        releaseDate.setTypeface(font);
        rating.setTypeface(font);
        synopsis.setTypeface(font);

        MovieData movieData = getIntent().getParcelableExtra("MovieData");
        setTitle(movieData.getTitle());
        Log.d("M-TITLE", movieData.getOriginal_title());

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        String strDate = "";
        try {
            Date date = originalFormat.parse(movieData.getRelease_date());
            strDate = targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        releaseDate.setText(releaseDate.getText().toString() + "\n\t" + strDate);
        rating.setText(rating.getText().toString() + "\n\t" + movieData.getVote_average());
        synopsis.setText(movieData.getOverview());

        imageDrop = (ImageView) findViewById(R.id.imageDrop);
        imageScrim = (ImageView) findViewById(R.id.imageScrim);

        if(CheckNetworkConnection.isNetworkAvailable(this)) {
            Picasso.with(this).load(AppConstants.BASE_IMAGE_PATH + movieData.getBackdrop_path()).placeholder(R.color.colorAccent).into(imageDrop);
        } else {
            Snackbar.make(imageDrop, "Cannot load poster.. Check internet connection", Snackbar.LENGTH_LONG).show();
        }
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(this);

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int toolBarHeight = toolbar.getMeasuredHeight();
        int appBarHeight = appBarLayout.getMeasuredHeight();
        Float f = ((((float) appBarHeight - toolBarHeight) + verticalOffset) / ((float) appBarHeight - toolBarHeight)) * 255;
        imageScrim.getBackground().setAlpha(255 - Math.round(f));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
