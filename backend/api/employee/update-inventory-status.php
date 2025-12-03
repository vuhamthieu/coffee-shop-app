<?php

include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data["id"]) || !isset($data["inventory_status"])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'id' or 'inventory_status'"
    ]);
    exit;
}

$id = intval($data["id"]);
$status = $data["inventory_status"];

// Kiểm tra giá trị ENUM hợp lệ
$valid_status = ['ok', 'low', 'out'];

if (!in_array($status, $valid_status)) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid inventory_status value"
    ]);
    exit;
}

try {
    $sql = "UPDATE Inventory 
            SET inventory_status = :status
            WHERE id = :id";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ":status" => $status,
        ":id" => $id
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Inventory status updated successfully"
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to update inventory status",
        "error" => $e->getMessage()
    ]);
}
?>
