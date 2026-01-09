<?php
include __DIR__ . '/../../../connect.php';

$year = $_GET['year'] ?? date('Y');
$month = $_GET['month'] ?? date('m');

// Note: Expenses would need to be tracked in a separate Expenses table
// This is a placeholder for querying operational expenses

$sql = "SELECT 
        COALESCE(SUM(s.total_salary), 0) as salary_expenses,
        COUNT(DISTINCT s.employee_id) as employee_count
        FROM Salary s
        WHERE s.salary_year = :year 
        AND s.salary_month = :month";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':year' => $year,
    ':month' => $month
]);

$expenses = $stmt->fetch(PDO::FETCH_ASSOC);

// Get revenue for comparison
$revenueSql = "SELECT SUM(total) as total_revenue FROM Orders 
               WHERE YEAR(created_at) = :year 
               AND MONTH(created_at) = :month
               AND order_status = 'paid'";

$revenueStmt = $pdo->prepare($revenueSql);
$revenueStmt->execute([
    ':year' => $year,
    ':month' => $month
]);

$revenue = $revenueStmt->fetch(PDO::FETCH_ASSOC);

$salary_expenses = $expenses['salary_expenses'] ?? 0;
$total_revenue = $revenue['total_revenue'] ?? 0;
$profit = $total_revenue - $salary_expenses;
$profit_margin = $total_revenue > 0 ? ($profit / $total_revenue * 100) : 0;

echo json_encode([
    "success" => true,
    "year" => $year,
    "month" => $month,
    "salary_expenses" => $salary_expenses,
    "employee_count" => $expenses['employee_count'] ?? 0,
    "total_revenue" => $total_revenue,
    "profit" => $profit,
    "profit_margin" => round($profit_margin, 2)
]);
