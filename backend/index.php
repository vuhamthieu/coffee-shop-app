<?php
    header("Access-Control-Allow-Origin: *");
    header("Content-Type: application/json; charset=UTF-8");
    header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE");
    header("Access-Control-Allow-Headers: *");

    $request = $_SERVER['REQUEST_URI'];
    $scriptName = dirname($_SERVER['SCRIPT_NAME']);

    // Lấy phần path, bỏ query param
    $fullPath = parse_url($request, PHP_URL_PATH);

    // Bỏ phần script path gốc, lấy phần còn lại
    $path = substr($fullPath, strlen($scriptName));
    $path = trim($path, '/');

    switch ($path) {
        case 'api/employee/add-order-item':   
            require 'api/employee/add-order-item.php';
            break;

        case 'api/employee/apply-coupon':
            require 'api/employee/apply-coupon.php';
            break;

        case 'api/employee/check-coupon':
            require 'api/employee/check-coupon.php';
            break;

        case 'api/employee/create-order':
            require 'api/employee/create-order.php';
            break;

        case 'api/employee/delete-order':
            require 'api/employee/delete-order.php';
            break;

        case 'api/employee/face-checkin':
            require 'api/employee/face-checkin.php';
            break;

        case 'api/employee/face-checkout':
            require 'api/employee/face-checkout.php';
            break;
        
        case 'api/employee/face-login':
            require 'api/employee/face-login.php';
            break;

        case 'api/employee/get-categories':
            require 'api/employee/get-categories.php';
            break;

        case 'api/employee/get-inventory':
            require 'api/employee/get-inventory.php';
            break;

        case 'api/employee/get-order':
            require 'api/employee/get-order.php';
            break;

        case 'api/employee/get-products':
            require 'api/employee/get-products.php';
            break;

        case 'api/employee/get-tables':
            require 'api/employee/get-tables.php';
            break;

        case 'api/employee/get-unpaid-orders':
            require 'api/employee/get-unpaid-orders.php';
            break;

        case 'api/employee/login':
            require 'api/employee/login.php';
            break;

        case 'api/employee/logout':
            require 'api/employee/logout.php';
            break;

        case 'api/employee/order-checkout':
            require 'api/employee/order-checkout.php';
            break;

        case 'api/employee/print-invoice':
            require 'api/employee/print-invoice.php';
            break;

        case 'api/employee/print-item-label':
            require 'api/employee/print-item-label.php';
            break;

        case 'api/employee/remove-coupon':
            require 'api/employee/remove-coupon.php';
            break;

        case 'api/employee/update-inventory-status':
            require 'api/employee/update-inventory-status.php';
            break;

        case 'api/employee/update-order-item':
            require 'api/employee/update-order-item.php';
            break;

        case 'api/employee/update-product-available':
            require 'api/employee/update-product-available.php';
            break;

        case 'api/employee/update-table-status':
            require 'api/employee/update-table-status.php';
            break;

        // --- Admin routes ---
        case 'api/admin/add-category':
            require 'api/admin/add_category.php';
            break;

        case 'api/admin/add-employee':
            require 'api/admin/add_employee.php';
            break;

        case 'api/admin/add-product':
            require 'api/admin/add_product.php';
            break;

        case 'api/admin/delete-category':
            require 'api/admin/delete_category.php';
            break;

        case 'api/admin/delete-employee':
            require 'api/admin/delete_employee.php';
            break;

        case 'api/admin/delete-product':
            require 'api/admin/delete_product.php';
            break;

        case 'api/admin/toggle-product-available':
            require 'api/admin/toggle_product_available.php';
            break;

        case 'api/admin/update-category':
            require 'api/admin/update_category.php';
            break;

        case 'api/admin/update-employee':
            require 'api/admin/update_employee.php';
            break;

        case 'api/admin/update-price':
            require 'api/admin/update_price.php';
            break;

        case 'api/admin/update-product':
            require 'api/admin/update_product.php';
            break;

        case 'api/admin/update-hot-product':
            require 'api/admin/update-hot-product.php';
            break;

        // coupons
        case 'api/admin/coupons/create':
            require 'api/admin/coupons/create.php';
            break;

        case 'api/admin/coupons/get-list':
            require 'api/admin/coupons/get-list.php';
            break;

        case 'api/admin/coupons/update':
            require 'api/admin/coupons/update.php';
            break;

        case 'api/admin/coupons/delete':
            require 'api/admin/coupons/delete.php';
            break;

        case 'api/admin/coupons/get-usage':
            require 'api/admin/coupons/get-usage.php';
            break;

        // employees (admin)
        case 'api/admin/employees/update-role':
            require 'api/admin/employees/update-role.php';
            break;

        case 'api/admin/employees/lock-account':
            require 'api/admin/employees/lock-account.php';
            break;

        case 'api/admin/employees/unlock-account':
            require 'api/admin/employees/unlock-account.php';
            break;

        case 'api/admin/employees/get-working-hours':
            require 'api/admin/employees/get-working-hours.php';
            break;

        case 'api/admin/employees/add-faceid':
            require 'api/admin/employees/add-faceid.php';
            break;

        case 'api/admin/employees/delete-faceid':
            require 'api/admin/employees/delete-faceid.php';
            break;

        // inventory
        case 'api/admin/inventory/get-list':
            require 'api/admin/inventory/get-list.php';
            break;

        case 'api/admin/inventory/add-inventory':
            require 'api/admin/inventory/add_inventory.php';
            break;

        case 'api/admin/inventory/update-inventory':
            require 'api/admin/inventory/update_inventory.php';
            break;

        case 'api/admin/inventory/delete-inventory':
            require 'api/admin/inventory/delete_inventory.php';
            break;

        case 'api/admin/inventory/get-low-stock-alert':
            require 'api/admin/inventory/get-low-stock-alert.php';
            break;

        // reports
        case 'api/admin/reports/revenue-by-day':
            require 'api/admin/reports/revenue-by-day.php';
            break;

        case 'api/admin/reports/revenue-by-week':
            require 'api/admin/reports/revenue-by-week.php';
            break;

        case 'api/admin/reports/revenue-by-month':
            require 'api/admin/reports/revenue-by-month.php';
            break;

        case 'api/admin/reports/revenue-by-shift':
            require 'api/admin/reports/revenue-by-shift.php';
            break;

        case 'api/admin/reports/best-selling-products':
            require 'api/admin/reports/best-selling-products.php';
            break;

        case 'api/admin/reports/customer-count':
            require 'api/admin/reports/customer-count.php';
            break;

        case 'api/admin/reports/expenses':
            require 'api/admin/reports/expenses.php';
            break;

        default:
            http_response_code(404);
            echo json_encode([
                "status" => "error",
                "message" => "API not found"
            ]);
            break;
    }
?>