package com.vndc.faces.RealTimeFaceDetection;

import java.util.ArrayList;
import java.util.List;

public class FaceAlgorithm {

    private static final int numOfListIfExceed = 6;
    private List<Float> increaseTurnLeft = new ArrayList<Float>();
    private List<Float> decreaseTurnRight = new ArrayList<Float>();
    private List<Float> increaseOpenMouth = new ArrayList<Float>();

    public List<Float> getIncreaseTurnLeft() {
        return increaseTurnLeft;
    }

    public List<Float> getDecreaseTurnRight() {
        return decreaseTurnRight;
    }

    public List<Float> getIncreaseOpenMouth() {
        return increaseOpenMouth;
    }

    public void setDecreaseTurnRight(List<Float> decreaseTurnRight) {
        this.decreaseTurnRight = decreaseTurnRight;
    }

    public void setIncreaseTurnLeft(List<Float> increaseTurnLeft) {
        this.increaseTurnLeft = increaseTurnLeft;
    }


    public void setIncreaseOpenMouth(List<Float> increaseOpenMouth) {
        this.increaseOpenMouth = increaseOpenMouth;
    }

    public boolean CheckExceedList(){

        if(this.increaseTurnLeft.size() > numOfListIfExceed ||
            this.decreaseTurnRight.size() > numOfListIfExceed ||
                this.increaseOpenMouth.size() > numOfListIfExceed){

            return true;
        }
    return false;
    }

    public void IncreaseValue(){
            int size1 = increaseTurnLeft.size();
            int size2 = decreaseTurnRight.size();
            int size3 = increaseOpenMouth.size();
            if(size1 > numOfListIfExceed) this.increaseTurnLeft = increaseTurnLeft.subList(size1 - numOfListIfExceed, size1);
            if(size2 > numOfListIfExceed) this.decreaseTurnRight = decreaseTurnRight.subList(size2 - numOfListIfExceed, size2);
            if(size3 > numOfListIfExceed) this.increaseOpenMouth = increaseOpenMouth.subList(size3 - numOfListIfExceed, size3);

    }

    public static boolean CheckIncrease(List<Float> list){
        for(int i = 1; i < numOfListIfExceed + 1; i ++){
            if(i == numOfListIfExceed) break;
            if(list.get(i) < list.get(i - 1)) return false;
        }
        return true;
    }

    public static boolean CheckDecrease(List<Float> list){
        for(int i = 1; i < numOfListIfExceed + 1; i ++){
            if(i == numOfListIfExceed) break;
            if(list.get(i) > list.get(i - 1)) return false;
        }
        return true;
    }

     public FaceAlgorithm(){
         increaseTurnLeft = new ArrayList<Float>();
         decreaseTurnRight = new ArrayList<Float>();
         increaseOpenMouth = new ArrayList<Float>();
     }
}
