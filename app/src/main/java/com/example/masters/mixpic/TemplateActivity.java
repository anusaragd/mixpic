package com.example.masters.mixpic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.credenceid.biometrics.BiometricsActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TemplateActivity extends BiometricsActivity {
    ImageView Imagefinger;
    ImageView picfinger;
    private static Bitmap mBitmapFP;

    Uri uri;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_template);

//            Imagefinger.MainActivity.class = Imagefinger;

            picfinger = (ImageView)findViewById(R.id.picfinger);
            picfinger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent,"Select Picture "),222);
                }
            });
//            picfinger.setImageDrawable(Imagefinger.findViewById(R.id.imageView));




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 111) {  // take photo

                int orientation = -1;
                ExifInterface exif;

                Uri selectedImage = uri;
                getContentResolver().notifyChange(selectedImage, null);
                Bitmap reducedSizeBitmap = getBitmap(uri.getPath()); // convert source picture to bitmap 500KB

                //+
                try {
                    exif = new ExifInterface(selectedImage.getPath());
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:   // 6
                        reducedSizeBitmap = rotateImage(reducedSizeBitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:  // 3
                        reducedSizeBitmap = rotateImage(reducedSizeBitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:  // 8
                        reducedSizeBitmap = rotateImage(reducedSizeBitmap, 270);
                        break;
                    default:   // 0
                        //reducedSizeBitmap = rotateImage(reducedSizeBitmap, 0);
                }
                //-

                ImageView imgView = (ImageView) findViewById(R.id.imageView);
                imgView.setImageBitmap(reducedSizeBitmap);

//                delete.setVisibility(View.VISIBLE);
//                galleryAddPic();
            }
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {  // rotate bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public Bitmap getBitmap(String path) {            // return Bitmap from file path

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 500000; // 500 KB
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            //Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                //Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            //Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
            return b;
        } catch (IOException e) {
            //Log.e("", e.getMessage(), e);
            return null;
        }
    }

    private class MainActivity {
    }
}
