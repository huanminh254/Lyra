# Lyra

Lyra là ứng dụng nghe nhạc Android được xây dựng bằng Kotlin và XML. Ứng dụng cho phép người dùng duyệt danh mục bài hát, tìm kiếm bài hát hoặc nghệ sĩ và điều khiển phát nhạc thông qua màn hình Now Playing.

> Đây là dự án Android cá nhân. Phiên bản hiện tại tập trung vào duyệt danh mục, phát nhạc trực tuyến, xác thực người dùng và quản lý lượt xem.

## Chức năng

- Duyệt danh mục bài hát từ Firebase Firestore.
- Hiển thị các bài hát Popular và Top Songs.
- Tìm kiếm theo tên bài hát hoặc nghệ sĩ.
- Mở màn hình Now Playing riêng.
- Phát, tạm dừng, tua và chuyển bài hát.
- Điều khiển phát nhạc từ Mini Player.
- Vuốt trái/phải trong màn hình Now Playing để chuyển bài.
- Hiển thị ảnh bìa cùng trạng thái loading, lỗi và danh sách rỗng.
- Đăng nhập, đăng ký và đặt lại mật khẩu bằng Firebase Authentication.
- Lưu bài hát yêu thích theo từng tài khoản người dùng.
- Cho phép người dùng tải bài hát lên Supabase Storage.
- Tính một lượt xem cho mỗi user trên mỗi bài hát sau khi nghe đủ 15 giây.
- Điều hướng bằng radial menu gồm Discover, Favorites, Downloads và Account.

Favorites và Downloads hiện vẫn đang được phát triển; Downloads chưa hỗ trợ tải nhạc offline hoàn chỉnh.

## Công nghệ sử dụng

- Kotlin
- XML layouts, ViewBinding, Fragments và RecyclerView
- Material Components for Android
- MVVM với ViewModel và LiveData
- Hilt Dependency Injection
- Repository-based data layer
- Firebase Authentication và Firestore
- Supabase Storage cho file MP3 và ảnh bìa
- AndroidX Media3 ExoPlayer
- Coil 3 với OkHttp
- Kotlin Coroutines
- JUnit, AndroidX Test và Espresso

## Kiến trúc

Ứng dụng sử dụng kiến trúc MVVM kết hợp Repository và Hilt:

```text
Firebase Authentication ─┐
Firebase Firestore ──────┼──► Remote data source
Supabase Storage ────────┘       │
                                  ▼
                              Repository
        │
        ▼
ViewModel ───────────────► XML UI
        │
        ▼
Media3 ExoPlayer
```

Các package chính:

```text
app/src/main/java/com/devpro/sound/
├── data/
│   ├── mapper/
│   ├── model/
│   ├── remote/
│   └── repository/
│       └── impl/
├── player/
└── ui/
    ├── components/
    ├── account/
    ├── auth/
    ├── discover/
    ├── downloads/
    ├── favorites/
    ├── nowplaying/
    └── search/
```

## Yêu cầu

- Android Studio
- JDK 11
- Android SDK 37
- Android API 24 trở lên
- Một Firebase project đã bật Authentication và Firestore
- Một Supabase project có bucket Storage

## Bắt đầu sử dụng

Clone repository:

```bash
git clone https://github.com/huanminh254/Lyra.git
cd Lyra
```

Mở dự án bằng Android Studio và đặt file cấu hình Firebase tại:

```text
app/google-services.json
```

Tạo file `supabase.properties` ở thư mục gốc từ mẫu `supabase.properties.example`, sau đó điền publishable/anon key của project Supabase. File này đã được gitignore.

Sau đó đồng bộ Gradle và chạy cấu hình `app` trên máy ảo hoặc thiết bị Android.

Không commit `google-services.json`, `supabase.properties` hoặc các thông tin xác thực riêng tư. Không dùng Supabase `service_role key` trong ứng dụng Android.

### Cấu hình Supabase Storage

Luồng upload sử dụng bucket trong `supabase.properties`:

```properties
supabase.url=https://uetfxxexepywuyqbbtwn.supabase.co
supabase.anonKey=YOUR_PUBLISHABLE_OR_ANON_KEY
supabase.audioBucket=covers
supabase.coverBucket=covers
supabase.coverPrefix=vpop
```

Bucket `covers` hiện đã được xác nhận là public. Nếu tạo bucket riêng cho MP3, đổi `supabase.audioBucket` sang tên bucket đó. Bucket cần có Storage policy cho phép role mà client sử dụng thực hiện `INSERT`; Firebase Auth không tự biến Firebase token thành Supabase Auth token.

## Dữ liệu Firestore

Ứng dụng đọc bài hát từ collection `songs`. Một document bài hát có thể gồm:

```json
{
  "title": "Example Song",
  "artist": "Example Artist",
  "audioUrl": "https://example.com/audio.mp3",
  "coverUrl": "https://example.com/cover.jpg",
  "sourceUrl": "https://example.com/source",
  "genre": "Pop",
  "year": 2026,
  "sortOrder": 1,
  "viewCount": 0
}
```

Khi user nghe đủ 15 giây, ứng dụng tạo dấu xem tại:

```text
songs/{songId}/viewers/{userId}
```

Transaction Firestore sẽ chỉ tăng `viewCount` một lần cho mỗi cặp `userId` và `songId`. Rules nằm trong file `firestore.rules` và cần được deploy lên Firebase.

Dữ liệu người dùng được lưu trong document:

```text
users/{userId}
```

Danh sách yêu thích hiện có trong user document là `favoriteSongIds`. Các bài đã đăng được truy vấn trực tiếp từ `songs.ownerId`, nên không cần duy trì thêm danh sách ID trong user document.

## Build và kiểm thử

Chạy unit test:

```bash
./gradlew test
```

Chạy kiểm tra lint:

```bash
./gradlew lint
```

Build debug APK:

```bash
./gradlew assembleDebug
```

Chạy instrumented test trên thiết bị hoặc máy ảo đã kết nối:

```bash
./gradlew connectedAndroidTest
```

## Trạng thái dự án

Đã hoàn thành:

- Giao diện Android XML với ViewBinding.
- Màn Discover, Search, Login, Account và Now Playing.
- Danh mục bài hát từ Firestore.
- Phát nhạc bằng Media3 ExoPlayer.
- Mini Player và các component dùng chung cho danh sách bài hát.
- Xác thực email/password bằng Firebase.
- Yêu thích và upload bài hát theo user.
- Theo dõi lượt xem theo user.

Đang lên kế hoạch:

- Lưu bài hát tải xuống bằng Room.
- Hoàn thiện chức năng nghe nhạc offline.
- Sắp xếp Popular theo lượt xem thực tế.
- Thêm background playback và MediaSession.
- Thêm bình luận realtime theo thời gian bài hát.
- Hoàn thiện các luồng Favorites và Downloads.

## Tài liệu

- [Use cases](docs/use-cases.md)
- [Architecture diagram](docs/diagrams/lyra-architecture.svg)

## Bản quyền nội dung

Lyra không đóng gói nhạc có bản quyền. Audio, ảnh bìa và metadata phải là nội dung do bạn sở hữu hoặc được phép sử dụng.

## Giấy phép

Hiện dự án chưa thêm giấy phép mã nguồn mở. Hãy liên hệ chủ repository trước khi phân phối hoặc sử dụng lại dự án.
