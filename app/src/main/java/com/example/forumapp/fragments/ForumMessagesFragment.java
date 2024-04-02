package com.example.forumapp.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.forumapp.databinding.FragmentForumMessagesBinding;
import com.example.forumapp.databinding.MessageRowItemBinding;
import com.example.forumapp.models.Auth;
import com.example.forumapp.models.Forum;
import com.example.forumapp.models.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ForumMessagesFragment extends Fragment {
    private static final String ARG_PARAM_FORUM = "ARG_PARAM_FORUM";
    private static final String ARG_PARAM_AUTH = "ARG_PARAM_AUTH";
    private Forum mForum;
    private Auth mAuth;
    ArrayList<Message> messages = new ArrayList<>();

    public ForumMessagesFragment() {
        // Required empty public constructor
    }

    public static ForumMessagesFragment newInstance(Forum forum, Auth auth) {
        ForumMessagesFragment fragment = new ForumMessagesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_FORUM, forum);
        args.putSerializable(ARG_PARAM_AUTH, auth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mForum = (Forum) getArguments().getSerializable(ARG_PARAM_FORUM);
            mAuth = (Auth) getArguments().getSerializable(ARG_PARAM_AUTH);
        }
    }

    FragmentForumMessagesBinding binding;
    MessagesAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForumMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private final OkHttpClient client = new OkHttpClient();

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Forum Messages");
        adapter = new MessagesAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.textViewForumCreatedAt.setText(mForum.getCreated_at());
        binding.textViewForumTitle.setText(mForum.getTitle());
        binding.textViewForumCreatorName.setText(mForum.getCreatedByFname() + " " + mForum.getCreatedByLname());
        getMessages();


        binding.buttonSubmit.setOnClickListener(view1 -> {
            String message = binding.editTextMessage.getText().toString();
            if(message.isEmpty()){
                Toast.makeText(getContext(), "Message is required", Toast.LENGTH_SHORT).show();
            } else {
                RequestBody body = new FormBody.Builder()
                        .add("message", message)
                        .add("thread_id", mForum.getThread_id())
                        .build();
                Request request = new Request.Builder()
                        .url("https://www.theappsdr.com/api/message/add")
                        .addHeader("Authorization", "Bearer " + mAuth.getToken())
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) {
                        requireActivity().runOnUiThread(() -> {
                            if(response.isSuccessful()){
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();
                                    binding.editTextMessage.setText("");
                                    getMessages();
                                });

                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    try {
                                        Toast.makeText(getContext(), Objects.requireNonNull(response.body()).string(), Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    void getMessages(){
        //https://www.theappsdr.com/api/messages/6
        String url = "https://www.theappsdr.com/api/messages/" + mForum.getThread_id();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + mAuth.getToken())
                .build();
client.newCall(request).enqueue(new okhttp3.Callback() {
    @Override
    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
        e.printStackTrace();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) {
        if(response.isSuccessful()){
            try {
                messages.clear();
                assert response.body() != null;
                String body = response.body().string();
                JSONObject jsonObject = new JSONObject(body);
                JSONArray jsonArray = jsonObject.getJSONArray("messages");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject messageObject = jsonArray.getJSONObject(i);
                    Message message = new Message(messageObject);
                    messages.add(message);
                }
                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            requireActivity().runOnUiThread(() -> {
                try {
                    Toast.makeText(getContext(), Objects.requireNonNull(response.body()).string(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
});



    }


    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

        @NonNull
        @Override
        public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MessagesViewHolder(MessageRowItemBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
            Message message = messages.get(position);
            holder.setupUI(message);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessagesViewHolder extends RecyclerView.ViewHolder {
            Message mMessage;
            MessageRowItemBinding mBinding;
            public MessagesViewHolder(MessageRowItemBinding mBinding) {
                super(mBinding.getRoot());
                this.mBinding = mBinding;
            }

            @SuppressLint("SetTextI18n")
            void setupUI(Message message){
                this.mMessage = message;
                mBinding.textViewMessage.setText(message.getMessage());
                mBinding.textViewMessageCreatedAt.setText(message.getCreated_at());
                mBinding.textViewMessageCreatorName.setText(message.getCreatedByFname() + " " + message.getCreatedByLname());
                if(mAuth.getUser_id().equals(message.getCreatedByUserId())){
                    mBinding.imageViewDeleteMessage.setVisibility(View.VISIBLE);
                    mBinding.imageViewDeleteMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // https://www.theappsdr.com/api/message/delete/2
                            String urlStr = "https://www.theappsdr.com/api/message/delete/" + mMessage.getMessage_id();
                            Request request = new okhttp3.Request.Builder()
                                    .url(urlStr)
                                    .addHeader("Authorization", "Bearer " + mAuth.getToken())
                                    .build();
                            client.newCall(request).enqueue(new okhttp3.Callback() {
                                @Override
                                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to delete message", Toast.LENGTH_SHORT).show());
                                    Log.d("demo", "onFailure: " + e.getMessage());
                                }

                                @Override
                                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) {
                                    requireActivity().runOnUiThread(() -> {
                                        if(response.isSuccessful()){
                                            requireActivity().runOnUiThread(() -> {
                                                Toast.makeText(getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                                                Log.d("demo", "onResponse: " + "Message deleted");
                                                getMessages();
                                            });

                                        } else {
                                            requireActivity().runOnUiThread(() -> {
                                                try {
                                                    Log.d("demo1", "onResponse: " + Objects.requireNonNull(response.body()).string());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    mBinding.imageViewDeleteMessage.setVisibility(View.GONE);
                }
            }
        }
    }

}