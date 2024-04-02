package com.example.forumapp.models;

import org.json.JSONObject;

import java.io.Serializable;

public class Auth implements Serializable {
    /*
    {
    "status": "ok",
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MTE5Njk2NTEsImV4cCI6MTc0MzUwNTY1MSwianRpIjoiMTRCMTJKRGhBd2F0Q2R0SjNVdGVQNSIsInVzZXIiOjV9.X9UhAkYWsUppi9y_pkLU7U8kv0OCHHEWd-37u_WhjFA",
    "user_id": 5,
    "user_email": "hamza@mail.com",
    "user_fname": "hamza",
    "user_lname": "damra",
    "user_role": "USER"
   }
*/

    private final String status;
    private final String token;
    private final String user_id;
    private String user_email;
    private final String user_fname;
    private final String user_lname;
    private String user_role;



    public Auth(JSONObject jsonObject) {
        this.status = jsonObject.optString("status");
        this.token = jsonObject.optString("token");
        this.user_id = jsonObject.optString("user_id");
        this.user_fname = jsonObject.optString("user_fname");
        this.user_lname = jsonObject.optString("user_lname");
    }



    public Auth(String status, String token, String user_id, String user_email, String user_fname, String user_lname, String user_role) {
        this.status = status;
        this.token = token;
        this.user_id = user_id;
        this.user_email = user_email;
        this.user_fname = user_fname;
        this.user_lname = user_lname;
        this.user_role = user_role;
    }


    public String getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUser_email() {
        return user_email;
    }

    public String getUser_fname() {
        return user_fname;
    }

    public String getUser_lname() {
        return user_lname;
    }

    public String getUser_role() {
        return user_role;
    }

}
