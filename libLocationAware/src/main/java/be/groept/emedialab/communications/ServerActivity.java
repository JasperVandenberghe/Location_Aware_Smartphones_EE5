package be.groept.emedialab.communications;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import be.groept.emedialab.R;
import be.groept.emedialab.server.BluetoothServer;
import be.groept.emedialab.server.ServerPassThrough;
import be.groept.emedialab.server.data.Position;
import be.groept.emedialab.util.GlobalResources;
import be.groept.emedialab.util.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ServerActivity extends Activity {

    private static final String TAG = "ServerActivity";
    private BluetoothServer server;
    private ArrayList<String> connectedDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        Log.i(TAG, "Creating Server Activity");
        ListView lstServerConnections = (ListView) findViewById(R.id.lst_server_connections);
        lstServerConnections.setAdapter(new ArrayAdapter<>(this, R.layout.list_item, connectedDevices));

        server = GlobalResources.getInstance().getBluetoothServer();
        if(server == null) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter == null){
                createToast("Your device doesn't support Bluetooth");
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else{
                if(!mBluetoothAdapter.isEnabled()) {
                    createToast("Enabling Bluetooth...");
                    if(mBluetoothAdapter.enable()){
                        server = new BluetoothServer(this.getBaseContext());
                    }else{
                        createToast("Couldn't enable Bluetooth");
                    }
                }else{
                    Log.d(TAG, "server = new BluetoothServer()");
                    server = new BluetoothServer(this.getBaseContext());
                }
            }

            if(server != null) {
                Log.d(TAG, "Starting BT Server");
                createToast("Starting BT Server");
                server.start();
                GlobalResources.getInstance().setBluetoothServer(server);

                //Also store the values of this device on the server.
                ServerPassThrough passThrough = new ServerPassThrough();
                passThrough.startPolling();
            }
        }
    }

    private void createToast(String text){
        Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    /*private void updatePositionsList() {
        ListView lstServerConnections = (ListView) findViewById(R.id.lst_server_connections);
        ArrayAdapter adapter = (ArrayAdapter<String>) lstServerConnections.getAdapter();
        adapter.clear();
        for (Map.Entry<String, Tuple<Position, String>> entry : server.getConnectedDevices().getAll())
        {
            String id = entry.getKey();
            Position pos = entry.getValue().element1;
            String line = String.format("%s (x: %.2f, y: %.2f, rot: %.2f, z: %.2f)", id,
                                pos.getX(), pos.getY(), pos.getRotation(), pos.getZ());
            adapter.insert(line,0);
        }
        ((ArrayAdapter) lstServerConnections.getAdapter()).notifyDataSetChanged();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server, menu);
        menu.add("Stop");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Stop") && server != null){
            //Stop Server.
            server.quit();
            Toast.makeText(this.getApplicationContext(), "[Server] Server Stopped", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}
