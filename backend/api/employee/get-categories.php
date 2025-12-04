<?php

include __DIR__ . '/../../connect.php';

try {
    $sql = "SELECT id, category_name 
            FROM Categories
            ORDER BY category_name ASC";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $categories = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "data" => $categories
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to fetch categories",
        "error" => $e->getMessage()
    ]);
}
?>
