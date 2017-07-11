package me.dm7.barcodescanner.zxing;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.umich.oasis.client.OASISConnection;

import me.dm7.barcodescanner.core.BarcodeScannerView;
import me.dm7.barcodescanner.core.DisplayUtils;

import edu.umich.oasis.client.Sealed;
import edu.umich.oasis.client.Soda;


public class ZXingScannerView extends BarcodeScannerView {

    private static final String TAG = "ZXingScannerView";
    private Context context = null;
    OASISConnection oconn = null;
    private int init = 0;
    private int isText ;
    Soda.S0<ZXingScannerViewSoda> ctor = null;
    Sealed<ZXingScannerViewSoda> soda1 = null;
    SharedPreferences prefs = null;
    SharedPreferences.Editor editor = null;
    SharedPreferences.OnSharedPreferenceChangeListener hasChanged = null;

    public interface ResultHandler {
        void handleResult(Sealed<String> finalResult);
    }


    private MultiFormatReader mMultiFormatReader;
    public static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<>();
    private List<BarcodeFormat> mFormats;
    private ResultHandler mResultHandler;

    static {
        ALL_FORMATS.add(BarcodeFormat.AZTEC);
        ALL_FORMATS.add(BarcodeFormat.CODABAR);
        ALL_FORMATS.add(BarcodeFormat.CODE_39);
        ALL_FORMATS.add(BarcodeFormat.CODE_93);
        ALL_FORMATS.add(BarcodeFormat.CODE_128);
        ALL_FORMATS.add(BarcodeFormat.DATA_MATRIX);
        ALL_FORMATS.add(BarcodeFormat.EAN_8);
        ALL_FORMATS.add(BarcodeFormat.EAN_13);
        ALL_FORMATS.add(BarcodeFormat.ITF);
        ALL_FORMATS.add(BarcodeFormat.MAXICODE);
        ALL_FORMATS.add(BarcodeFormat.PDF_417);
        ALL_FORMATS.add(BarcodeFormat.QR_CODE);
        ALL_FORMATS.add(BarcodeFormat.RSS_14);
        ALL_FORMATS.add(BarcodeFormat.RSS_EXPANDED);
        ALL_FORMATS.add(BarcodeFormat.UPC_A);
        ALL_FORMATS.add(BarcodeFormat.UPC_E);
        ALL_FORMATS.add(BarcodeFormat.UPC_EAN_EXTENSION);
    }

    public ZXingScannerView(Context context) {
        super(context);
        initMultiFormatReader();
       // prefs.edit().putInt("isText",6).commit();

    }

    public ZXingScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initMultiFormatReader();
        isText = 2;
    }


    public void setFormats(List<BarcodeFormat> formats) {
        mFormats = formats;
        initMultiFormatReader();

    }

    public void setOconn(OASISConnection oconn) {
        this.oconn = oconn;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        mResultHandler = resultHandler;
    }

    public Collection<BarcodeFormat> getFormats() {
        if (mFormats == null) {
            return ALL_FORMATS;
        }
        return mFormats;
    }

    private void initMultiFormatReader() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, getFormats());
        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mResultHandler == null) {
            return;
        }


        try {

            if (oconn != null) {
                try {
                    //get the TestSoda constructor

                    ctor = oconn.resolveConstructor(ZXingScannerViewSoda.class);

                    //create the object in the sandbox, and get a ref to it
                    soda1 = ctor.call();

                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    int width = size.width;
                    int height = size.height;

                    if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
                        byte[] rotatedData = new byte[data.length];
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++)
                                rotatedData[x * height + height - y - 1] = data[x + y * width];
                        }
                        int tmp = width;
                        width = height;
                        height = tmp;
                        data = rotatedData;
                    }


                    final Sealed<String> finalString = buildLuminanceSource(data, width, height);
                    //get a ref the incrementValue method in TestSoda object that was created in the sandbox


                        if (init == 0) {
                            prefs = getContext().getSharedPreferences("store", Context.MODE_PRIVATE);
                            editor = prefs.edit();
                            hasChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
                                @Override
                                public void onSharedPreferenceChanged (SharedPreferences
                                sharedPreferences, String key){
                                    isText = sharedPreferences.getInt("isText", -1);
                                    Log.i(TAG, "isText1: " + isText);

                                }
                            } ;
                            prefs.registerOnSharedPreferenceChangeListener(hasChanged);
                            init=1;
                        }



                        //   int a = prefs.getInt("isText", 5);


                        // Log.i(TAG, "isText1: " + a);



                    if (isText == 1) {

                        editor.putInt("isText",0);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Stopping the preview can take a little long.
                                // So we want to set result handler to null to discard subsequent calls to
                                // onPreviewFrame.
                                ResultHandler tmpResultHandler = mResultHandler;
                                mResultHandler = null;

                                stopCameraPreview();
                                tmpResultHandler.handleResult(finalString);
                            }
                        });
                    } else {
                        camera.setOneShotPreviewCallback(this);
                    }
                } catch (RuntimeException e) {
                    // TODO: Terrible hack. It is possible that this method is invoked after camera is released.
                    Log.e(TAG, e.toString(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error: " + e);
        }
    }


    public void resumeCameraPreview(ResultHandler resultHandler) {
        mResultHandler = resultHandler;
        super.resumeCameraPreview();
    }

    public Sealed<String> buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview(width, height);
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        Sealed<String> sourceString = null;

        try {
            int intData[] = {width, height, rect.left, rect.top, rect.width(), rect.height()};
            // Log.i(TAG,"extérieur soda: "+ intData[0] + intData[1]+intData[2]+intData[3]);
            Soda.S3<ZXingScannerViewSoda, byte[], int[], String> PlanarData = oconn.resolveInstance(String.class, ZXingScannerViewSoda.class, "Planar", byte[].class, int[].class);


            sourceString = PlanarData.arg(soda1).arg(data).arg(intData).call();
            //soda
        } catch (Exception e) {
        }

        return sourceString;
    }

    public void setInt(int i){
        isText = i;
    }
}
