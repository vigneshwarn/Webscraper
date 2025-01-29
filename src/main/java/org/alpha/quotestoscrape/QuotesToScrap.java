package org.alpha.quotestoscrape;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuotesToScrap {


    public static void main(String[] args) throws IOException, SQLException {

        List<DataDTO> dataDTOS = fetchDataFromSite();

        boolean isDataStored = storeData(dataDTOS);

        if (isDataStored) System.out.println("Data stored");
        else System.out.println("Data not stored");

        writeDataToExcel(dataDTOS,"QuotesToScrap.xlsx");
    }


    private static List<DataDTO> fetchDataFromSite() throws IOException {
        int pageNumber = 1;
        List<DataDTO> parserData = new ArrayList<DataDTO>();
        while (true) {
            System.out.println("===================================== " + pageNumber + " =====================================");
            String fullUrl = "https://quotes.toscrape.com";
            if (pageNumber != 1) {
                fullUrl += "/page/" + pageNumber + "/";
            }
            Document document = Jsoup.connect(fullUrl).get();
            Elements listOfData = document.select(".quote");
            for (Element data : listOfData) {
                String quotes = data.select(".text").text();
                String author = data.select(".author").text();
                String internalLinkURL = data.select("span > a").attr("href");
                Elements select = data.select(".tags > a");
                List<String> tags = select.stream().map(Element::text).collect(Collectors.toList());

                DataDTO dataDTO = new DataDTO(quotes, author, internalLinkURL, tags);

                parserData.add(dataDTO);

            }
            Elements nextButton = document.select("body > div > div:nth-child(2) > div.col-md-8 > nav > ul > li.next > a");
            if (nextButton.isEmpty()) {
                break;
            }
            pageNumber++;
        }
        return parserData;
    }


    private static boolean storeData(List<DataDTO> data) throws SQLException {
        Connection conn = initDBConnections();

        String insertQuery = "INSERT INTO quotes_data (quote,author,internal_link,tags) VALUES (?,?,?,?)";
        PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);

        for (int i = 0; i < data.size(); i++) {
            DataDTO quoteData = data.get(i);
            preparedStatement.setString(1, quoteData.getQuote());
            preparedStatement.setString(2, quoteData.getAuthor());
            preparedStatement.setString(3, quoteData.getInternalAuthorDetailLink());
            preparedStatement.setString(4, quoteData.getTags().toString());
            preparedStatement.addBatch();
        }

        preparedStatement.executeBatch();
        return false;
    }

    private static Connection initDBConnections() throws SQLException {
        String dbMySqlURL = "jdbc:mysql://localhost:3306/webscrapers?allowPublicKeyRetrieval=true&useSSL=false";
        String user = "root";
        String pass = "root@123";
        return DriverManager.getConnection(dbMySqlURL, user, pass);
    }


    public static void writeDataToExcel(List<DataDTO> dataList, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Books Data");

        String[] headers = {"quote", "author", "internal_link", "tags"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (DataDTO data : dataList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getQuote());
            row.createCell(1).setCellValue(data.getAuthor());
            row.createCell(2).setCellValue(data.getInternalAuthorDetailLink());
            row.createCell(3).setCellValue(data.getTags().toString());
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
