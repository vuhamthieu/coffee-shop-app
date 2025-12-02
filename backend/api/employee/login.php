<?php
    include __DIR__ . '/../../connect.php';

    // Đọc JSON input
    $input = json_decode(file_get_contents("php://input"), true);

    if (!isset($input['username']) || !isset($input['password'])) {
        echo json_encode([
            "success" => false,
            "message" => "Missing username or password"
        ]);
        exit;
    }

    $username = trim($input['username']);
    $password = $input['password'];

    // Lấy user từ database
    $stmt = $conn->prepare("
        SELECT e.id, e.employee_name, e.username, e.employee_password, e.role_id, e.active, r.role_name
        FROM Employees e
        JOIN Roles r ON e.role_id = r.id
        WHERE e.username = ?
        LIMIT 1
    ");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode([
            "success" => false,
            "message" => "Invalid username or password"
        ]);
        exit;
    }

    $user = $result->fetch_assoc();

    // Kiểm tra active
    if ((int)$user['active'] === 0) {
        echo json_encode([
            "success" => false,
            "message" => "Account is deactivated"
        ]);
        exit;
    }

    // Kiểm tra mật khẩu
    if (!password_verify($password, $user['employee_password'])) {
        echo json_encode([
            "success" => false,
            "message" => "Invalid username or password"
        ]);
        exit;
    }

    // Ghi Activity Log
    $log_stmt = $conn->prepare("
        INSERT INTO ActivityLog (employee_id, action_name, detail)
        VALUES (?, 'login', 'User logged in')
    ");
    $log_stmt->bind_param("i", $user['id']);
    $log_stmt->execute();

    // Trả kết quả thành công
    echo json_encode([
        "success" => true,
        "message" => "Login success",
        "employee" => [
            "id" => $user['id'],
            "name" => $user['employee_name'],
            "username" => $user['username'],
            "role_id" => $user['role_id'],
            "role_name" => $user['role_name']
        ]
    ]);
?>

