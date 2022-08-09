package com.vndc.faces.RealTimeFaceDetection;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceDetector;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Pass {
    int data;
    Pass(int dataValue){
        data = dataValue;
    }}

class TimeOut{
    int count;
    TimeOut(int value){
        count = value;
    }
}

public class MLKitFacesAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitFacesAnalyzer";
    private Button btGuide;
    private Button btContinue;
    private TextView txtDone;
    private FaceDetector faceDetector;
    private PreviewView previewView;

    private Bitmap bitmap;
    private Canvas canvas;
    private View mainView;
    private ProgressBar progressBar;
    private ImageCapture imageCapture;
    private Paint dotPaint, linePaint;
    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    private CameraSelector.LensFacing lens;
    private Pass pass = new Pass(0);
    private TimeOut timeout = new TimeOut(0);
    private List<Integer> chs = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
    private final int TIMEOUT = 20;

    private FaceAlgorithm faceAlgorithm = new FaceAlgorithm();

    MLKitFacesAnalyzer(PreviewView previewView, Button btGuide, Button btContinue, TextView txtDone, ProgressBar progressBar, View mainView) {
        this.previewView = previewView;
        this.btGuide = btGuide;
        this.btContinue = btContinue;
        this.txtDone = txtDone;
        this.progressBar = progressBar;
        this.mainView = mainView;
        Collections.shuffle(this.chs);
    }


    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {

            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            initDrawingUtils(image);
            initDetector();
            detectFaces(image, imageProxy, this.chs);
        }
        else{
            Log.v("Media image", "NULL");
        }

    }

    /*
    Khởi tạo bản vẽ trên bitmap
     */
    private void initDrawingUtils(InputImage image) {
        bitmap = Bitmap.createBitmap(previewView.getWidth(), previewView.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        dotPaint = new Paint();
        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setStrokeWidth(2f);
        dotPaint.setAntiAlias(true);
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);

    }
    /*
    Khởi tạo detector trong mlkit
     */
    private void initDetector() {
        FaceDetectorOptions detectorOptions = new FaceDetectorOptions
                .Builder()
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .enableTracking()
                .build();
        faceDetector = FaceDetection.getClient(detectorOptions);

    }
    /*
    Hàm thực hiện realtime action
     */
    private void detectFaces(InputImage fbImage, ImageProxy imageProxy, List<Integer> chs) {

        faceDetector
                .process(fbImage)

                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                if(faces.isEmpty()) {
                                    btGuide.setText("Không tìm thấy khuôn mặt");
                                    timeout.count = 0;
                                    pass.data = 0;
                                    progressBar.setProgress(0);
                                    Collections.shuffle(chs);
                                }
                                else{
                                    ProcessFaces(faces, chs);
                                }
                                imageProxy.close();
                                }

                            })

                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                imageProxy.close();
                            }
                        });
    }
    /*
    Thực hiện xử lý các tác vụ chính
     */
    public int ProcessFaces(List<Face> faces, List<Integer> chs) {
        LivenessProcess livenessProcess = new LivenessProcess();
        if(timeout.count > TIMEOUT){
            timeout.count = 0;
            pass.data = 0;
            progressBar.setProgress(0);
            Collections.shuffle(chs);
        }
        for (Face face: faces){
            if(!LivenessChallenge.CheckFaceInCircle(face, mainView)){
                btGuide.setText("Đảm bảo khuôn mặt của bạn vừa khung hình");
                timeout.count += 1;
//                pass.data = 0;
//                progressBar.setProgress(0);
//                Collections.shuffle(chs);

            }


            else {
                if(faceAlgorithm.CheckExceedList()){
                    faceAlgorithm.IncreaseValue();
                }
                if(faces.size() >= 2) {
                    btGuide.setText("Có nhiều hơn một khuôn mặt trong khung hình");
                    pass.data = 0;
                    progressBar.setProgress(0);
                    Collections.shuffle(chs);
                }
//                Log.v("YAW", String.valueOf(face.getHeadEulerAngleZ()));
//                Log.v("PITCH", String.valueOf(face.getHeadEulerAngleY()));
//                Log.v("ROLL", String.valueOf(face.getHeadEulerAngleX()));
                timeout.count = 0;
                LivenessChallenge.Challenge(face, btGuide, btContinue, livenessProcess, pass, chs, txtDone, progressBar, faceAlgorithm, previewView);
            }
        }

        return 0;
    }

    private void faceBoundingBox(List<Face> faces) {
        for (Face face : faces) {

            Rect box = new Rect((int) translateX(face.getBoundingBox().left),
                    (int) translateY(face.getBoundingBox().top),
                    (int) translateX(face.getBoundingBox().right),
                    (int) translateY(face.getBoundingBox().bottom));
            canvas.drawText(String.valueOf(face.getTrackingId()),
                    translateX(face.getBoundingBox().centerX()),
                    translateY(face.getBoundingBox().centerY()),
                    linePaint);
            Log.i("okkkk", "top: " + (int) translateY(face.getBoundingBox().top)
                    + "left: " + (int) translateX(face.getBoundingBox().left)
                    + "bottom: " + (int) translateY(face.getBoundingBox().bottom)
                    + "right: " + (int) translateX(face.getBoundingBox().right));

            Log.i("dsadasd", "top: " + face.getBoundingBox().top
                    + " left: " + face.getBoundingBox().left
                    + " bottom: " + face.getBoundingBox().bottom
                    + " right: " + face.getBoundingBox().right);

            canvas.drawRect(box, linePaint);
        }
    }

    private float translateY(float y) {
        return y * heightScaleFactor;
    }

    private float translateX(float x) {
        float scaledX = x * widthScaleFactor;
            return canvas.getWidth() - scaledX;
    }
}