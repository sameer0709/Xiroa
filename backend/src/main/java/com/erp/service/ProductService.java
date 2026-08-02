package com.erp.service;

import com.erp.dto.CategoryRequest;
import com.erp.dto.IdNameResponse;
import com.erp.dto.ProductRequest;
import com.erp.entity.Category;
import com.erp.entity.Inventory;
import com.erp.entity.Product;
import com.erp.entity.StockMovement;
import com.erp.enums.StockMovementType;
import com.erp.repository.CategoryRepository;
import com.erp.repository.InventoryRepository;
import com.erp.repository.ProductRepository;
import com.erp.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductService(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public List<Product> getActive() {
        return productRepository.findByActiveTrue();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Transactional
    public Product create(ProductRequest req) {
        if (productRepository.existsBySku(req.sku())) {
            throw new RuntimeException("SKU already exists: " + req.sku());
        }
        Product product = new Product();
        apply(product, req);
        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityOnHand(req.openingStock());
        inventory.setWarehouse("Main");
        inventoryRepository.save(inventory);

        if (req.openingStock() > 0) {
            var movement = new StockMovement();
            movement.setProduct(product);
            movement.setType(StockMovementType.ADJUSTMENT);
            movement.setQuantity(req.openingStock());
            movement.setUnitCost(product.getCostPrice());
            movement.setReference("OPENING");
            movement.setNotes("Opening stock on product creation");
            stockMovementRepository.save(movement);
        }
        return product;
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product product = getById(id);
        apply(product, req);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private void apply(Product product, ProductRequest req) {
        product.setSku(req.sku());
        product.setName(req.name());
        product.setDescription(req.description());
        product.setCostPrice(req.costPrice());
        product.setSellingPrice(req.sellingPrice());
        product.setGstRate(req.gstRate() == null ? java.math.BigDecimal.ZERO : req.gstRate());
        product.setHsnCode(req.hsnCode());
        product.setReorderLevel(req.reorderLevel());
        if (req.categoryId() != null) {
            product.setCategory(categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }
    }

    public List<Product> lowStock() {
        return productRepository.findLowStockProducts();
    }

    // ---- Categories ----
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public List<IdNameResponse> getCategoryIdNames() {
        return categoryRepository.findAll().stream()
                .map(c -> new IdNameResponse(c.getId(), c.getName()))
                .toList();
    }

    @Transactional
    public Category createCategory(CategoryRequest req) {
        Category category = new Category();
        category.setName(req.name());
        category.setDescription(req.description());
        return categoryRepository.save(category);
    }
}
