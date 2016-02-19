package be.groept.emedialab.animal_farm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import be.groept.emedialab.animal_farm.util.AniFarmPacket;
import be.groept.emedialab.animations.confetti.ConfettiFallView;
import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.communications.DataPacket;
import be.groept.emedialab.image_manipulation.RunPatternDetector;
import be.groept.emedialab.math.DistanceCalculation;
import be.groept.emedialab.server.data.Device;
import be.groept.emedialab.server.data.Position;
import be.groept.emedialab.util.GlobalResources;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private static final int[] allSounds = new int[]{R.raw.bird, R.raw.cat, R.raw.chicken, R.raw.cow, R.raw.dog, R.raw.duck, R.raw.elephant, R.raw.frog, R.raw.horse, R.raw.monkey, R.raw.mouse, R.raw.penguin, R.raw.pig, R.raw.sheep};
    private static final int[] nameAnimals = new int[]{R.raw.name_vogel, R.raw.name_kat, R.raw. name_kip, R.raw.name_koe, R.raw.name_hond, R.raw.name_eend, R.raw.name_olifant, R.raw.name_kikker, R.raw.name_paard, R.raw.name_aap, R.raw.name_muis, R.raw.name_pinguin, R.raw.name_varken, R.raw.name_schaap};
    private static final int TOTAL_LEVELS = 14;

    private View mContentView;
    private View boardView;
    private FrameLayout insertPoint;
    private ImageView image;
    private String macAddressCorrectSound;
    private String macAddressImage;
    private String serverMac;
    private int soundId;
    private int imageId;
    private String macDevice = "";
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private int level = 1;
    private int serverLevel = 1;
    private boolean levelingUp = false;
    private boolean newLevel = false;
    private boolean won = false;
    private boolean imageTouch = true;

    public static final int TYPE_DISPLAY = 0;
    public static final int TYPE_LEVEL_UP = 1;
    public static final int TYPE_NEW_LEVEL = 2;
    public static final int TYPE_RESTART_GAME = 3;
    public static final int TYPE_LEAVE_GAME = 4;
    public static final int TYPE_SOUND = 5;
    public static final int TYPE_WON = 6;

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
    public void handleMessage(Message msg){
            super.handleMessage(msg);
            Log.d(TAG, "Handler called");
            if(msg.what == DataHandler.DATA_TYPE_DATA_PACKET){
                Serializable data;
                if((data = GlobalResources.getInstance().readData()) != null)
                    handleData((DataPacket) data);
            } else if(msg.what == DataHandler.DATA_TYPE_COORDINATES){
                if(!levelingUp && !won)
                    checkDistance();
            } else if(msg.what == DataHandler.DATA_TYPE_DEVICE_DISCONNECTED){
                BluetoothDevice device = (BluetoothDevice) msg.obj;
                Log.e(TAG, "Device " + device.getAddress() + " disconnected!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Activity activity = this;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new RunPatternDetector(activity);
            }
        };
        runnable.run();
        GlobalResources.getInstance().setHandler(handler);

        setContentView(R.layout.activity_game);
        mContentView = findViewById(R.id.fullscreen_content);
        boardView = findViewById(R.id.boardView);
        insertPoint = (FrameLayout) findViewById(R.id.balloonView);
        image = (ImageView) findViewById(R.id.gameImage);

        if(!GlobalResources.getInstance().getClient())
            serverSetup(false);
        else{
            Serializable data;
            if((data = GlobalResources.getInstance().readData()) != null)
                handleData((DataPacket) data);
        }
    }

    @SuppressLint("InlinedApi")
    private void hide(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();

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
    protected void onPause(){
        super.onPause();
        GlobalResources.getInstance().getPatternDetector().destroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(GlobalResources.getInstance().getPatternDetector() != null && GlobalResources.getInstance().getPatternDetector().isPaused())
            GlobalResources.getInstance().getPatternDetector().setup();
        hide();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        GlobalResources.getInstance().getPatternDetector().destroy();
    }

    @Override
    public void onBackPressed(){
        //Do nothing, not allowed to go back
    }

    private void handleData(DataPacket dataPacket){
        Log.d(TAG, "Reading datapacket of type " + dataPacket.getDataType());
        switch (dataPacket.getDataType()){
            case TYPE_DISPLAY:
                AniFarmPacket aniFarmPacket = (AniFarmPacket) dataPacket.getOptionalData();
                soundId = aniFarmPacket.getSoundId();
                imageId = aniFarmPacket.getImageId();
                displayImage();
                break;
            case TYPE_LEVEL_UP:
                AniFarmPacket aniFarmPacket2 = (AniFarmPacket) dataPacket.getOptionalData();
                soundId = aniFarmPacket2.getSoundId();
                imageId = aniFarmPacket2.getImageId();
                levelUp();
                break;
            case TYPE_NEW_LEVEL:
                startSwipeOut();
                break;
            case TYPE_RESTART_GAME:
                recreate();
                break;
            case TYPE_LEAVE_GAME:
                relaunchApp();
                break;
            case TYPE_SOUND:
                displaySound((int) dataPacket.getOptionalData());
                break;
            case TYPE_WON:
                winning();
                break;
        }
    }

    private void serverSetup(boolean levelUp){
        if(levelUp)
            serverLevel++;
        Log.d(TAG, "Server level " + serverLevel);
        Map<String, Position> connectedDevices = GlobalResources.getInstance().getDevices();
        ArrayList<Integer> numbers = new ArrayList<>();

        numbers.add(serverLevel - 1);
        numbers = populateList(numbers, connectedDevices.size());
        Collections.shuffle(numbers);
        Device self = GlobalResources.getInstance().getDevice();
        HashMap<String, Position> connectedAddresses = new HashMap<>();
        for(String mac: connectedDevices.keySet()){
            connectedAddresses.put(mac, connectedDevices.get(mac));
        }
        connectedAddresses.put(self.getMac(), self.getPosition());
        serverMac = self.getMac();

        ArrayList<HashMap<String, Position>> groupedDevices = DistanceCalculation.getGroups(connectedAddresses);
        boolean foundOne = false;
        ArrayList<HashMap<String, Position>> groupOne = new ArrayList<>();
        ArrayList<HashMap<String, Position>> groupGreaterTwo = new ArrayList<>();
        for(HashMap<String, Position> group: groupedDevices){
            if(group.size() == 1){
                foundOne = true;
                groupOne.add(group);
            } else if(group.size() > 2){
                groupGreaterTwo.add(group);
            }
        }
        if(foundOne){
            HashMap<String, Position> oneDevice;
            if(groupOne.size() == 1){
                oneDevice = groupOne.get(0);
            } else{
                Random random = new Random();
                int group = random.nextInt(groupOne.size());
                oneDevice = groupOne.get(group);
            }
            Object[] macAddress = oneDevice.keySet().toArray();
            if(serverLevel <= TOTAL_LEVELS)
                macAddressImage = (String) macAddress[0];
        } else if(groupGreaterTwo.size() > 0){
            Random r = new Random();
            if(groupGreaterTwo.size() == 1){
                Object[] macAddresses = groupGreaterTwo.get(0).keySet().toArray();
                int mac = r.nextInt(macAddresses.length);
                if(serverLevel <= TOTAL_LEVELS)
                    macAddressImage = (String) macAddresses[mac];
            } else{
                int group = r.nextInt(groupGreaterTwo.size());
                HashMap<String, Position> randomGroup = groupGreaterTwo.get(group);
                Object[] macAddresses = randomGroup.keySet().toArray();
                int mac = r.nextInt(macAddresses.length);
                if(serverLevel <= TOTAL_LEVELS)
                    macAddressImage = (String) macAddresses[mac];
            }
        } else{
            Random random = new Random();
            if(groupedDevices.size() > 0) {
                int group = random.nextInt(groupedDevices.size());
                HashMap<String, Position> randomGroup = groupedDevices.get(group);
                Object[] macAddresses = randomGroup.keySet().toArray();
                int mac = random.nextInt(macAddresses.length);
                if (serverLevel <= TOTAL_LEVELS)
                    macAddressImage = (String) macAddresses[mac];
            } else{
                int group = random.nextInt(connectedAddresses.size());
                int i = 0;
                for(String key: connectedAddresses.keySet()){
                    if(i == group && serverLevel <= TOTAL_LEVELS)
                        macAddressImage = key;
                    i++;
                }
            }
        }
        if(macAddressImage.equals(serverMac)){
            //serverSound = false;
            imageId = serverLevel;
            if(!levelUp)
                displayImage();
            else {
                Log.d(TAG, "Server has image");
                levelUp();
            }
        } else {
            if (!levelUp)
                GlobalResources.getInstance().sendData(macAddressImage, new DataPacket(TYPE_DISPLAY, new AniFarmPacket(0, serverLevel)));
            else
                GlobalResources.getInstance().sendData(macAddressImage, new DataPacket(TYPE_LEVEL_UP, new AniFarmPacket(0, serverLevel)));
        }
        connectedAddresses.remove(macAddressImage);
        int i = 0;
        for(String key: connectedAddresses.keySet()){
            if(serverLevel <= TOTAL_LEVELS && serverLevel - 1 == numbers.get(i))
                macAddressCorrectSound = key;
            if (key.equals(serverMac)) {
                imageId = 0;
                soundId = numbers.get(i);
                if (!levelUp)
                    displayImage();
                else {
                    Log.d(TAG, "Server has sound");
                    levelUp();
                }
            } else {
                if (!levelUp)
                    GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_DISPLAY, new AniFarmPacket(numbers.get(i), 0)));
                else
                    GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_LEVEL_UP, new AniFarmPacket(numbers.get(i), 0)));
            }
            i++;
        }
    }

    private ArrayList<Integer> populateList(ArrayList<Integer> numbers, int numberOfDevices){
        while(numbers.size() < numberOfDevices){
            int number = generateRandom();
            if(!numbers.contains(number))
                numbers.add(number);
        }
        return numbers;
    }

    private int generateRandom(){
        Random r = new Random();
        int low = 0;
        int high = allSounds.length;
        return r.nextInt(high - low) + low;
    }

    private void displayImage(){
        Log.d(TAG, "Image ID: " + imageId);
        Log.d(TAG, "Sound ID: " + soundId);
        String uri = "@drawable/";
        if(imageId != 0) {
            switch (level) {
                case 1:
                    uri += "bird";
                    break;
                case 2:
                    uri += "cat";
                    break;
                case 3:
                    uri += "chicken";
                    break;
                case 4:
                    uri += "cow";
                    break;
                case 5:
                    uri += "dog";
                    break;
                case 6:
                    uri += "duck";
                    break;
                case 7:
                    uri += "elephant";
                    break;
                case 8:
                    uri += "frog";
                    break;
                case 9:
                    uri += "horse";
                    break;
                case 10:
                    uri += "monkey";
                    break;
                case 11:
                    uri += "mouse";
                    break;
                case 12:
                    uri += "penguin";
                    break;
                case 13:
                    uri += "pig";
                    break;
                case 14:
                    uri += "sheep";
                    break;
            }
        } else
            uri += "loudspeaker";

        if(level <= TOTAL_LEVELS) {
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            Drawable res = getResources().getDrawable(imageResource);
            image.setImageDrawable(res);
            if(imageId == 0){
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(imageTouch) {
                            Animation shake = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.shake);
                            image.startAnimation(shake);
                            new SoundTask().execute(allSounds[soundId]);
                        }
                    }
                });
            } else{
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(imageTouch){
                            Animation shake = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.shake);
                            image.startAnimation(shake);
                            new SoundTask().execute(nameAnimals[level - 1]);
                        }
                    }
                });
            }
            if(newLevel){
                Animation swipeIn = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.swipe_in);
                image.setVisibility(View.VISIBLE);
                image.startAnimation(swipeIn);
            }
        }
    }

    private void displaySound(int sound){
        switch (sound){
            case 1: //TA DA sound
                new SoundTask().execute(R.raw.tadaaa);
                break;
            case 2: //Fail sound
                new SoundTask().execute(R.raw.buzzer);
                break;
        }
    }

    private void checkDistance(){
        Map<String, Position> connectedDevices = GlobalResources.getInstance().getDevices();
        HashMap<String, Position> allDevices = new HashMap<>();
        for(String key: connectedDevices.keySet())
            allDevices.put(key, connectedDevices.get(key));
        allDevices.put(GlobalResources.getInstance().getDevice().getMac(), GlobalResources.getInstance().getDevice().getPosition());
        ArrayList<HashMap<String, Position>> groupedDevices = DistanceCalculation.getGroups(allDevices);
        for (HashMap<String, Position> group : groupedDevices) {
            if (group.size() == 2) {
                if (group.keySet().contains(macAddressImage)) {
                    if (group.keySet().contains(macAddressCorrectSound)) {
                        if (macAddressImage.equals(serverMac))
                            displaySound(1);
                        else
                            GlobalResources.getInstance().sendData(macAddressImage, new DataPacket(TYPE_SOUND, 1));
                        levelingUp = true;
                        serverSetup(true);
                    } else {
                        String currentMac = null;
                        for (String mac : group.keySet())
                            if (!mac.equals(macAddressImage))
                                currentMac = mac;
                        if (!macDevice.equals(currentMac)) {
                            if (macAddressImage.equals(serverMac))
                                displaySound(2);
                            else
                                GlobalResources.getInstance().sendData(macAddressImage, new DataPacket(TYPE_SOUND, 2));
                            macDevice = currentMac;
                        }
                    }
                }
            } else if (group.size() == 1 && group.keySet().contains(macAddressImage))
                macDevice = "";
        }
    }

    private void levelUp(){
        imageTouch = false;
        Log.d(TAG, "Level up called! Current level = " + level);
        level++;
        int levelComplete = level - 1;
        Animation slideIn = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.slide_in);
        TextView text = (TextView) findViewById(R.id.levelUpText);
        if(level > TOTAL_LEVELS)
            text.setText(getResources().getString(R.string.you_won));
        else
            text.setText(getResources().getString(R.string.level_complete, levelComplete));

        boardView.setVisibility(View.VISIBLE);
        boardView.startAnimation(slideIn);
        insertPoint.setVisibility(View.VISIBLE);
        ConfettiFallView confettiFallView = new ConfettiFallView(this);
        confettiFallView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        insertPoint.addView(confettiFallView);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(!GlobalResources.getInstance().getClient() && level <= TOTAL_LEVELS){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startSwipeOut();
                            HashMap<String, ArrayList<DataPacket>> connectedDevices = GlobalResources.getInstance().getConnectedDevices();
                            for(String key: connectedDevices.keySet())
                                GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_NEW_LEVEL));
                        }
                    }, 3000);
                } else if(!GlobalResources.getInstance().getClient() && level > TOTAL_LEVELS){
                    winning();

                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void winning(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSwipeOut();
            }
        }, 3000);
    }

    private void startSwipeOut(){
        Animation swipeOut = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.swipe_out);
        image.startAnimation(swipeOut);
        image.setVisibility(View.GONE);
        boardView.startAnimation(swipeOut);
        boardView.setVisibility(View.GONE);
        if (level <= TOTAL_LEVELS){
            insertPoint.startAnimation(swipeOut);
            insertPoint.setVisibility(View.GONE);
        }
        swipeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                levelingUp = false;
                imageTouch = true;
                if(level <= TOTAL_LEVELS){
                    newLevel = true;
                    displayImage();
                } else{
                    won = true;
                    RelativeLayout winning = (RelativeLayout) findViewById(R.id.winningFrame);
                    winning.setVisibility(View.VISIBLE);
                    Animation slideIn = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.slide_in);
                    winning.startAnimation(slideIn);
                    new SoundTask().execute(R.raw.cheers);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    Handler mySoundHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    image.clearAnimation();
                    break;
            }
        }
    };

    protected class SoundTask extends AsyncTask<Integer, Void, Void>{
        @Override
        protected Void doInBackground(Integer... params){
            AssetFileDescriptor afd = getResources().openRawResourceFd(params[0]);
            try{
                mediaPlayer.reset();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mySoundHandler.sendEmptyMessage(0);
                            }
                        });
                    }
                });
                afd.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void restartActivity(View v){
        for (String key: GlobalResources.getInstance().getConnectedDevices().keySet())
            GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_RESTART_GAME));
        recreate();
    }

    public void leaveGame(View v){
        for (String key: GlobalResources.getInstance().getConnectedDevices().keySet())
            GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_LEAVE_GAME));
        relaunchApp();
    }

    private void relaunchApp(){
        Intent intent = new Intent(this, SetupActivity.class);
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, pendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        //System.exit(0);
        GlobalResources.getInstance().getPatternDetector().destroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
