package com.mlproject.models;


public class UserRecord {

    
    private final String clientCode;
    private final String gender;
    private final double lineNetTotal;
    private final String brandCode;
    private final String category;

    
    private int    encodedClientCode;
    private int    encodedGender;         
    private double normalizedLineNetTotal;
    private int    encodedBrandCode;

    public UserRecord(String clientCode, String gender,
                      double lineNetTotal, String brandCode, String category) {
        this.clientCode   = clientCode;
        this.gender       = gender;
        this.lineNetTotal = lineNetTotal;
        this.brandCode    = brandCode;
        this.category     = category;
    }

    
    public String getClientCode()   { return clientCode; }
    public String getGender()       { return gender; }
    public double getLineNetTotal() { return lineNetTotal; }
    public String getBrandCode()    { return brandCode; }
    public String getCategory()     { return category; }

    
    public void setProcessedData(int encodedClientCode, int encodedGender,
                                  double normalizedLineNetTotal, int encodedBrandCode) {
        this.encodedClientCode        = encodedClientCode;
        this.encodedGender            = encodedGender;
        this.normalizedLineNetTotal   = normalizedLineNetTotal;
        this.encodedBrandCode         = encodedBrandCode;
    }

    
    public int    getEncodedClientCode()        { return encodedClientCode; }
    public int    getEncodedGender()            { return encodedGender; }
    public double getNormalizedLineNetTotal()   { return normalizedLineNetTotal; }
    public int    getEncodedBrandCode()         { return encodedBrandCode; }
}
