package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.model.Category;
import com.example.do_an_tot_nghiep.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService implements ICategoryService {
    @Autowired
    private ICategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrder();
    }
}
