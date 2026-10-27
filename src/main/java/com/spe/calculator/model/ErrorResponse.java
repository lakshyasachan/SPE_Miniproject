package com.spe.calculator.model;

import java.time.Instant;

/**
 * Structured error body returned with HTTP 400, e.g.
 * { "timestamp": "...", "status": 400, "error": "Bad Request", "message": "...", "path": "/api/v1/divide" }
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
