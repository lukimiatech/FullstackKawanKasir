package com.lukimia.bekawankasir.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class ApiResponse<T>(val success: Boolean, val message: String, val data: T? = null, val timestamp: OffsetDateTime = OffsetDateTime.now())
data class PageResponse<T>(val content: List<T>, val total: Int)

enum class RoleCode { DEVELOPER, SUPER_ADMIN, OWNER, ADMIN, MANAGER, CASHIER, INVENTORY_STAFF, FINANCE }
enum class StockMovementType { IN, OUT, SALE, RETURN, ADJUSTMENT, OPNAME, TRANSFER }
enum class SaleStatus { DRAFT, PAID, VOID }
enum class PaymentMethod { CASH, QRIS, DEBIT_CARD, CREDIT_CARD, TRANSFER, E_WALLET }
enum class ShiftStatus { OPEN, CLOSED }

data class Permission(val id: UUID = UUID.randomUUID(), val code: String, val name: String)
data class Role(val id: UUID = UUID.randomUUID(), val code: RoleCode, val name: String, val permissions: MutableSet<String> = mutableSetOf())
data class Outlet(val id: UUID = UUID.randomUUID(), @field:NotBlank val name: String, @field:NotBlank val code: String, val address: String? = null, val phone: String? = null, val email: String? = null, val taxNumber: String? = null, val logoUrl: String? = null, val active: Boolean = true, val createdAt: OffsetDateTime = OffsetDateTime.now(), val updatedAt: OffsetDateTime = OffsetDateTime.now())
data class UserAccount(val id: UUID = UUID.randomUUID(), val outletId: UUID? = null, @field:NotBlank val name: String, @field:Email val email: String, @field:NotBlank val username: String, val phone: String? = null, val passwordHash: String, val roleId: UUID, val active: Boolean = true, val lastLoginAt: OffsetDateTime? = null, val createdAt: OffsetDateTime = OffsetDateTime.now(), val updatedAt: OffsetDateTime = OffsetDateTime.now())
data class Category(val id: UUID = UUID.randomUUID(), val outletId: UUID, @field:NotBlank val name: String, val description: String? = null, val active: Boolean = true)
data class Product(val id: UUID = UUID.randomUUID(), val outletId: UUID, val categoryId: UUID? = null, @field:NotBlank val sku: String, val barcode: String? = null, @field:NotBlank val name: String, val description: String? = null, val imageUrl: String? = null, val unit: String = "pcs", val purchasePrice: BigDecimal = BigDecimal.ZERO, @field:PositiveOrZero val sellingPrice: BigDecimal, val wholesalePrice: BigDecimal? = null, var stock: Int = 0, val minStock: Int = 0, val stockTracked: Boolean = true, val active: Boolean = true, val createdAt: OffsetDateTime = OffsetDateTime.now(), val updatedAt: OffsetDateTime = OffsetDateTime.now())
data class StockMovement(val id: UUID = UUID.randomUUID(), val outletId: UUID, val productId: UUID, val type: StockMovementType, val quantity: Int, val beforeStock: Int, val afterStock: Int, val referenceType: String? = null, val referenceId: UUID? = null, val note: String? = null, val createdBy: UUID? = null, val createdAt: OffsetDateTime = OffsetDateTime.now())
data class Customer(val id: UUID = UUID.randomUUID(), val outletId: UUID, val name: String, val phone: String? = null, val email: String? = null, val address: String? = null)
data class Shift(val id: UUID = UUID.randomUUID(), val outletId: UUID, val cashierId: UUID, val openingCash: BigDecimal = BigDecimal.ZERO, val closingCash: BigDecimal? = null, val status: ShiftStatus = ShiftStatus.OPEN, val openedAt: OffsetDateTime = OffsetDateTime.now(), val closedAt: OffsetDateTime? = null)
data class SaleItem(val productId: UUID, val productName: String, val quantity: Int, val price: BigDecimal, val discount: BigDecimal = BigDecimal.ZERO, val subtotal: BigDecimal = price.multiply(BigDecimal(quantity)).minus(discount))
data class Sale(val id: UUID = UUID.randomUUID(), val outletId: UUID, val shiftId: UUID? = null, val customerId: UUID? = null, val invoiceNumber: String, val cashierId: UUID? = null, val items: List<SaleItem>, val subtotal: BigDecimal, val discount: BigDecimal = BigDecimal.ZERO, val tax: BigDecimal = BigDecimal.ZERO, val total: BigDecimal, val paymentMethod: PaymentMethod, val paidAmount: BigDecimal, val changeAmount: BigDecimal = paidAmount.minus(total), val status: SaleStatus = SaleStatus.PAID, val createdAt: OffsetDateTime = OffsetDateTime.now())
data class ReceiptSetting(val id: UUID = UUID.randomUUID(), val outletId: UUID, val storeName: String, val headerNote: String? = null, val footerNote: String? = "Terima kasih sudah berbelanja di Kawan Kasir", val showLogo: Boolean = true)

data class LoginRequest(@field:NotBlank val usernameOrEmail: String, @field:NotBlank val password: String)
data class RegisterRequest(@field:NotBlank val storeName: String, @field:NotBlank val ownerName: String, @field:Email val email: String, @field:NotBlank val username: String, val phone: String? = null, @field:Size(min = 6) val password: String)
data class AuthResponse(val accessToken: String, val refreshToken: String, val tokenType: String = "Bearer", val user: UserView)
data class UserView(val id: UUID, val outletId: UUID?, val name: String, val email: String, val username: String, val phone: String?, val role: RoleCode, val permissions: Set<String>)
data class CreateUserRequest(val outletId: UUID? = null, @field:NotBlank val name: String, @field:Email val email: String, @field:NotBlank val username: String, val phone: String? = null, @field:Size(min = 8) val password: String, val roleId: UUID)
data class UpdatePasswordRequest(@field:Size(min = 8) val newPassword: String)
data class RolePermissionRequest(val permissions: Set<String>)
data class SaleRequest(val outletId: UUID, val shiftId: UUID? = null, val customerId: UUID? = null, val cashierId: UUID? = null, val items: List<SaleLineRequest>, val discount: BigDecimal = BigDecimal.ZERO, val tax: BigDecimal = BigDecimal.ZERO, val paymentMethod: PaymentMethod = PaymentMethod.CASH, val paidAmount: BigDecimal)
data class SaleLineRequest(val productId: UUID, val quantity: Int, val price: BigDecimal? = null, val discount: BigDecimal = BigDecimal.ZERO)
data class OpenShiftRequest(val outletId: UUID, val cashierId: UUID, val openingCash: BigDecimal = BigDecimal.ZERO)
data class CloseShiftRequest(val closingCash: BigDecimal)
