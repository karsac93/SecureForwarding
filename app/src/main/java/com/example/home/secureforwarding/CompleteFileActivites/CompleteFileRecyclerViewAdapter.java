package com.example.home.secureforwarding.CompleteFileActivites;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileFragment.OnListFragmentInteractionListener;
import com.example.home.secureforwarding.R;

import java.io.File;
import java.util.List;

public class CompleteFileRecyclerViewAdapter extends RecyclerView.Adapter<CompleteFileRecyclerViewAdapter.ViewHolder> {

    private final List<CompleteFiles> mValues;
    private final OnListFragmentInteractionListener mListener;
    private static final String TAG = CompleteFileRecyclerViewAdapter.class.getSimpleName();

    public CompleteFileRecyclerViewAdapter(List<CompleteFiles> completeFiles, OnListFragmentInteractionListener listener) {
        mValues = completeFiles;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_completefile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.completeFiles =  mValues.get(position);
        Log.d("ImageAdapter", holder.completeFiles.toString());
        File file = new File(holder.completeFiles.getFilePath());
        if(file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            holder.imageView.setImageBitmap(bitmap);
            holder.fileName.setText("File name:" + file.getName());
            holder.destId.setText("Destination node:" + holder.completeFiles.getDestId());
        }

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
        public final ImageView imageView;
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
