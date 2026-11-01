package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.Product;
import com.ptskart.api.entity.Supplier;
import com.ptskart.api.entity.Warehouse;
import com.ptskart.api.repository.ProductRepository;
import com.ptskart.api.repository.SupplierRepository;
import com.ptskart.api.repository.WarehouseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;

    public InventoryController(ProductRepository productRepository,
                               WarehouseRepository warehouseRepository,
                               SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventorySummary() {
        List<Product> products = productRepository.findAll();
        int totalProducts = products.size();
        long inStockCount = products.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 15).count();
        long lowStockCount = products.stream().filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0 && p.getStockQuantity() <= 15).count();
        long outOfStockCount = products.stream().filter(p -> p.getStockQuantity() == null || p.getStockQuantity() == 0).count();
        int totalUnits = products.stream().mapToInt(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", totalProducts);
        summary.put("totalUnits", totalUnits);
        summary.put("inStockCount", inStockCount);
        summary.put("lowStockCount", lowStockCount);
        summary.put("outOfStockCount", outOfStockCount);
        summary.put("stockHealthPercent", totalProducts > 0 ? (int)((double) inStockCount / totalProducts * 100) : 100);

        return ResponseEntity.ok(ApiResponse.ok("Inventory summary calculated", summary));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<Warehouse>>> getWarehouses() {
        return ResponseEntity.ok(ApiResponse.ok("Warehouses retrieved", warehouseRepository.findAll()));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<Warehouse>> createWarehouse(@RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse created", warehouseRepository.save(warehouse)));
    }

    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse<List<Supplier>>> getSuppliers() {
        return ResponseEntity.ok(ApiResponse.ok("Suppliers retrieved", supplierRepository.findAll()));
    }

    @PostMapping("/suppliers")
    public ResponseEntity<ApiResponse<Supplier>> createSupplier(@RequestBody Supplier supplier) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier created", supplierRepository.save(supplier)));
    }
}

