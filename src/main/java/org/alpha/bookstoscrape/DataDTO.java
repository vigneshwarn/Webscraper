package org.alpha.bookstoscrape;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DataDTO {
    private String title;
    private String price;
    private int rating;
    private Boolean inStock;
    private String imageUrl;
}
