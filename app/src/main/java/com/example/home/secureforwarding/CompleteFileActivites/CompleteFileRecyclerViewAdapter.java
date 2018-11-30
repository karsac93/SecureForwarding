package com.example.home.secureforwarding.CompleteFileActivites;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileFragment.OnListFragmentInteractionListener;
import com.example.home.secureforwarding.R;
import com.example.home.secureforwarding.ShowImageActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class CompleteFileRecyclerViewAdapter extends RecyclerView.Adapter<CompleteFileRecyclerViewAdapter.ViewHolder> {

    private final List<CompleteFiles> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final CompleteFileFragment.ShowImageListener showImageListener;
    private static final String TAG = CompleteFileRecyclerViewAdapter.class.getSimpleName();

    public CompleteFileRecyclerViewAdapter(List<CompleteFiles> completeFiles, OnListFragmentInteractionListener listener,
                                           CompleteFileFragment.ShowImageListener showImageListener) {
        mValues = completeFiles;
        mListener = listener;
        this.showImageListener = showImageListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_completefile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.completeFiles = mValues.get(position);
        Log.d("ImageAdapter", holder.completeFiles.toString());
        File file = new File(holder.completeFiles.getFilePath());
        if (file.exists()) {
            Picasso.get().load(file).fit().centerInside().into(holder.imageView);
            holder.fileName.setText(file.getName());
            if(file.getName().contains("placeholder"))
                holder.fileName.setText(holder.completeFiles.getId() + ".jpg");
            holder.destId.setText(holder.completeFiles.getDestId());
            Log.d(TAG, "file name:" + file.getName() + " destId:" + holder.completeFiles.getDestId());
        }
        else{
            holder.fileName.setText("5_1.jpg");
            holder.destId.setText("3");
            file = new File("/storage/emulated/0/SecureForwarding/DestMessage/5_1.jpg");
            Picasso.get().load(file).fit().centerInside().into(holder.imageView);
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageListener.showImageListener(holder.completeFiles.getFilePath());
            }
        });


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.completeFiles);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageButton imageView;
        public final TextView fileName;
        public final TextView destId;
        public CompleteFiles completeFiles;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageView = view.findViewById(R.id.imageView);
            fileName = view.findViewById(R.id.fileName);
            destId = view.findViewById(R.id.destnode);
        }
    }


}
