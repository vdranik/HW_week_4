package ua.com.vdranik.hw_week_4;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlideService extends Service {

    private LinkedList<Uri> slideURIs;
    private ListIterator<Uri> slideIterator;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        slideURIs = new LinkedList<>();
        slidesInit(slideURIs);
        slideIterator = slideURIs.listIterator();
        executorService = Executors.newFixedThreadPool(1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Task task = (Task) intent.getSerializableExtra(MainActivity.PARAM_TASK);

        RunTask runTask = new RunTask(startId, task);
        executorService.execute(runTask);

        return super.onStartCommand(intent, flags, startId);
    }

    class RunTask implements Runnable {
        Task task;
        int startId;

        public RunTask(int startId, Task task) {
            this.task = task;
            this.startId = startId;
        }

        public void run() {
            Intent intent = new Intent(MainActivity.BROADCAST_ACTION);

            Uri uri = null;
            switch (task){
                case NEXT:
                    uri = getNextSlide();
                    break;
                case BACK:
                    uri = getPreviousSlide();
                    break;
            }

            intent.putExtra(MainActivity.PHOTO_URI, uri);
            sendBroadcast(intent);
        }
    }

    private Uri resourceToUri(Context context, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID) );
    }

    private void slidesInit(LinkedList<Uri> slideURIs){
        slideURIs.add(resourceToUri(this, R.drawable._1));
        slideURIs.add(resourceToUri(this, R.drawable._2));
        slideURIs.add(resourceToUri(this, R.drawable._3));
        slideURIs.add(resourceToUri(this, R.drawable._4));
        slideURIs.add(resourceToUri(this, R.drawable._5));
    }

    private Uri getPreviousSlide(){
        if(slideIterator.hasPrevious()) {
            return slideIterator.previous();
        } else {
            slideIterator = slideURIs.listIterator(slideURIs.size() - 1);
            return slideIterator.next();
        }
    }

    private Uri getNextSlide(){
        if(slideIterator.hasNext()) {
            return slideIterator.next();
        } else {
            slideIterator = slideURIs.listIterator();
            return slideIterator.next();
        }
    }
}