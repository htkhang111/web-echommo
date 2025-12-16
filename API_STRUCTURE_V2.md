# EchoMMO API Structure v2.0
**Restructured API endpoints với tổ chức logic rõ ràng**

---

## Authentication APIs
### `/api/auth`
| Method | Endpoint | Mô tả | Request | Response |
|--------|----------|-------|---------|----------|
| POST | `/login` | Đăng nhập | `{username, password}` | `{jwt, username, role}` |
| POST | `/register` | Đăng ký | `{username, email, password, fullName}` | `{message}` |
| POST | `/forgot-password` | Gửi OTP | `{email}` | `{message}` |
| POST | `/reset-password` | Đặt lại mật khẩu | `{email, otp, newPassword}` | `{message}` |

---

## Game APIs
### `/api/game`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/character` | Lấy thông tin nhân vật | Có |
| POST | `/explore` | Khám phá & loot vật phẩm | Có |

---

## Equipment APIs (gộp từ Game, Inventory, Item)
### `/api/equipment`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/inventory` | Lấy túi đồ | - | Có |
| POST | `/equip/{userItemId}` | Mặc đồ | - | Có |
| POST | `/unequip/{userItemId}` | Tháo đồ | - | Có |
| POST | `/enhance/{userItemId}` | Cường hóa item | - | Có |
| POST | `/upgrade/{userItemId}` | Nâng cấp (+1) | - | Có |
| POST | `/evolve-mythic/{userItemId}` | Tiến hóa Mythic | - | Có |
| POST | `/upgrade-mythic/{userItemId}` | Nâng cấp Mythic Level | - | Có |

---

## Shop APIs (từ Marketplace - Official Shop)
### `/api/shop`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/items` | Lấy danh sách shop | - | Có |
| POST | `/buy` | Mua từ shop | `{itemId, quantity}` | Có |
| POST | `/sell` | Bán cho shop | `{userItemId, quantity}` | Có |

---

## Player Market APIs (từ Marketplace - P2P)
### `/api/player-market`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/listings` | Danh sách bán của player khác | - | Có |
| GET | `/my-listings` | Danh sách bán của tôi | - | Có |
| POST | `/create` | Tạo listing bán | `{userItemId, quantity, price}` | Có |
| POST | `/buy/{id}` | Mua listing theo ID | `{quantity?}` | Có |
| POST | `/cancel/{id}` | Hủy listing | - | Có |

---

## Character APIs
### `/api/character`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/me` | Lấy nhân vật hiện tại | - | Có |
| POST | `/create` | Tạo nhân vật mới | `{name}` | Có |
| POST | `/add-stats` | Cộng điểm tiềm năng | `{str, vit, agi, int?}` | Có |

---

## Battle APIs
### `/api/battle`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| POST | `/start` | Bắt đầu trận đấu | `{enemyId?}` | Có |
| POST | `/action` | Thực hiện hành động | `{actionType, target}` | Có |
| POST | `/attack` | Tấn công quái | - | Có |

---

## Exploration APIs
### `/api/exploration`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| POST | `/explore` | Khám phá | - | Có |
| POST | `/gather` | Khai thác tài nguyên | `{type, amount}` | Có |

---

## Social APIs
### `/api/friends`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/list` | Danh sách bạn bè | - | Có |
| GET | `/requests` | Yêu cầu kết bạn | - | Có |
| POST | `/add` | Gửi yêu cầu kết bạn | `{addresseeId}` | Có |
| POST | `/accept/{id}` | Chấp nhận kết bạn | - | Có |
| DELETE | `/remove/{id}` | Xóa bạn | - | Có |

### `/api/chat`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/recent` | Tin nhắn gần đây | - | Có |
| POST | `/send` | Gửi tin nhắn chat | `{content, channel?}` | Có |

### `/api/dm` (Private Message)
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/{friendId}` | Lấy conversation | - | Có |
| POST | `/send` | Gửi tin nhắn riêng | `{receiverId, content}` | Có |

---

## Quest APIs
### `/api/quests`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/daily` | Danh sách daily quest | - | Có |
| POST | `/claim/{id}` | Nhận reward | - | Có |

---

## Leaderboard APIs
### `/api/leaderboard`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/level` | Bảng xếp hạng level | Có |
| GET | `/wealth` | Bảng xếp hạng tài sản | Có |

---

## Notification APIs
### `/api/notifications`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/` | Danh sách thông báo | - | Có |
| GET | `/unread-count` | Số chưa đọc | - | Có |
| POST | `/read/{id}` | Đánh dấu đã đọc | - | Có |
| POST | `/read-all` | Đánh dấu tất cả đã đọc | - | Có |

---

