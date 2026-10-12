package org.example.policyservice.util;

import org.example.policyservice.dto.response.PayoutSlabResponse;
import org.example.policyservice.entity.PayoutSlab;

public final class PayoutSlabMapper {

    private PayoutSlabMapper() {
    }

    public static PayoutSlabResponse toResponse(
            PayoutSlab slab) {

        return new PayoutSlabResponse(
                slab.getPayoutSlabId(),
                slab.getTriggerRuleId(),
                slab.getMinimumDurationHours(),
                slab.getMaximumDurationHours(),
                slab.getMinAvailabilityPct(),
                slab.getMaxAvailabilityPct(),
                slab.getPayoutPercentage(),
                slab.getStatus(),
                slab.getCreatedAt(),
                slab.getUpdatedAt()
        );
    }
}
