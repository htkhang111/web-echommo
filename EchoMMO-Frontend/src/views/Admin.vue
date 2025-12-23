<template>
  <div class="admin-page custom-scrollbar">
    <div class="bg-layer base"></div>
    <div class="bg-layer overlay"></div>
    <div class="particles"></div>

    <div class="admin-container">
      <header class="admin-header fade-in-down">
        <div class="header-content">
          <h1 class="glow-text">QUẢN TRỊ THIÊN PHỦ</h1>
          <p class="subtitle">
            Nơi nắm giữ vận mệnh chúng sinh, điều phối thiên cơ
          </p>
        </div>
        <div class="header-decor"></div>
      </header>

      <nav class="admin-nav fade-in">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="switchTab(tab.id)"
          :class="['nav-item', { active: activeTab === tab.id }]"
        >
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
          <div class="nav-glow"></div>
        </button>
      </nav>

      <main class="admin-content fade-in-up">
        <section v-if="activeTab === 'stats'" class="stats-section">
          <div class="stat-card glass-panel">
            <div class="icon-box user"><i class="fas fa-users"></i></div>
            <div class="info">
              <h3>Tổng Nhân Sĩ</h3>
              <div class="number">{{ adminStore.stats.totalUsers || 0 }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel">
            <div class="icon-box item"><i class="fas fa-khanda"></i></div>
            <div class="info">
              <h3>Kỳ Trân Dị Bảo</h3>
              <div class="number">{{ adminStore.stats.totalItems || 0 }}</div>
            </div>
          </div>
          <div class="stat-card glass-panel highlight">
            <div class="icon-box gold"><i class="fas fa-coins"></i></div>
            <div class="info">
              <h3>Tổng Ngân Lượng</h3>
              <div class="number gold-text">
                {{ formatNumber(adminStore.stats.totalGold || 0) }}
              </div>
            </div>
          </div>
          <div class="stat-card glass-panel highlight-echo">
            <div class="icon-box echo"><i class="fas fa-gem"></i></div>
            <div class="info">
              <h3>Tổng EchoCoin</h3>
              <div class="number echo-text">
                {{ formatNumber(adminStore.stats.totalEchoMined || 0) }}
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'users'" class="data-section">
          <div class="filter-bar glass-panel">
            <div class="search-group">
              <i class="fas fa-search"></i>
              <input
                v-model="userFilters.search"
                placeholder="Truy tìm danh tính..."
              />
            </div>
            <div class="actions">
              <select v-model="userFilters.role">
                <option value="">Tất cả Giới</option>
                <option value="USER">Đạo Hữu (User)</option>
                <option value="ADMIN">Tiên Đế (Admin)</option>
              </select>
              <select v-model="userFilters.status">
                <option value="">Sinh Mệnh</option>
                <option value="active">Tự Do</option>
                <option value="banned">Ngục Tối</option>
              </select>
            </div>
          </div>

          <div class="table-wrapper glass-panel custom-scroll-panel">
            <table class="wuxia-table">
              <thead>
                <tr>
                  <th>Linh Bài (ID)</th>
                  <th>Đạo Hiệu</th>
                  <th>Truyền Âm (Email)</th>
                  <th>Cảnh Giới</th>
                  <th>Trạng Thái</th>
                  <th>Phán Quyết</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in filteredUsers" :key="u.userId">
                  <td class="id-col">#{{ u.userId }}</td>
                  <td class="name-col">{{ u.username }}</td>
                  <td>{{ u.email }}</td>
                  <td>
                    <span :class="['badge', u.role.toLowerCase()]">{{
                      u.role
                    }}</span>
                  </td>
                  <td>
                    <span v-if="u.isActive" class="status-dot active"></span>
                    <span v-else class="status-dot banned"></span>
                    {{ u.isActive ? "Tiêu Dao" : "Phong Ấn" }}
                  </td>
                  <td class="actions-cell">
                    <button
                      @click="openEditUser(u)"
                      class="btn-icon edit"
                      title="Sửa hồ sơ"
                    >
                      <i class="fas fa-pen"></i>
                    </button>
                    <button
                      @click="openAdminChat(u)"
                      class="btn-icon chat"
                      title="Mật đàm"
                    >
                      <i class="fas fa-comment-dots"></i>
                    </button>
                    <button
                      v-if="u.isActive"
                      @click="openBanModal(u)"
                      class="btn-icon ban"
                      title="Giam cầm"
                    >
                      <i class="fas fa-gavel"></i>
                    </button>
                    <button
                      v-else
                      @click="requestUnban(u)"
                      class="btn-icon unban"
                      title="Ân xá"
                    >
                      <i class="fas fa-lock-open"></i>
                    </button>
                    <button
                      @click="requestDeleteUser(u)"
                      class="btn-icon delete"
                      title="Trảm hồn (Xóa)"
                    >
                      <i class="fas fa-skull"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredUsers.length === 0" class="empty-data">
              <i class="fas fa-wind"></i> Không tìm thấy bóng dáng ai...
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'items'" class="data-section">
          <div class="panel-header">
            <button @click="toggleCreateMode" class="btn-create">
              <i
                :class="showCreateItem ? 'fas fa-times' : 'fas fa-plus-circle'"
              ></i>
              {{ showCreateItem ? "Đóng Lò Luyện" : "Chế Tác Bảo Vật" }}
            </button>
          </div>

          <transition name="fade">
            <div
              v-if="showCreateItem"
              class="create-panel glass-panel fade-in wizard-container"
            >
              <div class="wizard-header">
                <h3 class="panel-title-small">
                  <i class="fas fa-fire"></i> Lò Luyện Tạo Tác
                </h3>
                <div class="step-counter">Bước {{ currentStep }}/6</div>
              </div>

              <div class="progress-container">
                <div
                  class="progress-bar"
                  :style="{ width: (currentStep / 6) * 100 + '%' }"
                ></div>
              </div>

              <form @submit.prevent="createItem" class="wizard-form">
                <div v-if="currentStep === 1" class="wizard-step active">
                  <div class="step-label">1. Chọn Chủng Loại</div>
                  <div class="type-grid">
                    <div
                      v-for="t in itemTypes"
                      :key="t.value"
                      :class="[
                        'type-card',
                        { selected: itemForm.type === t.value },
                      ]"
                      @click="selectType(t.value)"
                    >
                      <div class="card-glow"></div>
                      <i :class="t.icon"></i>
                      <span>{{ t.label }}</span>
                    </div>
                  </div>
                </div>

                <div v-if="currentStep === 2" class="wizard-step active">
                  <div class="step-label">2. Định Danh Pháp Bảo</div>
                  <div class="grid-2">
                    <div class="input-group">
                      <label>Mã Định Danh (Code)</label>
                      <input
                        v-model="itemForm.code"
                        placeholder="VD: SWORD_DRAGON_01"
                        required
                      />
                    </div>
                    <div class="input-group">
                      <label>Tên Hiển Thị</label>
                      <input
                        v-model="itemForm.name"
                        placeholder="VD: Huyết Long Đao"
                        required
                      />
                    </div>
                    <div class="input-group full-width">
                      <label>Mô Tả Huyền Năng</label>
                      <textarea
                        v-model="itemForm.description"
                        placeholder="Mô tả công dụng, truyền thuyết..."
                        rows="3"
                      ></textarea>
                    </div>
                  </div>
                  <div class="wizard-actions">
                    <button
                      type="button"
                      @click="currentStep--"
                      class="btn-back"
                    >
                      Quay lại
                    </button>
                    <button
                      type="button"
                      @click="nextStep"
                      class="btn-next"
                      :disabled="!itemForm.code || !itemForm.name"
                    >
                      Tiếp Tục <i class="fas fa-arrow-right"></i>
                    </button>
                  </div>
                </div>

                <div v-if="currentStep === 3" class="wizard-step active">
                  <div class="step-label">3. Chọn Hình Dáng (Asset)</div>
                  <div class="assets-wrapper custom-scrollbar">
                    <div class="assets-grid">
                      <div
                        v-for="(imgUrl, idx) in currentAssets"
                        :key="idx"
                        :class="[
                          'asset-item',
                          {
                            selected:
                              itemForm.imageUrl === currentRawAssets[idx],
                          },
                        ]"
                        @click="selectAsset(currentRawAssets[idx])"
                      >
                        <img :src="imgUrl" alt="asset" />
                        <div class="asset-check">
                          <i class="fas fa-check"></i>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="input-group mt-2">
                    <label>Hoặc nhập URL ảnh ngoài:</label>
                    <input
                      v-model="itemForm.imageUrl"
                      placeholder="https://..."
                      class="full-width"
                    />
                  </div>
                  <div class="wizard-actions">
                    <button
                      type="button"
                      @click="currentStep--"
                      class="btn-back"
                    >
                      Quay lại
                    </button>
                    <button
                      type="button"
                      @click="nextStep"
                      class="btn-next"
                      :disabled="!itemForm.imageUrl"
                    >
                      Tiếp Tục <i class="fas fa-arrow-right"></i>
                    </button>
                  </div>
                </div>

                <div v-if="currentStep === 4" class="wizard-step active">
                  <div class="step-label">4. Chọn Phẩm Cấp</div>
                  <div class="rarity-grid">
                    <div
                      v-for="r in rarities"
                      :key="r.value"
                      :class="[
                        'rarity-card',
                        r.value,
                        { selected: itemForm.rarity === r.value },
                      ]"
                      @click="setRarity(r.value)"
                    >
                      <span>{{ r.label }}</span>
                      <small>{{ getSubStatInfo(r.value) }}</small>
                    </div>
                  </div>
                  <div class="wizard-actions">
                    <button
                      type="button"
                      @click="currentStep--"
                      class="btn-back"
                    >
                      Quay lại
                    </button>
                    <button type="button" @click="nextStep" class="btn-next">
                      Tiếp Tục <i class="fas fa-arrow-right"></i>
                    </button>
                  </div>
                </div>

                <div v-if="currentStep === 5" class="wizard-step active">
                  <div class="step-label">5. Thổi Hồn (Chỉ Số)</div>

                  <div class="stat-section" v-if="getAllowedMainStats.length > 0">
                    <label class="sub-label highlight"
                      >1. Chỉ Số Chính (Dựa theo loại {{ itemForm.type }})</label
                    >
                    <div class="stat-type-selector">
                      <select
                        v-model="selectedMainStatType"
                        class="main-stat-select"
                        @change="calculatePrice"
                      >
                        <option
                          v-for="stat in getAllowedMainStats"
                          :key="stat.value"
                          :value="stat.value"
                        >
                          {{ stat.label }}
                        </option>
                      </select>
                    </div>
                    <div class="input-group mt-2">
                      <input
                        type="number"
                        v-model.number="selectedMainStatValue"
                        class="big-input"
                        placeholder="Nhập giá trị..."
                        @input="calculatePrice"
                      />
                    </div>
                  </div>
                  <div class="empty-text" v-else>
                    Vật phẩm này không cần chỉ số chính.
                  </div>

                  <div class="divider-line"></div>

                  <div class="stat-section">
                    <label class="sub-label"
                      >2. Dòng Phụ ({{ subStatsList.length }} dòng)</label
                    >
                    <div
                      v-if="subStatsList.length > 0 && getAllowedSubStats.length > 0"
                      class="sub-stats-container"
                    >
                      <div
                        v-for="(sub, idx) in subStatsList"
                        :key="idx"
                        class="sub-stat-row fade-in"
                      >
                        <span class="row-index">#{{ idx + 1 }}</span>
                        <select
                          v-model="sub.type"
                          class="sub-stat-select"
                          @change="calculatePrice"
                        >
                          <option value="">-- Chọn --</option>
                          <option
                            v-for="opt in getAllowedSubStats"
                            :key="opt.value"
                            :value="opt.value"
                          >
                            {{ opt.label }}
                          </option>
                        </select>
                        <input
                          type="number"
                          v-model.number="sub.value"
                          placeholder="Giá trị"
                          @input="calculatePrice"
                        />
                        <span
                          v-if="sub.type && sub.type.includes('_')"
                          class="unit-label"
                          >%</span
                        >
                      </div>
                    </div>
                    <div v-else class="empty-text">
                      <span v-if="getAllowedSubStats.length === 0"
                        >Vật phẩm này không hỗ trợ dòng phụ.</span
                      >
                      <span v-else>Phẩm chất này chưa có dòng phụ.</span>
                    </div>
                  </div>

                  <div class="wizard-actions">
                    <button
                      type="button"
                      @click="currentStep--"
                      class="btn-back"
                    >
                      Quay lại
                    </button>
                    <button type="button" @click="nextStep" class="btn-next">
                      Tiếp Tục <i class="fas fa-arrow-right"></i>
                    </button>
                  </div>
                </div>

                <div v-if="currentStep === 6" class="wizard-step active">
                  <div class="step-label">6. Thẩm Định & Xuất Xưởng</div>

                  <div class="summary-card">
                    <div class="item-preview">
                      <img
                        :src="resolveItemImage(itemForm.imageUrl)"
                        class="preview-img"
                      />
                      <div class="preview-info">
                        <h4 :class="'rarity-' + itemForm.rarity">
                          {{ itemForm.name }}
                        </h4>
                        <p>{{ itemForm.rarity }} - {{ itemForm.type }}</p>
                        <div class="preview-stats">
                          Main: +{{ selectedMainStatValue }}
                          {{ getStatLabel(selectedMainStatType) }}
                        </div>
                        <div class="sub-stats-preview">
                          <div
                            v-for="(s, i) in subStatsList"
                            :key="i"
                            class="preview-sub"
                          >
                            <small v-if="s.type && s.value">
                              +{{ s.value
                              }}{{ s.type.includes("_") ? "%" : "" }}
                              {{ getStatLabel(s.type) }}
                            </small>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="distribution-box">
                    <label class="sub-label">Loại Hình Phân Phối:</label>
                    <div class="dist-options">
                      <div
                        :class="[
                          'dist-card',
                          { active: distributionType === 'SHOP' },
                        ]"
                        @click="distributionType = 'SHOP'"
                      >
                        <i class="fas fa-store"></i>
                        <div>
                          <strong>Shop Vô Hạn</strong><small>Bán đại trà.</small>
                        </div>
                      </div>
                      <div
                        :class="[
                          'dist-card',
                          { active: distributionType === 'DROP' },
                        ]"
                        @click="distributionType = 'DROP'"
                      >
                        <i class="fas fa-skull"></i>
                        <div>
                          <strong>Săn Bắt (Drop)</strong
                          ><small>Rơi từ quái.</small>
                        </div>
                      </div>
                      <div
                        :class="[
                          'dist-card',
                          'limited',
                          { active: distributionType === 'LIMITED' },
                        ]"
                        @click="distributionType = 'LIMITED'"
                      >
                        <i class="fas fa-crown"></i>
                        <div>
                          <strong>Hàng Hiếm</strong
                          ><small>Độc quyền Admin.</small>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="pricing-box">
                    <div class="price-row">
                      <span>Giá Gợi Ý:</span>
                      <strong class="suggested-price"
                        >{{ formatNumber(suggestedPrice) }} Vàng</strong
                      >
                    </div>
                    <div class="input-group">
                      <label>Giá Niêm Yết:</label>
                      <input
                        v-model.number="itemForm.basePrice"
                        type="number"
                        class="price-input"
                      />
                    </div>
                  </div>

                  <div class="wizard-actions final">
                    <button
                      type="button"
                      @click="currentStep = 1"
                      class="btn-reset"
                    >
                      Làm Lại
                    </button>
                    <button type="submit" class="btn-submit-final">
                      KHAI LÒ (TẠO NGAY)
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </transition>

          <div class="filter-bar glass-panel mt-3">
            <div class="search-group">
              <i class="fas fa-search"></i>
              <input
                v-model="itemFilters.search"
                placeholder="Tìm tên hoặc mã bảo vật..."
              />
            </div>
            <div class="actions">
              <select v-model="itemFilters.type">
                <option value="">-- Tất Cả Loại --</option>
                <option v-for="t in itemTypes" :key="t.value" :value="t.value">
                  {{ t.label }}
                </option>
              </select>
              <select v-model="itemFilters.rarity">
                <option value="">-- Tất Cả Phẩm --</option>
                <option v-for="r in rarities" :key="r.value" :value="r.value">
                  {{ r.label }}
                </option>
              </select>
            </div>
          </div>

          <div class="table-wrapper glass-panel custom-scroll-panel">
            <table class="wuxia-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Pháp Bảo</th>
                  <th>Loại</th>
                  <th>Phẩm Cấp</th>
                  <th>Giá</th>
                  <th>Nguồn</th>
                  <th>Thao Tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="i in filteredItems" :key="i.id || i.itemId">
                  <td class="id-col">{{ i.code }}</td>
                  <td class="name-col">
                    <img
                      :src="resolveItemImage(i.imageUrl)"
                      class="mini-icon"
                    />
                    {{ i.name }}
                  </td>
                  <td>{{ i.type }}</td>
                  <td :class="['rarity', 'rarity-' + i.rarity]">
                    {{ i.rarity }}
                  </td>
                  <td class="gold-val">{{ formatNumber(i.basePrice) }}</td>
                  <td>
                    <span v-if="i.isLimited" class="badge limited">Limited</span>
                    <span v-else-if="i.isSystemItem" class="badge active"
                      >Shop</span
                    >
                    <span v-else class="badge drop">Drop</span>
                  </td>
                  <td class="actions-cell">
                    <button
                      @click="openEditItem(i)"
                      class="btn-icon edit"
                      title="Chỉnh sửa"
                    >
                      <i class="fas fa-pen"></i>
                    </button>
                    <button
                      @click="requestDeleteItem(i)"
                      class="btn-icon delete"
                    >
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="filteredItems.length === 0" class="empty-data">
              <i class="fas fa-box-open"></i> Kho tàng trống rỗng...
            </div>
          </div>
        </section>

        <section v-if="activeTab === 'notify'" class="grid-2-col">
          <div class="glass-panel">
            <h3 class="panel-title">
              <i class="fas fa-gift"></i> Ban Thưởng Thiên Triều
            </h3>

            <form @submit.prevent="handleGrantGold" class="simple-form">
              <input
                v-model="grantGoldForm.username"
                placeholder="Đạo hiệu người nhận"
                required
              />
              <input
                v-model.number="grantGoldForm.amount"
                type="number"
                placeholder="Số lượng vàng"
                required
              />
              <button
                type="submit"
                class="btn-action gold btn-interactive"
                :disabled="isGrantingGold"
              >
                <i
                  v-if="isGrantingGold"
                  class="fas fa-spinner fa-spin"
                ></i>
                {{ isGrantingGold ? "Đang chuyển..." : "Ban Ngân Lượng" }}
              </button>
            </form>

            <form
              @submit.prevent="handleGrantEcho"
              class="simple-form"
              style="margin-top: 15px"
            >
              <input
                v-model="grantEchoForm.username"
                placeholder="Đạo hiệu người nhận"
                required
              />
              <input
                v-model="grantEchoForm.amount"
                type="text"
                placeholder="Số lượng Echo (VD: 10.5)"
                required
              />
              <button
                type="submit"
                class="btn-action cyan btn-interactive"
                :disabled="isGrantingEcho"
              >
                <i
                  v-if="isGrantingEcho"
                  class="fas fa-spinner fa-spin"
                ></i>
                {{ isGrantingEcho ? "Đang chuyển..." : "Ban EchoCoin" }}
              </button>
            </form>

            <div class="divider-line"></div>

            <form @submit.prevent="handleGrantItem" class="simple-form">
              <input
                v-model="grantItemForm.username"
                placeholder="Đạo hiệu người nhận"
                required
              />

              <div class="item-selector-container">
                <div class="filter-row">
                  <input
                    v-model="grantItemSearch"
                    placeholder="Tìm tên/mã..."
                    class="search-input"
                  />
                  <select v-model="grantItemTypeFilter" class="type-filter">
                    <option value="">Tất cả</option>
                    <option
                      v-for="t in itemTypes"
                      :key="t.value"
                      :value="t.value"
                    >
                      {{ t.label }}
                    </option>
                  </select>
                </div>

                <div class="item-list custom-scroll-panel">
                  <div
                    v-for="item in filteredGrantList"
                    :key="item.id || item.itemId"
                    class="select-item-row"
                    :class="{ active: grantItemForm.itemId === (item.id || item.itemId) }"
                    @click="grantItemForm.itemId = (item.id || item.itemId)"
                  >
                    <span class="item-name">{{ item.name }}</span>
                    <span :class="['item-rarity-badge', 'rarity-' + item.rarity]"
                      >{{ item.rarity }}</span
                    >
                  </div>
                  <div
                    v-if="filteredGrantList.length === 0"
                    class="empty-hint"
                  >
                    Không tìm thấy vật phẩm
                  </div>
                </div>

                <div v-if="selectedGrantItem" class="selected-preview">
                  <img
                    :src="resolveItemImage(selectedGrantItem.imageUrl)"
                    class="preview-icon"
                  />
                  <div class="preview-details">
                    <div
                      class="preview-name"
                      :class="'rarity-' + selectedGrantItem.rarity"
                    >
                      {{ selectedGrantItem.name }}
                    </div>
                    <small>{{ selectedGrantItem.code }}</small>
                  </div>
                </div>
              </div>

              <input
                v-model.number="grantItemForm.quantity"
                type="number"
                placeholder="Số lượng"
                required
                style="margin-top: 10px"
              />
              <button
                type="submit"
                class="btn-action blue btn-interactive"
                :disabled="isGrantingItem || !grantItemForm.itemId"
              >
                <i
                  v-if="isGrantingItem"
                  class="fas fa-spinner fa-spin"
                ></i>
                {{ isGrantingItem ? "Đang chế tác..." : "Ban Bảo Vật" }}
              </button>
            </form>
          </div>

          <div class="glass-panel">
            <h3 class="panel-title">
              <i class="fas fa-scroll"></i> Soạn Cáo Thị
            </h3>
            <form @submit.prevent="handleSendNotification" class="simple-form">
              <input
                v-model="notificationForm.title"
                placeholder="Tiêu đề..."
                required
              />
              <select v-model="notificationForm.type" class="dark-select">
                <option value="INFO">Tin Tức</option>
                <option value="SUCCESS">Tin Mừng</option>
                <option value="WARNING">Cảnh Báo</option>
                <option value="REWARD">Phần Thưởng</option>
              </select>
              <textarea
                v-model="notificationForm.message"
                rows="5"
                placeholder="Nội dung cáo thị..."
              ></textarea>
              <input
                v-model="notificationForm.recipientUsername"
                placeholder="Người nhận (Để trống = Toàn Server)"
              />
              <button
                type="submit"
                class="btn-action red btn-interactive"
                :disabled="isSendingNotification"
              >
                <i
                  v-if="isSendingNotification"
                  class="fas fa-spinner fa-spin"
                ></i>
                {{ isSendingNotification ? "Đang phát loa..." : "Phát Loa Toàn Cõi" }}
              </button>
            </form>
          </div>
        </section>
      </main>
    </div>

    <div
      v-if="showEditItemModal"
      class="modal-backdrop"
      @click.self="showEditItemModal = false"
    >
      <div class="modal-content edit-theme zoom-in">
        <div class="modal-header">
          <i class="fas fa-edit"></i> CHỈNH SỬA PHÁP BẢO
        </div>
        <div class="modal-body custom-scrollbar">
          <div class="edit-preview">
            <img
              :src="resolveItemImage(editItemForm.imageUrl)"
              class="preview-large"
              @error="handleImgError"
            />
            <div class="preview-meta">
              <strong :class="'rarity-' + editItemForm.rarity">{{
                editItemForm.name
              }}</strong>
              <small>{{ editItemForm.code }}</small>
            </div>
          </div>

          <div class="form-grid">
            <div class="input-group">
              <label>Tên Hiển Thị</label>
              <input v-model="editItemForm.name" />
            </div>
            <div class="input-group">
              <label>Giá (Vàng)</label>
              <input v-model.number="editItemForm.basePrice" type="number" />
            </div>
            <div class="input-group full">
              <label>Mô Tả</label>
              <textarea
                v-model="editItemForm.description"
                rows="2"
              ></textarea>
            </div>
            <div class="input-group full">
              <label>URL Hình Ảnh</label>
              <input
                v-model="editItemForm.imageUrl"
                placeholder="https://..."
              />
            </div>

            <div class="stats-edit-section full">
              <label class="section-label">Chỉ Số Chiến Đấu</label>
              <div class="stats-grid-small">
                <div>
                  <label>ATK</label
                  ><input type="number" v-model.number="editItemForm.atkBonus" />
                </div>
                <div>
                  <label>DEF</label
                  ><input type="number" v-model.number="editItemForm.defBonus" />
                </div>
                <div>
                  <label>HP</label
                  ><input type="number" v-model.number="editItemForm.hpBonus" />
                </div>
                <div>
                  <label>SPD</label
                  ><input type="number" v-model.number="editItemForm.speedBonus" />
                </div>
              </div>
              <label class="section-label mt-2">Chỉ Số Công Cụ</label>
              <div class="stats-grid-small">
                <div>
                  <label>Bền</label
                  ><input
                    type="number"
                    v-model.number="editItemForm.maxDurability"
                  />
                </div>
                <div>
                  <label>Luck</label
                  ><input type="number" v-model.number="editItemForm.maxLuck" />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-btns">
          <button @click="showEditItemModal = false" class="btn-cancel">
            Hủy
          </button>
          <button @click="handleUpdateItem" class="btn-confirm-edit">
            Lưu Thay Đổi
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="showEditUserModal"
      class="modal-backdrop"
      @click.self="showEditUserModal = false"
    >
      <div class="modal-content edit-theme zoom-in">
        <div class="modal-header">
          <i class="fas fa-user-edit"></i> HỒ SƠ ĐẠO HỮU
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="input-group">
              <label>Tên Đăng Nhập (Readonly)</label>
              <input
                v-model="editUserForm.username"
                disabled
                class="disabled-input"
              />
            </div>
            <div class="input-group">
              <label>Email Liên Lạc</label>
              <input v-model="editUserForm.email" />
            </div>
            <div class="input-group full">
              <label>Avatar URL</label>
              <input v-model="editUserForm.avatarUrl" />
            </div>
            <div class="input-group full warning-zone">
              <label>Đổi Mật Khẩu (Bỏ trống nếu không đổi)</label>
              <input
                v-model="editUserForm.newPassword"
                type="password"
                placeholder="Nhập mật khẩu mới..."
              />
            </div>
          </div>
        </div>
        <div class="modal-btns">
          <button @click="showEditUserModal = false" class="btn-cancel">
            Hủy
          </button>
          <button @click="handleUpdateUser" class="btn-confirm-edit">
            Cập Nhật
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="showBanModal"
      class="modal-backdrop"
      @click.self="showBanModal = false"
    >
      <div class="modal-content ban-theme zoom-in">
        <div class="modal-header-danger">
          <i class="fas fa-gavel"></i> THI HÀNH KỶ LUẬT
        </div>
        <div class="modal-body">
          <p>
            Đối tượng:
            <strong class="target-name">{{ selectedUser?.username }}</strong>
          </p>
          <textarea
            v-model="banReason"
            placeholder="Lý do trừng phạt (Vi phạm thiên quy)..."
          ></textarea>
        </div>
        <div class="modal-btns">
          <button @click="showBanModal = false" class="btn-cancel">Hủy</button>
          <button @click="confirmBan" class="btn-confirm-ban">Thực Thi</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from "vue";
