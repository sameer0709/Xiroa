package com.erp.service;

import com.erp.dto.PurchaseOrderRequest;
import com.erp.entity.*;
import com.erp.enums.OrderStatus;
import com.erp.enums.StockMovementType;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;

    private static final DateTimeFormatter NUMBER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public PurchaseOrderService(PurchaseOrderRepository poRepository,
            VendorRepository vendorRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository) {
        this.poRepository = poRepository;
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<PurchaseOrder> getAll() {
        return poRepository.findAll();
    }

    public PurchaseOrder getById(Long id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found: " + id));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderRequest req) {
        Vendor vendor = vendorRepository.findById(req.vendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setVendor(vendor);
        po.setPoNumber("PO-" + LocalDateTime.now().format(NUMBER_FMT));
        po.setOrderDate(LocalDateTime.now());
        po.setStatus(OrderStatus.DRAFT);
        po.setNotes(req.notes());

        BigDecimal total = BigDecimal.ZERO;
        for (var itemReq : req.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            BigDecimal unitCost = itemReq.unitCost() == null ? product.getCostPrice() : itemReq.unitCost();
            BigDecimal lineTotal = unitCost.multiply(BigDecimal.valueOf(itemReq.quantity()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setUnitCost(unitCost);
            item.setLineTotal(lineTotal);
            po.addItem(item);
            total = total.add(lineTotal);
        }
        po.setTotalAmount(total);
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder receive(Long id) {
        PurchaseOrder po = getById(id);
        if (po.getStatus() == OrderStatus.CONFIRMED || po.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("PO already received");
        }
        for (PurchaseOrderItem item : po.getItems()) {
            Inventory inv = inventoryRepository.findByProductId(item.getProduct().getId())
                    .orElseGet(() -> {
                        Inventory i = new Inventory();
                        i.setProduct(item.getProduct());
                        i.setQuantityOnHand(0);
                        return i;
                    });
            inv.setQuantityOnHand(inv.getQuantityOnHand() + item.getQuantity());
            inventoryRepository.save(inv);

            StockMovement movement = new StockMovement();
            movement.setProduct(item.getProduct());
            movement.setType(StockMovementType.PURCHASE);
            movement.setQuantity(item.getQuantity());
            movement.setUnitCost(item.getUnitCost());
            movement.setReference(po.getPoNumber());
            movement.setNotes("Purchase order received");
            stockMovementRepository.save(movement);
        }
        po.setStatus(OrderStatus.DELIVERED);
        return poRepository.save(po);
    }
}
