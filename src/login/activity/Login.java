package login.activity;

import java.util.ArrayList;
import com.syncup.api.*;
import login.activity.MyService.LocalBinder;
import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity 
{
	MyService mService;
    boolean mBound = false;
	String username;
	String password;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);  
		 Intent intent=new Intent("login.activity.MyService");  
	        this.startService(intent);

    } 
	
	@Override
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
	
	public void LoginHandler(View view) 
    {
		if (mBound) 
		{	
				EditText name = (EditText) findViewById(R.id.xparam);
    			EditText pass = (EditText) findViewById(R.id.yparam);
    			username = name.getText().toString();
    			password = pass.getText().toString();
    			
    			LogInResponse response  = mService.LoginMethod(username, password);
    			
    			if(response == null)
    			{
    				Toast.makeText(getApplicationContext(), "Invalid Username/Password", Toast.LENGTH_SHORT).show();
    				return;
    			}
    			
    			ArrayList<ParcelablePresentation> pointsExtra = new ArrayList<ParcelablePresentation>();
    			for (Presentation point : response.getPresentationsList()) {
    			   pointsExtra.add(new ParcelablePresentation(point));
    			}

    	        Intent i = new Intent(this, PresentationList.class);
    	        i.putExtra("List", pointsExtra);
    	        i.putExtra("loginId", response.getLoginId());
    	        i.putExtra("sessionKey", response.getSessionKey());
    			startActivity(i);
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
	
	 
	public void SignUp(View view) 
	{	
    		Intent myIntent = new Intent(view.getContext(), SignUp.class);
   			startActivity(myIntent);
   			
   			System.out.println("Back to Login!!");
    }
	
}
    
