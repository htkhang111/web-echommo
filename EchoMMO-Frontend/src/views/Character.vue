<!-- <template>
  <div class="page-container character-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="char-wrapper">
      <div class="char-grid">
        
        <div class="panel stats-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>BẢN SẮC</h3>
            <div class="decor-line"></div>
          </div>
          
          <div class="stats-body">
            <div class="stat-row level-row">
              <span class="label">CẢNH GIỚI</span>
              <span class="value highlight">Luyện Khí Tầng {{ charStore.character?.lv || 1 }}</span>
            </div>

            <div class="divider"></div>

            <div class="stat-row">
              <span class="label"><i class="fas fa-fist-raised"></i> Công Lực</span>
              <span class="value atk">{{ charStore.character?.baseAtk || 0 }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-shield-alt"></i> Hộ Thể</span>
              <span class="value def">{{ charStore.character?.baseDef || 0 }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-wind"></i> Thân Pháp</span>
              <span class="value speed">{{ charStore.character?.baseSpeed || 0 }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-bolt"></i> Bạo Kích</span>
              <span class="value crit">{{ charStore.character?.baseCritRate || 0 }}%</span>
            </div>
          </div>
        </div>

        <div class="panel hero-panel">
          <div class="aura-bg"></div>
          
          <div class="char-figure">
            <i class="fas fa-user-ninja"></i>
          </div>

          <div 
            class="equip-slot head-slot" 
            :class="{ filled: equipment.HELMET }"
            @mousedown.left="unequipSlow('HELMET')" 
            @contextmenu.prevent="unequipFast('HELMET')"
            title="Mũ"
          >
            <div class="slot-frame">
              <img v-if="equipment.HELMET" :src="equipment.HELMET.item.imageUrl" />
              <i v-else class="fas fa-hat-cowboy-side placeholder"></i>
            </div>
            <div class="slot-label">Mão</div>
          </div>

          <div 
            class="equip-slot weapon-slot" 
            :class="{ filled: equipment.WEAPON }"
            @mousedown.left="unequipSlow('WEAPON')" 
            @contextmenu.prevent="unequipFast('WEAPON')"
            title="Vũ Khí"
          >
            <div class="slot-frame">
              <img v-if="equipment.WEAPON" :src="equipment.WEAPON.item.imageUrl" />
              <i v-else class="fas fa-gavel placeholder"></i>
            </div>
            <div class="slot-label">Binh Khí</div>
          </div>

          <div 
            class="equip-slot body-slot" 
            :class="{ filled: equipment.ARMOR }"
            @mousedown.left="unequipSlow('ARMOR')" 
            @contextmenu.prevent="unequipFast('ARMOR')"
            title="Y Phục"
          >
            <div class="slot-frame">
              <img v-if="equipment.ARMOR" :src="equipment.ARMOR.item.imageUrl" />
              <i v-else class="fas fa-tshirt placeholder"></i>
            </div>
            <div class="slot-label">Y Phục</div>
          </div>

          <div 
            class="equip-slot boots-slot" 
            :class="{ filled: equipment.BOOTS }"
            @mousedown.left="unequipSlow('BOOTS')" 
            @contextmenu.prevent="unequipFast('BOOTS')"
            title="Giày"
          >
            <div class="slot-frame">
              <img v-if="equipment.BOOTS" :src="equipment.BOOTS.item.imageUrl" />
              <i v-else class="fas fa-socks placeholder"></i>
            </div>
            <div class="slot-label">Hài</div>
          </div>
        </div>

        <div class="panel bag-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>HÀNH NANG</h3>
            <div class="decor-line"></div>
          </div>
          <div class="bag-info">
            Sức chứa: {{ inventoryStore.items.length }} / 50
          </div>
          
          <div class="mini-grid custom-scroll">
            <div 
              v-for="item in inventoryStore.items" 
              :key="item.userItemId" 
              class="mini-slot"
              :class="[
                'rarity-' + (item.item.rarity || 'C'),
                { 'is-equipped': item.isEquipped }
              ]" 
              @click="equip(item)" 
              :title="item.item.name"
            >
              <img :src="item.item.imageUrl" />
              <span class="qty" v-if="item.quantity > 1">{{ item.quantity }}</span>
              <div class="equipped-dot" v-if="item.isEquipped"></div>
            </div>
            <div v-for="n in (20 - (inventoryStore.items.length % 20))" :key="'e'+n" class="mini-slot empty"></div>
          </div>
        </div>

      </div>
    </div>

    <transition name="fade">
      <div v-if="showUnequipModal" class="modal-overlay" @click.self="closeModal">
        <div class="dark-modal">
          <div class="modal-border-top"></div>
          <div class="modal-content">
            <h3 class="modal-title">GỠ BỎ TRANG BỊ</h3>
            
            <div class="item-preview-box">
               <img v-if="itemToUnequip" :src="itemToUnequip.item.imageUrl" class="preview-img">
               <div class="preview-info" v-if="itemToUnequip">
                 <span class="p-name">{{ itemToUnequip.item.name }}</span>
                 <span class="p-type">Sẽ trở về hành trang</span>
               </div>
            </div>

            <div class="modal-actions">
              <button class="btn-wood cancel" @click="closeModal">HỦY BỎ</button>
              <button class="btn-wood confirm" @click="confirmUnequip">XÁC NHẬN</button>
            </div>
          </div>
          <div class="modal-border-bot"></div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'; 
import { useCharacterStore } from '@/stores/characterStore';
import { useInventoryStore } from '@/stores/inventoryStore';

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore();
const equipment = computed(() => inventoryStore.equippedItems);

// LOGIC MODAL
const showUnequipModal = ref(false);
const itemToUnequip = ref(null);
const slotToUnequip = ref(null);

const unequipSlow = (slotType) => {
    const item = equipment.value[slotType];
    if (item) {
        itemToUnequip.value = item;
        slotToUnequip.value = slotType;
        showUnequipModal.value = true;
    }
};

const unequipFast = async (slotType) => {
    const item = equipment.value[slotType];
    if (item) {
        await inventoryStore.unequipItem(item.userItemId);
    }
};

const confirmUnequip = async () => {
    if (itemToUnequip.value) {
        await inventoryStore.unequipItem(itemToUnequip.value.userItemId);
    }
    closeModal();
};

const closeModal = () => {
    showUnequipModal.value = false;
    itemToUnequip.value = null;
    slotToUnequip.value = null;
};

const equip = async (item) => {
    const types = ['WEAPON', 'ARMOR', 'HELMET', 'BOOTS'];
    if (types.includes(item.item.type)) {
        if (!item.isEquipped) await inventoryStore.equipItem(item.userItemId);
    }
};

onMounted(() => { 
    charStore.fetchCharacter(); 
    inventoryStore.fetchInventory(); 
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- PALETTE --- */
:root {
  --wood-dark: #3e2723;
  --wood-mid: #4e342e;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --gold-dim: #ffe082;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;
}

.wuxia-dark-theme {
    background-color: #212121;
    min-height: 100vh;
    font-family: "Noto Serif TC", serif;
    color: var(--text-light);
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
}

/* Background Layers */
.ink-bg-layer { position: absolute; inset: 0; z-index: 0; background-color: #3e2723; }
.mountain-bg {
    position: absolute; inset: 0;
    background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
    background-size: cover; filter: sepia(40%) brightness(0.5) contrast(1.2); opacity: 0.6;
}
.fog-anim {
    position: absolute; inset: 0;
    background: linear-gradient(to top, #261815 10%, transparent 90%);
}

.char-wrapper {
    position: relative;
    z-index: 10;
    width: 100%;
    max-width: 1100px;
    padding: 20px;
}

.char-grid {
    display: grid;
    grid-template-columns: 300px 1fr 300px;
    gap: 20px;
    height: 600px;
}

/* --- PANELS COMMON --- */
.panel {
    background: rgba(30, 20, 15, 0.95);
    border: 3px solid var(--wood-light);
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 10px 30px rgba(0,0,0,0.8);
    position: relative;
}

.panel-header {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 15px;
    background: rgba(0,0,0,0.2);
    border-bottom: 2px solid var(--wood-light);
    gap: 10px;
}

.panel-header h3 {
    margin: 0;
    color: var(--gold);
    font-size: 1.2rem;
    font-weight: 900;
    letter-spacing: 2px;
    text-shadow: 0 2px 2px #000;
}

.decor-line {
    height: 2px;
    width: 30px;
    background: var(--gold-dim);
    opacity: 0.5;
}

/* --- LEFT PANEL: STATS --- */
.stats-body {
    padding: 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.stat-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

.label {
    color: #a1887f;
    font-weight: bold;
    display: flex;
    align-items: center;
    gap: 10px;
}
.label i { width: 20px; text-align: center; color: var(--gold); }

.value { font-weight: bold; font-size: 1.1rem; }
.highlight { color: var(--gold); text-shadow: 0 0 5px rgba(255, 215, 0, 0.5); }
.atk { color: #ef5350; }
.def { color: #42a5f5; }
.speed { color: #66bb6a; }
.crit { color: #ab47bc; }

.divider { height: 2px; background: var(--wood-light); margin: 5px 0; }

/* --- CENTER PANEL: HERO --- */
.hero-panel {
    background: radial-gradient(circle at center, #4e342e 0%, #261815 100%);
    position: relative;
    border-color: var(--gold); /* Viền vàng nổi bật */
    overflow: hidden;
}

.aura-bg {
    position: absolute;
    top: 50%; left: 50%;
    transform: translate(-50%, -50%);
    width: 300px; height: 300px;
    background: radial-gradient(circle, rgba(255, 236, 179, 0.1) 0%, transparent 70%);
    animation: pulseAura 4s infinite;
}

.char-figure {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10rem;
    color: rgba(0,0,0,0.5);
    filter: drop-shadow(0 0 10px rgba(255,255,255,0.1));
    z-index: 1;
}

/* Slots Positioning */
.equip-slot {
    position: absolute;
    z-index: 5;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    transition: transform 0.2s;
}
.equip-slot:hover { transform: scale(1.1); }

.slot-frame {
    width: 64px;
    height: 64px;
    background: rgba(0,0,0,0.6);
    border: 2px solid #8d6e63;
    border-radius: 8px; /* Bo góc nhẹ */
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 0 10px rgba(0,0,0,0.8);
    transition: 0.3s;
}

.equip-slot.filled .slot-frame {
    border-color: var(--gold);
    background: rgba(255, 236, 179, 0.05);
    box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
}

.slot-frame img { width: 90%; height: 90%; object-fit: contain; }
.placeholder { font-size: 1.5rem; color: #5d4037; }
.slot-label {
    font-size: 0.8rem;
    color: var(--gold-dim);
    text-shadow: 1px 1px 2px #000;
    background: rgba(0,0,0,0.7);
    padding: 2px 6px;
    border-radius: 4px;
}

/* Positions */
.head-slot { top: 40px; left: 50%; transform: translateX(-50%); }
.head-slot:hover { transform: translateX(-50%) scale(1.1); }

.weapon-slot { top: 50%; left: 30px; transform: translateY(-50%); }
.weapon-slot:hover { transform: translateY(-50%) scale(1.1); }

.body-slot { top: 50%; right: 30px; transform: translateY(-50%); }
.body-slot:hover { transform: translateY(-50%) scale(1.1); }

.boots-slot { bottom: 40px; left: 50%; transform: translateX(-50%); }
.boots-slot:hover { transform: translateX(-50%) scale(1.1); }

/* --- RIGHT PANEL: BAG --- */
.bag-info {
    text-align: right;
    padding: 5px 15px;
    font-size: 0.8rem;
    color: #a1887f;
}

.mini-grid {
    flex: 1;
    padding: 10px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    grid-auto-rows: 60px;
    gap: 8px;
    overflow-y: auto;
    align-content: start;
}

.mini-slot {
    background: rgba(0,0,0,0.4);
    border: 1px solid #4e342e;
    border-radius: 4px;
    position: relative;
    cursor: pointer;
    transition: 0.2s;
}
.mini-slot.empty { opacity: 0.2; pointer-events: none; }

.mini-slot:hover { border-color: var(--gold); background: rgba(255,255,255,0.05); }

.mini-slot img { width: 100%; height: 100%; padding: 4px; object-fit: contain; }
.qty {
    position: absolute; bottom: 2px; right: 2px;
    background: rgba(0,0,0,0.8); color: #fff;
    font-size: 0.7rem; padding: 0 4px; border-radius: 2px;
}
.equipped-dot {
    position: absolute; top: 3px; left: 3px;
    width: 6px; height: 6px; background: #4caf50;
    border-radius: 50%; box-shadow: 0 0 5px #4caf50;
}

/* Rarity Borders */
.rarity-C { border-bottom: 2px solid #9e9e9e; }
.rarity-S { border-bottom: 2px solid var(--gold); }

/* --- MODAL --- */
.modal-overlay {
    position: fixed; inset: 0;
    background: rgba(0,0,0,0.85);
    display: flex; justify-content: center; align-items: center;
    z-index: 1000; backdrop-filter: blur(2px);
}
.dark-modal {
    width: 350px; background: var(--wood-dark);
    position: relative; box-shadow: 0 0 30px #000;
}
.modal-border-top { height: 10px; background: #2d1e1b; box-shadow: inset 0 -2px 5px rgba(0,0,0,0.5); }
.modal-border-bot { height: 10px; background: #2d1e1b; box-shadow: inset 0 2px 5px rgba(0,0,0,0.5); }
.modal-content { padding: 20px; text-align: center; }

.modal-title { color: var(--red-seal); margin: 0 0 15px 0; border-bottom: 1px solid #5d4037; padding-bottom: 10px; }

.item-preview-box {
    background: rgba(0,0,0,0.3); border: 1px solid #5d4037;
    padding: 10px; display: flex; align-items: center; gap: 15px; margin-bottom: 20px;
}
.preview-img { width: 50px; height: 50px; border: 1px solid var(--gold); padding: 2px; }
.preview-info { display: flex; flex-direction: column; text-align: left; }
.p-name { color: var(--gold); font-weight: bold; }
.p-type { font-size: 0.8rem; color: #9e9e9e; }

.modal-actions { display: flex; gap: 10px; }
.btn-wood { flex: 1; padding: 10px; border: none; font-weight: bold; cursor: pointer; transition: 0.2s; }
.cancel { background: #4e342e; color: #bdbdbd; }
.confirm { background: var(--red-seal); color: #fff; }
.confirm:hover { background: #d32f2f; }

@keyframes pulseAura {
    0% { transform: translate(-50%, -50%) scale(0.9); opacity: 0.5; }
    50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.8; }
    100% { transform: translate(-50%, -50%) scale(0.9); opacity: 0.5; }
}

/* Responsive */
@media (max-width: 900px) {
    .char-grid { grid-template-columns: 1fr; height: auto; }
    .hero-panel { height: 400px; }
}
</style> -->

<!-- code moi -->
<!-- <template>
  <div class="page-container character-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="char-wrapper">
      <div class="char-grid">
        <div class="panel stats-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>BẢN SẮC</h3>
            <div class="decor-line"></div>
          </div>

          <div class="stats-body">
            <div class="stat-row level-row">
              <span class="label">CẢNH GIỚI</span>
              <span class="value highlight"
                >Luyện Khí Tầng {{ charStore.character?.lv || 1 }}</span
              >
            </div>
            <div class="divider"></div>
            <div class="stat-row">
              <span class="label"
                ><i class="fas fa-fist-raised"></i> Công Lực</span
              ><span class="value atk">{{
                charStore.character?.baseAtk || 0
              }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-shield-alt"></i> Hộ Thể</span
              ><span class="value def">{{
                charStore.character?.baseDef || 0
              }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-wind"></i> Thân Pháp</span
              ><span class="value speed">{{
                charStore.character?.baseSpeed || 0
              }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-bolt"></i> Bạo Kích</span
              ><span class="value crit"
                >{{ charStore.character?.baseCritRate || 0 }}%</span
              >
            </div>
          </div>
        </div>

        <div class="panel hero-panel">
          <div class="aura-bg"></div>

          <div class="char-figure">
            <img :src="userSkinImg" class="skin-preview" />
          </div>

          <div
            class="equip-slot necklace-slot"
            :class="{ filled: equipment.NECKLACE }"
            @mousedown.left="unequipSlow('NECKLACE')"
            title="Dây Chuyền"
          >
            <div class="slot-frame">
              <img
                v-if="equipment.NECKLACE"
                :src="resolveItemImage(equipment.NECKLACE.item.imageUrl)"
              />
              <i v-else class="fas fa-gem placeholder"></i>
            </div>
          </div>

          <div
            class="equip-slot weapon-slot"
            :class="{ filled: equipment.WEAPON }"
            @mousedown.left="unequipSlow('WEAPON')"
            title="Vũ Khí"
          >
            <div class="slot-frame">
              <img
                v-if="equipment.WEAPON"
                :src="resolveItemImage(equipment.WEAPON.item.imageUrl)"
              />
              <i v-else class="fas fa-gavel placeholder"></i>
            </div>
          </div>

          <div
            class="equip-slot ring-slot"
            :class="{ filled: equipment.RING }"
            @mousedown.left="unequipSlow('RING')"
            title="Nhẫn"
          >
            <div class="slot-frame">
              <img
                v-if="equipment.RING"
                :src="resolveItemImage(equipment.RING.item.imageUrl)"
              />
              <i v-else class="fas fa-ring placeholder"></i>
            </div>
          </div>

          <div
            class="equip-slot head-slot"
            :class="{ filled: equipment.HELMET }"
            @mousedown.left="unequipSlow('HELMET')"
            title="Mũ"
          >
            <div class="slot-frame">
              <img
                v-if="equipment.HELMET"
                :src="resolveItemImage(equipment.HELMET.item.imageUrl)"
              />
              <i v-else class="fas fa-hat-cowboy-side placeholder"></i>
            </div>
          </div>

          <div
            class="equip-slot body-slot"
            :class="{ filled: equipment.ARMOR }"
            @mousedown.left="unequipSlow('ARMOR')"
            title="Y Phục"
          >
            <div class="slot-frame">
              <img
                v-if="equipment.ARMOR"
                :src="resolveItemImage(equipment.ARMOR.item.imageUrl)"
              />
              <i v-else class="fas fa-tshirt placeholder"></i>
            </div>
          </div>

          <div
            class="equip-slot boots-slot"
            :class="{ filled: equipment.BOOTS }"
            @mousedown.left="unequipSlow('BOOTS')"
            title="Giày"
          >
            <div class="slot-frame">
              <img
                v-if="equipment.BOOTS"
                :src="resolveItemImage(equipment.BOOTS.item.imageUrl)"
              />
              <i v-else class="fas fa-socks placeholder"></i>
            </div>
          </div>
        </div>

        <div class="panel bag-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>HÀNH NANG</h3>
            <div class="decor-line"></div>
          </div>
          <div class="bag-info">
            Sức chứa: {{ inventoryStore.items.length }} / 50
          </div>

          <div class="mini-grid custom-scroll">
            <div
              v-for="item in inventoryStore.items"
              :key="item.userItemId"
              class="mini-slot"
              :class="[
                'rarity-' + (item.item.rarity || 'C'),
                { 'is-equipped': item.isEquipped },
              ]"
              @click="equip(item)"
              :title="item.item.name"
            >
              <img :src="resolveItemImage(item.item.imageUrl)" />
              <span class="qty" v-if="item.quantity > 1">{{
                item.quantity
              }}</span>
              <div class="equipped-dot" v-if="item.isEquipped"></div>
            </div>
            <div
              v-for="n in 20 - (inventoryStore.items.length % 20)"
              :key="'e' + n"
              class="mini-slot empty"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div
        v-if="showUnequipModal"
        class="modal-overlay"
        @click.self="closeModal"
      >
        <div class="dark-modal">
          <div class="modal-border-top"></div>
          <div class="modal-content">
            <h3 class="modal-title">GỠ BỎ TRANG BỊ</h3>
            <div class="item-preview-box">
              <img
                v-if="itemToUnequip"
                :src="resolveItemImage(itemToUnequip.item.imageUrl)"
                class="preview-img"
              />
              <div class="preview-info" v-if="itemToUnequip">
                <span class="p-name">{{ itemToUnequip.item.name }}</span>
                <span class="p-type">Sẽ trở về hành trang</span>
              </div>
            </div>
            <div class="modal-actions">
              <button class="btn-wood cancel" @click="closeModal">
                HỦY BỎ
              </button>
              <button class="btn-wood confirm" @click="confirmUnequip">
                XÁC NHẬN
              </button>
            </div>
          </div>
          <div class="modal-border-bot"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useInventoryStore } from "@/stores/inventoryStore";
import { useAuthStore } from "@/stores/authStore";
import { resolveItemImage, getCurrentSkin } from "@/utils/assetHelper"; // Import hàm helper mới

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const equipment = computed(() => inventoryStore.equippedItems);

const userSkinImg = computed(
  () => getCurrentSkin(authStore.user?.avatarUrl).sprites.idle,
);

// --- LOGIC MODAL & EQUIP (Giữ nguyên) ---
const showUnequipModal = ref(false);
const itemToUnequip = ref(null);

const unequipSlow = (slotType) => {
  const item = equipment.value[slotType];
  if (item) {
    itemToUnequip.value = item;
    showUnequipModal.value = true;
  }
};

const confirmUnequip = async () => {
  if (itemToUnequip.value)
    await inventoryStore.unequipItem(itemToUnequip.value.userItemId);
  closeModal();
};

const closeModal = () => {
  showUnequipModal.value = false;
  itemToUnequip.value = null;
};

const equip = async (item) => {
  const types = ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"];
  if (types.includes(item.item.type)) {
    if (!item.isEquipped) await inventoryStore.equipItem(item.userItemId);
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  inventoryStore.fetchInventory();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- CORE VARIABLES --- */
:root {
  --wood-dark: #3e2723;
  --wood-mid: #4e342e;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --gold-dim: #ffe082;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;
}

.wuxia-dark-theme {
  background-color: #212121;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* ... (Giữ các CSS Background cũ) ... */
.ink-bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-color: #3e2723;
}

.mountain-bg {
  position: absolute;
  inset: 0;
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

.char-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1100px;
  padding: 20px;
}

.char-grid {
  display: grid;
  grid-template-columns: 300px 1fr 300px;
  gap: 20px;
  height: 600px;
}

.panel {
  background: rgba(30, 20, 15, 0.95);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 2px solid var(--wood-light);
  gap: 10px;
}

.panel-header h3 {
  margin: 0;
  color: var(--gold);
  font-size: 1.2rem;
  font-weight: 900;
  letter-spacing: 2px;
  text-shadow: 0 2px 2px #000;
}

.decor-line {
  height: 2px;
  width: 30px;
  background: var(--gold-dim);
  opacity: 0.5;
}

.stats-body {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

.label {
  color: #a1887f;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
}

.label i {
  width: 20px;
  text-align: center;
  color: var(--gold);
}

.value {
  font-weight: bold;
  font-size: 1.1rem;
}

.highlight {
  color: var(--gold);
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}

.atk {
  color: #ef5350;
}

.def {
  color: #42a5f5;
}

.speed {
  color: #66bb6a;
}

.crit {
  color: #ab47bc;
}

.divider {
  height: 2px;
  background: var(--wood-light);
  margin: 5px 0;
}

/* --- CENTER PANEL: HERO (SỬA LAYOUT) --- */
.hero-panel {
  background: radial-gradient(circle at center, #4e342e 0%, #261815 100%);
  position: relative;
  border-color: var(--gold);
  overflow: hidden;
}

.aura-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 300px;
  height: 300px;
  background: radial-gradient(
    circle,
    rgba(255, 236, 179, 0.1) 0%,
    transparent 70%
  );
  animation: pulseAura 4s infinite;
}

.char-figure {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.skin-preview {
  height: 150px;
  width: 150px;
  object-fit: contain;
  image-rendering: pixelated;
  transform: scale(2);
  filter: drop-shadow(0 5px 10px rgba(0, 0, 0, 0.8));
}

/* Slots Positioning (6 Món) */
.equip-slot {
  position: absolute;
  z-index: 5;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  transition: transform 0.2s;
}

.equip-slot:hover {
  transform: scale(1.1);
}

.slot-frame {
  width: 56px;
  height: 56px;
  background: rgba(0, 0, 0, 0.6);
  border: 2px solid #8d6e63;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.8);
  transition: 0.3s;
}

.equip-slot.filled .slot-frame {
  border-color: var(--gold);
  background: rgba(255, 236, 179, 0.05);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
}

.slot-frame img {
  width: 90%;
  height: 90%;
  object-fit: contain;
}

.placeholder {
  font-size: 1.5rem;
  color: #5d4037;
}

/* --- POSITIONING 6 SLOTS (Trái 3 - Phải 3) --- */
/* Cột Trái */
.necklace-slot {
  top: 15%;
  left: 15%;
}

.weapon-slot {
  top: 50%;
  left: 5%;
  transform: translateY(-50%);
}

.ring-slot {
  bottom: 15%;
  left: 15%;
}

/* Cột Phải */
.head-slot {
  top: 15%;
  right: 15%;
}

.body-slot {
  top: 50%;
  right: 5%;
  transform: translateY(-50%);
}

.boots-slot {
  bottom: 15%;
  right: 15%;
}

/* Override hover transform để giữ vị trí */
.weapon-slot:hover,
.body-slot:hover {
  transform: translateY(-50%) scale(1.1);
}

/* --- RIGHT PANEL & MODAL (Giữ nguyên) --- */
.bag-info {
  text-align: right;
  padding: 5px 15px;
  font-size: 0.8rem;
  color: #a1887f;
}

.mini-grid {
  flex: 1;
  padding: 10px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: 60px;
  gap: 8px;
  overflow-y: auto;
  align-content: start;
}

.mini-slot {
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid #4e342e;
  border-radius: 4px;
  position: relative;
  cursor: pointer;
  transition: 0.2s;
}

.mini-slot.empty {
  opacity: 0.2;
  pointer-events: none;
}

.mini-slot:hover {
  border-color: var(--gold);
  background: rgba(255, 255, 255, 0.05);
}

.mini-slot img {
  width: 100%;
  height: 100%;
  padding: 4px;
  object-fit: contain;
}

.qty {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 0.7rem;
  padding: 0 4px;
  border-radius: 2px;
}

.equipped-dot {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 6px;
  height: 6px;
  background: #4caf50;
  border-radius: 50%;
  box-shadow: 0 0 5px #4caf50;
}

.rarity-C {
  border-bottom: 2px solid #9e9e9e;
}

.rarity-S {
  border-bottom: 2px solid var(--gold);
}

/* Modal styles (lược bớt cho gọn, dùng lại từ code cũ) */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.dark-modal {
  width: 350px;
  background: var(--wood-dark);
  position: relative;
  box-shadow: 0 0 30px #000;
}

.modal-content {
  padding: 20px;
  text-align: center;
}

.item-preview-box {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4037;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.preview-img {
  width: 50px;
  height: 50px;
  border: 1px solid var(--gold);
  padding: 2px;
}

.modal-actions {
  display: flex;
  gap: 10px;
}

.btn-wood {
  flex: 1;
  padding: 10px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
}

.cancel {
  background: #4e342e;
  color: #bdbdbd;
}

.confirm {
  background: var(--red-seal);
  color: #fff;
}

@keyframes pulseAura {
  0% {
    transform: translate(-50%, -50%) scale(0.9);
    opacity: 0.5;
  }

  50% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.8;
  }

  100% {
    transform: translate(-50%, -50%) scale(0.9);
    opacity: 0.5;
  }
}

@media (max-width: 900px) {
  .char-grid {
    grid-template-columns: 1fr;
    height: auto;
  }

  .hero-panel {
    height: 400px;
  }
}
</style> -->
<template>
  <div class="page-container character-page wuxia-dark-theme">
    <div class="ink-bg-layer">
      <div class="mountain-bg"></div>
      <div class="fog-anim"></div>
    </div>

    <div class="char-wrapper">
      <div class="char-grid">
        <div class="panel stats-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>BẢN SẮC</h3>
            <div class="decor-line"></div>
          </div>

          <div class="stats-body">
            <div class="stat-row level-row">
              <span class="label">CẢNH GIỚI</span>
              <span class="value highlight"
                >Luyện Khí Tầng {{ charStore.character?.lv || 1 }}</span
              >
            </div>
            <div class="divider"></div>
            <div class="stat-row">
              <span class="label"
                ><i class="fas fa-fist-raised"></i> Công Lực</span
              ><span class="value atk">{{
                charStore.character?.baseAtk || 0
              }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-shield-alt"></i> Hộ Thể</span
              ><span class="value def">{{
                charStore.character?.baseDef || 0
              }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-wind"></i> Thân Pháp</span
              ><span class="value speed">{{
                charStore.character?.baseSpeed || 0
              }}</span>
            </div>
            <div class="stat-row">
              <span class="label"><i class="fas fa-bolt"></i> Bạo Kích</span
              ><span class="value crit"
                >{{ charStore.character?.baseCritRate || 0 }}%</span
              >
            </div>
          </div>
        </div>

        <div class="panel hero-panel">
          <div class="aura-bg"></div>

          <div class="char-figure">
            <img :src="userSkinImg" class="skin-preview" />
          </div>

          <div
            v-for="(slot, slotName) in SLOT_CONFIG"
            :key="slotName"
            class="equip-slot"
            :class="[slot.className, { filled: equipment[slotName] }]"
            @mousedown.left="unequipSlow(slotName)"
            :title="slot.label"
          >
            <div class="slot-frame">
              <img
                v-if="equipment[slotName]"
                :src="resolveItemImage(equipment[slotName].item.imageUrl)"
              />
              <i v-else :class="['fas placeholder', slot.icon]"></i>

              <div
                v-if="
                  equipment[slotName] &&
                  (equipment[slotName].enhanceLevel ||
                    equipment[slotName].level)
                "
                class="slot-level-tag"
                :class="
                  getLevelClass(
                    equipment[slotName].enhanceLevel ||
                      equipment[slotName].level,
                  )
                "
              >
                +{{
                  equipment[slotName].enhanceLevel || equipment[slotName].level
                }}
              </div>
            </div>
          </div>
        </div>

        <div class="panel bag-panel">
          <div class="panel-header">
            <div class="decor-line"></div>
            <h3>HÀNH NANG</h3>
            <div class="decor-line"></div>
          </div>
          <div class="bag-info">
            Sức chứa: {{ inventoryStore.items.length }} / 50
          </div>

          <div class="mini-grid custom-scroll">
            <div
              v-for="item in inventoryStore.items"
              :key="item.userItemId"
              class="mini-slot"
              :class="[
                'rarity-' + (item.item.rarity || 'C'),
                { 'is-equipped': item.isEquipped },
              ]"
              @click="equip(item)"
              :title="item.item.name"
            >
              <img :src="resolveItemImage(item.item.imageUrl)" />

              <div
                v-if="item.enhanceLevel || item.level"
                class="mini-level"
                :class="getLevelClass(item.enhanceLevel || item.level)"
              >
                +{{ item.enhanceLevel || item.level }}
              </div>

              <span class="qty" v-if="item.quantity > 1">{{
                item.quantity
              }}</span>
              <div class="equipped-dot" v-if="item.isEquipped"></div>
            </div>

            <div
              v-for="n in Math.max(0, 20 - (inventoryStore.items.length % 20))"
              :key="'e' + n"
              class="mini-slot empty"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div
        v-if="showUnequipModal"
        class="modal-overlay"
        @click.self="closeModal"
      >
        <div class="dark-modal">
          <div class="modal-border-top"></div>
          <div class="modal-content">
            <h3 class="modal-title">GỠ BỎ TRANG BỊ</h3>
            <div class="item-preview-box">
              <img
                v-if="itemToUnequip"
                :src="resolveItemImage(itemToUnequip.item.imageUrl)"
                class="preview-img"
              />
              <div class="preview-info" v-if="itemToUnequip">
                <span class="p-name">
                  {{ itemToUnequip.item.name }}
                  <span
                    class="level-text"
                    :class="
                      getLevelClass(
                        itemToUnequip.enhanceLevel || itemToUnequip.level,
                      )
                    "
                  >
                    (+{{
                      itemToUnequip.enhanceLevel || itemToUnequip.level || 0
                    }})
                  </span>
                </span>
                <span class="p-type">Sẽ trở về hành trang</span>
              </div>
            </div>
            <div class="modal-actions">
              <button class="btn-wood cancel" @click="closeModal">
                HỦY BỎ
              </button>
              <button class="btn-wood confirm" @click="confirmUnequip">
                XÁC NHẬN
              </button>
            </div>
          </div>
          <div class="modal-border-bot"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useCharacterStore } from "@/stores/characterStore";
import { useInventoryStore } from "@/stores/inventoryStore";
import { useAuthStore } from "@/stores/authStore";
import { resolveItemImage, getCurrentSkin } from "@/utils/assetHelper";

const charStore = useCharacterStore();
const inventoryStore = useInventoryStore();
const authStore = useAuthStore();
const equipment = computed(() => inventoryStore.equippedItems);

const userSkinImg = computed(
  () => getCurrentSkin(authStore.user?.avatarUrl).sprites.idle,
);

// Cấu hình slot để loop trong template cho gọn
const SLOT_CONFIG = {
  NECKLACE: { label: "Dây Chuyền", icon: "fa-gem", className: "necklace-slot" },
  WEAPON: { label: "Vũ Khí", icon: "fa-gavel", className: "weapon-slot" },
  RING: { label: "Nhẫn", icon: "fa-ring", className: "ring-slot" },
  HELMET: { label: "Mũ", icon: "fa-hat-cowboy-side", className: "head-slot" },
  ARMOR: { label: "Y Phục", icon: "fa-tshirt", className: "body-slot" },
  BOOTS: { label: "Giày", icon: "fa-socks", className: "boots-slot" },
};

// [MỚI] Hàm lấy class màu sắc level
const getLevelClass = (lv) => {
  if (!lv) return "";
  if (lv >= 15) return "lvl-red";
  if (lv >= 10) return "lvl-purple";
  if (lv >= 5) return "lvl-gold";
  return "lvl-white";
};

// --- LOGIC MODAL & EQUIP ---
const showUnequipModal = ref(false);
const itemToUnequip = ref(null);

const unequipSlow = (slotType) => {
  const item = equipment.value[slotType];
  if (item) {
    itemToUnequip.value = item;
    showUnequipModal.value = true;
  }
};

const confirmUnequip = async () => {
  if (itemToUnequip.value)
    await inventoryStore.unequipItem(itemToUnequip.value.userItemId);
  closeModal();
};

const closeModal = () => {
  showUnequipModal.value = false;
  itemToUnequip.value = null;
};

const equip = async (item) => {
  const types = ["WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE"];
  if (types.includes(item.item.type)) {
    if (!item.isEquipped) await inventoryStore.equipItem(item.userItemId);
  }
};

onMounted(() => {
  charStore.fetchCharacter();
  inventoryStore.fetchInventory();
});
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@400;700;900&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- CORE VARIABLES --- */
:root {
  --wood-dark: #3e2723;
  --wood-mid: #4e342e;
  --wood-light: #5d4037;
  --gold: #ffecb3;
  --gold-dim: #ffe082;
  --text-light: #f3f4f6;
  --red-seal: #b71c1c;
}

.wuxia-dark-theme {
  background-color: #212121;
  min-height: 100vh;
  font-family: "Noto Serif TC", serif;
  color: var(--text-light);
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.ink-bg-layer {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-color: #3e2723;
}

.mountain-bg {
  position: absolute;
  inset: 0;
  background-image: url("https://images.unsplash.com/photo-1518182170546-0766ce6fec56?q=80&w=2000&auto=format&fit=crop");
  background-size: cover;
  filter: sepia(40%) brightness(0.5) contrast(1.2);
  opacity: 0.6;
}

.fog-anim {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #261815 10%, transparent 90%);
}

.char-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1100px;
  padding: 20px;
}

.char-grid {
  display: grid;
  grid-template-columns: 300px 1fr 300px;
  gap: 20px;
  height: 600px;
}

.panel {
  background: rgba(30, 20, 15, 0.95);
  border: 3px solid var(--wood-light);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
  position: relative;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 2px solid var(--wood-light);
  gap: 10px;
}

.panel-header h3 {
  margin: 0;
  color: var(--gold);
  font-size: 1.2rem;
  font-weight: 900;
  letter-spacing: 2px;
  text-shadow: 0 2px 2px #000;
}

.decor-line {
  height: 2px;
  width: 30px;
  background: var(--gold-dim);
  opacity: 0.5;
}

.stats-body {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

.label {
  color: #a1887f;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
}

.label i {
  width: 20px;
  text-align: center;
  color: var(--gold);
}

.value {
  font-weight: bold;
  font-size: 1.1rem;
}

.highlight {
  color: var(--gold);
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}

.atk {
  color: #ef5350;
}
.def {
  color: #42a5f5;
}
.speed {
  color: #66bb6a;
}
.crit {
  color: #ab47bc;
}

.divider {
  height: 2px;
  background: var(--wood-light);
  margin: 5px 0;
}

.hero-panel {
  background: radial-gradient(circle at center, #4e342e 0%, #261815 100%);
  position: relative;
  border-color: var(--gold);
  overflow: hidden;
}

.aura-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 300px;
  height: 300px;
  background: radial-gradient(
    circle,
    rgba(255, 236, 179, 0.1) 0%,
    transparent 70%
  );
  animation: pulseAura 4s infinite;
}

.char-figure {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.skin-preview {
  height: 150px;
  width: 150px;
  object-fit: contain;
  image-rendering: pixelated;
  transform: scale(2);
  filter: drop-shadow(0 5px 10px rgba(0, 0, 0, 0.8));
}

.equip-slot {
  position: absolute;
  z-index: 5;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  transition: transform 0.2s;
}

.equip-slot:hover {
  transform: scale(1.1);
}

.slot-frame {
  width: 56px;
  height: 56px;
  background: rgba(0, 0, 0, 0.6);
  border: 2px solid #8d6e63;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.8);
  transition: 0.3s;
  position: relative; /* Quan trọng để đặt level tag */
}

.equip-slot.filled .slot-frame {
  border-color: var(--gold);
  background: rgba(255, 236, 179, 0.05);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
}

.slot-frame img {
  width: 90%;
  height: 90%;
  object-fit: contain;
}

.placeholder {
  font-size: 1.5rem;
  color: #5d4037;
}

/* [MỚI] STYLE LEVEL TAG TRÊN SLOT LỚN */
.slot-level-tag {
  position: absolute;
  bottom: -4px; /* Đẩy ra ngoài slot một chút hoặc để trong tùy ý */
  right: -4px;
  font-size: 10px;
  font-weight: 900;
  background: #000;
  border: 1px solid #444;
  border-radius: 4px;
  padding: 0 3px;
  z-index: 10;
  font-family: sans-serif;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.8);
}

/* --- COLORS --- */
.lvl-white {
  color: #fff;
  border-color: #666;
}
.lvl-gold {
  color: #ffd700;
  border-color: #ffd700;
  box-shadow: 0 0 5px #ffd700;
}
.lvl-purple {
  color: #d580ff;
  border-color: #d580ff;
  box-shadow: 0 0 5px #d580ff;
}
.lvl-red {
  color: #ff3333;
  border-color: #ff3333;
  box-shadow: 0 0 8px #ff0000;
}

.level-text {
  font-weight: bold;
  margin-left: 5px;
}

/* --- POSITIONING --- */
.necklace-slot {
  top: 15%;
  left: 15%;
}
.weapon-slot {
  top: 50%;
  left: 5%;
  transform: translateY(-50%);
}
.ring-slot {
  bottom: 15%;
  left: 15%;
}
.head-slot {
  top: 15%;
  right: 15%;
}
.body-slot {
  top: 50%;
  right: 5%;
  transform: translateY(-50%);
}
.boots-slot {
  bottom: 15%;
  right: 15%;
}

.weapon-slot:hover,
.body-slot:hover {
  transform: translateY(-50%) scale(1.1);
}

/* --- RIGHT PANEL --- */
.bag-info {
  text-align: right;
  padding: 5px 15px;
  font-size: 0.8rem;
  color: #a1887f;
}

.mini-grid {
  flex: 1;
  padding: 10px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: 60px;
  gap: 8px;
  overflow-y: auto;
  align-content: start;
}

.mini-slot {
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid #4e342e;
  border-radius: 4px;
  position: relative;
  cursor: pointer;
  transition: 0.2s;
}

.mini-slot.empty {
  opacity: 0.2;
  pointer-events: none;
}
.mini-slot:hover {
  border-color: var(--gold);
  background: rgba(255, 255, 255, 0.05);
}

.mini-slot img {
  width: 100%;
  height: 100%;
  padding: 4px;
  object-fit: contain;
}

.mini-level {
  position: absolute;
  top: 1px;
  right: 1px;
  font-size: 9px;
  font-weight: bold;
  background: rgba(0, 0, 0, 0.8);
  padding: 0 2px;
  border-radius: 2px;
}

.qty {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 0.7rem;
  padding: 0 4px;
  border-radius: 2px;
}

.equipped-dot {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 6px;
  height: 6px;
  background: #4caf50;
  border-radius: 50%;
  box-shadow: 0 0 5px #4caf50;
}

.rarity-C {
  border-bottom: 2px solid #9e9e9e;
}
.rarity-S {
  border-bottom: 2px solid var(--gold);
}

/* MODAL */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.dark-modal {
  width: 350px;
  background: var(--wood-dark);
  position: relative;
  box-shadow: 0 0 30px #000;
}

.modal-content {
  padding: 20px;
  text-align: center;
}

.item-preview-box {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid #5d4037;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.preview-img {
  width: 50px;
  height: 50px;
  border: 1px solid var(--gold);
  padding: 2px;
}

.modal-actions {
  display: flex;
  gap: 10px;
}
.btn-wood {
  flex: 1;
  padding: 10px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
}
.cancel {
  background: #4e342e;
  color: #bdbdbd;
}
.confirm {
  background: var(--red-seal);
  color: #fff;
}

@keyframes pulseAura {
  0% {
    transform: translate(-50%, -50%) scale(0.9);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(0.9);
    opacity: 0.5;
  }
}

@media (max-width: 900px) {
  .char-grid {
    grid-template-columns: 1fr;
    height: auto;
  }
  .hero-panel {
    height: 400px;
  }
}
</style>
