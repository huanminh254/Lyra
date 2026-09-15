## 6. Data flow: gửi từ đâu, đến đâu

```text
Firestore songs/{songId}
        ↓ đọc bởi SongRemoteDataSource
SongEntity
        ↓ SongMapper.toSong()
Domain Song
        ↓ Repository → ViewModel
LiveData / UI state
        ↓ người dùng chọn bài
AudioPlayerManager → Media3 ExoPlayer
```

Nguồn dữ liệu hiện tại trong project:

- Metadata bài hát: Firebase Firestore collection `songs`.
- User profile/settings: Firestore document `users/user_001` ở bản hiện tại.
- Audio: `audioUrl` trực tiếp vào Media3 ExoPlayer.
- Cover: `coverUrl` tải qua Coil.
- Favorites/downloads/settings persistence: cần hoàn thiện; không giả lập như đã lưu nếu chưa persist thành công.

## 7. JSON/data contract

### 7.1 Song document — Firestore `songs/{songId}`

```json
{
  "title": "Example Song",
  "artist": "Example Artist",
  "currentTime": "00:00",
  "duration": "03:42",
  "audioUrl": "https://cdn.example.com/audio/example-song.mp3",
  "coverUrl": "https://cdn.example.com/covers/example-song.jpg",
  "audioObjectPath": "audio/example-song.mp3",
  "coverObjectPath": "covers/example-song.jpg",
  "originalFileName": "example-song.mp3",
  "sizeBytes": 5242880,
  "sortOrder": 1,
  "sourceUrl": "https://example.com/source",
  "genre": "Pop",
  "year": "2026"
}
```

Field rules:

| Field | Type | Required | Rule |
| --- | --- | --- | --- |
| `title` | string | Có | Không rỗng; fallback `Unknown title` khi dữ liệu hỏng |
| `artist` | string | Có | Không rỗng; fallback `Unknown artist` |
| `audioUrl` | string | Có để phát stream | HTTPS hoặc local path hợp lệ |
| `coverUrl` | string | Không | URL ảnh hợp lệ; lỗi dùng placeholder |
| `sortOrder` | number | Có | Dùng để order ổn định, nhỏ hơn đứng trước |
| `currentTime` | string | Không | Chỉ là display fallback, không dùng làm player clock |
| `duration` | string | Không | Player duration thực tế là nguồn chính |
| `genre` | string | Không | Dùng cho filter khi có |
| `year` | string/number | Không | Normalize thành string ở domain |

`id` lấy từ document ID, không tin một field `id` gửi trong document nếu khác document ID.

### 7.2 User profile — Firestore `users/{userId}`

```json
{
  "name": "Listener",
  "accountSubtitle": "Free account",
  "audioQuality": "High",
  "streamOnlyOnWifi": false,
  "darkModeEnabled": true,
  "cacheSubtitle": "120 MB used",
  "appVersion": "1.0.0"
}
```

Rule:

- `userId` lấy từ authentication khi có auth; hiện tại fallback là `user_001`.
- Client chỉ được đọc/sửa document của user hiện tại.
- Không lưu password, token, API key hoặc thông tin nhạy cảm trong document này.

### 7.3 Favorite record — đề xuất `users/{userId}/favorites/{songId}`

```json
{
  "songId": "song_001",
  "createdAt": "2026-09-11T01:00:00Z"
}
```

`songId` là document ID của `songs`. Favorite là quan hệ user–song, không thêm `isFavorite` vào song catalog dùng chung.

### 7.4 Download record — local database

```json
{
  "songId": "song_001",
  "status": "COMPLETED",
  "progress": 1.0,
  "localAudioPath": "/app/files/audio/song_001.mp3",
  "bytesDownloaded": 5242880,
  "totalBytes": 5242880,
  "errorMessage": null,
  "updatedAt": "2026-09-11T01:00:00Z"
}
```

Giá trị `status`: `QUEUED`, `DOWNLOADING`, `COMPLETED`, `FAILED`, `CANCELLED`.

Chỉ `COMPLETED` với file tồn tại và kích thước hợp lệ mới được phát offline.

### 7.5 Playback UI state — không gửi lên Firestore

