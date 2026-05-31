package com.lukimia.bekawankasir.service

import com.lukimia.bekawankasir.dto.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class InMemoryStore(private val passwordEncoder: PasswordEncoder) {
    val permissions = ConcurrentHashMap<UUID, Permission>()
    val roles = ConcurrentHashMap<UUID, Role>()
    val outlets = ConcurrentHashMap<UUID, Outlet>()
    val users = ConcurrentHashMap<UUID, UserAccount>()
    val categories = ConcurrentHashMap<UUID, Category>()
    val products = ConcurrentHashMap<UUID, Product>()
    val stockMovements = ConcurrentHashMap<UUID, StockMovement>()
    val customers = ConcurrentHashMap<UUID, Customer>()
    val shifts = ConcurrentHashMap<UUID, Shift>()
    val sales = ConcurrentHashMap<UUID, Sale>()
    val receiptSettings = ConcurrentHashMap<UUID, ReceiptSetting>()
    private val invoiceCounters = ConcurrentHashMap<String, AtomicInteger>()

    init { seed() }

    private fun seed() {
        val permissionCodes = listOf("auth:me", "outlets:manage", "users:manage", "roles:manage", "products:manage", "stock:manage", "customers:manage", "shifts:manage", "sales:create", "sales:read", "sales:void", "reports:read", "receipts:manage")
        permissionCodes.forEach { code -> permissions[UUID.randomUUID()] = Permission(code = code, name = code.replace(':', ' ').replaceFirstChar { it.uppercase() }) }
        RoleCode.entries.forEach { roleCode ->
            val allowed = when (roleCode) {
                RoleCode.DEVELOPER, RoleCode.SUPER_ADMIN, RoleCode.OWNER -> permissionCodes.toSet()
                RoleCode.ADMIN -> setOf("auth:me", "users:manage", "products:manage", "stock:manage", "sales:read", "reports:read", "receipts:manage")
                RoleCode.MANAGER -> setOf("auth:me", "stock:manage", "shifts:manage", "sales:read", "reports:read")
                RoleCode.CASHIER -> setOf("auth:me", "shifts:manage", "sales:create", "sales:read")
                RoleCode.INVENTORY_STAFF -> setOf("auth:me", "products:manage", "stock:manage")
                RoleCode.FINANCE -> setOf("auth:me", "sales:read", "reports:read")
            }
            roles[UUID.randomUUID()] = Role(code = roleCode, name = roleCode.name.replace('_', ' '), permissions = allowed.toMutableSet())
        }
        val outlet = Outlet(name = "Kawan Kasir Demo Store", code = "STORE01", address = "Jakarta", phone = "081234567890", email = "store@kawankasir.local")
        outlets[outlet.id] = outlet
        val developerRole = roles.values.first { it.code == RoleCode.DEVELOPER }
        val superRole = roles.values.first { it.code == RoleCode.SUPER_ADMIN }
        val cashierRole = roles.values.first { it.code == RoleCode.CASHIER }
        val developer = UserAccount(outletId = outlet.id, name = "Lukimia Developer", email = "developer@lukimia.tech", username = "lukimia", phone = "080000000000", passwordHash = passwordEncoder.encode("lukimia"), roleId = developerRole.id)
        val admin = UserAccount(outletId = outlet.id, name = "Super Admin", email = "admin@kawankasir.local", username = "admin", phone = "081111111111", passwordHash = passwordEncoder.encode("password123"), roleId = superRole.id)
        val cashier = UserAccount(outletId = outlet.id, name = "Kasir Demo", email = "kasir@kawankasir.local", username = "kasir", phone = "082222222222", passwordHash = passwordEncoder.encode("password123"), roleId = cashierRole.id)
        users[developer.id] = developer; users[admin.id] = admin; users[cashier.id] = cashier
        val category = Category(outletId = outlet.id, name = "Minuman", description = "Produk minuman favorit")
        categories[category.id] = category
        val tea = Product(outletId = outlet.id, categoryId = category.id, sku = "DRK-TEA-001", barcode = "899000000001", name = "Es Teh Manis", unit = "cup", purchasePrice = BigDecimal("2500"), sellingPrice = BigDecimal("5000"), stock = 100, minStock = 10)
        val coffee = Product(outletId = outlet.id, categoryId = category.id, sku = "DRK-COF-001", barcode = "899000000002", name = "Kopi Susu", unit = "cup", purchasePrice = BigDecimal("6000"), sellingPrice = BigDecimal("12000"), stock = 80, minStock = 10)
        products[tea.id] = tea; products[coffee.id] = coffee
        receiptSettings[UUID.randomUUID()] = ReceiptSetting(outletId = outlet.id, storeName = outlet.name, headerNote = "Kawan Kasir - POS modern untuk UMKM")
    }

    fun userView(user: UserAccount): UserView {
        val role = roles[user.roleId] ?: error("Role not found")
        return UserView(user.id, user.outletId, user.name, user.email, user.username, user.phone, role.code, role.permissions)
    }

    fun findUser(login: String): UserAccount? = users.values.firstOrNull { it.username.equals(login, true) || it.email.equals(login, true) }
    fun nextInvoice(outletId: UUID): String {
        val outlet = outlets[outletId] ?: error("Outlet not found")
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val key = "${outlet.code}-$date"
        val running = invoiceCounters.computeIfAbsent(key) { AtomicInteger(0) }.incrementAndGet().toString().padStart(4, '0')
        return "KK-${outlet.code}-$date-$running"
    }

    fun createSale(request: SaleRequest): Sale {
        require(request.items.isNotEmpty()) { "Minimal satu item transaksi diperlukan" }
        val saleItems = request.items.map { line ->
            val product = products[line.productId] ?: error("Produk ${line.productId} tidak ditemukan")
            val price = line.price ?: product.sellingPrice
            require(line.quantity > 0) { "Quantity harus lebih dari 0" }
            if (product.stockTracked) require(product.stock >= line.quantity) { "Stok ${product.name} tidak cukup" }
            SaleItem(product.id, product.name, line.quantity, price, line.discount)
        }
        val subtotal = saleItems.fold(BigDecimal.ZERO) { acc, item -> acc + item.subtotal }
        val total = subtotal - request.discount + request.tax
        require(request.paidAmount >= total) { "Pembayaran kurang dari total" }
        val sale = Sale(outletId = request.outletId, shiftId = request.shiftId, customerId = request.customerId, invoiceNumber = nextInvoice(request.outletId), cashierId = request.cashierId, items = saleItems, subtotal = subtotal, discount = request.discount, tax = request.tax, total = total, paymentMethod = request.paymentMethod, paidAmount = request.paidAmount)
        sales[sale.id] = sale
        saleItems.forEach { item ->
            val product = products[item.productId] ?: return@forEach
            if (product.stockTracked) {
                val before = product.stock
                product.stock -= item.quantity
                val movement = StockMovement(outletId = product.outletId, productId = product.id, type = StockMovementType.SALE, quantity = item.quantity, beforeStock = before, afterStock = product.stock, referenceType = "SALE", referenceId = sale.id, createdBy = request.cashierId)
                stockMovements[movement.id] = movement
            }
        }
        return sale
    }

    fun voidSale(id: UUID): Sale {
        val sale = sales[id] ?: error("Transaksi tidak ditemukan")
        require(sale.status != SaleStatus.VOID) { "Transaksi sudah void" }
        val voided = sale.copy(status = SaleStatus.VOID)
        sales[id] = voided
        sale.items.forEach { item ->
            val product = products[item.productId] ?: return@forEach
            if (product.stockTracked) {
                val before = product.stock
                product.stock += item.quantity
                val movement = StockMovement(outletId = product.outletId, productId = product.id, type = StockMovementType.RETURN, quantity = item.quantity, beforeStock = before, afterStock = product.stock, referenceType = "SALE_VOID", referenceId = sale.id)
                stockMovements[movement.id] = movement
            }
        }
        return voided
    }

    fun addStockMovement(movement: StockMovement): StockMovement {
        val product = products[movement.productId] ?: error("Produk tidak ditemukan")
        val before = product.stock
        val after = when (movement.type) {
            StockMovementType.IN, StockMovementType.RETURN -> before + movement.quantity
            StockMovementType.OUT, StockMovementType.SALE -> before - movement.quantity
            StockMovementType.ADJUSTMENT, StockMovementType.OPNAME -> movement.afterStock
            StockMovementType.TRANSFER -> before - movement.quantity
        }
        require(after >= 0) { "Stok tidak boleh negatif" }
        product.stock = after
        val saved = movement.copy(beforeStock = before, afterStock = after)
        stockMovements[saved.id] = saved
        return saved
    }

    fun touchUser(user: UserAccount) = user.copy(updatedAt = OffsetDateTime.now())
}
