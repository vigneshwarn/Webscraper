package org.alpha.VinDataScraper;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class VinScraper1 {

    public static void main(String[] args) throws InterruptedException {
        fetchDataAndWriteData("", false, false, true);
    }

    private static void fetchDataAndWriteData(String baseUrl, boolean headLessMode, boolean writeDataToDB, boolean writeDataToXLfile) throws InterruptedException {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (headLessMode) chromeOptions.addArguments("--headless");
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(baseUrl);
        long skip = 0L;
        int loopCount = 1;
        long totalCount = 0L;
        boolean countUpdated = false;

        for (int i = 0; i < loopCount; i++) {
            HashMap<String, VinDTO> listVinData = new HashMap<>();
            driver.get(baseUrl + "/cars/api/search/run?skip=" + skip + "&take=100&radius=radius-nationwide&shipping=-1&sort=best-match&count=1");
            skip = skip + 100;
            String text = driver.findElement(By.xpath("//body")).getText();
            JSONObject jsonObject = new JSONObject(text);

            if (i == 0) {
                totalCount = jsonObject.getLong("totalCount");
                System.out.println("totalCount = " + totalCount);
                System.out.print("Fetching data...");
            }

            if (!countUpdated) {
                loopCount = Math.toIntExact(totalCount / 100);
                countUpdated = true;
            }

            JSONArray items = jsonObject.getJSONArray("items");

            String vin = "";
            int year = -1;
            String make = "";
            String model = "";
            for (int j = 0; j < items.length(); j++) {
                vin = items.getJSONObject(j).getString("vin");
                year = items.getJSONObject(j).getInt("year");
                make = items.getJSONObject(j).getString("make");
                model = items.getJSONObject(j).getString("model");
                listVinData.put(vin, VinDTO.builder().vin(vin).model(model).year(year).make(make).build());
            }
            System.out.println("Retrieved " + skip + " of " + totalCount);
            writeData(writeDataToDB, writeDataToXLfile, skip, listVinData);
        }
        driver.quit();
    }

    private static void writeData(boolean writeDataToDB, boolean writeDataToXLfile, long skip, HashMap<String, VinDTO> listVinData) {
        if (writeDataToDB) {
            System.out.println("Storing " + skip + " vins into database...");
            try {
                storeData(listVinData);
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
            }
            System.out.println(skip + " Data stored in database");
            System.out.println("=========================================================");
        }

        if (writeDataToXLfile) {
            System.out.println("Storing " + skip + " vins into XL file...");
            try {
                writeDataToExcel(listVinData, "vin_data.xlsx");
            } catch (Exception e) {
                System.out.println("Error Occurred while  writing data to xl: " + e.getMessage());
            }
        }
    }

    private static Connection initDBConnections() throws SQLException {
        String dbMySqlURL = "jdbc:mysql://localhost:3306/webscrapers?allowPublicKeyRetrieval=true&useSSL=false";
        String user = "root";
        String pass = "root@123";
        return DriverManager.getConnection(dbMySqlURL, user, pass);
    }

    private static boolean storeData(HashMap<String, VinDTO> data) throws SQLException {
        Connection conn = initDBConnections();

        String insertQuery = "INSERT INTO vin_data (vin,year,make,model) VALUES (?,?,?,?)";
        PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);

        for (Map.Entry<String, VinDTO> entry : data.entrySet()) {
            VinDTO vinData = entry.getValue();
            preparedStatement.setString(1, vinData.getVin());
            preparedStatement.setInt(2, vinData.getYear());
            preparedStatement.setString(3, vinData.getMake());
            preparedStatement.setString(4, vinData.getMake());
            preparedStatement.addBatch();
        }

        preparedStatement.executeBatch();
        return false;
    }

    public static void writeDataToExcel(HashMap<String, VinDTO> data, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vin Data");

        String[] headers = {"vin", "year", "make", "model"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        File tmpDir = new File(fileName);

        for (Map.Entry<String, VinDTO> entry : data.entrySet()) {
            VinDTO value = entry.getValue();

            Row row = sheet.createRow(sheet.getLastRowNum()+1);
            row.createCell(0).setCellValue(value.getVin());
            row.createCell(1).setCellValue(value.getVin());
            row.createCell(2).setCellValue(value.getMake());
            row.createCell(3).setCellValue(value.getModel());
        }

        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Excel file created successfully!");
    }


}