import { useAdminStore } from "../stores/adminStore";
import { useNotificationStore } from "../stores/notificationStore";
import { useChatStore } from "../stores/chatStore";
import axiosClient from "../api/axiosClient";
import { resolveItemImage } from "../utils/assetHelper";

const adminStore = useAdminStore();
const notificationStore = useNotificationStore();
const chatStore = useChatStore();

// State vars
const activeTab = ref("stats");
const showCreateItem = ref(false);
const showBanModal = ref(false);
const selectedUser = ref(null);
const banReason = ref("");

// Edit States
const showEditItemModal = ref(false);
const editItemForm = reactive({
  id: null,
  itemId: null,
  code: "",
  name: "",
  basePrice: 0,
  description: "",
  imageUrl: "",
  rarity: "",
  atkBonus: 0,
  defBonus: 0,
  hpBonus: 0,
  speedBonus: 0,
  maxDurability: 0,
  maxLuck: 0
});
const showEditUserModal = ref(false);
const editUserForm = reactive({});

// Filter States
const itemFilters = reactive({ search: "", type: "", rarity: "" });

const isGrantingGold = ref(false);
const isGrantingEcho = ref(false);
const isGrantingItem = ref(false);
const isSendingNotification = ref(false);

const currentStep = ref(1);
const suggestedPrice = ref(0);
const isPriceManuallyEdited = ref(false);
const distributionType = ref("DROP");
const selectedMainStatType = ref("ATK");
const selectedMainStatValue = ref(0);

