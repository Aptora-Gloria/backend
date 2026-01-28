package com.optimetric.github.domain.enumeration;

/**
 * Repository data readiness state.
 * Indicates the overall readiness level of repository data for metrics calculation.
 */
public enum ReadinessState {
    /**
     * No data is ready. All components are missing.
     */
    NOT_READY,
    
    /**
     * Some data is ready, but not all components are available.
     */
    PARTIAL,
    
    /**
     * All required data components are ready.
     */
    READY
}
