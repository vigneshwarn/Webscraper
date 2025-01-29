package org.alpha.VinDataScraper;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VinDTO {

    private String vin;
    private int year;
    private String make;
    private String model;
}
