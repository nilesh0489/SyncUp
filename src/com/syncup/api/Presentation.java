package com.syncup.api;

public class Presentation {
    private long id;
    private String name;
    private String loginId;
    //TODO go live dates for the presentations

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
    
    public String toString()
    {
    	return this.name;
    }
}
