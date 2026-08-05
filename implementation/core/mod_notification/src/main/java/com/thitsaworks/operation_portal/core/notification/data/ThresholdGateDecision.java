package com.thitsaworks.operation_portal.core.notification.data;

public record ThresholdGateDecision(
    boolean allowed,
    boolean schemeEnabled,
    boolean dfspEnabled,
    String reason
) {
}
