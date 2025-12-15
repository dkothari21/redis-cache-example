# 🏗️ Project Architecture - How Redis Caching Works

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [File Structure & Responsibilities](#file-structure--responsibilities)
3. [How Redis Caching Works](#how-redis-caching-works)
4. [Data Flow Diagrams](#data-flow-diagrams)
5. [Key Components Explained](#key-components-explained)

---

## 🎯 Project Overview

This is a **Spring Boot application** that demonstrates **Redis caching** using a real-world e-commerce product catalog example. It shows how Redis dramatically improves performance by caching frequently accessed data.

**Core Concept**: Store product data in Redis (in-memory cache) instead of querying the database every time.

---

## 📁 File Structure & Responsibilities

### Configuration Files

#### `build.gradle`
**Purpose**: Project dependencies and build configuration  
**Key Dependencies**:
- `spring-boot-starter-data-redis` - Redis integration
- `spring-boot-starter-cache` - Caching abstraction
- `redis.clients:jedis` - Redis client library
- `springdoc-openapi` - Swagger/API documentation

#### `settings.gradle`
**Purpose**: Defines project name  
**Content**: `rootProject.name = 'redis-cache-example'`

#### `src/main/resources/application.properties`
**Purpose**: Application configuration  
**Redis Settings**:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=600000  # 10 minutes
```

---

### Java Source Files

#### 1. `RedisDemoApplication.java`
**Location**: `src/main/java/com/example/redisdemo/`  
**Purpose**: Main application entry point  
**Key Annotation**: `@EnableCaching` - Enables Spring's caching mechanism

```java
@SpringBootApplication
@EnableCaching  // ← This enables Redis caching!
public class RedisDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }
}
```

---

#### 2. `RedisConfig.java`
**Location**: `src/main/java/com/example/redisdemo/config/`  
**Purpose**: Configure Redis connection and cache manager  
**What It Does**:
- Creates connection to Redis server (localhost:6379)
- Sets up serialization (how Java objects are stored in Redis)
- Configures cache TTL (Time To Live)

**Key Components**:
```java
@Bean
public JedisConnectionFactory redisConnectionFactory() {
    // Connects to Redis server
}

@Bean
public RedisTemplate<String, Object> redisTemplate() {
    // Handles serialization/deserialization
}

@Bean
public CacheManager cacheManager() {
    // Manages cache operations
    // TTL: 10 minutes
}
```

---

#### 3. `OpenApiConfig.java`
**Location**: `src/main/java/com/example/redisdemo/config/`  
**Purpose**: Configure Swagger UI documentation  
**What It Does**:
- Sets up API documentation at `/swagger-ui.html`
- Provides descriptions and examples for all endpoints

---

#### 4. `DataLoader.java`
**Location**: `src/main/java/com/example/redisdemo/config/`  
**Purpose**: Automatically load 500 sample products on startup  
**What It Does**:
- Runs when application starts (`CommandLineRunner`)
- Creates 500 realistic products across 10 categories
- Saves them to H2 database
- Only runs if database is empty

**Categories**: Electronics, Books, Clothing, Sports, Home & Garden, Toys, Food & Beverage, Health & Beauty, Automotive, Office Supplies

---

#### 5. `Product.java`
**Location**: `src/main/java/com/example/redisdemo/model/`  
**Purpose**: Product data model  
**Key Features**:
- `@Entity` - JPA entity for database
- `implements Serializable` - **Required for Redis storage!**
- Fields: id, name, description, price, category, stockQuantity

```java
@Entity
public class Product implements Serializable {  // ← Must be Serializable for Redis!
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;
}
```

---

#### 6. `ProductRepository.java`
**Location**: `src/main/java/com/example/redisdemo/repository/`  
**Purpose**: Database access layer  
**What It Does**:
- Extends `JpaRepository` - provides CRUD operations
- Custom queries: `findByCategory()`, `findByNameContaining()`
- Interacts with H2 database

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String name);
}
```

---

#### 7. `ProductService.java` ⭐ **MOST IMPORTANT FOR REDIS**
**Location**: `src/main/java/com/example/redisdemo/service/`  
**Purpose**: Business logic with Redis caching  
**This is where the Redis magic happens!**

##### Key Methods with Caching:

**a) Get Product by ID (Cached)**
```java
@Cacheable(value = "products", key = "#id")
public Optional<Product> getProductById(Long id) {
    // First call: Fetches from database (slow - 2 seconds)
    // Stores result in Redis with key "products::1"
    // Subsequent calls: Returns from Redis (fast - <50ms)
}
```

**b) Update Product (Updates Cache)**
```java
@CachePut(value = "products", key = "#id")
public Product updateProduct(Long id, Product productDetails) {
    // Updates database
    // Automatically updates Redis cache with new data
    // Cache stays synchronized!
}
```

**c) Delete Product (Evicts Cache)**
```java
@CacheEvict(value = "products", key = "#id")
public void deleteProduct(Long id) {
    // Deletes from database
    // Automatically removes from Redis cache
    // No stale data!
}
```

**d) Clear All Cache**
```java
@CacheEvict(value = "products", allEntries = true)
public void clearCache() {
    // Removes all cached products from Redis
}
```

