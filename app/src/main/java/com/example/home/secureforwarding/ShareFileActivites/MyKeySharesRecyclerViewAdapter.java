package com.example.home.secureforwarding.ShareFileActivites;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.home.secureforwarding.DataHandler.DataConstant;
import com.example.home.secureforwarding.Entities.Shares;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.R;
import com.example.home.secureforwarding.ShareFileActivites.SharesFragment.OnListKeyFragmentInteractionListener;

import org.w3c.dom.Text;

import java.util.List;

public class MyKeySharesRecyclerViewAdapter extends RecyclerView.Adapter<MyKeySharesRecyclerViewAdapter.ViewHolder> {

    private final List<Shares> mValues;
    private final OnListKeyFragmentInteractionListener mListener;

    public MyKeySharesRecyclerViewAdapter(List<Shares> items, OnListKeyFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_shares, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.shareFile = mValues.get(position);
        if(holder.shareFile.getShareType().contains(DataConstant.DATA_TYPE))
            holder.encryptedNum.setVisibility(View.GONE);
        else
            holder.encryptedNum.setVisibility(View.VISIBLE);
        holder.msg_id.setText("File id: " + holder.shareFile.getId());
        holder.share_type.setText("Share type: " + holder.shareFile.getShareType());
        holder.file_id.setText("ID of the share: " + String.valueOf(holder.shareFile.getFileId()));
        holder.dest_id.setText("Destination id: " + holder.shareFile.getDestId());

        String value = "sent";
        if(holder.shareFile.getStatus() == 0)
            value = "Not sent";
        holder.status.setText("Status: " + value);

        String senderInformation = "null";
        if(holder.shareFile.getSenderInfo() != null)
            senderInformation = holder.shareFile.getSenderInfo();

        holder.senderInfo.setText("This file sent to: " + senderInformation);

        String encryptedNum = "Proxy key not generated";
        if(holder.shareFile.getEncryptedNodeNum() != null && holder.shareFile.getEncryptedNodeNum().length() != 0)
            encryptedNum = holder.shareFile.getEncryptedNodeNum();
        holder.encryptedNum.setText("Proxy key generated with: " + encryptedNum);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.shareFile);
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
        public final TextView msg_id;
        public final TextView share_type;
        public final TextView file_id;
        public final TextView dest_id;
        public final TextView status;
        public final TextView senderInfo;
        public final TextView encryptedNum;


        public Shares shareFile;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            msg_id = view.findViewById(R.id.share_msgId);
            share_type = view.findViewById(R.id.share_type);
            file_id = view.findViewById(R.id.share_file_id);
            dest_id = view.findViewById(R.id.share_destId);
            status = view.findViewById(R.id.share_status);
            senderInfo = view.findViewById(R.id.share_senderinfo);
            encryptedNum = view.findViewById(R.id.share_encyptedNum);
        }
    }
}