// Danh sách sub-stat động
const subStatsList = ref([]);

// Grant Item Search
const grantItemSearch = ref("");
const grantItemTypeFilter = ref("");

// --- ASSET CONFIG (Giữ nguyên) ---
const assetLibrary = {
  WEAPON: [
    "s_sword_0.png", "s_sword_1.png", "s_sword_2.png", "s_sword_3.png", "s_sword_4.png",
  ],
  PICKAXE: [
    "tool/pickaxe/p-0-strongpickaxe.png", "tool/pickaxe/p-1-vikingpickaxe.png",
    "tool/pickaxe/p-2-superduperpickaxe.png", "tool/pickaxe/p-3-deathandaxes.png",
    "tool/pickaxe/p-4-dimensionalpickaxe.png",
  ],
  AXE: [
    "tool/axe/a-0-strongaxe.png", "tool/axe/a-1-best axeinthegame.png",
    "tool/axe/a-2-axeofthegods.png", "tool/axe/a-3-thiccaxe.png", "tool/axe/a-4-heartoftheforest.webp",
  ],
  SHOVEL: [
    "tool/shovel/s-0-strongshovel.png", "tool/shovel/s-1-epicshovel.png",
    "tool/shovel/s-2-theshovelii.png", "tool/shovel/s-3-supershovel.png", "tool/shovel/s-4-golddigger.png",
  ],
  FISHING_ROD: [
    "tool/fishing-rod/fr-0-strongfishingrod.png", "tool/fishing-rod/fr-1-superfishingrod.png", "tool/fishing-rod/fr-4-themasterbaiter.png",
  ],
  ARMOR: [
    "a_armor_0.png", "a_armor_1.png", "a_armor_2.png", "a_armor_3.png", "a_armor_4.png",
  ],
  HELMET: [
    "h_helmet_0.png", "h_helmet_1.png", "h_helmet_2.png", "h_helmet_3.png", "h_helmet_4.png",
  ],
  BOOTS: [
    "b_boot_0.png", "b_boot_1.png", "b_boot_2.png", "b_boot_3.png", "b_boot_4.png",
  ],
  RING: [
    "ri_ring_0.png", "ri_ring_1.png", "ri_ring_2.png", "ri_ring_3.png", "ri_ring_4.png",
  ],
  NECKLACE: [
    "n_necklace_0.png", "n_necklace_1.png", "n_necklace_2.png", "n_necklace_3.png", "n_necklace_4.png",
  ],
  CONSUMABLE: ["r_potion.png"],
  MATERIAL: [
    "w_wood.png", "o_iron.png", "o_gold.png", "f_fish.png", "r_coin.png", "r_coin-echo.png",
  ],
};