---

#### 8. `ProductController.java`
**Location**: `src/main/java/com/example/redisdemo/controller/`  
**Purpose**: REST API endpoints  
**What It Does**:
- Exposes HTTP endpoints for CRUD operations
- Measures and logs response times
- Shows cache hit vs miss in console

**Key Endpoints**:
- `GET /api/products` - List all (not cached)
- `GET /api/products/{id}` - Get by ID (⚡ cached)
- `POST /api/products` - Create new
- `PUT /api/products/{id}` - Update (🔄 updates cache)
- `DELETE /api/products/{id}` - Delete (🗑️ evicts cache)

**Performance Logging**:
```java
if (durationMs > 1000) {
    System.out.println("🐢 CACHE MISS - Data fetched from DATABASE");
} else {
    System.out.println("⚡ CACHE HIT - Data from REDIS!");
}
```

---

## 🔄 How Redis Caching Works

### Step-by-Step Flow

#### Scenario 1: First Request (Cache Miss)
```
1. User calls: GET /api/products/1
2. Controller receives request
3. Service checks Redis cache for key "products::1"
4. Cache MISS (not found in Redis)
5. Service queries database (slow - 2 seconds)
6. Service stores result in Redis with key "products::1"
7. Returns product to user
8. Console: "🐢 CACHE MISS - 2025ms"
```

#### Scenario 2: Second Request (Cache Hit)
```
1. User calls: GET /api/products/1 (same ID)
2. Controller receives request
3. Service checks Redis cache for key "products::1"
4. Cache HIT (found in Redis!)
5. Returns product from Redis (fast - 15ms)
6. NO database query!
7. Console: "⚡ CACHE HIT - 15ms - 135x FASTER!"
```

#### Scenario 3: Update Product
```
1. User calls: PUT /api/products/1
2. Service updates database
3. @CachePut automatically updates Redis cache
4. Next GET request returns updated data from cache
5. Cache and database stay synchronized!
```

#### Scenario 4: Delete Product
```
1. User calls: DELETE /api/products/1
2. Service deletes from database
3. @CacheEvict automatically removes from Redis
4. Next GET request returns 404 (not found)
5. No stale data in cache!
```

---

## 📊 Data Flow Diagrams

### Cache Miss Flow (First Request)
```
User → Controller → Service → Redis (Check)
                              ↓ Not Found
                         Database (Query - 2s)
                              ↓
                         Redis (Store)
                              ↓
                         Return to User
```

### Cache Hit Flow (Subsequent Requests)
```
User → Controller → Service → Redis (Check)
                              ↓ Found!
                         Return to User (15ms)
```

---

## 🔑 Key Components Explained

### 1. Cache Annotations

| Annotation | When to Use | What It Does |
|------------|-------------|--------------|
| `@Cacheable` | Read operations | Checks cache first, queries DB if miss, stores result |
| `@CachePut` | Update operations | Always queries DB, updates cache with result |
| `@CacheEvict` | Delete operations | Removes entry from cache |

### 2. Cache Key Structure

Redis stores data as key-value pairs:
```
Key: "products::1"
Value: {"id":1,"name":"MacBook Pro 16","price":3499.99,...}

Key: "products::5"
Value: {"id":5,"name":"iPhone 15 Pro","price":1199.99,...}
```

### 3. Serialization

**Why needed?** Redis stores data as bytes, Java objects must be converted.

**How it works**:
- Java Object → JSON → Bytes → Redis (Store)
- Redis → Bytes → JSON → Java Object (Retrieve)

Configured in `RedisConfig.java`:
```java
new GenericJackson2JsonRedisSerializer()  // Handles conversion
```

---

## 🎯 Summary

### What Each File Does:

| File | Purpose | Redis Related? |
|------|---------|----------------|
| `RedisDemoApplication.java` | Main entry, enables caching | ✅ Yes (`@EnableCaching`) |
| `RedisConfig.java` | Redis connection & setup | ✅ Yes (core config) |
| `OpenApiConfig.java` | Swagger documentation | ❌ No |
| `DataLoader.java` | Load 500 sample products | ❌ No |
| `Product.java` | Data model | ✅ Yes (must be Serializable) |
| `ProductRepository.java` | Database access | ❌ No |
| `ProductService.java` | **Caching logic** | ✅ **YES (main caching)** |
| `ProductController.java` | REST API + logging | ✅ Yes (shows cache performance) |

### The Magic Happens In:
1. **`ProductService.java`** - Cache annotations (`@Cacheable`, `@CachePut`, `@CacheEvict`)
2. **`RedisConfig.java`** - Redis connection and serialization
3. **`RedisDemoApplication.java`** - `@EnableCaching` annotation

---

## 💡 Key Takeaways

1. **Redis is transparent** - Just add annotations, Spring handles the rest
2. **Automatic synchronization** - Updates and deletes automatically manage cache
3. **Huge performance gain** - 135x faster with caching
4. **Simple to use** - 3 annotations do all the work
5. **Production ready** - Same pattern used by Amazon, Netflix, Google

---

**Want to see it in action?** Run the app and watch the console logs! 🚀
