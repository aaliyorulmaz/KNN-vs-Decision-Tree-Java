package com.mlproject.models;


public class TreeNode {

    private final boolean isLeaf;
    private final String  predictedCategory;  

    private final int      splitFeatureIndex;
    private final double   splitValue;
    private final TreeNode leftChild;
    private final TreeNode rightChild;

    
    public TreeNode(String predictedCategory) {
        this.isLeaf            = true;
        this.predictedCategory = predictedCategory;
        this.splitFeatureIndex = -1;
        this.splitValue        = 0.0;
        this.leftChild         = null;
        this.rightChild        = null;
    }

    
    public TreeNode(int splitFeatureIndex, double splitValue,
                    TreeNode leftChild, TreeNode rightChild) {
        this.isLeaf            = false;
        this.predictedCategory = null;
        this.splitFeatureIndex = splitFeatureIndex;
        this.splitValue        = splitValue;
        this.leftChild         = leftChild;
        this.rightChild        = rightChild;
    }

    public boolean  isLeaf()               { return isLeaf; }
    public String   getPredictedCategory() { return predictedCategory; }
    public int      getSplitFeatureIndex() { return splitFeatureIndex; }
    public double   getSplitValue()        { return splitValue; }
    public TreeNode getLeftChild()         { return leftChild; }
    public TreeNode getRightChild()        { return rightChild; }
}
