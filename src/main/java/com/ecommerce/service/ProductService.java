package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Product not found: " + id));
    }

    public Product save(Product product, MultipartFile imageFile, User admin) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageUrl(storeImage(imageFile));
        }
        if (product.getCreatedBy() == null) {
            product.setCreatedBy(admin);
        }
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    private String storeImage(MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + ext;

        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Path target = dirPath.resolve(filename);
        Files.copy(file.getInputStream(), target);

        return "/uploads/" + filename;
    }
}
