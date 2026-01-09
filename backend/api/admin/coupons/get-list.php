<?php
include __DIR__ . '/../../../connect.php';

$sql = "SELECT 
        c.id, 
        c.code, 
        c.coupon_type, 
        c.coupon_value,
        c.min_order,
        c.coupon_start_date,
        c.coupon_end_date,
        c.usage_limit,
        COUNT(oc.id) as usage_count
        FROM Coupons c
        LEFT JOIN OrderCoupons oc ON c.id = oc.coupon_id
        GROUP BY c.id, c.code, c.coupon_type, c.coupon_value, c.min_order, c.coupon_start_date, c.coupon_end_date, c.usage_limit
        ORDER BY c.id DESC";

$stmt = $pdo->prepare($sql);
$stmt->execute();

$coupons = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    "success" => true,
    "data" => $coupons
]);
