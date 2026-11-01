package com.ptskart.api.config;

import com.ptskart.api.entity.*;
import com.ptskart.api.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final EmailConfigRepository emailConfigRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final CouponRepository couponRepository;
    private final GiftCardRepository giftCardRepository;
    private final VendorRepository vendorRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final StoreSettingRepository storeSettingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository,
                           EmailConfigRepository emailConfigRepository,
                           CategoryRepository categoryRepository,
                           WarehouseRepository warehouseRepository,
                           SupplierRepository supplierRepository,
                           CouponRepository couponRepository,
                           GiftCardRepository giftCardRepository,
                           VendorRepository vendorRepository,
                           SupportTicketRepository supportTicketRepository,
                           StoreSettingRepository storeSettingRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.emailConfigRepository = emailConfigRepository;
        this.categoryRepository = categoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
        this.couponRepository = couponRepository;
        this.giftCardRepository = giftCardRepository;
        this.vendorRepository = vendorRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.storeSettingRepository = storeSettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        seedUsers();
        seedCategories();
        seedProducts();
        seedOrders();
        seedEmailConfig();
        seedWarehouses();
        seedSuppliers();
        seedCoupons();
        seedGiftCards();
        seedVendors();
        seedTickets();
        seedSettings();
    }

    private void seedRoles() {
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            roleRepository.save(new Role("ROLE_ADMIN", "Store Owner & Super Administrator with unrestricted access"));
        }
        if (!roleRepository.existsByName("ROLE_CUSTOMER")) {
            roleRepository.save(new Role("ROLE_CUSTOMER", "Verified Customer storefront account with checkout and order history"));
        }
        if (!roleRepository.existsByName("ROLE_EDITOR")) {
            roleRepository.save(new Role("ROLE_EDITOR", "Catalog and merchandising editor with product management"));
        }
        if (!roleRepository.existsByName("ROLE_SUPPORT")) {
            roleRepository.save(new Role("ROLE_SUPPORT", "Customer care specialist with tickets, RMA, and customer support access"));
        }
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN", "Store Administrator")));
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER", "Storefront Customer")));

        if (!userRepository.existsByEmail("admin@ptskart.com")) {
            User admin = new User("Alex Chen", "admin@ptskart.com", passwordEncoder.encode("12345678"), adminRole);
            admin.setPhone("+1 (555) 019-2831");
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("sophie.laurent@client.com")) {
            User customer = new User("Sophie Laurent", "sophie.laurent@client.com", passwordEncoder.encode("12345678"), customerRole);
            customer.setPhone("+33 6 12 34 56 78");
            customer.setLoyaltyPoints(680);
            userRepository.save(customer);
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Apparel", "apparel", "Curated essentials tailored from certified organic raw textiles.", 38, "Active", "👕"));
            categoryRepository.save(new Category("Accessories", "accessories", "Hand-crafted leatherwork, mulberry silk scarves, and minimalist totes.", 24, "Active", "👜"));
            categoryRepository.save(new Category("Living & Decor", "living", "Architectural ceramic vessels, ambient brass lamps, and artisanal stoneware.", 19, "Active", "🏺"));
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<Product> products = new ArrayList<>();

            Product p1 = new Product();
            p1.setName("Cashmere Knit Sweater");
            p1.setSubtitle("100% Mongolian combed wool");
            p1.setCategory("Apparel");
            p1.setPrice(185.0);
            p1.setOriginalPrice(220.0);
            p1.setBadge("Best Seller");
            p1.setBadgeType("gold");
            p1.setRating(4.95);
            p1.setReviewsCount(142);
            p1.setSoldCount(620);
            p1.setStockQuantity(48);
            p1.setInStock(true);
            p1.setEmoji("🧶");
            p1.setDescription("Crafted with ultra-soft 2-ply Mongolian cashmere. Features ribbed cuffs, relaxed dropped shoulders, and a tailored silhouette.");
            products.add(p1);

            Product p2 = new Product();
            p2.setName("Minimalist Leather Tote");
            p2.setSubtitle("Full-grain vegetable-tanned leather");
            p2.setCategory("Accessories");
            p2.setPrice(240.0);
            p2.setOriginalPrice(280.0);
            p2.setBadge("Staff Pick");
            p2.setBadgeType("mint");
            p2.setRating(4.9);
            p2.setReviewsCount(98);
            p2.setSoldCount(410);
            p2.setStockQuantity(26);
            p2.setInStock(true);
            p2.setEmoji("👜");
            p2.setDescription("Handmade with vegetable-tanned Italian calfskin that develops an authentic patina over time. Includes interior 13\" laptop sleeve.");
            products.add(p2);

            Product p3 = new Product();
            p3.setName("Artisan Ceramic Vessel");
            p3.setSubtitle("Hand-thrown stoneware with matte glaze");
            p3.setCategory("Living");
            p3.setPrice(92.0);
            p3.setOriginalPrice(115.0);
            p3.setBadge("Limited");
            p3.setBadgeType("amber");
            p3.setRating(4.88);
            p3.setReviewsCount(64);
            p3.setSoldCount(280);
            p3.setStockQuantity(14);
            p3.setInStock(true);
            p3.setEmoji("🏺");
            p3.setDescription("Each vase is individually turned on the wheel in Kyoto. Subtle warmth, mineral glaze tones, and timeless aesthetic.");
            products.add(p3);

            Product p4 = new Product();
            p4.setName("Pure Belgian Linen Shirt");
            p4.setSubtitle("Pre-washed breathable organic weave");
            p4.setCategory("Apparel");
            p4.setPrice(130.0);
            p4.setOriginalPrice(150.0);
            p4.setBadge("Trending");
            p4.setBadgeType("gold");
            p4.setRating(4.86);
            p4.setReviewsCount(82);
            p4.setSoldCount(390);
            p4.setStockQuantity(65);
            p4.setInStock(true);
            p4.setEmoji("👔");
            p4.setDescription("Woven from 100% natural flax in Flanders. Naturally temperature-regulating with mother-of-pearl buttons.");
            products.add(p4);

            Product p5 = new Product();
            p5.setName("Architectural Brass Lamp");
            p5.setSubtitle("Solid satin brass with warm LED core");
            p5.setCategory("Living");
            p5.setPrice(310.0);
            p5.setOriginalPrice(360.0);
            p5.setBadge("Award Winner");
            p5.setBadgeType("mint");
            p5.setRating(4.98);
            p5.setReviewsCount(118);
            p5.setSoldCount(512);
            p5.setStockQuantity(19);
            p5.setInStock(true);
            p5.setEmoji("💡");
            p5.setDescription("Cast brass base with custom touch-dimmer module. Emits a rich 2700K ambient illumination.");
            products.add(p5);

            productRepository.saveAll(products);
        }
    }

    private void seedOrders() {
        if (orderRepository.count() == 0) {
            Order o1 = new Order();
            o1.setOrderNumber("#VX83291");
            o1.setCustomerName("Sophie Laurent");
            o1.setCustomerEmail("sophie.laurent@client.com");
            o1.setShippingAddress("14 Rue de Rivoli, Apt 4B, Paris 75001, France");
            o1.setSubtotal(425.0);
            o1.setShippingFee(0.0);
            o1.setTotalAmount(425.0);
            o1.setPaymentMethod("Apple Pay");
            o1.setPaymentStatus("PAID");
            o1.setOrderStatus("IN_TRANSIT");
            o1.setCarrier("DHL Express Worldwide");
            o1.setTrackingNumber("TRK982341902");
            o1.setEstimatedDelivery("Tomorrow, by 2:00 PM");
            o1.setCreatedAt(LocalDateTime.now().minusDays(1));

            OrderItem i1 = new OrderItem();
            i1.setOrder(o1);
            i1.setProductName("Cashmere Knit Sweater");
            i1.setVariant("Ivory / M");
            i1.setQuantity(1);
            i1.setUnitPrice(185.0);
            i1.setEmoji("🧶");

            OrderItem i2 = new OrderItem();
            i2.setOrder(o1);
            i2.setProductName("Minimalist Leather Tote");
            i2.setVariant("Cognac Tan");
            i2.setQuantity(1);
            i2.setUnitPrice(240.0);
            i2.setEmoji("👜");

            o1.getItems().add(i1);
            o1.getItems().add(i2);
            orderRepository.save(o1);
        }
    }

    private void seedEmailConfig() {
        if (emailConfigRepository.count() == 0) {
            EmailConfig cfg = new EmailConfig();
            cfg.setHost("smtp-relay.brevo.com");
            cfg.setPort(587);
            cfg.setUsername(System.getenv().getOrDefault("BREVO_USER", "bbd375001@smtp-brevo.com"));
            cfg.setPassword(System.getenv().getOrDefault("BREVO_SMTP_KEY", ""));
            cfg.setFromEmail(System.getenv().getOrDefault("SENDER_EMAIL", "info@batohidriver.com"));
            cfg.setFromName("Ptskart Commerce");
            cfg.setSmtpAuth(true);
            cfg.setStartTls(true);
            cfg.setSslEnable(false);
            cfg.setActive(true);
            emailConfigRepository.save(cfg);
        }
    }

    private void seedWarehouses() {
        if (warehouseRepository.count() == 0) {
            warehouseRepository.save(new Warehouse("London Logistics Center", "WH-LON-01", "Park Royal, London, UK", 45000, 72, "Marcus Vance", "+44 20 7946 0912"));
            warehouseRepository.save(new Warehouse("Paris Gateway Hub", "WH-PAR-02", "Roissy CDG, Paris, France", 30000, 58, "Claire Dubois", "+33 1 48 16 00 00"));
            warehouseRepository.save(new Warehouse("New York Distribution", "WH-NYC-01", "Long Island City, NY, USA", 60000, 81, "David Miller", "+1 718 555 0192"));
        }
    }

    private void seedSuppliers() {
        if (supplierRepository.count() == 0) {
            supplierRepository.save(new Supplier("Flanders Linen Mills", "Jan De Smet", "orders@flanderslinen.be", "+32 9 234 5678", "Belgium", "Apparel"));
            supplierRepository.save(new Supplier("Tuscan Leathercraft Studio", "Marco Bellini", "craft@tuscanleather.it", "+39 055 123456", "Italy", "Accessories"));
            supplierRepository.save(new Supplier("Kyoto Ceramic Guild", "Kenji Sato", "export@kyotoceramics.jp", "+81 75 123 4567", "Japan", "Living"));
        }
    }

    private void seedCoupons() {
        if (couponRepository.count() == 0) {
            couponRepository.save(new Coupon("HARMONY10", "Autumn Studio 10% Off Sitewide", "PERCENTAGE", 10.0, 50.0, 2000));
            couponRepository.save(new Coupon("WELCOME20", "New Collector Welcome $20 Off", "FIXED", 20.0, 100.0, 500));
        }
    }

    private void seedGiftCards() {
        if (giftCardRepository.count() == 0) {
            giftCardRepository.save(new GiftCard("GIFT-HARMONY-8841", 150.0, 150.0, "sophie.laurent@client.com", "Sophie Laurent"));
        }
    }

    private void seedVendors() {
        if (vendorRepository.count() == 0) {
            vendorRepository.save(new Vendor("Studio Kanso", "Kenji Sato", "kanso@studio.jp", "+81 75 999 1122", 12.0, 4.95, "Active"));
            vendorRepository.save(new Vendor("Atelier Marais", "Camille Moreau", "marais@atelier.fr", "+33 1 42 68 00 00", 14.0, 4.9, "Active"));
            vendorRepository.save(new Vendor("Nordic Form House", "Lars Lindqvist", "info@nordicform.se", "+46 8 123 4567", 10.0, 4.88, "Active"));
        }
    }

    private void seedTickets() {
        if (supportTicketRepository.count() == 0) {
            supportTicketRepository.save(new SupportTicket("#TK-9041", "Sophie Laurent", "sophie.laurent@client.com", "Address update for order #VX83291", "Requested delivery address confirmation for upcoming Paris dispatch.", "MEDIUM", "RESOLVED"));
            supportTicketRepository.save(new SupportTicket("#TK-9042", "Liam Smith", "liam.smith@sample.co.uk", "Wholesale catalog inquiry", "Requesting tier 2 trade pricing for London boutique interior studio.", "LOW", "OPEN"));
        }
    }

    private void seedSettings() {
        if (storeSettingRepository.count() == 0) {
            storeSettingRepository.save(new StoreSetting("store_name", "Ptskart Commerce & Veylix Studio", "GENERAL", "Primary storefront brand name"));
            storeSettingRepository.save(new StoreSetting("default_currency", "USD", "GENERAL", "Primary store operating currency"));
            storeSettingRepository.save(new StoreSetting("free_shipping_threshold", "50.0", "SHIPPING", "Minimum cart amount for complimentary courier shipping"));
            storeSettingRepository.save(new StoreSetting("standard_tax_rate", "8.25", "TAX", "Default state / VAT percentage"));
        }
    }
}
