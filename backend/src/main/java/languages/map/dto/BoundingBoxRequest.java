package languages.map.dto;


import lombok.Getter;

@Getter
public class BoundingBoxRequest {
    private double[] leftTopPointLatLon;
    private double[] rightBottomPointLatLon;
}

