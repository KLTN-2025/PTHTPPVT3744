package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.model.Brand;
import com.example.do_an_tot_nghiep.repository.IBrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService implements IBrandService {
    @Autowired
    private IBrandRepository brandRepository;
    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
}
