import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    // Danh sách menu và bàn sẽ được load từ API, không hard-code nữa
    private final ObservableList<MenuItemModel> menuItems = FXCollections.observableArrayList();

    private final ObservableList<CafeTable> allTables = FXCollections.observableArrayList();

    private final Map<String, ObservableList<OrderItem>> ordersByTable = new HashMap<>();
    private final Map<String, String> notesByTable = new HashMap<>();

    private ListView<CafeTable> tableListView;
    private TableView<OrderItem> orderTable;
    private TextArea noteArea;
    private ComboBox<String> areaCombo;
    private Label totalLabel;
    private ListView<MenuItemModel> menuListView;

    // table management UI refs
    private FlowPane mgmtFloorMap;
    private Label mgmtSelectedLabel;
    private Label mgmtStatusLabel;
    private Label mgmtReservationLabel;
    private ComboBox<String> mgmtStatusCombo;
    private TextField mgmtCustomerField;
    private TextField mgmtPhoneField;
    private ComboBox<String> mgmtSlotCombo;
    private TextArea mgmtNoteArea;
    private CafeTable mgmtSelectedTable;

    private final ObservableList<PaymentRecord> paymentRecords = FXCollections.observableArrayList();
    // URL API PHP (chỉnh lại cho đúng domain/path thật của bạn)
    private static final String ADD_ORDER_ITEM_URL = "http://localhost:8080/backend/api/employee/add-order-item.php";
    private static final String CREATE_ORDER_URL = "http://localhost:8080/backend/api/employee/create-order.php";
    private static final String DELETE_ORDER_URL = "http://localhost:8080/backend/api/employee/delete-order.php";
    private static final String GET_CATEGORIES_URL = "http://localhost:8080/backend/api/employee/get-categories.php";
    private static final String GET_INVENTORY_URL = "http://localhost:8080/backend/api/employee/get-inventory.php";
    private static final String GET_ORDER_DETAILS_URL = "http://localhost:8080/backend/api/employee/get-order.php";
    private static final String GET_PRODUCTS_URL = "http://localhost:8080/backend/api/employee/get-products.php";
    private static final String GET_TABLES_URL = "http://localhost:8080/backend/api/employee/get-tables.php";
    private static final String GET_UNPAID_ORDERS_URL = "http://localhost:8080/backend/api/employee/get-unpaid-orders.php";
    private static final String CHECKOUT_ORDER_URL = "http://localhost:8080/backend/api/employee/order-checkout.php";
    private static final String UPDATE_ORDER_ITEM_URL = "http://localhost:8080/backend/api/employee/update-order-item.php";
    private static final String UPDATE_INVENTORY_STATUS_URL = "http://localhost:8080/backend/api/employee/update-inventory-status.php";
    private static final String UPDATE_PRODUCT_AVAILABLE_URL = "http://localhost:8080/backend/api/employee/update-product-available.php";
    private static final String UPDATE_TABLE_STATUS_URL = "http://localhost:8080/backend/api/employee/update-table-status.php";
    private static final String LOGOUT_URL = "http://localhost:8080/backend/api/employee/logout.php";
    // id order hiện tại, được set sau khi gọi create-order
    private int currentOrderId = -1;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Trạng thái bàn đồng bộ đúng với backend: empty, serving, reserved
    private final Map<String, String> statusColor = new HashMap<>() {
        {
            put("empty", "#D8E2C8"); // Bàn trống
            put("serving", "#C08A64"); // Đang phục vụ / có khách
            put("reserved", "#4E3627"); // Đặt trước
        }
    };

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8, 24, 16, 24));
        root.setStyle("-fx-background-color:#F5F0E1;-fx-font-family:'Segoe UI',sans-serif;");

        // Header với nút đăng xuất
        VBox header = buildHeader();
        root.setTop(header);

        TabPane tabs = buildTabs();
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1400, 820);
        primaryStage.setTitle("Coffee Aura");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Demo: gọi API categories + inventory + products + tables khi app khởi động
        fetchCategoriesFromBackend();
        fetchInventoryFromBackend();
        fetchProductsFromBackend();
        fetchTablesFromBackend();
    }

    private TabPane buildTabs() {
        Tab orderTab = new Tab("Order");
        orderTab.setClosable(false);
        orderTab.setContent(buildOrderWorkspace());

        Tab tableTab = new Tab("Table");
        tableTab.setClosable(false);
        tableTab.setContent(buildTableManagementPane());

        Tab statsTab = new Tab("Total Orders");
        statsTab.setClosable(false);
        statsTab.setContent(buildPaymentLogPane());

        TabPane tabPane = new TabPane(orderTab, tableTab, statsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // tab: Order nâu, Table caramel, Total giống Payment; nền tabPane trùng nền app
        tabPane.setStyle("-fx-background-color:#F5F0E1;"
                + "-fx-padding:2;"
                + "-fx-border-color:transparent;"
                + "-fx-tab-min-width:120;"
                + "-fx-tab-max-width:120;"
                + "-fx-tab-min-height:30;");
        orderTab.setStyle("-fx-background-color:#6B4C3B; -fx-text-base-color:#F5F0E1; -fx-font-weight:bold;");
        tableTab.setStyle("-fx-background-color:#C08A64; -fx-text-base-color:#2B2B2B; -fx-font-weight:bold;");
        statsTab.setStyle("-fx-background-color:#6B4C3B; -fx-text-base-color:#F5F0E1; -fx-font-weight:bold;");

        return tabPane;
    }

    private VBox buildHeader() {
        Label title = new Label("☕ Coffee Aura - Dashboard");
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

    private VBox buildOrderWorkspace() {
        BorderPane workbench = new BorderPane();
        workbench.setPadding(new Insets(6, 6, 6, 6));

        VBox leftPanel = buildTablePanel();
        VBox centerPanel = buildOrderPanel();
        VBox rightPanel = buildMenuPanel();

        workbench.setLeft(leftPanel);
        BorderPane.setMargin(leftPanel, new Insets(0, 10, 0, 0));
        workbench.setCenter(centerPanel);
        BorderPane.setMargin(centerPanel, new Insets(0, 10, 0, 10));
        workbench.setRight(rightPanel);

        return new VBox(workbench);
    }

    private VBox buildTablePanel() {
        Label caption = new Label("Chọn bàn");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:18px;-fx-font-weight:bold;");

        // Chỉ hiển thị danh sách bàn số, không cần khu vực
        tableListView = new ListView<>();
        tableListView.setPrefWidth(220);
        tableListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableListView.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:16;");
        tableListView.setCellFactory(list -> new CafeTableCell());
        tableListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                bindOrderToTable(selected);
            }
        });

        Button transferBtn = createGhostButton("Chuyển bàn");
        transferBtn.setOnAction(e -> handleTransfer());

        Button mergeBtn = createGhostButton("Gộp bàn");
        mergeBtn.setOnAction(e -> handleMerge());

        VBox buttons = new VBox(10, transferBtn, mergeBtn);
        buttons.setFillWidth(true);

        Button newOrderBtn = createGhostButton("Tạo order mới");
        newOrderBtn.setOnAction(e -> resetOrderForTable());

        VBox container = new VBox(12, caption, tableListView, buttons, newOrderBtn);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");
        container.setPrefWidth(230);
        VBox.setVgrow(tableListView, Priority.ALWAYS);
        return container;
    }

    private VBox buildOrderPanel() {
        Label caption = new Label("Order hiện tại");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:18px;-fx-font-weight:bold;");

        orderTable = new TableView<>();
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        orderTable.setPlaceholder(new Label("Chưa có món nào cho bàn này"));
        orderTable.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:16;");

        TableColumn<OrderItem, String> nameCol = new TableColumn<>("Món");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<OrderItem, Integer> qtyCol = new TableColumn<>("SL");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setStyle("-fx-alignment:CENTER;");

        TableColumn<OrderItem, Double> priceCol = new TableColumn<>("Đơn giá");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(col -> new CurrencyTableCell());

        TableColumn<OrderItem, Double> totalCol = new TableColumn<>("Thành tiền");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setCellFactory(col -> new CurrencyTableCell());

        orderTable.getColumns().setAll(List.of(nameCol, qtyCol, priceCol, totalCol));

        Button addQtyBtn = createGhostButton("+ Thêm SL");
        addQtyBtn.setOnAction(e -> adjustQuantity(1));

        Button minusQtyBtn = createGhostButton("- Giảm SL");
        minusQtyBtn.setOnAction(e -> adjustQuantity(-1));

        Button deleteBtn = createGhostButton("Hủy món");
        deleteBtn.setOnAction(e -> removeSelectedItem());

        Button replaceBtn = createGhostButton("Đổi sang món chọn");
        replaceBtn.setOnAction(e -> replaceSelectedItem());

        HBox qtyRow = new HBox(10, addQtyBtn, minusQtyBtn, deleteBtn, replaceBtn);
        qtyRow.setAlignment(Pos.CENTER);
        qtyRow.setFillHeight(true);
        qtyRow.setStyle("-fx-padding:10 0 0 0;");

        noteArea = new TextArea();
        noteArea.setPromptText("Ghi chú: ít đá, ít đường, mang về...");
        noteArea.setPrefRowCount(3);
        noteArea.setStyle(textAreaStyle());

        Button noteButton = createGhostButton("Lưu ghi chú");
        noteButton.setOnAction(e -> saveNoteForCurrentTable());

        totalLabel = new Label("Tổng: 0 đ");
        totalLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:22px;-fx-font-weight:bold;");

        Button payButton = createPrimaryButton("Tính tiền");
        payButton.setOnAction(e -> handlePayment());

        HBox bottomRow = new HBox(12, noteButton, totalLabel, payButton);
        bottomRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(totalLabel, Priority.ALWAYS);
        totalLabel.setMaxWidth(Double.MAX_VALUE);

        VBox container = new VBox(12, caption, orderTable, qtyRow, noteArea, bottomRow);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");
        VBox.setVgrow(orderTable, Priority.ALWAYS);
        return container;
    }

    private VBox buildMenuPanel() {
        Label caption = new Label("Menu nhanh");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:18px;-fx-font-weight:bold;");

        menuListView = new ListView<>(menuItems);
        menuListView.setCellFactory(list -> new MenuCell());
        menuListView.setStyle("-fx-background-radius:16;-fx-background-color:#FFFFFF;");
        menuListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                addSelectedMenuItem(menuListView.getSelectionModel().getSelectedItem());
            }
        });
        menuListView.setPrefHeight(520);
        menuListView.setFixedCellSize(74); // giúp thanh cuộn mượt khi danh sách dài

        Button addBtn = createPrimaryButton("Thêm món chọn");
        addBtn.setOnAction(e -> addSelectedMenuItem(menuListView.getSelectionModel().getSelectedItem()));

        VBox container = new VBox(12, caption, menuListView, addBtn);
        container.setPadding(new Insets(10));
        container.setPrefWidth(240);
        container.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.14),14,0,0,4);");
        VBox.setVgrow(menuListView, Priority.ALWAYS);
        return container;
    }

    private VBox buildTableManagementPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(6, 0, 0, 0));

        VBox mapPanel = buildMgmtMapPanel();
        VBox rightPanel = buildMgmtControlPanel();

        pane.setCenter(mapPanel);
        BorderPane.setMargin(mapPanel, new Insets(0, 24, 0, 0));
        pane.setRight(rightPanel);

        return new VBox(pane);
    }

    private VBox buildPaymentLogPane() {
        Label title = new Label("Tổng số order đã phục vụ");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");
        Label subtitle = new Label("Chỉ ghi nhận khi hoàn tất thanh toán");
        subtitle.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:13px;");

        TableView<PaymentRecord> table = new TableView<>(paymentRecords);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Chưa có order nào được thanh toán"));
        table.setStyle("-fx-background-radius:12;-fx-background-color:#FFFFFF;");

        TableColumn<PaymentRecord, String> colTime = new TableColumn<>("Thời gian");
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().time));

        TableColumn<PaymentRecord, String> colTable = new TableColumn<>("Bàn");
        colTable.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().table));

        TableColumn<PaymentRecord, String> colMethod = new TableColumn<>("Phương thức");
        colMethod.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().method));

        TableColumn<PaymentRecord, String> colTotal = new TableColumn<>("Tổng cộng");
        colTotal.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().totalFormatted));

        table.getColumns().setAll(List.of(colTime, colTable, colMethod, colTotal));

        VBox container = new VBox(8, title, subtitle, table);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:16;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),12,0,0,4);");
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    private VBox buildMgmtMapPanel() {
        Label caption = new Label("Sơ đồ bàn");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        mgmtFloorMap = new FlowPane();
        mgmtFloorMap.setHgap(12);
        mgmtFloorMap.setVgap(12);
        mgmtFloorMap.setPadding(new Insets(8));
        mgmtFloorMap.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.14),14,0,0,4);");
        refreshMgmtFloorMap();

        VBox legend = buildMgmtLegend();

        VBox container = new VBox(12, caption, mgmtFloorMap, legend);
        VBox.setVgrow(mgmtFloorMap, Priority.ALWAYS);
        return container;
    }

    private VBox buildMgmtLegend() {
        Label caption = new Label("Trạng thái");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");

        ListView<String> legend = new ListView<>();
        legend.setPrefHeight(110);
        statusColor.forEach((status, color) -> legend.getItems().add(status + " ●"));
        legend.setCellFactory(list -> new StatusLegendCell(statusColor));

        VBox box = new VBox(8, caption, legend);
        return box;
    }

    private VBox buildMgmtControlPanel() {
        Label caption = new Label("Đặt bàn / cập nhật");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:18px;-fx-font-weight:bold;");

        mgmtSelectedLabel = new Label("Chưa chọn bàn");
        mgmtSelectedLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:16px;");

        mgmtStatusLabel = new Label("Trạng thái: -");
        mgmtStatusLabel.setStyle("-fx-text-fill:#6B4C3B;");

        mgmtStatusCombo = new ComboBox<>();
        // Hiển thị trực tiếp status giống API: empty, serving, reserved
        mgmtStatusCombo.getItems().addAll(statusColor.keySet());
        mgmtStatusCombo.setPromptText("Cập nhật trạng thái");
        mgmtStatusCombo.valueProperty().addListener((obs, ov, nv) -> {
            if (mgmtSelectedTable != null && nv != null) {
                mgmtSelectedTable.setStatus(nv);
                mgmtStatusLabel.setText("Trạng thái: " + nv);
                refreshMgmtFloorMap();
                // Đồng bộ trạng thái bàn lên backend nếu biết id bàn (theo index + 1)
                int tableIndex = allTables.indexOf(mgmtSelectedTable);
                if (tableIndex >= 0) {
                    int tableId = tableIndex + 1;
                    // nv đã là status backend (empty, serving, reserved)
                    String backendStatus = nv;
                    updateTableStatus(tableId, backendStatus);
                }
            }
        });

        mgmtCustomerField = new TextField();
        mgmtCustomerField.setPromptText("Tên khách đặt");
        mgmtPhoneField = new TextField();
        mgmtPhoneField.setPromptText("SĐT");

        mgmtSlotCombo = new ComboBox<>();
        mgmtSlotCombo.getItems().addAll("Hôm nay 9:00", "Hôm nay 11:00", "Hôm nay 13:00", "Hôm nay 18:00", "Mai 9:00");
        mgmtSlotCombo.setPromptText("Khung giờ");

        mgmtNoteArea = new TextArea();
        mgmtNoteArea.setPromptText("Ghi chú");
        mgmtNoteArea.setPrefRowCount(3);

        Button reserveBtn = createPrimaryButton("Đặt trước");
        reserveBtn.setOnAction(e -> handleMgmtReservation());

        Button clearBtn = createGhostButton("Hủy / đánh dấu trống");
        clearBtn.setOnAction(e -> clearMgmtReservation());

        mgmtReservationLabel = new Label("Chưa có đặt trước.");
        mgmtReservationLabel.setWrapText(true);
        mgmtReservationLabel.setStyle("-fx-text-fill:#6B4C3B;");

        VBox container = new VBox(10,
                caption,
                mgmtSelectedLabel,
                mgmtStatusLabel,
                mgmtStatusCombo,
                new Label("Thông tin đặt bàn"),
                mgmtCustomerField,
                mgmtPhoneField,
                mgmtSlotCombo,
                mgmtNoteArea,
                new HBox(12, reserveBtn, clearBtn),
                mgmtReservationLabel);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");
        return container;
    }

    private void refreshMgmtFloorMap() {
        if (mgmtFloorMap == null) {
            return;
        }
        mgmtFloorMap.getChildren().clear();
        for (CafeTable table : allTables) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPrefSize(120, 110);
            card.setPadding(new Insets(10));
            card.setStyle(cardStyleFor(table.getStatus()));

            Label name = new Label(table.getName());
            name.setStyle("-fx-text-fill:#2B2B2B;-fx-font-size:16px;-fx-font-weight:bold;");
            Label seats = new Label(table.getCapacity() + " chỗ");
            seats.setStyle("-fx-text-fill:#6B4C3B;");

            // Chỉ hiển thị số bàn + số chỗ, không hiển thị khu vực
            card.getChildren().addAll(name, seats);
            card.setOnMouseClicked(e -> selectMgmtTable(table));
            mgmtFloorMap.getChildren().add(card);
        }
    }

    private void selectMgmtTable(CafeTable table) {
        this.mgmtSelectedTable = table;
        // Chỉ hiển thị số bàn + chỗ ngồi, bỏ khu vực
        mgmtSelectedLabel.setText(table.getName() + " • " + table.getCapacity() + " chỗ");
        mgmtStatusLabel.setText("Trạng thái: " + table.getStatus());
        mgmtReservationLabel.setText(table.getReservationInfo());
        if (mgmtStatusCombo != null) {
            mgmtStatusCombo.getSelectionModel().select(table.getStatus());
        }
    }

    private void handleMgmtReservation() {
        if (mgmtSelectedTable == null) {
            mgmtReservationLabel.setText("Chưa chọn bàn để đặt.");
            return;
        }
        String name = mgmtCustomerField.getText();
        String slot = mgmtSlotCombo.getValue();
        if (name == null || name.isBlank() || slot == null) {
            mgmtReservationLabel.setText("Tên khách và khung giờ không được trống.");
            return;
        }
        String phone = mgmtPhoneField.getText();
        String note = mgmtNoteArea.getText();
        String info = String.format("Đặt trước: %s (%s) • %s%nGhi chú: %s",
                name,
                phone == null ? "SĐT?" : phone,
                slot,
                note == null || note.isBlank() ? "Không" : note);
        mgmtSelectedTable.setReservationInfo(info);
        // backend dùng status 'reserved'
        mgmtSelectedTable.setStatus("reserved");
        mgmtReservationLabel.setText(info);
        refreshMgmtFloorMap();
    }

    private void clearMgmtReservation() {
        if (mgmtSelectedTable == null) {
            return;
        }
        mgmtSelectedTable.setReservationInfo("Chưa có đặt trước.");
        // quay lại trạng thái trống theo API
        mgmtSelectedTable.setStatus("empty");
        mgmtReservationLabel.setText(mgmtSelectedTable.getReservationInfo());
        refreshMgmtFloorMap();
    }

    private String cardStyleFor(String status) {
        String baseColor = statusColor.getOrDefault(status, "#E0E0E0");
        return "-fx-background-color:" + baseColor + ";"
                + "-fx-background-radius:18;"
                + "-fx-border-color:#6B4C3B;"
                + "-fx-border-width:1;";
    }

    private void filterTablesByArea(String area) {
        ObservableList<CafeTable> filtered = allTables.stream()
                .filter(t -> t.getArea().equals(area))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tableListView.setItems(filtered);
        if (!filtered.isEmpty()) {
            tableListView.getSelectionModel().selectFirst();
        }
    }

    private void bindOrderToTable(CafeTable table) {
        ObservableList<OrderItem> order = ordersByTable.computeIfAbsent(table.getName(),
                k -> FXCollections.observableArrayList());
        orderTable.setItems(order);
        noteArea.setText(notesByTable.getOrDefault(table.getName(), ""));
        updateTotalLabel(order);
    }

    private void addSelectedMenuItem(MenuItemModel menuItem) {
        CafeTable table = tableListView.getSelectionModel().getSelectedItem();
        if (table == null) {
            showWarning("Chưa chọn bàn", "Hãy chọn bàn trước khi thêm món.");
            return;
        }
        if (menuItem == null) {
            showWarning("Chưa chọn món", "Hãy chọn món trong menu.");
            return;
        }
        ObservableList<OrderItem> order = ordersByTable.computeIfAbsent(table.getName(),
                k -> FXCollections.observableArrayList());
        OrderItem existing = order.stream()
                .filter(item -> item.getItemName().equals(menuItem.getName()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            order.add(new OrderItem(menuItem.getName(), menuItem.getPrice(), 1));
        }
        // cập nhật backend: mỗi lần thêm món gửi quantity = 1
        sendAddOrderItemToBackend(menuItem, 1);

        // Khi có món thì chuyển bàn sang trạng thái 'serving' đúng theo API
        table.setStatus("serving");
        orderTable.refresh();
        updateTotalLabel(order);
    }

    private void adjustQuantity(int delta) {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn món", "Chọn món trong bảng để điều chỉnh số lượng.");
            return;
        }
        int newQty = selected.getQuantity() + delta;
        if (newQty <= 0) {
            orderTable.getItems().remove(selected);
        } else {
            selected.setQuantity(newQty);
        }
        orderTable.refresh();
        updateTotalLabel(orderTable.getItems());

        // Đồng bộ số lượng với backend (update-order-item)
        if (currentOrderId > 0) {
            sendUpdateOrderItemToBackend(selected, delta, noteArea.getText());
        }
    }

    private void removeSelectedItem() {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Trừ toàn bộ số lượng trên backend
            if (currentOrderId > 0 && selected.getQuantity() > 0) {
                sendUpdateOrderItemToBackend(selected, -selected.getQuantity(), noteArea.getText());
            }
            orderTable.getItems().remove(selected);
            orderTable.refresh();
            updateTotalLabel(orderTable.getItems());
        }
    }

    private void replaceSelectedItem() {
        OrderItem selectedOrderItem = orderTable.getSelectionModel().getSelectedItem();
        MenuItemModel selectedMenuItem = menuListView.getSelectionModel().getSelectedItem();
        if (selectedOrderItem == null) {
            showWarning("Chưa chọn món trong order", "Hãy chọn món cần đổi.");
            return;
        }
        if (selectedMenuItem == null) {
            showWarning("Chưa chọn món mới", "Chọn món mới ở menu bên phải.");
            return;
        }
        selectedOrderItem.setItemName(selectedMenuItem.getName());
        selectedOrderItem.setPrice(selectedMenuItem.getPrice());
        orderTable.refresh();
        updateTotalLabel(orderTable.getItems());
    }

    private void updateTotalLabel(ObservableList<OrderItem> order) {
        double total = order.stream().mapToDouble(OrderItem::getTotal).sum();
        totalLabel.setText("Tổng: " + currencyFormat.format(total));
    }

    private void saveNoteForCurrentTable() {
        CafeTable table = tableListView.getSelectionModel().getSelectedItem();
        if (table == null) {
            showWarning("Chưa chọn bàn", "Chọn bàn để lưu ghi chú.");
            return;
        }
        notesByTable.put(table.getName(), noteArea.getText());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Đã lưu ghi chú cho " + table.getName());
        alert.setContentText(noteArea.getText().isBlank() ? "Không có ghi chú." : noteArea.getText());
        alert.showAndWait();
    }

    private void handlePayment() {
        CafeTable table = tableListView.getSelectionModel().getSelectedItem();
        if (table == null) {
            showWarning("Chưa chọn bàn", "Chọn bàn trước khi tính tiền.");
            return;
        }

        // Gọi API lấy chi tiết order từ backend (nếu đã có currentOrderId)
        if (currentOrderId > 0) {
            fetchOrderDetailsFromBackend(currentOrderId);
        }

        ObservableList<OrderItem> order = ordersByTable.get(table.getName());
        if (order == null || order.isEmpty()) {
            showWarning("Order trống", "Bàn hiện chưa có món.");
            return;
        }
        ObservableList<PaymentScreen.InvoiceLine> invoiceLines = FXCollections.observableArrayList();
        order.forEach(item -> invoiceLines.add(new PaymentScreen.InvoiceLine(
                item.getItemName(),
                item.getQuantity(),
                item.getPrice())));
        // Ưu tiên lấy ghi chú đang hiển thị trong ô nhập (kể cả chưa ấn "Lưu ghi chú")
        String currentNoteText = noteArea != null ? noteArea.getText() : "";
        String note = currentNoteText != null && !currentNoteText.isBlank()
                ? currentNoteText
                : notesByTable.getOrDefault(table.getName(), "");
        PaymentScreen paymentScreen = new PaymentScreen(table.getName(), invoiceLines, note,
                (tbl, total, method, time) -> {
                    paymentRecords.add(
                            new PaymentRecord(tbl, currencyFormat.format(total), method, time));
                    // Sau khi thanh toán thành công trên UI:
                    // 1) Ghi chú xuống DB (lưu vào note của từng OrderItem qua
                    // update-order-item.php)
                    if (currentOrderId > 0 && order != null && !note.isBlank()) {
                        for (OrderItem oi : order) {
                            // deltaQuantity = 0 để không đổi số lượng, chỉ cập nhật note
                            sendUpdateOrderItemToBackend(oi, 0, note);
                        }
                    }
                    // 2) Gọi backend để checkout order.
                    if (currentOrderId > 0) {
                        checkoutOrder(currentOrderId, method);
                    }
                },
                // truyền orderId để PaymentScreen biết in đúng hoá đơn từ backend
                currentOrderId);
        paymentScreen.showStandalone();
    }

    private void resetOrderForTable() {
        CafeTable table = tableListView.getSelectionModel().getSelectedItem();
        if (table == null) {
            showWarning("Chưa chọn bàn", "Chọn bàn để tạo order.");
            return;
        }
        // gọi backend tạo order mới cho bàn này
        createOrderForTable(table);

        ObservableList<OrderItem> fresh = FXCollections.observableArrayList();
        ordersByTable.put(table.getName(), fresh);
        orderTable.setItems(fresh);
        noteArea.clear();
        notesByTable.remove(table.getName());
        // Sau khi tạo order mới, backend sẽ tự set 'serving', nhưng UI tạm thời để
        // 'empty' cho tới khi có món
        table.setStatus("empty");
        tableListView.refresh();
        updateTotalLabel(fresh);
    }

    private void handleTransfer() {
        CafeTable source = tableListView.getSelectionModel().getSelectedItem();
        if (source == null) {
            showWarning("Chưa chọn bàn", "Chọn bàn cần chuyển.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(source.getName(),
                allTables.stream().map(CafeTable::getName).collect(Collectors.toList()));
        dialog.setHeaderText("Chọn bàn đích để chuyển order");
        dialog.setTitle("Chuyển bàn");
        dialog.setContentText("Bàn đích:");
        dialog.showAndWait().ifPresent(targetName -> {
            if (targetName.equals(source.getName())) {
                showWarning("Trùng bàn", "Hãy chọn bàn khác.");
                return;
            }
            ObservableList<OrderItem> sourceOrder = ordersByTable.remove(source.getName());
            if (sourceOrder != null) {
                ordersByTable.put(targetName, sourceOrder);
            }
            String note = notesByTable.remove(source.getName());
            if (note != null) {
                notesByTable.put(targetName, note);
            }
            // Bàn nguồn sau khi chuyển order về trạng thái trống theo API
            source.setStatus("empty");
            allTables.stream()
                    .filter(t -> t.getName().equals(targetName))
                    .findFirst()
                    // Bàn đích đang phục vụ
                    .ifPresent(t -> t.setStatus("serving"));
            tableListView.refresh();
            bindOrderToTable(source);
        });
    }

    private void handleMerge() {
        CafeTable source = tableListView.getSelectionModel().getSelectedItem();
        if (source == null) {
            showWarning("Chưa chọn bàn", "Chọn bàn nguồn để gộp.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(source.getName(),
                allTables.stream().map(CafeTable::getName).collect(Collectors.toList()));
        dialog.setHeaderText("Chọn bàn muốn gộp vào " + source.getName());
        dialog.setTitle("Gộp bàn");
        dialog.setContentText("Bàn sẽ gộp:");
        dialog.showAndWait().ifPresent(targetName -> {
            if (targetName.equals(source.getName())) {
                showWarning("Trùng bàn", "Hãy chọn bàn khác để gộp.");
                return;
            }
            ObservableList<OrderItem> sourceOrder = ordersByTable.getOrDefault(source.getName(),
                    FXCollections.observableArrayList());
            ObservableList<OrderItem> targetOrder = ordersByTable.getOrDefault(targetName,
                    FXCollections.observableArrayList());
            targetOrder.addAll(sourceOrder);
            ordersByTable.put(targetName, targetOrder);
            ordersByTable.put(source.getName(), FXCollections.observableArrayList());
            notesByTable.put(targetName,
                    notesByTable.getOrDefault(targetName, "") + " | "
                            + notesByTable.getOrDefault(source.getName(), ""));
            notesByTable.remove(source.getName());
            sourceOrder.clear();
            source.setStatus("Trống");
            allTables.stream()
                    .filter(t -> t.getName().equals(targetName))
                    .findFirst()
                    .ifPresent(t -> t.setStatus("Đang phục vụ"));
            tableListView.refresh();
            bindOrderToTable(source);
        });
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        showWarning("Lỗi", "Không thể mở màn hình đăng nhập: " + e.getMessage());
                    }
                });
            }
        });
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:#F2C57C;-fx-background-radius:14;"
                + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;");
        return button;
    }

    private Button createGhostButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:transparent;-fx-border-color:rgba(107,76,59,0.4);"
                + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private String comboStyle() {
        return "-fx-background-radius:12;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-padding:10;-fx-text-fill:#6B4C3B;";
    }

    private String textAreaStyle() {
        return "-fx-background-radius:14;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-text-fill:#6B4C3B;";
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class MenuItemModel {
        private final String name;
        private final String category;
        private final double price;

        public MenuItemModel(String name, String category, double price) {
            this.name = name;
            this.category = category;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public double getPrice() {
            return price;
        }
    }

    public static class OrderItem {
        private final SimpleStringProperty itemName;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty quantity;

        public OrderItem(String itemName, double price, int quantity) {
            this.itemName = new SimpleStringProperty(itemName);
            this.price = new SimpleDoubleProperty(price);
            this.quantity = new SimpleIntegerProperty(quantity);
        }

        public String getItemName() {
            return itemName.get();
        }

        public void setItemName(String name) {
            this.itemName.set(name);
        }

        public double getPrice() {
            return price.get();
        }

        public void setPrice(double price) {
            this.price.set(price);
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setQuantity(int quantity) {
            this.quantity.set(quantity);
        }

        public double getTotal() {
            return getPrice() * getQuantity();
        }
    }

    public static class CafeTable {
        private final SimpleStringProperty name;
        private final SimpleStringProperty area;
        private final SimpleStringProperty status;
        private int capacity = 4;
        private String reservationInfo = "Chưa có đặt trước.";

        public CafeTable(String name, String area, String status) {
            this.name = new SimpleStringProperty(name);
            this.area = new SimpleStringProperty(area);
            this.status = new SimpleStringProperty(status);
        }

        public String getName() {
            return name.get();
        }

        public String getArea() {
            return area.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public String getReservationInfo() {
            return reservationInfo;
        }

        public void setReservationInfo(String reservationInfo) {
            this.reservationInfo = reservationInfo;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private static class CafeTableCell extends ListCell<CafeTable> {
        @Override
        protected void updateItem(CafeTable item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                // item.getStatus() lưu đúng mã API: empty / serving / reserved
                String apiStatus = item.getStatus();
                setText(item.getName() + " • " + apiStatus);
                setStyle("-fx-padding:12;-fx-text-fill:#6B4C3B;");
                if ("serving".equals(apiStatus)) {
                    setStyle(getStyle() + "-fx-background-color:rgba(242,197,124,0.3);");
                } else if ("reserved".equals(apiStatus)) {
                    setStyle(getStyle() + "-fx-background-color:rgba(107,76,59,0.15);");
                }
            }
        }
    }

    private static class StatusLegendCell extends ListCell<String> {
        private final Map<String, String> colorMap;

        StatusLegendCell(Map<String, String> colorMap) {
            this.colorMap = colorMap;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("");
            } else {
                setText(item);
                String status = item.replace(" ●", "");
                setStyle("-fx-background-color:" + colorMap.getOrDefault(status, "#E0E0E0") + ";"
                        + "-fx-text-fill:#2B2B2B;");
            }
        }
    }

    private class MenuCell extends ListCell<MenuItemModel> {
        @Override
        protected void updateItem(MenuItemModel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName() + "\n" + item.getCategory() + " • "
                        + currencyFormat.format(item.getPrice()));
                setStyle("-fx-padding:12;-fx-text-fill:#6B4C3B;");
            }
        }
    }

    private class CurrencyTableCell extends TableCell<OrderItem, Double> {
        @Override
        protected void updateItem(Double value, boolean empty) {
            super.updateItem(value, empty);
            if (empty || value == null) {
                setText(null);
            } else {
                setText(currencyFormat.format(value));
                setStyle("-fx-text-fill:#6B4C3B;");
            }
        }
    }

    // ================= API backend create-order.php =================
    private void createOrderForTable(CafeTable table) {
        int tableIndex = allTables.indexOf(table);
        if (tableIndex < 0) {
            System.err.println("Không tìm thấy bàn để tạo order");
            return;
        }
        int tableId = tableIndex + 1; // giả định id bảng Tables tương ứng thứ tự này
        int employeeId = 1; // TODO: lấy từ đăng nhập / nhận diện khuôn mặt

        String jsonBody = String.format(
                "{\"employee_id\":%d,\"table_id\":%d}",
                employeeId, tableId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(CREATE_ORDER_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                System.out.println("create-order => " + response.statusCode() + " " + body);
                if (response.statusCode() == 200) {
                    String orderIdStr = extractJsonValue(body, "order_id");
                    if (!orderIdStr.isBlank()) {
                        try {
                            currentOrderId = Integer.parseInt(orderIdStr);
                        } catch (NumberFormatException ignored) {
                            System.err.println("order_id không phải số: " + orderIdStr);
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Lỗi gọi create-order: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend add-order-item.php =================
    private void sendAddOrderItemToBackend(MenuItemModel menuItem, int quantity) {
        // Phải có order_id hợp lệ thì mới được thêm món (đã gọi create-order trước đó)
        if (currentOrderId <= 0) {
            System.err.println("Chưa có order_id hợp lệ khi gọi add-order-item");
            Platform.runLater(() -> showWarning(
                    "Chưa tạo order",
                    "Hãy bấm 'Tạo order mới' cho bàn này trước khi thêm món.\n"
                            + "Sau khi backend trả về order_id, mới có thể thêm món vào order."));
            return;
        }
        int orderId = currentOrderId;

        // Tạm dùng index trong danh sách làm product_id. Bạn có thể thay
        // bằng trường id riêng nếu Products.id không khớp thứ tự này.
        int productId = menuItems.indexOf(menuItem) + 1;
        if (productId <= 0) {
            System.err.println("Không tìm thấy productId cho menu: " + menuItem.getName());
            return;
        }

        String note = ""; // có thể lấy từ noteArea nếu muốn
        String safeNote = note.replace("\"", "\\\"");
        String jsonBody = String.format(
                "{\"order_id\":%d,\"product_id\":%d,\"quantity\":%d,\"note\":\"%s\"}",
                orderId, productId, quantity, safeNote);

        HttpRequest request = HttpRequest.newBuilder(URI.create(ADD_ORDER_ITEM_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("add-order-item => " + response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi add-order-item: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend update-order-item.php =================
    private void sendUpdateOrderItemToBackend(OrderItem item, int deltaQuantity, String note) {
        if (currentOrderId <= 0) {
            System.err.println("Chưa có order_id để cập nhật OrderItems");
            return;
        }

        // Tìm product_id dựa theo tên món trong menuItems (giống giả định
        // add-order-item)
        MenuItemModel matched = menuItems.stream()
                .filter(m -> m.getName().equals(item.getItemName()))
                .findFirst()
                .orElse(null);
        if (matched == null) {
            System.err.println("Không tìm thấy product tương ứng với món: " + item.getItemName());
            return;
        }
        int productId = menuItems.indexOf(matched) + 1;
        if (productId <= 0) {
            System.err.println("productId không hợp lệ cho món: " + item.getItemName());
            return;
        }

        String safeNote = (note == null ? "" : note).replace("\"", "\\\"");
        String jsonBody = String.format(
                "{\"order_id\":%d,\"product_id\":%d,\"quantity\":%d,\"note\":\"%s\"}",
                currentOrderId, productId, deltaQuantity, safeNote);

        HttpRequest request = HttpRequest.newBuilder(URI.create(UPDATE_ORDER_ITEM_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("update-order-item => " +
                        response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi update-order-item: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend delete-order.php =================
    private void deleteOrder(int orderId) {
        if (orderId < 0) {
            System.err.println("Chưa có order_id để xoá");
            return;
        }
        String jsonBody = String.format("{\"order_id\":%d}", orderId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(DELETE_ORDER_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("delete-order => " + response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi delete-order: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend checkout-order.php =================
    private void checkoutOrder(int orderId, String paymentMethod) {
        if (orderId <= 0) {
            System.err.println("Chưa có order_id hợp lệ để checkout");
            return;
        }
        String safeMethod = paymentMethod == null ? "" : paymentMethod.replace("\"", "\\\"");
        String jsonBody = String.format(
                "{\"order_id\":%d,\"payment_method\":\"%s\"}",
                orderId, safeMethod);

        HttpRequest request = HttpRequest.newBuilder(URI.create(CHECKOUT_ORDER_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("checkout-order => " +
                        response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi checkout-order: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend get-categories.php =================
    private void fetchCategoriesFromBackend() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GET_CATEGORIES_URL))
                .GET()
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("get-categories => " +
                        response.statusCode() + " " + response.body());
                // Ở đây mình chỉ log ra console; sau này bạn có thể parse JSON để đổ vào UI
                // filter menu.
            } catch (Exception ex) {
                System.err.println("Lỗi gọi get-categories: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend get-inventory.php =================
    private void fetchInventoryFromBackend() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GET_INVENTORY_URL))
                .GET()
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("get-inventory => " +
                        response.statusCode() + " " + response.body());
                // Tạm thời chỉ log ra console; sau này có thể parse JSON để hiển thị tab
                // Inventory.
            } catch (Exception ex) {
                System.err.println("Lỗi gọi get-inventory: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend get-order-details.php =================
    private void fetchOrderDetailsFromBackend(int orderId) {
        if (orderId <= 0) {
            System.err.println("order_id không hợp lệ khi gọi get-order-details");
            return;
        }
        String url = GET_ORDER_DETAILS_URL + "?order_id=" + orderId;

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("get-order-details => " +
                        response.statusCode() + " " + response.body());
                // Sau này có thể parse JSON để map lại vào UI / PaymentScreen.
            } catch (Exception ex) {
                System.err.println("Lỗi gọi get-order-details: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend get-products.php =================
    private void fetchProductsFromBackend() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GET_PRODUCTS_URL))
                .GET()
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("get-products => " +
                        response.statusCode() + " " + response.body());

                String body = response.body();
                // Kỳ vọng JSON dạng: { "status":true, "data":[{...},{...}] }
                int dataIdx = body.indexOf("\"data\"");
                if (dataIdx < 0) {
                    return;
                }
                int arrayStart = body.indexOf('[', dataIdx);
                int arrayEnd = body.indexOf(']', arrayStart);
                if (arrayStart < 0 || arrayEnd < 0 || arrayEnd <= arrayStart) {
                    return;
                }
                String dataArray = body.substring(arrayStart + 1, arrayEnd);
                if (dataArray.isBlank()) {
                    return;
                }

                String[] items = dataArray.split("\\},\\s*\\{");
                var loaded = FXCollections.<MenuItemModel>observableArrayList();

                for (String raw : items) {
                    String obj = raw.trim();
                    if (!obj.startsWith("{")) {
                        obj = "{" + obj;
                    }
                    if (!obj.endsWith("}")) {
                        obj = obj + "}";
                    }
                    // Map đúng field JSON thực tế: product_name, category_name, price, available
                    String name = extractJsonValue(obj, "product_name");
                    String category = extractJsonValue(obj, "category_name");
                    String priceStr = extractJsonValue(obj, "price");
                    String availableStr = extractJsonValue(obj, "available");

                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    // Bỏ những món không còn bán (available = 0)
                    try {
                        if ("0".equals(availableStr.trim())) {
                            continue;
                        }
                    } catch (Exception ignore) {
                    }

                    double price = 0.0;
                    try {
                        price = Double.parseDouble(priceStr);
                    } catch (Exception ignore) {
                    }
                    loaded.add(new MenuItemModel(name, category, price));
                }

                var finalLoaded = loaded;
                javafx.application.Platform.runLater(() -> {
                    menuItems.setAll(finalLoaded);
                });
            } catch (Exception ex) {
                System.err.println("Lỗi gọi get-products: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend get-tables.php =================
    private void fetchTablesFromBackend() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GET_TABLES_URL))
                .GET()
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("get-tables => " +
                        response.statusCode() + " " + response.body());

                String body = response.body();
                int dataIdx = body.indexOf("\"data\"");
                if (dataIdx < 0) {
                    return;
                }
                int arrayStart = body.indexOf('[', dataIdx);
                int arrayEnd = body.indexOf(']', arrayStart);
                if (arrayStart < 0 || arrayEnd < 0 || arrayEnd <= arrayStart) {
                    return;
                }
                String dataArray = body.substring(arrayStart + 1, arrayEnd);
                if (dataArray.isBlank()) {
                    return;
                }

                String[] items = dataArray.split("\\},\\s*\\{");
                var loadedTables = FXCollections.<CafeTable>observableArrayList();

                for (String raw : items) {
                    String obj = raw.trim();
                    if (!obj.startsWith("{")) {
                        obj = "{" + obj;
                    }
                    if (!obj.endsWith("}")) {
                        obj = obj + "}";
                    }
                    String tableName = extractJsonValue(obj, "table_name");
                    String areaName = extractJsonValue(obj, "area_name");
                    String statusBackend = extractJsonValue(obj, "table_status");
                    String capacityStr = extractJsonValue(obj, "capacity");

                    if (tableName == null || tableName.isBlank()) {
                        continue;
                    }

                    // Lưu trực tiếp status giống API (empty, serving, reserved)
                    String apiStatus;
                    switch (statusBackend) {
                        case "serving" -> apiStatus = "serving";
                        case "reserved" -> apiStatus = "reserved";
                        case "empty" -> apiStatus = "empty";
                        default -> apiStatus = "empty";
                    }

                    CafeTable table = new CafeTable(tableName,
                            areaName == null || areaName.isBlank() ? "Khu khác" : areaName,
                            apiStatus);
                    try {
                        int cap = Integer.parseInt(capacityStr);
                        table.setCapacity(cap);
                    } catch (Exception ignore) {
                    }
                    loadedTables.add(table);
                }

                javafx.application.Platform.runLater(() -> {
                    allTables.setAll(loadedTables);
                    // Không dùng giao diện khu vực ở panel Order: đổ thẳng toàn bộ bàn vào list
                    if (tableListView != null) {
                        tableListView.getItems().setAll(allTables);
                        if (!allTables.isEmpty()) {
                            tableListView.getSelectionModel().selectFirst();
                        }
                    }
                    // Làm tươi lại sơ đồ bàn nếu đang mở tab Table
                    refreshMgmtFloorMap();
                });
            } catch (Exception ex) {
                System.err.println("Lỗi gọi get-tables: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend get-unpaid-orders.php =================
    private void fetchUnpaidOrdersFromBackend() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GET_UNPAID_ORDERS_URL))
                .GET()
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("get-unpaid-orders => " +
                        response.statusCode() + " " + response.body());
                // Sau này có thể parse JSON để hiển thị danh sách order chưa thanh toán.
            } catch (Exception ex) {
                System.err.println("Lỗi gọi get-unpaid-orders: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend update-inventory-status.php =================
    private void updateInventoryStatus(int inventoryId, String status) {
        if (inventoryId <= 0 || status == null || status.isBlank()) {
            System.err.println("Thiếu id hoặc inventory_status khi gọi update-inventory-status");
            return;
        }
        String safeStatus = status.replace("\"", "\\\"");
        String jsonBody = String.format(
                "{\"id\":%d,\"inventory_status\":\"%s\"}",
                inventoryId, safeStatus);

        HttpRequest request = HttpRequest.newBuilder(URI.create(UPDATE_INVENTORY_STATUS_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("update-inventory-status => " +
                        response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi update-inventory-status: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend update-product-available.php =================
    private void updateProductAvailable(int productId, boolean available) {
        if (productId <= 0) {
            System.err.println("product_id không hợp lệ khi gọi update-product-available");
            return;
        }
        int availableInt = available ? 1 : 0;
        String jsonBody = String.format(
                "{\"product_id\":%d,\"available\":%d}",
                productId, availableInt);

        HttpRequest request = HttpRequest.newBuilder(URI.create(UPDATE_PRODUCT_AVAILABLE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("update-product-available => " +
                        response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi update-product-available: " + ex.getMessage());
            }
        }).start();
    }

    // ================= API backend update-table-status.php =================
    private void updateTableStatus(int tableId, String backendStatus) {
        if (tableId <= 0 || backendStatus == null || backendStatus.isBlank()) {
            System.err.println("Thiếu table_id hoặc table_status khi gọi update-table-status");
            return;
        }
        String safeStatus = backendStatus.replace("\"", "\\\"");
        String jsonBody = String.format(
                "{\"table_id\":%d,\"table_status\":\"%s\"}",
                tableId, safeStatus);

        HttpRequest request = HttpRequest.newBuilder(URI.create(UPDATE_TABLE_STATUS_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("update-table-status => " +
                        response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi update-table-status: " + ex.getMessage());
            }
        }).start();
    }

    // ================= JSON helper =================
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) {
            return "";
        }
        start += pattern.length();
        // bỏ qua dấu nháy nếu là string
        if (start < json.length() && json.charAt(start) == '"') {
            start++;
            int end = json.indexOf('"', start);
            return end > start ? json.substring(start, end) : "";
        } else {
            int end = json.indexOf(',', start);
            if (end < 0) {
                end = json.indexOf('}', start);
            }
            return end > start ? json.substring(start, end).trim() : "";
        }
    }

    // Helper parse number (double) từ JSON thô
    private double extractJsonNumber(String json, String key) {
        String raw = extractJsonValue(json, key);
        if (raw == null || raw.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(raw.replace("\"", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static class PaymentRecord {
        final String table;
        final String totalFormatted;
        final String method;
        final String time;

        PaymentRecord(String table, String totalFormatted, String method, LocalDateTime time) {
            this.table = table;
            this.totalFormatted = totalFormatted;
            this.method = method;
            this.time = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(time);
        }
    }
}
