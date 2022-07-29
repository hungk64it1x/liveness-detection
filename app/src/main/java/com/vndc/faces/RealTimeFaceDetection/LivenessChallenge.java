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
    public static final List<String> challenge_list = Arrays.asList(new String[]{OPEN_MOUTH, SMILE, TURN_LEFT, TURN_RIGHT, BLINK_EYE});
    private static int TOP_LEFT_RATIO = 15;
    private static int TOP_RIGHT_RETIO = 6;
    private static int BOTTOM_LEFT_RATIO = 380;
    private static int BOTTOM_RIGHT_RATIO = 450;
    private static int AREA_RATIO = 45000;

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

    private static boolean CheckArea(Face face){
        Rect box = new Rect(face.getBoundingBox().left,
                face.getBoundingBox().top,
                face.getBoundingBox().right,
                face.getBoundingBox().bottom);
        double s = (box.bottom - box.top) * (box.right - box.left);
        if(s >= AREA_RATIO) return true;

        return false;
    }

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
                if(FaceUtils.IOU(anchorRect, box) > 0.35) return true;
            }

        }

        return false;
    }

    public static int CheckChallengeResult(int k, LivenessProcess livenessProcess, Face face, FaceAlgorithm faceAlgorithm){
        if(k == 0){
            int res = livenessProcess.isMouthOpen(face, faceAlgorithm.getIncreaseOpenMouth());
            return res;
        }
        if(k == 1){
            int res = livenessProcess.isSmile(face);
            return res;
        }
        if(k == 2){
            int res = livenessProcess.isTurnLeft(face, faceAlgorithm.getIncreaseTurnLeft());
            return res;
        }
        if(k == 3){
            int res = livenessProcess.isTurnRight(face, faceAlgorithm.getDecreaseTurnRight());
            return res;
        }
        if(k == 4){
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
