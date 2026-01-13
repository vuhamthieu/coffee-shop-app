<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['inventory_id'])) {
    echo json_encode(["error" => "inventory_id is required"]);
    exit;
}

$inventory_id = $data['inventory_id'];

/* Check tồn tại */
$stmt = $pdo->prepare("SELECT inventory_name FROM Inventory WHERE id = :id");
$stmt->execute([':id' => $inventory_id]);
$inv = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$inv) {
    echo json_encode(["error" => "Inventory not found"]);
    exit;
}

try {
    $pdo->beginTransaction();

    // Delete logs
    $pdo->prepare("DELETE FROM InventoryLog WHERE inventory_id = :id")
        ->execute([':id' => $inventory_id]);

    // Delete inventory
    $pdo->prepare("DELETE FROM Inventory WHERE id = :id")
        ->execute([':id' => $inventory_id]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Inventory deleted",
        "inventory_name" => $inv['inventory_name']
    ]);

} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode(["error" => $e->getMessage()]);
}
