<?php
include __DIR__ . '/../../../connect.php';

if (empty($_GET['coupon_id'])) {
    echo json_encode(["error" => "coupon_id is required"]);
    exit;
}

$coupon_id = $_GET['coupon_id'];

$sql = "SELECT 
        o.id as order_id,
        o.created_at,
        o.total,
        oc.discount
        FROM OrderCoupons oc
        JOIN Orders o ON oc.order_id = o.id
        WHERE oc.coupon_id = :coupon_id
        ORDER BY o.created_at DESC";

$stmt = $pdo->prepare($sql);
$stmt->execute([':coupon_id' => $coupon_id]);

$usages = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    "success" => true,
    "data" => $usages,
    "total_usage" => count($usages)
]);
