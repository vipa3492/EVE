package victor.smartlockapp;

//Imports for speech recognition - delete any overrides if needed (e.g. android.speech.SpeechRecognizer)

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends Activity implements RecognitionListener {

    /************************
     * Declarations for speech recognition
     ************************/
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";

    // Keyword we are looking for to activate menu
    /* THESE MUST BE ADDED TO menu.gram UNDER THE assets > en-us-ptm FOLDER */
    private static final String CALL_NURSE = "call nurse";
    private static final String LIGHT_ON = "turn light on";
    private static final String LIGHT_OFF = "shut light off";
    private static final String CHANNEL_UP = "channel up";

    private SpeechRecognizer recognizer;

    /************************
     * Declarations for Bluetooth communication
     ************************/
    //Memeber Fields
    public BluetoothAdapter btAdapter = null;
    public BluetoothSocket btSocket1 = null;
    public BluetoothSocket btSocket2 = null;
    public BluetoothSocket btSocket3 = null;
    public OutputStream outStream1 = null;
    public OutputStream outStream2 = null;
    public OutputStream outStream3 = null;

    // UUID service - This is the type of Bluetooth device that the BT module is
    // It is very likely yours will be the same, if not google UUID for your manufacturer
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module
    public String addressBT1 = null;
    public String addressBT2 = null;
    public String addressBT3 = null;

    /************************
     * Called when MainActivity is first created
     ************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /******** Initialize speech recognition ********/
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");

        // Recognizer initialization is a time-consuming and it involves IO, so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    ((TextView) findViewById(R.id.caption_text)).setText("Please speak a command from the list below");
                    recognizer.startListening(KWS_SEARCH);
                    // Populate screen with the commands above
                    ((TextView) findViewById(R.id.textView1)).setText(CALL_NURSE);
                    ((TextView) findViewById(R.id.textView2)).setText(LIGHT_ON);
                    ((TextView) findViewById(R.id.textView3)).setText(LIGHT_OFF);
                    ((TextView) findViewById(R.id.textView4)).setText(CHANNEL_UP);
                }
            }
        }.execute();

        /******** Initialize bluetooth ********/
        //getting the bluetooth adapter value and calling checkBTstate function
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
        // MAC-address of Bluetooth module
        addressBT1 = ((MyApplication) this.getApplication()).getDEVICE_ADDRESS_1();
        addressBT2 = ((MyApplication) this.getApplication()).getDEVICE_ADDRESS_2();
        addressBT3 = ((MyApplication) this.getApplication()).getDEVICE_ADDRESS_3();
    }


    /************************
     * Methods for speech recognition
     ************************/
    public void resetClick(View v) {
        ((TextView) findViewById(R.id.result_text)).setText(" ");
        recognizer.startListening(KWS_SEARCH);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        /* If captured word matches keyphrase, display and stop recognizer */
        /* Click the Restart button to start the recognizer again */
        String text = hypothesis.getHypstr();
        if (text.equals(CALL_NURSE)) {
            ((TextView) findViewById(R.id.result_text)).setText(CALL_NURSE);
            recognizer.stop();
        } else if (text.equals(LIGHT_ON)) {
            ((TextView) findViewById(R.id.result_text)).setText(LIGHT_ON);
            recognizer.stop();
        } else if (text.equals(LIGHT_OFF)) {
            ((TextView) findViewById(R.id.result_text)).setText(LIGHT_OFF);
            recognizer.stop();
        } else if (text.equals(CHANNEL_UP)) {
            ((TextView) findViewById(R.id.result_text)).setText(CHANNEL_UP);
            recognizer.stop();
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                /*.setKeywordThreshold(1e-45f)*/
                .setKeywordThreshold(1e-45f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(KWS_SEARCH, menuGrammar);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
    }


    /************************
     * Methods for Bluetooth communication
     ************************/
    public void testBT1(View v) {
        sendData1("1");
    }

    public void testBT2(View v) {
        sendData2("1");
    }

    public void testBT3(View v) {
        sendData3("1");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initialize Bluetooth connections
        connectBT(addressBT1, addressBT2, addressBT3);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Close Bluetooth sockets when app is closed
        if (btSocket1.isConnected()) {
            try {
                btSocket1.close();
            } catch (IOException e2) {
            }
        }
        if (btSocket2.isConnected()) {
            try {
                btSocket2.close();
            } catch (IOException e2) {
            }
        }
        if (btSocket3.isConnected()) {
            try {
                btSocket3.close();
            } catch (IOException e2) {
            }
        }
    }

    //takes the UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    //same as in device list activity
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    // Method to send data
    private void connectBT(final String address1, final String address2, final String address3) {
        //Communication is time consuming so run on a new thread
        Runnable runnable = new Runnable() {
            public void run() {
                // Connect to Bluetooth
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                // Set up a pointer to the remote device using its address.
                BluetoothDevice device1 = btAdapter.getRemoteDevice(address1);
                BluetoothDevice device2 = btAdapter.getRemoteDevice(address2);
                BluetoothDevice device3 = btAdapter.getRemoteDevice(address3);
                //Attempt to create a bluetooth socket for comms
                try {
                    btSocket1 = device1.createRfcommSocketToServiceRecord(MY_UUID);
                    btSocket2 = device2.createRfcommSocketToServiceRecord(MY_UUID);
                    btSocket3 = device3.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e1) {
                }
                // Before connecting, it's good practice to cancel any discovery even if not requested
                btAdapter.cancelDiscovery();
                // Establish connections
                while (!btSocket1.isConnected()) {
                    try {
                        btSocket1.connect();
                    } catch (IOException e) {
                    }
                }
                while (!btSocket2.isConnected()) {
                    try {
                        btSocket2.connect();
                    } catch (IOException e) {
                    }
                }
                while (!btSocket3.isConnected()) {
                    try {
                        btSocket3.connect();
                    } catch (IOException e) {
                    }
                }
                // Create a data stream so we can talk to its Bluetooth receiver
                try {
                    outStream1 = btSocket1.getOutputStream();
                    outStream2 = btSocket2.getOutputStream();
                    outStream3 = btSocket3.getOutputStream();
                } catch (IOException e) {
                }
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    // Methods to send data
    private void sendData1(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            //attempt to place data on the outstream to the BT device
            outStream1.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendData2(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            //attempt to place data on the outstream to the BT device
            outStream2.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendData3(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            //attempt to place data on the outstream to the BT device
            outStream3.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}