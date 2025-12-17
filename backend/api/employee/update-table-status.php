<?php

include __DIR__ . '/../../connect.php';

// Lấy dữ liệu từ POST JSON
$data = json_decode(file_get_contents("php://input"), true);

$table_id = $data['table_id'] ?? null;
$table_status = $data['table_status'] ?? null;

$valid_status = ['empty', 'serving', 'reserved'];

if (!$table_id || !$table_status) {
    echo json_encode([
        "status" => false,
        "message" => "table_id and table_status are required"
    ]);
    exit;
}

if (!in_array($table_status, $valid_status)) {
    echo json_encode([
        "status" => false,
        "message" => "Invalid table_status value"
    ]);
    exit;
}

try {
    // Kiểm tra bàn tồn tại
    $stmt = $pdo->prepare("SELECT id FROM Tables WHERE id = ?");
    $stmt->execute([$table_id]);
    $table = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$table) {
        echo json_encode([
            "status" => false,
            "message" => "Table not found"
        ]);
        exit;
    }

    // Cập nhật trạng thái bàn
    $update = $pdo->prepare("UPDATE Tables SET table_status = ? WHERE id = ?");
    $update->execute([$table_status, $table_id]);

    echo json_encode([
        "status" => true,
        "message" => "Table status updated successfully",
        "data" => [
            "table_id" => $table_id,
            "table_status" => $table_status
        ]
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to update table",
        "error" => $e->getMessage()
    ]);
}
?>
