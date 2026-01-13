<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (
    empty($data['inventory_id']) ||
    !isset($data['change']) ||
    empty($data['log_type'])
) {
    echo json_encode(["error" => "inventory_id, change, log_type are required"]);
    exit;
}

$inventory_id = $data['inventory_id'];
$change = (double)$data['change'];
$log_type = $data['log_type']; // import | export | adjust
$note = $data['note'] ?? null;

/* Lấy inventory */
$stmt = $pdo->prepare("SELECT quantity FROM Inventory WHERE id = :id");
$stmt->execute([':id' => $inventory_id]);
$inventory = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$inventory) {
    echo json_encode(["error" => "Inventory not found"]);
    exit;
}

$newQty = $inventory['quantity'] + $change;
if ($newQty < 0) $newQty = 0;

/* Tính status */
if ($newQty <= 0) {
    $status = "out";
} elseif ($newQty < 10) {
    $status = "low";
} else {
    $status = "ok";
}

try {
    $pdo->beginTransaction();

    // Update Inventory
    $stmt = $pdo->prepare("
        UPDATE Inventory
        SET quantity = :qty, inventory_status = :status
        WHERE id = :id
    ");
    $stmt->execute([
        ':qty' => $newQty,
        ':status' => $status,
        ':id' => $inventory_id
    ]);

    // Insert log
    $stmt = $pdo->prepare("
        INSERT INTO InventoryLog (inventory_id, log_change, log_type, note)
        VALUES (:id, :change, :type, :note)
    ");
    $stmt->execute([
        ':id' => $inventory_id,
        ':change' => $change,
        ':type' => $log_type,
        ':note' => $note
    ]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Inventory updated",
        "new_quantity" => $newQty,
        "status" => $status
    ]);

} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode(["error" => $e->getMessage()]);
}
