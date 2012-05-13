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
import com.syncup.utils.Configuration;

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
    
    	RestClient rc = new RestClient(Configuration.loginUrl);
    	
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
    	 
    	User user = new User();
    	
    	user.setName(name);
    	user.setLoginId(login);
    	user.setPassword(password);
    	user.setEmailId(email);
    	
    	Gson gson = new Gson();
    	jsonString = gson.toJson(user);
    
    	RestClient rc = new RestClient(Configuration.signupUrl);
    	
    	rc.setJSONString(jsonString);
    	System.out.println(jsonString);
    	
    	try
    	{
    		rc.execute(RequestMethod.POST);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    
    	if(rc.getResponseCode() == 400)
    	{
    		return "Please enter all the details";
    	}
    	
    	if(rc.getResponseCode() == 409)
    	{
    		return "LoginId already exists. Please choose some other name";
    	}
    	
    	return "OK";
    }
    
    public void syncMethod(PathPoint p, int pId, int slide) 
    {
    	Gson gson = new Gson();
    	jsonString = gson.toJson(p);
    	System.out.println("String is: " + jsonString);
    	RestClient rc = new RestClient(Configuration.presentationUrl + pId + "/" + slide + "/sync");
    	rc.setJSONString(jsonString);
    	
    	try
    	{
    		rc.execute(RequestMethod.POST);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    }
    
    public SyncResponse syncReceiveMethod(int pId, int slide)
    {
    	Gson gson = new Gson();
    	RestClient rc = new RestClient(Configuration.presentationUrl + pId + "/" + slide + "/sync");
    	
    	try
    	{
    		rc.execute(RequestMethod.GET);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    	
    	System.out.println("In service file. JSON is: " + rc.getResponse());
    	SyncResponse response = gson.fromJson(rc.getResponse(), SyncResponse.class);
    	
    	return response;
    }
    
}