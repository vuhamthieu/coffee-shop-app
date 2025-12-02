<?php
header("Content-Type: application/json; charset=UTF-8");

include __DIR__ . '/../../connect.php';

// --- Kiểm tra phương thức POST ---
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        "status" => false,
        "message" => "Method not allowed"
    ]);
    exit;
}

// --- Nhận dữ liệu ---
$data = json_decode(file_get_contents("php://input"), true);
$employee_id = $data['employee_id'] ?? null;

if (!$employee_id) {
    echo json_encode([
        "status" => false,
        "message" => "employee_id is required"
    ]);
    exit;
}

try {
    // --- Kiểm tra tồn tại nhân viên ---
    $stmt = $pdo->prepare("SELECT id, employee_name, active FROM Employees WHERE id = ?");
    $stmt->execute([$employee_id]);
    $employee = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$employee) {
        echo json_encode([
            "status" => false,
            "message" => "Employee not found"
        ]);
        exit;
    }

    if ($employee['active'] == 0) {
        echo json_encode([
            "status" => false,
            "message" => "Employee is deactivated"
        ]);
        exit;
    }

    // --- Ghi log đăng xuất ---
    $log_stmt = $pdo->prepare("
        INSERT INTO ActivityLog (employee_id, action_name, detail)
        VALUES (?, 'logout', 'User logged out')
    ");
    $log_stmt->execute([$employee_id]);

    // --- Trả về thành công ---
    echo json_encode([
        "status" => true,
        "message" => "Logout successful",
        "employee_id" => $employee_id
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Server error",
        "error" => $e->getMessage()
    ]);
}
?>
