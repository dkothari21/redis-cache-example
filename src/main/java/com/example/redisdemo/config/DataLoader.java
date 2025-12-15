package com.example.redisdemo.config;

import com.example.redisdemo.model.Product;
import com.example.redisdemo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    private static final String[] CATEGORIES = {
            "Electronics", "Books", "Clothing", "Home & Garden", "Sports",
            "Toys", "Food & Beverage", "Health & Beauty", "Automotive", "Office Supplies"
    };

    private static final String[][] PRODUCTS = {
            // Electronics
            { "MacBook Pro 16", "Apple M3 Max chip, 36GB RAM, 1TB SSD" },
            { "iPhone 15 Pro", "256GB, Titanium Blue" },
            { "iPad Air", "10.9-inch, 64GB, Space Gray" },
            { "AirPods Pro", "Active Noise Cancellation" },
            { "Apple Watch Series 9", "GPS, 45mm, Midnight Aluminum" },
            { "Samsung Galaxy S24", "256GB, Phantom Black" },
            { "Dell XPS 15", "Intel i7, 16GB RAM, 512GB SSD" },
            { "Sony WH-1000XM5", "Wireless Noise Cancelling Headphones" },
            { "LG OLED TV", "55-inch 4K Smart TV" },
            { "Canon EOS R6", "Mirrorless Camera Body" },

            // Books
            { "Clean Code", "A Handbook of Agile Software Craftsmanship" },
            { "The Pragmatic Programmer", "Your Journey to Mastery" },
            { "Design Patterns", "Elements of Reusable Object-Oriented Software" },
            { "Effective Java", "Best Practices for Java Platform" },
            { "Spring in Action", "Covers Spring 6 and Spring Boot 3" },
            { "Head First Design Patterns", "Brain-Friendly Guide" },
            { "Refactoring", "Improving the Design of Existing Code" },
            { "Domain-Driven Design", "Tackling Complexity in Software" },
            { "The Clean Coder", "A Code of Conduct for Professional Programmers" },
            { "Microservices Patterns", "With examples in Java" },

            // Clothing
            { "Nike Air Max", "Running Shoes, Size 10" },
            { "Levi's 501 Jeans", "Original Fit, Blue Denim" },
            { "Adidas Hoodie", "Cotton Blend, Black" },
            { "North Face Jacket", "Waterproof, Winter Collection" },
            { "Ray-Ban Sunglasses", "Classic Aviator Style" },
            { "Patagonia Fleece", "Recycled Polyester, Navy" },
            { "Under Armour Shirt", "Performance Athletic Wear" },
            { "Columbia Hiking Boots", "Waterproof, Trail Rated" },
            { "Carhartt Work Pants", "Heavy Duty Canvas" },
            { "Timberland Boots", "Premium Leather, Wheat" },

            // Home & Garden
            { "Dyson Vacuum", "Cordless Stick Vacuum V15" },
            { "KitchenAid Mixer", "Stand Mixer, 5-Quart" },
            { "Ninja Blender", "Professional 1000W" },
            { "Instant Pot", "7-in-1 Pressure Cooker" },
            { "Keurig Coffee Maker", "K-Elite Single Serve" },
            { "Weber Grill", "Gas Grill, 3 Burners" },
            { "Roomba Vacuum", "Robot Vacuum with Mapping" },
            { "Nest Thermostat", "Smart Learning Thermostat" },
            { "Ring Doorbell", "Video Doorbell Pro 2" },
            { "Philips Hue Lights", "Smart LED Bulb Starter Kit" },

            // Sports
            { "Peloton Bike", "Indoor Cycling Bike" },
            { "Bowflex Dumbbells", "Adjustable Weight Set" },
            { "Yoga Mat", "Extra Thick Exercise Mat" },
            { "Fitbit Charge 6", "Fitness Tracker" },
            { "TRX Suspension", "Home Gym Training Kit" },
            { "Spalding Basketball", "Official Size and Weight" },
            { "Wilson Tennis Racket", "Pro Staff Series" },
            { "Callaway Golf Clubs", "Complete Set with Bag" },
            { "Hydro Flask", "32oz Water Bottle" },
            { "Garmin GPS Watch", "Forerunner 265" }
    };

    @Override
    public void run(String... args) {
        log.info("🚀 Starting to load sample data...");

        long count = productRepository.count();
        if (count > 0) {
            log.info("✅ Database already contains {} products. Skipping data load.", count);
            return;
        }

        List<Product> products = new ArrayList<>();
        Random random = new Random(42); // Fixed seed for consistent data

        // Create 500 products
        for (int i = 0; i < 500; i++) {
            String category = CATEGORIES[i % CATEGORIES.length];
            String[] productInfo = PRODUCTS[i % PRODUCTS.length];

            String name = productInfo[0];
            String description = productInfo[1];

            // Add variation to names for uniqueness
            if (i >= PRODUCTS.length) {
                name = name + " - Model " + (i / PRODUCTS.length + 1);
            }

            // Generate realistic prices based on category
            BigDecimal basePrice = getBasePriceForCategory(category);
            BigDecimal variation = BigDecimal.valueOf(random.nextDouble() * 500);
            BigDecimal price = basePrice.add(variation).setScale(2, java.math.RoundingMode.HALF_UP);

            // Generate stock quantity
            int stockQuantity = 10 + random.nextInt(100);

            Product product = new Product(name, description, price, category, stockQuantity);
            products.add(product);
        }

        productRepository.saveAll(products);
        log.info("✅ Successfully loaded {} products into the database!", products.size());
        log.info("📊 Categories: {}", String.join(", ", CATEGORIES));
        log.info("🎯 Ready to test Redis caching!");
    }

    private BigDecimal getBasePriceForCategory(String category) {
        return switch (category) {
            case "Electronics" -> BigDecimal.valueOf(500);
            case "Books" -> BigDecimal.valueOf(30);
            case "Clothing" -> BigDecimal.valueOf(50);
            case "Home & Garden" -> BigDecimal.valueOf(100);
            case "Sports" -> BigDecimal.valueOf(80);
            case "Toys" -> BigDecimal.valueOf(25);
            case "Food & Beverage" -> BigDecimal.valueOf(15);
            case "Health & Beauty" -> BigDecimal.valueOf(40);
            case "Automotive" -> BigDecimal.valueOf(200);
            case "Office Supplies" -> BigDecimal.valueOf(20);
            default -> BigDecimal.valueOf(50);
        };
    }
}
