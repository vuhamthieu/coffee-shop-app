<?php

include __DIR__ . '/../../connect.php';

try {
    // Lấy tất cả order chưa thanh toán
    $stmt = $pdo->prepare("
        SELECT o.id, o.table_id, o.employee_id, o.total, o.order_status, o.created_at,
               t.table_name, e.employee_name
        FROM Orders o
        LEFT JOIN Tables t ON o.table_id = t.id
        LEFT JOIN Employees e ON o.employee_id = e.id
        WHERE o.order_status = 'unpaid'
        ORDER BY o.created_at ASC
    ");
    $stmt->execute();
    $orders = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "orders" => $orders
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to get unpaid orders",
        "error" => $e->getMessage()
    ]);
}
?>
