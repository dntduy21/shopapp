package com.project.shopapp.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.controllers.CategoryController;
import com.project.shopapp.models.Category;

import java.util.Arrays;
import java.util.List;

import com.project.shopapp.services.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

//dùng để kiểm thử các controller
@WebMvcTest(controllers = CategoryController.class,
        // loại trừ các cấu hình bảo mật theo kiểu component scanning
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.project.shopapp.configurations.SecurityConfig.class,
                        com.project.shopapp.configurations.WebSecurityConfig.class,
                        com.project.shopapp.components.JwtTokenUtils.class,
                        com.project.shopapp.filters.JwtTokenFilter.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false) // Tắt các bộ lọc bảo mật trong test
public class CategoryControllerTest {

    // dùng để mô phỏng các request HTTP đến controller
    @Autowired
    private MockMvc mockMvc;

    // Mock các bean phụ thuộc trong controller
    @MockBean
    private CategoryService categoryService;

    @MockBean
    private LocalizationUtils localizationUtils;

    // Test GET /api/v1/categories/{id}
    @Test
    public void getById_ReturnsCategory() throws Exception {
        //Tạo dữ liệu giả
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Laptop ASUS");

        // Thiết lập hành vi cho mock categoryService: Khi gọi getCategoryById với categoryId,
        // trả về đối tượng category đã định nghĩa
        when(categoryService.getCategoryById(categoryId)).thenReturn(category);

        // Thực hiện yêu cầu HTTP GET đến endpoint /api/v1/categories/{id} với categoryId
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                        // Đặt header Content-Type là application/json
                        .contentType(MediaType.APPLICATION_JSON))

                // Kiểm tra mã trạng thái HTTP là 200 OK
                .andExpect(status().isOk())
                // Kiểm tra trường id trong JSON phản hồi có giá trị bằng categoryId
                .andExpect(jsonPath("$.id").value(categoryId))
                // Kiểm tra trường name trong JSON phản hồi có giá trị là "Laptop ASUS"
                .andExpect(jsonPath("$.name").value("Laptop ASUS"));
    // Kiểm tra xem phương thức getCategoryById của categoryService có được gọi với categoryId hay không
        verify(categoryService).getCategoryById(categoryId);
    }

    // Test GET /api/v1/categories?page=...&limit=...
    @Test
    public void getAllCategories_ReturnsListOfCategories() throws Exception {
        //Tạo dữ liệu giả
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Laptop ASUS");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Laptop HP");

        List<Category> categories = Arrays.asList(category1, category2);

        // Giả lập behavior của service
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories")
                        .param("page", "0")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Kiểm tra số lượng phần tử trả về
                .andExpect(jsonPath("$.length()").value(categories.size()))
                // Kiểm tra nội dung của phần tử đầu tiên
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop ASUS"))
                // Kiểm tra phần tử thứ hai
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Laptop HP"));

        verify(categoryService).getAllCategories();
    }
}