package be.groept.emedialab.opencvtester;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.image_manipulation.ImageSettings;
import be.groept.emedialab.image_manipulation.RunPatternDetector;
import be.groept.emedialab.util.GlobalResources;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ImageView imageView;

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == DataHandler.DATA_TYPE_OWN_POS_UPDATED){
                updateCameraView(GlobalResources.getInstance().getImage());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_rgb).setChecked(true);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView versionNumberTextView = (TextView) navigationHeaderView.findViewById(R.id.textView);
        versionNumberTextView.setText(String.format(getResources().getString(R.string.navigation_drawer_header_subscript), BuildConfig.VERSION_NAME));
        GlobalResources.getInstance().setHandler(handler);

        imageView = (ImageView) findViewById(R.id.cameraFeed);

        final Activity activity = this;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new RunPatternDetector(activity);
            }
        };
        runnable.run();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.about_title));
            alertDialog.setMessage(getResources().getString(R.string.about_message));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(R.string.about_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.nav_rgb) {
            checkRadio(item);
            GlobalResources.getInstance().getImageSettings().setBackgroundMode(ImageSettings.BACKGROUND_MODE_RGB);
        }else if(id == R.id.nav_grayscale){
            checkRadio(item);
            GlobalResources.getInstance().getImageSettings().setBackgroundMode(ImageSettings.BACKGROUND_MODE_GRAYSCALE);
        }else if(id == R.id.nav_binary){
            checkRadio(item);
            GlobalResources.getInstance().getImageSettings().setBackgroundMode(ImageSettings.BACKGROUND_MODE_BINARY);
        }else if(id == R.id.nav_contours_pattern){
            toggleItem(item, ImageSettings.OVERLAY_PATTERN);
        }else if(id == R.id.nav_contours_square){
            toggleItem(item, ImageSettings.OVERLAY_SQUARE_BIG_CONTOURS);
        }else if(id == R.id.nav_contours_big){
            toggleItem(item, ImageSettings.OVERLAY_BIG_CONTOURS);
        }else if(id == R.id.nav_contours_all){
            toggleItem(item, ImageSettings.OVERLAY_CONTOURS);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        GlobalResources.getInstance().getPatternDetector().destroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(GlobalResources.getInstance().getPatternDetector() != null && GlobalResources.getInstance().getPatternDetector().isPaused())
            GlobalResources.getInstance().getPatternDetector().setup();
    }

    private void toggleItem(MenuItem item, int type){
        boolean checked = !item.isChecked();
        item.setChecked(checked);
        GlobalResources.getInstance().getImageSettings().setOverlayEnabled(type, checked);
    }

    private void checkRadio(MenuItem item){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.nav_rgb).setChecked(false);
        navigationMenu.findItem(R.id.nav_grayscale).setChecked(false);
        navigationMenu.findItem(R.id.nav_binary).setChecked(false);

        item.setChecked(true);
    }

    private void updateCameraView(Mat matrix){
        Mat drawMatrix = new Mat();
        Core.flip(matrix, drawMatrix, 0);
        Core.flip(drawMatrix.t(), drawMatrix, 1);
        Bitmap bitmap = Bitmap.createBitmap(drawMatrix.cols(), drawMatrix.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(drawMatrix, bitmap, true);
        imageView.setImageBitmap(bitmap);
    }
}
