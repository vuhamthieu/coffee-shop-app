<?php
include __DIR__ . '/../../connect.php';

$sql = "SELECT e.id, e.employee_name,e.phone, e.username, e.role_id, e.active 
        FROM Employees e";

$stmt = $pdo->prepare($sql);
$stmt->execute();
$employees = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode(["data" => $employees]);
?>