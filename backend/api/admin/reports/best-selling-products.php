<?php
include __DIR__ . '/../../../connect.php';

$year = $_GET['year'] ?? date('Y');
$month = $_GET['month'] ?? date('m');
$limit = $_GET['limit'] ?? 10;

$sql = "SELECT 
        p.id, p.product_name, COUNT(oi.id) as sold_quantity, SUM(oi.quantity) as total_qty
        FROM OrderItems oi
        JOIN Products p ON oi.product_id = p.id
        JOIN Orders o ON oi.order_id = o.id
        WHERE YEAR(o.created_at) = :year 
        AND MONTH(o.created_at) = :month
        AND o.order_status = 'paid'
        GROUP BY p.id, p.product_name
        ORDER BY sold_quantity DESC
        LIMIT :limit";

$stmt = $pdo->prepare($sql);
$stmt->bindParam(':year', $year, PDO::PARAM_INT);
$stmt->bindParam(':month', $month, PDO::PARAM_INT);
$stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
$stmt->execute();

$products = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    "success" => true,
    "year" => $year,
    "month" => $month,
    "data" => $products
]);
