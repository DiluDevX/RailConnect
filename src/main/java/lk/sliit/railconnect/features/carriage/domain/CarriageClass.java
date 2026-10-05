package lk.sliit.railconnect.features.carriage.domain;

import java.math.BigDecimal;

public enum CarriageClass {
    FIRST(new BigDecimal("1.50")),
    SECOND(new BigDecimal("1.00")),
    THIRD(new BigDecimal("0.75"));

    private final BigDecimal fareMultiplier;

    CarriageClass(BigDecimal fareMultiplier) {
        this.fareMultiplier = fareMultiplier;
    }

    public BigDecimal getFareMultiplier() {
        return fareMultiplier;
    }
}
