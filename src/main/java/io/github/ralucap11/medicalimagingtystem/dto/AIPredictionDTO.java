package io.github.ralucap11.medicalimagingtystem.dto;


import lombok.Data;

import java.util.Map;
@Data
public class AIPredictionDTO
{
    private String predClass;
    private double confidence;
    private Map<String, Double> probabilities;
}
