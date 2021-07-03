package com.example.callback.drowsiness;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.example.callback.drowsiness.ui.camera.CameraSourcePreview;
import com.example.callback.drowsiness.ui.camera.GraphicOverlay;

import java.io.IOException;


public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;
    private Button end_button;
    private ToggleButton n_mode;
    private TextView tv,tv_1;
    private LinearLayout layout;
    private MediaPlayer mp;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private String key =" ";
    private static final int RC_HANDLE_GMS = 9001;

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public int flag = 0;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        end_button = (Button)findViewById(R.id.button);
        layout = (LinearLayout)findViewById(R.id.topLayout);
        n_mode=(ToggleButton)findViewById(R.id.toggleButton);
        n_mode.setTextOn("N-Mode ON");
        n_mode.setText("N-Mode");
        n_mode.setTextOff("N-Mode OFF");
        n_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mPreview.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"Increase Brightness to maximum for higher accuracy",Toast.LENGTH_LONG).show();
                }
                else
                {
                    mPreview.setVisibility(View.VISIBLE);
                }
            }
        });
        tv = (TextView)findViewById(R.id.textView3);
        tv_1 = (TextView)findViewById(R.id.textView4);
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int c = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(c==0)
        {
            Toast.makeText(getApplicationContext(),"Volume is MUTE",Toast.LENGTH_LONG).show();
        }

        View decorview = getWindow().getDecorView(); //hide navigation bar
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorview.setSystemUiVisibility(uiOptions);

        end_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent next = new Intent(FaceTrackerActivity.this,end.class);
                next.putExtra(key,tv_1.getText());
                next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(next);
                FaceTrackerActivity.this.finish();
                return false;
            }
        });



        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();

    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {

            Toast.makeText(getApplicationContext(),"Dependencies are not yet available. ",Toast.LENGTH_LONG).show();
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(45.0f)
                .build();

    }


    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        stop_alarm();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ALERT")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private void startCameraSource() {


        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void start_alarm()
    {
        stop_alarm();
        mp = MediaPlayer.create(this, R.raw.alarm);
        mp.start();
    }
    public void stop_alarm()
    {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public void alert_box()
    {   start_alarm();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                start_alarm();
                AlertDialog dig;
                dig = new AlertDialog.Builder(FaceTrackerActivity.this)
                        .setTitle("Drowsiness alert")
                        .setMessage("Touch OK to stop the alarm")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                stop_alarm();
                                flag = 0;
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                dig.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        stop_alarm();
                        flag = 0;
                    }
                });
            }
        });


    }



    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }


        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

            int state1,state2=-1;
            long begin,stop;


        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

            if (flag == 0)
            {
                eye_tracking(face);
            }
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
            tv_1.setText("FACE MISSING");
            tv_1.setTextColor(Color.BLUE);
            tv_1.setTypeface(Typeface.DEFAULT_BOLD);

        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }

        private void setText(final TextView text,final String value){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText(value);
                }
            });
        }


        private void eye_tracking(Face face)
        {
            float l = face.getIsLeftEyeOpenProbability();
            float r = face.getIsRightEyeOpenProbability();
            if(l<0.50 && r<0.50)
            {
                /*********temporary**************/
                //alert_box();
                //flag=1;
                state1 = 0;
            }
            else
            {
                state1 = 1;
            }
            if(state1 != state2)
            {
                stop = System.currentTimeMillis();
            }
            else if (state1 == 0 && state2 ==0 ) {
                begin = System.currentTimeMillis();
                if(begin - stop > 1500 )
                {
                    alert_box();
                    flag = 1;
                }
                begin = stop;
            }
            state2 = state1;
            status();
        }

    }
    public void status()
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(flag!=1)
                    {
                        tv_1.setText("ACTIVE");
                        tv_1.setTextColor(Color.GREEN);
                        tv_1.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                    else if(flag==1)
                    {
                        tv_1.setText("DROWSY");
                        tv_1.setTextColor(Color.RED);
                        tv_1.setTypeface(Typeface.DEFAULT_BOLD);
                    }


                }
            });

    }

    @Override
    public void onBackPressed() {
        Intent next = new Intent(FaceTrackerActivity.this,end.class);
        next.putExtra(key,tv_1.getText());
        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(next);
        FaceTrackerActivity.this.finish();
    }


}



