package com.mlproject.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mlproject.models.UserRecord;

public class DataLoader {

    private static final String EXCEL_PATH =
        "C:\\Users\\aliyo\\Desktop\\kouacademy\\prolab2-2.projem\\MarketSalesKocaeli.xlsx";

    
    public List<UserRecord> loadData() {
        List<UserRecord> dataset = new ArrayList<>();

        try (InputStream inp = new FileInputStream(EXCEL_PATH);
             Workbook wb = new XSSFWorkbook(inp)) {

            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                
                if (row.getCell(9) == null || row.getCell(12) == null) continue;

                try {
                    
                    String clientCode = getCellString(row, 9);
                    if (clientCode.isEmpty()) continue;

                    
                    String category = getCellString(row, 12);
                    if (category.isEmpty()) continue;

                    
                    String gender = "Female";
                    String rawGender = getCellString(row, 17);
                    if (!rawGender.isEmpty()) gender = rawGender;

                    
                    double lineNetTotal = getCellNumeric(row, 7);

                    
                    String brandCode = getCellString(row, 10);
                    if (brandCode.isEmpty()) brandCode = "UNKNOWN";

                    dataset.add(new UserRecord(clientCode, gender, lineNetTotal, brandCode, category));

                } catch (Exception ignored) {
                    
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Yuklenen kayit sayisi: " + dataset.size());
        return dataset;
    }

    private String getCellString(Row row, int col) {
        if (row.getCell(col) == null) return "";
        if (row.getCell(col).getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) row.getCell(col).getNumericCellValue());
        }
        return row.getCell(col).getStringCellValue().trim();
    }

    private double getCellNumeric(Row row, int col) {
        if (row.getCell(col) == null) return 0.0;
        if (row.getCell(col).getCellType() == CellType.NUMERIC) {
            return row.getCell(col).getNumericCellValue();
        }
        try {
            return Double.parseDouble(row.getCell(col).getStringCellValue().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
