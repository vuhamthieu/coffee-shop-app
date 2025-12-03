<?php
header("Content-Type: application/json; charset=UTF-8");
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data["code"]) || !isset($data["order_total"])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'code' or 'order_total'"
    ]);
    exit;
}

$code = trim($data["code"]);
$order_total = floatval($data["order_total"]);

try {
    // Lấy coupon
    $sql = "SELECT * FROM Coupons WHERE code = :code";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([":code" => $code]);
    $coupon = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$coupon) {
        echo json_encode(["success" => false, "message" => "Invalid coupon code"]);
        exit;
    }

    // Kiểm tra thời gian
    $now = date("Y-m-d H:i:s");
    if ($coupon["coupon_start_date"] && $now < $coupon["coupon_start_date"]) {
        echo json_encode(["success" => false, "message" => "Coupon not started"]);
        exit;
    }
    if ($coupon["coupon_end_date"] && $now > $coupon["coupon_end_date"]) {
        echo json_encode(["success" => false, "message" => "Coupon expired"]);
        exit;
    }

    // Kiểm tra min_order
    if ($order_total < $coupon["min_order"]) {
        echo json_encode([
            "success" => false,
            "message" => "Order does not meet minimum requirement"
        ]);
        exit;
    }

    // Kiểm tra usage_limit
    if ($coupon["usage_limit"] <= 0) {
        echo json_encode([
            "success" => false,
            "message" => "Coupon usage limit reached"
        ]);
        exit;
    }

    // Tính discount
    $discount = 0;
    if ($coupon["coupon_type"] === "percent") {
        $discount = $order_total * ($coupon["coupon_value"] / 100);
    } else if ($coupon["coupon_type"] === "amount") {
        $discount = $coupon["coupon_value"];
    }

    echo json_encode([
        "success" => true,
        "message" => "Coupon valid",
        "data" => [
            "coupon_id" => $coupon["id"],
            "discount"  => $discount,
            "type"      => $coupon["coupon_type"],
            "value"     => $coupon["coupon_value"]
        ]
    ]);

} catch(Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Server error",
        "error" => $e->getMessage()
    ]);
}
?>
