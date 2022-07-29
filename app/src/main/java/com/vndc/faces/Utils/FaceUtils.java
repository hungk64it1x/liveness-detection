package com.vndc.faces.Utils;

import android.graphics.Rect;

public class FaceUtils {

    public static double IOU(Rect boxA, Rect boxB){
        double xA = Math.max(boxA.left, boxB.left);
        double yA = Math.max(boxA.top, boxB.top);
        double xB = Math.min(boxA.right, boxB.right);
        double yB = Math.min(boxA.bottom, boxB.bottom);
        double interArea = (xB - xA) * (yB - yA);
        double boxAArea = (boxA.right - boxA.left) * (boxA.bottom - boxA.top);
        double boxBArea = (boxB.right - boxB.left) * (boxB.bottom - boxB.top);
        double iou = interArea / (float)(boxAArea + boxBArea - interArea);
        return iou;
    }
}
