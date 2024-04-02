package com.example.forumapp.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.forumapp.databinding.FragmentRegisterBinding;
import com.example.forumapp.models.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class RegisterFragment extends Fragment {
    public RegisterFragment() {
        // Required empty public constructor
    }

    FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Register");
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.gotoLogin();
            }
        });


        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fname = binding.editTextFirstName.getText().toString();
                String lname = binding.editTextLastName.getText().toString();
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                if(fname.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                   // https://www.theappsdr.com/api/signup
                    FormBody formBody = new FormBody.Builder()
                            .add("fname", fname)
                            .add("lname", lname)
                            .add("email", email)
                            .add("password", password)
                            .build();
                    Request request = new Request.Builder()
                            .url("https://www.theappsdr.com/api/signup")
                            .post(formBody)
                            .build();
                    client.newCall(request).enqueue(new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                            if (response.isSuccessful()) {
                                try {
                                    assert response.body() != null;
                                    String body = response.body().string();
                                    JSONObject jsonObject = new JSONObject(body);
                                    Auth auth = new Auth(jsonObject);
                                    mListener.authSuccessful(auth);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Registration failed", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });

                }
            }
        });
    }

    RegisterListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RegisterListener) {
            mListener = (RegisterListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement RegisterListener");
        }
    }

   public interface RegisterListener {
        void authSuccessful(Auth auth);
        void gotoLogin();
    }
}