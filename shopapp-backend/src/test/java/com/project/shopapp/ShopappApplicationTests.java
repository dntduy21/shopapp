package com.project.shopapp;

import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShopappApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category savedCategory;

    @BeforeEach
    public void setup() {
        // Khởi tạo dữ liệu test (ví dụ một category)
        Category category = Category.builder()
                .name("Laptop ASUS")
                .build();
        savedCategory = categoryRepository.save(category);
    }

    @Test
    public void testGetCategoryById() {
        // Tạo URL dựa vào port và api.prefix (ở đây api.prefix = "/api/v1")
        String url = "http://localhost:" + port + "/api/v1/categories/" + savedCategory.getId();

        ResponseEntity<Category> response = restTemplate.getForEntity(url, Category.class);

        // Kiểm tra status code và nội dung trả về
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code không đúng");
        Category categoryResponse = response.getBody();
        assertNotNull(categoryResponse, "Category trả về là null");
        assertEquals(savedCategory.getId(), categoryResponse.getId(), "ID không khớp");
        assertEquals(savedCategory.getName(), categoryResponse.getName(), "Tên không khớp");
    }

}
