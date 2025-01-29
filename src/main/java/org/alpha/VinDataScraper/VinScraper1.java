package org.alpha.carmax;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Sleeper;

import java.util.concurrent.TimeUnit;


public class CarmaxVinScraper {

    public static void main(String[] args) throws InterruptedException {
        test();
    }

    private static void test() throws InterruptedException {
        ChromeOptions chromeOptions = new ChromeOptions();
        //chromeOptions.addArguments("--headless");
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://www.carmax.com/");
        long skip = 0L;
        int loopCount = 1;
        long totalCount = 0L;
        boolean countUpdated = false;
        StringBuilder stringBuilder = null;

        for (int i = 0; i < 3; i++) {
            driver.get("https://www.carmax.com/cars/api/search/run?skip=" + skip + "&take=100&radius=radius-nationwide&shipping=-1&sort=best-match&count=1");
            skip = skip + 100;
            String text = driver.findElement(By.xpath("//body")).getText();
            System.out.println("text = " + text);
            JSONObject jsonObject = new JSONObject(text);
            totalCount = jsonObject.getLong("totalCount");

            if (!countUpdated) {
                loopCount = Math.toIntExact(totalCount / 100);
                countUpdated = true;
            }

            JSONArray items = jsonObject.getJSONArray("items");

            stringBuilder = new StringBuilder();
            stringBuilder.append("insert into vininfo (VIN, year, make, model) values");
            for (int j = 0; j < items.length(); j++) {
                String vin = items.getJSONObject(j).getString("vin");
                int year = items.getJSONObject(j).getInt("year");
                String make = items.getJSONObject(j).getString("make");
                String model = items.getJSONObject(j).getString("model");
                stringBuilder.append("('").append(vin).append("',1").append(year).append(" , '").append(make).append("', '").append(model).append("')");
                if (j != items.length() - 1) stringBuilder.append(",");
            }
            try {
                //conn.prepareStatement(stringBuilder.toString()).execute();
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
            System.out.println(stringBuilder);
        }
        driver.quit();
    }
}
