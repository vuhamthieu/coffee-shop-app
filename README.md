# coffee-shop-app

## Chạy JavaFX app

Kho chứa đã kèm theo thư mục `lib/` chứa JavaFX SDK. Để build và chạy:

### Windows (PowerShell/CMD)
```bat
runApp.bat
```
Script sẽ:
1. Compile `App.java` với module-path trỏ vào `lib/`.
2. Chạy app bằng cùng module-path.

### Nếu muốn chạy file khác
```bat
runApp.bat PaymentScreen
```
Sẽ launch lớp `PaymentScreen` thay vì `App`.

### VS Code / IDE khác
- Tạo launch config với `vmArgs`: `--module-path ${workspaceFolder}/lib --add-modules javafx.controls,javafx.fxml`
- Đặt `mainClass` theo file muốn chạy (`App` hoặc `PaymentScreen`).
