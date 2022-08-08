package com.vndc.faces.RealTimeFaceDetection;

import android.graphics.Rect;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vndc.faces.Utils.FaceUtils;
import com.google.mlkit.vision.face.Face;

import java.util.Arrays;
import java.util.List;

public class LivenessChallenge {
    private static  final int NUMBER_OF_CHALLENGES = 4;
    public static final String OPEN_MOUTH = "Hãy mở miệng";
    public static final String SMILE = "Hãy thử cười";
    public static final String TURN_LEFT = "Hãy quay đầu sang trái";
    public static final String TURN_RIGHT = "Hãy quay đầu sang phải";
    public static final String BLINK_EYE = "Hãy nháy mắt";
    private static final double IOU_THRES = 0.35;
    private static final int OPEN_MOUTH_INDEX = 0;
    private static final int SMILE_INDEX = 1;
    private static final int TURN_LEFT_INDEX = 2;
    private static final int TURN_RIGHT_INDEX = 3;
    private static final int BLINK_INDEX = 4;

    //Khởi tạo danh sách các challengs
    public static final List<String> challenge_list = Arrays.asList(new String[]{OPEN_MOUTH, SMILE, TURN_LEFT, TURN_RIGHT, BLINK_EYE});
    private static int TOP_LEFT_RATIO = 15;
    private static int TOP_RIGHT_RETIO = 6;
    private static int BOTTOM_LEFT_RATIO = 380;
    private static int BOTTOM_RIGHT_RATIO = 450;
    private static int AREA_RATIO = 45000;

    // Khởi tạo hình chữ nhật
    private static Rect InitRect(View view){
        int view_top_left = 0;
        int view_top_right = 0;
        int view_bottom_left = view_top_left + view.getWidth();
        int view_bottom_right = view_top_right + view.getHeight();
        int rect_top_left = (int) (view.getWidth() / TOP_LEFT_RATIO);
        int rect_top_right = (int) (view.getHeight() / TOP_RIGHT_RETIO);
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
//        Log.v("rect lef", String.valueOf(face.getBoundingBox().left));
//        Log.v("rect top", String.valueOf(face.getBoundingBox().top));
//        Log.v("rect right", String.valueOf(face.getBoundingBox().right));
//        Log.v("rect bottom", String.valueOf(face.getBoundingBox().bottom));
//        Log.v("IOU", String.valueOf(FaceUtils.IOU(anchorRect, box)));
        if(CheckFaceInRect(face, view)){
            if(CheckArea(face)){
                if(FaceUtils.IOU(anchorRect, box) > IOU_THRES) return true;
            }

        }

        return false;
    }
    /*
    Hàm kiểu tra kết quả của các challenges
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
        return 0;
    }


    public static void Challenge(Face face, Button btGuide, LivenessProcess livenessProcess, Pass pass, List<Integer> randomList, TextView txtDone, ProgressBar progressBar, FaceAlgorithm faceAlgorithm){

        if(pass.data == 0){
            btGuide.setText(challenge_list.get(randomList.get(0)));
            int x = CheckChallengeResult(randomList.get(0), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(25);
            }
        }
        if(pass.data == 1){
            btGuide.setText(challenge_list.get(randomList.get(1)));
            int x = CheckChallengeResult(randomList.get(1), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(25);

            }
        }

        if(pass.data == 2){
            btGuide.setText(challenge_list.get(randomList.get(2)));
            int x = CheckChallengeResult(randomList.get(2), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(25);

            }
        }
        if(pass.data == 3){
            btGuide.setText(challenge_list.get(randomList.get(3)));
            int x = CheckChallengeResult(randomList.get(3), livenessProcess, face, faceAlgorithm);
            if(x == 1){
                pass.data += 1;
                progressBar.incrementProgressBy(25);

            }
        }

        if(pass.data == 4){
            txtDone.setVisibility(View.VISIBLE);
            btGuide.clearAnimation();
            btGuide.setVisibility(View.INVISIBLE);
        }
    }

}
