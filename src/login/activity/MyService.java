package login.activity;

import java.util.*;
import com.google.gson.Gson;
import com.syncup.api.*;
import com.syncup.rest.RequestMethod;
import com.syncup.rest.RestClient;

import android.app.Service;
import android.os.Binder;
import android.os.IBinder;
import android.content.Intent;

public class MyService extends Service 
{
	String jsonString;
    private final IBinder mBinder = new LocalBinder();
   
    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public LogInResponse LoginMethod(String name, String passwd)
    {
    	System.out.println("Inside service file");
    	
    	Calendar cal = new GregorianCalendar();
		long hour = cal.get(Calendar.HOUR);
		long minutes = cal.get(Calendar.MINUTE);
		long seconds = cal.get(Calendar.SECOND);
		
		long timestamp = hour*60 + minutes*60 + seconds;
    	
    	LogInRequest login = new LogInRequest();
    	
    	login.setLoginId(name);
    	login.setPassword(passwd);
    	login.setTimestamp(timestamp);
    	login.setNonce((int)Math.random());
    	
    	Gson gson = new Gson();
    	jsonString = gson.toJson(login);
    
    	RestClient rc = new RestClient("http://10.0.2.2:8080/login");
    	
    	rc.setJSONString(jsonString);
    	System.out.println(jsonString);
    	
    	try
    	{
    		rc.execute(RequestMethod.POST);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    	
    	if(rc.getResponseCode() != 200)
    	{
    		return null;
    	}
    	
    	LogInResponse response = gson.fromJson(rc.getResponse(), LogInResponse.class);
    	return response;
    	
    }
    
    public String signupMethod(String name, String login, String password, String email)
    {
    	System.out.println("Inside service file for signupMethod");
    	 
    	User u = new User();
    	
    	u.setName(name);
    	u.setLoginId(login);
    	u.setPassword(password);
    	u.setEmailId(email);
    	
    	Gson gson = new Gson();
    	jsonString = gson.toJson(u);
    
    	RestClient rc = new RestClient("http://10.0.2.2:8080/signup");
    	
    	rc.setJSONString(jsonString);
    	System.out.println(jsonString);
    	
    	try
    	{
    		rc.execute(RequestMethod.POST);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    	
    	if(rc.getResponseCode() != 200)
    	{
    		return rc.getErrorMessage();
    	}
        
    	return rc.getResponse();
    }
    
}