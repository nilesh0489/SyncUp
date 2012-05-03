package com.syncup.api;

import java.util.List;


public class LogInResponse {
    private String loginId;
    private String sessionKey;
    private int nonce;
    private List<Presentation> presentationsList;
    
 
    public List<Presentation> getPresentationsList() {
        return presentationsList;
    }

    public void setPresentationsList(List<Presentation> presentationsList) {
        this.presentationsList = presentationsList;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
}