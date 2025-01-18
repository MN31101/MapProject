package languages.map.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoundingBoxRequest {
    private double[] leftTopPointLatLon;
    private double[] rightBottomPointLatLon;
}

