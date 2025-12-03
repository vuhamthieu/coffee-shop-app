<?php

include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['order_id']) || !isset($data['coupon_id'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'order_id' or 'coupon_id'"
    ]);
    exit;
}

$order_id = intval($data['order_id']);
$coupon_id = intval($data['coupon_id']);

try {
    // Xóa coupon khỏi order
    $stmt_delete = $pdo->prepare("DELETE FROM OrderCoupons 
                                  WHERE order_id = :order_id AND coupon_id = :coupon_id");
    $stmt_delete->execute([
        ":order_id" => $order_id,
        ":coupon_id" => $coupon_id
    ]);

    // Cập nhật lại tổng tiền order
    $stmt_total = $pdo->prepare("SELECT SUM(price * quantity) AS total FROM OrderItems WHERE order_id = :order_id");
    $stmt_total->execute([":order_id" => $order_id]);
    $total = $stmt_total->fetch(PDO::FETCH_ASSOC)['total'];
    if (!$total) $total = 0;

    $stmt_update_order = $pdo->prepare("UPDATE Orders SET total = :total WHERE id = :order_id");
    $stmt_update_order->execute([
        ":total" => $total,
        ":order_id" => $order_id
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Coupon removed successfully",
        "total" => $total
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to remove coupon",
        "error" => $e->getMessage()
    ]);
}
?>
