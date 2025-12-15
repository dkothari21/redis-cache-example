package com.example.redisdemo.controller;

import com.example.redisdemo.model.Product;
import com.example.redisdemo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products with Redis caching")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all products", description = "Retrieves all products from the database. This endpoint is NOT cached to ensure fresh data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all products", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        long startTime = System.nanoTime();
        List<Product> products = productService.getAllProducts();
        long duration = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println(
                "📋 GET ALL PRODUCTS - Retrieved " + products.size() + " products in " + duration + "ms (NOT CACHED)");

        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get product by ID (CACHED ⚡)", description = """
            Retrieves a product by ID with Redis caching enabled.

            **Cache Behavior:**
            - **First Request**: Fetches from database (~3 seconds) and stores in Redis
            - **Subsequent Requests**: Returns from Redis cache (<50ms) - 60x faster!

            **Try it:**
            1. Call this endpoint with ID 1
            2. Wait for response (~3 seconds)
            3. Call again with same ID
            4. Notice instant response!
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found and returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 REDIS CACHE PERFORMANCE TEST - Product ID: " + id);
        System.out.println("=".repeat(80));

        long startTime = System.nanoTime();

        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        System.out.println("\n📈 PERFORMANCE RESULTS:");
        System.out.println("   Product: " + product.getName());
        System.out.println("   Category: " + product.getCategory());
        System.out.println("   Price: $" + product.getPrice());

        if (durationMs > 1000) {
            System.out.println("\n🐢 CACHE MISS - Data fetched from DATABASE");
            System.out.println("   ⏱️  Response Time: " + durationMs + "ms (~" + (durationMs / 1000.0) + " seconds)");
            System.out.println("   📍 Source: PostgreSQL/H2 Database");
            System.out.println("   💡 TIP: Call this endpoint again with the same ID to see Redis magic!");
        } else {
            System.out.println("\n⚡ CACHE HIT - Data served from REDIS!");
            System.out.println("   ⏱️  Response Time: " + durationMs + "ms");
            System.out.println("   📍 Source: Redis In-Memory Cache");
            System.out.println(
                    "   🚀 Speed Improvement: ~" + (2000 / Math.max(durationMs, 1)) + "x FASTER than database!");
        }

        System.out.println("=".repeat(80) + "\n");

        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Create a new product", description = "Creates a new product in the database. The product will be cached when first retrieved by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product data", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Product object to create", required = true, content = @Content(schema = @Schema(implementation = Product.class))) @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Operation(summary = "Update a product (Updates Cache 🔄)", description = "Updates a product in the database and automatically updates the Redis cache with new data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "Product ID", example = "1", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated product data", required = true, content = @Content(schema = @Schema(implementation = Product.class))) @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Delete a product (Evicts Cache 🗑️)", description = "Deletes a product from the database and automatically removes it from the Redis cache.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID to delete", example = "1", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear all cache entries", description = "Removes all cached products from Redis. Useful for testing or when you need to force fresh data.")
    @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        productService.clearCache();
        return ResponseEntity.ok("Cache cleared successfully");
    }

    @Operation(summary = "Get products by category", description = "Retrieves all products in a specific category. This endpoint is NOT cached.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @Parameter(description = "Product category", example = "Electronics", required = true) @PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @Operation(summary = "Search products by name", description = "Searches for products containing the specified name (case-insensitive). This endpoint is NOT cached.")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @Parameter(description = "Search term", example = "MacBook", required = true) @RequestParam String name) {
        return ResponseEntity.ok(productService.searchProducts(name));
    }
}
