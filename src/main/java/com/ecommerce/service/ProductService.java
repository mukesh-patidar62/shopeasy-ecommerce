package com.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

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

    public Product save(Product product, MultipartFile imageFile, User admin) throws Exception {
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageUrl(uploadToCloudinary(imageFile));
        }
        if (product.getCreatedBy() == null) {
            product.setCreatedBy(admin);
        }
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Uploads the image to Cloudinary (persistent cloud storage) and returns
     * the permanent HTTPS URL. This survives redeploys, unlike local disk
     * storage, which gets wiped every time Render spins up a fresh container.
     */
    private String uploadToCloudinary(MultipartFile file) throws Exception {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));

        Map<String, Object> options = new HashMap<>();
        options.put("folder", "shopeasy-products");

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }
}
