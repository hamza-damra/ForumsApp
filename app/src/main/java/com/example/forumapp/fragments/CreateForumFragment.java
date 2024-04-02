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

import com.example.forumapp.databinding.FragmentCreateForumBinding;
import com.example.forumapp.models.Auth;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class CreateForumFragment extends Fragment {
    private static final String ARG_FORUME_AUTH = "AUTH";
    Auth mAuth;


    public CreateForumFragment() {
        // Required empty public constructor
    }



    public static CreateForumFragment newInstance(Auth auth) {
        CreateForumFragment fragment = new CreateForumFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FORUME_AUTH, auth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAuth = (Auth) getArguments().getSerializable(ARG_FORUME_AUTH);
        }
    }

    FragmentCreateForumBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateForumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create Forum");
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.cancelForumCreate();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = binding.editTextForumTitle.getText().toString();
                if(title.isEmpty()) {
                    Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                } else {
                    FormBody formBody = new FormBody.Builder()
                            .add("title", title)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://www.theappsdr.com/api/thread/add")
                            .addHeader("Authorization", "Bearer " + mAuth.getToken())
                            .post(formBody)
                            .build();
                    client.newCall(request).enqueue(new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                            if(response.isSuccessful()) {
                                requireActivity().runOnUiThread(() -> mListener.completedForumCreate());
                            } else {
                                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to create forum", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                }
            }
        });
    }

    CreateForumListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CreateForumListener) {
            mListener = (CreateForumListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CreateForumListener");
        }
    }

    public interface CreateForumListener {
        void cancelForumCreate();
        void completedForumCreate();
    }
}