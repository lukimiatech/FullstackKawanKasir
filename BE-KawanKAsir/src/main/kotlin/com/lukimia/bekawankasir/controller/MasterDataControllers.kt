package com.lukimia.bekawankasir.controller

import com.lukimia.bekawankasir.dto.*
import com.lukimia.bekawankasir.service.InMemoryStore
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController @RequestMapping("/api/outlets")
@PreAuthorize("hasAnyAuthority('outlets:manage')")
class OutletController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Daftar outlet", store.outlets.values.toList())
    @PostMapping fun create(@Valid @RequestBody outlet: Outlet) = ApiResponse(true, "Outlet dibuat", outlet.also { store.outlets[it.id] = it })
    @GetMapping("/{id}") fun get(@PathVariable id: UUID) = ApiResponse(true, "Detail outlet", store.outlets[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    @PutMapping("/{id}") fun update(@PathVariable id: UUID, @Valid @RequestBody outlet: Outlet) = ApiResponse(true, "Outlet diperbarui", outlet.copy(id = id).also { store.outlets[id] = it })
    @DeleteMapping("/{id}") fun delete(@PathVariable id: UUID) = ApiResponse(true, "Outlet dihapus", store.outlets.remove(id))
}

@RestController @RequestMapping("/api/users")
@PreAuthorize("hasAnyAuthority('users:manage')")
class UserController(private val store: InMemoryStore, private val passwordEncoder: PasswordEncoder) {
    @GetMapping fun list() = ApiResponse(true, "Daftar user", store.users.values.map(store::userView))
    @PostMapping fun create(@Valid @RequestBody request: CreateUserRequest): ApiResponse<UserView> {
        if (store.findUser(request.username) != null || store.findUser(request.email) != null) throw ResponseStatusException(HttpStatus.CONFLICT, "Username/email sudah digunakan")
        val user = UserAccount(outletId = request.outletId, name = request.name, email = request.email, username = request.username, phone = request.phone, passwordHash = passwordEncoder.encode(request.password), roleId = request.roleId)
        store.users[user.id] = user
        return ApiResponse(true, "User dibuat", store.userView(user))
    }
    @GetMapping("/{id}") fun get(@PathVariable id: UUID) = ApiResponse(true, "Detail user", store.userView(store.users[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)))
    @PutMapping("/{id}") fun update(@PathVariable id: UUID, @RequestBody request: CreateUserRequest): ApiResponse<UserView> {
        val old = store.users[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val user = old.copy(outletId = request.outletId, name = request.name, email = request.email, username = request.username, phone = request.phone, roleId = request.roleId)
        store.users[id] = store.touchUser(user)
        return ApiResponse(true, "User diperbarui", store.userView(user))
    }
    @DeleteMapping("/{id}") fun delete(@PathVariable id: UUID) = ApiResponse(true, "User dihapus", store.users.remove(id)?.let(store::userView))
    @PostMapping("/{id}/reset-password") fun reset(@PathVariable id: UUID, @Valid @RequestBody request: UpdatePasswordRequest): ApiResponse<UserView> {
        val user = store.users[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val saved = store.touchUser(user.copy(passwordHash = passwordEncoder.encode(request.newPassword)))
        store.users[id] = saved
        return ApiResponse(true, "Password user direset", store.userView(saved))
    }
    @PostMapping("/{id}/activate") fun activate(@PathVariable id: UUID) = setActive(id, true)
    @PostMapping("/{id}/deactivate") fun deactivate(@PathVariable id: UUID) = setActive(id, false)
    private fun setActive(id: UUID, active: Boolean): ApiResponse<UserView> { val user = store.users[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND); val saved = store.touchUser(user.copy(active = active)); store.users[id] = saved; return ApiResponse(true, if (active) "User aktif" else "User nonaktif", store.userView(saved)) }
}

@RestController @RequestMapping("/api/roles")
@PreAuthorize("hasAnyAuthority('roles:manage')")
class RoleController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Daftar role", store.roles.values.toList())
    @PostMapping fun create(@Valid @RequestBody role: Role) = ApiResponse(true, "Role dibuat", role.also { store.roles[it.id] = it })
    @GetMapping("/{id}") fun get(@PathVariable id: UUID) = ApiResponse(true, "Detail role", store.roles[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    @PutMapping("/{id}") fun update(@PathVariable id: UUID, @RequestBody role: Role) = ApiResponse(true, "Role diperbarui", role.copy(id = id).also { store.roles[id] = it })
    @DeleteMapping("/{id}") fun delete(@PathVariable id: UUID) = ApiResponse(true, "Role dihapus", store.roles.remove(id))
    @PostMapping("/{id}/permissions") fun permissions(@PathVariable id: UUID, @RequestBody request: RolePermissionRequest): ApiResponse<Role> { val role = store.roles[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND); role.permissions.clear(); role.permissions.addAll(request.permissions); return ApiResponse(true, "Permission role diperbarui", role) }
}
