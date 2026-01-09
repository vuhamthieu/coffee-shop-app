<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['inventory_id']) || $data['quantity'] === null) {
    echo json_encode(["error" => "inventory_id and quantity are required"]);
    exit;
}

$quantity = $data['quantity'];
$note = $data['note'] ?? 'Xuất kho';

$pdo->beginTransaction();

try {
    // Update inventory quantity
    $updateSql = "UPDATE Inventory SET quantity = quantity - :quantity WHERE id = :id AND quantity >= :quantity";
    $updateStmt = $pdo->prepare($updateSql);
    $updateStmt->execute([
        ':quantity' => $quantity,
        ':id' => $data['inventory_id']
    ]);

    if ($updateStmt->rowCount() === 0) {
        throw new Exception("Insufficient inventory quantity");
    }

    // Add to log
    $logSql = "INSERT INTO InventoryLog (inventory_id, log_change, log_type, note) 
               VALUES (:inventory_id, :log_change, 'export', :note)";
    $logStmt = $pdo->prepare($logSql);
    $logStmt->execute([
        ':inventory_id' => $data['inventory_id'],
        ':log_change' => -$quantity,
        ':note' => $note
    ]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Inventory exported successfully"
    ]);
} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode(["error" => $e->getMessage()]);
}
