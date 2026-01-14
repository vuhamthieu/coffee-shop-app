# Hướng Dẫn Các Chức Năng Coffee Shop App

## 📋 Tổng Quan Dự Án

**Coffee Shop App** là hệ thống quản lý quán cà phê toàn diện với:

- **Frontend**: Java GUI (JavaFX)
- **Backend**: PHP + MySQL
- **AI**: Python - Face Recognition (dlib)

---

## 🔑 CÁC CHỨC NĂNG CHÍNH VÀ CÓ ĐỊA CHỈ TẬP TIN

### 1️⃣ **QUẢN LÝ MENU**

**Nơi code:** `AdminDashboard.java` (lines 620-760)

**Chức năng:**

- ✅ Thêm/Xóa/Sửa danh mục
- ✅ Thêm/Xóa/Sửa sản phẩm
- ✅ Thay đổi giá
- ✅ Bật/tắt sản phẩm (available)
- ✅ Đánh dấu sản phẩm hot/bán chạy

**Cách hoạt động:**

```
User clicks → buildMenuTab() → TableView hiển thị
→ User chọn → Dialog form → Nhập dữ liệu
→ sendPostRequest() → Backend API
→ PHP xử lý → Database → loadProducts() refresh UI
```

**Backend URLs:**

- `GET_PRODUCTS_URL`: `/backend/api/employee/get-products.php`
- `ADD_PRODUCT_URL`: `/backend/api/admin/add_product.php`
- `DELETE_PRODUCT_URL`: `/backend/api/admin/delete_product.php`
- `UPDATE_PRODUCT_URL`: `/backend/api/admin/update_product.php`

**Key Methods:**

- `buildMenuTab()` - Xây dựng giao diện tab
- `loadProducts()` - Tải danh sách sản phẩm từ backend
- `showAddProductDialog()` - Dialog thêm sản phẩm
- `deleteProduct()` - Xóa sản phẩm

---

### 2️⃣ **QUẢN LÝ KHO (NEW - Vừa thêm)**

**Nơi code:** `AdminDashboard.java` (lines 808-851, 1915-1993)

**Chức năng:**

- ✅ **Thêm** nguyên liệu mới
- ✅ **Xóa** nguyên liệu (vừa thêm)
- ✅ **Nhập kho** (import inventory)
- ✅ **Cảnh báo** khi hết hàng (low stock)

**Cách hoạt động:**

```
User clicks "➕ Thêm nguyên liệu"
→ showAddInventoryDialog() hiện dialog
→ User nhập: Tên, Số lượng, Đơn vị
→ Validate input (kiểm tra blank, số hợp lệ)
→ JSON: {"inventory_name":"...", "quantity":100, "unit":"kg"}
→ POST tới add_inventory.php
→ Backend tính status (ok/low/out) tự động
→ loadInventory() refresh TableView
```

**Backend URLs:**

- `GET_INVENTORY_LIST_URL`: `/backend/api/admin/inventory/get-list.php`
- `ADD_INVENTORY_URL`: `/backend/api/admin/inventory/add_inventory.php` ← **VỪA THÊM NÚT**
- `DELETE_INVENTORY_URL`: `/backend/api/admin/inventory/delete_inventory.php` ← **VỪA THÊM NÚT**
- `IMPORT_INVENTORY_URL`: `/backend/api/admin/inventory/import.php`

**Key Methods:**

- `buildInventoryTab()` - Xây dựng tab kho
- `showAddInventoryDialog()` - **Dialog thêm nguyên liệu mới** (NEW)
- `deleteInventory()` - Xóa nguyên liệu (NEW)
- `loadInventory()` - Tải danh sách kho
- `loadLowStockAlerts()` - Hiển thị cảnh báo

---

### 3️⃣ **THANH TOÁN**

**Nơi code:** `PaymentScreen.java` (lines 1-700)

**Chức năng:**

- ✅ Hiển thị hóa đơn chi tiết
- ✅ Tính giảm giá (áp dụng mã coupon)
- ✅ Chọn phương thức thanh toán (tiền mặt, chuyển khoản, ví)
- ✅ Tính tax (8%), phí dịch vụ (5%)
- ✅ In hóa đơn
- ✅ In tem món

**Cách hoạt động:**

```
PaymentScreen.start()
→ buildScene() tạo UI
→ TableView hiển thị danh sách món
→ User nhập mã giảm giá → applyDiscountCode()
→ Backend validate coupon → refreshSummary() tính lại
→ User click "Hoàn tất thanh toán"
→ recordPaymentToBackend() ghi record
→ PaymentListener.onCompleted() callback
```

