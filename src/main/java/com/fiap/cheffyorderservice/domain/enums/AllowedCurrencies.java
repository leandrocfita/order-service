package com.fiap.cheffyorderservice.domain.enums;

import java.util.stream.Stream;

public enum AllowedCurrencies {
    BRL,
    USD,
    EUR;

    public static boolean isValid(String currency) {
        return Stream.of(values())
                .anyMatch(c -> c.name().equals(currency));
    }
}
