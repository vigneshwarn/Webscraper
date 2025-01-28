package org.alpha.truecarvins;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class TrueCarVinScraper {
    public static void main(String[] args) throws IOException {
        String baseUrl = "https://www.truecar.com/used-cars-for-sale/listings/";
        Document document;

        try {
            document = Jsoup.connect(baseUrl).get();
        } catch (IOException e) {
            System.out.println("Connect Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        Elements carElements = document.select(".vehicle-card");
        for (Element carElement : carElements) {
            String vin = carElement.select(".vin").text();
            System.out.println("VIN: " + vin);
        }
    }
}
