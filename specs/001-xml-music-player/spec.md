# Feature Specification: Lyra Music Player Rebuild

**Feature Branch**: `001-xml-music-player`  
**Created**: 2026-09-10  
**Status**: Draft  
**Input**: User description: "Viết đặc tả feature cho toàn bộ dự án Lyra để có thể tự code lại từ đầu bằng XML"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Khám phá thư viện nhạc (Priority: P1)

Là người nghe nhạc, tôi muốn mở ứng dụng và xem danh sách bài hát được phân loại theo các nhóm nổi bật, mới phát hành và thể loại để nhanh chóng chọn nội dung muốn nghe.

**Why this priority**: Đây là điểm vào chính của ứng dụng và là nguồn dữ liệu cho mọi luồng nghe nhạc khác.

**Independent Test**: Với một catalog hợp lệ, người kiểm thử mở màn hình Khám phá, nhìn thấy các nhóm bài hát, chọn một bài và chuyển sang màn hình phát nhạc mà không cần thực hiện luồng nào khác trước đó.

**Acceptance Scenarios**:

1. **Given** catalog có dữ liệu hợp lệ, **When** người dùng mở màn hình Khám phá, **Then** ứng dụng hiển thị danh sách bài hát với tiêu đề, nghệ sĩ và ảnh bìa nếu có.
2. **Given** catalog có nhiều bài hát, **When** người dùng cuộn danh sách, **Then** các bài tiếp theo được hiển thị đầy đủ và không bị lặp hoặc mất thứ tự.
3. **Given** catalog rỗng, **When** người dùng mở màn hình Khám phá, **Then** ứng dụng hiển thị trạng thái rỗng dễ hiểu và hướng dẫn thử lại.
4. **Given** dữ liệu catalog không tải được, **When** yêu cầu tải hoàn tất, **Then** ứng dụng hiển thị lỗi có thể thử lại và không bị đóng đột ngột.

### User Story 2 - Tìm kiếm bài hát (Priority: P1)

Là người nghe nhạc, tôi muốn tìm theo tên bài hát hoặc nghệ sĩ để truy cập nhanh nội dung mình đang cần.

**Why this priority**: Tìm kiếm là cách chính để truy cập một bài hát cụ thể khi catalog lớn.

**Independent Test**: Từ màn hình Tìm kiếm, nhập một từ khóa khớp với tiêu đề hoặc nghệ sĩ, xác nhận kết quả, sau đó mở một kết quả để phát.

**Acceptance Scenarios**:

1. **Given** catalog đã sẵn sàng, **When** người dùng nhập từ khóa vào ô tìm kiếm, **Then** ứng dụng hiển thị các bài hát khớp theo tiêu đề hoặc nghệ sĩ.
2. **Given** từ khóa có chữ hoa, chữ thường hoặc khoảng trắng dư, **When** người dùng tìm kiếm, **Then** kết quả vẫn tương đương với từ khóa đã chuẩn hóa.
3. **Given** không có bài hát phù hợp, **When** người dùng hoàn tất nhập từ khóa, **Then** ứng dụng hiển thị trạng thái “không có kết quả” và cho phép sửa hoặc xóa từ khóa.
4. **Given** ô tìm kiếm trống, **When** người dùng truy cập màn hình Tìm kiếm, **Then** ứng dụng hiển thị lời nhắc và không coi chuỗi trống là một lỗi.

### User Story 3 - Phát và điều khiển bài hát (Priority: P1)

Là người nghe nhạc, tôi muốn phát, tạm dừng, tua, chuyển bài và xem tiến độ để kiểm soát việc nghe nhạc.

**Why this priority**: Phát nhạc là giá trị cốt lõi của sản phẩm.

**Independent Test**: Chọn một bài có đường dẫn âm thanh hợp lệ, kiểm tra phát/tạm dừng, tua đến vị trí mới, chuyển bài kế tiếp/trước đó và xác nhận trạng thái hiển thị nhất quán.

