<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

// Kiểm tra dữ liệu bắt buộc
if (!isset($data['employee_id']) || !isset($data['table_id'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'employee_id' or 'table_id'"
    ]);
    exit;
}

$employee_id = intval($data['employee_id']);
$table_id    = intval($data['table_id']);
$total       = 0; // Ban đầu chưa có món
$order_status = 'unpaid';

try {
    // Tạo order mới
    $sql = "INSERT INTO Orders (table_id, employee_id, total, order_status, created_at)
            VALUES (:table_id, :employee_id, :total, :order_status, NOW())";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ":table_id" => $table_id,
        ":employee_id" => $employee_id,
        ":total" => $total,
        ":order_status" => $order_status
    ]);

    // Lấy order_id vừa tạo
    $order_id = $pdo->lastInsertId();

    // Cập nhật trạng thái bàn thành 'serving'
    $sql_table = "UPDATE Tables SET table_status = 'serving' WHERE id = :table_id";
    $stmt_table = $pdo->prepare($sql_table);
    $stmt_table->execute([":table_id" => $table_id]);

    echo json_encode([
        "success" => true,
        "message" => "Order created successfully",
        "order_id" => $order_id
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to create order",
        "error" => $e->getMessage()
    ]);
}
?>
