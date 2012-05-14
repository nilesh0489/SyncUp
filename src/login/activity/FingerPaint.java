package login.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.syncup.api.PathPoint;
import com.syncup.api.SyncResponse;
import com.syncup.utils.SerializablePath;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import login.activity.MyService.LocalBinder;

public class FingerPaint extends GraphicsActivity
        implements ColorPickerDialog.OnColorChangedListener {

    MyService mService;
    boolean mBound = false;
    private ProgressDialog pd;
    private String loginId;
    private String sessionKey;
    private String url;
    private int currentSlide;
    private int totalSlides;
    private int[] slidesBitMap;
    private MyView view;
    private String folderName;
    private Timer syncTimer;
    private Button button;
    private Button button1;
    private int pId;
    private int mpathCounter;
    private String presenterId;
    boolean isPresenter = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

    	mpathCounter = 0;
        Intent intent=new Intent("login.activity.MyService");
        this.startService(intent);

        url = getIntent().getStringExtra("URL");
        presenterId = getIntent().getStringExtra("Presenter-Id");
        loginId = getIntent().getStringExtra("login-id");
        sessionKey = getIntent().getStringExtra("session-key");
        totalSlides = (int)getIntent().getLongExtra("size", 30);
        folderName = getIntent().getStringExtra("folderName");
        pId = getIntent().getIntExtra("Id", 1);
        currentSlide = 0;
        slidesBitMap = new int[totalSlides];

        for (int j = 0; j < totalSlides; j++) {
            slidesBitMap[j] = -1;
        }
        
        if(presenterId.equals(loginId))
        {
        	isPresenter = true;
        }
        
        else
        {
        	isPresenter = false;
        }
        
        pd = ProgressDialog.show(this, "", "Loading ...", true);

        super.onCreate(savedInstanceState);
        LinearLayout Game = new LinearLayout(this);
        Game.setWeightSum((float)1.0);
        LinearLayout l1 = new LinearLayout(this);

        l1.setOrientation(LinearLayout.VERTICAL);

        view = new MyView(this, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, (float)0.9));

        button = new Button(this);
        button1 = new Button(this);

        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.05));
        button.setText(R.string.button2);

        button1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, (float)0.05));
        button1.setText(R.string.button1);

        l1.addView(view);
        l1.addView(button);
        l1.addView(button1);
        Game.addView(l1);


        setContentView(Game);

        slideDownloader(currentSlide);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

        button.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (currentSlide == 0) {
                    String error = "Reached Start of Presentation";
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                }
                else  {
                    currentSlide = currentSlide - 1;
                    slideDownloader(currentSlide);
                }
            }
        });

        button1.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (currentSlide == totalSlides) {
                    String error = "Reached End of Presentation";
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                }
                else  {
                    currentSlide = currentSlide + 1;
                    slideDownloader(currentSlide);
                }
            }
        });
        
        //TODO: if presenter then don't start timer
        if(!isPresenter) {

        	syncTimer = new Timer();
            syncTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }

            }, 0, 5000);

        }
    }

    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    public void displayToast() {
        Toast.makeText(getApplicationContext(), "Working", Toast.LENGTH_SHORT);

    }
    
    public void changeButtonText() {
        button.setText("Hello World");
    }

    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            List<PathPoint> pList;
            SyncResponse sr = mService.syncReceiveMethod(pId, currentSlide);
            pList = sr.getPathPointList();
            System.out.println("PList is : " + pList);
            if (pList != null) {
                    view.drawSyncPath(pList);                       
            }
        }
    };

    public class MyView extends View {

        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private SerializablePath mPath;
        private Paint   mBitmapPaint;
        private int height;
        private int width;

        public MyView(Context c, int width, int height) {
            super(c);

            mPath = new SerializablePath();
            System.out.println("In the view constructor");

            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            this.width = width;
            this.height = height -200;
        }

        public void changeBitmap(Bitmap bp)
        {
            //mBitmap = bp;
            mCanvas = new Canvas(bp);
            Bitmap newbp = Bitmap.createScaledBitmap(bp, width, height, true);
            mBitmap = newbp;
            mCanvas.drawBitmap(newbp, 0, 0, mBitmapPaint);
            invalidate();

        }

        class Pt{
            float x, y;
            Pt(float _x, float _y){
                x = _x;
                y = _y;
            }
        }
        
        public void drawSyncPath(List<PathPoint> p){
        	SerializablePath path = new SerializablePath();
        	for(int i = mpathCounter; i < p.size(); i++)
        	{
        		path.addPathPointList(p.get(i).getPathPoints());
            	path.loadPathPointsAsQuadTo();
            	mCanvas.drawPath(path, mPaint);
        	}
        	        	
        	//mCanvas.drawPath(path, mPaint);
            invalidate();


        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            System.out.println("In on size changed function");

            if(mBitmap == null)
            {
                mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            }
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mPath, mPaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mPath.addPathPoints(new float[]{mX, mY, (x + mX)/2, (y + mY)/2});
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // TODO Copy this to a new path object and sync it
            // TODO If presenter then only do this
           // mCanvas = new Canvas(mBitmap);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawPath(mPath, mPaint);
        	invalidate();
            if(isPresenter)
            {
            	PathPoint pathPoint = new PathPoint();
                pathPoint.setPathPoints(mPath.getPathPoints());
                mService.syncClientMethod(pathPoint, pId, currentSlide);             
            }            
            mPath.reset(); 
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                        PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                        PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void slideDownloader(int slide) {
        int n = slide + 5;
        for (int j = slide ; j < totalSlides && j < n; j++) {
            if (slidesBitMap[j] == -1) {
                DownloadImageTask task = new DownloadImageTask(folderName, j);
                task.execute(new String[] {url});
            }
            else if (slidesBitMap[j] == 0 && currentSlide == j) {
                pd = ProgressDialog.show(this, "", "Loading ...", true);
            }
            else if (currentSlide == j){
                String fileName = slide + ".jpg";
                String path = folderName + fileName;
                System.out.println("Path is: "+path);
                Bitmap bmp;
                bmp = BitmapFactory.decodeFile(path).copy(Bitmap.Config.ARGB_8888, true);
                view.changeBitmap(bmp);
            }
        }

    }


    private class DownloadImageTask extends AsyncTask<String, Void, String> {

        String folderName;
        String fileName;
        int slideId;

        public DownloadImageTask(String folderName, int slideId) {
            this.folderName = folderName;
            this.slideId = slideId;
        }

        @Override
        protected void onPreExecute() {
            slidesBitMap[slideId] = 0;
        }

        protected String doInBackground (String ... urls) {
            for (String url : urls) {
                url = url + slideId;
                fileName = slideId + ".jpg";
                File output = new File(folderName, fileName);
                if (output.exists()) {
                    return null;
                }

                InputStream stream = null;
                FileOutputStream fos = null;
                try {

                    HttpGet httpGet = new HttpGet(url);
                    HttpClient httpclient = new DefaultHttpClient();
                    httpGet.setHeader("login-id", loginId);
                    httpGet.setHeader("session-key", sessionKey);
                    HttpResponse response = httpclient.execute(httpGet);
                    stream = response.getEntity().getContent();
                    BufferedInputStream buf = new BufferedInputStream(stream);
                    System.out.println(Environment.getExternalStorageState());
                    fos = new FileOutputStream(output);
                    int next = -1;
                    while ((next = buf.read()) != -1) {
                        fos.write(next);
                    }
                    fos.close();
                    buf.close();
                }
                catch (Exception e) {
                }
            }
            return null;
        }

        protected void onPostExecute(String result) {
            System.out.println("Inside Post Execute function");
            slidesBitMap[slideId] = 1;
            if (currentSlide == slideId) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                String path = folderName + fileName;
                Bitmap bmp;
                bmp = BitmapFactory.decodeFile(path).copy(Bitmap.Config.ARGB_8888, true);
                view.changeBitmap(bmp);
            }

        }

    }
}