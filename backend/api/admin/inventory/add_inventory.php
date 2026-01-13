<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (
    empty($data['inventory_name']) ||
    !isset($data['quantity']) ||
    empty($data['unit'])
) {
    echo json_encode(["error" => "inventory_name, quantity and unit are required"]);
    exit;
}

$inventory_name = $data['inventory_name'];
$quantity = (double)$data['quantity'];
$unit = $data['unit'];

/* Tự tính status */
if ($quantity <= 0) {
    $status = "out";
} elseif ($quantity < 10) {
    $status = "low";
} else {
    $status = "ok";
}

try {
    $pdo->beginTransaction();

    // Insert inventory
    $stmt = $pdo->prepare("
        INSERT INTO Inventory (inventory_name, quantity, unit, inventory_status)
        VALUES (:name, :qty, :unit, :status)
    ");
    $stmt->execute([
        ':name' => $inventory_name,
        ':qty' => $quantity,
        ':unit' => $unit,
        ':status' => $status
    ]);

    $inventoryId = $pdo->lastInsertId();

    // Log
    $stmt = $pdo->prepare("
        INSERT INTO InventoryLog (inventory_id, log_change, log_type, note)
        VALUES (:id, :change, 'adjust', 'Initial inventory created')
    ");
    $stmt->execute([
        ':id' => $inventoryId,
        ':change' => $quantity
    ]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Inventory created",
        "inventory_id" => $inventoryId
    ]);

} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode(["error" => $e->getMessage()]);
}
