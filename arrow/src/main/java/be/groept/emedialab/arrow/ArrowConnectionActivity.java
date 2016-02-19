package be.groept.emedialab.arrow;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import be.groept.emedialab.fragments.ClientFragment;
import be.groept.emedialab.fragments.PartyReadyListener;
import be.groept.emedialab.fragments.ServerFragment;

public class ArrowConnectionActivity extends AppCompatActivity implements PartyReadyListener {

    public static final int minNumberOfDevices = 1;
    public static final int maxNumberOfDevices = 1;

    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow_connection);
        mContentView = findViewById(R.id.relativeLayout);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Bundle bundle = getIntent().getExtras();
        if(bundle.getBoolean("create")){
            Bundle b = new Bundle();
            b.putInt("minNumberOfDevices", minNumberOfDevices);
            b.putInt("maxNumberOfDevices", maxNumberOfDevices);
            ServerFragment serverFragment = new ServerFragment();
            serverFragment.setArguments(b);
            b.putString("Theme", "HMT");
            fragmentTransaction.add(R.id.fragment_container, serverFragment);
        }else{
            Bundle b = new Bundle();
            ClientFragment client = new ClientFragment();
            client.setArguments(b);
            b.putString("Theme", "HMT");
            fragmentTransaction.add(R.id.fragment_container, client);
        }
        fragmentTransaction.commit();

        hide();
    }

    private void hide(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

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
        startActivity(new Intent(this, ArrowGame.class));
    }

}
