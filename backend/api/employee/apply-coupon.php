<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data["order_id"]) || !isset($data["coupon_id"]) || !isset($data["discount"])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'order_id', 'coupon_id' or 'discount'"
    ]);
    exit;
}

$order_id  = intval($data["order_id"]);
$coupon_id = intval($data["coupon_id"]);
$discount  = floatval($data["discount"]);

try {
    $pdo->beginTransaction();

    // Lưu vào OrderCoupons
    $sql = "INSERT INTO OrderCoupons (order_id, coupon_id, discount)
            VALUES (:order_id, :coupon_id, :discount)";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ":order_id" => $order_id,
        ":coupon_id" => $coupon_id,
        ":discount" => $discount
    ]);

    // Trừ usage_limit
    $sql = "UPDATE Coupons
            SET usage_limit = usage_limit - 1
            WHERE id = :id AND usage_limit > 0";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([":id" => $coupon_id]);

    // Cập nhật lại tổng tiền order
    // Lấy tổng tiền từ OrderItems
    $stmt_total = $pdo->prepare("SELECT SUM(price * quantity) AS subtotal 
                                 FROM OrderItems 
                                 WHERE order_id = :order_id");
    $stmt_total->execute([":order_id" => $order_id]);
    $subtotal = $stmt_total->fetch(PDO::FETCH_ASSOC)['subtotal'];
    if (!$subtotal) $subtotal = 0;

    // Lấy tổng discount hiện có
    $stmt_discount = $pdo->prepare("SELECT SUM(discount) AS total_discount 
                                    FROM OrderCoupons 
                                    WHERE order_id = :order_id");
    $stmt_discount->execute([":order_id" => $order_id]);
    $total_discount = $stmt_discount->fetch(PDO::FETCH_ASSOC)['total_discount'];
    if (!$total_discount) $total_discount = 0;

    // Tính total mới
    $total = max($subtotal - $total_discount, 0);

    // Cập nhật Orders.total
    $stmt_update_order = $pdo->prepare("UPDATE Orders SET total = :total WHERE id = :order_id");
    $stmt_update_order->execute([
        ":total" => $total,
        ":order_id" => $order_id
    ]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Coupon applied successfully",
        "total" => $total
    ]);

} catch(Exception $e) {
    $pdo->rollBack();
    echo json_encode([
        "success" => false,
        "message" => "Failed to apply coupon",
        "error" => $e->getMessage()
    ]);
}
?>
