package ua.com.vdranik.hw_week_4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResourceType")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public final static String BROADCAST_ACTION = "ua.com.vdranik.hw_week_4";
    public final static String PARAM_TASK = Task.class.getSimpleName();
    public final static String PHOTO_URI = "photo_uri";
    private BroadcastReceiver broadcastReceiver;
    private Button nextButton;
    private Button backButton;
    private Button slideShowButton;
    private boolean isStartedSlideShow;
    private ImageView imageView;
    private SlideShowTask slideShowTask;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextButton = (Button) findViewById(R.id.nextButton);
        backButton = (Button) findViewById(R.id.backBatton);
        slideShowButton = (Button) findViewById(R.id.slideshowButton);
        imageView = (ImageView) findViewById(R.id.imageView);

        nextButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        slideShowButton.setOnClickListener(this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                uri = intent.getParcelableExtra(PHOTO_URI);
                imageView.setImageURI(uri);
            }
        };

        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        slideShowTask = (SlideShowTask) getLastCustomNonConfigurationInstance() ;
        if(slideShowTask == null){
            slideShowTask = new SlideShowTask();
        }

        slideShowTask.link(this);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        slideShowTask.unLink();
        return slideShowTask;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.nextButton:
                intent = new Intent(this, SlideService.class).putExtra(PARAM_TASK, Task.NEXT);
                startService(intent);
                break;
            case R.id.backBatton:
                intent = new Intent(this, SlideService.class).putExtra(PARAM_TASK, Task.BACK);
                startService(intent);
                break;
            case R.id.slideshowButton:
                if (!isStartedSlideShow) {
                    slideShowButton.setText("Stop slideshow");
                    isStartedSlideShow = true;

                    intent = new Intent(this, SlideService.class).putExtra(PARAM_TASK, Task.NEXT);

                    if(slideShowTask.isDone()){
                        slideShowTask = new SlideShowTask();
                        slideShowTask.link(this);
                    }
                    slideShowTask.execute(intent);

                    nextButton.setVisibility(View.GONE);
                    backButton.setVisibility(View.GONE);
                } else {
                    slideShowButton.setText("Start slideshow");
                    isStartedSlideShow = false;

                    if(slideShowTask != null ) {
                        slideShowTask.quit();
                    }
                    nextButton.setVisibility(View.VISIBLE);
                    backButton.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int nextButtonVisibility = nextButton.getVisibility();
        int backButtonVisibility = backButton.getVisibility();
        String slideshowButtonName = (String) slideShowButton.getText();
        outState.putBoolean("isStartedSlideShow", isStartedSlideShow);
        outState.putInt("nextButtonVisibility", nextButtonVisibility);
        outState.putInt("backButtonVisibility", backButtonVisibility);
        outState.putString("slideshowButtonName", slideshowButtonName);
        if(uri!=null) outState.putString("uri", uri.toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String stringUri = savedInstanceState.getString("uri");
        if(stringUri != null) uri = Uri.parse(stringUri);
        isStartedSlideShow = savedInstanceState.getBoolean("isStartedSlideShow");
        nextButton.setVisibility(savedInstanceState.getInt("nextButtonVisibility"));
        backButton.setVisibility(savedInstanceState.getInt("backButtonVisibility"));
        slideShowButton.setText(savedInstanceState.getString("slideshowButtonName"));
        imageView.setImageURI(uri);
    }
}

class SlideShowTask extends AsyncTask<Intent, Void, Void> {
    private MainActivity activity;

    public void link(MainActivity act) {
        activity = act;
    }

    public void unLink() {
        activity = null;
    }

    private boolean done = false;

    @Override
    protected Void doInBackground(Intent... params) {
        Intent intent = params[0];

            while (!done) {
                try {
                    activity.startService(intent);
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        return null;
    }

    public void quit() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }
}