**Acceptance Scenarios**:

1. **Given** người dùng chọn bài hát có nguồn phát hợp lệ, **When** màn hình phát nhạc mở, **Then** bài hát được nạp và ứng dụng hiển thị trạng thái đang phát hoặc đang chờ nạp.
2. **Given** bài hát đang phát, **When** người dùng nhấn tạm dừng rồi phát lại, **Then** âm thanh dừng và tiếp tục đúng bài hát tại vị trí gần nhất.
3. **Given** bài hát có thời lượng xác định, **When** người dùng kéo thanh tiến độ, **Then** phát nhạc chuyển đến vị trí được chọn và thời gian hiển thị được cập nhật.
4. **Given** danh sách phát có bài kế tiếp hoặc trước đó, **When** người dùng chọn điều khiển chuyển bài hoặc vuốt chuyển bài, **Then** bài hiện tại thay đổi đúng hướng và mini player được đồng bộ.
5. **Given** nguồn phát không hợp lệ hoặc mất mạng, **When** ứng dụng cố gắng phát, **Then** ứng dụng hiển thị lỗi có thể thử lại và giữ nguyên khả năng quay lại nội dung khác.
6. **Given** bài hát kết thúc, **When** danh sách phát có bài kế tiếp, **Then** ứng dụng chuyển sang bài kế tiếp theo thứ tự đã chọn.

### User Story 4 - Quản lý yêu thích và tải xuống (Priority: P2)

Là người nghe nhạc, tôi muốn đánh dấu bài hát yêu thích và tải bài hát để có thể truy cập lại nhanh, kể cả khi không có kết nối mạng đối với nội dung đã tải.

**Why this priority**: Tính năng này tạo thư viện cá nhân và hoàn thiện trải nghiệm nghe nhạc ngoài luồng khám phá.

**Independent Test**: Đánh dấu một bài yêu thích, mở lại màn hình Yêu thích, bỏ đánh dấu; tải một bài có nguồn hợp lệ, mở màn hình Tải xuống và kiểm tra dữ liệu sau khi khởi động lại ứng dụng.

**Acceptance Scenarios**:

1. **Given** bài hát chưa được yêu thích, **When** người dùng chọn biểu tượng yêu thích, **Then** trạng thái được đổi sang đã yêu thích và bài hát xuất hiện trong thư viện Yêu thích.
2. **Given** bài hát đã được yêu thích, **When** người dùng bỏ yêu thích, **Then** bài hát biến mất khỏi danh sách Yêu thích sau khi trạng thái được lưu.
3. **Given** thiết bị còn đủ dung lượng và nguồn bài hợp lệ, **When** người dùng yêu cầu tải xuống, **Then** ứng dụng hiển thị tiến độ, kết quả thành công hoặc lỗi rõ ràng.
4. **Given** bài hát đã tải xong, **When** người dùng mở Tải xuống trong trạng thái ngoại tuyến, **Then** bài hát vẫn có thể được nhìn thấy và phát từ bản lưu cục bộ.
5. **Given** thao tác tải xuống bị hủy hoặc thất bại, **When** người dùng mở Tải xuống, **Then** không xuất hiện mục tải xuống không hoàn chỉnh như một bản có thể phát.
6. **Given** người dùng khởi động lại ứng dụng, **When** mở Yêu thích hoặc Tải xuống, **Then** các trạng thái đã lưu trước đó được khôi phục.

### User Story 5 - Cài đặt trải nghiệm người dùng (Priority: P2)

Là người dùng, tôi muốn điều chỉnh các tùy chọn chất lượng âm thanh, chỉ phát khi có Wi-Fi, giao diện tối và bộ nhớ đệm để ứng dụng phù hợp với nhu cầu của mình.

**Why this priority**: Cài đặt giúp kiểm soát dữ liệu, trải nghiệm hiển thị và hành vi phát nhạc.

