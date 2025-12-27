# Folia Support Implementation

## Tổng quan

Plugin đã được cập nhật để hỗ trợ Folia, một fork của PaperMC sử dụng mô hình đa luồng region-based để cải thiện hiệu suất server.

## Các thay đổi chính

### 1. SchedulerUtil.java (Mới)

- Utility class để tự động phát hiện Folia và sử dụng scheduler phù hợp
- Cung cấp các method:
  - `runTask()` - Global region scheduler cho Folia, main thread cho Paper
  - `runTaskAsynchronously()` - Executor service cho Folia, async scheduler cho Paper
  - `runTaskTimer()` - Global region scheduler cho Folia
  - `runTaskAtLocation()` - Region scheduler cho Folia
  - `runTaskOnEntity()` - Entity scheduler cho Folia
  - `callSyncMethod()` - Wrapper cho sync method calls

### 2. Các file đã được cập nhật

#### ServerTapMain.java

- `runTaskTimer()` → `SchedulerUtil.runTaskTimer()`

#### ServerApi.java

- `runTask()` → `SchedulerUtil.runTask()`
- `broadcastMessage()` → Scheduled trên GlobalRegionScheduler
- `player.sendMessage()` → Scheduled trên EntityScheduler

#### ServerExecCommandSender.java

- `callSyncMethod()` → `SchedulerUtil.callSyncMethod()`

#### WorldApi.java

- `scheduleSyncDelayedTask()` → `SchedulerUtil.runTaskAtLocation()` (sử dụng region scheduler)

#### WebsocketHandler.java

- `scheduleSyncDelayedTask()` → `SchedulerUtil.runTask()`

#### WebhookEventListener.java

- `runTaskAsynchronously()` → `SchedulerUtil.runTaskAsynchronously()`

#### Metrics.java

- `runTask()` → `SchedulerUtil.runTask()`

## Logic không tương thích đã được xử lý

### ✅ Đã sửa

1. **Scheduler calls**: Tất cả `Bukkit.getScheduler()` calls đã được thay thế
2. **World operations**: World save operations sử dụng RegionScheduler
3. **Command execution**: Sử dụng GlobalRegionScheduler
4. **Async tasks**: Sử dụng ExecutorService cho Folia
5. **Broadcast operations**: Scheduled trên GlobalRegionScheduler
6. **Entity operations**: Entity message sending sử dụng EntityScheduler

### ⚠️ Cần kiểm tra thêm

1. **PlayerApi read operations**: Các thao tác đọc dữ liệu player (getLocation, getInventory, etc.) có thể cần được schedule nếu Folia yêu cầu. Hiện tại chúng được gọi trực tiếp từ HTTP endpoints (async threads). Nếu gặp lỗi, cần refactor để sử dụng CompletableFuture và schedule trên EntityScheduler.

2. **World read operations**: Các thao tác đọc world data (getWorlds, getWorld) có thể cần được schedule. Hiện tại chúng được gọi trực tiếp.

## Cách kiểm tra

1. **Test trên Folia server**:

   - Deploy plugin lên server Folia
   - Kiểm tra các API endpoints
   - Kiểm tra websocket connections
   - Kiểm tra webhook events
   - Kiểm tra command execution

2. **Test trên Paper server**:

   - Đảm bảo plugin vẫn hoạt động bình thường trên Paper
   - Tất cả chức năng phải giữ nguyên

3. **Kiểm tra logs**:
   - Tìm các lỗi liên quan đến thread safety
   - Tìm các lỗi "Not on main thread" hoặc "Not on region thread"

## Lưu ý

- Plugin vẫn tương thích với Paper/Bukkit servers
- Folia detection được thực hiện tự động tại runtime
- Không cần thay đổi cấu hình
- Nếu gặp lỗi thread safety, cần schedule các thao tác đọc entity/world data

## Tài liệu tham khảo

- Folia GitHub: https://github.com/PaperMC/Folia
- Folia API Documentation: https://docs.papermc.io/folia/
