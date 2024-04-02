package com.example.forumapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.forumapp.R;
import com.example.forumapp.fragments.CreateForumFragment;
import com.example.forumapp.fragments.ForumMessagesFragment;
import com.example.forumapp.fragments.ForumsFragment;
import com.example.forumapp.fragments.LoginFragment;
import com.example.forumapp.fragments.RegisterFragment;
import com.example.forumapp.models.Auth;
import com.example.forumapp.models.Forum;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, RegisterFragment.RegisterListener, CreateForumFragment.CreateForumListener, ForumsFragment.ForumsFragmentListener {
    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_AUTH = "auth";
    private Auth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Restore mAuth from SharedPreferences
        String authJson = sharedPreferences.getString(KEY_AUTH, null);
        if (authJson != null) {
            mAuth = new Gson().fromJson(authJson, Auth.class);
        }

        if (mAuth == null) {
            // If mAuth is null, show the login fragment
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.rootView, new LoginFragment())
                        .commit();
            }
        } else {
            // If mAuth is not null, show the ForumsFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rootView, ForumsFragment.newInstance(mAuth))
                    .commit();
        }
    }

    @Override
    public void authSuccessful(Auth auth) {
        this.mAuth = auth;
        saveAuthToSharedPreferences(auth);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, ForumsFragment.newInstance(auth))
                .commit();
    }

    private void saveAuthToSharedPreferences(Auth auth) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String authJson = new Gson().toJson(auth);
        editor.putString(KEY_AUTH, authJson);
        editor.apply();
    }



    @Override
    public void gotoLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();
    }

    @Override
    public void gotoRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new RegisterFragment())
                .commit();
    }

    @Override
    public void cancelForumCreate() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void completedForumCreate() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void logout() {
        mAuth = null;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_AUTH);
        editor.apply();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();
    }

    @Override
    public void gotoCreateForum() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, CreateForumFragment.newInstance(mAuth))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoForumMessages(Forum forum) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, ForumMessagesFragment.newInstance(forum, mAuth))
                .addToBackStack(null)
                .commit();
    }
}
