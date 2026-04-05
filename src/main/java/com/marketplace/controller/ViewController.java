package com.marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/products/view")
    public String products() {
        return "products";
    }

    @GetMapping("/products/view/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "product-details";
    }

    @GetMapping("/cart/view")
    public String cart() {
        return "cart";
    }

    @GetMapping("/orders/view")
    public String orders() {
        return "orders";
    }

    @GetMapping("/seller/dashboard")
    public String sellerDashboard() {
        return "seller-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }
}
