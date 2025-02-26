package com.project.shopapp.services;

import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ICategoryServiceTest {
    private ICategoryService iCategoryService;

    private @Mock CategoryRepository categoryRepository;
    private static Category asus;
    private static Category hp;

    @BeforeAll
    static void beforeAll() {
        asus = new Category();
        asus.setId(1L);
        asus.setName("Laptop ASUS");

        hp = new Category();
        hp.setId(2L);
        hp.setName("Laptop HP");
    }

    @BeforeEach
    void setUp() {
        // Khởi tạo bước triển khai là CategoryService
        iCategoryService = new CategoryService(categoryRepository);
    }

    @Test
    void getCategoryById() {
        // Trường hợp 1: Tìm thấy category theo id
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(asus));
        Category result = iCategoryService.getCategoryById(1L);
        assertNotNull(result);
        assertEquals(asus.getId(), result.getId());
        assertEquals(asus.getName(), result.getName());
        verify(categoryRepository, times(1)).findById(1L);

        // Trường hợp 2: Không tìm thấy category -> ném RuntimeException với thông báo "Category not found"
        when(categoryRepository.findById(100L)).thenReturn(Optional.empty());
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            iCategoryService.getCategoryById(100L);
        });
        assertEquals("Category not found", thrownException.getMessage());
        verify(categoryRepository, times(1)).findById(100L);
    }

    @Test
    void getAllCategories() {
        // Trường hợp 1: Có danh sách các category
        List<Category> categoryList = Arrays.asList(asus, hp);
        when(categoryRepository.findAll()).thenReturn(categoryList);
        List<Category> result = iCategoryService.getAllCategories();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(asus));
        assertTrue(result.contains(hp));
        verify(categoryRepository, times(1)).findAll();

        // Trường hợp 2: Không có category nào trong repository (danh sách rỗng)
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        List<Category> emptyResult = iCategoryService.getAllCategories();
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
        // Tổng số lần gọi getAllCategories
        verify(categoryRepository, times(2)).findAll();
    }
}