<?php

include __DIR__ . '/../../connect.php';

try {

    $sql = "SELECT id, inventory_name, quantity, unit, inventory_status 
            FROM Inventory 
            ORDER BY inventory_name ASC";

    $stmt = $pdo->prepare($sql);
    $stmt->execute();

    $inventory = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "inventory" => $inventory
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to fetch inventory",
        "error" => $e->getMessage()
    ]);
}
?>
