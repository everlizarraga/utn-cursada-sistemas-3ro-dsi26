package io.github.everlizarraga.clase04prac01proy00.dtos.error;

import java.time.Instant;

public record ErrorResponse(
    String error,
    String message,
    Instant timestamp
) {
}
// ↑ error: una etiqueta corta y estable ("not_found", "bad_request") que un
//   programa puede switchear ·
//   message: la frase para humanos ·
//   timestamp: cuándo (Instant = el "ahora" universal de Java, en UTC).
