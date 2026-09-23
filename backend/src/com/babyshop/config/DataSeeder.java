package com.babyshop.config;

import com.babyshop.dao.CartDao;
import com.babyshop.dao.CategoryDao;
import com.babyshop.dao.ProductDao;
import com.babyshop.dao.UserDao;
import com.babyshop.model.Category;
import com.babyshop.model.Product;
import com.babyshop.model.Role;
import com.babyshop.model.User;
import com.babyshop.util.PasswordUtil;

import java.math.BigDecimal;

/**
 * Runs once on application startup. Creates a default admin account and a
 * starter catalog of categories/products if the database is empty, so the
 * storefront isn't blank on first run. This writes real rows into MySQL
 * through the DAOs, exactly like any other write in the app - no mock data.
 */
public class DataSeeder {

    private final UserDao userDao = new UserDao();
    private final CartDao cartDao = new CartDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ProductDao productDao = new ProductDao();

    public void run() {
        seedAdmin();
        seedCatalog();
    }

    private void seedAdmin() {
        if (userDao.existsByEmail("admin@babyshop.com")) return;

        User admin = new User();
        admin.setName("Store Administrator");
        admin.setEmail("admin@babyshop.com");
        admin.setPhone("9999999999");
        admin.setPassword(PasswordUtil.hash("Admin@123"));
        admin.setRole(Role.ROLE_ADMIN);
        admin.setEnabled(true);
        User saved = userDao.save(admin);
        cartDao.findOrCreateByUserId(saved.getId());
        System.out.println(">>> Seeded default admin user: admin@babyshop.com / Admin@123");
    }

    private void seedCatalog() {
        if (!categoryDao.findAll().isEmpty()) return;

        Category diapers = categoryDao.save(cat("Diapers & Wipes", "Diapers, wipes and changing essentials"));
        Category feeding = categoryDao.save(cat("Feeding", "Bottles, formula, high chairs and feeding accessories"));
        Category clothing = categoryDao.save(cat("Clothing", "Baby onesies, rompers and outfits"));
        Category toys = categoryDao.save(cat("Toys", "Safe and educational toys for babies and toddlers"));
        Category bath = categoryDao.save(cat("Bath & Skincare", "Baby-safe bath and skincare products"));
        Category gear = categoryDao.save(cat("Gear & Travel", "Strollers, car seats and baby carriers"));

        addProduct("Ultra-Soft Diapers (Size 2, 84 count)", "Super absorbent overnight diapers with wetness indicator.",
                "899.00", 150, "https://images.unsplash.com/photo-1519689680058-324335c77eba?w=500", "SoftCare", diapers.getId());
        addProduct("Baby Wipes Fragrance Free (3-pack)", "99% water wipes, gentle on sensitive skin.",
                "299.00", 200, "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=500", "PureTouch", diapers.getId());
        addProduct("Anti-Colic Baby Bottle 250ml", "BPA-free bottle with anti-colic venting system.",
                "449.00", 100, "https://images.unsplash.com/photo-1595231712607-976fda2b0a41?w=500", "FeedWell", feeding.getId());
        addProduct("Wooden High Chair", "Adjustable 3-in-1 wooden high chair for toddlers.",
                "6999.00", 25, "https://images.unsplash.com/photo-1618160702438-9b02ab6515c9?w=500", "TinyTable", feeding.getId());
        addProduct("Organic Cotton Onesie Set (5-pack)", "Soft breathable onesies for newborns 0-3 months.",
                "1199.00", 80, "https://images.unsplash.com/photo-1522771930-78848d9293e8?w=500", "LittleThreads", clothing.getId());
        addProduct("Baby Winter Romper", "Warm fleece-lined romper for cold weather.",
                "899.00", 60, "https://images.unsplash.com/photo-1519457851200-6c2e8dfd4c5b?w=500", "CozyKid", clothing.getId());
        addProduct("Soft Plush Elephant Toy", "Machine-washable plush toy, safe for all ages.",
                "599.00", 120, "https://images.unsplash.com/photo-1584399937011-30690f512cae?w=500", "HuggyFriends", toys.getId());
        addProduct("Wooden Stacking Rings", "Educational stacking toy to build motor skills.",
                "799.00", 90, "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=500", "PlaySmart", toys.getId());
        addProduct("Tear-Free Baby Shampoo 200ml", "Gentle tear-free formula for daily use.",
                "349.00", 150, "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=500", "PureTouch", bath.getId());
        addProduct("Baby Bath Tub with Support", "Ergonomic bath tub with non-slip support.",
                "1499.00", 45, "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=500", "SplashTime", bath.getId());
        addProduct("Lightweight Travel Stroller", "Compact fold, all-terrain wheels, 6M+.",
                "8999.00", 20, "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=500", "RideAlong", gear.getId());
        addProduct("Ergonomic Baby Carrier", "Adjustable, breathable, newborn to toddler.",
                "3499.00", 35, "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?w=500", "CarryMe", gear.getId());

        System.out.println(">>> Seeded categories and sample products");
    }

    private Category cat(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return c;
    }

    private void addProduct(String name, String desc, String price, int stock, String img, String brand, Long categoryId) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setStockQuantity(stock);
        p.setImageUrl(img);
        p.setBrand(brand);
        p.setCategoryId(categoryId);
        p.setActive(true);
        productDao.save(p);
    }
}
