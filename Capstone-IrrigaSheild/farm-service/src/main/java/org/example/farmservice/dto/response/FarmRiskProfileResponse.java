package org.example.farmservice.dto.response;

import java.util.List;

public record FarmRiskProfileResponse(

        FarmResponse farm,
        List<CropResponse> crops,
        IrrigationProfileResponse irrigationProfile

) {
}