const tabs = [
  { id: "stats", label: "Thiên Cơ (Thống Kê)", icon: "fas fa-chart-pie" },
  { id: "users", label: "Nhân Sĩ (User)", icon: "fas fa-users" },
  { id: "items", label: "Tàng Bảo (Item)", icon: "fas fa-box-open" },
  { id: "notify", label: "Điều Hành (Action)", icon: "fas fa-tasks" },
];

const itemTypes = [
  { value: "WEAPON", label: "Binh Khí", icon: "fas fa-khanda" },
  { value: "ARMOR", label: "Y Phục", icon: "fas fa-tshirt" },
  { value: "HELMET", label: "Mũ Giáp", icon: "fas fa-hard-hat" },
  { value: "BOOTS", label: "Hài (Giày)", icon: "fas fa-shoe-prints" },
  { value: "RING", label: "Nhẫn", icon: "fas fa-ring" },
  { value: "NECKLACE", label: "Vòng Cổ", icon: "fas fa-gem" },
  { value: "PICKAXE", label: "Cúp Đào", icon: "fas fa-hammer" },
  { value: "AXE", label: "Rìu Chặt", icon: "fas fa-tree" },
  { value: "SHOVEL", label: "Xẻng Đào", icon: "fas fa-trowel" },
  { value: "FISHING_ROD", label: "Cần Câu", icon: "fas fa-fish" },
  { value: "CONSUMABLE", label: "Đan Dược", icon: "fas fa-flask" },
  { value: "MATERIAL", label: "Nguyên Liệu", icon: "fas fa-cubes" },
];

const rarities = [
  { value: "COMMON", label: "Thường" },
  { value: "UNCOMMON", label: "Phi Thường" },
  { value: "RARE", label: "Hiếm" },
  { value: "EPIC", label: "Sử Thi" },
  { value: "LEGENDARY", label: "Huyền Thoại" },
  { value: "MYTHIC", label: "Thần Thoại" },
];

const userFilters = reactive({ search: "", role: "", status: "" });

// Item Form
const itemForm = reactive({
  code: "",
  name: "",
  description: "",
  type: "WEAPON",
  slotType: "WEAPON",
  rarity: "COMMON",
  basePrice: 0,
  imageUrl: "",
  isSystemItem: false,
  isLimited: false,
  atkBonus: 0,
  defBonus: 0,
  hpBonus: 0,
  speedBonus: 0,
  maxDurability: 0,
  minLuck: 0,
  maxLuck: 0,
  energySaveChance: 0
});

const grantGoldForm = reactive({ username: "", amount: 1000 });
const grantEchoForm = reactive({ username: "", amount: "" });
const grantItemForm = reactive({ username: "", itemId: 0, quantity: 1 });
const notificationForm = reactive({
  title: "",
  message: "",
  type: "INFO",
  recipientUsername: "",
});

const formatNumber = (n) => Number(n).toLocaleString();

const filteredUsers = computed(() => {
  let users = adminStore.users || [];
  if (userFilters.search) {
    const s = userFilters.search.toLowerCase();
    users = users.filter(
      (u) =>
        u.username?.toLowerCase().includes(s) ||
        u.email?.toLowerCase().includes(s) ||
        u.userId?.toString().includes(s),
    );
  }
  if (userFilters.role)
    users = users.filter((u) => u.role === userFilters.role);
  if (userFilters.status === "active") users = users.filter((u) => u.isActive);
  if (userFilters.status === "banned") users = users.filter((u) => !u.isActive);
  return users;
});

// [FIX] Sửa logic filter search để không crash nếu field null
const filteredItems = computed(() => {
  let items = adminStore.items || [];
  
  if (itemFilters.search) {
    const s = itemFilters.search.toLowerCase();
    items = items.filter(i => 
      (i.name && i.name.toLowerCase().includes(s)) || 
      (i.code && i.code.toLowerCase().includes(s))
    );
  }
  if (itemFilters.type) {
    items = items.filter(i => i.type === itemFilters.type);
  }
  if (itemFilters.rarity) {
    items = items.filter(i => i.rarity === itemFilters.rarity);
  }

  return items.slice().reverse();
});

const filteredGrantList = computed(() => {
  let list = adminStore.items || [];
  if (grantItemTypeFilter.value) {
    list = list.filter(i => i.type === grantItemTypeFilter.value);
  }
  if (grantItemSearch.value) {
    const s = grantItemSearch.value.toLowerCase();
    list = list.filter(i => (i.name && i.name.toLowerCase().includes(s)) || (i.code && i.code.toLowerCase().includes(s)));
  }
  return list.slice(0, 20); 
});

const selectedGrantItem = computed(() => {
  if (!grantItemForm.itemId) return null;
  // [FIX] Check cả id và itemId
  return adminStore.items.find(i => (i.id || i.itemId) === grantItemForm.itemId);
});

// [FIX] Check cả id và itemId
const itemOptions = computed(() =>
  (adminStore.items || []).map((i) => ({ id: i.id || i.itemId, name: i.name })),
);

const currentRawAssets = computed(() => {
  return assetLibrary[itemForm.type] || assetLibrary["WEAPON"];
});

const currentAssets = computed(() => {
  return currentRawAssets.value.map((path) => resolveItemImage(path));
});