## Announcement APIs
### `/api/announcements`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/` | Danh sách thông báo active | Không |
| POST | `/create` | Tạo thông báo (Admin) | Có (Admin) |
| DELETE | `/{id}` | Xóa thông báo (Admin) | Có (Admin) |

---

## Spa APIs
### `/api/spa`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| POST | `/use` | Sử dụng spa | `{packageType}` | Có |
| GET | `/status` | Trạng thái spa hiện tại | - | Có |
| POST | `/complete` | Hoàn thành spa treatment | - | Có |

---

## Captcha APIs
### `/api/captcha`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/generate` | Generate captcha | Không |
| POST | `/solve` | Giải captcha | `{answer}` | Không |

---

## Report APIs
### `/api/report`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| POST | `/send` | Gửi bug report | `{title, content}` | Có |

---

## Admin APIs
### `/api/admin`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/stats` | Thống kê server | - | Có (Admin) |
| GET | `/users` | Danh sách users | - | Có (Admin) |
| GET | `/items` | Danh sách items | - | Có (Admin) |
| GET | `/listings` | Danh sách listings | - | Có (Admin) |
| POST | `/user/ban/{id}` | Ban user | `{reason}` | Có (Admin) |
| POST | `/user/unban/{id}` | Unban user | - | Có (Admin) |
| POST | `/user/toggle/{id}` | Toggle user status | - | Có (Admin) |
| DELETE | `/user/{id}` | Xóa user | - | Có (Admin) |
| POST | `/item/create` | Tạo item | `{...}` | Có (Admin) |
| DELETE | `/item/{id}` | Xóa item | - | Có (Admin) |
| DELETE | `/listing/{id}` | Xóa listing | - | Có (Admin) |
| POST | `/grant-gold` | Tặng gold | `{userId, amount}` | Có (Admin) |
| POST | `/grant-item` | Tặng item | `{userId, itemId}` | Có (Admin) |
| POST | `/notification/create` | Tạo notification | `{...}` | Có (Admin) |

---

## User APIs
### `/api/user`
| Method | Endpoint | Mô tả | Request | Auth |
|--------|----------|-------|---------|------|
| GET | `/me` | Lấy profile | - | Có |
| PUT | `/update` | Cập nhật profile | `{fullName, avatarUrl}` | Có |
| POST | `/change-password` | Đổi mật khẩu | `{oldPassword, newPassword}` | Có |

---

## Changes Summary

### Deleted Controllers (4)
- ~~ForgotPasswordController~~ → Gộp vào AuthController
- ~~InventoryController~~ → Gộp vào EquipmentController
- ~~ItemController~~ → Gộp vào EquipmentController
- ~~MarketplaceController~~ → Tách thành ShopController + PlayerMarketController

### New Controllers (3)
- ✅ EquipmentController (`/api/equipment`) - Tập trung logic trang bị
- ✅ ShopController (`/api/shop`) - Shop chính thức
- ✅ PlayerMarketController (`/api/player-market`) - Chợ của player

### New Files (2)
- ✅ BaseController - Base class với helper methods
- ✅ GlobalExceptionHandler - Xử lý exception chung

### Updated Controllers (1)
- ✅ AuthController - Gộp forgot password
- ✅ GameController - Chỉ giữ character + explore

---

## Frontend Update Guide

### Old Endpoints → New Endpoints
```javascript
// OLD
POST /api/inventory/equip → POST /api/equipment/equip
POST /api/items/{id}/upgrade → POST /api/equipment/upgrade/{id}
POST /api/market/buy-shop → POST /api/shop/buy
POST /api/market/listings → GET /api/player-market/listings

// NEW (Same base URL, just cleaner organization)
POST /api/equipment/equip/{userItemId}
POST /api/equipment/upgrade/{userItemId}
POST /api/shop/buy
GET /api/player-market/listings
```

---

## Total Endpoints

| Category | Count |
|----------|-------|
| Auth | 4 |
| Game | 2 |
| Equipment | 7 |
| Shop | 3 |
| Player Market | 5 |
| Character | 3 |
| Battle | 3 |
| Exploration | 2 |
| Friends | 5 |
| Chat | 2 |
| DM | 2 |
| Quests | 2 |
| Leaderboard | 2 |
| Notifications | 4 |
| Announcements | 3 |
| Spa | 3 |
| Captcha | 2 |
| Report | 1 |
| Admin | 14 |
| User | 3 |
| **TOTAL** | **71** |

---

## Controllers Count

| Type | Old | New |
|------|-----|-----|
| Regular | 21 | 18 |
| Base/Utility | 0 | 1 |
| Exception Handler | 0 | 1 |
| **Total** | 21 | 20 |

**Improvement:** Giảm từ 21 controllers xuống 20, nhưng tổ chức logic rõ ràng hơn, gộp các logic tương tự.
