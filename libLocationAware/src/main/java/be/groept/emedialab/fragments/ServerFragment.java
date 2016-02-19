package be.groept.emedialab.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.ArrayList;

import be.groept.emedialab.R;
import be.groept.emedialab.adapters.BluetoothDeviceAdapter;
import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.fragments.theme.ThemeSelection;
import be.groept.emedialab.math.CameraConstants;
import be.groept.emedialab.server.BluetoothServer;
import be.groept.emedialab.util.GlobalResources;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the interface
 * to handle interaction events.
 * Use the {@link ServerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServerFragment extends Fragment {

    private static final String TAG = "ServerFragment";

    /**
     * UI elements
     */
    private ArrayList<BluetoothDevice> connectedDevices = new ArrayList<>();
    private TextView statusTextView;
    private Button startButton;
    private CircularProgressView circle;
    private View rootView;

    private BluetoothServer bluetoothServer;
    private BluetoothDeviceAdapter connectionAdapter;
    private PartyReadyListener partyReadyListener;

    private static int minDevices = 1;
    private static int maxDevices = 1;

    public static ServerFragment newInstance() {
        return new ServerFragment();
    }

    public ServerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalResources.getInstance().setHandler(handler);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            try {
                minDevices = bundle.getInt("minNumberOfDevices");
                maxDevices = bundle.getInt("maxNumberOfDevices");
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == DataHandler.DATA_TYPE_DEVICE_CONNECTED){
                Log.d(TAG, "Device connected!");
                addDeviceToList((BluetoothDevice) msg.obj);
            }else if(msg.what == DataHandler.DATA_TYPE_DEVICE_DISCONNECTED){
                Log.d(TAG, "Device disconnected!");
                removeDeviceFromList((BluetoothDevice) msg.obj);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            partyReadyListener = (PartyReadyListener) activity;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setupServer(){
        bluetoothServer = new BluetoothServer(rootView.getContext());
        updateStatus();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(bluetoothServer != null){
            bluetoothServer.quit();
        }
    }

    private void updateStatus(){
        Log.d(TAG, "Status: connDev[" + connectedDevices.size() + "] minDev[" + minDevices + "] maxDev[" + maxDevices + "]");
        if(connectedDevices.size() < minDevices){
            int amount = minDevices - connectedDevices.size();
            statusTextView.setText(getResources().getQuantityString(R.plurals.numberOfDevicesNeeded, amount, amount));
            hideButton(false);
        }else if(connectedDevices.size() > maxDevices){
            int amount = connectedDevices.size() - maxDevices;
            statusTextView.setText(getResources().getQuantityString(R.plurals.numberOfDevicesTooMuch, amount, amount));
            hideButton(false);
        }else{
            hideButton(true);
        }
    }

    private void hideButton(boolean inverse){
        if(!inverse){
            startButton.setVisibility(View.GONE);
            circle.setVisibility(View.VISIBLE);
            statusTextView.setVisibility(View.VISIBLE);
        }else{
            startButton.setVisibility(View.VISIBLE);
            circle.setVisibility(View.GONE);
            statusTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ThemeSelection theme = new ThemeSelection();
        LayoutInflater localInflater = theme.selectTheme(getArguments(), inflater, getActivity());

        //Use this rootView instead of getView()
        rootView = localInflater.inflate(R.layout.fragment_server, container, false);

        TelephonyManager tm = (TelephonyManager) getActivity().getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        CameraConstants cameraConstants = CameraConstants.getInstance();
        cameraConstants.initPhone(tm.getDeviceId());

        statusTextView = (TextView) rootView.findViewById(R.id.statusMessage);
        statusTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        startButton = (Button) rootView.findViewById(R.id.startButton);
        startButton.setGravity(Gravity.CENTER_HORIZONTAL);
        //startButton.getBackground().setColorFilter(ContextCompat.getColor(getActivity(), theme.getColor()), PorterDuff.Mode.SRC);
        // startButton.setBackgroundColor(getResources().getColor(theme.getColor()));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setEnabled(false);
                GlobalResources.getInstance().setClient(false);
                GlobalResources.getInstance().sendData(null, DataHandler.DATA_TYPE_PARTY_READY, null);
                partyReadyListener.partyReady();
                Log.d(TAG, "Calling to STOP LISTENING");
                GlobalResources.getInstance().getBluetoothServer().stopListening();
            }
        });

        //waitingForDevices = (LinearLayout) rootView.findViewById(R.id.waitingForDevices);
        circle = (CircularProgressView) rootView.findViewById(R.id.progress_view);

        ListView connectionList = (ListView) rootView.findViewById(R.id.connection_list);
        connectedDevices = new ArrayList<>();
        connectionAdapter = new BluetoothDeviceAdapter(rootView.getContext(), connectedDevices);

        connectionList.setAdapter(connectionAdapter);

        bluetoothServer = GlobalResources.getInstance().getBluetoothServer();

        if(bluetoothServer == null){
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null){  // device doesn't support BT
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else{  // device supports BT
                statusTextView.setText(getResources().getString(R.string.device_supports_bt));
                if(!mBluetoothAdapter.isEnabled()){
                    statusTextView.setText(getResources().getString(R.string.enabling_bluetooth));
                    if(mBluetoothAdapter.enable()){
                        rootView.getContext().registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                    }
                }else{
                    setupServer();
                }
            }

            if(bluetoothServer != null){
                bluetoothServer.start();

                GlobalResources.getInstance().setBluetoothServer(bluetoothServer);

                //ServerPassThrough passThrough = new ServerPassThrough();
                //passThrough.startPolling();
            }
        }/*else {
            //bluetoothServer.setHandler(handler);
        }*/

        return rootView;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(state == BluetoothAdapter.STATE_ON){
                    setupServer();
                }
            }
        }
    };

    private void addDeviceToList(BluetoothDevice device){
        connectedDevices.add(device);
        connectionAdapter.notifyDataSetChanged();
        updateStatus();

        GlobalResources.getInstance().addDevice(device.getAddress());
    }

    private void removeDeviceFromList(BluetoothDevice device){
        connectedDevices.remove(device);
        connectionAdapter.notifyDataSetChanged();
        updateStatus();

        GlobalResources.getInstance().removeDevice(device.getAddress());
    }

}