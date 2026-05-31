package com.lukimia.bekawankasir.config

import com.lukimia.bekawankasir.dto.ApiResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException::class)
    fun responseStatus(error: ResponseStatusException): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.status(error.statusCode).body(ApiResponse(false, error.reason ?: "Request gagal"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(error: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Map<String, String?>>> {
        val fields = error.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity.badRequest().body(ApiResponse(false, "Validasi data gagal", fields))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraint(error: ConstraintViolationException): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.badRequest().body(ApiResponse(false, error.message ?: "Validasi data gagal"))

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun illegal(error: RuntimeException): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.badRequest().body(ApiResponse(false, error.message ?: "Request tidak valid"))

    @ExceptionHandler(Exception::class)
    fun generic(error: Exception): ResponseEntity<ApiResponse<Unit>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse(false, error.message ?: "Terjadi kesalahan server"))
}
