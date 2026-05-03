package com.mlproject.utils;

import java.util.HashMap;
import java.util.List;
import com.mlproject.models.UserRecord;


public class PreProcessor {

    private final HashMap<String, Integer> clientCodes = new HashMap<>();
    private final HashMap<String, Integer> brandCodes  = new HashMap<>();

    private int nextClientCode = 1;
    private int nextBrandCode  = 1;

    private double minLineNet =  Double.MAX_VALUE;
    private double maxLineNet = -Double.MAX_VALUE;

    public void processAll(List<UserRecord> dataset) {
        
        for (UserRecord r : dataset) {
            if (r.getLineNetTotal() < minLineNet) minLineNet = r.getLineNetTotal();
            if (r.getLineNetTotal() > maxLineNet) maxLineNet = r.getLineNetTotal();
        }

        
        for (UserRecord r : dataset) {
            
            clientCodes.putIfAbsent(r.getClientCode(), nextClientCode++);
            int encClient = clientCodes.get(r.getClientCode());

            
            int encGender = r.getGender().equalsIgnoreCase("Male") ? 1 : 0;

            
            double normLineNet = (maxLineNet != minLineNet)
                ? (r.getLineNetTotal() - minLineNet) / (maxLineNet - minLineNet)
                : 0.0;

            
            brandCodes.putIfAbsent(r.getBrandCode(), nextBrandCode++);
            int encBrand = brandCodes.get(r.getBrandCode());

            r.setProcessedData(encClient, encGender, normLineNet, encBrand);
        }
    }
}
