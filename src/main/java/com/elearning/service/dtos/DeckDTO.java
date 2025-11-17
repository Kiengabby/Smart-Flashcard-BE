package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckDTO {
    
    private Long id;
    private String name;
    private String description;
    private String language;
    private Integer cardCount;
}
