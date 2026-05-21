package io.github.ralucap11.medicalimagingtystem.dto;


import lombok.Data;

import java.util.List;

@Data
public class CobbAngleDTO {

    private Double  cobbAngle;
    private String  severity;
    private Integer vertebraeCount;
    private List<VertebraBox> vertebrae;
    private CobbDetails cobbDetails;
    private String  visualization;
    private String  message;

    @Data
    public static class VertebraBox {
        private double x, y, w, h, conf;
    }

    @Data
    public static class CobbDetails {
        private Integer      topVertebra;
        private Integer      bottomVertebra;
        private List<Double> segmentAngles;
        private String       error;
    }
}
