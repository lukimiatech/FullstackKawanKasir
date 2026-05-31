package com.lukimia.bekawankasir.controller

import com.lukimia.bekawankasir.dto.*
import com.lukimia.bekawankasir.service.InMemoryStore
import com.lukimia.bekawankasir.service.TokenService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(private val store: InMemoryStore, private val passwordEncoder: PasswordEncoder, private val tokenService: TokenService) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<AuthResponse> {
        val user = store.findUser(request.usernameOrEmail)?.takeIf { it.active && passwordEncoder.matches(request.password, it.passwordHash) }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username/email atau password salah")
        val role = store.roles[user.roleId] ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Role tidak valid")
        store.users[user.id] = user.copy(lastLoginAt = java.time.OffsetDateTime.now())
        return ApiResponse(true, "Login berhasil", AuthResponse(tokenService.accessToken(user.id, role.code.name), tokenService.refreshToken(user.id, role.code.name), user = store.userView(user)))
    }


    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ApiResponse<AuthResponse> {
        if (store.findUser(request.username) != null || store.findUser(request.email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username/email sudah digunakan")
        }
        val outletCode = request.storeName
            .uppercase()
            .replace(Regex("[^A-Z0-9]+"), "")
            .take(8)
            .ifBlank { "STORE" } + (store.outlets.size + 1).toString().padStart(2, '0')
        val outlet = Outlet(
            name = request.storeName,
            code = outletCode,
            phone = request.phone,
            email = request.email,
        )
        store.outlets[outlet.id] = outlet
        val ownerRole = store.roles.values.firstOrNull { it.code == RoleCode.OWNER }
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role OWNER belum tersedia")
        val user = UserAccount(
            outletId = outlet.id,
            name = request.ownerName,
            email = request.email,
            username = request.username,
            phone = request.phone,
            passwordHash = passwordEncoder.encode(request.password),
            roleId = ownerRole.id,
        )
        store.users[user.id] = user
        store.receiptSettings[UUID.randomUUID()] = ReceiptSetting(
            outletId = outlet.id,
            storeName = outlet.name,
            headerNote = "Selamat datang di Kawan Kasir",
        )
        return ApiResponse(
            true,
            "Pendaftaran akun berhasil",
            AuthResponse(tokenService.accessToken(user.id, ownerRole.code.name), tokenService.refreshToken(user.id, ownerRole.code.name), user = store.userView(user)),
        )
    }

    @PostMapping("/logout") fun logout() = ApiResponse(true, "Logout berhasil")
    @PostMapping("/refresh-token") fun refresh(@RequestHeader("Authorization") bearer: String): ApiResponse<Map<String, String>> {
        val claims = tokenService.parse(bearer.removePrefix("Bearer "))?.takeIf { it.type == "refresh" } ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token tidak valid")
        val role = store.roles[store.users[claims.userId]?.roleId]?.code?.name ?: claims.role
        return ApiResponse(true, "Token diperbarui", mapOf("accessToken" to tokenService.accessToken(claims.userId, role)))
    }
    @PostMapping("/forgot-password") fun forgotPassword() = ApiResponse(true, "Instruksi reset password akan dikirim bila akun terdaftar")
    @PostMapping("/reset-password") fun resetPassword() = ApiResponse(true, "Password berhasil direset")
    @PostMapping("/change-password") fun changePassword(@Valid @RequestBody request: UpdatePasswordRequest): ApiResponse<Unit> {
        val id = currentUserId()
        val user = store.users[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan")
        store.users[id] = store.touchUser(user.copy(passwordHash = passwordEncoder.encode(request.newPassword)))
        return ApiResponse(true, "Password berhasil diganti")
    }
    @GetMapping("/me") fun me(): ApiResponse<UserView> = ApiResponse(true, "Profil aktif", store.userView(store.users[currentUserId()] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)))
}

fun currentUserId(): UUID = UUID.fromString(SecurityContextHolder.getContext().authentication?.principal?.toString() ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED))
