package com.vndc.faces.RealTimeFaceDetection;

import android.graphics.PointF;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;

import java.util.List;



public class LivenessProcess {
    public final double MOUTH_AR_THRESH = 0.5; // Tham số kiểm tra xem miệng có đang mở không
    public final double BLINK_THRESH = 0.5; // Tham số kiểm tra xem mắt có nháy hay không
    public final double TURN_RIGHT_THRESH = -14; // Tham số kiểm tra xem ngưỡng có vượt quá để gọi là quay sang phải
    public final double TURN_LEFT_THRESH = 14; // Tham số kiểm tra xem ngưỡng có vượt quá để gọi là quay sang trái
    public final double SMILE_THRESH = 0.4; // Tham số kiểm tra xem người dùng có đang cười hay không

    /*
    Hàm tính khoảng cách giữa 2 điểm
    Params: (x1,y1), (x2,y2)
    Return: Khoảng cách theo euclide
     */
    public static double calculateDistanceBetweenPoints(
            double x1,
            double y1,
            double x2,
            double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    /*
    Hàm kiểm tra xem miệng có đang mở hay không
    Params: Face face, List<Float> increaseMouthOpen: danh sách điểm tăng trong các point ở miệng
    Return: 1 nếu đúng 0 nếu sai
     */
    public int isMouthOpen(Face face, List<Float> increaseMouthOpen) {
        if(face != null){

            try{
                List<PointF> lowerLipBottom = face.getContour(FaceContour.LOWER_LIP_TOP).getPoints();
                List<PointF> upperLipBottom = face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();
                PointF pt1 = lowerLipBottom.get(0);
                PointF pt2 = upperLipBottom.get(3);

                PointF pt3 = upperLipBottom.get(6);
                PointF pt4 = lowerLipBottom.get(8);

                PointF pt5 = lowerLipBottom.get(6);
                PointF pt6 = lowerLipBottom.get(3);

                double A = LivenessProcess.calculateDistanceBetweenPoints(pt3.x, pt3.y, pt5.x, pt5.y);
                double B = LivenessProcess.calculateDistanceBetweenPoints(pt2.x, pt2.y, pt6.x, pt6.y);
                double C = LivenessProcess.calculateDistanceBetweenPoints(pt1.x, pt1.y, pt4.x, pt4.y);
                double mar = (A + B) / (2.0 * C);
                increaseMouthOpen.add((float)mar);
                if (mar > MOUTH_AR_THRESH) {
                    if(FaceAlgorithm.CheckIncrease(increaseMouthOpen)){
                        return 1;
                    }

                } else return 0;
            }
            catch (Exception exception) {
                return 0;
            }

        }
        return 0;

    }
    /*
    Hàm kiểm tra xem mắt có nháy hay không
    Params: Face face
    Return: 1 nếu đúng 0 nêu sai
     */
    public int isEyeBlink(Face face){
        if(face != null){
            int count = 0;
            try{
                if((face.getLeftEyeOpenProbability() < BLINK_THRESH || face.getRightEyeOpenProbability() < BLINK_THRESH) && isFaceStraight(face) == 1){
                    count += 1;
                }
            }catch (Exception e){
                count = 0;
            }

            if(count == 1) return 1;
            return 0;
        }
        return 0;
    }

    /*
    Hàm kiểm tra xem có đang quay sang trái hay không
    Params: Face face, List<Float> increaseLeft (danh sách tăng dần của trục ngang nếu quay sang trái thì tọa độ ngang tăng dần)
    Return: 1 nếu đúng 0 nếu sai
     */
    public int isTurnLeft(Face face, List<Float> increaseLeft){
        if(face != null){
            increaseLeft.add(face.getHeadEulerAngleY());
            if(face.getHeadEulerAngleY() > TURN_LEFT_THRESH){
                if(FaceAlgorithm.CheckIncrease(increaseLeft)){
                    return 1;
                }
            }
        }
        return 0;
    }
    /*
    Hàm kiểm tra xem có đang quay sang phải hay không
    Params: Face face, List<Float> decreaseRight (danh sách giảm dần của trục ngang nếu quay sang phải thì tọa độ ngang giảm dần)
    Return: 1 nếu đúng 0 nếu sai
     */
    public int isTurnRight(Face face, List<Float> decreaseRight){
        if(face != null){
            decreaseRight.add(face.getHeadEulerAngleY());
            if(face.getHeadEulerAngleY() < TURN_RIGHT_THRESH){
                if(FaceAlgorithm.CheckDecrease(decreaseRight)){
                    return 1;
                }
            }
        }
        return 0;
    }
    /*
    Hàm kiểm tra xem có đang cười hay không
    Params: Face face
    Return: 1 nếu đúng 0 nếu sai
     */
    public int isSmile(Face face){
        if(face != null) {
            try{
                if (face.getSmilingProbability() > SMILE_THRESH) {
                    return 1;
                }
            }
            catch (Exception e){
                return 0;
            }

        }
        return 0;
    }

    public int isKeepFaceStraight(Face face){
        if(face != null){
            try{
                if (face.getSmilingProbability() < 0.4) {
                    double yaw = face.getHeadEulerAngleZ();
                    double pitch = face.getHeadEulerAngleY();
                    double roll = face.getHeadEulerAngleX();
                    if(yaw >= -3 && yaw <= 2 &&  pitch >= -1.8 && pitch <= 1.8 && roll >= -1.8 && roll <= 1.8
                            && face.getLeftEyeOpenProbability() > 0.6
                            && face.getRightEyeOpenProbability() > 0.6){
                        return 1;
                    }
                }
            }
            catch (Exception e){
                return 0;
            }

        }
        return 0;
    }

    private int isFaceStraight(Face face){
        if(face != null){
            double yaw = face.getHeadEulerAngleZ();
            double pitch = face.getHeadEulerAngleY();
            double roll = face.getHeadEulerAngleX();
            if(yaw >= -2.5 && yaw <= 2.5 &&  pitch >= -2.5 && pitch <= 2.5 && roll >= -2.5 && roll <= 2.5) {
                return 1;
            }
        }
        return 0;
    }


}
