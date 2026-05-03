package com.mlproject.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mlproject.interfaces.IClassifier;
import com.mlproject.models.UserRecord;

public class Evaluator {

    private static final double TRAIN_RATIO = 0.8;

    private List<UserRecord> trainData = new ArrayList<>();
    private List<UserRecord> testData  = new ArrayList<>();

    
    public void splitData(List<UserRecord> fullDataset) {
        Collections.shuffle(fullDataset);
        int trainSize = (int) (fullDataset.size() * TRAIN_RATIO);
        
        this.trainData = new ArrayList<>(fullDataset.subList(0, trainSize));
        this.testData  = new ArrayList<>(fullDataset.subList(trainSize, fullDataset.size()));
    }

    public List<UserRecord> getTrainData() { return trainData; }
    public List<UserRecord> getTestData()  { return testData;  }

    
    public EvaluationResult evaluateModel(String name, IClassifier model) {
        Runtime runtime = Runtime.getRuntime();

        
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        long start = System.currentTimeMillis();
        model.train(trainData);

        
        int correct = 0;
        Map<String, Map<String, Integer>> confMatrix = new LinkedHashMap<>();

        for (UserRecord r : testData) {
            String predicted = model.predict(r);
            String actual    = r.getCategory();

            if (predicted.equals(actual)) correct++;

            
            confMatrix.computeIfAbsent(actual, k -> new LinkedHashMap<>())
                      .merge(predicted, 1, Integer::sum);
        }

        long elapsed  = System.currentTimeMillis() - start;
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long memKB    = Math.max(0, (memAfter - memBefore) / 1024);

        double accuracy = testData.isEmpty() ? 0.0 : ((double) correct / testData.size()) * 100.0;

        
        List<String> categories = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> row : confMatrix.entrySet()) {
            if (!categories.contains(row.getKey())) categories.add(row.getKey());
            for (String pred : row.getValue().keySet()) {
                if (!categories.contains(pred)) categories.add(pred);
            }
        }
        Collections.sort(categories);

        return new EvaluationResult(name, accuracy, elapsed, memKB, categories, confMatrix);
    }

    
    public static class EvaluationResult {
        public final String algorithmName;
        public final double accuracy;
        public final long   executionTimeMs;
        public final long   memoryUsedKB;
        public final List<String> categories;
        public final Map<String, Map<String, Integer>> confusionMatrix;

        public EvaluationResult(String name, double accuracy, long timeMs, long memKB,
                                List<String> categories,
                                Map<String, Map<String, Integer>> confusionMatrix) {
            this.algorithmName   = name;
            this.accuracy        = accuracy;
            this.executionTimeMs = timeMs;
            this.memoryUsedKB    = memKB;
            this.categories      = categories;
            this.confusionMatrix = confusionMatrix;
        }
    }
}
