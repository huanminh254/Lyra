# Đặc tả use case — Lyra

## 1. Phạm vi hệ thống

Lyra là ứng dụng nghe nhạc Android cho phép người dùng khám phá danh sách bài hát, tìm kiếm nội dung, mở màn hình đang phát và điều khiển Media3 ExoPlayer. Dữ liệu bài hát và cấu hình người dùng được lấy từ Firebase Firestore; ảnh bìa được tải qua Coil.

Phiên bản hiện tại tập trung vào trải nghiệm khám phá và phát nhạc. Các chức năng lưu yêu thích, tải xuống thực tế và xác thực người dùng mới có màn hình/luồng giao diện hoặc đang trong lộ trình hoàn thiện.

## 2. Actor

| Actor | Vai trò |
| --- | --- |
| Người nghe | Khám phá, tìm kiếm và phát bài hát trên thiết bị Android. |
| Firebase Firestore | Cung cấp danh sách bài hát và cấu hình người dùng. |
| Firebase Storage/CDN | Lưu hoặc phân phối ảnh bìa, media theo URL cấu hình. |
| Media3 ExoPlayer | Tải, đệm và phát audio từ `audioUrl`. |

## 3. Danh sách use case

| Mã | Use case | Actor chính | Kết quả |
| --- | --- | --- | --- |
| UC-01 | Xem nội dung khám phá | Người nghe | Danh sách bài hát được hiển thị theo thứ tự `sortOrder`. |
| UC-02 | Tìm kiếm bài hát | Người nghe | Danh sách được lọc theo tên bài hát hoặc nghệ sĩ. |
| UC-03 | Phát bài hát | Người nghe, Media3 | Bài hát được tải và phát, có trạng thái buffering/lỗi. |
| UC-04 | Điều khiển trình phát | Người nghe | Play/pause, tua, chuyển bài trước/sau và vuốt chuyển bài. |
| UC-05 | Xem thông tin bài đang phát | Người nghe | Hiển thị tiêu đề, nghệ sĩ, ảnh bìa, thời lượng và tiến trình. |
| UC-06 | Xem cài đặt người dùng | Người nghe | Hiển thị tùy chọn chất lượng, Wi‑Fi, giao diện và phiên bản. |
| UC-07 | Mở khu vực yêu thích/tải xuống | Người nghe | Hiển thị các màn hình quản lý nội dung tương ứng. |

## 4. Đặc tả chi tiết

### UC-01 — Xem nội dung khám phá

**Tiền điều kiện:** Ứng dụng đã được mở; thiết bị có mạng hoặc dữ liệu đã được nạp trước đó.

**Luồng chính:**

1. Người nghe mở màn hình Discover.
2. ViewModel yêu cầu `SongRepository` lấy danh sách bài hát.
3. Repository gọi remote data source đọc collection `songs` từ Firestore.
4. Mapper chuyển document thành model `Song` của domain.
5. UI hiển thị bài nổi bật, danh sách phổ biến và mini-player nếu đang có bài phát.

**Ngoại lệ:** Nếu Firestore lỗi hoặc collection rỗng, UI hiển thị trạng thái lỗi/trống và không làm ứng dụng bị crash.

### UC-02 — Tìm kiếm bài hát

**Tiền điều kiện:** Danh sách bài hát đã có trong SearchViewModel.

**Luồng chính:**

1. Người nghe nhập từ khóa.
2. SearchViewModel chuẩn hóa từ khóa và lọc theo `title` hoặc `artist`.
3. UI cập nhật danh sách kết quả theo thời gian nhập.
4. Người nghe chọn một kết quả để mở màn hình Now Playing.

**Ngoại lệ:** Từ khóa rỗng hoặc không có kết quả thì hiển thị trạng thái hướng dẫn/tìm thấy 0 bài.

### UC-03 — Phát bài hát

**Tiền điều kiện:** Bài hát có `audioUrl` hợp lệ.

**Luồng chính:**

1. Người nghe chọn bài hát.
2. ViewModel gửi bài hát cho `AudioPlayerManager`.
3. Media3 tạo `MediaItem` từ URL và chuẩn bị ExoPlayer.
4. Player phát audio, phát các trạng thái loading, ready, playing hoặc error.
5. UI đồng bộ tiến trình và trạng thái với player.

**Ngoại lệ:** URL không hợp lệ, mất mạng hoặc format không hỗ trợ thì hiển thị lỗi phát; người nghe có thể thử lại hoặc chọn bài khác.

### UC-04 — Điều khiển trình phát

Người nghe có thể play/pause, tua trên progress bar, chuyển bài trước/sau và vuốt giữa các bài. ViewModel giữ logic điều khiển, còn `AudioPlayerManager` là lớp duy nhất giao tiếp trực tiếp với ExoPlayer.

### UC-05 — Xem thông tin bài đang phát

Now Playing nhận `Song` đã chọn, hiển thị thông tin metadata, ảnh bìa từ `coverUrl`, tiến trình hiện tại và thời lượng. Coil sử dụng ảnh placeholder khi tải ảnh thất bại.

### UC-06 — Xem cài đặt người dùng

SettingsViewModel đọc document người dùng từ collection `users` (hiện mặc định `user_001`) rồi hiển thị chất lượng audio, giới hạn Wi‑Fi, dark mode, cache và phiên bản. Việc lưu thay đổi tùy chọn chưa phải luồng hoàn chỉnh trong phiên bản hiện tại.

### UC-07 — Mở khu vực yêu thích/tải xuống

Người nghe có thể điều hướng tới Favorites hoặc Downloads từ navigation. Hai màn hình đã được tổ chức trong UI; cơ chế lưu bền vững và tải file offline là phần cần tiếp tục triển khai.

## 5. Quy tắc dữ liệu chính

- Document bài hát thuộc collection `songs` và cần có tối thiểu `title`, `artist`, `audioUrl`, `coverUrl`, `sortOrder`.
- `audioUrl` phải phát được bởi Media3 ExoPlayer.
- `coverUrl` phải truy cập được bởi Coil.
- Không đưa file media có bản quyền hoặc `google-services.json` vào repository.
- Firebase Security Rules phải giới hạn quyền đọc/ghi phù hợp với môi trường triển khai.

## 6. Yêu cầu phi chức năng

- Giao diện phản hồi trong trạng thái loading, buffering và lỗi mạng.
- Tách UI, ViewModel, repository và data source để dễ kiểm thử.
- Player cần giải phóng tài nguyên khi vòng đời màn hình/ứng dụng kết thúc.
- Chỉ sử dụng media có quyền phân phối.