**Independent Test**: Mở Cài đặt, thay đổi từng tùy chọn, rời màn hình, mở lại hoặc khởi động lại ứng dụng và xác nhận giá trị được giữ cũng như ảnh hưởng đúng đến hành vi liên quan.

**Acceptance Scenarios**:

1. **Given** người dùng mở Cài đặt, **When** thay đổi một tùy chọn, **Then** giá trị mới được lưu và phản ánh ngay trên giao diện.
2. **Given** chế độ chỉ phát bằng Wi-Fi được bật, **When** thiết bị không có Wi-Fi, **Then** ứng dụng không tự bắt đầu một luồng phát trực tuyến mới và giải thích lý do.
3. **Given** chế độ tối được bật hoặc tắt, **When** người dùng xác nhận thay đổi, **Then** giao diện sử dụng chủ đề tương ứng mà không làm mất trạng thái phát nhạc đang có.
4. **Given** người dùng đã lưu cài đặt, **When** mở lại ứng dụng, **Then** các tùy chọn được khôi phục.

### User Story 6 - Điều hướng và phục hồi trạng thái (Priority: P2)

Là người dùng, tôi muốn chuyển giữa các khu vực Khám phá, Tìm kiếm, Yêu thích, Tải xuống và Cài đặt mà không mất bài đang phát hoặc bị mắc kẹt khi một dịch vụ gặp lỗi.

**Why this priority**: Ứng dụng phải duy trì trải nghiệm liên tục khi người dùng chuyển màn hình hoặc mạng không ổn định.

**Independent Test**: Bắt đầu phát một bài, chuyển qua toàn bộ khu vực chính, quay lại màn hình phát và mô phỏng lỗi mạng/không có dữ liệu để kiểm tra các trạng thái phục hồi.

**Acceptance Scenarios**:

1. **Given** một bài đang phát, **When** người dùng chuyển giữa các khu vực chính, **Then** mini player vẫn hiển thị đúng bài và trạng thái phát hiện tại.
2. **Given** người dùng rời màn hình phát, **When** quay lại mini player hoặc mở lại bài, **Then** ứng dụng khôi phục được bài hiện tại, vị trí và thao tác điều khiển phù hợp.
3. **Given** một yêu cầu dữ liệu đang tải, **When** người dùng thử lại sau lỗi, **Then** ứng dụng thực hiện lại yêu cầu mà không tạo bản ghi trùng hoặc trạng thái tải vô hạn.
4. **Given** người dùng nhấn quay lại ở màn hình gốc, **When** không còn màn hình con để đóng, **Then** hành vi thoát ứng dụng phù hợp với quy ước nền tảng.

### Edge Cases

