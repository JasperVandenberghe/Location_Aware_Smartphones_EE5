package be.groept.emedialab.rank_em;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import be.groept.emedialab.fragments.ClientFragment;
import be.groept.emedialab.fragments.PartyReadyListener;
import be.groept.emedialab.fragments.ServerFragment;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SecondActivity extends FragmentActivity implements PartyReadyListener {

    private static final String TAG = "SecondActivity";

    public static final int minNumberOfDevices = 1;
    public static final int maxNumberOfDevices = 5;

    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        mContentView = findViewById(R.id.fullscreen_content);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Bundle bundle = getIntent().getExtras();
        if(bundle.getBoolean("create")){
            Bundle bundle2 = new Bundle();
            bundle2.putInt("minNumberOfDevices", minNumberOfDevices);
            bundle2.putInt("maxNumberOfDevices", maxNumberOfDevices);
            bundle2.putString("Theme", "HMT");
            ServerFragment serverFragment = new ServerFragment();
            serverFragment.setArguments(bundle2);
            fragmentTransaction.add(R.id.fragment_container, serverFragment);
        }else{
            Bundle b = new Bundle();
            b.putString("Theme", "HMT");
            ClientFragment client = new ClientFragment();
            client.setArguments(b);
            fragmentTransaction.add(R.id.fragment_container, client);
        }
        fragmentTransaction.commit();

        hide();
    }

    private void hide(){
        if(Build.VERSION.SDK_INT >= 21){
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hide();
    }

    @Override
    public void partyReady() {
        Log.d(TAG, "The party is ready, dear sir.");
        startActivity(new Intent(this, GameActivity.class));
    }
}
