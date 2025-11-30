CREATE TABLE Employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_name TEXT NOT NULL,
    phone TEXT,
    username TEXT UNIQUE NOT NULL,
    employee_password TEXT NOT NULL,
    role_id INTEGER NOT NULL,
    active INTEGER DEFAULT 1,
    FOREIGN KEY(role_id) REFERENCES Roles(id)
);

CREATE TABLE Roles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_name TEXT NOT NULL  -- Admin, Cashier, Staff
);

CREATE TABLE Categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_name TEXT NOT NULL
);

CREATE TABLE Products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_name TEXT NOT NULL,
    category_id INTEGER NOT NULL,
    price REAL NOT NULL,
    product_image TEXT,
    available INTEGER DEFAULT 1,  -- 1 = còn, 0 = hết
    FOREIGN KEY(category_id) REFERENCES Categories(id)
);

CREATE TABLE Tables (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_name TEXT NOT NULL,
    table_status TEXT DEFAULT 'empty'  -- empty / serving / reserved
);

CREATE TABLE Orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    table_id INTEGER,
    employee_id INTEGER,
    total REAL,
    order_status TEXT DEFAULT 'unpaid', -- unpaid / paid / cancelled
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,
    FOREIGN KEY(table_id) REFERENCES Tables(id),
    FOREIGN KEY(employee_id) REFERENCES Employees(id)
);

CREATE TABLE OrderItems (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,        -- lưu giá tại thời điểm bán
    note TEXT,
    FOREIGN KEY(order_id) REFERENCES Orders(id),
    FOREIGN KEY(product_id) REFERENCES Products(id)
);

CREATE TABLE WorkShift (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    checkin_time DATETIME NOT NULL,
    checkout_time DATETIME,
    total_hours REAL,
    FOREIGN KEY(employee_id) REFERENCES Employees(id)
);

CREATE TABLE Inventory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    inventory_name TEXT NOT NULL,
    quantity REAL NOT NULL,
    unit TEXT NOT NULL,       -- gram, ml, kg…
    low_threshold REAL DEFAULT 0   -- cảnh báo hết hàng
);

CREATE TABLE InventoryLog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    inventory_id INTEGER NOT NULL,
    change REAL NOT NULL,      -- số lượng thay đổi (+ nhập, - xuất)
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(inventory_id) REFERENCES Inventory(id)
);

CREATE TABLE Coupons (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT UNIQUE NOT NULL,
    coupon_type TEXT NOT NULL,          -- percent / amount
    coupon_value REAL NOT NULL,
    min_order REAL DEFAULT 0,
    coupon_start_date DATETIME,
    coupon_end_date DATETIME,
    usage_limit INTEGER DEFAULT 1
);

CREATE TABLE OrderCoupons (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    coupon_id INTEGER NOT NULL,
    discount REAL NOT NULL,
    FOREIGN KEY(order_id) REFERENCES Orders(id),
    FOREIGN KEY(coupon_id) REFERENCES Coupons(id)
);

CREATE TABLE ActivityLog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER,
    action_name TEXT NOT NULL,
    detail TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(employee_id) REFERENCES Employees(id)
);

CREATE TABLE Salary (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER,
    salary_month INTEGER,
    salary_year INTEGER,
    total_hours REAL,
    hourly_rate REAL,
    total_salary REAL,
    FOREIGN KEY(employee_id) REFERENCES Employees(id)
);