- Catalog chứa bài trùng mã định danh, thiếu tiêu đề, thiếu nghệ sĩ hoặc thiếu ảnh bìa.
- Đường dẫn âm thanh trả về lỗi, hết hạn, định dạng không hỗ trợ hoặc thời lượng không xác định.
- Kết nối mạng bị mất giữa lúc tải catalog, tìm kiếm, phát hoặc tải xuống.
- Ảnh bìa không tải được nhưng bài hát vẫn có thể phát.
- Thiết bị hết dung lượng, quyền lưu trữ không sẵn sàng hoặc người dùng hủy tải xuống.
- Người dùng thao tác nhanh nhiều lần vào phát/tạm dừng, yêu thích, tải xuống hoặc chuyển bài.
- Danh sách phát không có bài trước đó/bài kế tiếp.
- Ứng dụng bị đóng hoặc hệ thống thu hồi tài nguyên trong lúc phát hoặc tải xuống.
- Thay đổi cài đặt khi chưa có mạng và đồng bộ lại khi kết nối được khôi phục.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Hệ thống phải cung cấp các khu vực chính Khám phá, Tìm kiếm, Yêu thích, Tải xuống, Cài đặt và một màn hình phát nhạc chi tiết.
- **FR-002**: Hệ thống phải tải và hiển thị catalog bài hát gồm tối thiểu tiêu đề, nghệ sĩ, đường dẫn phát và thông tin ảnh bìa khi có.
- **FR-003**: Hệ thống phải giữ thứ tự catalog ổn định theo dữ liệu nguồn và không hiển thị bản ghi trùng trong cùng một danh sách.
- **FR-004**: Hệ thống phải hiển thị trạng thái đang tải, rỗng, lỗi và thử lại cho mỗi khu vực có dữ liệu từ bên ngoài.
- **FR-005**: Hệ thống phải cho phép tìm kiếm không phân biệt hoa thường theo tiêu đề hoặc nghệ sĩ.
- **FR-006**: Hệ thống phải cập nhật kết quả tìm kiếm khi từ khóa thay đổi và cho phép xóa toàn bộ từ khóa.
- **FR-007**: Hệ thống phải cho phép người dùng mở một bài hát từ Khám phá, Tìm kiếm, Yêu thích hoặc Tải xuống để phát.
- **FR-008**: Hệ thống phải hỗ trợ phát, tạm dừng, tua đến vị trí, chuyển bài trước/kế tiếp và hiển thị tiến độ cùng thời lượng khi xác định được.
- **FR-009**: Hệ thống phải thể hiện rõ trạng thái đang nạp, đang phát, đã tạm dừng, đã kết thúc và phát sinh lỗi.
- **FR-010**: Hệ thống phải tự chuyển bài theo danh sách phát khi bài hiện tại kết thúc, nếu có bài kế tiếp.
- **FR-011**: Hệ thống phải duy trì bài hiện tại và trạng thái điều khiển khi người dùng chuyển giữa các khu vực chính.
- **FR-012**: Hệ thống phải cho phép thêm, bỏ và xem bài hát yêu thích; thay đổi phải được lưu bền vững.
- **FR-013**: Hệ thống phải cho phép bắt đầu, theo dõi, hủy và nhận kết quả tải xuống; chỉ bản tải hoàn chỉnh mới được xem là sẵn sàng ngoại tuyến.
- **FR-014**: Hệ thống phải cho phép xem và phát các bài đã tải khi không có kết nối mạng.
- **FR-015**: Hệ thống phải lưu và khôi phục các tùy chọn chất lượng âm thanh, chỉ phát bằng Wi-Fi, giao diện tối và bộ nhớ đệm.
- **FR-016**: Hệ thống phải chặn hoặc cảnh báo trước khi phát trực tuyến khi tùy chọn chỉ phát bằng Wi-Fi đang bật và thiết bị không có Wi-Fi.
- **FR-017**: Hệ thống phải xử lý lỗi mạng, dữ liệu thiếu, nguồn phát hỏng, lỗi ảnh và thiếu dung lượng bằng thông báo dễ hiểu, có hành động tiếp theo phù hợp.
- **FR-018**: Hệ thống không được mất dữ liệu yêu thích, tải xuống hoàn chỉnh hoặc cài đặt đã lưu khi ứng dụng được mở lại.
- **FR-019**: Hệ thống phải ngăn thao tác lặp nhanh tạo ra trạng thái mâu thuẫn, tải trùng hoặc nhiều phiên phát đồng thời cho cùng một người dùng.
- **FR-020**: Hệ thống phải hiển thị thông tin phiên bản ứng dụng và nhận diện hồ sơ người dùng hiện tại trong khu vực Cài đặt.

### Non-Functional Requirements

- **NFR-001**: Các luồng chính phải dùng được bằng thao tác chạm thông thường và có trạng thái phản hồi rõ ràng cho mọi thao tác có thể mất thời gian.
- **NFR-002**: Ứng dụng phải giữ được trạng thái nhất quán khi xoay màn hình, chuyển nền rồi quay lại hoặc khôi phục sau khi bị hệ thống tạo lại màn hình.
- **NFR-003**: Ứng dụng không được bị đóng đột ngột khi dữ liệu ngoài không hợp lệ hoặc dịch vụ mạng không khả dụng.
- **NFR-004**: Nội dung hiển thị phải có khả năng đọc được ở chế độ sáng và tối, không phụ thuộc duy nhất vào màu sắc để truyền đạt trạng thái.

