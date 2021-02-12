package com.devarshb.qrcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.IOException;

public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SELECT_PHOTO = 100;
    private boolean s;
    private IntentIntegrator qrscan;
    private TextView textdata;
    private File file;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        Button camScan = findViewById(R.id.scnbtn);
        final Button fileScan = findViewById(R.id.filebtn);
        qrscan = new IntentIntegrator(this);
        textdata = findViewById(R.id.dataview);


        camScan.setOnClickListener(this);

        fileScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if S = true, it will pop-up "Select an image from" window
                s = true;
                //Intent to show the "Select an image from" window
                Intent photoPic = new Intent(Intent.ACTION_PICK);
                photoPic.setType("image/*");
                startActivityForResult(photoPic, SELECT_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (s) {
            if (requestCode == SELECT_PHOTO) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        try {
                            useImage(uri);
                        } catch (IOException | FormatException | ChecksumException | NotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                //if qrcode has nothing in it
                if (result.getContents() == null) {
                    Toast.makeText(ScannerActivity.this, "Nothing Found", Toast.LENGTH_LONG).show();
                } else {
                    /*
                    *This String text will get the result content
                    *and will call the "setTextdata" Function
                    */
                    text = result.getContents();
                    setTextdata();
                    //This "if" will open Dial pad if condition is true
                    if (text.contains("tel:")) {
                        Intent dial = new Intent(Intent.ACTION_DIAL);
                        dial.setData(Uri.parse(text));
                        startActivity(dial);
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void setTextdata() {
        textdata.setText(text);
    }

    //This function is use to read the content of QRCode from Selected File Option
    private void useImage(Uri uri) throws IOException, FormatException, ChecksumException, NotFoundException {
        s = false;
        String contents;
        Bitmap bmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        int[] intArray = new int[bmap.getWidth()*bmap.getHeight()];
        bmap.getPixels(intArray, 0, bmap.getWidth(), 0, 0, bmap.getWidth(), bmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bmap.getWidth(), bmap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        Result result = reader.decode(bitmap);
        contents = result.getText();
        textdata.setText(contents);
        //if there is "tel:" in the content, then dial pad will open up directly.
        if (contents.contains("tel:")) {
            Intent dial = new Intent(Intent.ACTION_DIAL);
            dial.setData(Uri.parse(contents));
            startActivity(dial);
        }
    }

    @Override
    public void onClick(View view) {
        //This is the function to scan QR Code from Camera (ZXing)
        qrscan.setPrompt("Scan a QR/Barcode");
        qrscan.setCameraId(0);
        qrscan.setOrientationLocked(false);
        qrscan.initiateScan();
    }
}