package me.dm7.barcodescanner.zxing.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;

import edu.umich.oasis.client.OASISConnection;
import edu.umich.oasis.client.Sealed;
import edu.umich.oasis.client.Soda;
import edu.umich.oasis.common.OASISContext;
import edu.umich.oasis.common.SodaDescriptor;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import me.dm7.barcodescanner.zxing.ZXingSoda;

public class SimpleScannerActivity extends BaseScannerActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private SharedPreferences prefs = null;
    private OASISConnection oconn = null;
    Soda.S0<ZXingSoda> ctor = null;
    Soda.S2<ZXingSoda, String, Void> handle = null;
    private static final String TAG = "ZXing.SimpleScanner";

    private IntentFilter filtre = null;
    private MyReceiver receiver = null;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_simple_scanner);
        setupToolbar();

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        Log.i(TAG,"package :"+ getPackageName());
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        connectToOASIS();

        filtre = new IntentFilter("OASIS.ACTION");

        receiver = new MyReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        connectToOASIS();
        registerReceiver(receiver, filtre);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        unregisterReceiver(receiver);
    }


    public void connectToOASIS() {
        Log.i(TAG, "Binding to OASIS...");
        OASISConnection.bind(this, new OASISConnection.Callback() {
            @Override
            public void onConnect(OASISConnection conn) throws Exception {
                Log.i(TAG, "Bound to OASIS");
                onOASISConnect(conn);
            }
        });
    }

    private void onOASISConnect(OASISConnection conn) {
        oconn = conn;
        mScannerView.setOconn(oconn);

        prefs = getSharedPreferences("store",Context.MODE_MULTI_PROCESS);
        prefs.edit().putInt("isText",7).commit();

        Toast t = Toast.makeText(getApplicationContext(), "connected to OASIS", Toast.LENGTH_SHORT);
        t.show();
    }

    @Override
    public void handleResult(Sealed <String> finalResult) {

        if (oconn != null) {
            try {
                //get the TestSoda constructor
                System.out.println("Ctor..");
                ctor = oconn.resolveConstructor(ZXingSoda.class);
                System.out.println("ok");
                //create the object in the sandbox, and get a ref to it
                Sealed<ZXingSoda> soda1 = ctor.call();

                //get a ref the incrementValue method in TestSoda object that was created in the sandbox
                handle = oconn.resolveInstance(void.class,
                        ZXingSoda.class, "handleResult", String.class);

                handle.arg(soda1).arg(finalResult).call();



            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "error: " + e);
            }

            // Note:
            // * Wait 2 seconds to resume the preview.
            // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
            // * I don't know why this is the case but I don't have the time to figure out.
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScannerView.resumeCameraPreview(SimpleScannerActivity.this);
                }
            }, 2000);
        }
    }





}
