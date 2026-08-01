package io.github.everlizarraga.clase04prac01proy00.controllers.advice;

import io.github.everlizarraga.clase04prac01proy00.dtos.error.ErrorResponse;
import io.github.everlizarraga.clase04prac01proy00.exceptions.BusinessException;
import io.github.everlizarraga.clase04prac01proy00.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice                // "atrapo lo que ESCAPE de cualquier
public class GlobalExceptionHandler {//  controller" — registro automático:
  //  nadie lo conecta, ningún controller
  //  sabe que existe.

  private static final Logger log =
      LoggerFactory.getLogger(GlobalExceptionHandler.class);
  // ↑ ⭐ (extensión tuya) el canal hacia TU consola — su porqué, en la Parte 8.

  @ExceptionHandler(ResourceNotFoundException.class)   // este TIPO → este método
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());      // → 404
  }

  @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
    return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage());  // → 400
  }
  // ↑ Las llaves = array (tu gramática, regla 3): UN handler para DOS tipos.
  //   Se agrupan POR RESPUESTA, no por origen: ambos merecen el mismo 400.
  //   ¿IllegalArgumentException acá? RED DE SEGURIDAD para las que se
  //   escapen sin traducir (la Parte 6 le saca punta).

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    return build(HttpStatus.BAD_REQUEST, "bad_request",
        "El body no es un JSON válido");                          // → 400
  }
  // ↑ ⭐ (extensión tuya) el JSON ilegible es culpa del CLIENTE: 400.
  //   Sin este handler se lo traga el catch-all y sale 500 — el defecto
  //   que descubriste. Verificación y porqué completo: Parte 9.

  @ExceptionHandler(Exception.class)                   // TODO lo no previsto:
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
    log.error("Error no previsto", ex);              // ⭐ la verdad, a TU consola
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
        "Ocurrió un error interno");        // mensaje OPACO a propósito
  }

  private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(error, message, Instant.now()));
  }   // ↑ la fábrica: status elegido + tu forma uniforme como body
}
