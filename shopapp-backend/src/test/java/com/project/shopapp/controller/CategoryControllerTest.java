package com.project.shopapp.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.configurations.SecurityConfig;
import com.project.shopapp.configurations.WebSecurityConfig;
import com.project.shopapp.controllers.CategoryController;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.filters.JwtTokenFilter;
import com.project.shopapp.models.Category;

import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.CategoryService;
import com.project.shopapp.utils.MessageKeys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.User;


// Import các cấu hình bảo mật của bạn
@WebMvcTest(controllers = CategoryController.class)
@Import({WebSecurityConfig.class, SecurityConfig.class, JwtTokenUtils.class})
//Cung cấp cau hinh
@TestPropertySource(properties = {
        "api.prefix=/api/v1",
        "jwt.expiration=2592000",
        "jwt.secretKey=TaqlmGv1iEDMRiFp/pHuID1+T84IABfuA0xXh4GhiUI=",
        "app.test.mode=true"
})
public class CategoryControllerTest {

    // Mô phỏng gửi request HTTP đến controller
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private LocalizationUtils localizationUtils;

    // Cung cấp UserDetailsService để JwtTokenFilter có thể load user khi validate token
    @MockBean
    private UserDetailsService userDetailsService;

    // giữ nguyên hành vi thực của JwtTokenFilter (không hoàn toàn mock)
    @SpyBean
    private JwtTokenFilter jwtTokenFilter;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    // Token hợp lệ dùng cho test
    private String validToken;

    @BeforeEach
    public void setup() throws Exception {
        com.project.shopapp.models.User dummyUser = com.project.shopapp.models.User.builder()
                .id(1L)
                .phoneNumber("admin")
                .build();


        // Cài đặt UserDetailsService: khi load "admin", trả về đối tượng UserDetails có role ADMIN
        UserDetails adminUser = User.withUsername("admin")
                .password("password") // Mật khẩu không quan trọng trong test
                .roles("ADMIN")
                .build();
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(adminUser);

        // Sinh token hợp lệ từ dummyUser
        validToken = jwtTokenUtils.generateToken(dummyUser);
        assertTrue(jwtTokenUtils.validateToken(validToken, adminUser), "Token không hợp lệ");
    }

    @Test
    public void getById_ReturnsCategory_WithValidToken() throws Exception {
        Long categoryId = 1L;
        Category category = Category.builder()
                .id(categoryId)
                .name("Laptop ASUS")
                .build();

        when(categoryService.getCategoryById(categoryId)).thenReturn(category);

        // Gọi endpoint GET với header Authorization chứa token hợp lệ.
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Laptop ASUS"));

        verify(categoryService).getCategoryById(categoryId);
    }

    // Test thành công với dữ liệu hợp lệ
    @Test
    public void createCategory_Success() throws Exception {
        // Tạo DTO hợp lệ
        CategoryDTO validDto = new CategoryDTO();
        validDto.setName("Laptop ASUS");

        // Giả lập hành vi của service: khi gọi createCategory, trả về đối tượng Category đã tạo
        Category createdCategory = Category.builder()
                .id(1L)
                .name(validDto.getName())
                .build();
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(createdCategory);

        // Chuyển DTO thành JSON
        String jsonBody = objectMapper.writeValueAsString(validDto);

        // Gọi POST request với header Authorization và CSRF token
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + validToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.id").value(createdCategory.getId()))
                .andExpect(jsonPath("$.category.name").value("Laptop ASUS"));

        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    // Test lỗi binding: dữ liệu không hợp lệ (name rỗng)
    @Test
    public void createCategory_BindingError() throws Exception {
        // Tạo DTO không hợp lệ: name rỗng
        CategoryDTO invalidDto = new CategoryDTO();
        invalidDto.setName("");

        // Giả lập hành vi của localizationUtils khi có lỗi binding
        when(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_FAILED))
                .thenReturn("Insert category failed");

        String jsonBody = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + validToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insert category failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    public void createCategory_Forbidden_ForUser() throws Exception {
        // Tạo dummy user với role USER
        com.project.shopapp.models.User dummyUserUser = com.project.shopapp.models.User.builder()
                .id(2L)
                .phoneNumber("user")
                .build();

        // Cài đặt UserDetailsService: khi load "user", trả về đối tượng UserDetails có role USER
        UserDetails userUser = org.springframework.security.core.userdetails.User.withUsername("user")
                .password("password") // mật khẩu không quan trọng trong test
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userUser);

        // Sinh token hợp lệ từ dummyUserUser (với role USER)
        String userToken = jwtTokenUtils.generateToken(dummyUserUser);
        assertTrue(jwtTokenUtils.validateToken(userToken, userUser), "Token không hợp lệ");

        // Tạo DTO hợp lệ
        CategoryDTO validDto = new CategoryDTO();
        validDto.setName("Laptop ASUS");
        String jsonBody = objectMapper.writeValueAsString(validDto);

        // Gọi POST request với token của người dùng có role USER
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());
    }
}