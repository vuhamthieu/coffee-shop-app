CREATE TABLE Roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE  -- Admin, Cashier, Staff
) ENGINE=InnoDB;

CREATE TABLE Employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    username VARCHAR(50) NOT NULL UNIQUE,
    employee_password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    active TINYINT(1) DEFAULT 1,
    FOREIGN KEY (role_id) REFERENCES Roles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- FaceID: lưu embedding khuôn mặt
CREATE TABLE FaceData (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    embedding LONGBLOB NOT NULL,           -- vector 128 hoặc 512 chiều
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE Products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    price DOUBLE NOT NULL,
    product_image VARCHAR(255),
    available TINYINT(1) DEFAULT 1,        -- 1 = còn, 0 = hết
    FOREIGN KEY (category_id) REFERENCES Categories(id)
) ENGINE=InnoDB;

CREATE TABLE Tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    table_status ENUM('empty', 'serving', 'reserved') DEFAULT 'empty'
) ENGINE=InnoDB;

CREATE TABLE Orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_id INT,
    employee_id INT,
    total_before_discount DOUBLE,
    total DOUBLE,
    order_status ENUM('unpaid','paid','cancelled') DEFAULT 'unpaid',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,
    FOREIGN KEY (table_id) REFERENCES Tables(id) ON DELETE SET NULL,
    FOREIGN KEY (employee_id) REFERENCES Employees(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE OrderItems (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    note TEXT,
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Products(id)
) ENGINE=InnoDB;

CREATE TABLE Attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    checkin_time DATETIME NOT NULL,
    checkout_time DATETIME,
    FOREIGN KEY (employee_id) REFERENCES Employees(id)
) ENGINE=InnoDB;

CREATE TABLE Inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    inventory_name VARCHAR(100) NOT NULL,
    quantity DOUBLE NOT NULL,
    unit VARCHAR(20) NOT NULL,                  -- gram, ml, kg…
    inventory_status ENUM('ok', 'low', 'out') DEFAULT 'ok'
) ENGINE=InnoDB;

CREATE TABLE InventoryLog (
    id INT AUTO_INCREMENT PRIMARY KEY,
    inventory_id INT NOT NULL,
    log_change DOUBLE NOT NULL,                
    log_type ENUM('import','export','adjust') NOT NULL,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventory_id) REFERENCES Inventory(id)
) ENGINE=InnoDB;

CREATE TABLE Coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    coupon_type ENUM('percent','amount') NOT NULL,
    coupon_value DOUBLE NOT NULL,
    min_order DOUBLE DEFAULT 0,
    coupon_start_date DATETIME,
    coupon_end_date DATETIME,
    usage_limit INT DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE OrderCoupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    coupon_id INT NOT NULL,
    discount DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES Coupons(id)
) ENGINE=InnoDB;

CREATE TABLE ActivityLog (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    action_name VARCHAR(100) NOT NULL,
    detail TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES Employees(id)
) ENGINE=InnoDB;

CREATE TABLE Salary (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    salary_month INT,
    salary_year INT,
    total_hours DOUBLE,
    hourly_rate DOUBLE,
    total_salary DOUBLE,
    FOREIGN KEY (employee_id) REFERENCES Employees(id),
    UNIQUE KEY unique_salary(employee_id, salary_month, salary_year)
) ENGINE=InnoDB;
