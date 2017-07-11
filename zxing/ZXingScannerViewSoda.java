package me.dm7.barcodescanner.zxing;

import android.os.Parcel;
import android.os.Parcelable;

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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.umich.oasis.common.IDynamicAPI;
import edu.umich.oasis.common.OASISContext;


/**
 * Created by NATHAN on 30/06/17.
 */

public class ZXingScannerViewSoda implements Parcelable {

    // Tag
    private static final String TAG = "ZXing/ScannerSoda";
    private Result rawResult = null;
    private PlanarYUVLuminanceSource source = null;
    private static final List<BarcodeFormat> ALL_FORMATS = new ArrayList<>();
    private static MultiFormatReader mMultiFormatReader;
    private static boolean isInit = false;

    private String text = null;

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

    public ZXingScannerViewSoda() {
        //  Log.i(TAG, "ZXingScannerViewSoda ctor");
    }


    private ZXingScannerViewSoda(Parcel in) {

        this.text = in.readString();
    }


    public String Planar(byte[] data, int[] intData) {
        // Log.i(TAG,"interieur :" + intData[0] + intData[1]+intData[2]+intData[3]);

        try {
            source = new PlanarYUVLuminanceSource(data, intData[0], intData[1], intData[2], intData[3],
                    intData[4], intData[5], false);
        } catch (Exception e) {
        }

        if (!isInit) {
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, ALL_FORMATS);
            mMultiFormatReader = new MultiFormatReader();
            mMultiFormatReader.setHints(hints);
            isInit = true;
        }


        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));


            try {

                rawResult = mMultiFormatReader.decodeWithState(bitmap);

            } catch (ReaderException re) {
                // continue

            } catch (NullPointerException npe) {


                // This is terrible
            } catch (ArrayIndexOutOfBoundsException aoe) {


            } finally {
                mMultiFormatReader.reset();
            }

            if (rawResult == null) {

                LuminanceSource invertedSource = source.invert();
                bitmap = new BinaryBitmap(new HybridBinarizer(invertedSource));
                try {
                    rawResult = mMultiFormatReader.decodeWithState(bitmap);
                } catch (NotFoundException e) {


                    // continue
                } finally {
                    mMultiFormatReader.reset();
                }
            }


        }

        if (rawResult == null) {


            IDynamicAPI setter = (IDynamicAPI)OASISContext.getInstance().getTrustedAPI("setter");

            setter.invoke("sharedPrefs", "isText", 0);
            return null;
        } else {

            IDynamicAPI setter = (IDynamicAPI) OASISContext.getInstance().getTrustedAPI("setter");
            setter.invoke("sharedPrefs", "isText", 1);
            return rawResult.getText();

        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
    }


    public static final Parcelable.Creator<ZXingScannerViewSoda> CREATOR = new Parcelable.Creator<ZXingScannerViewSoda>() {

        public ZXingScannerViewSoda createFromParcel(Parcel in) {
            return new ZXingScannerViewSoda(in);
        }

        public ZXingScannerViewSoda[] newArray(int size) {
            return new ZXingScannerViewSoda[size];
        }
    };
}




