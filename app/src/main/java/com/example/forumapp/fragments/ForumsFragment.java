package com.example.forumapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.forumapp.databinding.ForumRowItemBinding;
import com.example.forumapp.databinding.FragmentForumsBinding;
import com.example.forumapp.models.Auth;
import com.example.forumapp.models.Forum;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ForumsFragment extends Fragment {
    private final OkHttpClient client = new OkHttpClient();

    private static final String ARG_FORUME_AUTH = "AUTH";

    Auth mAuth;


    public ForumsFragment() {
        // Required empty public constructor
    }


    public static ForumsFragment newInstance(Auth auth) {
        ForumsFragment fragment = new ForumsFragment();
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



    FragmentForumsBinding binding;
    ArrayList<Forum> forums = new ArrayList<>();
    ForumsAdapter adapter;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForumsBinding.inflate(inflater, container, false);
        binding.textViewWelcome.setText("Welcome " +mAuth.getUser_fname() + " " + mAuth.getUser_lname());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle("Forums");
        binding.buttonCreateForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.gotoCreateForum();
            }
        });


        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.logout();
            }
        });

        adapter = new ForumsAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        getForums();


        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            requireActivity().runOnUiThread(() -> binding.swipeRefreshLayout.setRefreshing(true));
            getForums();
            binding.swipeRefreshLayout.setRefreshing(false);
        });

    }

    void getForums() {
        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/api/threads")
                .addHeader("Authorization", "Bearer " + mAuth.getToken())
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    forums.clear();
                    assert response.body() != null;
                    String body = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        JSONArray threads = jsonObject.getJSONArray("threads");
                        for (int i = 0; i < threads.length(); i++) {
                            JSONObject thread = threads.getJSONObject(i);
                            Forum forum = new Forum(thread);
                            forums.add(forum);
                        }
                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }




    class ForumsAdapter extends RecyclerView.Adapter<ForumsAdapter.ForumsViewHolder> {

        @NonNull
        @Override
        public ForumsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ForumsViewHolder(ForumRowItemBinding.inflate(getLayoutInflater(), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ForumsViewHolder holder, int position) {
            Forum forum = forums.get(position);
            holder.setupUI(forum);
        }

        @Override
        public int getItemCount() {
            return forums.size();
        }

        class ForumsViewHolder extends RecyclerView.ViewHolder {
            Forum mForum;
            ForumRowItemBinding mBinding;

            public ForumsViewHolder(ForumRowItemBinding mBinding) {
                super(mBinding.getRoot());
                this.mBinding = mBinding;
                mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.gotoForumMessages(mForum);
                    }
                });
            }

            @SuppressLint("SetTextI18n")
            void setupUI(Forum forum) {
                this.mForum = forum;
                mBinding.textViewForumTitle.setText(mForum.getTitle());
                mBinding.textViewForumCreatedAt.setText(forum.getCreated_at());
                mBinding.textViewForumCreatorName.setText(forum.getCreatedByFname() + " " + forum.getCreatedByLname());

                if (mAuth.getUser_id().equals(forum.getCreatedByUserId())) {
                    mBinding.imageViewDeleteForum.setVisibility(View.VISIBLE);
                    mBinding.imageViewDeleteForum.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           String url = "https://www.theappsdr.com/api/thread/delete/" + mForum.getThread_id();
                            Request request = new Request.Builder()
                                    .url(url)
                                    .addHeader("Authorization", "Bearer " + mAuth.getToken())
                                    .build();
                            client.newCall(request).enqueue(new okhttp3.Callback() {
                                @Override
                                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        getForums();
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
                    });
                } else {
                    mBinding.imageViewDeleteForum.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    ForumsFragmentListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ForumsFragmentListener) {
            mListener = (ForumsFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement ForumsFragmentListener");
        }
    }

    public interface ForumsFragmentListener {
        void logout();
        void gotoCreateForum();
        void gotoForumMessages(Forum forum);
    }


}