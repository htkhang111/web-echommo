<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Serif+TC:wght@500;700;900&display=swap");

:root {
  --bg-deep: #020617;
  --jade: #10b981;
  --gold: #fbbf24;
  --text-main: #f3f4f6;
}

.page-wrapper {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: radial-gradient(circle at 20% 20%, #111827, #020617 90%);
  color: var(--text-main);
  font-family: "Noto Serif TC", serif;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
}

.page-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--jade) transparent;
}
.page-content::-webkit-scrollbar {
  width: 6px;
}
.page-content::-webkit-scrollbar-thumb {
  background: linear-gradient(var(--jade), transparent);
  border-radius: 6px;
  opacity: 0.4;
}
.page-content::-webkit-scrollbar-track {
  background: transparent;
}

.page-content.no-padding-scroll {
  padding: 0 !important;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.bg-texture {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: url("https://www.transparenttextures.com/patterns/asfalt-dark.png");
  opacity: 0.07;
  mix-blend-mode: overlay;
}
.fog-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(
    circle at top,
    rgba(16, 185, 129, 0.12),
    transparent 70%
  );
  filter: blur(18px);
  opacity: 0.45;
}

.ornamental-frame {
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow:
    0 0 35px rgba(16, 185, 129, 0.12),
    inset 0 0 45px rgba(0, 0, 0, 0.45);
  z-index: 50;
}

.corner {
  width: 30px;
  height: 30px;
  border: 2px solid var(--jade);
  filter: drop-shadow(0 0 12px rgba(16, 185, 129, 0.55));
  opacity: 0.9;
  transition: 0.4s ease;
}
.corner:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 18px rgba(16, 185, 129, 0.8));
}

.scroll-top-seal {
  position: fixed;
  bottom: 45px;
  right: 40px;
  width: 60px;
  height: 60px;
  cursor: pointer;
  z-index: 1200;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: float 3s ease-in-out infinite both;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}

.seal-inner {
  background: linear-gradient(135deg, #34d399, #047857);
  border: 2px solid #cdfae5;
  border-radius: 14px;
  box-shadow:
    0 5px 20px rgba(4, 120, 87, 0.45),
    inset 0 0 14px rgba(255, 255, 255, 0.08);
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-weight: 900;
  color: #064e3b;
}

.seal-glow {
  position: absolute;
  inset: -9px;
  border-radius: 16px;
  border: 1px solid var(--jade);
  opacity: 0;
  transition: 0.45s ease;
  animation: jadePulse 2.6s infinite;
}
@keyframes jadePulse {
  0% {
    opacity: 0.35;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.35);
  }
}
.scroll-top-seal:hover .seal-glow {
  opacity: 1;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .ornamental-frame {
    inset: 6px;
  }
  .page-content:not(.no-padding-scroll) {
    padding: 10px;
  } /* Giảm padding */
  .scroll-top-seal {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }
}
</style>
