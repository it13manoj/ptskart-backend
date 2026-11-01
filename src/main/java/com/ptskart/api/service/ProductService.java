package com.ptskart.api.service;

import com.ptskart.api.entity.Product;
import com.ptskart.api.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product details) {
        Product p = getProductById(id);
        p.setName(details.getName());
        p.setBrand(details.getBrand());
        p.setSubtitle(details.getSubtitle());
        p.setCategory(details.getCategory());
        p.setPrice(details.getPrice());
        p.setOriginalPrice(details.getOriginalPrice());
        p.setBadge(details.getBadge());
        p.setBadgeType(details.getBadgeType());
        p.setRating(details.getRating());
        p.setReviewsCount(details.getReviewsCount());
        p.setSoldCount(details.getSoldCount());
        p.setBoughtPastMonth(details.getBoughtPastMonth());
        p.setPrimeEligible(details.getPrimeEligible());
        p.setStockQuantity(details.getStockQuantity());
        p.setInStock(details.getInStock());
        p.setEmoji(details.getEmoji());
        p.setDescription(details.getDescription());
        p.setImagesJson(details.getImagesJson());
        p.setFeaturesJson(details.getFeaturesJson());
        p.setSpecificationsJson(details.getSpecificationsJson());
        return productRepository.save(p);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
