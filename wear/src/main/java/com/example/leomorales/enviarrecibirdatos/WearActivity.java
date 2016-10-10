package com.example.leomorales.enviarrecibirdatos;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import java.io.InputStream;

public class WearActivity extends Activity
{

    private TextView mTextView;
    /* mTextView: object that will be used to display the message received from the Android
     * handheld device.
     */
    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = "WearActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub)
            {
                //mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView = (TextView) stub.findViewById(R.id.received_message_input);
            }
        });
        // Establish our connection
        initGoogleApiClient();
    }

    private void initGoogleApiClient()
    {
        /*
         * Establishes a connection between the mobile and wearable
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(new
                GoogleApiClient.ConnectionCallbacks()
                {
                    @Override
                    public void onConnected(Bundle connectionHint)
                    {
                    /*
                     * the onConnected method is called where we set up a listener service
                     * on our MessageApi that will capture any messages our wearable class receives,
                     * and these will be handled by the onMessageReceived method.
                     */
                        Log.d(LOG_TAG, "onConnected: " +
                                connectionHint);
                        Wearable.MessageApi.addListener(
                                mGoogleApiClient, messageListener);
                    }
                    @Override
                    public void onConnectionSuspended(int cause)
                    {
                        Log.d(LOG_TAG, "onConnectionSuspended: " + cause);
                    }
                }
            )
            .addOnConnectionFailedListener(new
                GoogleApiClient.OnConnectionFailedListener()
                {
                    @Override
                    public void onConnectionFailed(ConnectionResult result)
                    {
                        Log.d(LOG_TAG, "onConnectionFailed: "
                               + result);
                    }
                }
            )
            .addApi(Wearable.API)
            .build();
        mGoogleApiClient.connect();

    }

    MessageApi.MessageListener messageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(final MessageEvent messageEvent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (messageEvent.getPath().equalsIgnoreCase("/message")) {
                        Log.i(LOG_TAG, new
                                String(messageEvent.getData()));
                        mTextView.setText(new
                                String(messageEvent.getData()));
                    }
                }
            });
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
