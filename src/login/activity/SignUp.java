package login.activity;

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

public class SignUp extends Activity 
{
	MyService mService;
    boolean mBound = false;
	String username;
	String password;
	String loginId;
	String emailId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);  
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
	
/*	public void LoginHandler(View view) 
    {
		if (mBound) 
		{	
				EditText name = (EditText) findViewById(R.id.xparam);
    			EditText pass = (EditText) findViewById(R.id.yparam);
    			username = name.getText().toString();
    			password = pass.getText().toString();
    			
    			String result  = mService.LoginMethod(username, password);
    			
    			System.out.println("Result is: "+ result);
		}
    } 
		*/
		
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
	
	 
		public void SignUp_handler(View view) 
	    {
			if (mBound) 
			{	
					EditText name = (EditText) findViewById(R.id.name);
					EditText login = (EditText) findViewById(R.id.login);
					EditText pass = (EditText) findViewById(R.id.passwd);
					EditText email = (EditText) findViewById(R.id.email);
	    			
	    			username = name.getText().toString();
	    			loginId = login.getText().toString();
	    			password = pass.getText().toString();
	    			emailId = email.getText().toString();
	    			
	    				
	    			String result  = mService.signupMethod(username, loginId, password, emailId);
	    			
	    			System.out.println("In Signup: Result is: "+ result);
			
	    			Intent myIntent = new Intent(view.getContext(), Login.class);
	    			startActivity(myIntent);
			}
	    }
	
}
    
