package io.github.everlizarraga.clase04prac01proy00.controllers.advice;

import io.github.everlizarraga.clase04prac01proy00.dtos.error.ErrorResponse;
import io.github.everlizarraga.clase04prac01proy00.exceptions.BusinessException;
import io.github.everlizarraga.clase04prac01proy00.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
  // ↑ "Esta clase atrapa lo que ESCAPE de cualquier controller."
  //   Registro automático: la anotación alcanza — nadie la conecta a mano, ningún
  //   controller sabe que existe. Por eso hoy no tocaste ni un controller.

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());
  }
  // ↑ "Toda excepción DE ESTE TIPO que escape, cae acá" → 404. El despacho
  //   es POR TIPO: por eso las excepciones necesitaban nombre propio.

  @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
    return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage());
  }
  // ↑ ¡Las LLAVES de la gramática, regla 3! El atributo es Class[] — este
  //   handler atrapa DOS tipos. ¿Por qué también IllegalArgumentException?
  //   Red de seguridad: si alguna escapa sin traducir, al menos sale 400.
  //   (El Experimento 1 le saca punta a esta decisión.)

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
        "Ocurrió un error interno");
  }
  // ↑ La red final: TODO lo no previsto → 500 con mensaje OPACO a propósito.
  //   El Experimento 2 te muestra por qué la opacidad acá es una virtud.

  private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
    return ResponseEntity
        .status(status)
        .body(new ErrorResponse(error, message, Instant.now()));
  }
  // ↑ La fábrica de respuestas: status elegido + tu DTO uniforme como body.
}
