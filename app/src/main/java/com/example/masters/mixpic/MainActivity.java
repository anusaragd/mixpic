package com.example.masters.mixpic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.credenceid.biometrics.BiometricsActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends BiometricsActivity {

    Button srtbutton,sendbutton;
    ImageView Imagefinger;
    TextView statustext;

    String device_id = "";
    Uri uri;

    int i = 0;

    public static int mCaptureType = 0;
    // Capture image type
    public static final int CAPTURE_RAW = 1;
    public static final int CAPTURE_WSQ = 2;

    public static byte[] mImageFP = new byte[153602];
    private static Bitmap mBitmapFP;
    public static byte[] mWsqImageFP;

    private static final int REQUEST_FILE_FORMAT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Imagefinger = (ImageView)findViewById(R.id.imageView);
        statustext = (TextView) findViewById(R.id.status_text);
        srtbutton = (Button)findViewById(R.id.fingerstart);
        srtbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statustext.setText("Grab Fingerprint Start");
                Imagefinger.setImageDrawable(null);
                grabFingerprint();
            }
        });


        sendbutton = (Button)findViewById(R.id.button_send);
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(MainActivity.this, TemplateActivity.class);
//                serverIntent.putExtra("Image",Imagefinger.);
                startActivityForResult(serverIntent, REQUEST_FILE_FORMAT);
            }
        });


    }


    @Override
    public void onFingerprintGrabbed(ResultCode result, Bitmap bitmap,
                                     byte[] iso, String filepath, String status) {

        if(status != null){
            statustext.setText(status);
        }
        if(bitmap != null){
            Imagefinger.setImageBitmap(bitmap);
            SaveImage(bitmap);
//            }

        }

    }

    public void onCloseFingerprintReader(CloseReasonCode reasonCode) {
        statustext.setText("Sensor Closed. Reason=" + reasonCode);
    }

    private void SaveImage(Bitmap finalBitmap) {
        i+=1;
//        String root = Environment.getExternalStorageDirectory().toString();
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES.toString()).toString();
        File myDir = new File(root + "/" + "MixFinger");

        myDir.mkdirs();
//        Random generator = new Random();
//        int n = 10;
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        n = generator.nextInt(n);
//            String fname = "Image-" + timeStamp + "-" +Integer.valueOf(i) + ".jpg";
        String fname = "Image-" + Integer.valueOf(i).toString() + ".bmp";
        File file = new File(myDir, fname );
        if (file.exists())
            file.delete();
        try {
//            FileOutputStream out = new FileOutputStream(file);
//            MyBitmapFile fileBMP = new MyBitmapFile(256, 360, mImageFP);
//            out.write(fileBMP.toBytes());
//            out.close();
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(myDir);
            scanIntent.setData(contentUri);
            sendBroadcast(scanIntent);
        } else {
            final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            sendBroadcast(intent);
        }

//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
//                Uri.parse("file://" + Environment.getExternalStorageDirectory())));
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

    private void SaveImageByFileFormat(String fileFormat, String fileName)
    {
        if( fileFormat.compareTo("WSQ") == 0 )	//save wsq file
        {
            if( mWsqImageFP != null )
            {
                File file = new File(fileName);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(mWsqImageFP, 0, mWsqImageFP.length);	// save the wsq_size bytes data to file
                    out.close();
                    statustext.setText("Image is saved as " + fileName);
                } catch (Exception e) {
                    statustext.setText("Exception in saving file");
                }
            }
            else
                statustext.setText("Invalid WSQ image!");
            return;
        }
        // 0 - save bitmap file
        File file = new File(fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            MyBitmapFile fileBMP = new MyBitmapFile(320, 480, mImageFP);
            out.write(fileBMP.toBytes());
            out.close();
            statustext.setText("Image is saved as " + fileName);
        } catch (Exception e) {
            statustext.setText("Exception in saving file");
        }
    }
}
