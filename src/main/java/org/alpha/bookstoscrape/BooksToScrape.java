package org.alpha.bookstoscrape;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class BooksToScrape {
    public static void main(String[] args) throws IOException, SQLException {

        //Fetch data from website
        List<DataDTO> dataDTOS = fetchData();

        //Store Data from website
        boolean isDataRecordedInDB = storeData(dataDTOS);
        if (isDataRecordedInDB)
            System.out.println("Data recorded in DB");
        else
            System.out.println("Data recorded in DB failed");



        //Writedata to excel
        writeDataToExcel(dataDTOS,"BooksToScrape.xlsx");
    }


    private static List<DataDTO>  fetchData() throws IOException {
        String baseUrl = "https://books.toscrape.com/";
        Document document;

        try {
            document = Jsoup.connect(baseUrl).get();
        } catch (IOException e) {
            System.out.println("Connect Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        Elements catList = document.select(".side_categories ul li ul li a");
        List<DataDTO> dataDTOList = new ArrayList<>();
        for (Element category : catList) {
            System.out.println("===========================" + category.text() + "=================================");
            String href = category.attr("href");
            document = Jsoup.connect(baseUrl + href).get();
            while (true) {
                Elements bookElements = document.select(".product_pod");
                for (Element element : bookElements) {
                    String imageUrl = element.select(".thumbnail").attr("src").replace("../../../../", "");
                    String title = element.select("h3 > a").text();
                    String price = element.select(".price_color").text();
                    String rating = element.select(".star-rating").attr("class").replace("star-rating ", "");
                    boolean inStock = element.select(".instock").text().equalsIgnoreCase("In Stock");
                    DataDTO build = DataDTO.builder().title(title).price(price).rating(getRatingIntValue(rating)).inStock(inStock).imageUrl(imageUrl).build();
                    dataDTOList.add(build);
                }
                Elements nextButton = document.select(".next > a");
                if (nextButton.isEmpty()) {
                    break;
                }

                String nextLink = baseUrl + href.replace("index.html", "") + nextButton.attr("href").replace("catalogue/", "");
                document = Jsoup.connect(nextLink).get();
            }
        }
        return dataDTOList;
    }

    private static boolean storeData(List<DataDTO> dataToStore) {
        try (Connection conn = initDBConnections()) {
            String insertQuery = "INSERT INTO books_data (title, price, rating, instock, image_url) VALUES (?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);

            for (DataDTO bookData : dataToStore) {
                preparedStatement.setString(1, bookData.getTitle());
                preparedStatement.setString(2, bookData.getPrice());
                preparedStatement.setInt(3, bookData.getRating());
                preparedStatement.setBoolean(4, bookData.getInStock());
                preparedStatement.setString(5, bookData.getImageUrl());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            return true;
        } catch (SQLException e) {
            System.out.println("Error while storing data = " + e);
            return false;
        }
    }

    private static Connection initDBConnections() throws SQLException {
        String dbMySqlURL = "jdbc:mysql://localhost:3306/webscrapers?allowPublicKeyRetrieval=true&useSSL=false";
        String user = "root";
        String pass = "root@123";
        return DriverManager.getConnection(dbMySqlURL, user, pass);
    }

    private static int getRatingIntValue(String val) {
        Map<String, Integer> numberMap = new HashMap<>();
        numberMap.put("Zero", 0);
        numberMap.put("One", 1);
        numberMap.put("Two", 2);
        numberMap.put("Three", 3);
        numberMap.put("Four", 4);
        numberMap.put("Five", 5);
        return numberMap.getOrDefault(val, 0);
    }

    public static void writeDataToExcel(List<DataDTO> dataList, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Books Data");

        String[] headers = {"Title", "Price", "Rating", "In Stock", "Image URL"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (DataDTO data : dataList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getTitle());
            row.createCell(1).setCellValue(data.getPrice());
            row.createCell(2).setCellValue(data.getRating());
            row.createCell(3).setCellValue(data.getInStock());
            row.createCell(4).setCellValue(data.getImageUrl());
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