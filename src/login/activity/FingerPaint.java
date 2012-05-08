package login.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FingerPaint extends GraphicsActivity
        implements ColorPickerDialog.OnColorChangedListener {

    private ProgressDialog pd;
    private String loginId;
    private String sessionKey;
    private String url;
     private int currentSlide;
    private int totalSlides;
    private int[] slidesBitMap;
    private MyView view;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        
        url = getIntent().getStringExtra("URL");
        loginId = getIntent().getStringExtra("login-id");
        sessionKey = getIntent().getStringExtra("session-key");
        totalSlides = getIntent().getIntExtra("size", 30);
        folderName = getIntent().getStringExtra("folderName");
        
        currentSlide = 0;
        slidesBitMap = new int[totalSlides];
        
        for (int j = 0; j < totalSlides; j++) {
        	slidesBitMap[j] = -1;
         }
        
        pd = ProgressDialog.show(this, "", "Loading ...", true);
        
        super.onCreate(savedInstanceState);
        LinearLayout Game = new LinearLayout(this);
        Game.setWeightSum((float)1.0);
        LinearLayout l1 = new LinearLayout(this);

        l1.setOrientation(LinearLayout.VERTICAL);

        view = new MyView(this, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, (float)0.9));

        Button button = new Button(this);
        Button button1 = new Button(this);

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
        
    }

    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    public class MyView extends View {

        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Path    mPath;
        private Paint   mBitmapPaint;
        private int height;
        private int width;

        public MyView(Context c, int width, int height) {
            super(c);

            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            this.width = width;
            // TODO don't hardcode this values
            this.height = height -(int)(((double)height)*0.1);
        }

        public void changeBitmap(Bitmap bp)
        { 
            mBitmap = bp;
            mCanvas = new Canvas(bp);
            Bitmap newbp = Bitmap.createScaledBitmap(bp, width, height, true);
            mCanvas.drawBitmap(newbp, 0, 0, mPaint);
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
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
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
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
                System.out.println("Bitmap is: "+bmp);
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
           
            slidesBitMap[slideId] = 1;
            if (currentSlide == slideId) {
            	 if (pd.isShowing()) {
                     pd.dismiss();
                 }
                String path = folderName + fileName;
                System.out.println("Path is: "+path);
                Bitmap bmp;
                bmp = BitmapFactory.decodeFile(path).copy(Bitmap.Config.ARGB_8888, true);
                System.out.println("Bitmap is: "+bmp);
                view.changeBitmap(bmp);
           }

        }

    }
}