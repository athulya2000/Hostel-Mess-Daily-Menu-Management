package com.hostel.mess.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.hostel.mess.dao.MenuDAO;
import com.hostel.mess.dao.MenuDAOImpl;
import com.hostel.mess.model.MealType;
import com.hostel.mess.model.MenuItem;
import com.hostel.mess.util.AppConstants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Built-in HTTP Server serving both:
 * 1. Client Side: Daily Menu List (at '/')
 * 2. Admin Side: Menu Management CRUD (at '/admin')
 */
public class MessWebServer {

    public static final int PORT = 8080;
    private static HttpServer server;
    private static boolean running = false;
    private static boolean adminLoggedIn = false;

    public static synchronized void startServer() {
        if (running) return;

        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new ClientMenuHandler());
            server.createContext("/admin", new AdminMenuHandler());
            server.createContext("/admin/add", new AdminAddHandler());
            server.createContext("/admin/update", new AdminUpdateHandler());
            server.createContext("/admin/delete", new AdminDeleteHandler());
            server.createContext("/admin/login", new AdminLoginHandler());
            server.createContext("/admin/logout", new AdminLogoutHandler());
            server.setExecutor(null);
            server.start();
            running = true;
            System.out.println("Hostel Mess Web Server running at: http://localhost:" + PORT + "/");
        } catch (IOException e) {
            System.err.println("Could not start Web Server on port " + PORT + ": " + e.getMessage());
        }
    }

    public static boolean isRunning() {
        return running;
    }

    // ==========================================
    // CLIENT SIDE HANDLER: Daily Menu Board
    // ==========================================
    static class ClientMenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!"/".equals(path)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String selectedDay = getQueryParam(query, "day");
            if (selectedDay == null || selectedDay.isEmpty()) {
                selectedDay = "Monday";
            }

            String html = buildClientMenuPage(selectedDay);
            sendHtmlResponse(exchange, html, 200);
        }
    }

    // ==========================================
    // ADMIN SIDE HANDLERS: Menu CRUD
    // ==========================================
    static class AdminMenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!adminLoggedIn) {
                sendHtmlResponse(exchange, buildAdminLoginPage(""), 200);
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            String editParam = getQueryParam(query, "edit");
            String msgParam = getQueryParam(query, "msg");
            String errParam = getQueryParam(query, "err");
            String pageParam = getQueryParam(query, "page");

            int page = 1;
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    page = Math.max(1, Integer.parseInt(pageParam.trim()));
                } catch (Exception ignored) {}
            }

            MenuItem editItem = null;
            if (editParam != null && !editParam.trim().isEmpty()) {
                try {
                    int editId = Integer.parseInt(editParam.trim());
                    MenuDAO dao = new MenuDAOImpl();
                    editItem = dao.getMenuItemById(editId);
                } catch (Exception ignored) {}
            }
            sendHtmlResponse(exchange, buildAdminDashboardPage(editItem, msgParam, errParam, page), 200);
        }
    }

    static class AdminLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                String user = params.getOrDefault("username", "");
                String pass = params.getOrDefault("password", "");

                if (AppConstants.DEFAULT_ADMIN_USER.equals(user) && AppConstants.DEFAULT_ADMIN_PASS.equals(pass)) {
                    adminLoggedIn = true;
                    redirect(exchange, "/admin");
                } else {
                    sendHtmlResponse(exchange, buildAdminLoginPage("Invalid username or password. Please try again."), 200);
                }
            } else {
                redirect(exchange, "/admin");
            }
        }
    }

    static class AdminLogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            adminLoggedIn = false;
            redirect(exchange, "/");
        }
    }

    static class AdminAddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) && adminLoggedIn) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);

                String day = params.getOrDefault("dayOfWeek", "Monday");
                String meal = params.getOrDefault("mealType", "BREAKFAST");
                String name = params.getOrDefault("itemName", "").trim();
                String cat = params.getOrDefault("category", "Vegetarian");
                String desc = params.getOrDefault("description", "").trim();
                double cost = 0.0;
                try {
                    cost = Double.parseDouble(params.getOrDefault("extraCost", "0.0"));
                } catch (NumberFormatException ignored) {}

                if (!name.isEmpty()) {
                    try {
                        MenuDAO dao = new MenuDAOImpl();
                        MenuItem item = new MenuItem(day, MealType.valueOf(meal), name, cat, desc, cost, true);
                        dao.addMenuItem(item);
                        redirect(exchange, "/admin?msg=" + URLEncoder.encode("Record added successfully!", StandardCharsets.UTF_8));
                        return;
                    } catch (Exception e) {
                        System.err.println("Web Admin error adding menu item: " + e.getMessage());
                        redirect(exchange, "/admin?err=" + URLEncoder.encode("Error adding item: " + e.getMessage(), StandardCharsets.UTF_8));
                        return;
                    }
                } else {
                    redirect(exchange, "/admin?err=" + URLEncoder.encode("Dish Name cannot be empty!", StandardCharsets.UTF_8));
                    return;
                }
            } else {
                redirect(exchange, "/admin");
            }
        }
    }

    static class AdminUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) && adminLoggedIn) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                try {
                    int id = Integer.parseInt(params.getOrDefault("id", "0"));
                    String day = params.getOrDefault("dayOfWeek", "Monday");
                    String meal = params.getOrDefault("mealType", "BREAKFAST");
                    String name = params.getOrDefault("itemName", "").trim();
                    String cat = params.getOrDefault("category", "Vegetarian");
                    String desc = params.getOrDefault("description", "").trim();
                    double cost = 0.0;
                    try {
                        cost = Double.parseDouble(params.getOrDefault("extraCost", "0.0"));
                    } catch (NumberFormatException ignored) {}

                    if (id > 0 && !name.isEmpty()) {
                        MenuDAO dao = new MenuDAOImpl();
                        MenuItem item = new MenuItem(day, MealType.valueOf(meal), name, cat, desc, cost, true);
                        item.setId(id);
                        dao.updateMenuItem(item);
                        redirect(exchange, "/admin?msg=" + URLEncoder.encode("Record updated successfully!", StandardCharsets.UTF_8));
                        return;
                    } else if (name.isEmpty()) {
                        redirect(exchange, "/admin?err=" + URLEncoder.encode("Dish Name cannot be empty!", StandardCharsets.UTF_8));
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Web Admin error updating menu item: " + e.getMessage());
                    redirect(exchange, "/admin?err=" + URLEncoder.encode("Error updating item: " + e.getMessage(), StandardCharsets.UTF_8));
                    return;
                }
                redirect(exchange, "/admin");
            } else {
                redirect(exchange, "/admin");
            }
        }
    }

    static class AdminDeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) && adminLoggedIn) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                try {
                    int id = Integer.parseInt(params.getOrDefault("id", "0"));
                    if (id > 0) {
                        MenuDAO dao = new MenuDAOImpl();
                        dao.deleteMenuItem(id);
                        redirect(exchange, "/admin?msg=" + URLEncoder.encode("Record deleted successfully!", StandardCharsets.UTF_8));
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Web Admin error deleting item: " + e.getMessage());
                    redirect(exchange, "/admin?err=" + URLEncoder.encode("Error deleting item: " + e.getMessage(), StandardCharsets.UTF_8));
                    return;
                }
                redirect(exchange, "/admin");
            } else {
                redirect(exchange, "/admin");
            }
        }
    }

    // ==========================================
    // HTML GENERATION METHODS
    // ==========================================

    private static String buildClientMenuPage(String currentDay) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang='en'>\n<head>\n");
        sb.append("<meta charset='UTF-8'>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        sb.append("<title>Hostel Mess - Daily Menu Board</title>\n");
        sb.append("<style>\n");
        sb.append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, sans-serif; }\n");
        sb.append("body { background: #f4f6fa; color: #333; line-height: 1.6; }\n");
        sb.append("header { background: #1c3879; color: white; padding: 20px 32px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }\n");
        sb.append("header h1 { font-size: 22px; font-weight: 700; }\n");
        sb.append("header p { font-size: 13px; opacity: 0.9; margin-top: 3px; }\n");
        sb.append(".nav-actions { display: flex; gap: 12px; align-items: center; }\n");
        sb.append(".btn-admin { background: #f58220; color: white; padding: 8px 18px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 13px; transition: background 0.2s; }\n");
        sb.append(".btn-admin:hover { background: #e07316; }\n");
        sb.append(".container { max-width: 1100px; margin: 26px auto; padding: 0 20px; }\n");
        sb.append(".day-nav { display: flex; gap: 8px; flex-wrap: wrap; background: white; padding: 14px 18px; border-radius: 8px; margin-bottom: 24px; box-shadow: 0 2px 6px rgba(0,0,0,0.05); align-items: center; }\n");
        sb.append(".day-nav span { font-weight: 700; margin-right: 10px; color: #1c3879; font-size: 14px; }\n");
        sb.append(".day-nav a { text-decoration: none; padding: 8px 16px; border-radius: 6px; font-weight: 600; font-size: 13px; color: #444; background: #eaedf3; transition: all 0.2s; }\n");
        sb.append(".day-nav a.active, .day-nav a:hover { background: #1c3879; color: white; }\n");
        sb.append(".meals-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }\n");
        sb.append(".meal-card { background: white; border-radius: 10px; padding: 18px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border-top: 4px solid #1c3879; }\n");
        sb.append(".meal-header { display: flex; justify-content: space-between; align-items: baseline; border-bottom: 1px solid #edf0f5; padding-bottom: 10px; margin-bottom: 12px; }\n");
        sb.append(".meal-title { font-size: 18px; font-weight: 700; color: #1c3879; }\n");
        sb.append(".meal-time { font-size: 12px; color: #777; }\n");
        sb.append(".dish-item { background: #fbfcfe; border: 1px solid #e7ecf3; border-radius: 6px; padding: 10px; margin-bottom: 10px; }\n");
        sb.append(".dish-name { font-weight: 600; font-size: 14px; color: #222; }\n");
        sb.append(".dish-desc { font-size: 12px; color: #666; margin-top: 3px; font-style: italic; }\n");
        sb.append(".dish-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; font-size: 11px; }\n");
        sb.append(".cat-veg { color: #28a745; font-weight: bold; }\n");
        sb.append(".cat-nonveg { color: #dc3545; font-weight: bold; }\n");
        sb.append(".cat-special { color: #d97706; font-weight: bold; }\n");
        sb.append(".cost-tag { background: #e0e7ff; color: #1c3879; padding: 2px 6px; border-radius: 4px; font-weight: bold; }\n");
        sb.append("footer { text-align: center; padding: 24px; font-size: 13px; color: #888; margin-top: 24px; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        sb.append("<header>\n<div>\n");
        sb.append("<h1>Hostel Mess Daily Menu Register</h1>\n");
        sb.append("<p>Resident Daily Meal Schedules & Details</p>\n");
        sb.append("</div>\n");
        sb.append("<div class='nav-actions'>\n");
        sb.append("<a href='/admin' class='btn-admin'>Mess Admin Panel</a>\n");
        sb.append("</div>\n</header>\n");

        sb.append("<div class='container'>\n");
        sb.append("<div class='day-nav'>\n<span>Select Day:</span>\n");
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String d : days) {
            String activeClass = d.equalsIgnoreCase(currentDay) ? "active" : "";
            sb.append("<a class='").append(activeClass).append("' href='/?day=").append(d).append("'>").append(d).append("</a>\n");
        }
        sb.append("</div>\n");

        sb.append("<div class='meals-grid'>\n");
        try {
            MenuDAO dao = new MenuDAOImpl();
            List<MenuItem> items = dao.getMenuItemsByDay(currentDay);

            for (MealType mt : MealType.values()) {
                sb.append("<div class='meal-card'>\n");
                sb.append("<div class='meal-header'>\n");
                sb.append("<div class='meal-title'>").append(mt.getDisplayName()).append("</div>\n");
                sb.append("<div class='meal-time'>").append(mt.getDefaultTiming()).append("</div>\n");
                sb.append("</div>\n");

                int count = 0;
                for (MenuItem item : items) {
                    if (item.getMealType() == mt) {
                        count++;
                        sb.append("<div class='dish-item'>\n");
                        sb.append("<div class='dish-name'>").append(escapeHtml(item.getItemName())).append("</div>\n");
                        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            sb.append("<div class='dish-desc'>").append(escapeHtml(item.getDescription())).append("</div>\n");
                        }
                        sb.append("<div class='dish-footer'>\n");
                        String catClass = "Non-Vegetarian".equalsIgnoreCase(item.getCategory()) ? "cat-nonveg" :
                                          "Special Feast".equalsIgnoreCase(item.getCategory()) ? "cat-special" : "cat-veg";
                        sb.append("<span class='").append(catClass).append("'>[").append(item.getCategory()).append("]</span>\n");
                        if (item.getExtraCost() > 0) {
                            sb.append("<span class='cost-tag'>+ Rs ").append(String.format("%.1f", item.getExtraCost())).append("</span>\n");
                        }
                        sb.append("</div>\n</div>\n");
                    }
                }
                if (count == 0) {
                    sb.append("<p style='font-size:13px; color:#888;'>No dishes scheduled for this meal.</p>\n");
                }
                sb.append("</div>\n");
            }
        } catch (Exception e) {
            sb.append("<p style='color:red;'>Error loading menu items: ").append(e.getMessage()).append("</p>\n");
        }
        sb.append("</div>\n</div>\n");
        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    private static String buildAdminLoginPage(String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang='en'>\n<head>\n");
        sb.append("<meta charset='UTF-8'>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        sb.append("<title>Admin Login - Hostel Mess Register</title>\n");
        sb.append("<style>\n");
        sb.append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, sans-serif; }\n");
        sb.append("body { background: #f4f6fa; display: flex; align-items: center; justify-content: center; min-height: 100vh; }\n");
        sb.append(".login-card { background: white; width: 380px; padding: 32px; border-radius: 10px; box-shadow: 0 4px 16px rgba(0,0,0,0.1); border-top: 5px solid #1c3879; }\n");
        sb.append("h2 { color: #1c3879; margin-bottom: 6px; font-size: 20px; }\n");
        sb.append("p { color: #666; font-size: 13px; margin-bottom: 20px; }\n");
        sb.append(".form-group { margin-bottom: 16px; }\n");
        sb.append("label { display: block; font-weight: 600; font-size: 13px; margin-bottom: 5px; color: #333; }\n");
        sb.append("input { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 6px; font-size: 14px; }\n");
        sb.append(".btn-login { width: 100%; background: #1c3879; color: white; border: none; padding: 11px; border-radius: 6px; font-weight: 700; cursor: pointer; font-size: 14px; }\n");
        sb.append(".btn-login:hover { background: #142a5c; }\n");
        sb.append(".back-link { display: block; text-align: center; margin-top: 16px; font-size: 13px; color: #1c3879; text-decoration: none; font-weight: 600; }\n");
        sb.append(".error { color: #dc3545; font-size: 12px; margin-bottom: 14px; font-weight: bold; }\n");
        sb.append(".hint { font-size: 11px; color: #888; margin-top: 8px; text-align: center; }\n");
        sb.append("</style>\n</head>\n<body>\n");
        sb.append("<div class='login-card'>\n");
        sb.append("<h2>Mess Admin Panel</h2>\n");
        sb.append("<p>Enter credentials to access menu management</p>\n");
        if (!errorMessage.isEmpty()) {
            sb.append("<div class='error'>").append(errorMessage).append("</div>\n");
        }
        sb.append("<form action='/admin/login' method='POST'>\n");
        sb.append("<div class='form-group'><label>Username</label><input type='text' name='username' placeholder='Username' required autofocus></div>\n");
        sb.append("<div class='form-group'><label>Password</label><input type='password' name='password' placeholder='Password' required></div>\n");
        sb.append("<button type='submit' class='btn-login'>Login to Admin Panel</button>\n");
        sb.append("</form>\n");
        sb.append("<a href='/' class='back-link'>&larr; Back to Client Menu View</a>\n");
        sb.append("</div>\n</body>\n</html>\n");
        return sb.toString();
    }

    private static String buildAdminDashboardPage(MenuItem editItem, String successMsg, String errorMsg, int page) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang='en'>\n<head>\n");
        sb.append("<meta charset='UTF-8'>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        sb.append("<title>Admin Control Panel - Daily Menu Register</title>\n");
        sb.append("<style>\n");
        sb.append("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, sans-serif; }\n");
        sb.append("body { background: #f4f6fa; color: #333; line-height: 1.6; }\n");
        sb.append("header { background: #1c3879; color: white; padding: 18px 30px; display: flex; justify-content: space-between; align-items: center; }\n");
        sb.append("header h1 { font-size: 20px; font-weight: 700; }\n");
        sb.append(".nav-links a { margin-left: 12px; color: white; text-decoration: none; font-size: 13px; font-weight: 600; padding: 6px 14px; border-radius: 4px; }\n");
        sb.append(".btn-view { background: #28a745; }\n");
        sb.append(".btn-logout { background: #dc3545; }\n");
        sb.append(".alert-banner { max-width: 1200px; margin: 20px auto -8px auto; padding: 0 20px; transition: opacity 0.4s ease; }\n");
        sb.append(".alert { padding: 12px 18px; border-radius: 6px; font-size: 14px; font-weight: 600; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 6px rgba(0,0,0,0.06); }\n");
        sb.append(".alert-success { background: #d1e7dd; color: #0f5132; border: 1px solid #badbcc; }\n");
        sb.append(".alert-danger { background: #f8d7da; color: #842029; border: 1px solid #f5c2c7; }\n");
        sb.append(".alert-close { background: transparent; border: none; font-size: 22px; font-weight: 700; line-height: 1; cursor: pointer; color: inherit; opacity: 0.65; padding: 0 4px; }\n");
        sb.append(".alert-close:hover { opacity: 1; }\n");
        sb.append(".container { max-width: 1200px; margin: 24px auto; padding: 0 20px; display: grid; grid-template-columns: 360px 1fr; gap: 24px; }\n");
        sb.append("@media (max-width: 900px) { .container { grid-template-columns: 1fr; } }\n");
        sb.append(".card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }\n");
        sb.append(".card h2 { font-size: 17px; color: #1c3879; margin-bottom: 14px; border-bottom: 2px solid #eaedf3; padding-bottom: 8px; }\n");
        sb.append(".form-group { margin-bottom: 12px; }\n");
        sb.append("label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 4px; }\n");
        sb.append("input, select, textarea { width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 6px; font-size: 13px; }\n");
        sb.append(".btn-submit { width: 100%; background: #1c3879; color: white; border: none; padding: 10px; border-radius: 6px; font-weight: bold; cursor: pointer; }\n");
        sb.append(".btn-submit:hover { background: #142a5c; }\n");
        sb.append(".btn-cancel { display: block; text-align: center; margin-top: 10px; color: #666; font-size: 13px; text-decoration: none; }\n");
        sb.append(".btn-cancel:hover { text-decoration: underline; color: #1c3879; }\n");
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 13px; }\n");
        sb.append("th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; vertical-align: middle; }\n");
        sb.append("th { background: #f8f9fa; color: #1c3879; font-weight: 700; }\n");
        sb.append(".btn-edit { background: #0d6efd; color: white; border: none; padding: 5px 12px; border-radius: 4px; font-size: 12px; text-decoration: none; display: inline-block; margin-right: 6px; font-weight: 600; }\n");
        sb.append(".btn-edit:hover { background: #0b5ed7; }\n");
        sb.append(".btn-del { background: #dc3545; color: white; border: none; padding: 5px 12px; border-radius: 4px; font-size: 12px; cursor: pointer; font-weight: 600; }\n");
        sb.append(".btn-del:hover { background: #b02a37; }\n");
        sb.append(".pagination-container { display: flex; justify-content: space-between; align-items: center; margin-top: 14px; padding-top: 12px; border-top: 1px solid #edf0f5; font-size: 13px; color: #555; }\n");
        sb.append(".pagination-links { display: flex; gap: 4px; align-items: center; }\n");
        sb.append(".page-btn { display: inline-block; padding: 5px 10px; border: 1px solid #d0d7de; border-radius: 4px; text-decoration: none; color: #1c3879; font-weight: 600; font-size: 12px; background: white; }\n");
        sb.append(".page-btn.active { background: #1c3879; color: white; border-color: #1c3879; }\n");
        sb.append(".page-btn:hover:not(.active):not(.disabled) { background: #f0f4f8; }\n");
        sb.append(".page-btn.disabled { color: #bbb; border-color: #eee; cursor: not-allowed; pointer-events: none; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        sb.append("<header>\n<div>\n");
        sb.append("<h1>Mess Admin Panel</h1>\n");
        sb.append("</div>\n<div class='nav-links'>\n");
        sb.append("<a href='/' class='btn-view'>View Client Menu</a>\n");
        sb.append("<a href='/admin/logout' class='btn-logout'>Logout</a>\n");
        sb.append("</div>\n</header>\n");

        // Success / Error Alerts with Close Icon and Auto-Dismiss
        if (successMsg != null && !successMsg.trim().isEmpty()) {
            sb.append("<div class='alert-banner' id='alertBanner'><div class='alert alert-success'>\n");
            sb.append("<span>&#10004; ").append(escapeHtml(successMsg)).append("</span>\n");
            sb.append("<button type='button' class='alert-close' onclick='closeAlert()' title='Close'>&times;</button>\n");
            sb.append("</div></div>\n");
        }
        if (errorMsg != null && !errorMsg.trim().isEmpty()) {
            sb.append("<div class='alert-banner' id='alertBanner'><div class='alert alert-danger'>\n");
            sb.append("<span>&#9888; ").append(escapeHtml(errorMsg)).append("</span>\n");
            sb.append("<button type='button' class='alert-close' onclick='closeAlert()' title='Close'>&times;</button>\n");
            sb.append("</div></div>\n");
        }

        sb.append("<div class='container'>\n");

        // Form (Create or Update)
        boolean isEdit = (editItem != null);
        String formAction = isEdit ? "/admin/update" : "/admin/add";
        String formTitle = isEdit ? "Edit Menu Item #" + editItem.getId() : "+ Add Menu Item";
        String submitBtnText = isEdit ? "Update Menu Item" : "Add Item to Menu";

        sb.append("<div class='card'>\n");
        sb.append("<h2>").append(formTitle).append("</h2>\n");
        sb.append("<form action='").append(formAction).append("' method='POST'>\n");
        if (isEdit) {
            sb.append("<input type='hidden' name='id' value='").append(editItem.getId()).append("'>\n");
        }

        String curDay = isEdit ? editItem.getDayOfWeek() : "Monday";
        sb.append("<div class='form-group'><label>Day of Week</label><select name='dayOfWeek'>\n");
        for (String d : AppConstants.DAYS_OF_WEEK) {
            sb.append("<option").append(d.equalsIgnoreCase(curDay) ? " selected" : "").append(">").append(d).append("</option>\n");
        }
        sb.append("</select></div>\n");

        MealType curMeal = isEdit ? editItem.getMealType() : MealType.BREAKFAST;
        sb.append("<div class='form-group'><label>Meal Type</label><select name='mealType'>\n");
        for (MealType mt : MealType.values()) {
            sb.append("<option value='").append(mt.name()).append("'").append(mt == curMeal ? " selected" : "").append(">").append(mt.getDisplayName()).append("</option>\n");
        }
        sb.append("</select></div>\n");

        String curName = isEdit ? editItem.getItemName() : "";
        sb.append("<div class='form-group'><label>Dish / Item Name</label><input type='text' name='itemName' required value='").append(escapeHtml(curName)).append("' placeholder='e.g., Ghee Roast Dosa'></div>\n");

        String curCat = isEdit ? editItem.getCategory() : "Vegetarian";
        sb.append("<div class='form-group'><label>Category</label><select name='category'>\n");
        for (String c : AppConstants.CATEGORIES) {
            sb.append("<option").append(c.equalsIgnoreCase(curCat) ? " selected" : "").append(">").append(c).append("</option>\n");
        }
        sb.append("</select></div>\n");

        double curCost = isEdit ? editItem.getExtraCost() : 0.0;
        sb.append("<div class='form-group'><label>Extra Cost (Rs)</label><input type='number' step='0.5' name='extraCost' value='").append(curCost).append("'></div>\n");

        String curDesc = isEdit ? (editItem.getDescription() != null ? editItem.getDescription() : "") : "";
        sb.append("<div class='form-group'><label>Side Dishes / Details</label><textarea name='description' rows='2' placeholder='Served with sambar & chutney'>").append(escapeHtml(curDesc)).append("</textarea></div>\n");

        sb.append("<button type='submit' class='btn-submit'>").append(submitBtnText).append("</button>\n");
        if (isEdit) {
            sb.append("<a href='/admin?page=").append(page).append("' class='btn-cancel'>Cancel Edit</a>\n");
        }
        sb.append("</form>\n</div>\n");

        // Table with Pagination (Read / List & Edit & Delete)
        sb.append("<div class='card'>\n");
        sb.append("<h2>Daily Menu Items</h2>\n");
        sb.append("<div style='overflow-x:auto;'>\n");
        sb.append("<table>\n<thead>\n<tr>\n<th>ID</th><th>Day</th><th>Meal</th><th>Dish Name</th><th>Category</th><th>Extra Cost</th><th style='width:140px;'>Action</th>\n</tr>\n</thead>\n<tbody>\n");

        try {
            MenuDAO dao = new MenuDAOImpl();
            List<MenuItem> all = dao.getAllMenuItems();
            int totalItems = all.size();
            int pageSize = 8;
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
            if (page > totalPages) page = totalPages;
            if (page < 1) page = 1;

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            List<MenuItem> pageItems = (start < totalItems) ? all.subList(start, end) : Collections.emptyList();

            for (MenuItem item : pageItems) {
                sb.append("<tr>\n");
                sb.append("<td>").append(item.getId()).append("</td>\n");
                sb.append("<td>").append(item.getDayOfWeek()).append("</td>\n");
                sb.append("<td>").append(item.getMealType().getDisplayName()).append("</td>\n");
                sb.append("<td><strong>").append(escapeHtml(item.getItemName())).append("</strong></td>\n");
                sb.append("<td>").append(item.getCategory()).append("</td>\n");
                sb.append("<td>Rs ").append(String.format("%.2f", item.getExtraCost())).append("</td>\n");
                sb.append("<td>\n");
                sb.append("<a href='/admin?edit=").append(item.getId()).append("&page=").append(page).append("' class='btn-edit'>Edit</a>\n");
                sb.append("<form action='/admin/delete' method='POST' style='display:inline;' onsubmit=\"return confirm('Delete this menu item?');\">\n");
                sb.append("<input type='hidden' name='id' value='").append(item.getId()).append("'>\n");
                sb.append("<button type='submit' class='btn-del'>Delete</button>\n");
                sb.append("</form>\n</td>\n");
                sb.append("</tr>\n");
            }
            sb.append("</tbody>\n</table>\n</div>\n");

            // Pagination Controls
            sb.append("<div class='pagination-container'>\n");
            sb.append("<div>Showing ").append(totalItems == 0 ? 0 : (start + 1)).append(" to ").append(end).append(" of ").append(totalItems).append(" records</div>\n");
            sb.append("<div class='pagination-links'>\n");

            if (page > 1) {
                sb.append("<a href='/admin?page=1' class='page-btn'>&laquo; First</a>\n");
                sb.append("<a href='/admin?page=").append(page - 1).append("' class='page-btn'>&lsaquo; Prev</a>\n");
            } else {
                sb.append("<span class='page-btn disabled'>&laquo; First</span>\n");
                sb.append("<span class='page-btn disabled'>&lsaquo; Prev</span>\n");
            }

            for (int p = 1; p <= totalPages; p++) {
                if (p == page) {
                    sb.append("<span class='page-btn active'>").append(p).append("</span>\n");
                } else if (p <= 3 || p > totalPages - 2 || Math.abs(p - page) <= 1) {
                    sb.append("<a href='/admin?page=").append(p).append("' class='page-btn'>").append(p).append("</a>\n");
                } else if (p == 4 || p == totalPages - 2) {
                    sb.append("<span style='padding: 0 4px;'>...</span>\n");
                }
            }

            if (page < totalPages) {
                sb.append("<a href='/admin?page=").append(page + 1).append("' class='page-btn'>Next &rsaquo;</a>\n");
                sb.append("<a href='/admin?page=").append(totalPages).append("' class='page-btn'>Last &raquo;</a>\n");
            } else {
                sb.append("<span class='page-btn disabled'>Next &rsaquo;</span>\n");
                sb.append("<span class='page-btn disabled'>Last &raquo;</span>\n");
            }

            sb.append("</div>\n</div>\n");
        } catch (Exception e) {
            sb.append("<tr><td colspan='7' style='color:red;'>Error: ").append(e.getMessage()).append("</td></tr>\n");
            sb.append("</tbody>\n</table>\n</div>\n");
        }

        sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("<script>\n");
        sb.append("function closeAlert() {\n");
        sb.append("    var b = document.getElementById('alertBanner');\n");
        sb.append("    if (b) {\n");
        sb.append("        b.style.opacity = '0';\n");
        sb.append("        setTimeout(function() { b.style.display = 'none'; }, 400);\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("setTimeout(closeAlert, 3500);\n");
        sb.append("if (window.history.replaceState && (window.location.search.includes('msg=') || window.location.search.includes('err='))) {\n");
        sb.append("    var cleanUrl = window.location.pathname + (window.location.search.replace(/[\\?&](msg|err)=[^&]*/g, '').replace(/^&/, '?'));\n");
        sb.append("    window.history.replaceState(null, '', cleanUrl || window.location.pathname);\n");
        sb.append("}\n");
        sb.append("</script>\n");
        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    private static void sendHtmlResponse(HttpExchange exchange, String html, int status) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }

    private static String getQueryParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(key)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                map.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
