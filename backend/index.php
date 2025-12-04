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
        //đăng nhập
        case 'api/login':   
            require 'api/login.php';
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