// [UPDATED] DANH SÁCH CHỈ SỐ TOÀN CỤC
const allStats = [
  { value: "ATK", label: "Tấn Công (Flat)" },
  { value: "ATK_PERCENT", label: "Tấn Công (%)" },
  { value: "DEF", label: "Phòng Thủ (Flat)" },
  { value: "DEF_PERCENT", label: "Phòng Thủ (%)" },
  { value: "HP", label: "Sinh Lực (Flat)" },
  { value: "HP_PERCENT", label: "Sinh Lực (%)" },
  { value: "SPEED", label: "Tốc Độ" },
  { value: "CRIT_RATE", label: "Bạo Kích (%)" },
  { value: "CRIT_DMG", label: "Sát Thương Bạo (%)" },
  { value: "DURABILITY", label: "Độ Bền" },
  { value: "LUCK", label: "May Mắn" },
];

const getAllowedMainStats = computed(() => {
  const type = itemForm.type;
  if (["PICKAXE", "AXE", "SHOVEL", "FISHING_ROD"].includes(type)) {
    return allStats.filter(s => ["DURABILITY", "LUCK"].includes(s.value));
  }
  if (type === "CONSUMABLE") {
    return allStats.filter(s => s.value === "HP");
  }
  switch (type) {
    case "WEAPON":  
      return allStats.filter(s => !["HP", "HP_PERCENT", "DEF", "DEF_PERCENT", "DURABILITY", "LUCK"].includes(s.value));
    case "ARMOR":
    case "HELMET":
      return allStats.filter(s => !["ATK", "ATK_PERCENT", "CRIT_RATE", "CRIT_DMG", "SPEED", "DURABILITY", "LUCK"].includes(s.value));
    case "BOOTS":
      return allStats.filter(s => !["ATK", "ATK_PERCENT", "CRIT_RATE", "CRIT_DMG", "DURABILITY", "LUCK"].includes(s.value));
    case "RING":
      return allStats.filter(s => ["HP", "HP_PERCENT", "DEF", "DEF_PERCENT", "ATK", "ATK_PERCENT"].includes(s.value));
    case "NECKLACE":
      return allStats.filter(s => !["SPEED", "DURABILITY", "LUCK"].includes(s.value));
    default: return allStats.slice(0, 4);
  }
});

const getAllowedSubStats = computed(() => {
  const type = itemForm.type;
  const mainStat = selectedMainStatType.value;
  if (["PICKAXE", "AXE", "SHOVEL", "FISHING_ROD", "CONSUMABLE"].includes(type)) {
    return []; 
  }
  let allowed = [...allStats].filter(s => !["DURABILITY", "LUCK"].includes(s.value));
  if (!["RING", "NECKLACE"].includes(type)) {
    if (type === "WEAPON") {
      allowed = allowed.filter(s => !["HP", "HP_PERCENT", "DEF", "DEF_PERCENT"].includes(s.value));
    } else if (["ARMOR", "HELMET"].includes(type)) {
      allowed = allowed.filter(s => !["ATK", "ATK_PERCENT", "CRIT_RATE", "CRIT_DMG", "SPEED"].includes(s.value));
    } else if (type === "BOOTS") {
      allowed = allowed.filter(s => !["ATK", "ATK_PERCENT", "CRIT_RATE", "CRIT_DMG"].includes(s.value));
    }
  }
  return allowed.filter(s => s.value !== mainStat);
});

const switchTab = (tab) => {
  activeTab.value = tab;
  if (tab === "stats") adminStore.fetchStats();
  if (tab === "users") adminStore.fetchUsers();
  if (tab === "items" || tab === "notify") adminStore.fetchItems();
};

const openAdminChat = (user) => {
  chatStore.openChatWith(user);
  notificationStore.showToast(
    `Đang kết nối thần thức với ${user.username}`,
    "info",
  );
};

const toggleCreateMode = () => {
  showCreateItem.value = !showCreateItem.value;
  if (showCreateItem.value) {
    currentStep.value = 1;
    Object.assign(itemForm, {
      code: "", name: "", description: "",
      type: "WEAPON", slotType: "WEAPON", rarity: "COMMON",
      basePrice: 0, imageUrl: "",
      isSystemItem: false, isLimited: false,
      atkBonus: 0, defBonus: 0, hpBonus: 0, speedBonus: 0,
      maxDurability: 0, minLuck: 0, maxLuck: 0, energySaveChance: 0
    });
    distributionType.value = "DROP";
    selectedMainStatType.value = "ATK";
    selectedMainStatValue.value = 0;
    suggestedPrice.value = 0;
    isPriceManuallyEdited.value = false;
    subStatsList.value = [];
  }
};

const selectType = (t) => {
  itemForm.type = t;
  itemForm.slotType = t;
  if (["PICKAXE", "AXE", "SHOVEL", "FISHING_ROD"].includes(t)) {
    selectedMainStatType.value = "DURABILITY";
  } else if (t === "ARMOR" || t === "HELMET") {
    selectedMainStatType.value = "DEF";
  } else if (t === "BOOTS") {
    selectedMainStatType.value = "SPEED";
  } else if (t === "CONSUMABLE") {
    selectedMainStatType.value = "HP";
  } else {
    selectedMainStatType.value = "ATK";
  }
  setTimeout(() => nextStep(), 200);
};

const selectAsset = (rawPath) => {
  itemForm.imageUrl = rawPath;
};

const setRarity = (r) => {
  itemForm.rarity = r;
  subStatsList.value = [];
  const count = getSubStatCount();
  for (let i = 0; i < count; i++) {
    subStatsList.value.push({ type: "", value: 0 });
  }
  calculatePrice();
};

const nextStep = () => {
  if (currentStep.value < 6) currentStep.value++;
  if (currentStep.value === 4) {
     setRarity(itemForm.rarity);
  }
  if (currentStep.value === 6) calculatePrice();
};

const getSubStatCount = () => {
  if (["PICKAXE", "AXE", "SHOVEL", "FISHING_ROD", "CONSUMABLE"].includes(itemForm.type)) {
    return 0;
  }
  const map = {
    COMMON: 0, UNCOMMON: 1, RARE: 2,
    EPIC: 3, LEGENDARY: 4, MYTHIC: 5,
  };
  return map[itemForm.rarity] || 0;
};

const getSubStatInfo = (r) => {
  if (["PICKAXE", "AXE", "SHOVEL", "FISHING_ROD", "CONSUMABLE"].includes(itemForm.type)) {
    return "0 dòng";
  }
  const map = {
    COMMON: 0, UNCOMMON: 1, RARE: 2,
    EPIC: 3, LEGENDARY: 4, MYTHIC: 5,
  };
  return (map[r] || 0) + " dòng";
};

const getStatLabel = (key) => {
  const found = allStats.find(s => s.value === key);
  return found ? found.label : key;
};

const calculatePrice = () => {
  itemForm.atkBonus = 0;
  itemForm.defBonus = 0;
  itemForm.hpBonus = 0;
  itemForm.speedBonus = 0;
  itemForm.maxDurability = 0;
  itemForm.minLuck = 0;
  itemForm.maxLuck = 0;

  applyStat(selectedMainStatType.value, selectedMainStatValue.value);

  subStatsList.value.forEach(sub => {
    if (sub.type && sub.value) {
      applyStat(sub.type, sub.value);
    }
  });

  let score = 200;
  score += (itemForm.atkBonus || 0) + (itemForm.defBonus || 0) + (itemForm.hpBonus || 0);
  score += (itemForm.speedBonus || 0) * 200;
  score += (itemForm.maxDurability || 0) + (itemForm.maxLuck || 0) * 10;

  subStatsList.value.forEach(sub => {
    if (sub.type && sub.value) {
      if (sub.type.includes("PERCENT") || sub.type.includes("CRIT")) {
        score += sub.value * 50; 
      }
    }
  });

  if (selectedMainStatType.value.includes("PERCENT") || selectedMainStatType.value.includes("CRIT")) {
      score += selectedMainStatValue.value * 50;
  }

  const mult = {
    COMMON: 1, UNCOMMON: 2, RARE: 5,
    EPIC: 15, LEGENDARY: 50, MYTHIC: 200,
  };

  suggestedPrice.value = score * (mult[itemForm.rarity] || 1);

  if (!isPriceManuallyEdited.value) {
    itemForm.basePrice = suggestedPrice.value;
  }
};

const applyStat = (type, val) => {
  if (type === "ATK") itemForm.atkBonus += val;
  else if (type === "DEF") itemForm.defBonus += val;
  else if (type === "HP") itemForm.hpBonus += val;
  else if (type === "SPEED") itemForm.speedBonus += val;
  else if (type === "DURABILITY") itemForm.maxDurability = val;
  else if (type === "LUCK") {
    itemForm.maxLuck = val;
    itemForm.minLuck = Math.floor(val * 0.8);
  }
};

watch(
  () => itemForm.basePrice,
  (newVal) => {
    if (newVal !== suggestedPrice.value) {
      isPriceManuallyEdited.value = true;
    }
  },
);

const createItem = async () => {
  try {
    itemForm.slotType = itemForm.type;

    if (distributionType.value === "SHOP") {
      itemForm.isSystemItem = true;
      itemForm.isLimited = false;
    } else if (distributionType.value === "LIMITED") {
      itemForm.isSystemItem = false;
      itemForm.isLimited = true;
    } else {
      itemForm.isSystemItem = false;
      itemForm.isLimited = false;
    }

    await axiosClient.post("/admin/item/create", itemForm);
    notificationStore.showToast("Chế tác thành công!", "success");
    adminStore.fetchItems();
    showCreateItem.value = false;
  } catch (e) {
    console.error(e);
    notificationStore.showToast(
      "Lỗi chế tác: " + (e.response?.data?.message || "Kiểm tra lại thông tin"),
      "error",
    );
  }
};

