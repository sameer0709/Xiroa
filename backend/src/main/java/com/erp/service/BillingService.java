package com.erp.service;

import com.erp.dto.InvoiceRequest;
import com.erp.dto.PaymentRequest;
import com.erp.entity.*;
import com.erp.enums.PaymentStatus;
import com.erp.enums.StockMovementType;
import com.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;

    private static final DateTimeFormatter NUMBER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public BillingService(InvoiceRepository invoiceRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    public Invoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
    }

    @Transactional
    public Invoice create(InvoiceRequest req) {
        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceNumber("INV-" + LocalDateTime.now().format(NUMBER_FMT));
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setNotes(req.notes());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;

        for (var itemReq : req.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));

            Inventory inv = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new RuntimeException("No inventory record for product " + product.getName()));
            if (inv.getAvailableQuantity() < itemReq.quantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName() +
                        " (available: " + inv.getAvailableQuantity() + ")");
            }

            BigDecimal unitPrice = itemReq.unitPrice() == null ? product.getSellingPrice() : itemReq.unitPrice();
            BigDecimal gstRate = product.getGstRate();

            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(unitPrice);
            item.setGstRate(gstRate);
            item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity())));
            item.setGstAmount(
                    item.getSubtotal().multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            item.setLineTotal(item.getSubtotal().add(item.getGstAmount()));

            invoice.addItem(item);

            subtotal = subtotal.add(item.getSubtotal());
            gstTotal = gstTotal.add(item.getGstAmount());

            // Reduce stock
            inv.setQuantityOnHand(inv.getQuantityOnHand() - itemReq.quantity());
            inventoryRepository.save(inv);

            StockMovement movement = new StockMovement();
            movement.setProduct(product);
            movement.setType(StockMovementType.SALE);
            movement.setQuantity(itemReq.quantity());
            movement.setUnitCost(product.getCostPrice());
            movement.setReference(invoice.getInvoiceNumber());
            movement.setNotes("Sale invoice line");
            stockMovementRepository.save(movement);
        }

        invoice.setSubtotal(subtotal);
        invoice.setGstAmount(gstTotal);
        invoice.setTotal(subtotal.add(gstTotal));

        BigDecimal paid = req.amountPaid() == null ? BigDecimal.ZERO : req.amountPaid();
        invoice.setAmountPaid(paid);
        invoice.setBalanceDue(invoice.getTotal().subtract(paid));
        invoice.setPaymentMode(req.paymentMode());
        invoice.setPaymentStatus(paid.compareTo(invoice.getTotal()) >= 0
                ? PaymentStatus.PAID
                : (paid.compareTo(BigDecimal.ZERO) > 0 ? PaymentStatus.PARTIALLY_PAID : PaymentStatus.UNPAID));

        invoice = invoiceRepository.save(invoice);

        // Update customer stats
        customer.setTotalPurchases(customer.getTotalPurchases().add(invoice.getTotal()));
        customer.setTotalOrders(customer.getTotalOrders() + 1);
        customerRepository.save(customer);

        return invoice;
    }

    @Transactional
    public Invoice recordPayment(Long id, PaymentRequest req) {
        Invoice invoice = getById(id);
        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Invoice already fully paid");
        }
        BigDecimal newPaid = invoice.getAmountPaid().add(req.amount());
        if (newPaid.compareTo(invoice.getTotal()) > 0) {
            throw new RuntimeException("Payment exceeds invoice total");
        }
        invoice.setAmountPaid(newPaid);
        invoice.setBalanceDue(invoice.getTotal().subtract(newPaid));
        invoice.setPaymentMode(req.paymentMode());
        invoice.setPaymentStatus(newPaid.compareTo(invoice.getTotal()) >= 0
                ? PaymentStatus.PAID
                : PaymentStatus.PARTIALLY_PAID);
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> outstanding() {
        return invoiceRepository.findOutstandingInvoices();
    }
}