**Key Methods:**

- `applyDiscountCode()` - Validate mã giảm giá từ backend
- `removeCoupon()` - Xóa mã giảm giá
- `completePayment()` - Hoàn tất thanh toán
- `refreshSummary()` - Tính lại tổng tiền
- `openInvoicePdfFromBackend()` - In hóa đơn

---

### 4️⃣ **QUẢN LÝ NHÂN VIÊN**

**Nơi code:** `AdminDashboard.java` (lines 520-620)

**Chức năng:**

- ✅ Danh sách nhân viên
- ✅ Thêm/xóa nhân viên
- ✅ Phân công vai trò (Admin, Nhân viên bán hàng, Quản lý)
- ✅ Khóa/mở khóa tài khoản
- ✅ Quản lý Face ID

**Cách hoạt động:**

```
User clicks "➕ Thêm NV"
→ showAddEmployeeDialog()
→ Nhập: Tên, Username, Mật khẩu, Vai trò
→ POST tới /admin/add_employee.php
→ Backend hash password + insert DB
→ loadEmployees() refresh
```

**Backend URLs:**

- `GET_EMPLOYEES_URL`: `/backend/api/admin/get_employee.php`
- `ADD_EMPLOYEE_URL`: `/backend/api/admin/add_employee.php`
- `UPDATE_ROLE_URL`: `/backend/api/admin/employees/update-role.php`
- `LOCK_ACCOUNT_URL`: `/backend/api/admin/employees/lock-account.php`
- `ADD_FACEID_URL`: `/backend/api/admin/add_faceid.php`

---

### 5️⃣ **MÃ GIẢM GIÁ (COUPON)**

**Nơi code:** `AdminDashboard.java` (lines 850-920)

**Chức năng:**

- ✅ Tạo mã giảm giá
- ✅ Xóa mã
- ✅ Xem lịch sử sử dụng
- ✅ 2 loại: Giảm % hoặc giảm cố định

**Cách hoạt động:**

```
User clicks "➕ Tạo mã"
→ showAddCouponDialog()
→ Nhập: Mã, Loại (%, VND), Giá trị, Limit dùng
→ POST /coupons/create.php
→ loadCoupons() refresh
```

---

### 6️⃣ **BÁO CÁO & THỐNG KÊ**

**Nơi code:** `AdminDashboard.java` (lines 920-1100)

**Chức năng:**

- ✅ Doanh thu theo ngày/tuần/tháng
- ✅ Doanh thu theo ca (shift)
- ✅ Sản phẩm bán chạy nhất
- ✅ Số lượng khách hàng
- ✅ Chi phí

**Cách hoạt động:**

```
User chọn loại báo cáo + ngày/tháng/năm
→ Click "📊 Tải báo cáo"
→ loadReport() tạo URL + params
→ GET request tới backend
→ Backend query database
→ parseReportData() xử lý JSON
→ TableView hiển thị kết quả
```

---

### 7️⃣ **NHẬN DIỆN KHUÔN MẶT (Face Recognition)**

**Nơi code:**

- Python: `capture_faces.py`, `encode_faces.py`, `recognize_and_log.py`
- Java: `FaceApp.java`

**Chức năng:**

- ✅ Chụp hình khuôn mặt nhân viên (dataset)
- ✅ Encode khuôn mặt thành vector (encoding)
- ✅ Nhận diện khuôn mặt → tự động login

**Cách hoạt động:**

```
1. CAPTURE PHASE (capture_faces.py):
   - Mở camera
   - User chọn tên nhân viên
   - Chụp 30 ảnh khuôn mặt
   - Lưu vào dataset/[tên]/

2. ENCODE PHASE (encode_faces.py):
   - Dùng dlib để detect face landmarks
   - Tạo vector 128D từ mỗi ảnh
   - Lưu pickle file: encodings.pkl

3. RECOGNIZE PHASE (recognize_and_log.py):
   - Camera bật
   - Detect face trong video
   - Compare với encodings.pkl
   - Nếu match → login tự động
   - POST tới backend ghi log
```

**Dùng Library:**

- `dlib`: Face detection + encoding (19.22.99)
- `face_recognition`: Wrapper của dlib
- `opencv (cv2)`: Video capture

---

### 8️⃣ **ĐĂNG NHẬP & BẢO MẬT**

**Nơi code:** `LoginPage.java`

**Chức năng:**