const handleGrantGold = async () => {
  if (isGrantingGold.value) return;
  isGrantingGold.value = true;
  try {
    await adminStore.grantGold(grantGoldForm.username, grantGoldForm.amount);
    notificationStore.showToast("Đã cấp ngân lượng!", "success");
    grantGoldForm.amount = 1000;
  } catch (e) {
    notificationStore.showToast("Lỗi: " + (e.response?.data?.message || e.message), "error");
  } finally {
    isGrantingGold.value = false;
  }
};

const handleGrantEcho = async () => {
  if (isGrantingEcho.value) return;
  isGrantingEcho.value = true;
  try {
    await adminStore.grantEcho(grantEchoForm.username, grantEchoForm.amount);
    notificationStore.showToast("Đã ban EchoCoin!", "success");
    grantEchoForm.username = "";
    grantEchoForm.amount = "";
  } catch (e) {
    notificationStore.showToast("Lỗi ban thưởng Echo!", "error");
  } finally {
    isGrantingEcho.value = false;
  }
};

const handleGrantItem = async () => {
  if (isGrantingItem.value) return;
  isGrantingItem.value = true;
  try {
    await adminStore.grantItem(
      grantItemForm.username,
      grantItemForm.itemId,
      grantItemForm.quantity,
    );
    notificationStore.showToast("Đã ban bảo vật!", "success");
    grantItemForm.quantity = 1;
  } catch (e) {
    notificationStore.showToast("Lỗi: " + (e.response?.data?.message || e.message), "error");
  } finally {
    isGrantingItem.value = false;
  }
};

const handleSendNotification = async () => {
  if (isSendingNotification.value) return;

  if (!notificationForm.title || !notificationForm.message) {
      notificationStore.showToast("Thiếu tiêu đề hoặc nội dung!", "warning");
      return;
  }

  isSendingNotification.value = true;
  try {
    await adminStore.sendNotification(notificationForm);
    notificationStore.showToast("Đã phát loa!", "success");
    notificationForm.title = "";
    notificationForm.message = "";
    notificationForm.recipientUsername = "";
  } catch (e) {
    notificationStore.showToast("Lỗi phát loa: " + (e.response?.data || e.message), "error");
  } finally {
    isSendingNotification.value = false;
  }
};

const openBanModal = (u) => {
  selectedUser.value = u;
  banReason.value = "";
  showBanModal.value = true;
};
const confirmBan = async () => {
  if (!banReason.value)
    return notificationStore.showToast("Cần lý do trừng phạt!", "warning");
  try {
    await adminStore.banUser(selectedUser.value.userId, banReason.value);
    notificationStore.showToast("Đã thi hành án!", "success");
    showBanModal.value = false;
  } catch (e) {
    notificationStore.showToast("Lỗi ban!", "error");
  }
};
const requestUnban = async (u) => {
  if (confirm(`Ân xá cho ${u.username}?`)) {
    try {
      await adminStore.unbanUser(u.userId);
      notificationStore.showToast("Đã ân xá!", "success");
    } catch (e) {
      notificationStore.showToast("Lỗi!", "error");
    }
  }
};
const requestDeleteUser = async (u) => {
  if (confirm(`Xóa vĩnh viễn ${u.username}?`)) {
    try {
      await axiosClient.delete(`/admin/user/${u.userId}`);
      adminStore.fetchUsers();
      notificationStore.showToast("Đã xóa!", "success");
    } catch (e) {
      notificationStore.showToast("Lỗi!", "error");
    }
  }
};

// [FIX] Sửa logic Delete Item: dùng i.id hoặc i.itemId
const requestDeleteItem = async (i) => {
  if (confirm(`Hủy bỏ vật phẩm ${i.name}?`)) {
    try {
      // Ưu tiên id, fallback sang itemId
      const targetId = i.id || i.itemId;
      await axiosClient.delete(`/admin/item/${targetId}`);
      adminStore.fetchItems();
      notificationStore.showToast("Đã hủy!", "success");
    } catch (e) {
      notificationStore.showToast("Lỗi: " + (e.response?.data?.message || "Không thể xóa"), "error");
    }
  }
};

const handleImgError = (e) => {
  e.target.src = resolveItemImage("o_coal.png"); 
};

// --- EDIT HANDLERS (UPDATED) ---
const openEditItem = (item) => {
  Object.assign(editItemForm, JSON.parse(JSON.stringify(item)));
  // [FIX] Đảm bảo form có id
  if (!editItemForm.id && editItemForm.itemId) {
      editItemForm.id = editItemForm.itemId;
  }
  showEditItemModal.value = true;
};

const handleUpdateItem = async () => {
  try {
    // [FIX] Dùng id hoặc itemId từ form
    const targetId = editItemForm.id || editItemForm.itemId;
    await axiosClient.put(`/admin/item/${targetId}`, editItemForm);
    notificationStore.showToast("Cập nhật vật phẩm thành công!", "success");
    showEditItemModal.value = false;
    adminStore.fetchItems();
  } catch (e) {
    notificationStore.showToast("Lỗi cập nhật: " + (e.response?.data || e.message), "error");
  }
};

const openEditUser = (user) => {
  Object.assign(editUserForm, {
    userId: user.userId,
    username: user.username,
    email: user.email,
    avatarUrl: user.avatarUrl,
    newPassword: "" 
  });
  showEditUserModal.value = true;
};

const handleUpdateUser = async () => {
  try {
    const payload = { ...editUserForm };
    if (!payload.newPassword) delete payload.newPassword;
    
    await axiosClient.put(`/admin/user/${editUserForm.userId}`, payload);
    notificationStore.showToast("Cập nhật hồ sơ thành công!", "success");
    showEditUserModal.value = false;
    adminStore.fetchUsers();
  } catch (e) {
    notificationStore.showToast("Lỗi cập nhật: " + (e.response?.data || e.message), "error");
  }
};

