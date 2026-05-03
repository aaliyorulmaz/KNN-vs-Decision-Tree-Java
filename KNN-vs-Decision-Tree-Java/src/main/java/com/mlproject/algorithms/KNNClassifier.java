package com.mlproject.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mlproject.base.BaseAlgorithm;
import com.mlproject.models.UserRecord;


public class KNNClassifier extends BaseAlgorithm {

    private List<UserRecord> trainingData;
    private final int k;

    public KNNClassifier(int k) {
        this.k = k;
    }

    @Override
    public void train(List<UserRecord> trainingData) {
       
        this.trainingData = trainingData;
    }

    @Override
    public String predict(UserRecord target) {
        
        List<double[]> distances = new ArrayList<>();
        for (int i = 0; i < trainingData.size(); i++) {
            double dist = euclideanDistance(target, trainingData.get(i));
            distances.add(new double[]{dist, i});
        }

        
        distances.sort((a, b) -> Double.compare(a[0], b[0]));

        
        Map<String, Integer> votes = new HashMap<>();
        int limit = Math.min(k, distances.size());
        for (int i = 0; i < limit; i++) {
            String cat = trainingData.get((int) distances.get(i)[1]).getCategory();
            votes.merge(cat, 1, Integer::sum);
        }

        
        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    
    private double euclideanDistance(UserRecord u1, UserRecord u2) {
        double dc = u1.getEncodedClientCode()      - u2.getEncodedClientCode();
        double dg = u1.getEncodedGender()           - u2.getEncodedGender();
        double dl = u1.getNormalizedLineNetTotal()  - u2.getNormalizedLineNetTotal();
        double db = u1.getEncodedBrandCode()        - u2.getEncodedBrandCode();
        return Math.sqrt(dc*dc + dg*dg + dl*dl + db*db);
    }
}
