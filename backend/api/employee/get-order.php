<?php

include __DIR__ . '/../../connect.php';

$order_id = isset($_GET['order_id']) ? intval($_GET['order_id']) : 0;

if ($order_id <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Missing or invalid 'order_id'"
    ]);
    exit;
}

try {
    // Lấy thông tin order
    $stmt_order = $pdo->prepare("SELECT o.id, o.table_id, o.employee_id, o.total, o.order_status, o.created_at, o.paid_at,
                                        t.table_name, e.employee_name
                                 FROM Orders o
                                 LEFT JOIN Tables t ON o.table_id = t.id
                                 LEFT JOIN Employees e ON o.employee_id = e.id
                                 WHERE o.id = :order_id");
    $stmt_order->execute([":order_id" => $order_id]);
    $order = $stmt_order->fetch(PDO::FETCH_ASSOC);

    if (!$order) {
        echo json_encode(["success" => false, "message" => "Order not found"]);
        exit;
    }

    // Lấy danh sách order items
    $stmt_items = $pdo->prepare("SELECT oi.id, oi.product_id, p.product_name, oi.quantity, oi.price, oi.note
                                 FROM OrderItems oi
                                 LEFT JOIN Products p ON oi.product_id = p.id
                                 WHERE oi.order_id = :order_id");
    $stmt_items->execute([":order_id" => $order_id]);
    $items = $stmt_items->fetchAll(PDO::FETCH_ASSOC);

    // Lấy coupon (nếu có)
    $stmt_coupon = $pdo->prepare("SELECT c.code, c.coupon_type, c.coupon_value, oc.discount
                                  FROM OrderCoupons oc
                                  LEFT JOIN Coupons c ON oc.coupon_id = c.id
                                  WHERE oc.order_id = :order_id");
    $stmt_coupon->execute([":order_id" => $order_id]);
    $coupons = $stmt_coupon->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "order" => $order,
        "items" => $items,
        "coupons" => $coupons
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to get order",
        "error" => $e->getMessage()
    ]);
}
?>
