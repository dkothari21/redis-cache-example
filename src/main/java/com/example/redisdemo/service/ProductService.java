package com.example.redisdemo.service;

import com.example.redisdemo.model.Product;
import com.example.redisdemo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all products (not cached to show fresh data)
     */
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database");
        return productRepository.findAll();
    }

    /**
     * Get product by ID with caching
     * First call: fetches from database and stores in Redis
     * Subsequent calls: fetches from Redis (much faster!)
     */
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getProductById(Long id) {
        long startTime = System.nanoTime();
        log.info("🔍 CACHE MISS - Fetching product ID {} from DATABASE (This will be slow...)", id);

        // Simulate slow database query
        try {
            Thread.sleep(2000); // 2 second delay to simulate slow DB
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Optional<Product> product = productRepository.findById(id);
        long duration = (System.nanoTime() - startTime) / 1_000_000;

        if (product.isPresent()) {
            log.info("✅ Product '{}' fetched from database in {}ms - NOW CACHING IN REDIS",
                    product.get().getName(), duration);
        }

        return product;
    }

    /**
     * Create a new product
     */
    public Product createProduct(Product product) {
        log.info("➕ Creating new product: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * Update product and update the cache
     * The cache will be updated with the new product data
     */
    @CachePut(value = "products", key = "#id")
    public Product updateProduct(Long id, Product productDetails) {
        log.info("🔄 UPDATE OPERATION - Product ID {} - Database AND Cache will be updated", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        String oldName = product.getName();
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setStockQuantity(productDetails.getStockQuantity());

        Product updated = productRepository.save(product);
        log.info("✅ Product updated: '{}' -> '{}' | Cache synchronized with database", oldName, updated.getName());

        return updated;
    }

    /**
     * Delete product and remove from cache
     * The cache entry will be evicted
     */
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        String productName = product.map(Product::getName).orElse("Unknown");

        log.info("🗑️ DELETE OPERATION - Product ID {} ('{}') - Removing from database AND cache", id, productName);
        productRepository.deleteById(id);
        log.info("✅ Product deleted and cache entry evicted for ID {}", id);
    }

    /**
     * Clear all cache entries
     */
    @CacheEvict(value = "products", allEntries = true)
    public void clearCache() {
        log.info("🧹 CLEARING ALL PRODUCT CACHE ENTRIES");
    }

    /**
     * Get products by category (not cached)
     */
    public List<Product> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    /**
     * Search products by name (not cached)
     */
    public List<Product> searchProducts(String name) {
        log.info("Searching products with name containing: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }
}
