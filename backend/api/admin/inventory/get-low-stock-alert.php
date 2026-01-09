<?php
include __DIR__ . '/../../../connect.php';

// Get all ingredients with status "low" or "out"
$sql = "SELECT id, inventory_name, quantity, unit, inventory_status 
        FROM Inventory 
        WHERE inventory_status IN ('low', 'out')
        ORDER BY inventory_status DESC, quantity ASC";

$stmt = $pdo->prepare($sql);
$stmt->execute();

$alerts = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    "success" => true,
    "data" => $alerts,
    "count" => count($alerts)
]);
