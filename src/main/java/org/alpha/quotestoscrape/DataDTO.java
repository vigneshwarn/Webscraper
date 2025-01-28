package org.alpha.quotestoscrape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataDTO {
    private String quote;
    private String author;
    private String internalAuthorDetailLink;
    private List<String> tags;
}