- ✅ Login bằng username/password
- ✅ Login bằng khuôn mặt
- ✅ Session management
- ✅ Logout

**Cách hoạt động:**

```
Username/Password:
→ POST /login.php
→ Backend hash password + compare
→ Nếu match → set session
→ Chuyển sang Admin/Employee Dashboard

Face ID:
→ recognize_and_log.py chạy
→ Nhận diện được → tự động POST login
```

---

## ❓ CÁC CÂU HỎI PHẢN BIỆN CÓ KHẢ NĂNG THẦY HỎI

### 📌 Về QUẢN LÝ KHO (Chức năng vừa thêm):

1. **"Khi thêm nguyên liệu mới, trạng thái (status) được tính như thế nào?"**

   - **Trả lời**: Backend trong `add_inventory.php` tự tính:
     - `quantity <= 0` → status = "out"
     - `quantity < 10` → status = "low"
     - `quantity >= 10` → status = "ok"

2. **"Khi xóa nguyên liệu, database sẽ xóa những bản ghi gì?"**

   - **Trả lời**: `delete_inventory.php` xóa:
     - Tất cả `InventoryLog` có `inventory_id` tương ứng
     - Rồi xóa record trong bảng `Inventory`
     - (Transaction - xóa xong cả 2 hoặc không xóa gì)

3. **"Làm sao phân biệt giữa 'Nhập kho' vs 'Thêm nguyên liệu mới'?"**

   - **Trả lời**:
     - **Thêm mới**: Tạo nguyên liệu mới lần đầu + số lượng ban đầu (dùng button "➕ Thêm")
     - **Nhập kho**: Cập nhật số lượng nguyên liệu đã tồn tại (dùng button "📥 Nhập")

4. **"Cảnh báo low stock hiển thị ở đâu, bao giờ cập nhật?"**
   - **Trả lời**:
     - Hiển thị tại top của tab Kho (VBox cảnh báo)
     - Cập nhật khi: Load lần đầu hoặc user click "🔄 Làm mới"
     - Gọi `loadLowStockAlerts()` → GET `/inventory/get-low-stock-alert.php`

---

### 📌 Về THANH TOÁN:

5. **"Mã giảm giá được validate thế nào?"**

   - **Trả lời**:
     - Frontend gửi mã + order_total lên backend
     - Backend kiểm tra: mã tồn tại? Còn hạn dùng? Đã dùng hết limit?
     - Trả về: discount value + coupon_id
     - Frontend ghi nhớ coupon_id để xóa nếu cần

6. **"Tính tax 8% + phí dịch vụ 5% áp dụng trước hay sau giảm giá?"**

   - **Trả lời**:
     ```
     subtotal = tổng tiền hàng
     tax = subtotal * 0.08
     service = subtotal * 0.05
     total = subtotal + tax + service - appliedDiscount
     ```
     (Giảm giá áp dụng SAU)

7. **"Nếu người dùng nhập mã giảm giá rồi bỏ chọn, tiền có được hoàn lại không?"**
   - **Trả lời**:
     - Click button "Xoá mã" → `removeCoupon()`
     - Nếu có order_id hợp lệ → POST lên backend xóa
     - Refresh summary → tiền được hoàn lại

---

### 📌 Về NHÂN VIÊN & KHÓ ACCOUNT:

8. **"Khóa account nhân viên có ảnh hưởng gì?"**

   - **Trả lời**:
     - Backend set `is_active = 0`
     - Nhân viên không thể login
     - Admin có thể mở khóa bất kỳ lúc nào

9. **"Quản lý Face ID khi nào được dùng?"**
   - **Trả lời**:
     - Sau khi thêm nhân viên
     - Admin click "🔗 Thêm Face" → chạy `capture_faces.py` + `encode_faces.py`
     - Lần sau nhân viên có thể login bằng khuôn mặt

---

### 📌 Về BÁO CÁO:

10. **"Doanh thu 'theo ca' được tính từ đâu?"**

    - **Trả lời**:
      - Mỗi hóa đơn có `shift_id` (ca sáng/chiều/tối)
      - Backend tính tổng doanh thu theo `shift_id` + ngày
      - Trả về 3 dòng (sáng/chiều/tối)

11. **"Sản phẩm bán chạy là top mấy?"**
    - **Trả lời**:
      - Lấy top 10 sản phẩm
      - ORDER BY số lượng bán + tháng/năm được chọn

---

### 📌 Về FACE RECOGNITION:

