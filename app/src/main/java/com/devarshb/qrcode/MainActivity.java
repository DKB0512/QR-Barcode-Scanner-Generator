package com.devarshb.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private EditText text;
    private Button saveImg;
    private ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.editText);
        Button btn = findViewById(R.id.button);
        Button scn = findViewById(R.id.button2);
        saveImg = findViewById(R.id.saveImage);
        imgView = findViewById(R.id.imageView);
        saveImg.setVisibility(View.GONE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(text.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Text field is Empty!", Toast.LENGTH_SHORT).show();
                }else{
                    QRGenerator();
                }
            }
        });

        scn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scannerActivity = new Intent(MainActivity.this, ScannerActivity.class);
                startActivity(scannerActivity);
            }
        });

        saveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, WRITE_EXTERNAL_STORAGE_CODE);
                    } else {
                        saveImageFunction();
                    }
                }
            }
        });
    }

    //Function to Generate QR code
    private void QRGenerator() {
        String data = text.getText().toString();
        if (data.contains("+91")) {
            data = "tel:" + data;
        }
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imgView.setImageBitmap(bitmap);
            saveImg.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /*
     *This saveImageFunction is use to save the
     * QRCode created from the user text input
     * and will save it in a "QRCodes" directory
     * with it's time in "ddmmyyyy_HHmmss" format
     * with same name as the data_time format
     */

    private void saveImageFunction() {
        Bitmap bitmap2 = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        String time = new SimpleDateFormat("ddmmyyyy_HHmmsss", Locale.getDefault()).format(System.currentTimeMillis());
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/QRCodes");
        dir.mkdirs();
        String imagename = time + ".PNG";
        File file = new File(dir, imagename);
        OutputStream out;

        try {
            out = new FileOutputStream(file);
            bitmap2.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            Toast.makeText(MainActivity.this, "QRCode Saved! Go to File Manager -> QRCode folder", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "Permission enabled", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}