<?php
include __DIR__ . '/../../../connect.php';

$sql = "SELECT id, inventory_name, quantity, unit, inventory_status FROM Inventory ORDER BY inventory_name ASC";
$stmt = $pdo->prepare($sql);
$stmt->execute();

$ingredients = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    "success" => true,
    "data" => $ingredients
]);