12. **"Nếu có 2 người giống nhau, nhận diện khuôn mặt có bị nhầm không?"**

    - **Trả lời**:
      - Dlib sử dụng công nghệ deep learning (ResNet)
      - Tính toán 128 điểm đặc trưng khuôn mặt
      - Nếu khoảng cách Euclidean < 0.6 → là cùng 1 người
      - Rất hiếm khi nhầm (ngoài trường hợp sinh đôi hoàn toàn)

13. **"Nếu quên cập nhật Face ID, nhân viên có đăng nhập được không?"**

    - **Trả lời**:
      - CÓ - vẫn đăng nhập được bằng username/password
      - Face ID chỉ là tùy chọn tiện lợi thêm

14. **"Dữ liệu khuôn mặt lưu ở đâu?"**
    - **Trả lời**:
      - `dataset/[employee_name]/` - Ảnh gốc
      - `encodings.pkl` - Dlib encoding (vector 128D)
      - KHÔNG lưu trong database (chỉ lưu file)

---

### 📌 Về KIẾN TRÚC TỔNG QUÁT:

15. **"Frontend (Java) giao tiếp backend (PHP) bằng gì?"**

    - **Trả lời**:
      - HttpClient (Java 11+)
      - Gửi POST/GET request
      - JSON format
      - Response: `{"success":true, "data":[...], "message":"..."}`

16. **"Nếu database offline, ứng dụng có crash không?"**

    - **Trả lời**:
      - Có try-catch ở hầu hết chỗ
      - Hiển thị Alert dialog "Lỗi kết nối"
      - User có thể retry

17. **"Các mật khẩu được mã hóa bằng gì?"**
    - **Trả lời**:
      - Backend sử dụng `password_hash()` (PHP)
      - Dùng bcrypt algorithm
      - Verify bằng `password_verify()`

---

## 🎯 TRÁC XUẤT CÓ ĐỊA CHỈ

| Chức năng  | File Java                    | File PHP              | Method chính             |
| ---------- | ---------------------------- | --------------------- | ------------------------ |
| Thêm KHO   | AdminDashboard.java:1933     | add_inventory.php     | showAddInventoryDialog() |
| Xóa KHO    | AdminDashboard.java:1911     | delete_inventory.php  | deleteInventory()        |
| Thanh toán | PaymentScreen.java:300-600   | complete-payment.php  | completePayment()        |
| Mã giảm    | PaymentScreen.java:300-370   | check-coupon.php      | applyDiscountCode()      |
| Nhân viên  | AdminDashboard.java:500-620  | add_employee.php      | showAddEmployeeDialog()  |
| Báo cáo    | AdminDashboard.java:900-1100 | reports/\*.php        | loadReport()             |
| Face ID    | FaceApp.java + Python        | recognize-and-log.php | (Python script)          |

---

## 📝 MẸO TRẢ LỜI:

**Khi thầy hỏi:**

- "Code đó ở đâu?" → **Nêu file + dòng** (dùng Ctrl+G trong IDE)
- "Làm sao mà...?" → **Nêu luồng từng bước** (User → Frontend → Backend → DB → Response)
- "Sao không...?" → **Giải thích design choice** (ví dụ: "Vì muốn...")
- "Nếu...xảy ra?" → **Nêu error handling** (try-catch, validation, etc.)

---

---

**HƯỚNG DẪN DEMO CHI TIẾT (BƯỚC-TRONG-BƯỚC)**

Dưới đây là các kịch bản demo nhanh — bạn có thể làm trực tiếp khi thầy yêu cầu kiểm tra tính năng.

1. Demo Thêm Nguyên Liệu (Inventory)

- Mở `AdminDashboard` → Tab `Kho`.
- Click `➕ Thêm nguyên liệu` → dialog hiện.
- Nhập `Tên`, `Số lượng`, `Đơn vị` → OK.
- Frontend gửi POST tới:
  - URL: `/backend/api/admin/inventory/add_inventory.php`
  - Payload JSON ví dụ:

```json
{ "inventory_name": "Bột cacao", "quantity": 5, "unit": "kg" }
```

- Backend response (ví dụ):

```json
{ "success": true, "message": "Inventory created", "inventory_id": 123 }
```

- Verify: `loadInventory()` được gọi lại, bảng `inventoryTable` hiển thị dòng mới; nếu `quantity < 10` thì `status` = `low`.

Kiểm tra nhanh (cli):

```powershell
curl -X POST "http://localhost/coffee-shop-app/backend/api/admin/inventory/add_inventory.php" -H "Content-Type: application/json" -d "{\"inventory_name\":\"Bột cacao\",\"quantity\":5,\"unit\":\"kg\"}"
```

