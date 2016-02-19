#Location Aware Smartphones
The goal of this project is to enable a new way of interaction using smartphones - their location. Location awareness of smartphones has been a hot topic for quite some time, but this project is aimed at a new way of localization and, more importantly, a new user interaction method.

This project contains a few modules. The primary module is the [`libLocationAware`](libLocationAware) module. This module enables the localization of smart devices. This localization method has two requirements: a smart device with a front-facing camera and a pattern mounted on the ceiling above the playing area. By analysing the camera frames, a smartphone can determine its position relative to the pattern. The library also allows multiple devices to connect to each other over Bluetooth. By sending their coordinates to other devices, calculations can be done and actions can be taken.

![pattern](docs/pattern.png)

To allow developers to easily develop applications with this library, without having to come into contact with the library too much, some [Fragments](http://developer.android.com/guide/components/fragments.html) are provided. Of course modification and enhancements to the library are also allowed. To demonstrate the use of the fragments, some demo applications were also made. Some of these applications were also user-tested with preschoolers to test if the user interaction was intuitive. The results of these tests were good to say the least.

####Accuracy and speed
The accuracy and speed has been tested using [Medion P4013](http://aldi.medion.com/md98332/be_nl/) devices. These devices have a 0.3 MP camera and a 1 GHz dual-core processor. We can assume that if we can run this smoothly on these low-end devices, it can also run on newer hardware.

**Accuracy**
Using these devices, we can reach a maximum accuracy of 0.364 cm per pixel, if the smartphone is at a height of 225 cm of the pattern. In practice we can see values that come pretty close to that value, largely depending on the calibration of the smartphone.

**Speed**
For the communication speed, we can see Bluetooth round-trips ranging from 20 ms up to 200 ms. This is probably due to when the Bluetooth thread is allowed to run on the device. This was tested in real-world usage while the pattern detection was running. This is an average of 110 ms for a round-trip, so ~55 ms for a single-way communication.

For the pattern detection an average of 100 ms was needed to grab the camera frame, process it, and calculate the final coordinate of the device. Together with ~55 ms single-way communication, we achieve a speed of ~150 ms from the start of the image processing on the client up to receiving the coordinate on the server device, which can be considered fast enough.

##Building Instructions

Use 'Import Project' in Android Studio (Don't use 'Open Project', unless you have no 'Import' option)
Point to the repository directory (Default under Windows: 'Documents/Github/location_aware_smartphones')

Make sure to run 'Sync Project with gradle files' (Tools->Android) so that the .iml files are regenerated.

##Project Modules Explained

* [*libLocationAware*](libLocationAware): The entire library for image processing, communication and calculations. This is a Library Module and cannot run separately on a device.
* [*opencvtester*](opencvtester): A small demo application that can run on a single device that show the steps in the image processing.
* [*arrow*](arrow): A demo application that requires two smart devices. Each device will have an arrow that points to the other device. If you tap the arrow on one of the devices, the Bluetooth speed will also be tested and a round-trip time will be displayed.
* [*animal_farm*](animal_farm): A bigger application that makes use of some special calculation parts of the library. This game requires at least three smart devices. In this game, one device will get a picture of an animal on the farm and all the other devices are sounds. When one of those devices is tapped, it will play the sound. The objecive is to place the correct sound next to the animal. This game also features error detection, so when a wrong sound is placed next to the animal, an error-sound will be played.
* [*rank_em*](rank_em): Another bigger application with some additional unique features. This game requires 2 to 5 devices. The objective of this game is to place all the devices in the correct order, depending on the content that is on them. But beware - each level contains new figures!
* [*openCVLibrary2410*](openCVLibrary2410): The OpenCV library that is needed for the image processing.

##Coupling demo applications to library code

####Step 1: Depencencies

Edit the build.gradle file and make sure the dependencies are correct:

```gradle
dependencies {
        // ...
        compile project(':libLocationAware')
}
```

####Step 2: Start communication _(Optional)_
In order to start the communication, the advised way is to use the provided fragments [`be.groept.emedialab.ClientFragment`](libLocationAware/src/main/java/be/groept/emedialab/fragments/ClientFragment.java) and [`be.groept.emedialab.ServerFragment`](libLocationAware/src/main/java/be/groept/emedialab/fragments/ServerFragment.java). These fragments provide both the setup of the connections which are then maintained by the library, as well as a simple interface. The `ClientFragment` displays a list of paired devices. The user must click on the name of the server device so that it can connect to the server. The `ServerFragment` displays a list of connected devices as well as a button that is enabled once enough devices are connected. This amount of devices can be passed on to the fragment with two parameters, `minNumberOfDevices` and `maxNumberOfDevices`. When the button is pushed on the server, the fragment will send a signal to the parent (your) Activity that all devices are connected. Then the developer can go to the next activity. To register this signal, the activity must implement the [`PartyReadyListener`](libLocationAware/src/main/java/be/groept/emedialab/fragments/PartyReadyListener.java).

```Java
public class ArrowConnectionActivity extends AppCompatActivity implements PartyReadyListener {

	// Minimum and maximum amount of connected devices
    public static final int minNumberOfDevices = 1;
    public static final int maxNumberOfDevices = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_activity);
        View mContentView = findViewById(R.id.relativeLayout);

		// Start either the ClientFragment or the ServerFragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Bundle bundle = getIntent().getExtras();
        if(bundle.getBoolean("create")) {
            Bundle b = new Bundle();
            b.putInt("minNumberOfDevices", minNumberOfDevices);
            b.putInt("maxNumberOfDevices", maxNumberOfDevices);
            ServerFragment serverFragment = new ServerFragment();
            serverFragment.setArguments(b);
            b.putString("Theme", "HMT");
            fragmentTransaction.add(R.id.fragment_container, serverFragment);
        } else {
            Bundle b = new Bundle();
            ClientFragment client = new ClientFragment();
            client.setArguments(b);
            b.putString("Theme", "HMT");
            fragmentTransaction.add(R.id.fragment_container, client);
        }
        fragmentTransaction.commit();
    }

	// Called on both client and server when the 'Start' button is clicked on the server
    @Override
    public void partyReady() {
        startActivity(new Intent(this, MyGameActivity.class));
    }

}
```

_Note: This step is optional, as not all applications require communications. Applications that run on a single device are perfectly conceivable, for example our 'opencvtester'-demo app._

####Step 3: Run pattern detection
To start and run the pattern detection, run the pattern detector in a new [`Runnable`](http://developer.android.com/reference/java/lang/Runnable.html):

```Java
final Activity activity = this;
Runnable runnable = new Runnable() {
    @Override
    public void run() {
        new RunPatternDetector(activity);
    }
};
runnable.run();
```

This will automatically start the runnable. This will frequently analyse camera frames for the pattern and calculate the position of the device. It will also, if the device is a client, automatically send the new position to the server.

####Step 4: Accessing the data
If all previous steps were followed correctly, the server will now know the position of all the connected devices at all times. But when is this position updated, and where can you access it?

A hub where all information is stored on the device is the [`be.groept.emedialab.util.GlobalResources`]((libLocationAware/src/main/java/be/groept/emedialab/util/GlobalResources.java). This contains input- and outputbuffers, location data, ... Polling this data would however be a nightmare. To prevent the need for polling, the `GlobalResources` has a [`Handler`](http://developer.android.com/reference/android/os/Handler.html) that it notifies of certain updates. To receive these updates, simply create a `Handler` in your application and pass this to the `GlobalResources` when your `Activity` is created:

```Java
Handler handler = new Handler(Looper.getMainLooper()) {
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == DataHandler.DATA_TYPE_COORDINATES) {
			// Take action
		} else if (msg.what == DataHandler.DATA_TYPE_DEVICE_DISCONNECTED) {
			// Take more action
		} else if (msg.what == DataHandler.DATA_TYPE_OWN_POS_UPDATED) {
			// Even more action needs to be taken!
		}
	}
};

@Override
protected void onCreate(Bundle savedInstanceState) {
	// ...
	GlobalResources.getInstance().setHandler(handler);
	// ...
}
```
        
For a full list of available data types that the library sends, please read the Data Types section.

####Step 5: Sending your own data _(Optional)_
You can now successfully receive data sent by the library. Of course you might also want to send your own types of data, depending on the application. The server is the only device that standardly gets all the locations, so he still needs to send commands (or coordinates) to other devices so they know what to do. This approach was taken to minimize computing power and battery usage on most devices. Since most calculations only need to be done once, the server can efficiently calculate what needs to be calculated and send the appropriate command to the correct device.
To send this custom data, again `GlobalResources` provides some functions. Using the `sendData()`-command, a client can send data to the server and the server can send data to all devices or one specific device. Behind the scenes, this function also sends the commands descried in the Data Type section.
There is one requirement for this custom data however - it needs to be [`Serializable`](http://developer.android.com/reference/java/io/Serializable.html). This is to allow faster transfer speeds than using regular objects. To receive this data, the `GlobalResources` handler is notified with `msg.what == DataHandler.DATA_TYPE_DATA_PACKET`. If this is received, data is available on the `inputBuffer` of `GlobalResources`, and can be read out using `GlobalResources.getInstance().readData()` _(Note: it is possible that the data packet was read out by a previous `.readData()` call, so always check that the read-in data is not `null`.)_.

_Note: This step is optional, since if you create a single-smartphone game you won't be needing to send any data around!_

####Step 6: Create a fun game!
Congratulations, you have now done all the steps required for making a full-fledged multi-player location-aware game! Feel free to provide PR's and submit issues, so this library can be optimized and used by more people!


##Data Types
This library provides several data types that are notified to the `Handler` of the `GlobalResources`. This can be filtered by the `msg.what`-fields shown below. Some of these also have a `msg.obj`-field containing an object. Here is a short summary of the data types:

* *DataHandler.DATA_TYPE_COORDINATES*: Received *only* by server when one of the connected devices has sent an update of its location. *Object*: `Position` of the device.
* *DataHandler.DATA_TYPE_OWN_POS_UPDATED*: Received by both client and server when the position of the device itself is updated. *Object*: `Position` of the current device.
* *DataHandler.DATA_TYPE_DATA_PACKET*: Received by both client and server when data is received. This data needs to be collected from the `inputBuffer` of said device. *Object*: null. _(Note: this does not directly provide the received object since this buffer is also used by other parts of the library, thus requiring to check the input buffer)_
* *DataHandler.DATA_TYPE_DEVICE_CONNECTED*: Received *only* by server when a new device is connected. *Object*: [`BluetoothDevice`](http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html) of the connected device.
* *DataHandler.DATA_TYPE_DEVICE_DISCONNECTED*: Received *only* by server when a client is disconnected. *Object*: [`BluetoothDevice`](http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html) of the disconnected device. This will be adjusted in the future to also be received by the client when the server disconnects.
* More to come!

##Installing OpenCVManager

To enable the power of [OpenCV](http://opencv.org/), each Android device must have the OpenCVManager installed. Since this is going to be developed on multiple smartphones at the same time for testing, most probably these are development smartphones that aren't logged in with a Google Account. However, without a Google Account you do not have access to the Google Play Store. For this reason, the [OpenCVManager apk](http://sourceforge.net/projects/opencvlibrary/files/opencv-android/) must be sideloaded.

Use the following commands to push the apk to the smartphone.
(Make sure you run this in the platform tools directory (default `Users/Yourname/Local/Android/sdk/platform-tools`) or add this dir to your environment variables)

```Bash
cd OpenCV-2.4.10-android-sdk/apk/
adb install OpenCV_2.4.10_Manager_2.19_armeabi.apk
```

A similar approach can be used to install the apk on a simulator. The apk directory contains different .apk files for different architectures. Make sure you pick the right one.

##Important note
This project is still a work in progress. An utmost important step in achieving a high accuracy is camera calibration. At time of writing, this is however still not implemented in the library. Currently this was done using some measurement data, and the calculations done in MatLab. Like this, camera offsets on both x- and y-axis were calculated with all our development smartphones (in our case the Medion P4013). These offsets were then hardcoded in `be.groept.emedialab.math.CameraConstants`.
