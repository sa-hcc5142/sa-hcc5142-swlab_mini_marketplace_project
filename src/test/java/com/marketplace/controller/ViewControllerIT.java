package com.marketplace.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/test-cleanup.sql", "/sql/test-seed-roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class ViewControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardPage_Public_Success() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    void loginPage_Public_Success() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser(username = "buyer", roles = {"BUYER"})
    void cartPage_BuyerRole_Success() throws Exception {
        mockMvc.perform(get("/cart/view"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    @WithMockUser(username = "seller", roles = {"SELLER"})
    void sellerDashboard_SellerRole_Success() throws Exception {
        mockMvc.perform(get("/seller/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller-dashboard"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminDashboard_AdminRole_Success() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"));
    }
}
