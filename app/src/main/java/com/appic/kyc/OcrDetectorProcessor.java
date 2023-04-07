package com.appic.kyc;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    @Override
    public void release() {
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        SparseArray<TextBlock> items = detections.getDetectedItems();

        if (items.size() != 0) {

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                String capturedString = item.getValue().trim();
                Log.e("itemData", item.getValue());

                String[] str = item.getValue().split(" ");

                for (int j = 0; j < str.length; j++) {

                    Pattern pattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");
                    Matcher matcher = pattern.matcher(str[j]);
                    if (matcher.matches()) {
                        stringBuilder.append(str[j]);
                    }
                    break;
                }
               /*
                if (matcher.matches()){
                    stringBuilder.append(capturedString);
                    break;
                }*/
                break;
            }
            String panNumber = stringBuilder.toString();
            Log.e("panNumber", panNumber);
            // Do something with the extracted PAN number

        }
    }
}

