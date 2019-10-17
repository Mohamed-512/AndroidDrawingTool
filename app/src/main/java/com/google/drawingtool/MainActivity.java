package com.google.drawingtool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    LinearLayout mainLayout;

    RelativeLayout paintingCVLayout;
    Painter painter;
    ImageView clearImage;

    Button save;

    private static final int MAX_IMAGE_SIZE = 128;

    HashMap<Character, Integer> charMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_main_layout);

        paintingCVLayout = findViewById(R.id.main_drawing_cv_layout);
        clearImage = findViewById(R.id.main_drawing_clear);

        save = findViewById(R.id.main_save_button);

        painter = new Painter(this, 15);
        painter.params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        painter.params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        paintingCVLayout.addView(painter);

        initMap();

        clearImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                painter.clear();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                if (painter.somethingDrawn) {
                    Bitmap bitmap = painter.getOnlyDrawingBitmap();
                    bitmap = painter.normalizeImage(bitmap);
                    openSaveDialog(bitmap);
                }
            }
        });


    }

    private void openSaveDialog(final Bitmap bitmap) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.save_dialog);
        ImageView imageView = dialog.findViewById(R.id.image_dialog_image);
        imageView.setImageBitmap(bitmap);
        Button dialogTrain = dialog.findViewById(R.id.image_dialog_train_button);
        final EditText charInput = dialog.findViewById(R.id.image_dialog_edit_text);
        dialogTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chosenChar = charInput.getText().toString();
                if (chosenChar.equals("")) {
                    Snackbar.make(save, "You need to assign a character for the drawn shape",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Bitmap b = painter.resizeImage(bitmap, MAX_IMAGE_SIZE);
                    save(b, charMap.get(chosenChar.charAt(0)));
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public void save(Bitmap b, int charASCII) {
        int fileCount = 1;
        String fileName = charASCII + "_" + fileCount;
        String extension = ".png";

        String root = Environment.getExternalStorageDirectory().toString();
        Log.d("======", root);
        File myDir = new File(root+"" + "/Pictures//Drawn Letters/");
        if(!myDir.exists())
            myDir.mkdir();

        File file = new File(myDir, fileName+extension);

        if(!myDir.exists())
            myDir.mkdir();


        while (file.exists()) {
            fileCount++;
            fileName = charASCII + "_" + fileCount;
            file = new File(myDir, fileName+extension);
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMap() {
        for (int i = 33; i < 127; i++) {
            charMap.put((char) i, i);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }
}
