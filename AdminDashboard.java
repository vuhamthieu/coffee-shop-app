import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdminDashboard extends Application {

    // Base URL for Admin API
    private static final String BASE_URL = "http://localhost:8080/backend/api/admin/";
    private static final String BASE_EMPLOYEE_URL = "http://localhost:8080/backend/api/employee/";

    // API Endpoints
    // Categories
    private static final String GET_CATEGORIES_URL = BASE_EMPLOYEE_URL + "get-categories.php";
    private static final String ADD_CATEGORY_URL = BASE_URL + "add_category.php";
    private static final String UPDATE_CATEGORY_URL = BASE_URL + "update_category.php";
    private static final String DELETE_CATEGORY_URL = BASE_URL + "delete_category.php";

    // Products
    private static final String GET_PRODUCTS_URL = BASE_EMPLOYEE_URL + "get-products.php";
    private static final String ADD_PRODUCT_URL = BASE_URL + "add_product.php";
    private static final String UPDATE_PRODUCT_URL = BASE_URL + "update_product.php";
    private static final String DELETE_PRODUCT_URL = BASE_URL + "delete_product.php";
    private static final String TOGGLE_PRODUCT_AVAILABLE_URL = BASE_URL + "toggle_product_available.php";
    private static final String UPDATE_PRICE_URL = BASE_URL + "update_price.php";
    private static final String UPDATE_HOT_PRODUCT_URL = BASE_URL + "update-hot-product.php";

    // Employees
    private static final String ADD_EMPLOYEE_URL = BASE_URL + "add_employee.php";
    private static final String UPDATE_EMPLOYEE_URL = BASE_URL + "update_employee.php";
    private static final String DELETE_EMPLOYEE_URL = BASE_URL + "delete_employee.php";
    private static final String UPDATE_ROLE_URL = BASE_URL + "employees/update-role.php";
    private static final String LOCK_ACCOUNT_URL = BASE_URL + "employees/lock-account.php";
    private static final String UNLOCK_ACCOUNT_URL = BASE_URL + "employees/unlock-account.php";
    private static final String GET_WORKING_HOURS_URL = BASE_URL + "employees/get-working-hours.php";

    // Inventory
    private static final String GET_INVENTORY_LIST_URL = BASE_URL + "inventory/get-list.php";
    private static final String IMPORT_INVENTORY_URL = BASE_URL + "inventory/import.php";
    private static final String EXPORT_INVENTORY_URL = BASE_URL + "inventory/export.php";
    private static final String GET_LOW_STOCK_ALERT_URL = BASE_URL + "inventory/get-low-stock-alert.php";

    // Coupons
    private static final String GET_COUPONS_URL = BASE_URL + "coupons/get-list.php";
    private static final String CREATE_COUPON_URL = BASE_URL + "coupons/create.php";
    private static final String UPDATE_COUPON_URL = BASE_URL + "coupons/update.php";
    private static final String DELETE_COUPON_URL = BASE_URL + "coupons/delete.php";
    private static final String GET_COUPON_USAGE_URL = BASE_URL + "coupons/get-usage.php";

    // Reports
    private static final String REVENUE_BY_DAY_URL = BASE_URL + "reports/revenue-by-day.php";
    private static final String REVENUE_BY_WEEK_URL = BASE_URL + "reports/revenue-by-week.php";
    private static final String REVENUE_BY_MONTH_URL = BASE_URL + "reports/revenue-by-month.php";
    private static final String REVENUE_BY_SHIFT_URL = BASE_URL + "reports/revenue-by-shift.php";
    private static final String BEST_SELLING_PRODUCTS_URL = BASE_URL + "reports/best-selling-products.php";
    private static final String CUSTOMER_COUNT_URL = BASE_URL + "reports/customer-count.php";
    private static final String EXPENSES_URL = BASE_URL + "reports/expenses.php";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    // Dashboard Tab Data
    private final ObservableList<PaymentRecord> allPayments = FXCollections.observableArrayList();
    private final FilteredList<PaymentRecord> filteredPayments = new FilteredList<>(allPayments);
    private final SortedList<PaymentRecord> sortedPayments = new SortedList<>(filteredPayments);

    // UI Components - Dashboard
    private Label totalRevenueLabel;
    private Label todayRevenueLabel;
    private Label weeklyRevenueLabel;
    private Label monthlyRevenueLabel;
    private Label cashLabel;
    private Label cardLabel;
    private Label qrLabel;
    private Label transferLabel;
    private TableView<PaymentRecord> dashboardTable;
    private TextField searchField;
    private ComboBox<String> filterMethodCombo;
    private ComboBox<String> dateRangeCombo;
    private Label reportDisplayLabel;

    // Menu Tab Data
    private ObservableList<CategoryModel> categories = FXCollections.observableArrayList();
    private ObservableList<ProductModel> products = FXCollections.observableArrayList();
    private TableView<CategoryModel> categoryTable;
    private TableView<ProductModel> productTable;

    // Employee Tab Data
    private ObservableList<EmployeeModel> employees = FXCollections.observableArrayList();
    private TableView<EmployeeModel> employeeTable;

    // Inventory Tab Data
    private ObservableList<InventoryModel> inventoryItems = FXCollections.observableArrayList();
    private TableView<InventoryModel> inventoryTable;

    // Coupon Tab Data
    private ObservableList<CouponModel> coupons = FXCollections.observableArrayList();
    private TableView<CouponModel> couponTable;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setStyle("-fx-background-color:#F5F0E1;-fx-font-family:'Segoe UI',sans-serif;");

        // Header
        VBox header = buildHeader();
        root.setTop(header);

        // TabPane với các tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color:#F5F0E1;");

        // Tab 1: Dashboard (Doanh thu)
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setContent(buildDashboardTab());

        // Tab 2: Menu (Quản lý Menu)
        Tab menuTab = new Tab("Menu");
        menuTab.setContent(buildMenuTab());

        // Tab 3: Nhân viên
        Tab employeeTab = new Tab("Nhân viên");
        employeeTab.setContent(buildEmployeeTab());

        // Tab 4: Kho
        Tab inventoryTab = new Tab("Kho");
        inventoryTab.setContent(buildInventoryTab());

        // Tab 5: Khuyến mãi
        Tab couponTab = new Tab("Khuyến mãi");
        couponTab.setContent(buildCouponTab());

        // Tab 6: Báo cáo
        Tab reportTab = new Tab("Báo cáo");
        reportTab.setContent(buildReportTab());

        tabPane.getTabs().addAll(dashboardTab, menuTab, employeeTab, inventoryTab, couponTab, reportTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1400, 850);
        primaryStage.setTitle("Coffee Aura - Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load initial data
        loadDashboardData();
        loadCategories();
        loadProducts();
        loadInventory();
        loadCoupons();
    }

    private VBox buildHeader() {
        Label title = new Label("☕ Coffee Aura - Quản trị hệ thống");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:28px;-fx-font-weight:bold;");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().add(title);
        headerBox.setPadding(new Insets(10, 0, 15, 10));

        return new VBox(headerBox);
    }

    // ==================== DASHBOARD TAB ====================
    private VBox buildDashboardTab() {
        VBox dashboard = new VBox(15);
        dashboard.setPadding(new Insets(15, 20, 20, 20));

        // Statistics Cards
        FlowPane statsCards = buildStatsCards();

        // Controls Panel
        HBox controlsPanel = buildControlsPanel();

        // Table Panel
        VBox tablePanel = buildTablePanel();

        dashboard.getChildren().addAll(statsCards, controlsPanel, tablePanel);
        VBox.setVgrow(tablePanel, Priority.ALWAYS);

        return dashboard;
    }

    private FlowPane buildStatsCards() {
        FlowPane cards = new FlowPane();
        cards.setHgap(15);
        cards.setVgap(15);
        cards.setPadding(new Insets(0, 0, 20, 0));

        // Total Revenue Card
        VBox totalCard = createStatCard("Tổng doanh thu", "0", "#6B4C3B");
        totalRevenueLabel = (Label) totalCard.getChildren().get(1);

        // Today Revenue Card
        VBox todayCard = createStatCard("Hôm nay", "0", "#C08A64");
        todayRevenueLabel = (Label) todayCard.getChildren().get(1);

        // Weekly Revenue Card
        VBox weeklyCard = createStatCard("Tuần này", "0", "#F2C57C");
        weeklyRevenueLabel = (Label) weeklyCard.getChildren().get(1);

        // Monthly Revenue Card
        VBox monthlyCard = createStatCard("Tháng này", "0", "#6B4C3B");
        monthlyRevenueLabel = (Label) monthlyCard.getChildren().get(1);

        // Payment Method Cards
        VBox cashCard = createStatCard("Tiền mặt", "0", "#6B4C3B");
        cashLabel = (Label) cashCard.getChildren().get(1);

        VBox cardCard = createStatCard("💳 Thẻ", "0", "#C08A64");
        cardLabel = (Label) cardCard.getChildren().get(1);

        VBox qrCard = createStatCard("📱 QR", "0", "#F2C57C");
        qrLabel = (Label) qrCard.getChildren().get(1);

        VBox transferCard = createStatCard("🏦 Chuyển khoản", "0", "#6B4C3B");
        transferLabel = (Label) transferCard.getChildren().get(1);

        cards.getChildren().addAll(totalCard, todayCard, weeklyCard, monthlyCard,
                cashCard, cardCard, qrCard, transferCard);
        return cards;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:14px;-fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:" + color + ";-fx-font-size:20px;-fx-font-weight:bold;");
        valueLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private HBox buildControlsPanel() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 10, 0));

        // Search Field
        Label searchLabel = new Label("Tìm kiếm:");
        searchLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        searchField = new TextField();
        searchField.setPromptText("Tìm theo bàn, phương thức...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius:12;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-padding:10;-fx-text-fill:#6B4C3B;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Filter by Payment Method
        Label methodLabel = new Label("Phương thức:");
        methodLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        filterMethodCombo = new ComboBox<>();
        filterMethodCombo.getItems().addAll("Tất cả", "Tiền mặt", "Thẻ", "QR", "Chuyển khoản");
        filterMethodCombo.setValue("Tất cả");
        filterMethodCombo.setPrefWidth(150);
        filterMethodCombo.setStyle("-fx-background-radius:12;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-padding:10;-fx-text-fill:#6B4C3B;");
        filterMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Date Range Filter
        Label dateLabel = new Label("Khoảng thời gian:");
        dateLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        dateRangeCombo = new ComboBox<>();
        dateRangeCombo.getItems().addAll("Tất cả", "Hôm nay", "Tuần này", "Tháng này", "7 ngày qua", "30 ngày qua");
        dateRangeCombo.setValue("Tất cả");
        dateRangeCombo.setPrefWidth(150);
        dateRangeCombo.setStyle("-fx-background-radius:12;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-padding:10;-fx-text-fill:#6B4C3B;");
        dateRangeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
            updateStatistics();
        });

        // Buttons
        Button refreshBtn = createPrimaryButton("🔄 Làm mới");
        refreshBtn.setOnAction(e -> {
            loadDashboardData();
            updateStatistics();
        });

        Button exportBtn = createGhostButton("📊 Xuất dữ liệu");
        exportBtn.setOnAction(e -> exportData());

        HBox buttonBox = new HBox(10, refreshBtn, exportBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        controls.getChildren().addAll(searchLabel, searchField, methodLabel, filterMethodCombo,
                dateLabel, dateRangeCombo, buttonBox);

        return controls;
    }

    private VBox buildTablePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");

        Label tableTitle = new Label("Lịch sử thanh toán");
        tableTitle.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:18px;-fx-font-weight:bold;");

        dashboardTable = new TableView<>(sortedPayments);
        dashboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dashboardTable.setPlaceholder(new Label("Chưa có dữ liệu thanh toán"));
        dashboardTable.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;"
                + "-fx-border-color:rgba(107,76,59,0.1);-fx-border-width:1;");

        // Configure sorted list
        sortedPayments.comparatorProperty().bind(dashboardTable.comparatorProperty());

        // Date Column
        TableColumn<PaymentRecord, String> colDate = new TableColumn<>("Thời gian");
        colDate.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDate.setPrefWidth(180);
        colDate.setStyle("-fx-alignment:CENTER_LEFT;");

        // Table Column
        TableColumn<PaymentRecord, String> colTable = new TableColumn<>("Bàn");
        colTable.setCellValueFactory(new PropertyValueFactory<>("table"));
        colTable.setPrefWidth(120);
        colTable.setStyle("-fx-alignment:CENTER;");

        // Payment Method Column
        TableColumn<PaymentRecord, String> colMethod = new TableColumn<>("Phương thức");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colMethod.setPrefWidth(140);
        colMethod.setStyle("-fx-alignment:CENTER;");

        // Amount Column with custom cell factory for currency formatting
        TableColumn<PaymentRecord, Double> colAmount = new TableColumn<>("Số tiền");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(160);
        colAmount.setCellFactory(column -> new javafx.scene.control.TableCell<PaymentRecord, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currency.format(amount));
                    setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-alignment:CENTER_RIGHT;");
                }
            }
        });

        dashboardTable.getColumns().addAll(colDate, colTable, colMethod, colAmount);

        // Summary row
        Label summaryLabel = new Label();
        summaryLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:14px;-fx-font-weight:bold;");
        updateSummaryLabel(summaryLabel);

        // Update summary when filtered list changes
        filteredPayments.addListener((javafx.collections.ListChangeListener.Change<? extends PaymentRecord> c) -> {
            updateSummaryLabel(summaryLabel);
        });

        panel.getChildren().addAll(tableTitle, dashboardTable, summaryLabel);
        VBox.setVgrow(dashboardTable, Priority.ALWAYS);

        return panel;
    }

    private void updateSummaryLabel(Label label) {
        double filteredTotal = filteredPayments.stream()
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        int count = filteredPayments.size();
        label.setText(String.format("Tổng: %s • %d giao dịch", currency.format(filteredTotal), count));
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String methodFilter = filterMethodCombo.getValue();
        String dateFilter = dateRangeCombo.getValue();

        filteredPayments.setPredicate(payment -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    payment.getTable().toLowerCase().contains(searchText) ||
                    payment.getMethod().toLowerCase().contains(searchText);

            // Method filter
            boolean matchesMethod = methodFilter == null || methodFilter.equals("Tất cả") ||
                    payment.getMethod().equals(methodFilter);

            // Date filter
            boolean matchesDate = true;
            if (dateFilter != null && !dateFilter.equals("Tất cả")) {
                LocalDate paymentDate = LocalDate.parse(payment.getTime().substring(0, 10),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate today = LocalDate.now();

                switch (dateFilter) {
                    case "Hôm nay":
                        matchesDate = paymentDate.equals(today);
                        break;
                    case "Tuần này":
                        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                        matchesDate = !paymentDate.isBefore(weekStart);
                        break;
                    case "Tháng này":
                        matchesDate = paymentDate.getMonth() == today.getMonth() &&
                                paymentDate.getYear() == today.getYear();
                        break;
                    case "7 ngày qua":
                        matchesDate = !paymentDate.isBefore(today.minusDays(7));
                        break;
                    case "30 ngày qua":
                        matchesDate = !paymentDate.isBefore(today.minusDays(30));
                        break;
                }
            }

            return matchesSearch && matchesMethod && matchesDate;
        });
    }

    private void updateStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = today.withDayOfMonth(1);

        double total = allPayments.stream().mapToDouble(PaymentRecord::getAmount).sum();
        double todayRevenue = allPayments.stream()
                .filter(p -> {
                    LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return pDate.equals(today);
                })
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        double weeklyRevenue = allPayments.stream()
                .filter(p -> {
                    LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return !pDate.isBefore(weekStart);
                })
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        double monthlyRevenue = allPayments.stream()
                .filter(p -> {
                    LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return !pDate.isBefore(monthStart);
                })
                .mapToDouble(PaymentRecord::getAmount)
                .sum();

        totalRevenueLabel.setText(currency.format(total));
        todayRevenueLabel.setText(currency.format(todayRevenue));
        weeklyRevenueLabel.setText(currency.format(weeklyRevenue));
        monthlyRevenueLabel.setText(currency.format(monthlyRevenue));

        // Payment method breakdown
        double cashRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("Tiền mặt"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long cashCount = allPayments.stream().filter(p -> p.getMethod().equals("Tiền mặt")).count();

        double cardRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("Thẻ"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long cardCount = allPayments.stream().filter(p -> p.getMethod().equals("Thẻ")).count();

        double qrRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("QR"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long qrCount = allPayments.stream().filter(p -> p.getMethod().equals("QR")).count();

        double transferRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("Chuyển khoản"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long transferCount = allPayments.stream().filter(p -> p.getMethod().equals("Chuyển khoản")).count();

        // Update individual payment method cards
        cashLabel.setText(currency.format(cashRevenue) + "\n(" + cashCount + " giao dịch)");
        cardLabel.setText(currency.format(cardRevenue) + "\n(" + cardCount + " giao dịch)");
        qrLabel.setText(currency.format(qrRevenue) + "\n(" + qrCount + " giao dịch)");
        transferLabel.setText(currency.format(transferRevenue) + "\n(" + transferCount + " giao dịch)");
    }

    private void loadDashboardData() {
        allPayments.clear();
        updateStatistics();
    }

    private void exportData() {
        // Simple export functionality
        StringBuilder sb = new StringBuilder();
        sb.append("Thời gian,Bàn,Phương thức,Số tiền\n");
        for (PaymentRecord p : filteredPayments) {
            sb.append(String.format("%s,%s,%s,%.0f\n",
                    p.getTime(), p.getTable(), p.getMethod(), p.getAmount()));
        }
        System.out.println("=== EXPORT DATA ===");
        System.out.println(sb.toString());
        System.out.println("===================");
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:#F2C57C;-fx-background-radius:14;"
                + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;"
                + "-fx-cursor:hand;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color:#E8B86A;-fx-background-radius:14;"
                        + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;"
                        + "-fx-cursor:hand;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color:#F2C57C;-fx-background-radius:14;"
                        + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;"
                        + "-fx-cursor:hand;"));
        return button;
    }

    private Button createGhostButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:transparent;-fx-border-color:rgba(107,76,59,0.4);"
                + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;"
                + "-fx-cursor:hand;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color:rgba(107,76,59,0.1);-fx-border-color:rgba(107,76,59,0.6);"
                        + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;"
                        + "-fx-cursor:hand;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color:transparent;-fx-border-color:rgba(107,76,59,0.4);"
                        + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;"
                        + "-fx-cursor:hand;"));
        return button;
    }

    // ==================== MENU TAB ====================
    private VBox buildMenuTab() {
        VBox menuTab = new VBox(15);
        menuTab.setPadding(new Insets(15));

        Label title = new Label("Quản lý Menu");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        // Split pane: Categories bên trái, Products bên phải
        javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
        splitPane.setDividerPositions(0.4);

        // Categories Panel
        VBox categoryPanel = buildCategoryPanel();
        // Products Panel
        VBox productPanel = buildProductPanel();

        splitPane.getItems().addAll(categoryPanel, productPanel);

        menuTab.getChildren().addAll(title, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        return menuTab;
    }

    private VBox buildCategoryPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;");

        Label title = new Label("Danh mục");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:16px;-fx-font-weight:bold;");

        categoryTable = new TableView<>(categories);
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<CategoryModel, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<CategoryModel, String> nameCol = new TableColumn<>("Tên danh mục");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        categoryTable.getColumns().addAll(idCol, nameCol);

        HBox btnBox = new HBox(10);
        Button addCatBtn = createPrimaryButton("➕ Thêm");
        Button editCatBtn = createGhostButton("✏️ Sửa");
        Button delCatBtn = createGhostButton("🗑️ Xóa");

        addCatBtn.setOnAction(e -> showAddCategoryDialog());
        editCatBtn.setOnAction(e -> showEditCategoryDialog());
        delCatBtn.setOnAction(e -> deleteCategory());

        btnBox.getChildren().addAll(addCatBtn, editCatBtn, delCatBtn);

        panel.getChildren().addAll(title, categoryTable, btnBox);
        VBox.setVgrow(categoryTable, Priority.ALWAYS);

        return panel;
    }

    private VBox buildProductPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;");

        Label title = new Label("Sản phẩm");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:16px;-fx-font-weight:bold;");

        productTable = new TableView<>(products);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ProductModel, Integer> pIdCol = new TableColumn<>("ID");
        pIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<ProductModel, String> pNameCol = new TableColumn<>("Tên món");
        pNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ProductModel, String> pCatCol = new TableColumn<>("Danh mục");
        pCatCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<ProductModel, Double> pPriceCol = new TableColumn<>("Giá");
        pPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        pPriceCol.setCellFactory(col -> new javafx.scene.control.TableCell<ProductModel, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null)
                    setText(null);
                else
                    setText(currency.format(price));
            }
        });

        TableColumn<ProductModel, Boolean> pAvailCol = new TableColumn<>("Còn bán");
        pAvailCol.setCellValueFactory(new PropertyValueFactory<>("available"));

        productTable.getColumns().addAll(pIdCol, pNameCol, pCatCol, pPriceCol, pAvailCol);

        HBox btnBox = new HBox(10);
        Button addProdBtn = createPrimaryButton("➕ Thêm");
        Button editProdBtn = createGhostButton("✏️ Sửa");
        Button delProdBtn = createGhostButton("🗑️ Xóa");
        Button toggleAvailBtn = createGhostButton("🔒 Khóa/Mở");
        Button updatePriceBtn = createGhostButton("💰 Đổi giá");

        addProdBtn.setOnAction(e -> showAddProductDialog());
        editProdBtn.setOnAction(e -> showEditProductDialog());
        delProdBtn.setOnAction(e -> deleteProduct());
        toggleAvailBtn.setOnAction(e -> toggleProductAvailable());
        updatePriceBtn.setOnAction(e -> showUpdatePriceDialog());

        btnBox.getChildren().addAll(addProdBtn, editProdBtn, delProdBtn, toggleAvailBtn, updatePriceBtn);

        panel.getChildren().addAll(title, productTable, btnBox);
        VBox.setVgrow(productTable, Priority.ALWAYS);

        return panel;
    }

    // ==================== EMPLOYEE TAB ====================
    private VBox buildEmployeeTab() {
        VBox employeeTab = new VBox(15);
        employeeTab.setPadding(new Insets(15));

        Label title = new Label("Quản lý Nhân viên");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        employeeTable = new TableView<>(employees);
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<EmployeeModel, Integer> eIdCol = new TableColumn<>("ID");
        eIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<EmployeeModel, String> eNameCol = new TableColumn<>("Tên");
        eNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<EmployeeModel, String> eUsernameCol = new TableColumn<>("Username");
        eUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<EmployeeModel, Integer> eRoleCol = new TableColumn<>("Vai trò");
        eRoleCol.setCellValueFactory(new PropertyValueFactory<>("roleId"));

        TableColumn<EmployeeModel, Boolean> eActiveCol = new TableColumn<>("Hoạt động");
        eActiveCol.setCellValueFactory(new PropertyValueFactory<>("active"));

        employeeTable.getColumns().addAll(eIdCol, eNameCol, eUsernameCol, eRoleCol, eActiveCol);

        HBox btnBox = new HBox(10);
        Button addEmpBtn = createPrimaryButton("➕ Thêm");
        Button editEmpBtn = createGhostButton("✏️ Sửa");
        Button delEmpBtn = createGhostButton("🗑️ Xóa");
        Button changeRoleBtn = createGhostButton("👤 Đổi vai trò");
        Button lockBtn = createGhostButton("🔒 Khóa/Mở");
        Button hoursBtn = createGhostButton("⏰ Giờ làm");

        addEmpBtn.setOnAction(e -> showAddEmployeeDialog());
        editEmpBtn.setOnAction(e -> showEditEmployeeDialog());
        delEmpBtn.setOnAction(e -> deleteEmployee());
        changeRoleBtn.setOnAction(e -> showChangeRoleDialog());
        lockBtn.setOnAction(e -> toggleEmployeeLock());
        hoursBtn.setOnAction(e -> showWorkingHoursDialog());

        btnBox.getChildren().addAll(addEmpBtn, editEmpBtn, delEmpBtn, changeRoleBtn, lockBtn, hoursBtn);

        employeeTab.getChildren().addAll(title, employeeTable, btnBox);
        VBox.setVgrow(employeeTable, Priority.ALWAYS);

        return employeeTab;
    }

    // ==================== INVENTORY TAB ====================
    private VBox buildInventoryTab() {
        VBox inventoryTab = new VBox(15);
        inventoryTab.setPadding(new Insets(15));

        Label title = new Label("Quản lý Kho");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        // Alert panel
        VBox alertPanel = new VBox(5);
        alertPanel.setPadding(new Insets(10));
        alertPanel.setStyle("-fx-background-color:#FFF3CD;-fx-background-radius:8;");
        Label alertTitle = new Label("⚠️ Cảnh báo nguyên liệu sắp hết");
        alertTitle.setStyle("-fx-font-weight:bold;");
        Label alertLabel = new Label("Đang tải...");
        alertPanel.getChildren().addAll(alertTitle, alertLabel);

        inventoryTable = new TableView<>(inventoryItems);
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<InventoryModel, Integer> invIdCol = new TableColumn<>("ID");
        invIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<InventoryModel, String> invNameCol = new TableColumn<>("Tên nguyên liệu");
        invNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<InventoryModel, Double> invQtyCol = new TableColumn<>("Số lượng");
        invQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<InventoryModel, String> invUnitCol = new TableColumn<>("Đơn vị");
        invUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<InventoryModel, String> invStatusCol = new TableColumn<>("Trạng thái");
        invStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        inventoryTable.getColumns().addAll(invIdCol, invNameCol, invQtyCol, invUnitCol, invStatusCol);

        HBox btnBox = new HBox(10);
        Button importBtn = createPrimaryButton("📥 Nhập kho");
        Button exportBtn = createGhostButton("📤 Xuất kho");
        Button refreshInvBtn = createGhostButton("🔄 Làm mới");

        importBtn.setOnAction(e -> showImportDialog());
        exportBtn.setOnAction(e -> showExportDialog());
        refreshInvBtn.setOnAction(e -> loadInventory());

        btnBox.getChildren().addAll(importBtn, exportBtn, refreshInvBtn);

        inventoryTab.getChildren().addAll(title, alertPanel, inventoryTable, btnBox);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        // Load alerts
        loadLowStockAlerts(alertLabel);

        return inventoryTab;
    }

    // ==================== COUPON TAB ====================
    private VBox buildCouponTab() {
        VBox couponTab = new VBox(15);
        couponTab.setPadding(new Insets(15));

        Label title = new Label("Quản lý Mã giảm giá");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        couponTable = new TableView<>(coupons);
        couponTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<CouponModel, String> cCodeCol = new TableColumn<>("Mã");
        cCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<CouponModel, String> cTypeCol = new TableColumn<>("Loại");
        cTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<CouponModel, Double> cValueCol = new TableColumn<>("Giá trị");
        cValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        TableColumn<CouponModel, String> cUsageCol = new TableColumn<>("Đã dùng/Limit");
        cUsageCol.setCellValueFactory(new PropertyValueFactory<>("usageDisplay"));

        couponTable.getColumns().addAll(cCodeCol, cTypeCol, cValueCol, cUsageCol);

        HBox btnBox = new HBox(10);
        Button addCouponBtn = createPrimaryButton("➕ Tạo mã");
        Button editCouponBtn = createGhostButton("✏️ Sửa");
        Button delCouponBtn = createGhostButton("🗑️ Xóa");
        Button usageBtn = createGhostButton("📊 Lịch sử dùng");

        addCouponBtn.setOnAction(e -> showAddCouponDialog());
        editCouponBtn.setOnAction(e -> showEditCouponDialog());
        delCouponBtn.setOnAction(e -> deleteCoupon());
        usageBtn.setOnAction(e -> showCouponUsageDialog());

        btnBox.getChildren().addAll(addCouponBtn, editCouponBtn, delCouponBtn, usageBtn);

        couponTab.getChildren().addAll(title, couponTable, btnBox);
        VBox.setVgrow(couponTable, Priority.ALWAYS);

        return couponTab;
    }

    // ==================== REPORT TAB ====================
    private VBox buildReportTab() {
        VBox reportTab = new VBox(15);
        reportTab.setPadding(new Insets(15));

        Label title = new Label("Báo cáo & Thống kê");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        // Report type selector
        HBox selectorBox = new HBox(10);
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll("Doanh thu theo ngày", "Doanh thu theo tuần",
                "Doanh thu theo tháng", "Doanh thu theo ca", "Món bán chạy", "Số lượng khách", "Chi phí");
        reportTypeCombo.setValue("Doanh thu theo ngày");
        reportTypeCombo.setPrefWidth(200);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> monthCombo = new ComboBox<>();
        monthCombo.getItems().addAll("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12");
        monthCombo.setValue("Tháng " + LocalDate.now().getMonthValue());

        ComboBox<Integer> yearCombo = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear; i++) {
            yearCombo.getItems().add(i);
        }
        yearCombo.setValue(currentYear);

        Button loadBtn = createPrimaryButton("📊 Tải báo cáo");

        loadBtn.setOnAction(e -> loadReport(reportTypeCombo.getValue(), datePicker.getValue(),
                monthCombo.getSelectionModel().getSelectedIndex() + 1, yearCombo.getValue()));

        selectorBox.getChildren().addAll(new Label("Loại báo cáo:"), reportTypeCombo,
                new Label("Ngày:"), datePicker, new Label("Tháng:"), monthCombo,
                new Label("Năm:"), yearCombo, loadBtn);
        selectorBox.setAlignment(Pos.CENTER_LEFT);

        // Report display area
        VBox reportContent = new VBox(10);
        reportContent.setPadding(new Insets(10));
        reportContent.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;");
        reportContent.setPrefHeight(500);

        Label reportLabel = new Label("Chọn loại báo cáo và nhấn 'Tải báo cáo'");
        reportLabel.setStyle("-fx-text-fill:#6B4C3B;");
        reportContent.getChildren().add(reportLabel);

        reportTab.getChildren().addAll(title, selectorBox, reportContent);
        VBox.setVgrow(reportContent, Priority.ALWAYS);

        return reportTab;
    }

    // ==================== MODEL CLASSES ====================
    public static class CategoryModel {
        private final int id;
        private final String name;

        public CategoryModel(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class ProductModel {
        private final int id;
        private final String name;
        private final int categoryId;
        private final String categoryName;
        private final double price;
        private final boolean available;
        private final boolean isHot;

        public ProductModel(int id, String name, int categoryId, String categoryName, double price, boolean available,
                boolean isHot) {
            this.id = id;
            this.name = name;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.price = price;
            this.available = available;
            this.isHot = isHot;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public double getPrice() {
            return price;
        }

        public boolean getAvailable() {
            return available;
        }

        public boolean getIsHot() {
            return isHot;
        }
    }

    public static class EmployeeModel {
        private final int id;
        private final String name;
        private final String username;
        private final int roleId;
        private final boolean active;

        public EmployeeModel(int id, String name, String username, int roleId, boolean active) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.roleId = roleId;
            this.active = active;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public int getRoleId() {
            return roleId;
        }

        public boolean getActive() {
            return active;
        }
    }

    public static class InventoryModel {
        private final int id;
        private final String name;
        private final double quantity;
        private final String unit;
        private final String status;

        public InventoryModel(int id, String name, double quantity, String unit, String status) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class CouponModel {
        private final int id;
        private final String code;
        private final String type;
        private final double value;
        private final int usageCount;
        private final int usageLimit;

        public CouponModel(int id, String code, String type, double value, int usageCount, int usageLimit) {
            this.id = id;
            this.code = code;
            this.type = type;
            this.value = value;
            this.usageCount = usageCount;
            this.usageLimit = usageLimit;
        }

        public int getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getType() {
            return type;
        }

        public double getValue() {
            return value;
        }

        public int getUsageCount() {
            return usageCount;
        }

        public int getUsageLimit() {
            return usageLimit;
        }

        public String getUsageDisplay() {
            return usageCount + "/" + usageLimit;
        }
    }

    // ==================== API CALL METHODS ====================
    private void loadCategories() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_CATEGORIES_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                String body = response.body();
                String[] items = extractDataArrayObjects(body);
                if (items.length == 0) {
                    Platform.runLater(() -> categories.clear());
                    return;
                }

                var loaded = FXCollections.<CategoryModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "id"), extractJsonValue(obj, "category_id"));
                    String name = firstNonBlank(extractJsonValue(obj, "name"), extractJsonValue(obj, "category_name"));
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    int id = parseIntSafe(idStr);
                    loaded.add(new CategoryModel(id, name));
                }

                Platform.runLater(() -> {
                    categories.setAll(loaded);
                    if (categoryTable != null) {
                        categoryTable.refresh();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading categories: " + e.getMessage());
            }
        }).start();
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_PRODUCTS_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                String body = response.body();
                String[] items = extractDataArrayObjects(body);
                if (items.length == 0) {
                    Platform.runLater(() -> products.clear());
                    return;
                }

                var loaded = FXCollections.<ProductModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "product_id"), extractJsonValue(obj, "id"));
                    String name = firstNonBlank(extractJsonValue(obj, "product_name"), extractJsonValue(obj, "name"));
                    String categoryIdStr = firstNonBlank(extractJsonValue(obj, "category_id"),
                            extractJsonValue(obj, "categoryId"));
                    String categoryName = firstNonBlank(extractJsonValue(obj, "category_name"),
                            extractJsonValue(obj, "categoryName"));
                    String priceStr = firstNonBlank(extractJsonValue(obj, "price"),
                            extractJsonValue(obj, "unit_price"));
                    String availableStr = firstNonBlank(extractJsonValue(obj, "available"),
                            extractJsonValue(obj, "is_available"));
                    String hotStr = firstNonBlank(extractJsonValue(obj, "is_hot"), extractJsonValue(obj, "hot"));

                    if (name == null || name.isBlank()) {
                        continue;
                    }

                    int id = parseIntSafe(idStr);
                    int categoryId = parseIntSafe(categoryIdStr);
                    double price = parseDoubleSafe(priceStr);
                    boolean available = parseBooleanInt(availableStr, true);
                    boolean isHot = parseBooleanInt(hotStr, false);
                    loaded.add(new ProductModel(id, name, categoryId, categoryName, price, available, isHot));
                }

                Platform.runLater(() -> {
                    products.setAll(loaded);
                    if (productTable != null) {
                        productTable.refresh();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading products: " + e.getMessage());
            }
        }).start();
    }

    private void loadInventory() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_INVENTORY_LIST_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                String body = response.body();
                String[] items = extractDataArrayObjects(body);
                if (items.length == 0) {
                    Platform.runLater(() -> inventoryItems.clear());
                    return;
                }

                var loaded = FXCollections.<InventoryModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "id"), extractJsonValue(obj, "inventory_id"));
                    String name = firstNonBlank(extractJsonValue(obj, "name"), extractJsonValue(obj, "inventory_name"));
                    String qtyStr = firstNonBlank(extractJsonValue(obj, "quantity"), extractJsonValue(obj, "qty"),
                            extractJsonValue(obj, "stock"));
                    String unit = firstNonBlank(extractJsonValue(obj, "unit"), extractJsonValue(obj, "inventory_unit"));
                    String status = firstNonBlank(extractJsonValue(obj, "status"),
                            extractJsonValue(obj, "inventory_status"));

                    if (name == null || name.isBlank()) {
                        continue;
                    }

                    int id = parseIntSafe(idStr);
                    double qty = parseDoubleSafe(qtyStr);
                    loaded.add(new InventoryModel(id, name, qty, unit, status));
                }

                Platform.runLater(() -> {
                    inventoryItems.setAll(loaded);
                    if (inventoryTable != null) {
                        inventoryTable.refresh();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading inventory: " + e.getMessage());
            }
        }).start();
    }

    private void loadLowStockAlerts(Label alertLabel) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_LOW_STOCK_ALERT_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                String[] items = extractDataArrayObjects(body);
                if (items.length == 0) {
                    Platform.runLater(() -> alertLabel.setText("Không có cảnh báo"));
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String name = firstNonBlank(extractJsonValue(obj, "name"), extractJsonValue(obj, "inventory_name"));
                    String qtyStr = firstNonBlank(extractJsonValue(obj, "quantity"), extractJsonValue(obj, "qty"),
                            extractJsonValue(obj, "stock"));
                    String unit = firstNonBlank(extractJsonValue(obj, "unit"), extractJsonValue(obj, "inventory_unit"));
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    if (!sb.isEmpty()) {
                        sb.append("\n");
                    }
                    String qtyDisplay = qtyStr == null ? "" : qtyStr;
                    sb.append(name);
                    if (!qtyDisplay.isBlank()) {
                        sb.append(": ").append(qtyDisplay);
                        if (unit != null && !unit.isBlank()) {
                            sb.append(" ").append(unit);
                        }
                    }
                }

                String text = sb.isEmpty() ? "Không có cảnh báo" : sb.toString();
                Platform.runLater(() -> alertLabel.setText(text));
            } catch (Exception e) {
                System.err.println("Error loading alerts: " + e.getMessage());
            }
        }).start();
    }

    private void loadCoupons() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_COUPONS_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                String body = response.body();
                String[] items = extractDataArrayObjects(body);
                if (items.length == 0) {
                    Platform.runLater(() -> coupons.clear());
                    return;
                }

                var loaded = FXCollections.<CouponModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "id"), extractJsonValue(obj, "coupon_id"));
                    String code = firstNonBlank(extractJsonValue(obj, "code"), extractJsonValue(obj, "coupon_code"));
                    String type = firstNonBlank(extractJsonValue(obj, "type"), extractJsonValue(obj, "coupon_type"));
                    String valueStr = firstNonBlank(extractJsonValue(obj, "value"),
                            extractJsonValue(obj, "discount_value"));
                    String usageCountStr = firstNonBlank(extractJsonValue(obj, "usage_count"),
                            extractJsonValue(obj, "used"));
                    String usageLimitStr = firstNonBlank(extractJsonValue(obj, "usage_limit"),
                            extractJsonValue(obj, "limit"));

                    if (code == null || code.isBlank()) {
                        continue;
                    }

                    int id = parseIntSafe(idStr);
                    double value = parseDoubleSafe(valueStr);
                    int usageCount = parseIntSafe(usageCountStr);
                    int usageLimit = parseIntSafe(usageLimitStr);
                    loaded.add(new CouponModel(id, code, type, value, usageCount, usageLimit));
                }

                Platform.runLater(() -> {
                    coupons.setAll(loaded);
                    if (couponTable != null) {
                        couponTable.refresh();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading coupons: " + e.getMessage());
            }
        }).start();
    }

    private void loadReport(String type, LocalDate date, int month, int year) {
        new Thread(() -> {
            try {
                String url = "";
                if (type.contains("ngày")) {
                    url = REVENUE_BY_DAY_URL + "?date=" + date.toString();
                } else if (type.contains("tuần")) {
                    url = REVENUE_BY_WEEK_URL + "?year=" + year + "&week=" + LocalDate.now().getDayOfYear() / 7;
                } else if (type.contains("tháng")) {
                    url = REVENUE_BY_MONTH_URL + "?year=" + year + "&month=" + month;
                } else if (type.contains("ca")) {
                    url = REVENUE_BY_SHIFT_URL + "?date=" + date.toString();
                } else if (type.contains("Món bán chạy")) {
                    url = BEST_SELLING_PRODUCTS_URL + "?year=" + year + "&month=" + month;
                } else if (type.contains("khách")) {
                    url = CUSTOMER_COUNT_URL + "?year=" + year + "&month=" + month;
                } else if (type.contains("Chi phí")) {
                    url = EXPENSES_URL + "?year=" + year + "&month=" + month;
                }

                if (!url.isEmpty()) {
                    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    String body = response.body();
                    String display = extractReportDisplay(body);
                    Platform.runLater(() -> {
                        // TODO: Implement report display logic
                        System.out.println("Report: " + display);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading report: " + e.getMessage());
            }
        }).start();
    }

    // ==================== DIALOG METHODS ====================
    private void showAddCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm danh mục");
        dialog.setHeaderText("Nhập tên danh mục mới");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.isEmpty()) {
                // Call API add_category.php
            }
        });
    }

    private void showEditCategoryDialog() {
        // TODO: Implement
    }

    private void deleteCategory() {
        // TODO: Implement
    }

    private void showAddProductDialog() {
        // TODO: Implement
    }

    private void showEditProductDialog() {
        // TODO: Implement
    }

    private void deleteProduct() {
        // TODO: Implement
    }

    private void toggleProductAvailable() {
        // TODO: Implement
    }

    private void showUpdatePriceDialog() {
        // TODO: Implement
    }

    private void showAddEmployeeDialog() {
        // TODO: Implement
    }

    private void showEditEmployeeDialog() {
        // TODO: Implement
    }

    private void deleteEmployee() {
        // TODO: Implement
    }

    private void showChangeRoleDialog() {
        // TODO: Implement
    }

    private void toggleEmployeeLock() {
        // TODO: Implement
    }

    private void showWorkingHoursDialog() {
        // TODO: Implement
    }

    private void showImportDialog() {
        // TODO: Implement
    }

    private void showExportDialog() {
        // TODO: Implement
    }

    private void showAddCouponDialog() {
        // TODO: Implement
    }

    private void showEditCouponDialog() {
        // TODO: Implement
    }

    private void deleteCoupon() {
        // TODO: Implement
    }

    private void showCouponUsageDialog() {
        // TODO: Implement
    }

    // ==================== JSON PARSER HELPER ====================
    private String[] extractDataArrayObjects(String body) {
        if (body == null || body.isBlank()) {
            return new String[0];
        }
        int dataIdx = body.indexOf("\"data\"");
        if (dataIdx < 0) {
            return new String[0];
        }
        int arrayStart = body.indexOf('[', dataIdx);
        int arrayEnd = body.indexOf(']', arrayStart);
        if (arrayStart < 0 || arrayEnd < 0 || arrayEnd <= arrayStart) {
            return new String[0];
        }
        String dataArray = body.substring(arrayStart + 1, arrayEnd);
        if (dataArray.isBlank()) {
            return new String[0];
        }
        return dataArray.split("\\},\\s*\\{");
    }

    private String normalizeJsonObject(String raw) {
        if (raw == null) {
            return "{}";
        }
        String obj = raw.trim();
        if (!obj.startsWith("{")) {
            obj = "{" + obj;
        }
        if (!obj.endsWith("}")) {
            obj = obj + "}";
        }
        return obj;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private int parseIntSafe(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.replace("\"", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(raw.replace("\"", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean parseBooleanInt(String raw, boolean defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        String v = raw.replace("\"", "").trim();
        if ("1".equals(v) || "true".equalsIgnoreCase(v)) {
            return true;
        }
        if ("0".equals(v) || "false".equalsIgnoreCase(v)) {
            return false;
        }
        return defaultValue;
    }

    private String extractReportDisplay(String body) {
        if (body == null || body.isBlank()) {
            return "Không có dữ liệu";
        }
        String[] items = extractDataArrayObjects(body);
        if (items.length == 0) {
            return body;
        }
        StringBuilder sb = new StringBuilder();
        for (String raw : items) {
            String obj = normalizeJsonObject(raw);
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(obj);
        }
        return sb.toString();
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || json.trim().isEmpty())
            return "";

        // Tạo pattern tìm key: "key": hoặc "key":value (có thể có khoảng trắng)
        String pattern = "\"" + key + "\"\\s*:\\s*";
        int start = -1;

        // Tìm vị trí đầu tiên của pattern
        for (int i = 0; i < json.length(); i++) {
            if (json.startsWith(pattern, i)) {
                start = i + pattern.length();
                break;
            }
        }

        if (start < 0)
            return "";

        // Bỏ qua khoảng trắng sau dấu :
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length())
            return "";

        char firstChar = json.charAt(start);

        if (firstChar == '"') {
            // Là chuỗi
            start++; // bỏ dấu " mở
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escape) {
                    sb.append(c);
                    escape = false;
                    continue;
                }
                if (c == '\\') {
                    escape = true;
                    continue;
                }
                if (c == '"') {
                    // Kết thúc chuỗi
                    return sb.toString();
                }
                sb.append(c);
            }
            return sb.toString(); // trường hợp chuỗi không đóng (dù hiếm)
        } else {
            // Là số, boolean, null, hoặc object/array (lấy đến dấu , hoặc })
            int end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == ',' || c == '}' || c == ']') {
                    break;
                }
                end++;
            }
            String value = json.substring(start, end).trim();
            // Loại bỏ dấu nháy nếu có (thỉnh thoảng backend trả số trong dấu nháy)
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        }
    }

    public static class PaymentRecord {
        private final String time;
        private final String table;
        private final double amount;
        private final String method;

        public PaymentRecord(String time, String table, double amount, String method) {
            this.time = time;
            this.table = table;
            this.amount = amount;
            this.method = method;
        }

        public String getTime() {
            return time;
        }

        public String getTable() {
            return table;
        }

        public double getAmount() {
            return amount;
        }

        public String getMethod() {
            return method;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