2. Demo Xóa Nguyên Liệu

- Chọn 1 dòng trong `Kho` → click `🗑️ Xóa` → Confirm.
- Frontend gửi POST tới `/backend/api/admin/inventory/delete_inventory.php` với payload:

```json
{ "inventory_id": 123 }
```

- Backend trả về:

```json
{
  "success": true,
  "message": "Inventory deleted",
  "inventory_name": "Bột cacao"
}
```

- Verify: Dòng bị xóa khỏi `inventoryTable`; database không còn record; `InventoryLog` liên quan bị xóa.

3. Demo Nhập Kho (Import)

- Click `📥 Nhập kho` → nếu UI show import dialog: nhập `inventory_id` + `quantity_added`.
- Frontend gọi API `inventory/import.php` (xem request trong code `sendPostRequest(...)`).
- Verify: quantity tăng lên, status cập nhật (out→low→ok) tùy giá trị.

4. Demo Thanh Toán: Áp dụng Coupon + Hoàn tất

- Mở `PaymentScreen` với hoá đơn mẫu.
- Nhập mã voucher vào `discountField` → click `Áp dụng mã`.
- Frontend gọi `check-coupon.php` với body:

```json
{ "code": "AURA10", "order_total": 150000 }
```

- Ví dụ response:

```json
{ "success": true, "coupon_id": 5, "discount": 15000 }
```

- Verify: `appliedDiscount` cập nhật, `refreshSummary()` hiển thị tổng mới.
- Click `Hoàn tất thanh toán` → frontend gọi `complete-payment.php` với order_id + phương thức.

CLI kiểm tra coupon (curl):

```powershell
curl -X POST "http://localhost/coffee-shop-app/backend/api/employee/check-coupon.php" -H "Content-Type: application/json" -d "{\"code\":\"AURA10\",\"order_total\":150000}"
```

5. Kiểm tra logs / lỗi nếu có

- Backend: kiểm tra file `logs/error_log.txt` hoặc PHP error_log (xampp/apache logs).
- Frontend: xem console khi chạy `runApp.bat` (có stacktrace của Java exceptions).
- DB: dùng phpMyAdmin hoặc MySQL client, check bảng `Inventory`, `InventoryLog`, `Orders`, `Coupons`.

---

NHỮNG ĐIỂM DỄ BỊ HỎI VÀ CÁC CÂU TRẢ LỜI NGẮN GỌN (SẴN SÀNG NỐI):

- "Làm sao bạn biết API đã chạy?"

  - Trả lời: Kiểm tra response JSON (success = true), kiểm tra UI đã refresh, kiểm tra DB row.

- "Nếu backend trả về lỗi 500 thì sao?"

  - Trả lời: Frontend có try-catch; showAlert("Lỗi", message) và khuyên retry; kiểm tra logs trên server (xampp/apache error log).

- "Làm sao demo nhanh nếu không có DB?"

  - Trả lời: Có thể mock bằng curl trả về JSON mẫu hoặc sửa code tạm để trả JSON giả; nhưng tốt nhất nên chạy XAMPP + DB local.

- "Làm thế nào để chứng minh quy trình hoàn tất thanh toán?"
  - Trả lời: Show `PaymentScreen` → apply coupon → complete payment → show alert "Thanh toán thành công" và kiểm tra bảng `orders`/`payments` trên DB.

---

CHECKLIST TRƯỚC KHI DEMO (3 phút kiểm tra):

- [ ] Khởi động XAMPP (Apache + MySQL)
- [ ] DB có schema + user, chạy migrations nếu cần
- [ ] Chạy `./runApp.bat AdminDashboard` (kiểm tra console không có exception)
- [ ] Mở `AdminDashboard` → Tab `Kho` → thử thêm 1 item + xóa → xác nhận trên DB
- [ ] Mở `PaymentScreen` → thử áp coupon + hoàn tất → xác nhận payment record
- [ ] Chuẩn bị lệnh curl để test nhanh nếu cần (đã có ví dụ ở trên)

---

Tôi đã cập nhật tài liệu chi tiết cho các bước demo chính. Bạn muốn tôi:

- (A) Thêm ví dụ response và câu hỏi phản biện cho các phần còn lại (Menu, Nhân viên, Báo cáo)?
- (B) Hoàn thiện một file checklist in sẵn để in ra khi trình bày?

Chọn A hoặc B hoặc cả hai, tôi sẽ tiếp tục.
