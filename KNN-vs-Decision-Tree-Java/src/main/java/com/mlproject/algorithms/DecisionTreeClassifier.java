package com.mlproject.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mlproject.base.BaseAlgorithm;
import com.mlproject.models.TreeNode;
import com.mlproject.models.UserRecord;


public class DecisionTreeClassifier extends BaseAlgorithm {

    private TreeNode root;
    private final int maxDepth;

    
    private static final int F_CLIENT  = 0; 
    private static final int F_GENDER  = 1; 
    private static final int F_LINENET = 2; 
    private static final int F_BRAND   = 3; 

    public DecisionTreeClassifier(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void train(List<UserRecord> trainingData) {
        System.out.println("DT agac insasi basliyor... Egitim kaydi: " + trainingData.size());
        this.root = buildTree(trainingData, 0);
        System.out.println("DT agac insasi tamamlandi.");
    }

    @Override
    public String predict(UserRecord user) {
        return traverse(user, root);
    }

    

    private TreeNode buildTree(List<UserRecord> data, int depth) {
        if (depth == 0 || depth == 5)
            System.out.println("  buildTree depth=" + depth + " data.size=" + data.size());
        String majority = majorityCategory(data);

        
        if (depth >= maxDepth || data.isEmpty() || isPure(data)) {
            return new TreeNode(majority);
        }

        int    bestFeature = -1;
        double bestValue   = 0.0;
        double bestGini    = Double.MAX_VALUE;

        for (int f = 0; f < 4; f++) {
            double mean  = mean(data, f);
            List<UserRecord> left  = split(data, f, mean, true);
            List<UserRecord> right = split(data, f, mean, false);

            if (left.isEmpty() || right.isEmpty()) continue;

            double gini = weightedGini(left, right);
            if (gini < bestGini) {
                bestGini    = gini;
                bestFeature = f;
                bestValue   = mean;
            }
        }

        if (bestFeature == -1) return new TreeNode(majority);

        List<UserRecord> leftData  = split(data, bestFeature, bestValue, true);
        List<UserRecord> rightData = split(data, bestFeature, bestValue, false);

        return new TreeNode(
            bestFeature, bestValue,
            buildTree(leftData,  depth + 1),
            buildTree(rightData, depth + 1)
        );
    }

    
    private String traverse(UserRecord user, TreeNode node) {
        TreeNode current = node;
        while (!current.isLeaf()) {
            double val = featureValue(user, current.getSplitFeatureIndex());
            current = (val <= current.getSplitValue())
                ? current.getLeftChild()
                : current.getRightChild();
        }
        return current.getPredictedCategory();
    }

    

    private String majorityCategory(List<UserRecord> data) {
        if (data.isEmpty()) return "Unknown";
        Map<String, Integer> counts = new HashMap<>();
        for (UserRecord r : data) counts.merge(r.getCategory(), 1, Integer::sum);
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private boolean isPure(List<UserRecord> data) {
        if (data.isEmpty()) return true;
        String first = data.get(0).getCategory();
        return data.stream().allMatch(r -> r.getCategory().equals(first));
    }

    private double featureValue(UserRecord u, int f) {
        return switch (f) {
            case F_CLIENT  -> u.getEncodedClientCode();
            case F_GENDER  -> u.getEncodedGender();
            case F_LINENET -> u.getNormalizedLineNetTotal();
            case F_BRAND   -> u.getEncodedBrandCode();
            default        -> 0.0;
        };
    }

    private double mean(List<UserRecord> data, int f) {
        return data.stream().mapToDouble(r -> featureValue(r, f)).average().orElse(0.0);
    }

    private List<UserRecord> split(List<UserRecord> data, int f, double threshold, boolean left) {
        return data.stream()
                .filter(r -> left ? featureValue(r, f) <= threshold : featureValue(r, f) > threshold)
                .collect(Collectors.toList());
    }

    private double weightedGini(List<UserRecord> left, List<UserRecord> right) {
        int total = left.size() + right.size();
        return (left.size() * gini(left) + right.size() * gini(right)) / total;
    }

    
    private double gini(List<UserRecord> data) {
        if (data.isEmpty()) return 0.0;
        Map<String, Integer> counts = new HashMap<>();
        for (UserRecord r : data) counts.merge(r.getCategory(), 1, Integer::sum);
        double sumSq = 0;
        for (int c : counts.values()) {
            double p = (double) c / data.size();
            sumSq += p * p;
        }
        return 1.0 - sumSq;
    }
}
