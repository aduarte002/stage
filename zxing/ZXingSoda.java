package me.dm7.barcodescanner.zxing;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;

import edu.umich.oasis.common.IDynamicAPI;
import edu.umich.oasis.common.OASISContext;


public class ZXingSoda implements Parcelable {

    // Barcode data

    // Tag
    private static final String TAG = "ZXing/Soda";

    public ZXingSoda() {

        Log.i(TAG, "ZXingSoda ctor");
    }

    private ZXingSoda(Parcel in) {

    }

    public void handleResult(String result ) {


        if (result !=null) {
            IDynamicAPI toast = (IDynamicAPI) OASISContext.getInstance().getTrustedAPI("toast");

            toast.invoke("showText", "handleResult: " + result, Toast.LENGTH_LONG);
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Log.i(TAG, "Data read and saved correctly.");
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static final Parcelable.Creator<ZXingSoda> CREATOR = new Parcelable.Creator<ZXingSoda>() {

        public ZXingSoda createFromParcel(Parcel in) {
            return new ZXingSoda(in);
        }

        public ZXingSoda[] newArray(int size) {
            return new ZXingSoda[size];
        }
    };
}
