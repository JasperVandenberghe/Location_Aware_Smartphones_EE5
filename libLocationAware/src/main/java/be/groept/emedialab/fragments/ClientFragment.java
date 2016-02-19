package be.groept.emedialab.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.ArrayList;

import be.groept.emedialab.R;
import be.groept.emedialab.adapters.BluetoothDeviceAdapter;
import be.groept.emedialab.communications.ClientBluetoothConnection;
import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.fragments.theme.ThemeSelection;
import be.groept.emedialab.math.CameraConstants;
import be.groept.emedialab.util.ConnectionException;
import be.groept.emedialab.util.GlobalResources;

/**
 * A {@link Fragment} subclass for the client connection setup.
 */
public class ClientFragment extends ListFragment {

    private static final String TAG = "ClientFragment";

    /**
     * Interface elements
     */
    private static CircularProgressView circle;
    private static TextView label;
    private static TextView title;
    private static ListView listView;

    private static boolean connection = false;

    private PartyReadyListener partyReadyListener;

    public ClientFragment() {
        // Required empty public constructor
    }

    /**
     * Handler for getting signal when the game is started
     */
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == DataHandler.DATA_TYPE_PARTY_READY){
                Log.d(TAG, "The party is ready to game!");
                GlobalResources.getInstance().setClient(true);
                partyReadyListener.partyReady();
            }
        }
    };

    /**
     * Needed to set the PartyReadyListener
     * @param activity context the fragment is attached to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            partyReadyListener = (PartyReadyListener) activity;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalResources.getInstance().setHandler(handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ThemeSelection theme = new ThemeSelection();
        LayoutInflater localInflater = theme.selectTheme(getArguments(), inflater, getActivity());
        View rootView =  localInflater.inflate(R.layout.fragment_client, container, false);

        TelephonyManager tm = (TelephonyManager) getActivity().getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        CameraConstants cameraConstants = CameraConstants.getInstance();
        cameraConstants.initPhone(tm.getDeviceId());

        circle = (CircularProgressView) rootView.findViewById(R.id.progress_view);
        label = (TextView) rootView.findViewById(R.id.label);
        title = (TextView) rootView.findViewById(R.id.title);
        listView = (ListView) rootView.findViewById(android.R.id.list);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{
            if(!mBluetoothAdapter.isEnabled()){
                // Enable bluetooth adapter, wait for populating list until it is enabled
                if(mBluetoothAdapter.enable()){
                    IntentFilter bluetoothConnectedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    rootView.getContext().registerReceiver(mReceiver, bluetoothConnectedFilter);
                }
            }else{
                populateList();
            }

        }

        return rootView;
    }

    /**
     * Populates the list of bonded devices
     */
    private void populateList(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
        for(BluetoothDevice device: mBluetoothAdapter.getBondedDevices())
            bluetoothDevices.add(device);
        BluetoothDeviceAdapter deviceAdapter = new BluetoothDeviceAdapter(getActivity(), bluetoothDevices);
        listView.setAdapter(deviceAdapter);
        Log.d(TAG, "Populating listView with " + bluetoothDevices.size() + ( bluetoothDevices.size() == 1 ? " device" : " devices"));
    }

    /**
     * When an item in the list of devices is clicked
     */
    @Override
    public void onListItemClick(ListView list, View view, int position, long id){
        if(!connection)
            try{
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                BluetoothDevice targetDevice = (BluetoothDevice) list.getItemAtPosition(position);
                ClientBluetoothConnection connection = new ClientBluetoothConnection(getActivity().getApplicationContext(), targetDevice);
                connection.connect();
                GlobalResources.getInstance().setConnection(connection);
            } catch(ConnectionException|AssertionError e){
                e.printStackTrace();
            }
        else {
            circle.setVisibility(View.GONE);
            label.setText(getResources().getString(R.string.device_already_connected_to_server));
            label.setTextColor(Color.RED);
            label.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * To be called when the connection is made, this updates the layout
     * @param isConnected boolean to be connected or not
     * @param context Context needed because this is a static method
     */
    public static void connectionDone(boolean isConnected, Context context){
        connection = isConnected;
        if(isConnected) {
            label.setText(context.getResources().getString(R.string.waiting_for_game_start));
            label.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            circle.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            title.setText(context.getResources().getString(R.string.waiting_for_friends));
            GlobalResources.getInstance().addDevice("");
        }else{
            label.setText(context.getResources().getString(R.string.oops));
        }

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(state == BluetoothAdapter.STATE_ON){
                    populateList();
                }
            }
        }
    };

}
