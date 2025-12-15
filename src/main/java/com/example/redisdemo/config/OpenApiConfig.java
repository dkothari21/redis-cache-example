package com.example.redisdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Redis Cache Example API")
                                                .version("1.0.0")
                                                .description(
                                                                """
                                                                                ## Product Catalog API with Redis Caching

                                                                                This API demonstrates **Redis caching** in a real-world e-commerce product catalog scenario.

                                                                                ### 🎯 Key Features:
                                                                                - **Fast Product Retrieval**: Redis caching reduces response time by 60x
                                                                                - **Automatic Cache Management**: Cache updates and evictions handled automatically
                                                                                - **Complete CRUD Operations**: Create, Read, Update, and Delete products

                                                                                ### 🧪 Testing Cache Behavior:
                                                                                1. Create a product using `POST /api/products`
                                                                                2. Get the product by ID using `GET /api/products/{id}` - First call takes ~3 seconds
                                                                                3. Get the same product again - Second call returns instantly from Redis cache!
                                                                                4. Update the product using `PUT /api/products/{id}` - Cache is automatically updated
                                                                                5. Delete the product using `DELETE /api/products/{id}` - Cache is automatically cleared

                                                                                ### 📊 Performance:
                                                                                - **Cache Miss** (first request): ~3000ms
                                                                                - **Cache Hit** (subsequent requests): <50ms
                                                                                - **Improvement**: 60x faster!

                                                                                ### 🔧 Technologies:
                                                                                - Spring Boot 3.3.6
                                                                                - Spring Data Redis
                                                                                - H2 In-Memory Database
                                                                                - Redis Cache
                                                                                """)
                                                .contact(new Contact()
                                                                .name("Redis Cache Example")
                                                                .url("https://github.com/yourusername/redis-cache-example")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Local Development Server")));
        }
}
