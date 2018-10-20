package com.example.home.secureforwarding.ShareFileActivites;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.R;
import com.example.home.secureforwarding.ShareFileActivites.SharesFragment.OnListKeyFragmentInteractionListener;

import java.util.List;

public class MyKeySharesRecyclerViewAdapter extends RecyclerView.Adapter<MyKeySharesRecyclerViewAdapter.ViewHolder> {

    private final List<DataShares> shares;
    private final OnListKeyFragmentInteractionListener mListener;

    public MyKeySharesRecyclerViewAdapter(List<DataShares> shares, OnListKeyFragmentInteractionListener listener) {
        this.shares = shares;
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
        holder.shareFile = shares.get(position);
        final DataShares dataShare;
        if (holder.shareFile instanceof DataShares) {
            dataShare = holder.shareFile;
            holder.encryptedNum.setVisibility(View.GONE);
            holder.proxyTxt.setVisibility(View.GONE);
        } else {
            KeyShares keyShare = (KeyShares) holder.shareFile;
            String encryptedNum = "NA";
            if (keyShare.getEncryptedNodeNum() != null && keyShare.getEncryptedNodeNum().length() != 0)
                encryptedNum = keyShare.getEncryptedNodeNum();
            holder.encryptedNum.setText(encryptedNum);
            dataShare = keyShare;
        }

        holder.share_type.setText(dataShare.getShareType());
        holder.file_id.setText(String.valueOf(dataShare.getFileId()));
        holder.dest_id.setText(dataShare.getDestId());

        String value = "Sent";
        if (dataShare.getStatus() == 0)
            value = "Not sent";
        holder.status.setText(value);

        String senderInformation = "NA";
        if (dataShare.getSenderInfo() != null)
            senderInformation = dataShare.getSenderInfo();
        holder.senderInfo.setText(senderInformation);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction((KeyShares) dataShare);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return shares.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView share_type;
        public final TextView file_id;
        public final TextView dest_id;
        public final TextView status;
        public final TextView senderInfo;
        public final TextView encryptedNum;
        public final TextView proxyTxt;


        public DataShares shareFile;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            share_type = view.findViewById(R.id.share_type);
            file_id = view.findViewById(R.id.share_file_id);
            dest_id = view.findViewById(R.id.share_destId);
            status = view.findViewById(R.id.share_status);
            senderInfo = view.findViewById(R.id.share_senderinfo);
            proxyTxt = view.findViewById(R.id.proxyTxt);
            encryptedNum = view.findViewById(R.id.share_encyptedNum);
        }
    }
}
