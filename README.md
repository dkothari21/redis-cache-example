# Redis Cache Example - Product Catalog

A real-world Spring Boot application demonstrating **Redis caching** with a Product Catalog example. This project shows how Redis improves application performance by caching frequently accessed data.

## 📋 Table of Contents

- [What is Redis?](#what-is-redis)
- [Real-Life Use Case](#real-life-use-case)
- [How Caching Works in This App](#how-caching-works-in-this-app)
- [Prerequisites](#prerequisites)
- [Redis Installation](#redis-installation)
- [Running the Application](#running-the-application)
- [Testing the Application](#testing-the-application)
- [API Endpoints](#api-endpoints)
- [Understanding Cache Behavior](#understanding-cache-behavior)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)

---

## 🔍 What is Redis?

**Redis** (Remote Dictionary Server) is an in-memory data structure store used as:
- **Cache**: Store frequently accessed data in memory for fast retrieval
- **Database**: Persistent key-value storage
- **Message Broker**: Pub/sub messaging

**Why Redis for Caching?**
- ⚡ **Extremely Fast**: Data stored in RAM (microsecond latency)
- 💾 **Reduces Database Load**: Fewer queries to your primary database
- 📈 **Scalable**: Handles millions of requests per second
- 🔄 **Automatic Expiration**: TTL (Time To Live) for cache entries

---

## 🛒 Real-Life Use Case

**E-Commerce Product Catalog**

Imagine an online store like Amazon where:
- Products are viewed **thousands of times** per day
- Product details **rarely change** (name, description, price)
- Database queries are **expensive** and slow

**Without Redis:**
- Every product view = Database query (slow, ~3 seconds)
- High database load
- Poor user experience

**With Redis:**
- First view = Database query + Store in Redis (slow, ~3 seconds)
- Subsequent views = Fetch from Redis (fast, <50ms)
- 60x faster response times!
- Reduced database load by 95%

---

## ⚙️ How Caching Works in This App

### Cache Annotations Used

1. **`@Cacheable`** - Get Product by ID
   ```java
   @Cacheable(value = "products", key = "#id")
   public Optional<Product> getProductById(Long id)
   ```
   - **First call**: Fetches from database → Stores in Redis
   - **Subsequent calls**: Returns from Redis (instant!)

2. **`@CachePut`** - Update Product
   ```java
   @CachePut(value = "products", key = "#id")
   public Product updateProduct(Long id, Product productDetails)
   ```
   - Updates database AND cache simultaneously
   - Cache stays in sync with database

3. **`@CacheEvict`** - Delete Product
   ```java
   @CacheEvict(value = "products", key = "#id")
   public void deleteProduct(Long id)
   ```
   - Deletes from database AND removes from cache
   - Prevents stale data

---

## 📦 Prerequisites

- **Java 17** or higher
- **Gradle** (or use the Gradle wrapper included)
- **Redis** (see installation below)

---

## 🚀 Redis Installation

### macOS (using Homebrew)

```bash
# Install Redis
brew install redis

# Start Redis server
brew services start redis

# Verify Redis is running
redis-cli ping
# Should return: PONG
```

### Linux (Ubuntu/Debian)

```bash
# Install Redis
sudo apt update
sudo apt install redis-server

# Start Redis
sudo systemctl start redis-server

# Enable Redis to start on boot
sudo systemctl enable redis-server

# Verify Redis is running
redis-cli ping
# Should return: PONG
```

### Windows

1. Download Redis from: https://github.com/microsoftarchive/redis/releases
2. Extract and run `redis-server.exe`
3. Or use **Docker**:
   ```bash
   docker run -d -p 6379:6379 redis:latest
   ```

### Verify Redis Installation

```bash
# Connect to Redis CLI
redis-cli

# Test commands
127.0.0.1:6379> SET test "Hello Redis"
127.0.0.1:6379> GET test
# Should return: "Hello Redis"

127.0.0.1:6379> exit
```

---

## 📚 Swagger UI - Interactive API Documentation

This application includes **Swagger UI** for easy API testing and documentation!

### Access Swagger UI

Once the application is running, open your browser and navigate to:

**http://localhost:8080/swagger-ui.html**

### What You'll See

- **Complete API Documentation**: All endpoints with descriptions
- **Try It Out**: Test APIs directly from your browser
- **Request/Response Examples**: See exactly what to send and expect
- **Cache Behavior Explained**: Each endpoint shows whether it's cached

### Using Swagger UI to Test Redis Caching

1. **Expand "Product Management"** section
2. **Create a Product**:
   - Click `POST /api/products`
   - Click "Try it out"
   - Use this example JSON:
   ```json
   {
     "name": "MacBook Pro 16",
     "description": "Apple M3 Max chip, 36GB RAM, 1TB SSD",
     "price": 3499.99,
     "category": "Electronics",
     "stockQuantity": 15
   }
   ```
   - Click "Execute"
   - Note the product ID in the response (usually 1)

3. **Test Cache Behavior**:
   - Click `GET /api/products/{id}` (marked with ⚡ CACHED)
   - Click "Try it out"
   - Enter the product ID (e.g., 1)
   - Click "Execute" - **Takes ~3 seconds** (cache miss)
   - Click "Execute" again - **Instant response!** (cache hit)

4. **Update Product** (updates cache):
   - Click `PUT /api/products/{id}` (marked with 🔄)
   - Test updating and see cache automatically update

5. **Delete Product** (evicts cache):
   - Click `DELETE /api/products/{id}` (marked with 🗑️)
   - Test deletion and see cache automatically cleared

### Swagger vs cURL

- **Swagger UI**: Great for visual exploration and quick testing
- **cURL**: Better for automation and scripting

Use whichever you prefer! Both methods are documented below.

---

## 🏃 Running the Application

### 1. Navigate to Project Directory

```bash
cd /Users/dipkothari/Project/redis-cache-example
```

### 2. Build the Project

```bash
./gradlew build
```

### 3. Run the Application

```bash
./gradlew bootRun
```

The application will start on **http://localhost:8080**

You should see logs indicating:
```
✅ Started RedisDemoApplication in X seconds
✅ Successfully loaded 500 products into the database!
✅ Tomcat started on port(s): 8080
```

**Note:** The application automatically creates **500 sample products** on startup! 🎉

---

## 🧪 Testing the Application

### ⭐ Super Easy Guide (Recommended for Everyone!)

See **[QUICKSTART.md](QUICKSTART.md)** for a **kid-friendly, step-by-step guide** with pictures and emojis! 🎮

Perfect for beginners or anyone who wants simple instructions!

### Option 1: Using Swagger UI (Visual & Interactive)

1. Open **http://localhost:8080/swagger-ui.html** in your browser
2. Follow the interactive guide in the Swagger UI section above
3. Test all endpoints visually with the "Try it out" button
4. **500 products are already loaded** - just start testing!

### Option 2: Using cURL (Command Line)

### Step 1: Create Sample Products

```bash
# Create Product 1 - Laptop
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16",
    "description": "Apple M3 Max chip, 36GB RAM, 1TB SSD",
    "price": 3499.99,
    "category": "Electronics",
    "stockQuantity": 15
  }'

# Create Product 2 - Phone
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "description": "256GB, Titanium Blue",
    "price": 1199.99,
    "category": "Electronics",
    "stockQuantity": 50
  }'

# Create Product 3 - Book
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Clean Code",
    "description": "A Handbook of Agile Software Craftsmanship by Robert C. Martin",
    "price": 42.99,
    "category": "Books",
    "stockQuantity": 100
  }'
```

### Step 2: Test Cache Behavior (THE MAGIC!)

#### First Request (Cache Miss - Slow)

```bash
# This will take ~3 seconds (simulated slow database)
time curl http://localhost:8080/api/products/1
```

**Expected Output:**
- Response time: ~3000ms
- Console log: `Fetching product with ID 1 from database (CACHE MISS)`
- Product data returned

#### Second Request (Cache Hit - Fast!)

```bash
# This will be instant! (<50ms)
time curl http://localhost:8080/api/products/1
```

**Expected Output:**
- Response time: <50ms (60x faster!)
- No database query log (served from Redis)
- Same product data returned

### Step 3: Verify Cache in Redis

```bash
# Open Redis CLI
redis-cli

# List all keys
127.0.0.1:6379> KEYS *
# Should show: "products::1"

# View cached product
127.0.0.1:6379> GET "products::1"
# Shows JSON representation of the product

# Exit Redis CLI
127.0.0.1:6379> exit
```

### Step 4: Test Cache Update

```bash
# Update product (updates both DB and cache)
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16 (Updated)",
    "description": "Apple M3 Max chip, 64GB RAM, 2TB SSD",
    "price": 4299.99,
    "category": "Electronics",
    "stockQuantity": 10
  }'

# Fetch again - should be instant AND show updated data
curl http://localhost:8080/api/products/1
```

### Step 5: Test Cache Eviction

```bash
# Delete product (removes from DB and cache)
curl -X DELETE http://localhost:8080/api/products/1

# Try to fetch - should return 404
curl http://localhost:8080/api/products/1
```

---

## 📡 API Endpoints

### Swagger UI

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Interactive API Documentation |
| `http://localhost:8080/api-docs` | OpenAPI JSON Specification |

### Product CRUD Operations

| Method | Endpoint | Description | Cached? |
|--------|----------|-------------|---------|
| `GET` | `/api/products` | Get all products | ❌ No |
| `GET` | `/api/products/{id}` | Get product by ID | ✅ Yes |
| `POST` | `/api/products` | Create new product | ❌ No |
| `PUT` | `/api/products/{id}` | Update product | ✅ Updates cache |
| `DELETE` | `/api/products/{id}` | Delete product | ✅ Evicts cache |

### Additional Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products/category/{category}` | Get products by category |
| `GET` | `/api/products/search?name={name}` | Search products by name |
| `POST` | `/api/products/cache/clear` | Clear all cache entries |

### Example Requests

```bash
# Get all products
curl http://localhost:8080/api/products

# Get products by category
curl http://localhost:8080/api/products/category/Electronics

# Search products
curl http://localhost:8080/api/products/search?name=MacBook

# Clear cache
curl -X POST http://localhost:8080/api/products/cache/clear
```

---

## 📊 Understanding Cache Behavior

### Performance Comparison

| Scenario | Without Redis | With Redis | Improvement |
|----------|---------------|------------|-------------|
| First request | 3000ms | 3000ms | Same (cache miss) |
| Second request | 3000ms | 50ms | **60x faster** |
| 100 requests | 300 seconds | 3.05 seconds | **98% faster** |

### Cache Flow Diagram

```
User Request → Controller → Service
                              ↓
                         Check Redis Cache
                              ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
              Cache HIT            Cache MISS
                    ↓                   ↓
            Return from Redis    Query Database
                                       ↓
                                 Store in Redis
                                       ↓
                                 Return to User
```

### Monitoring Cache Activity

Watch the application logs to see cache behavior:

```bash
# Run the app and watch logs
./gradlew bootRun

# In another terminal, make requests
curl http://localhost:8080/api/products/1  # Cache MISS
curl http://localhost:8080/api/products/1  # Cache HIT (no DB log)
```

**Look for these log messages:**
- `Fetching product with ID X from database (CACHE MISS)` - Data fetched from DB
- No log on second request = Cache HIT (served from Redis)

---

## 📁 Project Structure

```
redis-cache-example/
├── build.gradle                          # Dependencies and build configuration
├── src/main/java/com/example/redisdemo/
│   ├── RedisDemoApplication.java         # Main application class
│   ├── config/
│   │   └── RedisConfig.java              # Redis configuration
│   ├── model/
│   │   └── Product.java                  # Product entity
│   ├── repository/
│   │   └── ProductRepository.java        # JPA repository
│   ├── service/
│   │   └── ProductService.java           # Business logic + caching
│   └── controller/
│       └── ProductController.java        # REST API endpoints
└── src/main/resources/
    └── application.properties            # App configuration
```

---

## 🛠️ Technologies Used

- **Spring Boot 3.3.6** - Application framework
- **Spring Data JPA** - Database access
- **Spring Data Redis** - Redis integration
- **Spring Cache** - Caching abstraction
- **Redis (Jedis)** - Redis client
- **H2 Database** - In-memory database
- **Lombok** - Reduce boilerplate code
- **Gradle** - Build tool

---

## 🎯 Key Learning Points

1. **`@Cacheable`**: Caches method results (read operations)
2. **`@CachePut`**: Updates cache (write operations)
3. **`@CacheEvict`**: Removes from cache (delete operations)
4. **Serialization**: Objects must be `Serializable` for Redis storage
5. **TTL**: Cache entries expire after 10 minutes (configurable)
6. **Cache Key**: Uses method parameters (e.g., product ID)

---

## 🐛 Troubleshooting

### Redis Connection Error

**Error:** `Unable to connect to Redis`

**Solution:**
```bash
# Check if Redis is running
redis-cli ping

# If not running, start Redis
brew services start redis  # macOS
sudo systemctl start redis-server  # Linux
```

### Port Already in Use

**Error:** `Port 8080 is already in use`

**Solution:**
```bash
# Change port in application.properties
server.port=8081
```

### Cache Not Working

**Solution:**
```bash
# Clear Redis cache manually
redis-cli FLUSHALL

# Restart the application
./gradlew bootRun
```

---

## 📚 Additional Resources

- [Spring Boot Redis Documentation](https://spring.io/projects/spring-data-redis)
- [Redis Official Documentation](https://redis.io/documentation)
- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)

---

## 📝 License

This project is for educational purposes.

---

## 👨‍💻 Author

Created to demonstrate Redis caching in Spring Boot applications.

**Happy Caching! 🚀**
