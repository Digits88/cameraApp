package com.kulak.izabel.cameraapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.kulak.izabel.cameraapp.ColorBlobDetector;
import com.kulak.izabel.cameraapp.ColorHintInterface;
import com.kulak.izabel.cameraapp.ColorPickerFragment;
import com.kulak.izabel.cameraapp.ColorPickerOwner;
import com.kulak.izabel.cameraapp.DynamicImageView;
import com.kulak.izabel.cameraapp.LeftMenu;
import com.kulak.izabel.cameraapp.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class PhotoActivity extends Activity implements View.OnTouchListener, ColorPickerOwner, ColorHintInterface {

    private static final int SELECT_PICTURE = 1;
    private final LeftMenu leftMenu = new LeftMenu(R.id.drawer_layout_photo_activity);

    private DynamicImageView imageView;
    private String TAG = "PhotoActivity";


    private CharSequence appTitle = "";
    private static boolean COLOR_PICKER_ON;
    private String mCurrentPhotoPath;
    private Bitmap bitmap;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {

                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private ColorPickerFragment rightFragment;
    private Scalar pickedColor = null;
    private ColorHintFragment colorHintFragment;
    private LinkedList<Scalar> colorHintColors = new LinkedList<>();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        leftMenu.changedLeftMenuConfiguration(newConfig);
        rightFragment.onConfigurationChanged(newConfig);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        setContentView(R.layout.activity_photo);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        imageView = (DynamicImageView) findViewById(R.id.selected_photo);
        imageView.setOnTouchListener(PhotoActivity.this);
        leftMenu.initializeLeftMenu(getResources(), getApplicationContext(), this);
        rightFragment = new ColorPickerFragment(R.id.drawer_layout_photo_activity, this);

        colorHintFragment = new ColorHintFragment(/*this, new LinkedList<Scalar>()*/);


        final ImageButton backButton = (ImageButton) findViewById(R.id.back);

        backButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              Intent changeActivity = new Intent(PhotoActivity.this, StartActivity.class);
                                              PhotoActivity.this.startActivity(changeActivity);
                                          }
                                      }
        );

        final ImageButton pickAPhotoButton = (ImageButton) findViewById(R.id.pick_photo_button);
        pickAPhotoButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                    startActivityForResult(intent, SELECT_PICTURE);

                                                }
                                            }
        );

        final ImageButton pickColorButton = (ImageButton) findViewById(R.id.pick_color);
        //pickColorButton.setLayoutParams(new LinearLayout.LayoutParams(btnSize, btnSize));
        pickColorButton.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   // if (savedInstanceState == null) {

                                                   // }

                                               }
                                           }
        );

        mRgba = new Mat(imageView.getHeight(), imageView.getWidth(), CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255, 255, 0);
        setDisplay();

    }

    private void setDisplay() {
        Display display = ((WindowManager)this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)
        {
            Log.d(TAG, "--->0");
            DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
            float scale = displayMetrics.density;
            float scaleHeigth = displayMetrics.heightPixels;
            float scaleWidth = displayMetrics.widthPixels;
            Log.d(TAG, "scaleHeight: " + scaleHeigth);
            Log.d(TAG, "scaleWidth: " + scaleWidth);
            Log.d(TAG, "density: " + displayMetrics.density);
            Log.d(TAG, "densityDpi: " + displayMetrics.densityDpi);
            Log.d(TAG, "scaledDensity: " + displayMetrics.scaledDensity);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        appTitle = title;
        getActionBar().setTitle(appTitle);
    }

    private boolean mIsColorSelected = false;
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        closeColorPickerFragment();

        if (colorIsPicked()) {
            Log.d(TAG, "Color is picked");
            getBitmapFromDrawable();

            int cols = mRgba.cols();
            int rows = mRgba.rows();

            int xOffset = (imageView.getWidth() - cols) / 2;
            int yOffset = (imageView.getHeight() - rows) / 2;

            int x = (int) event.getX() - xOffset;
            int y = (int) event.getY() - yOffset;

            Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

            if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

            org.opencv.core.Rect touchedRect = new org.opencv.core.Rect();

            touchedRect.x = (x > 4) ? x - 4 : 0;
            touchedRect.y = (y > 4) ? y - 4 : 0;

            touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
            touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
            Mat touchedRegionRgba = mRgba.submat(touchedRect);
            Mat touchedRegionHsv = new Mat();
            selectColor(touchedRect, touchedRegionRgba, touchedRegionHsv);
            Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
            mIsColorSelected = true;

            releaseUnusedRegions(touchedRegionRgba, touchedRegionHsv);

            List<MatOfPoint> contours = detectContours();

            Mat orig = mRgba.clone();

            pickedColor = ColorPickerFragment.getLastPicked();

            colorBitmap(x, y, contours, orig);
            Utils.matToBitmap(mRgba, bitmap);
            updateImageView();
        }
        return false; // don't need subsequent touch events
    }

    private void colorBitmap(int x, int y, List<MatOfPoint> contours, Mat orig) {
        Imgproc.drawContours(orig, contours, -1, pickedColor, -1);
        Core.addWeighted(orig, 0.4, mRgba, 0.6, 0.0, mRgba);
        Imgproc.circle(mRgba, new Point(x, y), 3, new Scalar(255, 255, 255), -1);
    }

    private void updateImageView() {
        imageView.setImageBitmap(bitmap);
        Log.d(TAG, "Invalidate");
        imageView.invalidate();
        Log.d(TAG, "Invalidating done");
    }

    private void selectColor(org.opencv.core.Rect touchedRect, Mat touchedRegionRgba, Mat touchedRegionHsv) {
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        colorHintColors.add(mBlobColorRgba);
        colorHintFragment = new ColorHintFragment(/*this, colorHintColors*/);
        mDetector.setHsvColor(mBlobColorHsv);
    }

    private List<MatOfPoint> detectContours() {
        mDetector.process(mRgba);
        return mDetector.getContours();
    }

    private void releaseUnusedRegions(Mat touchedRegionRgba, Mat touchedRegionHsv) {
        touchedRegionRgba.release();
        touchedRegionHsv.release();
    }

    private void getBitmapFromDrawable() {
        Drawable imgDrawable = ((DynamicImageView) imageView).getDrawable();
        bitmap = ((BitmapDrawable) imgDrawable).getBitmap();
        Utils.bitmapToMat(bitmap, mRgba);
    }

    private boolean colorIsPicked() {
        return ColorPickerFragment.getLastPicked() != null;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    //UPDATED
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
            try {
                pickAPhoto(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        COLOR_PICKER_ON = false;
        if (bitmap != null)
            bitmap.recycle();
    }

    private void pickAPhoto(Intent data) throws FileNotFoundException {
        Uri selectedImage = data.getData();
        mCurrentPhotoPath = selectedImage.getPath();
        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
        //imageView.setImageURI(selectedImage);
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        Log.d(TAG, "targetW " + targetW);
        Log.d(TAG, "targetH " + targetH);
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        Log.d(TAG, "A ");
        bitmap = BitmapFactory.decodeStream(imageStream, new Rect(), bmOptions);

        //BitmapFactory.decodeStream(imageStream, new Rect(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        Log.d(TAG, "photoW " + photoW);
        Log.d(TAG, "photoH " + photoH);
        int scaleFactor = 1;
        if (photoH > targetH) {
            scaleFactor = Math.round((float) photoH / (float) targetH);
        }
        int expectedWidth = photoW / scaleFactor;
        if (expectedWidth > targetW) {
            scaleFactor = Math.round((float) photoW / (float) targetW);
        }
        // Determine how much to scale down the image
        //scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        Log.d(TAG, "scaleFactor-" + scaleFactor);
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 1;
        bmOptions.inPurgeable = true;
        Log.d(TAG, "B ");
        InputStream imageStream2 = getContentResolver().openInputStream(selectedImage);

        bitmap = BitmapFactory.decodeStream(imageStream2, new Rect(), bmOptions);

        //imageView.setImageBitmap(bitmap);
        imageView.setImageDrawable(new BitmapDrawable(bitmap));

    }


/*

    Display display = ((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

    List<Camera.Size> sizes = getmSupportedPreviewSizes();

    if(display.getRotation() == Surface.ROTATION_0)
    {
        Log.d(TAG, "--->0");

        updateCameraSupportedSizeAndRotation(width, height, parameters, sizes, 90);
    }

    if(display.getRotation() == Surface.ROTATION_90)
    {
        Log.d(TAG, "--->90");

        updateCameraSupportedSizeAndRotation(height, width, parameters, sizes, 0);
    }

    if(display.getRotation() == Surface.ROTATION_180)
    {
        Log.d(TAG, "--->180");

        updateCameraSupportedSizeAndRotation(width, height, parameters, sizes, 0);
    }

    if(display.getRotation() == Surface.ROTATION_270)
    {
        Log.d(TAG, "--->270");

        updateCameraSupportedSizeAndRotation(height, width, parameters, sizes, 180);

    }
    previewCamera();
*/

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        leftMenu.synchronizeLeftMenuState();
        rightFragment.synchronizeMenuState();
    }

    private void closeRightPaneIfItIsOpen() {
        closeColorPickerFragment();
    }

    public void openColorPickerFragment() {
        if (COLOR_PICKER_ON == false) {
            if (getFragmentManager().findFragmentById(R.id.fragment_place) == null) {
                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack("A")
                        .add(R.id.fragment_place, rightFragment)
                        .commit();
            }
            COLOR_PICKER_ON = true;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        COLOR_PICKER_ON = false;

    }

    @Override
    public void closeColorPickerFragment() {
        rightFragment.closeDrawer();

    }

    @Override
    public void startColorHintFragment() {
        if (getFragmentManager().findFragmentById(R.id.color_hint_fragment) == null) {
            getFragmentManager()
                    .beginTransaction()
                    .addToBackStack("A")
                    .add(R.id.color_hint_fragment_place, colorHintFragment)
                    .commit();
        }
    }
}
