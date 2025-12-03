<?php

include __DIR__ . '/../../connect.php';

try {
    // Nhận dữ liệu JSON
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['product_id']) || !isset($data['available'])) {
        echo json_encode([
            "status" => false,
            "message" => "Missing product_id or available"
        ]);
        exit;
    }

    $product_id = $data['product_id'];
    $available = $data['available']; // 1 hoặc 0

    // Chuẩn bị câu SQL
    $sql = "UPDATE Products SET available = :available WHERE id = :id";
    $stmt = $pdo->prepare($sql);

    $success = $stmt->execute([
        ":available" => $available,
        ":id" => $product_id
    ]);

    if ($success) {
        echo json_encode([
            "status" => true,
            "message" => "Product availability updated"
        ]);
    } else {
        echo json_encode([
            "status" => false,
            "message" => "Update failed"
        ]);
    }

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Error updating product availability",
        "error" => $e->getMessage()
    ]);
}
?>
