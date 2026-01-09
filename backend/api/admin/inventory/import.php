<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['inventory_id']) || empty($data['quantity'])) {
    echo json_encode(["error" => "inventory_id and quantity are required"]);
    exit;
}

$quantity = $data['quantity'];
$note = $data['note'] ?? 'Nhập kho';

$pdo->beginTransaction();

try {
    // Update inventory quantity
    $updateSql = "UPDATE Inventory SET quantity = quantity + :quantity WHERE id = :id";
    $updateStmt = $pdo->prepare($updateSql);
    $updateStmt->execute([
        ':quantity' => $quantity,
        ':id' => $data['inventory_id']
    ]);

    // Add to log
    $logSql = "INSERT INTO InventoryLog (inventory_id, log_change, log_type, note) 
               VALUES (:inventory_id, :log_change, 'import', :note)";
    $logStmt = $pdo->prepare($logSql);
    $logStmt->execute([
        ':inventory_id' => $data['inventory_id'],
        ':log_change' => $quantity,
        ':note' => $note
    ]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Inventory imported successfully"
    ]);
} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode(["error" => "Failed to import inventory: " . $e->getMessage()]);
}