```json
{
  "songId": "song_001",
  "isPlaying": true,
  "playbackState": "READY",
  "currentPositionMs": 42300,
  "durationMs": 222000,
  "playlistIndex": 0,
  "hasPrevious": false,
  "hasNext": true,
  "errorMessage": null
}
```

State này thuộc `NowPlayingViewModel` và `AudioPlayerManager`. Không tạo request mạng mỗi lần progress thay đổi.

## 8. Action/event contract

| Event | Sender | Destination | Payload chính |
| --- | --- | --- | --- |
| `catalog.load` | ViewModel | `SongRepository` → Firestore | không có |
| `song.play` | UI | ViewModel → ExoPlayer | `songId`, `playlist`, `index` |
| `player.seek` | SeekBar | ViewModel → ExoPlayer | `positionMs` |
| `favorite.set` | UI | Favorite repository | `userId`, `songId`, `value` |
| `download.start` | UI | Download manager | `songId`, `audioUrl` |
| `settings.update` | UI | Settings repository | `userId`, `key`, `value` |

Logical event format khi cần log hoặc analytics:

```json
{
  "type": "favorite.set",
  "version": 1,
  "source": "android",
  "userId": "user_001",
  "occurredAt": "2026-09-11T01:00:00Z",
  "data": {
    "songId": "song_001",
    "value": true
  }
}
```

Rule event:

- `type`, `version`, `source`, `occurredAt` bắt buộc nếu event được persist hoặc gửi ra ngoài.
- `occurredAt` dùng ISO-8601 UTC.
- Không gửi URL private, token hoặc dữ liệu người dùng không cần thiết.
- Playback progress chỉ giữ local; không gửi event liên tục.

## 9. Error contract và UI state

Mọi nguồn dữ liệu ngoài phải có đủ bốn trạng thái:

```json
{
  "status": "ERROR",
  "data": [],
  "message": "Không tải được danh sách bài hát",
  "retryable": true
}
```

Giá trị `status`: `IDLE`, `LOADING`, `CONTENT`, `EMPTY`, `ERROR`.

Rule:

- Error message cho người dùng phải dễ hiểu; exception raw chỉ dùng cho log debug.
- Retry không tạo request trùng hoặc nhiều job chạy song song.
- Dữ liệu cũ có thể giữ lại khi refresh lỗi, nhưng phải báo rằng dữ liệu đang stale nếu UX cần.
- Không được crash vì thiếu cover, thiếu duration, URL hỏng hoặc catalog rỗng.

## 10. Mapping vào code hiện tại

| Design responsibility | Code hiện tại |
| --- | --- |
| Root navigation | `MainActivity` |
| Discover | `DiscoverFragment`, `fragment_discover.xml` |
| Search | `SearchFragment`, `fragment_search.xml` |
| Now Playing | `NowPlayingFragment`, `NowPlayingViewModel` |
| Favorites | `FavoritesFragment` — UI/WIP |
| Downloads | `DownloadsFragment` — UI/WIP |
| Settings | `SettingsFragment`, `SettingsViewModel` |
| Catalog | `SongRemoteDataSource` → `SongRepositoryImpl` |
| Playback | `AudioPlayerManager` dùng Media3 ExoPlayer |
| Shared mini player | `MiniPlayerBinder` |

Các phần cần bổ sung để khớp đầy đủ Stitch/spec:

- Songs Library và Albums screen.
- Persist favorites.
- Download manager và phát offline.
- Persist settings.
- Background playback/media session nếu muốn phát khi app ở background.

## 11. Definition of Done cho Developer

Một màn hình chỉ được xem là hoàn tất khi:

- Layout XML khớp hierarchy và visual direction của Stitch.
- Có loading, content, empty và error state phù hợp.
- Có accessibility label cho icon-only control.
- Dữ liệu đi đúng source → data source → repository → ViewModel → UI.
- Không hard-code dữ liệu production trong Fragment.
- Không crash khi dữ liệu null, rỗng, URL hỏng hoặc mất mạng.
- Navigation không tạo vòng lặp hoặc duplicate back stack.
- Player dùng một instance chung và mini player đồng bộ.
- Test/build chạy thành công trên thiết bị Android baseline.

## 12. Change log

| Date | Owner | Change |
| --- | --- | --- |
| 2026-09-11 | Design Department | Tạo design–dev handoff dựa trên Stitch và codebase hiện tại. |