onMounted(() => {
  adminStore.fetchStats();
  adminStore.fetchUsers();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700&family=Noto+Serif+TC:wght@400;700&display=swap");

/* --- GLOBAL LAYOUT --- */
.admin-page { min-height: 100vh; background-color: #0a0a0a; color: #e0e0e0; font-family: "Noto Serif TC", serif; position: relative; overflow-x: hidden; }
.bg-layer { position: fixed; inset: 0; z-index: 0; pointer-events: none; }
.bg-layer.base { background: radial-gradient(circle at top, #1a100e, #000000); }
.bg-layer.overlay { background-image: url("https://www.transparenttextures.com/patterns/black-scales.png"); opacity: 0.3; }
.admin-container { position: relative; z-index: 10; max-width: 1400px; margin: 0 auto; padding: 40px 20px; }

/* ... (Style buttons, header, nav, tables... giữ nguyên) ... */
.btn-interactive { transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden; }
.btn-interactive:not(:disabled):hover { filter: brightness(1.2); transform: translateY(-2px); box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3); }
.btn-interactive:not(:disabled):active { transform: translateY(1px) scale(0.98); box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2); }
.btn-interactive:disabled { opacity: 0.7; cursor: not-allowed; filter: grayscale(0.5); transform: none; box-shadow: none; }
.admin-header { text-align: center; margin-bottom: 40px; position: relative; }
.glow-text { font-family: "Cinzel", serif; font-size: 3.5rem; color: #ffca28; text-transform: uppercase; margin: 0; text-shadow: 0 0 10px rgba(255, 202, 40, 0.5), 0 0 20px rgba(255, 0, 0, 0.3); letter-spacing: 5px; background: -webkit-linear-gradient(#ffd700, #ff8c00); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.subtitle { color: #aaa; font-size: 1.1rem; font-style: italic; margin-top: 10px; letter-spacing: 1px; }
.header-decor { height: 2px; background: linear-gradient(90deg, transparent, #ffca28, transparent); width: 50%; margin: 20px auto 0; }
.admin-nav { display: flex; justify-content: center; gap: 20px; margin-bottom: 40px; flex-wrap: wrap; }
.nav-item { background: rgba(20, 20, 20, 0.8); border: 1px solid #444; color: #bbb; padding: 12px 25px; font-size: 1.2rem; cursor: pointer; position: relative; transition: all 0.4s ease; display: flex; align-items: center; gap: 10px; font-family: "Noto Serif TC", serif; text-transform: uppercase; clip-path: polygon(10% 0, 100% 0, 100% 100%, 0 100%, 0 20%); }
.nav-item:hover, .nav-item.active { background: rgba(62, 39, 35, 0.9); color: #ffca28; border-color: #ffca28; box-shadow: 0 0 15px rgba(255, 202, 40, 0.2); transform: translateY(-2px); }
.nav-item.active i { color: #ffca28; }
.glass-panel { background: rgba(20, 20, 20, 0.6); backdrop-filter: blur(12px); border: 1px solid rgba(255, 215, 0, 0.15); border-radius: 8px; padding: 25px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5); transition: transform 0.3s ease, box-shadow 0.3s ease; margin-bottom: 20px; }
.table-wrapper { max-height: 70vh; overflow-y: auto; position: relative; padding: 0; }
.custom-scroll-panel::-webkit-scrollbar { width: 8px; height: 8px; }
.custom-scroll-panel::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.3); border-radius: 4px; }
.custom-scroll-panel::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 4px; border: 1px solid #3e2723; }
.custom-scroll-panel::-webkit-scrollbar-thumb:hover { background: #ffca28; }
.wuxia-table { width: 100%; border-collapse: collapse; }
.wuxia-table th { position: sticky; top: 0; z-index: 2; text-align: left; padding: 15px; color: #ffca28; font-family: "Cinzel", serif; border-bottom: 2px solid rgba(255, 215, 0, 0.5); text-transform: uppercase; letter-spacing: 1px; background: rgba(30, 20, 15, 0.98); box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5); }
.wuxia-table td { padding: 15px; border-bottom: 1px solid rgba(255, 255, 255, 0.05); vertical-align: middle; color: #ccc; transition: background 0.2s; }
.wuxia-table tr:hover td { background: linear-gradient(90deg, rgba(255, 202, 40, 0.05), transparent); color: #fff; }
.id-col { font-family: monospace; color: #666; }
.name-col { font-weight: bold; color: #fff; font-size: 1.1rem; }
.gold-val { color: #ffca28; font-weight: bold; }
.stats-section { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 25px; margin-bottom: 40px; }
.stat-card { display: flex; align-items: center; gap: 25px; border-left: 4px solid #444; }
.stat-card.highlight { border-left-color: #ffca28; }
.stat-card.highlight-echo { border-left-color: #26c6da; }
.icon-box { width: 80px; height: 80px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; background: rgba(0, 0, 0, 0.4); }
.user { color: #42a5f5; text-shadow: 0 0 10px #42a5f5; }
.item { color: #ab47bc; text-shadow: 0 0 10px #ab47bc; }
.gold { color: #ffca28; text-shadow: 0 0 10px #ffca28; }
.echo { color: #26c6da; text-shadow: 0 0 10px #26c6da; }
.info h3 { margin: 0; color: #888; font-size: 1rem; text-transform: uppercase; letter-spacing: 1px; }
.info .number { font-size: 2.5rem; font-weight: bold; color: #eee; font-family: "Cinzel", serif; }
.echo-text { color: #26c6da; }
.badge { padding: 4px 10px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; text-transform: uppercase; }
.badge.user { background: rgba(33, 150, 243, 0.15); color: #64b5f6; border: 1px solid #1e88e5; }
.badge.admin { background: rgba(255, 167, 38, 0.15); color: #ffb74d; border: 1px solid #fb8c00; }
.badge.active { color: #00e676; border: 1px solid #00e676; }
.badge.drop { color: #29b6f6; border: 1px solid #29b6f6; }
.badge.limited { color: #ffca28; border: 1px solid #ffca28; font-weight: bold; text-shadow: 0 0 5px #ffca28; }
.status-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 8px; }
.status-dot.active { background: #00e676; box-shadow: 0 0 8px #00e676; }
.status-dot.banned { background: #ff1744; box-shadow: 0 0 8px #ff1744; }
.actions-cell { display: flex; gap: 8px; }
.btn-icon { width: 40px; height: 40px; border: none; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s; color: #fff; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3); }
.chat { background: linear-gradient(135deg, #42a5f5, #1565c0); }
.ban { background: linear-gradient(135deg, #ef5350, #c62828); }
.unban { background: linear-gradient(135deg, #66bb6a, #2e7d32); }
.delete { background: linear-gradient(135deg, #757575, #424242); }
.btn-icon:hover { transform: translateY(-3px) scale(1.1); filter: brightness(1.2); }
.filter-bar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.search-group { position: relative; flex: 1; max-width: 400px; }
.search-group i { position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #ffca28; }
.search-group input { width: 100%; padding: 12px 12px 12px 45px; background: rgba(0, 0, 0, 0.3); border: 1px solid #555; color: #fff; border-radius: 30px; outline: none; font-family: inherit; }
.search-group input:focus { border-color: #ffca28; box-shadow: 0 0 10px rgba(255, 202, 40, 0.2); }
select { background: #222; color: #fff; border: 1px solid #555; padding: 10px 15px; border-radius: 4px; outline: none; }
.simple-form input, .simple-form textarea { width: 100%; padding: 12px; background: rgba(0, 0, 0, 0.4); border: 1px solid #555; color: #fff; border-radius: 4px; box-sizing: border-box; margin-bottom: 10px; font-family: inherit; }
.btn-action { width: 100%; padding: 12px; border: none; font-weight: bold; cursor: pointer; border-radius: 4px; text-transform: uppercase; transition: 0.3s; font-family: "Cinzel", serif; letter-spacing: 1px; }
.btn-action.gold { background: linear-gradient(45deg, #ffd700, #ffa000); color: #000; }
.btn-action.cyan { background: linear-gradient(45deg, #26c6da, #00acc1); color: #fff; }
.btn-action.blue { background: linear-gradient(45deg, #42a5f5, #1976d2); color: #fff; }
.btn-action.red { background: linear-gradient(45deg, #ef5350, #c62828); color: #fff; }
.btn-create { background: transparent; border: 1px solid #ffca28; color: #ffca28; padding: 10px 25px; border-radius: 30px; cursor: pointer; font-weight: bold; transition: 0.3s; text-transform: uppercase; }
.btn-create:hover { background: #ffca28; color: #000; box-shadow: 0 0 15px #ffca28; }
.grid-2-col { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
.divider-line { height: 1px; background: #444; margin: 20px 0; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.85); backdrop-filter: blur(5px); z-index: 2000; display: flex; align-items: center; justify-content: center; }
.modal-content.ban-theme { background: #1a0505; border: 2px solid #ef5350; width: 450px; box-shadow: 0 0 50px rgba(239, 83, 80, 0.4); overflow: hidden; }
.modal-header-danger { background: #c62828; color: #fff; padding: 15px; text-align: center; font-weight: bold; font-size: 1.2rem; text-transform: uppercase; }
.modal-body { padding: 25px; }
.target-name { color: #ef5350; font-size: 1.2rem; margin-left: 5px; }
.modal-content textarea { width: 100%; height: 100px; margin-top: 15px; background: #000; border: 1px solid #555; color: #fff; padding: 10px; }
.modal-btns { display: flex; justify-content: flex-end; padding: 15px; gap: 10px; background: rgba(0, 0, 0, 0.2); }
.btn-cancel { background: transparent; color: #aaa; border: 1px solid #555; padding: 8px 20px; cursor: pointer; }
.btn-confirm-ban { background: #ef5350; color: #fff; border: none; padding: 8px 25px; cursor: pointer; font-weight: bold; }
.rarity-COMMON { color: #bdbdbd; }
.rarity-UNCOMMON { color: #4caf50; }
.rarity-RARE { color: #42a5f5; }
.rarity-EPIC { color: #ab47bc; }
.rarity-LEGENDARY { color: #ffca28; text-shadow: 0 0 5px #ffca28; }
.rarity-MYTHIC { color: #ef5350; text-shadow: 0 0 8px #ef5350; font-weight: bold; }
.mini-icon { width: 24px; height: 24px; vertical-align: middle; margin-right: 5px; }
.wizard-container { background: rgba(15, 10, 8, 0.95); border: 1px solid #5d4037; box-shadow: 0 0 30px rgba(0, 0, 0, 0.8); border-radius: 12px; padding: 30px; min-height: 500px; display: flex; flex-direction: column; }
.wizard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.panel-title-small { font-family: "Cinzel", serif; color: #ffca28; font-size: 1.4rem; margin: 0; }
.step-counter { color: #888; font-family: monospace; }
.progress-container { height: 4px; background: #333; border-radius: 2px; margin-bottom: 30px; overflow: hidden; }
.progress-bar { height: 100%; background: linear-gradient(90deg, #ff6f00, #ffca28); transition: width 0.4s ease; box-shadow: 0 0 10px #ffca28; }
.wizard-step { animation: fadeInUp 0.4s ease; flex: 1; display: flex; flex-direction: column; }
.step-label { font-size: 1.2rem; color: #e0e0e0; margin-bottom: 20px; border-left: 3px solid #ffca28; padding-left: 10px; font-weight: bold; }
.type-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.type-card { background: #1a1a1a; padding: 25px; text-align: center; border-radius: 8px; border: 1px solid #444; cursor: pointer; position: relative; overflow: hidden; transition: all 0.3s; }
.type-card:hover, .type-card.selected { border-color: #ffca28; transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5); }
.type-card.selected i, .type-card.selected span { color: #ffca28; }
.type-card i { font-size: 2.5rem; margin-bottom: 10px; color: #888; transition: 0.3s; }
.card-glow { position: absolute; inset: 0; background: radial-gradient(circle, rgba(255, 202, 40, 0.1), transparent); opacity: 0; transition: 0.3s; }
.type-card:hover .card-glow { opacity: 1; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.full-width { grid-column: span 2; width: 100%; }
.input-group label { display: block; color: #aaa; margin-bottom: 8px; font-size: 0.9rem; }
.input-group input, .input-group textarea { width: 100%; background: rgba(0, 0, 0, 0.3); border: 1px solid #444; color: #fff; padding: 12px; border-radius: 4px; font-family: inherit; transition: 0.3s; box-sizing: border-box; }
.input-group input:focus, .input-group textarea:focus { border-color: #ffca28; box-shadow: 0 0 10px rgba(255, 202, 40, 0.1); outline: none; }
.assets-wrapper { padding-bottom: 10px; overflow-x: auto; }
.assets-grid { display: flex; gap: 15px; }
.asset-item { min-width: 90px; height: 90px; border: 2px solid #333; border-radius: 8px; background: #000; cursor: pointer; position: relative; transition: 0.3s; }
.asset-item img { width: 100%; height: 100%; object-fit: contain; padding: 5px; box-sizing: border-box; }
.asset-item.selected { border-color: #00e676; box-shadow: 0 0 15px rgba(0, 230, 118, 0.3); }
.asset-check { position: absolute; top: -8px; right: -8px; background: #00e676; color: #000; width: 20px; height: 20px; border-radius: 50%; font-size: 12px; display: flex; align-items: center; justify-content: center; opacity: 0; transform: scale(0); transition: 0.3s; }
.asset-item.selected .asset-check { opacity: 1; transform: scale(1); }
.rarity-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.rarity-card { padding: 15px; border: 1px solid #444; border-radius: 6px; cursor: pointer; text-align: center; background: #181818; transition: 0.2s; }
.rarity-card span { display: block; font-weight: bold; margin-bottom: 5px; }
.rarity-card small { color: #666; font-size: 0.8rem; }
.rarity-card.COMMON.selected { border-color: #bdbdbd; box-shadow: 0 0 10px #bdbdbd; }
.rarity-card.UNCOMMON.selected { border-color: #4caf50; box-shadow: 0 0 10px #4caf50; }
.rarity-card.RARE.selected { border-color: #42a5f5; box-shadow: 0 0 10px #42a5f5; }
.rarity-card.EPIC.selected { border-color: #ab47bc; box-shadow: 0 0 10px #ab47bc; }
.rarity-card.LEGENDARY.selected { border-color: #ffca28; box-shadow: 0 0 10px #ffca28; }
.rarity-card.MYTHIC.selected { border-color: #ef5350; box-shadow: 0 0 10px #ef5350; }
.stat-type-selector { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 10px; }
.stat-chip { padding: 8px 15px; background: #333; border-radius: 20px; cursor: pointer; border: 1px solid #555; transition: 0.2s; color: #aaa; }
.stat-chip.active { background: #ffca28; color: #000; border-color: #ffca28; font-weight: bold; box-shadow: 0 0 10px rgba(255, 202, 40, 0.3); }
.big-input { font-size: 1.5rem; color: #00e676 !important; font-weight: bold; text-align: center; }
.sub-stats-container { margin-top: 15px; }
.sub-stat-row { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }
.row-index { color: #555; font-family: monospace; }
.sub-stat-select { width: 150px; }
.main-stat-select { width: 200px; }
.distribution-box { margin-bottom: 20px; }
.dist-options { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 10px; }
.dist-card { background: #1a1a1a; border: 1px solid #444; border-radius: 8px; padding: 15px; cursor: pointer; display: flex; align-items: center; gap: 10px; transition: 0.3s; }
.dist-card:hover { background: #222; transform: translateY(-2px); }
.dist-card.active { border-color: #00e676; background: rgba(0, 230, 118, 0.1); }
.dist-card.active i { color: #00e676; }
.dist-card.limited.active { border-color: #ffca28; background: rgba(255, 202, 40, 0.1); }
.dist-card.limited.active i { color: #ffca28; }
.dist-card i { font-size: 1.5rem; color: #666; }
.dist-card strong { display: block; color: #eee; }
.dist-card small { color: #888; font-size: 0.8rem; }
.wizard-actions { margin-top: auto; padding-top: 20px; border-top: 1px solid #333; display: flex; justify-content: space-between; }
.btn-next, .btn-submit-final { background: linear-gradient(45deg, #ffd700, #ffa000); color: #000; border: none; padding: 10px 30px; border-radius: 30px; font-weight: bold; cursor: pointer; box-shadow: 0 4px 15px rgba(255, 160, 0, 0.3); transition: 0.3s; }
.btn-next:disabled { background: #444; color: #888; box-shadow: none; cursor: not-allowed; }
.btn-next:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 160, 0, 0.5); }
.btn-back { background: transparent; border: 1px solid #555; color: #aaa; padding: 8px 20px; border-radius: 30px; cursor: pointer; transition: 0.3s; }
.btn-back:hover { border-color: #fff; color: #fff; }
.summary-card { background: #121212; padding: 15px; border-radius: 8px; border: 1px solid #333; margin-bottom: 20px; }
.item-preview { display: flex; gap: 15px; align-items: center; }
.preview-img { width: 64px; height: 64px; border: 1px solid #444; border-radius: 4px; background: #000; object-fit: contain; }
.preview-info h4 { margin: 0; font-size: 1.2rem; }
.preview-info p { margin: 5px 0; color: #888; font-size: 0.9rem; }
.preview-stats { color: #00e676; font-family: monospace; }
.pricing-box { display: flex; justify-content: space-between; align-items: center; background: #1a1a1a; padding: 15px; border-radius: 8px; margin-bottom: 15px; border-left: 4px solid #ffca28; }
.suggested-price { color: #00e676; font-size: 1.5rem; margin-left: 10px; }
.price-input { width: 150px !important; text-align: right; color: #ffca28 !important; font-weight: bold; }
.fade-in { animation: fadeIn 0.5s ease; }
.fade-in-down { animation: fadeInDown 0.6s ease; }
.fade-in-up { animation: fadeInUp 0.6s ease; }
.zoom-in { animation: zoomIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes zoomIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.unit-label { font-size: 1.2rem; color: #888; font-weight: bold; }
.sub-stats-preview { margin-top: 5px; display: flex; flex-direction: column; gap: 2px; }
.preview-sub { color: #a5d6a7; font-family: monospace; }

/* Item Selector */
.item-selector-container { margin-bottom: 10px; background: rgba(0,0,0,0.2); border: 1px solid #444; border-radius: 6px; padding: 10px; }
.filter-row { display: flex; gap: 10px; margin-bottom: 10px; }
.search-input { flex: 1; padding: 8px; background: rgba(0,0,0,0.5); border: 1px solid #555; color: #fff; border-radius: 4px; }
.type-filter { width: 120px; padding: 8px; background: rgba(0,0,0,0.5); border: 1px solid #555; color: #fff; border-radius: 4px; }
.item-list { max-height: 200px; overflow-y: auto; background: rgba(0,0,0,0.3); border-radius: 4px; border: 1px solid #333; }
.select-item-row { display: flex; justify-content: space-between; padding: 8px 12px; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.05); transition: 0.2s; }
.select-item-row:hover { background: rgba(255, 215, 0, 0.1); }
.select-item-row.active { background: rgba(255, 215, 0, 0.2); border-left: 3px solid #ffca28; }
.item-name { color: #e0e0e0; font-weight: 500; }
.item-rarity-badge { font-size: 0.75rem; padding: 2px 6px; border-radius: 3px; background: rgba(0,0,0,0.3); }
.empty-hint { padding: 15px; text-align: center; color: #777; font-style: italic; }
.selected-preview { display: flex; align-items: center; gap: 15px; margin-top: 15px; padding: 10px; background: rgba(255, 215, 0, 0.05); border: 1px solid #ffca28; border-radius: 6px; animation: fadeIn 0.3s; }
.preview-icon { width: 40px; height: 40px; object-fit: contain; background: #000; border-radius: 4px; border: 1px solid #555; }
.preview-details { display: flex; flex-direction: column; }
.preview-name { font-weight: bold; font-size: 1rem; }
.preview-details small { color: #888; }

/* EDIT MODAL STYLES */
.edit-theme { background: #1a1a1a; border: 2px solid #42a5f5; width: 600px; max-width: 95%; overflow: hidden; box-shadow: 0 0 50px rgba(66, 165, 245, 0.3); }
.edit-theme .modal-header { background: #1565c0; color: #fff; padding: 15px; text-align: center; font-weight: bold; font-size: 1.2rem; text-transform: uppercase; }
.edit-preview { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; padding: 15px; background: rgba(0,0,0,0.3); border-radius: 8px; }
.preview-large { width: 80px; height: 80px; object-fit: contain; border: 1px solid #444; background: #000; border-radius: 6px; }
.preview-meta { display: flex; flex-direction: column; }
.preview-meta strong { font-size: 1.4rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; padding: 0 10px; }
.form-grid .full { grid-column: span 2; }
.stats-edit-section { background: rgba(0,0,0,0.2); padding: 15px; border-radius: 6px; border: 1px solid #333; margin-top: 10px; }
.section-label { display: block; color: #42a5f5; font-weight: bold; margin-bottom: 10px; text-transform: uppercase; font-size: 0.8rem; }
.stats-grid-small { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.stats-grid-small div { display: flex; flex-direction: column; gap: 5px; }
.stats-grid-small label { font-size: 0.75rem; color: #aaa; text-align: center; }
.stats-grid-small input { text-align: center; padding: 5px; background: rgba(0,0,0,0.4); border: 1px solid #444; color: #fff; border-radius: 4px; }
.btn-confirm-edit { background: #42a5f5; color: #fff; border: none; padding: 10px 30px; border-radius: 30px; font-weight: bold; cursor: pointer; transition: 0.2s; }
.btn-confirm-edit:hover { background: #1e88e5; box-shadow: 0 0 15px rgba(66, 165, 245, 0.4); }
.edit { background: linear-gradient(135deg, #ffb74d, #f57c00); }
.warning-zone { border: 1px solid #ef5350; padding: 10px; border-radius: 4px; background: rgba(239, 83, 80, 0.1); }
.disabled-input { opacity: 0.6; cursor: not-allowed; background: #222; }
</style>