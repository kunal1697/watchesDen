package com.aeon.common.logging;

/**
 * Correlation-id constants, kept free of servlet types so the reactive gateway can use them.
 */
public final class CorrelationId {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }
}
