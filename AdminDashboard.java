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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class AdminDashboard extends Application {

    // Base URL for Admin API
    private static final String BASE_URL = "http://localhost/coffee-shop-app/backend/api/admin/";
    private static final String BASE_EMPLOYEE_URL = "http://localhost/coffee-shop-app/backend/api/employee/";

    // API Endpoints
    private static final String GET_ORDER_HISTORY_URL = BASE_URL + "get-order-history.php";
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
    private static final String GET_EMPLOYEES_URL = BASE_URL + "get_employee.php";
    private static final String ADD_FACEID_URL = BASE_URL + "add_faceid.php";
    private static final String DELETE_FACEID_URL = BASE_URL + "delete_faceid.php";

    // Inventory
    private static final String GET_INVENTORY_LIST_URL = BASE_URL + "inventory/get-list.php";
    private static final String IMPORT_INVENTORY_URL = BASE_URL + "inventory/import.php";
    private static final String EXPORT_INVENTORY_URL = BASE_URL + "inventory/export.php";
    private static final String GET_LOW_STOCK_ALERT_URL = BASE_URL + "inventory/get-low-stock-alert.php";
    private static final String DELETE_INVENTORY_URL = BASE_URL + "inventory/delete_inventory.php";

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

    private static final String LOGOUT_URL = BASE_EMPLOYEE_URL + "logout.php";
    // UI Components - Dashboard
    private Label totalRevenueLabel;
    private Label todayRevenueLabel;
    private Label weeklyRevenueLabel;
    private Label monthlyRevenueLabel;
    private Label cashLabel;
    private Label cardLabel;
    private Label transferLabel;
    private TableView<PaymentRecord> dashboardTable;
    private TextField searchField;
    private ComboBox<String> filterMethodCombo;
    private ComboBox<String> dateRangeCombo;
    private TextArea reportDisplayArea; // Changed from Label to TextArea for better display

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

    // Report Tab Data
    private TableView<ReportItem> reportTable;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        // Màu nền chính xám be (#F5F0E1)
        root.setStyle("-fx-background-color:#F5F0E1;-fx-font-family:'Segoe UI',sans-serif;");

        // Header
        VBox header = buildHeader();
        root.setTop(header);

        // TabPane với các tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        // Đặt màu nền cho khu vực chứa nội dung tab để đồng bộ
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

        // --- ÁP DỤNG THEME NÂU CHO TABS ---
        // Gọi hàm tạo CSS và thêm vào scene
        String cssUrl = createCoffeeThemeCss();
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl);
        }

        primaryStage.setTitle("Coffee Bean - Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load initial data
        loadDashboardData();
        loadCategories();
        loadProducts();
        loadInventory();
        loadCoupons();
        loadEmployees();
    }

    /**
     * Hàm này tạo ra một file CSS tạm thời chứa định nghĩa giao diện
     * cho các tab theo tông màu nâu/vàng của ứng dụng.
     * 
     * @return URL dạng chuỗi đến file CSS tạm thời.
     */
    private String createCoffeeThemeCss() {
        try {
            // Màu nền chính: #F5F0E1
            // Màu nâu đậm (text/border): #6B4C3B
            // Màu vàng nút bấm (hover): #F2C57C
            // Màu be đậm hơn cho tab chưa chọn: #E6DCCA

            String cssContent = "/* Nền tổng thể của TabPane */\n" +
                    ".tab-pane {\n" +
                    "    -fx-background-color: #F5F0E1;\n" +
                    "}\n" +
                    "/* Khu vực chứa các đầu tab (header) */\n" +
                    ".tab-pane > .tab-header-area {\n" +
                    "    -fx-padding: 5 5 0 5;\n" +
                    "}\n" +
                    "/* Nền phía sau các tab header - vẽ một đường kẻ nâu bên dưới */\n" +
                    ".tab-pane > .tab-header-area > .tab-header-background {\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-border-color: #6B4C3B;\n" +
                    "    -fx-border-width: 0 0 2 0;\n" +
                    "}\n" +
                    "/* Định dạng cho các Tab khi CHƯA chọn */\n" +
                    ".tab {\n" +
                    "    -fx-background-color: #E6DCCA;\n" + // Màu be hơi tối hơn nền
                    "    -fx-background-radius: 10 10 0 0;\n" + // Bo tròn góc trên
                    "    -fx-border-color: #A08060;\n" + // Viền nâu nhạt
                    "    -fx-border-width: 1 1 0 1;\n" +
                    "    -fx-border-radius: 10 10 0 0;\n" +
                    "    -fx-padding: 6 12 6 12;\n" +
                    "    -fx-cursor: hand;\n" +
                    "}\n" +
                    "/* Chữ bên trong Tab */\n" +
                    ".tab .tab-label {\n" +
                    "    -fx-text-fill: #6B4C3B;\n" + // Chữ màu nâu đậm
                    "    -fx-font-weight: bold;\n" +
                    "    -fx-font-size: 13px;\n" +
                    "}\n" +
                    "/* Hiệu ứng khi di chuột vào Tab chưa chọn */\n" +
                    ".tab:hover {\n" +
                    "    -fx-background-color: #F2C57C;\n" + // Màu vàng giống nút bấm
                    "}\n" +
                    "/* Định dạng cho Tab khi ĐANG ĐƯỢC CHỌN */\n" +
                    ".tab:selected {\n" +
                    "    -fx-background-color: #F5F0E1;\n" + // Nền tiệp với màu nền nội dung
                    "    -fx-border-color: #6B4C3B;\n" + // Viền nâu đậm nổi bật
                    "    -fx-border-width: 3 2 0 2;\n" + // Viền trên dày hơn để làm điểm nhấn
                    "    -fx-background-insets: 0 0 -2 0;\n" + // Đẩy nền xuống che đường kẻ ngang
                    "}\n" +
                    "/* Chữ bên trong Tab đang chọn */\n" +
                    ".tab:selected .tab-label {\n" +
                    "    -fx-font-size: 14px;\n" +
                    "    -fx-translate-y: -1;\n" + // Nhích chữ lên xíu do viền dày
                    "}";

            File tempCssFile = File.createTempFile("coffee_theme", ".css");
            tempCssFile.deleteOnExit(); // Tự động xóa file khi tắt app
            try (FileWriter writer = new FileWriter(tempCssFile)) {
                writer.write(cssContent);
            }
            return tempCssFile.toURI().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private VBox buildHeader() {
        Label title = new Label("☕ Coffee - Quản trị hệ thống");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:28px;-fx-font-weight:bold;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪 Đăng xuất");
        logoutBtn.setStyle("-fx-background-color:#D32F2F;-fx-text-fill:white;-fx-font-weight:bold;"
                + "-fx-background-radius:10;-fx-cursor:hand;-fx-padding: 8 15;");

        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color:#B71C1C;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-padding: 8 15;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color:#D32F2F;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;-fx-padding: 8 15;"));

        logoutBtn.setOnAction(e -> handleLogout((Stage) logoutBtn.getScene().getWindow()));

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(title, spacer, logoutBtn);
        headerBox.setPadding(new Insets(10, 20, 15, 10));

        return new VBox(headerBox);
    }

    // ==================== DASHBOARD TAB ====================
    private VBox buildDashboardTab() {
        VBox dashboard = new VBox(15);
        dashboard.setPadding(new Insets(15, 20, 20, 20));
        FlowPane statsCards = buildStatsCards();
        HBox controlsPanel = buildControlsPanel();
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
        VBox totalCard = createStatCard("Tổng doanh thu", "0", "#6B4C3B");
        totalRevenueLabel = (Label) totalCard.getChildren().get(1);
        VBox todayCard = createStatCard("Hôm nay", "0", "#C08A64");
        todayRevenueLabel = (Label) todayCard.getChildren().get(1);
        VBox weeklyCard = createStatCard("Tuần này", "0", "#F2C57C");
        weeklyRevenueLabel = (Label) weeklyCard.getChildren().get(1);
        VBox monthlyCard = createStatCard("Tháng này", "0", "#6B4C3B");
        monthlyRevenueLabel = (Label) monthlyCard.getChildren().get(1);
        VBox cashCard = createStatCard("Tiền mặt", "0", "#6B4C3B");
        cashLabel = (Label) cashCard.getChildren().get(1);
        VBox cardCard = createStatCard("💳 Ví điện tử", "0", "#C08A64");
        cardLabel = (Label) cardCard.getChildren().get(1);
        VBox transferCard = createStatCard("🏦 Chuyển khoản", "0", "#6B4C3B");
        transferLabel = (Label) transferCard.getChildren().get(1);
        cards.getChildren().addAll(totalCard, todayCard, weeklyCard, monthlyCard, cashCard, cardCard, transferCard);
        return cards;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color:#FFFFFF;-fx-background-radius:18;-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");
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
        Label searchLabel = new Label("Tìm kiếm:");
        searchLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        searchField = new TextField();
        searchField.setPromptText("Tìm theo bàn, phương thức...");
        searchField.setPrefWidth(250);
        searchField.setStyle(
                "-fx-background-radius:12;-fx-background-color:#FAFAFA;-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;-fx-padding:10;-fx-text-fill:#6B4C3B;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        Label methodLabel = new Label("Phương thức:");
        methodLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        filterMethodCombo = new ComboBox<>();
        filterMethodCombo.getItems().addAll("Tất cả", "Tiền mặt", "Ví điện tử", "Chuyển khoản");
        filterMethodCombo.setValue("Tất cả");
        filterMethodCombo.setPrefWidth(150);
        filterMethodCombo.setStyle(
                "-fx-background-radius:12;-fx-background-color:#FAFAFA;-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;-fx-padding:10;-fx-text-fill:#6B4C3B;");
        filterMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        Label dateLabel = new Label("Khoảng thời gian:");
        dateLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        dateRangeCombo = new ComboBox<>();
        dateRangeCombo.getItems().addAll("Tất cả", "Hôm nay", "Tuần này", "Tháng này", "7 ngày qua", "30 ngày qua");
        dateRangeCombo.setValue("Tất cả");
        dateRangeCombo.setPrefWidth(150);
        dateRangeCombo.setStyle(
                "-fx-background-radius:12;-fx-background-color:#FAFAFA;-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;-fx-padding:10;-fx-text-fill:#6B4C3B;");
        dateRangeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
            updateStatistics();
        });
        Button refreshBtn = createPrimaryButton("🔄 Làm mới");
        refreshBtn.setOnAction(e -> {
            loadDashboardData();
            updateStatistics();
        });
        HBox buttonBox = new HBox(10, refreshBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);
        controls.getChildren().addAll(searchLabel, searchField, methodLabel, filterMethodCombo, dateLabel,
                dateRangeCombo, buttonBox);
        return controls;
    }

    private VBox buildTablePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle(
                "-fx-background-color:#FFFFFF;-fx-background-radius:18;-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");
        Label tableTitle = new Label("Lịch sử thanh toán");
        tableTitle.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:18px;-fx-font-weight:bold;");
        dashboardTable = new TableView<>(sortedPayments);
        dashboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dashboardTable.setPlaceholder(new Label("Chưa có dữ liệu thanh toán"));
        dashboardTable.setStyle(
                "-fx-background-color:#FFFFFF;-fx-background-radius:12;-fx-border-color:rgba(107,76,59,0.1);-fx-border-width:1;");
        sortedPayments.comparatorProperty().bind(dashboardTable.comparatorProperty());
        TableColumn<PaymentRecord, String> colDate = new TableColumn<>("Thời gian");
        colDate.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDate.setPrefWidth(180);
        colDate.setStyle("-fx-alignment:CENTER_LEFT;");
        TableColumn<PaymentRecord, String> colTable = new TableColumn<>("Bàn");
        colTable.setCellValueFactory(new PropertyValueFactory<>("table"));
        colTable.setPrefWidth(120);
        colTable.setStyle("-fx-alignment:CENTER;");
        TableColumn<PaymentRecord, String> colMethod = new TableColumn<>("Phương thức");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colMethod.setPrefWidth(140);
        colMethod.setStyle("-fx-alignment:CENTER;");
        TableColumn<PaymentRecord, Double> colAmount = new TableColumn<>("Số tiền");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(160);
        colAmount.setCellFactory(column -> new javafx.scene.control.TableCell<PaymentRecord, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null)
                    setText(null);
                else {
                    setText(currency.format(amount));
                    setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-alignment:CENTER_RIGHT;");
                }
            }
        });
        dashboardTable.getColumns().addAll(colDate, colTable, colMethod, colAmount);
        Label summaryLabel = new Label();
        summaryLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:14px;-fx-font-weight:bold;");
        updateSummaryLabel(summaryLabel);
        filteredPayments.addListener(
                (javafx.collections.ListChangeListener.Change<? extends PaymentRecord> c) -> updateSummaryLabel(
                        summaryLabel));
        panel.getChildren().addAll(tableTitle, dashboardTable, summaryLabel);
        VBox.setVgrow(dashboardTable, Priority.ALWAYS);
        return panel;
    }

    private void updateSummaryLabel(Label label) {
        double filteredTotal = filteredPayments.stream().mapToDouble(PaymentRecord::getAmount).sum();
        int count = filteredPayments.size();
        label.setText(String.format("Tổng: %s • %d giao dịch", currency.format(filteredTotal), count));
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String methodFilter = filterMethodCombo.getValue();
        String dateFilter = dateRangeCombo.getValue();
        filteredPayments.setPredicate(payment -> {
            boolean matchesSearch = searchText.isEmpty() || payment.getTable().toLowerCase().contains(searchText)
                    || payment.getMethod().toLowerCase().contains(searchText);
            boolean matchesMethod = methodFilter == null || methodFilter.equals("Tất cả")
                    || payment.getMethod().equals(methodFilter);
            boolean matchesDate = true;
            if (dateFilter != null && !dateFilter.equals("Tất cả")) {
                try {
                    LocalDate paymentDate = LocalDate.parse(payment.getTime().substring(0, 10),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate today = LocalDate.now();
                    switch (dateFilter) {
                        case "Hôm nay":
                            matchesDate = paymentDate.equals(today);
                            break;
                        case "Tuần này":
                            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                            matchesDate = !paymentDate.isBefore(weekStart) && !paymentDate.isAfter(today);
                            break;
                        case "Tháng này":
                            matchesDate = paymentDate.getMonth() == today.getMonth()
                                    && paymentDate.getYear() == today.getYear();
                            break;
                        case "7 ngày qua":
                            matchesDate = !paymentDate.isBefore(today.minusDays(7)) && !paymentDate.isAfter(today);
                            break;
                        case "30 ngày qua":
                            matchesDate = !paymentDate.isBefore(today.minusDays(30)) && !paymentDate.isAfter(today);
                            break;
                    }
                } catch (Exception e) {
                    matchesDate = false;
                }
            }
            return matchesSearch && matchesMethod && matchesDate;
        });
    }

    private void updateStatistics() {
        if (allPayments.isEmpty()) {
            totalRevenueLabel.setText(currency.format(0));
            todayRevenueLabel.setText(currency.format(0));
            weeklyRevenueLabel.setText(currency.format(0));
            monthlyRevenueLabel.setText(currency.format(0));
            cashLabel.setText(currency.format(0));
            cardLabel.setText(currency.format(0));
            transferLabel.setText(currency.format(0));
            return;
        }
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        double total = filteredPayments.stream().mapToDouble(PaymentRecord::getAmount).sum();
        double todayTotal = allPayments.stream()
                .filter(p -> p.getTime().startsWith(today.toString()))
                .mapToDouble(PaymentRecord::getAmount).sum();
        double monthlyTotal = allPayments.stream()
                .filter(p -> {
                    try {
                        LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10));
                        return pDate.getMonthValue() == currentMonth && pDate.getYear() == currentYear;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .mapToDouble(PaymentRecord::getAmount).sum();
        LocalDate oneWeekAgo = today.minusDays(7);
        double weeklyTotal = allPayments.stream()
                .filter(p -> {
                    try {
                        LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10));
                        return !pDate.isBefore(oneWeekAgo) && !pDate.isAfter(today);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .mapToDouble(PaymentRecord::getAmount).sum();
        double cash = filteredPayments.stream()
                .filter(p -> p.getMethod().equalsIgnoreCase("Cash") || p.getMethod().equalsIgnoreCase("Tiền mặt"))
                .mapToDouble(PaymentRecord::getAmount).sum();
        double card = filteredPayments.stream()
                .filter(p -> p.getMethod().equalsIgnoreCase("Card") || p.getMethod().equalsIgnoreCase("Ví điện tử"))
                .mapToDouble(PaymentRecord::getAmount).sum();
        double transfer = filteredPayments.stream()
                .filter(p -> p.getMethod().equalsIgnoreCase("Transfer")
                        || p.getMethod().equalsIgnoreCase("Chuyển khoản"))
                .mapToDouble(PaymentRecord::getAmount).sum();
        totalRevenueLabel.setText(currency.format(total));
        todayRevenueLabel.setText(currency.format(todayTotal));
        weeklyRevenueLabel.setText(currency.format(weeklyTotal));
        monthlyRevenueLabel.setText(currency.format(monthlyTotal));
        cashLabel.setText(currency.format(cash));
        cardLabel.setText(currency.format(card));
        transferLabel.setText(currency.format(transfer));
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                System.out.println("================= BẮT ĐẦU TẢI DASHBOARD =================");
                System.out.println("[DEBUG] Đang gọi API: " + GET_ORDER_HISTORY_URL);

                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_ORDER_HISTORY_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("[DEBUG] Mã phản hồi (Code): " + response.statusCode());
                String body = response.body();
                System.out.println("[DEBUG] Nội dung phản hồi: " + body);

                if (response.statusCode() == 200) {
                    // Kiểm tra xem backend có báo lỗi trong JSON không
                    if (body.contains("\"success\":false")) {
                        System.err.println("[ERROR] Backend trả về lỗi: " + body);
                        return;
                    }

                    String[] items = extractDataArrayObjects(body);
                    System.out.println("[DEBUG] Tìm thấy " + items.length + " mục trong JSON.");

                    var loaded = FXCollections.<PaymentRecord>observableArrayList();

                    for (String raw : items) {
                        String obj = normalizeJsonObject(raw);

                        String time = extractJsonValue(obj, "created_at");
                        String table = extractJsonValue(obj, "table_name");
                        String method = extractJsonValue(obj, "payment_method");
                        String status = extractJsonValue(obj, "order_status");
                        double amount = parseDoubleSafe(extractJsonValue(obj, "total"));

                        // Log chi tiết từng đơn hàng để kiểm tra
                        // System.out.println("[DEBUG] Đơn: Time=" + time + ", Status=" + status + ",
                        // Total=" + amount);

                        // Xử lý dữ liệu trống
                        if (method == null || method.isEmpty() || method.equals("null"))
                            method = "Tiền mặt";
                        if (table == null || table.isEmpty())
                            table = "Mang về";

                        // Normalize payment method to Vietnamese
                        method = normalizePaymentMethod(method);

                        // Chỉ lấy đơn đã thanh toán (Không phân biệt hoa thường)
                        if ("paid".equalsIgnoreCase(status)) {
                            loaded.add(new PaymentRecord(time, table, method, amount));
                        } else {
                            // System.out.println("[DEBUG] -> Bỏ qua do trạng thái là: " + status);
                        }
                    }

                    System.out.println("[DEBUG] Số lượng đơn 'paid' hợp lệ: " + loaded.size());

                    Platform.runLater(() -> {
                        allPayments.setAll(loaded);
                        dashboardTable.refresh();
                        updateStatistics();
                        System.out.println("[DEBUG] Đã cập nhật giao diện Dashboard thành công.");
                    });
                } else {
                    System.err.println("[ERROR] Lỗi kết nối HTTP: " + response.statusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Lỗi", "Không tải được dữ liệu: " + e.getMessage()));
            }
        }).start();
    }

    private void exportData() {
        System.out.println("Exporting data...");
    }

    /**
     * Normalize payment method từ các dạng khác nhau sang tiếng Việt
     * - "transfer", "bank", "chuyển khoản" → "Chuyển khoản"
     * - "card", "wallet", "e-wallet", "thẻ", "ví điện tử" → "Ví điện tử"
     * - "cash", "tiền mặt" → "Tiền mặt"
     */
    private String normalizePaymentMethod(String method) {
        if (method == null || method.isBlank()) {
            return "Tiền mặt";
        }

        String normalized = method.toLowerCase().trim();

        // Chuyển khoản
        if (normalized.contains("transfer") || normalized.contains("bank")
                || normalized.equals("chuyển khoản")) {
            return "Chuyển khoản";
        }

        // Ví điện tử (Card, Wallet, E-wallet)
        if (normalized.contains("card") || normalized.contains("wallet")
                || normalized.contains("e-wallet") || normalized.equals("thẻ")
                || normalized.equals("ví điện tử")) {
            return "Ví điện tử";
        }

        // Tiền mặt
        if (normalized.contains("cash") || normalized.equals("tiền mặt")) {
            return "Tiền mặt";
        }

        // Mặc định trả về như cũ nếu không khớp
        return method;
    }

    // ==================== MENU TAB ====================
    private VBox buildMenuTab() {
        VBox menuTab = new VBox(15);
        menuTab.setPadding(new Insets(15));
        Label title = new Label("Quản lý Menu");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.4);
        VBox categoryPanel = buildCategoryPanel();
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

        // --- Hàng nút chức năng ---
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        Button addEmpBtn = createPrimaryButton("➕ Thêm");
        Button editEmpBtn = createGhostButton("✏️ Sửa");
        Button delEmpBtn = createGhostButton("🗑️ Xóa");
        Button lockBtn = createGhostButton("🔒 Khóa/Mở");
        Button roleBtn = createGhostButton("⚖️ Phân quyền");
        Button faceBtn = createGhostButton("👤 FaceID");
        Button timeBtn = createGhostButton("⏱️ Hoạt đông");

        addEmpBtn.setOnAction(e -> showAddEmployeeDialog());
        editEmpBtn.setOnAction(e -> showEditEmployeeDialog());
        delEmpBtn.setOnAction(e -> deleteEmployee());
        lockBtn.setOnAction(e -> toggleEmployeeLock());
        roleBtn.setOnAction(e -> showChangeRoleDialog());
        faceBtn.setOnAction(e -> showFaceIDOptions());
        timeBtn.setOnAction(e -> showWorkingHoursDialog());

        btnBox.getChildren().addAll(addEmpBtn, editEmpBtn, delEmpBtn, lockBtn, roleBtn, faceBtn, timeBtn);

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
        Button addInvBtn = createPrimaryButton("➕ Thêm nguyên liệu");
        Button importBtn = createPrimaryButton("📥 Nhập kho");
        Button deleteInvBtn = createGhostButton("🗑️ Xóa");
        Button refreshInvBtn = createGhostButton("🔄 Làm mới");
        addInvBtn.setOnAction(e -> showAddInventoryDialog());
        importBtn.setOnAction(e -> showImportDialog());
        deleteInvBtn.setOnAction(e -> deleteInventory());
        refreshInvBtn.setOnAction(e -> {
            loadInventory();
            loadLowStockAlerts(alertLabel);
        });
        btnBox.getChildren().addAll(addInvBtn, importBtn, deleteInvBtn, refreshInvBtn);
        inventoryTab.getChildren().addAll(title, alertPanel, inventoryTable, btnBox);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
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
        couponTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        TableColumn<CouponModel, String> cCodeCol = new TableColumn<>("Mã");
        cCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        cCodeCol.setPrefWidth(100);
        TableColumn<CouponModel, String> cTypeCol = new TableColumn<>("Loại");
        cTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        cTypeCol.setPrefWidth(100);
        TableColumn<CouponModel, Double> cValueCol = new TableColumn<>("Giá trị");
        cValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        cValueCol.setPrefWidth(100);
        TableColumn<CouponModel, String> cUsageCol = new TableColumn<>("Đã dùng/Limit");
        cUsageCol.setCellValueFactory(new PropertyValueFactory<>("usageDisplay"));
        cUsageCol.setPrefWidth(120);
        couponTable.getColumns().addAll(cCodeCol, cTypeCol, cValueCol, cUsageCol);
        HBox btnBox = new HBox(10);
        Button addCouponBtn = createPrimaryButton("➕ Tạo mã");
        Button delCouponBtn = createGhostButton("🗑️ Xóa");
        addCouponBtn.setOnAction(e -> showAddCouponDialog());
        delCouponBtn.setOnAction(e -> deleteCoupon());
        btnBox.getChildren().addAll(addCouponBtn, delCouponBtn);
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
        HBox selectorBox = new HBox(10);
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll("Doanh thu theo ngày", "Doanh thu theo tuần", "Doanh thu theo tháng",
                "Doanh thu theo ca", "Món bán chạy", "Số lượng khách", "Chi phí");
        reportTypeCombo.setValue("Doanh thu theo ngày");
        reportTypeCombo.setPrefWidth(200);
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> monthCombo = new ComboBox<>();
        monthCombo.getItems().addAll("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7",
                "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12");
        monthCombo.setValue("Tháng " + LocalDate.now().getMonthValue());
        ComboBox<Integer> yearCombo = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear; i++)
            yearCombo.getItems().add(i);
        yearCombo.setValue(currentYear);
        Button loadBtn = createPrimaryButton("📊 Tải báo cáo");
        loadBtn.setOnAction(e -> loadReport(reportTypeCombo.getValue(), datePicker.getValue(),
                monthCombo.getSelectionModel().getSelectedIndex() + 1, yearCombo.getValue()));
        selectorBox.getChildren().addAll(new Label("Loại báo cáo:"), reportTypeCombo, new Label("Ngày:"), datePicker,
                new Label("Tháng:"), monthCombo, new Label("Năm:"), yearCombo, loadBtn);
        selectorBox.setAlignment(Pos.CENTER_LEFT);

        // Thay TextArea thành TableView
        ObservableList<ReportItem> reportData = FXCollections.observableArrayList();
        TableView<ReportItem> reportTable = new TableView<>(reportData);
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportTable.setPlaceholder(new Label("Chọn loại báo cáo và nhấn 'Tải báo cáo'"));
        reportTable.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;");

        TableColumn<ReportItem, String> labelCol = new TableColumn<>("Chỉ tiêu");
        labelCol.setCellValueFactory(new PropertyValueFactory<>("label"));
        labelCol.setPrefWidth(300);

        TableColumn<ReportItem, String> valueCol = new TableColumn<>("Giá trị");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setStyle("-fx-alignment:CENTER_RIGHT;");

        reportTable.getColumns().addAll(labelCol, valueCol);

        reportTab.getChildren().addAll(title, selectorBox, reportTable);
        VBox.setVgrow(reportTable, Priority.ALWAYS);

        // Lưu reference để dùng trong loadReport()
        this.reportTable = reportTable;

        return reportTab;
    }

    // ==================== DIALOG METHODS (stub implementations)
    // ====================

    private ObservableList<ReportItem> parseReportData(String body) {
        ObservableList<ReportItem> items = FXCollections.observableArrayList();

        try {
            if (body == null || body.isEmpty()) {
                items.add(new ReportItem("Kết quả", "Không có dữ liệu"));
                return items;
            }

            // Kiểm tra success
            if (body.contains("\"success\":false")) {
                String error = extractJsonValue(body, "message");
                items.add(new ReportItem("Lỗi", error.isEmpty() ? "Không tải được báo cáo" : error));
                return items;
            }

            // Thử extract data array trước
            String[] dataItems = extractDataArrayObjects(body);

            // Nếu không có data array, parse trực tiếp từ body
            if (dataItems.length == 0) {
                // Parse các field chung như total_revenue, order_count, customer_count, v.v...
                String totalRevenue = extractJsonValue(body, "total_revenue");
                String orderCount = extractJsonValue(body, "order_count");
                String customerCount = extractJsonValue(body, "customer_count");
                String date = extractJsonValue(body, "date");
                String salaryExpenses = extractJsonValue(body, "salary_expenses");
                String profit = extractJsonValue(body, "profit");
                String profitMargin = extractJsonValue(body, "profit_margin");
                String employeeCount = extractJsonValue(body, "employee_count");
                String totalCustomers = extractJsonValue(body, "total_customers");
                String totalOrders = extractJsonValue(body, "total_orders");

                // Thêm date nếu có
                if (!date.isEmpty()) {
                    items.add(new ReportItem("Ngày", date));
                }

                // Thêm các metric chính
                if (!totalRevenue.isEmpty()) {
                    try {
                        double val = Double.parseDouble(totalRevenue);
                        items.add(new ReportItem("Tổng doanh thu", currency.format(val)));
                    } catch (Exception e) {
                        items.add(new ReportItem("Tổng doanh thu", totalRevenue));
                    }
                }

                if (!orderCount.isEmpty()) {
                    items.add(new ReportItem("Số đơn", orderCount + " đơn"));
                }

                if (!customerCount.isEmpty()) {
                    items.add(new ReportItem("Số bàn", customerCount));
                }

                if (!totalCustomers.isEmpty()) {
                    items.add(new ReportItem("Tổng khách", totalCustomers));
                }

                if (!totalOrders.isEmpty()) {
                    items.add(new ReportItem("Tổng đơn hàng", totalOrders));
                }

                if (!salaryExpenses.isEmpty()) {
                    try {
                        double val = Double.parseDouble(salaryExpenses);
                        items.add(new ReportItem("Chi phí lương", currency.format(val)));
                    } catch (Exception e) {
                        items.add(new ReportItem("Chi phí lương", salaryExpenses));
                    }
                }

                if (!employeeCount.isEmpty()) {
                    items.add(new ReportItem("Số nhân viên", employeeCount));
                }

                if (!profit.isEmpty()) {
                    try {
                        double val = Double.parseDouble(profit);
                        items.add(new ReportItem("Lợi nhuận", currency.format(val)));
                    } catch (Exception e) {
                        items.add(new ReportItem("Lợi nhuận", profit));
                    }
                }

                if (!profitMargin.isEmpty()) {
                    items.add(new ReportItem("Tỷ lệ lợi nhuận", profitMargin + "%"));
                }

                if (items.isEmpty()) {
                    items.add(new ReportItem("Kết quả", "Không có dữ liệu"));
                }

                return items;
            }

            // Parse các item trong data array (gộp mỗi bản ghi thành 1 dòng)
            double aggRevenue = 0.0;
            double aggTotal = 0.0;
            int aggOrders = 0;
            int aggSold = 0;
            for (String raw : dataItems) {
                String obj = normalizeJsonObject(raw);

                // Lấy các field phổ biến
                String date = extractJsonValue(obj, "date");
                String day = extractJsonValue(obj, "day");
                String shift = extractJsonValue(obj, "shift");
                String shiftName = extractJsonValue(obj, "shift_name");
                String product = extractJsonValue(obj, "product_name");
                String revenue = extractJsonValue(obj, "revenue");
                String total = extractJsonValue(obj, "total");
                String totalRevenue = extractJsonValue(obj, "total_revenue");
                String count = extractJsonValue(obj, "count");
                String orderCount = extractJsonValue(obj, "order_count");
                String soldQty = extractJsonValue(obj, "sold_quantity");
                String totalQty = extractJsonValue(obj, "total_qty");
                String dailyCustomers = extractJsonValue(obj, "daily_customers");
                String expense = extractJsonValue(obj, "expense");

                // Xây dựng nhãn (label): ưu tiên tên sản phẩm; nếu không có thì dùng ngày/ca
                String label;
                if (!product.isEmpty()) {
                    label = product;
                } else {
                    StringBuilder l = new StringBuilder();
                    if (!day.isEmpty())
                        l.append(day);
                    else if (!date.isEmpty())
                        l.append(date);
                    if (!shiftName.isEmpty()) {
                        if (l.length() > 0)
                            l.append(" · ");
                        l.append("Ca ").append(shiftName);
                    } else if (!shift.isEmpty() && shift.matches("\\d+")) {
                        if (l.length() > 0)
                            l.append(" · ");
                        l.append(getShiftLabel(Integer.parseInt(shift)));
                    }
                    label = l.length() == 0 ? "Chỉ tiêu" : l.toString();
                }

                // Xây dựng giá trị (value): gộp các metric vào 1 dòng
                StringBuilder value = new StringBuilder();
                // Doanh thu
                String rev = !revenue.isEmpty() ? revenue : totalRevenue;
                if (rev != null && !rev.isEmpty()) {
                    try {
                        double v = Double.parseDouble(rev);
                        appendMetric(value, "Doanh thu", currency.format(v));
                        aggRevenue += v;
                    } catch (Exception ex) {
                        appendMetric(value, "Doanh thu", rev);
                    }
                }
                // Tổng (có thể là số tiền hoặc số lượng tùy API)
                if (!total.isEmpty()) {
                    try {
                        double v = Double.parseDouble(total);
                        appendMetric(value, "Tổng", currency.format(v));
                        aggTotal += v;
                    } catch (Exception ex) {
                        appendMetric(value, "Tổng", total);
                    }
                }
                if (!soldQty.isEmpty()) {
                    appendMetric(value, "Đã bán", soldQty + " đơn");
                    try {
                        aggSold += Integer.parseInt(soldQty.replace("\"", ""));
                    } catch (Exception ignore) {
                    }
                }
                if (!orderCount.isEmpty()) {
                    appendMetric(value, "Số đơn", orderCount);
                    try {
                        aggOrders += Integer.parseInt(orderCount.replace("\"", ""));
                    } catch (Exception ignore) {
                    }
                }
                if (!count.isEmpty()) {
                    appendMetric(value, "Số lượng", count);
                }
                if (!totalQty.isEmpty()) {
                    appendMetric(value, "Tổng số", totalQty);
                }
                if (!dailyCustomers.isEmpty()) {
                    appendMetric(value, "Khách hàng", dailyCustomers);
                }
                if (!expense.isEmpty()) {
                    try {
                        double v = Double.parseDouble(expense);
                        appendMetric(value, "Chi phí", currency.format(v));
                    } catch (Exception ex) {
                        appendMetric(value, "Chi phí", expense);
                    }
                }

                if (value.length() == 0)
                    value.append("—");
                items.add(new ReportItem(label, value.toString()));
            }

            // Thêm dòng tổng hợp cuối cùng nếu có dữ liệu
            if (aggRevenue > 0 || aggTotal > 0 || aggOrders > 0 || aggSold > 0) {
                StringBuilder totalVal = new StringBuilder();
                if (aggRevenue > 0)
                    appendMetric(totalVal, "Doanh thu", currency.format(aggRevenue));
                if (aggTotal > 0)
                    appendMetric(totalVal, "Tổng", currency.format(aggTotal));
                if (aggOrders > 0)
                    appendMetric(totalVal, "Số đơn", String.valueOf(aggOrders));
                if (aggSold > 0)
                    appendMetric(totalVal, "Đã bán", aggSold + " đơn");
                items.add(new ReportItem("Tổng kỳ", totalVal.toString()));
            }

            if (items.isEmpty()) {
                items.add(new ReportItem("Kết quả", "Không có dữ liệu"));
            }
        } catch (Exception e) {
            items.add(new ReportItem("Lỗi", "Không xử lý được dữ liệu: " + e.getMessage()));
        }

        return items;
    }

    private void appendMetric(StringBuilder sb, String key, String val) {
        if (val == null || val.isEmpty())
            return;
        if (sb.length() > 0)
            sb.append(" • ");
        sb.append(key).append(": ").append(val);
    }

    private String getShiftLabel(int hour) {
        if (hour >= 6 && hour <= 11)
            return "Sáng (6-11)";
        if (hour >= 12 && hour <= 17)
            return "Chiều (12-17)";
        if (hour >= 18 && hour <= 23)
            return "Tối (18-23)";
        return "Đêm (0-5)";
    }

    private void showAddCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm danh mục");
        dialog.setHeaderText("Nhập tên danh mục mới");
        dialog.setContentText("Tên:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String json = String.format("{\"category_name\": \"%s\"}", name);
                sendPostRequest(ADD_CATEGORY_URL, json, "Thêm danh mục", this::loadCategories);
            }
        });
    }

    private void showEditCategoryDialog() {
        CategoryModel selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn danh mục", "Vui lòng chọn danh mục để sửa");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Sửa danh mục");
        dialog.setHeaderText("Nhập tên mới cho: " + selected.getName());
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String json = String.format("{\"id\": %d, \"category_name\": \"%s\"}", selected.getId(), name);
                sendPostRequest(UPDATE_CATEGORY_URL, json, "Sửa danh mục", this::loadCategories);
            }
        });
    }

    private void deleteCategory() {
        CategoryModel selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn danh mục", "Vui lòng chọn danh mục để xóa");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa danh mục");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa: " + selected.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String json = String.format("{\"id\": %d}", selected.getId());
                sendPostRequest(DELETE_CATEGORY_URL, json, "Xóa danh mục", this::loadCategories);
            }
        });
    }

    // --- Product CRUD ---
    private void showAddProductDialog() {
        Dialog<Pair<String, Pair<Integer, Double>>> dialog = new Dialog<>();
        dialog.setTitle("Thêm sản phẩm");
        ButtonType loginButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField nameField = new TextField();
        ComboBox<CategoryModel> categoryCombo = new ComboBox<>(categories);

        categoryCombo.setCellFactory(lv -> new ListCell<CategoryModel>() {
            @Override
            protected void updateItem(CategoryModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        categoryCombo.setButtonCell(new ListCell<CategoryModel>() {
            @Override
            protected void updateItem(CategoryModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        TextField priceField = new TextField();

        grid.add(new Label("Tên món:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Danh mục:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Giá:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    String name = nameField.getText();
                    int catId = categoryCombo.getValue().getId();
                    double price = Double.parseDouble(priceField.getText());
                    return new Pair<>(name, new Pair<>(catId, price));
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String json = String.format(
                    "{\"product_name\": \"%s\", \"category_id\": %d, \"price\": %.0f, \"product_image\":\"\", \"is_hot\":0}",
                    result.getKey(), result.getValue().getKey(), result.getValue().getValue());
            sendPostRequest(ADD_PRODUCT_URL, json, "Thêm sản phẩm", this::loadProducts);
        });
    }

    private void showEditProductDialog() {
        ProductModel selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Chọn sản phẩm để sửa");
            return;
        }

        Dialog<Pair<String, Double>> dialog = new Dialog<>();
        dialog.setTitle("Sửa sản phẩm");
        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField nameField = new TextField(selected.getName());
        TextField priceField = new TextField(String.valueOf(selected.getPrice()));

        grid.add(new Label("Tên món:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Giá:"), 0, 1);
        grid.add(priceField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    return new Pair<>(name, price);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result != null) {
                String json = String.format(
                        "{\"id\": %d, \"product_name\": \"%s\", \"price\": %.0f}",
                        selected.getId(),
                        result.getKey().replace("\"", "\\\""),
                        result.getValue());
                sendPostRequest(UPDATE_PRODUCT_URL, json, "Cập nhật sản phẩm", this::loadProducts);
            }
        });
    }

    private void deleteProduct() {
        ProductModel selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn sản phẩm để xóa");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa sản phẩm");
        alert.setHeaderText("Xóa " + selected.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String json = String.format("{\"id\": %d}", selected.getId());
                sendPostRequest(DELETE_PRODUCT_URL, json, "Xóa sản phẩm", this::loadProducts);
            }
        });
    }

    private void toggleProductAvailable() {
        ProductModel selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn sản phẩm");
            return;
        }
        int newStatus = selected.getAvailable() ? 0 : 1;
        String json = String.format("{\"id\": %d, \"available\": %d}", selected.getId(), newStatus);
        sendPostRequest(TOGGLE_PRODUCT_AVAILABLE_URL, json, "Cập nhật trạng thái", this::loadProducts);
    }

    private void showUpdatePriceDialog() {
        ProductModel selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn sản phẩm");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getPrice()));
        dialog.setTitle("Đổi giá");
        dialog.setHeaderText("Nhập giá mới cho: " + selected.getName());
        dialog.showAndWait().ifPresent(priceStr -> {
            try {
                double price = Double.parseDouble(priceStr);
                String json = String.format("{\"id\": %d, \"price\": %.0f}", selected.getId(), price);
                sendPostRequest(UPDATE_PRICE_URL, json, "Đổi giá", this::loadProducts);
            } catch (NumberFormatException e) {
                showAlert("Lỗi", "Giá phải là số");
            }
        });
    }

    // --- Employee CRUD ---
    private void showAddEmployeeDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Thêm nhân viên");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        ComboBox<Integer> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(1, 2); // 1: Admin, 2: Employee
        roleCombo.setValue(2);
        grid.add(new Label("Tên:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("User:"), 0, 2);
        grid.add(userField, 1, 2);
        grid.add(new Label("Pass:"), 0, 3);
        grid.add(passField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleCombo, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return String.format(
                        "{\"employee_name\":\"%s\",\"phone\":\"%s\",\"username\":\"%s\", \"employee_password\":\"%s\", \"role_id\":%d}",
                        nameField.getText(), phoneField.getText(), userField.getText(), passField.getText(),
                        roleCombo.getValue());
            }
            return null;
        });
        dialog.showAndWait()
                .ifPresent(json -> sendPostRequest(ADD_EMPLOYEE_URL, json, "Thêm nhân viên", this::loadEmployees));
    }

    private void loadEmployees() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_EMPLOYEES_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String[] items = extractDataArrayObjects(response.body());
                    var loaded = FXCollections.<EmployeeModel>observableArrayList();
                    for (String raw : items) {
                        String obj = normalizeJsonObject(raw);
                        String idStr = extractJsonValue(obj, "id");
                        String name = extractJsonValue(obj, "employee_name");
                        String username = extractJsonValue(obj, "username");
                        String roleId = extractJsonValue(obj, "role_id");
                        String active = extractJsonValue(obj, "active");

                        if (username != null && !username.isBlank()) {
                            loaded.add(new EmployeeModel(parseIntSafe(idStr), name, username, parseIntSafe(roleId),
                                    parseBooleanInt(active, true)));
                        }
                    }
                    Platform.runLater(() -> {
                        employees.setAll(loaded);
                        if (employeeTable != null)
                            employeeTable.refresh();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showEditEmployeeDialog() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên để sửa thông tin");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin nhân viên");
        dialog.setHeaderText("Cập nhật cho: " + selected.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(selected.getName());
        PasswordField passField = new PasswordField();
        passField.setPromptText("Để trống nếu không đổi");

        grid.add(new Label("Tên hiển thị:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(passField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                // Chỉ gửi password nếu người dùng có nhập
                String passJson = passField.getText().isEmpty() ? ""
                        : String.format(", \"employee_password\":\"%s\"", passField.getText());

                return String.format("{\"id\":%d, \"employee_name\":\"%s\"%s}",
                        selected.getId(), nameField.getText(), passJson);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(
                json -> sendPostRequest(UPDATE_EMPLOYEE_URL, json, "Cập nhật nhân viên", this::loadEmployees));
    }

    private void deleteEmployee() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên để xóa");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa nhân viên: " + selected.getName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String json = String.format("{\"id\": %d}", selected.getId());
                sendPostRequest(DELETE_EMPLOYEE_URL, json, "Xóa nhân viên", this::loadEmployees);
            }
        });
    }

    private void toggleEmployeeLock() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên");
            return;
        }
        String url = selected.getActive() ? LOCK_ACCOUNT_URL : UNLOCK_ACCOUNT_URL;
        // Sửa "id" thành "employee_id" để khớp với API PHP
        String json = String.format("{\"employee_id\": %d}", selected.getId());
        sendPostRequest(url, json, "Khóa/Mở khóa", this::loadEmployees);
    }

    private void showChangeRoleDialog() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên để đổi vai trò");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Employee", "Admin", "Employee");
        dialog.setTitle("Đổi vai trò");
        dialog.setHeaderText("Chọn vai trò mới cho: " + selected.getName());
        dialog.setContentText("Vai trò:");

        dialog.showAndWait().ifPresent(role -> {
            int roleId = role.equals("Admin") ? 1 : 2;
            String json = String.format("{\"employee_id\": %d, \"role_id\": %d}", selected.getId(), roleId);
            sendPostRequest(UPDATE_ROLE_URL, json, "Cập nhật vai trò", this::loadEmployees);
        });
    }

    private void showWorkingHoursDialog() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên");
            return;
        }

        new Thread(() -> {
            try {
                int month = LocalDate.now().getMonthValue();
                int year = LocalDate.now().getYear();
                String url = GET_WORKING_HOURS_URL + "?employee_id=" + selected.getId() +
                        "&month=" + month + "&year=" + year;

                HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();

                String totalHours = extractJsonValue(body, "total_hours");
                String[] records = extractDataArrayObjects(body);

                StringBuilder display = new StringBuilder();
                display.append("Giờ làm việc của ").append(selected.getName())
                        .append(" (Tháng ").append(month).append("/").append(year).append(")\n\n");
                display.append("Tổng giờ: ").append(totalHours).append(" giờ\n\n");
                display.append("Chi tiết:\n");
                display.append("─────────────────────────────\n");

                for (String raw : records) {
                    String obj = normalizeJsonObject(raw);
                    String checkin = extractJsonValue(obj, "checkin_time");
                    String checkout = extractJsonValue(obj, "checkout_time");
                    display.append("Vào: ").append(checkin).append("\n");
                    display.append("Ra: ").append(checkout.isEmpty() ? "Chưa checkout" : checkout).append("\n");
                    display.append("─────────────────────────────\n");
                }

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Giờ làm việc");
                    alert.setHeaderText(null);
                    TextArea textArea = new TextArea(display.toString());
                    textArea.setEditable(false);
                    textArea.setPrefRowCount(15);
                    alert.getDialogPane().setContent(textArea);
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Lỗi", "Không thể tải dữ liệu giờ làm: " + e.getMessage()));
            }
        }).start();
    }

    private void showFaceIDOptions() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quản lý FaceID");
        alert.setHeaderText("FaceID: " + selected.getName());
        alert.setContentText("Bạn muốn thực hiện hành động gì?");

        ButtonType btnAdd = new ButtonType("Thêm mới");
        ButtonType btnDel = new ButtonType("Xóa cũ");
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnAdd, btnDel, btnCancel);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnAdd) {
                showAddFaceDialog();
            } else if (type == btnDel) {
                showDeleteFaceDialog();
            }
        });
    }

    private void showAddFaceDialog() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên để thêm Face ID");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thêm Face ID");
        alert.setHeaderText("Hướng dẫn thêm Face ID cho: " + selected.getName());
        alert.setContentText("Bước 1: Nhân viên đứng trước camera\n" +
                "Bước 2: Hệ thống Python sẽ chụp và tạo embedding\n" +
                "Bước 3: Embedding được lưu vào database\n\n" +
                "Nhấn OK để bắt đầu quá trình nhận diện...");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Gọi Python script để capture và tạo embedding
                captureFaceAndSave(selected.getId(), selected.getName());
            }
        });
    }

    private void captureFaceAndSave(int employeeId, String employeeName) {
        new Thread(() -> {
            try {
                // Gọi Python script để capture face
                ProcessBuilder pb = new ProcessBuilder("python", "capture_face.py",
                        String.valueOf(employeeId), employeeName);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));

                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    System.out.println("[Python]: " + line);
                }

                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    // Nếu Python script thành công, nó sẽ trả về embedding dạng base64
                    String embedding = output.toString().trim();

                    // Gửi embedding lên backend
                    String json = String.format(
                            "{\"employee_id\":%d,\"embedding\":\"%s\"}",
                            employeeId, embedding);

                    HttpRequest request = HttpRequest.newBuilder(URI.create(ADD_FACEID_URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> response = httpClient.send(request,
                            HttpResponse.BodyHandlers.ofString());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            showAlert("Thành công",
                                    "Đã lưu Face ID cho " + employeeName);
                        } else {
                            showAlert("Lỗi", "Không thể lưu Face ID: " +
                                    response.body());
                        }
                    });
                } else {
                    Platform.runLater(() -> showAlert("Lỗi",
                            "Không thể capture khuôn mặt. Vui lòng thử lại."));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Lỗi",
                        "Lỗi hệ thống: " + e.getMessage()));
            }
        }).start();
    }

    private void showDeleteFaceDialog() {
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn nhân viên");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa Face ID");
        confirm.setHeaderText("Xóa dữ liệu khuôn mặt của: " + selected.getName() + "?");
        confirm.setContentText("Hành động này không thể hoàn tác!");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String json = String.format("{\"employee_id\": %d}", selected.getId());
                sendPostRequest(DELETE_FACEID_URL, json, "Xóa Face ID", () -> {
                    Platform.runLater(() -> showAlert("Thành công",
                            "Đã xóa Face ID của " + selected.getName()));
                });
            }
        });
    }

    // --- Inventory ---
    private void showImportDialog() {
        InventoryModel selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Chọn nguyên liệu để nhập");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Nhập kho");
        dialog.setHeaderText("Nhập số lượng thêm cho: " + selected.getName());
        dialog.showAndWait().ifPresent(qtyStr -> {
            try {
                double qty = Double.parseDouble(qtyStr);
                String json = String.format("{\"inventory_id\": %d, \"quantity\": %.2f, \"price\": 0}",
                        selected.getId(), qty);
                sendPostRequest(IMPORT_INVENTORY_URL, json, "Nhập kho", this::loadInventory);
            } catch (Exception e) {
                showAlert("Lỗi", "Số lượng không hợp lệ");
            }
        });
    }

    private void showExportDialog() {
    }

    // --- Coupons ---
    private void showAddCouponDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Tạo mã giảm giá");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField codeField = new TextField();
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("percent", "amount");
        typeCombo.setValue("amount");
        TextField valueField = new TextField();
        TextField limitField = new TextField("100");

        grid.add(new Label("Mã:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Loại:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Giá trị:"), 0, 2);
        grid.add(valueField, 1, 2);
        grid.add(new Label("Giới hạn:"), 0, 3);
        grid.add(limitField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return String.format(
                        "{\"code\":\"%s\", \"coupon_type\":\"%s\", \"coupon_value\":%s, \"usage_limit\":%s}",
                        codeField.getText(), typeCombo.getValue(), valueField.getText(), limitField.getText());
            }
            return null;
        });
        dialog.showAndWait().ifPresent(json -> sendPostRequest(CREATE_COUPON_URL, json, "Tạo mã", this::loadCoupons));
    }

    private void deleteCoupon() {
        CouponModel selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        String json = String.format(
                "{\"coupon_id\": %d}",
                selected.getId());

        sendPostRequest(DELETE_COUPON_URL, json, "Xóa mã", this::loadCoupons);
    }

    private void showCouponUsageDialog() {
    }

    // ==================== GENERIC API METHODS ====================

    private void sendPostRequest(String url, String jsonBody, String actionName, Runnable onSuccess) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println(actionName + " Status: " + response.statusCode());
                System.out.println("Response: " + response.body());

                if (response.statusCode() == 200) {
                    Platform.runLater(() -> {
                        showAlert("Thành công", actionName + " thành công!");
                        if (onSuccess != null)
                            onSuccess.run();
                    });
                } else {
                    Platform.runLater(() -> showAlert("Lỗi", "Thất bại: " + response.statusCode()));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Lỗi kết nối", e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== LOAD DATA METHODS (EXISTING) ====================

    private void loadCategories() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_CATEGORIES_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                String[] items = extractDataArrayObjects(body);
                var loaded = FXCollections.<CategoryModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "id"), extractJsonValue(obj, "category_id"));
                    String name = firstNonBlank(extractJsonValue(obj, "name"), extractJsonValue(obj, "category_name"));
                    if (name != null && !name.isBlank())
                        loaded.add(new CategoryModel(parseIntSafe(idStr), name));
                }
                Platform.runLater(() -> {
                    categories.setAll(loaded);
                    if (categoryTable != null)
                        categoryTable.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
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
                var loaded = FXCollections.<ProductModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "product_id"), extractJsonValue(obj, "id"));
                    String name = firstNonBlank(extractJsonValue(obj, "product_name"), extractJsonValue(obj, "name"));
                    String categoryIdStr = firstNonBlank(extractJsonValue(obj, "category_id"));
                    String categoryName = firstNonBlank(extractJsonValue(obj, "category_name"));
                    String priceStr = firstNonBlank(extractJsonValue(obj, "price"),
                            extractJsonValue(obj, "unit_price"));
                    String availableStr = firstNonBlank(extractJsonValue(obj, "available"),
                            extractJsonValue(obj, "is_available"));
                    String hotStr = firstNonBlank(extractJsonValue(obj, "is_hot"));

                    if (name != null && !name.isBlank()) {
                        loaded.add(
                                new ProductModel(parseIntSafe(idStr), name, parseIntSafe(categoryIdStr), categoryName,
                                        parseDoubleSafe(priceStr), parseBooleanInt(availableStr, true),
                                        parseBooleanInt(hotStr, false)));
                    }
                }
                Platform.runLater(() -> {
                    products.setAll(loaded);
                    if (productTable != null)
                        productTable.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadInventory() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_INVENTORY_LIST_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String[] items = extractDataArrayObjects(response.body());
                var loaded = FXCollections.<InventoryModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "id"), extractJsonValue(obj, "inventory_id"));
                    String name = firstNonBlank(extractJsonValue(obj, "name"), extractJsonValue(obj, "inventory_name"));
                    String qtyStr = firstNonBlank(extractJsonValue(obj, "quantity"), extractJsonValue(obj, "qty"));
                    String unit = firstNonBlank(extractJsonValue(obj, "unit"));
                    String status = firstNonBlank(extractJsonValue(obj, "status"),
                            extractJsonValue(obj, "inventory_status"));
                    if (name != null && !name.isBlank())
                        loaded.add(
                                new InventoryModel(parseIntSafe(idStr), name, parseDoubleSafe(qtyStr), unit, status));
                }
                Platform.runLater(() -> {
                    inventoryItems.setAll(loaded);
                    if (inventoryTable != null)
                        inventoryTable.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteInventory() {
        InventoryModel selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Cảnh báo", "Vui lòng chọn thành phần để xóa");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Xóa thành phần kho");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa '" + selected.getName() + "'?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String jsonBody = String.format("{\"inventory_id\":%d}", selected.getId());
            sendPostRequest(DELETE_INVENTORY_URL, jsonBody, "Xóa thành phần", this::loadInventory);
        }
    }

    private void showAddInventoryDialog() {
        Dialog<Pair<String, Pair<Double, String>>> dialog = new Dialog<>();
        dialog.setTitle("Thêm nguyên liệu mới");
        dialog.setHeaderText("Nhập thông tin nguyên liệu");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Tên nguyên liệu");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Số lượng");
        TextField unitField = new TextField();
        unitField.setPromptText("Đơn vị (ví dụ: kg, lít, cái)");

        grid.add(new Label("Tên nguyên liệu:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Số lượng:"), 0, 1);
        grid.add(qtyField, 1, 1);
        grid.add(new Label("Đơn vị:"), 0, 2);
        grid.add(unitField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    double qty = qtyField.getText() != null && !qtyField.getText().isBlank()
                            ? Double.parseDouble(qtyField.getText())
                            : 0;
                    return new Pair<>(nameField.getText(), new Pair<>(qty, unitField.getText()));
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "Số lượng phải là số hợp lệ");
                    return null;
                }
            }
            return null;
        });

        Optional<Pair<String, Pair<Double, String>>> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            Pair<String, Pair<Double, String>> data = result.get();
            String name = data.getKey();
            double quantity = data.getValue().getKey();
            String unit = data.getValue().getValue();

            if (name.isBlank() || unit.isBlank()) {
                showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin");
                return;
            }

            String jsonBody = String.format(Locale.US,
                    "{\"inventory_name\":\"%s\",\"quantity\":%.0f,\"unit\":\"%s\"}",
                    name.replace("\"", "\\\""), quantity, unit.replace("\"", "\\\""));

            sendPostRequest("http://localhost/coffee-shop-app/backend/api/admin/inventory/add_inventory.php",
                    jsonBody, "Thêm nguyên liệu", this::loadInventory);
        }
    }

    private void loadCoupons() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_COUPONS_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String[] items = extractDataArrayObjects(response.body());
                var loaded = FXCollections.<CouponModel>observableArrayList();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String idStr = firstNonBlank(extractJsonValue(obj, "id"), extractJsonValue(obj, "coupon_id"));
                    String code = firstNonBlank(extractJsonValue(obj, "code"));
                    String type = firstNonBlank(extractJsonValue(obj, "coupon_type"));
                    String valueStr = firstNonBlank(extractJsonValue(obj, "coupon_value"));
                    String usageCountStr = firstNonBlank(extractJsonValue(obj, "usage_count"));
                    String usageLimitStr = firstNonBlank(extractJsonValue(obj, "usage_limit"));
                    if (code != null && !code.isBlank())
                        loaded.add(new CouponModel(parseIntSafe(idStr), code, type, parseDoubleSafe(valueStr),
                                parseIntSafe(usageCountStr), parseIntSafe(usageLimitStr)));
                }
                Platform.runLater(() -> {
                    coupons.setAll(loaded);
                    if (couponTable != null)
                        couponTable.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadLowStockAlerts(Label alertLabel) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(GET_LOW_STOCK_ALERT_URL)).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String[] items = extractDataArrayObjects(response.body());
                StringBuilder sb = new StringBuilder();
                for (String raw : items) {
                    String obj = normalizeJsonObject(raw);
                    String name = firstNonBlank(extractJsonValue(obj, "name"), extractJsonValue(obj, "inventory_name"));
                    String qtyStr = firstNonBlank(extractJsonValue(obj, "quantity"), extractJsonValue(obj, "qty"));
                    if (name != null && !name.isBlank()) {
                        if (!sb.isEmpty())
                            sb.append(", ");
                        sb.append(name).append(" (").append(qtyStr).append(")");
                    }
                }
                String text = sb.isEmpty() ? "Không có cảnh báo" : "Sắp hết: " + sb.toString();
                Platform.runLater(() -> alertLabel.setText(text));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadReport(String type, LocalDate date, int month, int year) {
        new Thread(() -> {
            try {
                String url = "";
                if (type.contains("ngày"))
                    url = REVENUE_BY_DAY_URL + "?date=" + date.toString();
                else if (type.contains("tuần"))
                    url = REVENUE_BY_WEEK_URL + "?year=" + year + "&week=" + (date.getDayOfYear() / 7);
                else if (type.contains("tháng"))
                    url = REVENUE_BY_MONTH_URL + "?year=" + year + "&month=" + month;
                else if (type.contains("ca"))
                    url = REVENUE_BY_SHIFT_URL + "?date=" + date.toString();
                else if (type.contains("Món bán chạy"))
                    url = BEST_SELLING_PRODUCTS_URL + "?year=" + year + "&month=" + month;
                else if (type.contains("khách"))
                    url = CUSTOMER_COUNT_URL + "?year=" + year + "&month=" + month;
                else if (type.contains("Chi phí"))
                    url = EXPENSES_URL + "?year=" + year + "&month=" + month;

                if (!url.isEmpty()) {
                    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    ObservableList<ReportItem> items = parseReportData(response.body());
                    Platform.runLater(() -> {
                        if (reportTable != null) {
                            reportTable.setItems(items);
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (reportTable != null) {
                        reportTable.setItems(FXCollections.observableArrayList(
                                new ReportItem("Lỗi", e.getMessage())));
                    }
                });
            }
        }).start();
    }

    // ==================== PARSER HELPERS ====================
    private String[] extractDataArrayObjects(String body) {
        if (body == null || body.isBlank())
            return new String[0];
        int dataIdx = body.indexOf("\"data\"");
        if (dataIdx < 0)
            return new String[0];
        int arrayStart = body.indexOf('[', dataIdx);
        int arrayEnd = body.indexOf(']', arrayStart);
        if (arrayStart < 0 || arrayEnd < 0)
            return new String[0];
        String dataArray = body.substring(arrayStart + 1, arrayEnd);
        if (dataArray.isBlank())
            return new String[0];
        return dataArray.split("\\},\\s*\\{");
    }

    private String normalizeJsonObject(String raw) {
        String obj = raw.trim();
        if (!obj.startsWith("{"))
            obj = "{" + obj;
        if (!obj.endsWith("}"))
            obj = obj + "}";
        return obj;
    }

    private String firstNonBlank(String... values) {
        for (String v : values)
            if (v != null && !v.isBlank())
                return v;
        return "";
    }

    private int parseIntSafe(String raw) {
        try {
            return Integer.parseInt(raw.replace("\"", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String raw) {
        try {
            return Double.parseDouble(raw.replace("\"", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean parseBooleanInt(String raw, boolean def) {
        if (raw == null)
            return def;
        String v = raw.replace("\"", "").trim();
        if ("1".equals(v) || "true".equalsIgnoreCase(v))
            return true;
        if ("0".equals(v) || "false".equalsIgnoreCase(v))
            return false;
        return def;
    }

    private String extractReportDisplay(String body) {
        if (body == null || body.isEmpty()) {
            return "Không có dữ liệu báo cáo";
        }

        try {
            // Kiểm tra xem response có success=false không
            if (body.contains("\"success\":false")) {
                String errorMsg = extractJsonValue(body, "message");
                return "Lỗi: " + (errorMsg.isEmpty() ? "Không tải được báo cáo" : errorMsg);
            }

            // Extract data array từ JSON
            String[] items = extractDataArrayObjects(body);
            if (items.length == 0) {
                return "Không có dữ liệu trong khoảng thời gian này";
            }

            StringBuilder display = new StringBuilder();
            display.append("═══════════════════════════════════════════════════════\n");
            display.append("                        BÁO CÁO\n");
            display.append("═══════════════════════════════════════════════════════\n\n");

            // Parse từng item trong data array
            for (int i = 0; i < items.length; i++) {
                String item = normalizeJsonObject(items[i]);

                // Thử lấy các field phổ biến
                String date = extractJsonValue(item, "date");
                String day = extractJsonValue(item, "day");
                String month = extractJsonValue(item, "month");
                String week = extractJsonValue(item, "week");
                String shift = extractJsonValue(item, "shift");
                String product = extractJsonValue(item, "product_name");
                String revenue = extractJsonValue(item, "revenue");
                String total = extractJsonValue(item, "total");
                String count = extractJsonValue(item, "count");
                String expense = extractJsonValue(item, "expense");

                // Xây dựng dòng hiển thị tùy theo loại báo cáo
                StringBuilder line = new StringBuilder();
                if (!date.isEmpty()) {
                    line.append("Ngày: ").append(date);
                }
                if (!day.isEmpty()) {
                    line.append("Ngày: ").append(day);
                }
                if (!week.isEmpty()) {
                    if (line.length() > 0)
                        line.append(" | ");
                    line.append("Tuần: ").append(week);
                }
                if (!shift.isEmpty()) {
                    if (line.length() > 0)
                        line.append(" | ");
                    line.append("Ca: ").append(shift);
                }
                if (!product.isEmpty()) {
                    if (line.length() > 0)
                        line.append("\n");
                    line.append("Sản phẩm: ").append(product);
                }

                // Thêm giá trị chính
                if (!revenue.isEmpty()) {
                    if (line.length() > 0)
                        line.append("\n");
                    line.append("Doanh thu: ").append(currency.format(Double.parseDouble(revenue)));
                }
                if (!total.isEmpty()) {
                    if (line.length() > 0)
                        line.append("\n");
                    line.append("Tổng: ").append(currency.format(Double.parseDouble(total)));
                }
                if (!count.isEmpty()) {
                    if (line.length() > 0)
                        line.append("\n");
                    line.append("Số lượng: ").append(count);
                }
                if (!expense.isEmpty()) {
                    if (line.length() > 0)
                        line.append("\n");
                    line.append("Chi phí: ").append(currency.format(Double.parseDouble(expense)));
                }

                if (line.length() > 0) {
                    display.append(line).append("\n");
                    display.append("───────────────────────────────────────────────────────\n");
                }
            }

            return display.toString();
        } catch (Exception e) {
            return "Lỗi xử lý báo cáo: " + e.getMessage();
        }
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || json.isEmpty())
            return "";
        String pattern = "\"" + key + "\"";
        int start = json.indexOf(pattern);
        if (start < 0)
            return "";
        int valStart = json.indexOf(":", start) + 1;
        int valEnd = json.indexOf(",", valStart);
        if (valEnd < 0)
            valEnd = json.indexOf("}", valStart);
        if (valStart > 0 && valEnd > valStart) {
            return json.substring(valStart, valEnd).replace("\"", "").trim();
        }
        return "";
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

        @Override
        public String toString() {
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

        public String getCategoryName() {
            return categoryName;
        }

        public double getPrice() {
            return price;
        }

        public boolean getAvailable() {
            return available;
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

        public String getUsageDisplay() {
            return usageCount + "/" + usageLimit;
        }
    }

    public static class ReportItem {
        private final String label;
        private final String value;

        public ReportItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }

    public static class PaymentRecord {
        private final String time;
        private final String table;
        private final double amount;
        private final String method;

        public PaymentRecord(String time, String table, String method, double amount) {
            this.time = time;
            this.table = table;
            this.method = method;
            this.amount = amount;
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

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color:#F2C57C;-fx-background-radius:14;-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;-fx-cursor:hand;");
        return button;
    }

    private Button createGhostButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color:transparent;-fx-border-color:rgba(107,76,59,0.4);-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;-fx-cursor:hand;");
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void handleLogout(Stage currentStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        HttpRequest request = HttpRequest.newBuilder(URI.create(LOGOUT_URL))
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build();
                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception ex) {
                        System.err.println("Logout API failed: " + ex.getMessage());
                    }
                }).start();

                Platform.runLater(() -> {
                    try {
                        new LoginPage().start(new Stage());
                        currentStage.close();
                        System.out.println("Đã đăng xuất. (Cần implement chuyển hướng về trang Login)");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Lỗi", "Không thể mở màn hình đăng nhập: " + e.getMessage());
                    }
                });
            }
        });
    }
}