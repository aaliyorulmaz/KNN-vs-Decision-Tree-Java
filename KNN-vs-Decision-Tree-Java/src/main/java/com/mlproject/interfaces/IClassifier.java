package com.mlproject.interfaces;

import java.util.List;
import com.mlproject.models.UserRecord;


public interface IClassifier {
    void train(List<UserRecord> trainingData);
    String predict(UserRecord user);
}
