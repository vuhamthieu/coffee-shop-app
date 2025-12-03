<?php

include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['order_id'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'order_id'"
    ]);
    exit;
}

$order_id = intval($data['order_id']);

try {
    $pdo->beginTransaction();

    // Xóa OrderCoupons
    $stmt = $pdo->prepare("DELETE FROM OrderCoupons WHERE order_id = :order_id");
    $stmt->execute([":order_id" => $order_id]);

    // Xóa OrderItems
    $stmt = $pdo->prepare("DELETE FROM OrderItems WHERE order_id = :order_id");
    $stmt->execute([":order_id" => $order_id]);

    // Xóa Orders
    $stmt = $pdo->prepare("DELETE FROM Orders WHERE id = :order_id");
    $stmt->execute([":order_id" => $order_id]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Order deleted successfully"
    ]);

} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode([
        "success" => false,
        "message" => "Failed to delete order",
        "error" => $e->getMessage()
    ]);
}
?>
