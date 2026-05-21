package io.github.ralucap11.medicalimagingtystem.dto;


import lombok.Data;

import java.util.Map;
@Data
public class AIPredictionDTO
{
    private String predictedClass;
    private Integer predictedIndex;
    private Double confidence;
    private Map<String, Double> probabilities;
}
