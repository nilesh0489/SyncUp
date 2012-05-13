package login.activity;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import com.syncup.api.ParcelablePresentation;
import com.syncup.api.Presentation;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import com.syncup.utils.Configuration;

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

                String url = Configuration.presentationUrl + presentation.getId() + "/";
                String folderName = Environment.getExternalStorageDirectory() + "/SyncUp/" + presentation.getName() + "/";
                
                File file = new File(folderName);
                file.mkdirs();
                Intent myIntent = new Intent(view.getContext(), FingerPaint.class);
                myIntent.putExtra("URL", url);
                myIntent.putExtra("login-id", loginId);
                myIntent.putExtra("session-key", sessionKey);
                myIntent.putExtra("folderName", folderName);
                myIntent.putExtra("size", presentation.getNoSlides());
                myIntent.putExtra("Id", presentation.getId());
                startActivity(myIntent);
            }
        });
    }


    }
