package be.groept.emedialab.rank_em;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.core.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import be.groept.emedialab.animations.confetti.ConfettiFallView;
import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.communications.DataPacket;
import be.groept.emedialab.image_manipulation.PatternDetector;
import be.groept.emedialab.image_manipulation.RunPatternDetector;
import be.groept.emedialab.math.DistanceCalculation;
import be.groept.emedialab.server.data.Position;
import be.groept.emedialab.util.GlobalResources;

/**
 * Activity with the actual game.
 * In case of server: it receives coordinates and sends out what other devices must do.
 * In case of client: it receives commands of the server and executes them.
 */
public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private static final int[] allExplanations = new int[]{R.raw.klein_naar_groot, R.raw.leg_de_penguin, R.raw.leg_aantal_appels, R.raw.leg_de_eend, R.raw.leg_de_dieren, R.raw.leg_het_aapje, R.raw.leg_aantal_snoepjes, R.raw.leg_de_ballen, R.raw.leg_de_kerstman, R.raw.jong_naar_oud};

    private View mContentView;
    private HashMap<String, Integer> numbersToPhone = new HashMap<>();
    private LinkedHashMap<String, Integer> sortedValues = new LinkedHashMap<>();
    private ImageView image;
    private int level;
    private boolean imageTouch = true;
    private boolean levelingUp = false;
    private boolean newLevel = false;
    private boolean wrongPosition = false;
    private boolean won = false;

    private View boardView;
    private FrameLayout insertPoint;
    private int imageNumber;

    private ArrayList<Integer> oldNumbers = new ArrayList<>();

    /**
     * Data types for this game
     */
    public final static int TYPE_DISPLAY = 0;
    public final static int TYPE_ERROR = 1;
    public final static int TYPE_OK = 2;
    public final static int TYPE_LEVEL_UP = 3;
    public final static int TYPE_NEW_LEVEL = 4;
    public final static int TYPE_RESTART_GAME = 5;
    public final static int TYPE_LEAVE_GAME = 6;
    public final static int TYPE_START_SHAKE = 7;
    public final static int TYPE_STOP_SHAKE = 8;

    public final static int TOTAL_LEVELS = 10;

    private MediaPlayer mediaPlayer = new MediaPlayer();

    /**
     * Handler for getting signals
     */
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "Handler called");
            if(msg.what == DataHandler.DATA_TYPE_DATA_PACKET){
                Serializable data;
                if((data = GlobalResources.getInstance().readData()) != null){
                    handleData((DataPacket) data);
                }
            }else if(msg.what == DataHandler.DATA_TYPE_COORDINATES){
                if(!levelingUp && !won) {
                    checkDistance();
                }
            }else if(msg.what == DataHandler.DATA_TYPE_DEVICE_DISCONNECTED){
                BluetoothDevice bluetoothDevice = (BluetoothDevice) msg.obj;
                Log.e(TAG, "Device " + bluetoothDevice.getAddress() + " disconnected!");
                sortedValues.remove(bluetoothDevice.getAddress());
                if(sortedValues.size() <= SecondActivity.minNumberOfDevices){
                    leaveGame(mContentView);
                }
            }
        }
    };

    private void handleData(DataPacket dataPacket){
        Log.d(TAG, "Reading datapacket of type " + dataPacket.getDataType());
        switch(dataPacket.getDataType()){
            case TYPE_DISPLAY:
                displayImage((int) dataPacket.getOptionalData());
                break;
            case TYPE_ERROR:
                Log.d(TAG, "ERROR! Shake the number!");
                break;
            case TYPE_LEVEL_UP:
                Log.d(TAG, "LEVELUP! Leveling up!");
                levelUp((int) dataPacket.getOptionalData());
                break;
            case TYPE_NEW_LEVEL:
                Log.d(TAG, "Starting new level");
                startSwipeOut();
                break;
            case TYPE_RESTART_GAME:
                Log.d(TAG, "Restarting game!");
                recreate();
                break;
            case TYPE_LEAVE_GAME:
                Log.d(TAG, "Leaving game");
                relaunchApp();
                break;
            case TYPE_OK:
                Log.d(TAG, "OK! Hurray, it's ok!");
                break;
            case TYPE_START_SHAKE:
                Log.d(TAG, "Starting to shake!");
                if(!wrongPosition){
                    shakeImage();
                    wrongPosition = true;
                }
                break;
            case TYPE_STOP_SHAKE:
                Log.d(TAG, "Stopping shake!");
                if(wrongPosition){
                    image.clearAnimation();
                    wrongPosition = false;
                }
                break;
        }
    }

    private void shakeImage(){
        if(image != null) {
            Animation shake = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.shake);
            image.startAnimation(shake);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        level = 1;
        GlobalResources.getInstance().setHandler(handler);

        setContentView(R.layout.activity_game);
        mContentView = findViewById(R.id.fullscreen_content);
        image = (ImageView) findViewById(R.id.gameImage);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageTouch){
                    shakeImage();
                    switch (level) {
                        case 1: //numbers
                            switch (imageNumber) {
                                case 1:
                                    new SoundTask().execute(R.raw.een);
                                    break;
                                case 2:
                                    new SoundTask().execute(R.raw.twee);
                                    break;
                                case 3:
                                    new SoundTask().execute(R.raw.drie);
                                    break;
                                case 4:
                                    new SoundTask().execute(R.raw.vier);
                                    break;
                                case 5:
                                    new SoundTask().execute(R.raw.vijf);
                                    break;
                            }
                            break;
                        case 2: //penguin
                            new SoundTask().execute(R.raw.penguin);
                            break;
                        case 3: //apple
                            new SoundTask().execute(R.raw.apple);
                            break;
                        case 4: //duck
                            new SoundTask().execute(R.raw.duck);
                            break;
                        case 5: //animal
                            switch (imageNumber) {
                                case 1: //mouse
                                    new SoundTask().execute(R.raw.mouse);
                                    break;
                                case 2: //cat
                                    new SoundTask().execute(R.raw.cat);
                                    break;
                                case 3: //dog
                                    new SoundTask().execute(R.raw.dog);
                                    break;
                                case 4: //cow
                                    new SoundTask().execute(R.raw.cow);
                                    break;
                                case 5: //elephant
                                    new SoundTask().execute(R.raw.elephant);
                                    break;
                            }
                            break;
                        case 6: //monkey
                            new SoundTask().execute(R.raw.monkey);
                            break;
                        case 7: //candy
                            new SoundTask().execute(R.raw.candy);
                            break;
                        case 8: //ball
                            switch (imageNumber) {
                                case 1: // golfball
                                    new SoundTask().execute(R.raw.golfball);
                                    break;
                                case 2: //tennis
                                    new SoundTask().execute(R.raw.tennisball);
                                    break;
                                case 3: //baseball
                                    new SoundTask().execute(R.raw.baseball);
                                    break;
                                case 4: //soccer
                                    new SoundTask().execute(R.raw.soccerball);
                                    break;
                                case 5: //beach ball
                                    new SoundTask().execute(R.raw.beachball);
                                    break;
                            }
                            break;
                        case 9: //santa
                            new SoundTask().execute(R.raw.santa);
                            break;
                        case 10: //family
                            switch (imageNumber) {
                                case 1: //baby
                                    new SoundTask().execute(R.raw.baby);
                                    break;
                                case 2: //little brother
                                    new SoundTask().execute(R.raw.kleine_broer);
                                    break;
                                case 3: //big sister
                                    new SoundTask().execute(R.raw.grote_zus);
                                    break;
                                case 4: //father
                                    new SoundTask().execute(R.raw.papa);
                                    break;
                                case 5: //grandmother
                                    new SoundTask().execute(R.raw.oma);
                                    break;
                            }
                            break;
                    }
                }
            }
        });

        // Hide navigation on > 5.0
        hide();

        if(!GlobalResources.getInstance().getClient()) {
            serverSetup(false);
        }else{
            Serializable data;
            if((data = GlobalResources.getInstance().readData()) != null){
                handleData((DataPacket) data);
            }
        }
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
        protected Void doInBackground(Integer...params){
            AssetFileDescriptor afd = getResources().openRawResourceFd(params[0]);
            try{
                mediaPlayer.reset();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer arg0) {
                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                            public void onCompletion(MediaPlayer mp){
                                mySoundHandler.sendEmptyMessage(0);
                            }
                        });
                    }
                });
                afd.close();
            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @SuppressLint("InlinedApi")
    private void hide() {
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
    protected void onPause(){
        super.onPause();
        PatternDetector patternDetector = GlobalResources.getInstance().getPatternDetector();
        if(patternDetector != null)
            patternDetector.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(GlobalResources.getInstance().getPatternDetector() != null && GlobalResources.getInstance().getPatternDetector().isPaused())
            GlobalResources.getInstance().getPatternDetector().setup();
        hide();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        PatternDetector patternDetector = GlobalResources.getInstance().getPatternDetector();
        if(patternDetector != null)
            patternDetector.destroy();
    }

    @Override
    public void onBackPressed(){
        //Do nothing, not allowed to go back
    }

    private void displayImage(int number){
        imageNumber = number;
        String uri = "@drawable/";
        switch (level) {
            case 1:
                uri += "number";
                break;
            case 2:
                uri += "penguin";
                break;
            case 3:
                uri += "apple";
                break;
            case 4:
                uri += "duck";
                break;
            case 5:
                uri += "animal";
                break;
            case 6:
                uri += "monkey";
                break;
            case 7:
                uri += "candy";
                break;
            case 8:
                uri += "ball";
                break;
            case 9:
                uri += "santa";
                break;
            case 10:
                uri += "human_cycle_";
                break;
            default:
                break;
        }
        if(level <= TOTAL_LEVELS){
            uri += number;
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            Drawable res = getResources().getDrawable(imageResource);
            image.setImageDrawable(res);
            //if (newLevel) {
                Animation swipeIn = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.swipe_in);
                image.setVisibility(View.VISIBLE);
                image.startAnimation(swipeIn);
                if(!GlobalResources.getInstance().getClient()){
                    swipeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            new SoundTask().execute(allExplanations[level - 1]);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            //}
        }
    }

    private void serverSetup(boolean levelUp){
        numbersToPhone.clear();
        HashMap<String, Position> connectedDevices = (HashMap) GlobalResources.getInstance().getDevices();
        ArrayList<Integer> newNumbers = populateList(connectedDevices.size());

        while(newNumbers.equals(oldNumbers))
            newNumbers = populateList(connectedDevices.size());
        oldNumbers = newNumbers;

        int i = 0;
        for(String key: connectedDevices.keySet()){
            if(!levelUp) {
                GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_DISPLAY, newNumbers.get(i)));
            }else{
                GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_LEVEL_UP, newNumbers.get(i)));
            }
            numbersToPhone.put(key, newNumbers.get(i));
            Log.d(TAG, "Number " + newNumbers.get(i) + " sent to " + key);
            i++;
        }
        if(levelUp){
            levelUp(newNumbers.get(i));
        }else{
            displayImage(newNumbers.get(i));
        }
        numbersToPhone.put("ownpos", newNumbers.get(i));

        sortedValues = sortMapByValues(numbersToPhone);
    }

    private void levelUp(final int number){
        imageTouch = false;
        imageNumber = number;
        level++;
        int levelComplete = level - 1;
        Log.d("LevelUp", "Leveling up called");
        Animation slideIn = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.slide_in);
        TextView text = (TextView) findViewById(R.id.levelUpText);
        if(level > TOTAL_LEVELS)
            text.setText(getResources().getString(R.string.you_won));
        else
            text.setText(getResources().getString(R.string.level_complete, levelComplete));
        boardView = findViewById(R.id.boardView);
        boardView.setVisibility(View.VISIBLE);
        boardView.startAnimation(slideIn);
        insertPoint = (FrameLayout) findViewById(R.id.balloonView);
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
                if (!GlobalResources.getInstance().getClient() && level <= TOTAL_LEVELS) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startSwipeOut();
                            HashMap<String, ArrayList<DataPacket>> connectedDevices = GlobalResources.getInstance().getConnectedDevices();
                            for (String key : connectedDevices.keySet())
                                GlobalResources.getInstance().sendData(key, new DataPacket(TYPE_NEW_LEVEL));
                        }
                    }, 3000);

                } else if (!GlobalResources.getInstance().getClient() && level > TOTAL_LEVELS) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startSwipeOut();
                        }
                    }, 3000);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void startSwipeOut(){
        Animation swipeOut = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.swipe_out);
        image.startAnimation(swipeOut);
        image.setVisibility(View.GONE);
        boardView.startAnimation(swipeOut);
        boardView.setVisibility(View.GONE);
        if(level <= TOTAL_LEVELS) {
            insertPoint.setAnimation(swipeOut);
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
                if (level <= TOTAL_LEVELS) {
                    newLevel = true;
                    displayImage(imageNumber);
                } else {
                    won = true;
                    RelativeLayout winning = (RelativeLayout) findViewById(R.id.winningFrame);
                    winning.setVisibility(View.VISIBLE);
                    Animation slideIn = AnimationUtils.loadAnimation(mContentView.getContext(), R.anim.swipe_in);
                    winning.startAnimation(slideIn);
                    new SoundTask().execute(R.raw.cheers);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private int generateRandom(int connectedDevices){
        Random r = new Random();
        int low = 1;
        //We need to add 2 to the number of connectedDevices because if 3 devices with 1 server
        //Connected devices = 2, so add 1 to get the total number of devices
        //And add 1 because it is a half open collection, meaning high is not part of the random numbers.
        int high = connectedDevices + 2;
        return r.nextInt(high - low) + low;
    }

    private ArrayList<Integer> populateList(int numberOfDevices){
        ArrayList<Integer> newNumbers = new ArrayList<>();
        while(newNumbers.size() < numberOfDevices + 1){
            int number = generateRandom(numberOfDevices);
            Log.d(TAG, "Generated number " + number);
            if(!newNumbers.contains(number))
                newNumbers.add(number);
        }
        return newNumbers;
    }

    private void checkDistance(){
        //detectLackOfMovement();
        Log.d(TAG, "Checking distance because position of device was updated");
        Map<String, Position> map = GlobalResources.getInstance().getDevices();
        Map<String, Position> realMap = new TreeMap<>();
        realMap.putAll(map);
        realMap.put("ownpos", GlobalResources.getInstance().getDevice().getPosition());

        LinkedHashMap<String, Point> line = DistanceCalculation.getLine(realMap);
        Log.d(TAG, "Line: " + line.toString());

        ArrayList<String> wrongDevices = new ArrayList<>();

        Log.d(TAG, "EXPECTED " + sortedValues.size() + " devices, line returned " + line.size() + " devices.");

        if(line.size() == sortedValues.size()){
            //int i = 0;
            ArrayList<String> realArray = new ArrayList(sortedValues.keySet());
            ArrayList<String> array = new ArrayList(line.keySet());
            Log.d(TAG, "EXPECTED expected values: " + realArray.toString());
            Log.d(TAG, "EXPECTED received values: " + array.toString());

            for(int i = 0; i < array.size(); i++){
                Log.d(TAG, "Element " + i + " is " + array.get(i) + ", expected " + realArray.get(i));
                if(!realArray.get(i).equals(array.get(i))){
                    wrongDevices.add(realArray.get(i));
                    Log.e(TAG, "EXPECTED Element " + i + " is wrong!");
                }
            }

            if(wrongDevices.size() == 0){
                new SoundTask().execute(R.raw.tadaaa);
                levelingUp = true;
                serverSetup(true);
            }
        }
    }

    private static final int secondsBeforeLackOfMovement = 5;
    private static final double distanceForLackOfMovement = 1.5;
    private long lackOfMovementTime = System.currentTimeMillis();
    Map<String, Position> oldPositions = new HashMap<>();

    private void detectLackOfMovement(){
        Map<String, Position> deviceList = GlobalResources.getInstance().getDevices();
        deviceList.put("ownpos", GlobalResources.getInstance().getDevice().getPosition());

        if(oldPositions.size() == 0){
            oldPositions.putAll(deviceList);
            lackOfMovementTime = System.currentTimeMillis();
        }

        for(Map.Entry<String, Position> entry : deviceList.entrySet()){
            Log.d(TAG, "MovementDetection: Device[" + entry.getKey() + "] at " + entry.getValue());
        }

        for(Map.Entry<String, Position> entry : deviceList.entrySet()){
            Log.d(TAG, "MovementDetection: coordinate[" + entry.getValue() + "]");
            double distance = DistanceCalculation.getDistance(oldPositions.get(entry.getKey()), entry.getValue());
            if(distance > distanceForLackOfMovement / 2 || Double.isNaN(distance)){
                lackOfMovementTime = System.currentTimeMillis();
                oldPositions.put(entry.getKey(), entry.getValue());
            }
        }

        if((System.currentTimeMillis() - lackOfMovementTime) / 1000 >= secondsBeforeLackOfMovement){
            Log.d(TAG, "MovementDetection: Lack of movement!");
            ArrayList<String> wrongDevices = getWrongDevices();
            for(String deviceAddress : wrongDevices){
                GlobalResources.getInstance().sendData(deviceAddress, new DataPacket(TYPE_START_SHAKE));
            }
        }else{
            Log.d(TAG, "Devices have moved " + (System.currentTimeMillis() - lackOfMovementTime) / 1000 + " seconds ago.");
            GlobalResources.getInstance().sendData(new DataPacket(TYPE_STOP_SHAKE));
        }
    }

    private ArrayList<String> getWrongDevices(){
        ArrayList<String> wrongDevices = new ArrayList<>();

        Map<String, Position> allDevices = GlobalResources.getInstance().getDevices();
        allDevices.put("ownpos", GlobalResources.getInstance().getDevice().getPosition());

        for(Map.Entry<String, Position> entry : allDevices.entrySet()){
            boolean nextToSomeone = false;
            for(Map.Entry<String, Position> secondEntry : allDevices.entrySet()){
                if(!secondEntry.getKey().equals(entry.getKey())){
                    if(DistanceCalculation.isNextInLineHorizontal(entry.getValue(), secondEntry.getValue(), 1))
                        nextToSomeone = true;
                }
            }
            if(!nextToSomeone){
                Log.d(TAG, "Device " + entry.getKey() + " is alone!");
                wrongDevices.add(entry.getKey());
            }else{
                Log.d(TAG, "Device " + entry.getKey() + " is next to someone.");
            }
        }

        return wrongDevices;
    }

    private LinkedHashMap<String, Integer> sortMapByValues(HashMap<String, Integer> unsortedMap){
        List<Map.Entry<String, Integer>> entries = new LinkedList<>(unsortedMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for(Map.Entry<String, Integer> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        Log.d("SortedMap", sortedMap.toString());
        return sortedMap;
    }

    public void restartActivity(View v){
        GlobalResources.getInstance().sendData(new DataPacket(TYPE_RESTART_GAME));
        recreate();
    }

    public void leaveGame(View v){
        GlobalResources.getInstance().sendData(new DataPacket(TYPE_LEAVE_GAME));
        relaunchApp();
    }

    private void relaunchApp(){
        Intent intent = new Intent(this, FullscreenActivity.class);
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, pendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        //System.exit(0);
        GlobalResources.getInstance().getPatternDetector().destroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}