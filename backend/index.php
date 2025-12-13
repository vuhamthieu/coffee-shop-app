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

        default:
            http_response_code(404);
            echo json_encode([
                "status" => "error",
                "message" => "API not found"
            ]);
            break;
    }
?>