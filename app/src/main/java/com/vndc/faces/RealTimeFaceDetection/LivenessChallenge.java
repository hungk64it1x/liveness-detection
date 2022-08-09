package com.vndc.faces.RealTimeFaceDetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.vndc.faces.ShowCaptureActivity;
import com.vndc.faces.Utils.FaceUtils;
import com.google.mlkit.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LivenessChallenge {

    private static  final int NUMBER_OF_CHALLENGES = 4;
    public static final String OPEN_MOUTH = "Hãy mở miệng";
    public static final String SMILE = "Hãy thử cười";
    public static final String TURN_LEFT = "Hãy quay đầu sang trái";
    public static final String TURN_RIGHT = "Hãy quay đầu sang phải";
    public static final String BLINK_EYE = "Hãy nháy mắt";
    public static final String KEEP_FACE_STRAIGHT = "Hãy giữ khuôn mặt nhìn thẳng";
    private static final double IOU_THRES = 0.24;
    private static final int OPEN_MOUTH_INDEX = 0;
    private static final int SMILE_INDEX = 1;
    private static final int TURN_LEFT_INDEX = 2;
    private static final int TURN_RIGHT_INDEX = 3;
    private static final int BLINK_INDEX = 4;
    private static final int KEEP_FACE_STRAIGHT_INDEX = 5;

    //Khởi tạo danh sách các challengs
    public static final List<String> challenge_list = Arrays.asList(new String[]{OPEN_MOUTH, SMILE, TURN_LEFT, TURN_RIGHT, BLINK_EYE});
    private static int TOP_LEFT_RATIO = 18; // Tăng giá trị này làm giảm top left                                                                                                                                                                                                                                                                                                                                                                      ;
    private static int TOP_RIGHT_RATIO = 18; // Tăng giá trị này làm giảm top right
    private static int BOTTOM_LEFT_RATIO = 460; // Tăng giá trị này làm tăng bottom left
    private static int BOTTOM_RIGHT_RATIO = 460; // Tăng giá trị này làm tăng bottom righr;
    private static int AREA_RATIO = 38000;
    // Khởi tạo hình chữ nhật
    private static Rect InitRect(View view){
        int view_top_left = 0;
        int view_top_right = 0;
        int view_bottom_left = view_top_left + view.getWidth();
        int view_bottom_right = view_top_right + view.getHeight();

        int rect_top_left = (int) (view.getWidth() / TOP_LEFT_RATIO);
        int rect_top_right = (int) (view.getHeight() / TOP_RIGHT_RATIO);
        int rect_bottom_left = rect_top_left + BOTTOM_LEFT_RATIO;
        int rect_bottom_right = rect_top_right + BOTTOM_RIGHT_RATIO;
        return new Rect(rect_top_left, rect_top_right, rect_bottom_left, rect_bottom_right);
    }
    /*
    Hàm kiểm tra xem diện tích bbox bao quanh khuôn mặt có thỏa mãn một lớn hơn một ngưỡng không
     */
    private static boolean CheckArea(Face face){
        Rect box = new Rect(face.getBoundingBox().left,
                face.getBoundingBox().top,
                face.getBoundingBox().right,
                face.getBoundingBox().bottom);
        double s = (box.bottom - box.top) * (box.right - box.left);
        if(s >= AREA_RATIO) return true;

        return false;
    }
    /*
    Hàm kiểm tra khuôn mặt có nằm trong hình chứ nhật
    Params: Face face, View view
    Return: true nếu đúng false nếu sai
     */
    private static boolean CheckFaceInRect(Face face, View view){
//        Rect anchorRect = new Rect(40, 100, 500, 500);
        Rect anchorRect = InitRect(view);
        Log.v("Anchor rect", String.valueOf(anchorRect));
        Rect box = new Rect(face.getBoundingBox().left,
                face.getBoundingBox().top,
                face.getBoundingBox().right,
                face.getBoundingBox().bottom);
        if(box.left > anchorRect.left && box.top > anchorRect.top && box.right < anchorRect.right && box.bottom < anchorRect.bottom){
            return true;
        }
        return false;
    }
    /*
    Hàm kiểm tra xem khuôn mặt có phù hợp để bắt đầu hay không (kết hợp các điều kiện)
     */
    public static boolean CheckFaceInCircle(Face face, View view) {
        Rect anchorRect = InitRect(view);
//        Rect anchorRect = new Rect(40, 100, 500, 500);

        Rect box = new Rect(face.getBoundingBox().left,
                face.getBoundingBox().top,
                face.getBoundingBox().right,
                face.getBoundingBox().bottom);
        if(face.getBoundingBox().left < 0 || face.getBoundingBox().right < 0 || face.getBoundingBox().top < 0 || face.getBoundingBox().bottom < 0) return false;
        Log.v("rect lef", String.valueOf(face.getBoundingBox().left));
        Log.v("rect top", String.valueOf(face.getBoundingBox().top));
        Log.v("rect right", String.valueOf(face.getBoundingBox().right));
        Log.v("rect bottom", String.valueOf(face.getBoundingBox().bottom));
        Log.v("IOU", String.valueOf(FaceUtils.IOU(anchorRect, box)));
        if(CheckFaceInRect(face, view)){
            if(CheckArea(face)){
                if(FaceUtils.IOU(anchorRect, box) > IOU_THRES) return true;
            }

        }

        return false;
    }
    /*
    Hàm kiểm tra kết quả của các challenges
     */

    public static int CheckChallengeResult(int k, LivenessProcess livenessProcess, Face face, FaceAlgorithm faceAlgorithm){
        if(k == OPEN_MOUTH_INDEX){
            int res = livenessProcess.isMouthOpen(face, faceAlgorithm.getIncreaseOpenMouth());
            return res;
        }
        if(k == SMILE_INDEX){
            int res = livenessProcess.isSmile(face);
            return res;
        }
        if(k == TURN_LEFT_INDEX){
            int res = livenessProcess.isTurnLeft(face, faceAlgorithm.getIncreaseTurnLeft());
            return res;
        }
        if(k == TURN_RIGHT_INDEX){
            int res = livenessProcess.isTurnRight(face, faceAlgorithm.getDecreaseTurnRight());
            return res;
        }
        if(k == BLINK_INDEX){
            int res = livenessProcess.isEyeBlink(face);
            return res;
        }
        if(k == KEEP_FACE_STRAIGHT_INDEX){
            int res = livenessProcess.isKeepFaceStraight(face);
            return res;
        }
        return 0;
    }

    public static String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public static void Challenge(Face face, Button btGuide, Button btContinue, LivenessProcess livenessProcess, Pass pass, List<Integer> randomList, TextView txtDone, ProgressBar progressBar, FaceAlgorithm faceAlgorithm, PreviewView previewView){
        Bitmap bitmap;
        if(pass.data == 0){
            btGuide.setText(challenge_list.get(randomList.get(0)));
            int x = CheckChallengeResult(randomList.get(0), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(20);
            }


        }
        if(pass.data == 1){
            btGuide.setText(challenge_list.get(randomList.get(1)));
            int x = CheckChallengeResult(randomList.get(1), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(20);

            }
        }

        if(pass.data == 2){
            btGuide.setText(challenge_list.get(randomList.get(2)));
            int x = CheckChallengeResult(randomList.get(2), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(20);

            }
        }
        if(pass.data == 3){
            btGuide.setText(challenge_list.get(randomList.get(3)));
            int x = CheckChallengeResult(randomList.get(3), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(20);

            }
        }

        if(pass.data == 4){
            btGuide.setText(KEEP_FACE_STRAIGHT);
            int x = CheckChallengeResult(KEEP_FACE_STRAIGHT_INDEX, livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                bitmap = previewView.getBitmap();
                progressBar.incrementProgressBy(20);
                btContinue.setVisibility(View.VISIBLE);

                btContinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri imageUri = getImageUri(view.getContext(), bitmap);
                        Intent intent = new Intent(view.getContext(), ShowCaptureActivity.class);
                        intent.putExtra("imageUri", imageUri.toString());
                        view.getContext().startActivity(intent);
                    }
                });
                txtDone.setVisibility(View.VISIBLE);
                btGuide.clearAnimation();
                btGuide.setVisibility(View.INVISIBLE);
            }
        }
        if(pass.data == 5){
//            Bitmap bitmap = Bitmap.createBitmap(previewView.getWidth(), previewView.getHeight(), Bitmap.Config.ARGB_8888);

//            btContinue.setVisibility(View.VISIBLE);
//
//            btContinue.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Uri imageUri = getImageUri(view.getContext(), bitmap);
//
//                    Intent intent = new Intent(view.getContext(), ShowCaptureActivity.class);
//                    intent.putExtra("imageUri", imageUri.toString());
//                    view.getContext().startActivity(intent);
//                }
//            });

//            txtDone.setVisibility(View.VISIBLE);
//            btGuide.clearAnimation();
//            btGuide.setVisibility(View.INVISIBLE);
        }
    }

}
