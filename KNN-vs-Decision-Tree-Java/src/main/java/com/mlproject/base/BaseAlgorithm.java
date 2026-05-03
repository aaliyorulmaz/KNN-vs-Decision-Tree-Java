package com.mlproject.base;

import java.util.List;
import com.mlproject.interfaces.IClassifier;
import com.mlproject.models.UserRecord;


public abstract class BaseAlgorithm implements IClassifier {

    
    public double calculateAccuracy(List<UserRecord> testData) {
        int correct = 0;
        for (UserRecord r : testData) {
            if (predict(r).equals(r.getCategory())) correct++;
        }
        return ((double) correct / testData.size()) * 100.0;
    }
}
