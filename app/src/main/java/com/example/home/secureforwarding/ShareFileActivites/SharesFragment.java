package com.example.home.secureforwarding.ShareFileActivites;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileActivity;
import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.MainActivity;
import com.example.home.secureforwarding.R;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;

import org.apache.commons.lang3.SerializationUtils;

import java.util.List;


public class SharesFragment extends Fragment implements MyKeySharesRecyclerViewAdapter.OnListKeyFragmentInteractionListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private MyKeySharesRecyclerViewAdapter.OnListKeyFragmentInteractionListener mListener;
    Context context;
    String msg_id;
    List<DataShares> dataShares;
    MyKeySharesRecyclerViewAdapter adapter;
    AppDatabase database;
    RecyclerView recyclerView;
    boolean check = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SharesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shares_list, container, false);
        mListener = this;
        Bundle bundle = getArguments();
        msg_id = bundle.getString(CompleteFileActivity.MSG_ID);
        Log.d("=======", msg_id);
        database = AppDatabase.getAppDatabase(context);
        if (msg_id != KeyConstant.INTER_TYPE) {
            dataShares = database.dao().getDataShareForMsg(msg_id);
            dataShares.addAll(database.dao().getKeyShareForMsg(msg_id));
        } else {
            dataShares = database.dao().getInterDataShares(KeyConstant.INTER_TYPE);
            dataShares.addAll(database.dao().getInterKeyShares(KeyConstant.INTER_TYPE));
        }

        // Set the adapter
        adapter = new MyKeySharesRecyclerViewAdapter(dataShares, mListener);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            //RecyclerView recyclerView = (RecyclerView) view;
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onListFragmentInteraction(DataShares dataShare, int position) {
        KeyShares share = (KeyShares) dataShare;
        String id = SharedPreferenceHandler.getStringValues(getContext(),  MainActivity.DEVICE_ID);
        if(share.getType().contains(KeyConstant.OWNER_TYPE) || share.getEncryptedNodeNum().contains(id)){
            Intent intent = new Intent(getContext(), ChooseEncryption.class);
            Bundle bundle = new Bundle();
            byte[] shareByte = SerializationUtils.serialize(share);
            bundle.putByteArray(ShareFilesActivity.SEND_SHARE_KEY, shareByte);
            bundle.putInt(ShareFilesActivity.POS, position);
            intent.putExtras(bundle);
            startActivityForResult(intent, ShareFilesActivity.REQ);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        Log.d("HEopee", "Result code:" + resultCode);
        if(requestCode==ShareFilesActivity.REQ && resultCode == ShareFilesActivity.REQ)
        {
            Bundle bundle = data.getExtras();
            byte[] shareObject = bundle.getByteArray(ShareFilesActivity.SEND_SHARE_KEY);
            KeyShares share = SerializationUtils.deserialize(shareObject);
            Log.d("HEopee", "In Listener:" + share.getEncryptedNodeNum());
            int position = bundle.getInt(ShareFilesActivity.POS);
            dataShares.set(position, share);
            adapter.notifyItemChanged(position);

        }
    }

}
