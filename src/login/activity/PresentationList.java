package login.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.syncup.api.Decompress;
import com.syncup.api.ParcelablePresentation;
import com.syncup.api.Presentation;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

public class PresentationList extends ListActivity {
	private ArrayList<ParcelablePresentation> ar;
	private String loginId;
	private String sessionKey;
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	
	  ar = getIntent().getParcelableArrayListExtra("List");
	  loginId = getIntent().getStringExtra("loginId");
	  sessionKey = getIntent().getStringExtra("sessionKey");
	  	  
	  setListAdapter(new ArrayAdapter<ParcelablePresentation>(this, R.layout.main, ar));

	  ListView lv = getListView();
	  
	  
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() 
	  {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) 
	    {
	    	Presentation presentation = ar.get(position).getPresentation();
	    	Toast.makeText(getApplicationContext(), presentation.getName() ,
	          Toast.LENGTH_SHORT).show(); 
	    	String url = "http://10.0.2.2:8080/presentation/" + presentation.getId();
	    	String fileName = presentation.getName() + ".zip";
	    	File output = new File(Environment.getExternalStorageDirectory(),
					fileName);
			if (output.exists()) {
				output.delete();
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
				
				String zipFile = Environment.getExternalStorageDirectory() + "/" + presentation.getName() + ".zip"; 
				String unzipLocation = Environment.getExternalStorageDirectory() + "/SyncUp/"; 
				
				Decompress d = new Decompress(zipFile, unzipLocation);
				d.unzip();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	    }
	  });
	}
	
	
	

}