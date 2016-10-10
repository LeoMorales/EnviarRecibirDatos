package com.example.leomorales.enviarrecibirdatos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Random;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient.
        ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.
        OnConnectionFailedListener;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Button;
import android.widget.EditText;

public class MobileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);
        // Get a pointer to our buttons and textField
        final Button mSendMessageButton = (Button)
            findViewById(R.id.send_message_button);
        final EditText mSendMessageInput = (EditText)
            findViewById(R.id.send_message_input);
        // Set up our hint message for our Text Field
        mSendMessageInput.setHint(R.string.send_message_text);

        // Set up our send message button onClick method handler
        mSendMessageButton.setOnClickListener(new
            View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create a new thread to send the entered message
                    Thread thread = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try {
                                String messageText =
                                      mSendMessageInput.getText().toString();
                                //get a list of all nodes that are currently connected to the device:
                                NodeApi.GetConnectedNodesResult nodes =
                                      Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                                for (Node node : nodes.getNodes()) {
                                    /*
                                     * Once we have the list, we send a message to each of the nodes using Wearable.
                                     * MessageApi.sendMessage that makes reference to GoogleApiClient , the current
                                     * node ID, the path used to determine the type of message being sent, and finally the
                                     * message payload, which is defined as a byte array.
                                     */
                                    MessageApi.SendMessageResult result = Wearable.MessageApi.
                                            sendMessage(mGoogleApiClient,
                                                    node.getId(), "/message",
                                                    messageText.getBytes()).await();
                                    // We use the await property to block our wearable UI until the task completes.
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSendMessageInput.getText().clear();
                                    }
                                });
                            }
                            catch (Exception e) {
                                Log.e(LOG_TAG,
                                        e.getMessage());
                            }
                        }
                    });
                    // Starts our Thread
                    thread.start();
                    Log.d(LOG_TAG, "Message has been sent");
                }
            });

    }
    /*
    Declarar GoogleApiClient:
    that will be responsible for establishing and handling the connection
    between the Android handheld and the Android wearable device.
    */
    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = "MobileActivity";

    // establishes a connection between the mobile and wearable
    private void initGoogleApiClient() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(LOG_TAG, "Connected");
        } else {
            // Creates a new GoogleApiClient object with all
            // connection callbacks
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        /*
                         * This method executes when GoogleApiClient has successfully
                         * established a connection with the wearable device:
                         */
                        Log.d(LOG_TAG, "onConnected: " + connectionHint);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(LOG_TAG, "onConnectionSuspended: "
                                + cause);
                    }
                })
                .addOnConnectionFailedListener(new OnConnectionFailedListener() {
                   @Override
                   public void onConnectionFailed(ConnectionResult result) {
                       Log.d(LOG_TAG, "onConnectionFailed: " + result);
                   }
                })
                .addApi(Wearable.API)
                .build();
                // Make the connection
                mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStart() {
        /*
        * The onStart() method will be called whenever the activity becomes
        * visible and is displayed to the user
        */
        super.onStart();
        initGoogleApiClient();
    }
    @Override
    protected void onStop() {
        /*
         * The onStop()
         * method is called when the activity is no longer visible to the user, which happens
         * when another activity has been resumed, or the current one is being destroyed.
         */
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    @Override
    protected void onResume() {
        /*
         * the onResume() method is called after the
         * onStart() method when the activity is displayed in the foreground.
         */
        super.onResume();
        initGoogleApiClient();
    }

    @Override
    protected void onDestroy() {
        /*
         * The onDestroy() method will be called once the activity has been removed from
         * the activity chain, and is responsible for destroying any memory that has been
         * previously allocated to variables from the memory.
         */
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}