## Key Entities *(include if feature involves data)*

- **Song**: Bài hát trong catalog, gồm mã định danh, tiêu đề, nghệ sĩ, thể loại, năm, nguồn âm thanh, ảnh bìa và nguồn thông tin tùy chọn.
- **User Profile**: Hồ sơ người dùng hiện tại và các tùy chọn trải nghiệm/cấu hình phát nhạc.
- **Favorite Membership**: Quan hệ giữa hồ sơ và bài hát yêu thích, bao gồm trạng thái và thời điểm thay đổi.
- **Downloaded Track**: Bản ghi bài hát đã tải, trạng thái tải, tiến độ, vị trí dữ liệu cục bộ, dung lượng và lỗi gần nhất nếu có.
- **Playback Session**: Bài đang phát, danh sách phát, vị trí hiện tại, trạng thái phát, thời lượng và lỗi phiên nếu có.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Trong điều kiện mạng ổn định và catalog sẵn sàng, ít nhất 95% lần mở Khám phá hiển thị nội dung hoặc trạng thái rỗng hợp lệ trong vòng 3 giây.
- **SC-002**: Với catalog tối đa 10.000 bài, ít nhất 95% thao tác tìm kiếm trả về kết quả hoặc trạng thái không có kết quả trong vòng 1 giây sau khi người dùng dừng nhập.
- **SC-003**: Với nguồn âm thanh hợp lệ và mạng ổn định, ít nhất 95% lượt chọn bài bắt đầu phát hoặc hiển thị lỗi có thể hành động trong vòng 5 giây.
- **SC-004**: 100% lỗi catalog, ảnh, phát và tải xuống được biểu diễn bằng trạng thái giao diện có thể hiểu và không làm ứng dụng đóng đột ngột.
- **SC-005**: 100% bài yêu thích, bản tải xuống hoàn chỉnh và cài đặt đã lưu trước khi đóng ứng dụng được khôi phục sau lần mở tiếp theo.
- **SC-006**: Ít nhất 90% người dùng thử nghiệm hoàn thành được ba tác vụ: tìm và phát bài, thêm/bỏ yêu thích, mở lại bài đã tải, mà không cần hướng dẫn bên ngoài.
- **SC-007**: Trong một phiên phát, trạng thái mini player và màn hình phát chi tiết không được mâu thuẫn sau khi chuyển qua lại giữa các khu vực chính.

## Assumptions

- Đây là bản rebuild hoàn chỉnh dựa trên hành vi và dữ liệu hiện có của dự án, không phải bản sao từng dòng mã cũ.
- Phiên bản đầu tiên nhắm đến điện thoại Android và một hồ sơ người dùng hiện tại; hệ thống đăng nhập nhiều tài khoản là phạm vi mở rộng.
- Catalog và nguồn phát trực tuyến cần kết nối mạng; chỉ các bài tải xuống hoàn chỉnh mới được kỳ vọng hoạt động ngoại tuyến.
- Dữ liệu nguồn có thể cung cấp mã bài hát ổn định, tiêu đề, nghệ sĩ, đường dẫn phát và ảnh bìa; các trường còn thiếu phải được xử lý bằng giá trị thay thế hoặc giao diện phù hợp.
- Các yêu cầu phát nhạc nền, thông báo điều khiển hệ thống, đồng bộ đa thiết bị, lời bài hát, đề xuất cá nhân hóa và bảng quản trị nội dung không thuộc phạm vi rebuild này nếu chưa có đặc tả riêng.
- Ràng buộc triển khai đã thống nhất trước đó là giao diện XML; ràng buộc này sẽ được đưa vào tài liệu kế hoạch kỹ thuật và danh sách task, không thay đổi các tiêu chí hành vi ở trên.
