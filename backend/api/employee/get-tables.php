<?php

include __DIR__ . '/../../connect.php';

try {
    // Lấy toàn bộ bàn
    $sql = "SELECT id, table_name, table_status FROM Tables ORDER BY id ASC";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $tables = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => true,
        "message" => "Tables retrieved successfully",
        "data" => $tables
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to fetch tables",
        "error" => $e->getMessage()
    ]);
}
?>
