package com.lukimia.bekawankasir.controller

import com.lukimia.bekawankasir.dto.*
import com.lukimia.bekawankasir.service.InMemoryStore
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@RestController @RequestMapping("/api/categories")
@PreAuthorize("hasAnyAuthority('products:manage')")
class CategoryController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Daftar kategori", store.categories.values.toList())
    @PostMapping fun create(@Valid @RequestBody category: Category) = ApiResponse(true, "Kategori dibuat", category.also { store.categories[it.id] = it })
    @GetMapping("/{id}") fun get(@PathVariable id: UUID) = ApiResponse(true, "Detail kategori", store.categories[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    @PutMapping("/{id}") fun update(@PathVariable id: UUID, @Valid @RequestBody category: Category) = ApiResponse(true, "Kategori diperbarui", category.copy(id = id).also { store.categories[id] = it })
    @DeleteMapping("/{id}") fun delete(@PathVariable id: UUID) = ApiResponse(true, "Kategori dihapus", store.categories.remove(id))
}

@RestController @RequestMapping("/api/products")
@PreAuthorize("hasAnyAuthority('products:manage', 'sales:create', 'sales:read')")
class ProductController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Daftar produk", store.products.values.toList())
    @PostMapping fun create(@Valid @RequestBody product: Product) = ApiResponse(true, "Produk dibuat", product.also { store.products[it.id] = it })
    @GetMapping("/{id}") fun get(@PathVariable id: UUID) = ApiResponse(true, "Detail produk", store.products[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    @PutMapping("/{id}") fun update(@PathVariable id: UUID, @Valid @RequestBody product: Product) = ApiResponse(true, "Produk diperbarui", product.copy(id = id).also { store.products[id] = it })
    @DeleteMapping("/{id}") fun delete(@PathVariable id: UUID) = ApiResponse(true, "Produk dihapus", store.products.remove(id))
    @GetMapping("/search") fun search(@RequestParam keyword: String) = ApiResponse(true, "Hasil pencarian produk", store.products.values.filter { it.name.contains(keyword, true) || it.sku.contains(keyword, true) || it.barcode?.contains(keyword, true) == true })
    @GetMapping("/barcode/{barcode}") fun barcode(@PathVariable barcode: String) = ApiResponse(true, "Produk berdasarkan barcode", store.products.values.firstOrNull { it.barcode == barcode } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    @PostMapping("/import") fun importProducts(@RequestBody products: List<Product>) = ApiResponse(true, "Produk diimpor", products.onEach { store.products[it.id] = it })
    @GetMapping("/export") fun export() = ApiResponse(true, "Data export produk", store.products.values.toList())
}

@RestController @RequestMapping("/api/stock-movements")
@PreAuthorize("hasAnyAuthority('stock:manage')")
class StockMovementController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Histori stok", store.stockMovements.values.sortedByDescending { it.createdAt })
    @PostMapping fun create(@RequestBody movement: StockMovement) = ApiResponse(true, "Pergerakan stok disimpan", store.addStockMovement(movement))
    @GetMapping("/product/{productId}") fun byProduct(@PathVariable productId: UUID) = ApiResponse(true, "Histori stok produk", store.stockMovements.values.filter { it.productId == productId })
}

@RestController @RequestMapping("/api/customers")
@PreAuthorize("hasAnyAuthority('customers:manage', 'sales:create')")
class CustomerController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Daftar customer", store.customers.values.toList())
    @PostMapping fun create(@RequestBody customer: Customer) = ApiResponse(true, "Customer dibuat", customer.also { store.customers[it.id] = it })
    @GetMapping("/{id}") fun get(@PathVariable id: UUID) = ApiResponse(true, "Detail customer", store.customers[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    @PutMapping("/{id}") fun update(@PathVariable id: UUID, @RequestBody customer: Customer) = ApiResponse(true, "Customer diperbarui", customer.copy(id = id).also { store.customers[id] = it })
    @DeleteMapping("/{id}") fun delete(@PathVariable id: UUID) = ApiResponse(true, "Customer dihapus", store.customers.remove(id))
}

@RestController @RequestMapping("/api/shifts")
@PreAuthorize("hasAnyAuthority('shifts:manage')")
class ShiftController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Daftar shift", store.shifts.values.toList())
    @PostMapping("/open") fun open(@RequestBody request: OpenShiftRequest): ApiResponse<Shift> {
        val existing = store.shifts.values.firstOrNull { it.cashierId == request.cashierId && it.status == ShiftStatus.OPEN }
        if (existing != null) throw ResponseStatusException(HttpStatus.CONFLICT, "Kasir masih memiliki shift terbuka")
        val shift = Shift(outletId = request.outletId, cashierId = request.cashierId, openingCash = request.openingCash)
        store.shifts[shift.id] = shift
        return ApiResponse(true, "Shift dibuka", shift)
    }
    @PostMapping("/{id}/close") fun close(@PathVariable id: UUID, @RequestBody request: CloseShiftRequest): ApiResponse<Shift> {
        val shift = store.shifts[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val closed = shift.copy(status = ShiftStatus.CLOSED, closingCash = request.closingCash, closedAt = OffsetDateTime.now())
        store.shifts[id] = closed
        return ApiResponse(true, "Shift ditutup", closed)
    }
}

@RestController @RequestMapping("/api/sales")
@PreAuthorize("hasAnyAuthority('sales:create', 'sales:read')")
class SaleController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Daftar transaksi", store.sales.values.sortedByDescending { it.createdAt })
    @PostMapping fun create(@Valid @RequestBody request: SaleRequest) = ApiResponse(true, "Transaksi berhasil", store.createSale(request))
    @GetMapping("/{id}") fun get(@PathVariable id: UUID) = ApiResponse(true, "Detail transaksi", store.sales[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND))
    @PostMapping("/{id}/void") fun void(@PathVariable id: UUID) = ApiResponse(true, "Transaksi divoid", store.voidSale(id))
    @GetMapping("/{id}/receipt") fun receipt(@PathVariable id: UUID): ApiResponse<Map<String, Any?>> {
        val sale = store.sales[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val outlet = store.outlets[sale.outletId]
        val setting = store.receiptSettings.values.firstOrNull { it.outletId == sale.outletId }
        return ApiResponse(true, "Preview struk", mapOf("invoice" to sale.invoiceNumber, "store" to (setting?.storeName ?: outlet?.name), "items" to sale.items, "total" to sale.total, "paid" to sale.paidAmount, "change" to sale.changeAmount, "footer" to setting?.footerNote))
    }
}

@RestController @RequestMapping("/api/receipt-settings")
@PreAuthorize("hasAnyAuthority('receipts:manage')")
class ReceiptSettingController(private val store: InMemoryStore) {
    @GetMapping fun list() = ApiResponse(true, "Pengaturan struk", store.receiptSettings.values.toList())
    @PostMapping fun create(@RequestBody setting: ReceiptSetting) = ApiResponse(true, "Pengaturan struk dibuat", setting.also { store.receiptSettings[it.id] = it })
    @PutMapping("/{id}") fun update(@PathVariable id: UUID, @RequestBody setting: ReceiptSetting) = ApiResponse(true, "Pengaturan struk diperbarui", setting.copy(id = id).also { store.receiptSettings[id] = it })
}

@RestController @RequestMapping("/api/reports")
@PreAuthorize("hasAnyAuthority('reports:read')")
class ReportController(private val store: InMemoryStore) {
    @GetMapping("/dashboard") fun dashboard() = ApiResponse(true, "Ringkasan dashboard", mapOf("outlets" to store.outlets.size, "products" to store.products.size, "transactions" to store.sales.size, "grossSales" to store.sales.values.filter { it.status == SaleStatus.PAID }.fold(BigDecimal.ZERO) { acc, sale -> acc + sale.total }, "lowStockProducts" to store.products.values.count { it.stockTracked && it.stock <= it.minStock }))
    @GetMapping("/sales") fun sales() = ApiResponse(true, "Laporan penjualan", store.sales.values.toList())
    @GetMapping("/stock") fun stock() = ApiResponse(true, "Laporan stok", store.products.values.map { mapOf("productId" to it.id, "sku" to it.sku, "name" to it.name, "stock" to it.stock, "minStock" to it.minStock) })
    @GetMapping("/export/excel") fun excel() = ApiResponse(true, "Placeholder export Excel", mapOf("format" to "xlsx", "status" to "ready-for-integration"))
    @GetMapping("/export/pdf") fun pdf() = ApiResponse(true, "Placeholder export PDF", mapOf("format" to "pdf", "status" to "ready-for-integration"))
}
