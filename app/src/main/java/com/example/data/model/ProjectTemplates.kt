package com.example.data.model

object ProjectTemplates {

    val CYBERPUNK_TIMER = Project(
        title = "Neon Pomodoro Timer",
        description = "Futuristic countdown timer with circular progress, sound toggle, and focus stats",
        htmlContent = """
<div class="flex flex-col items-center justify-center min-h-screen p-6 select-none">
  <div class="w-full max-w-sm bg-slate-800/80 backdrop-blur border border-purple-500/30 rounded-3xl p-6 shadow-2xl flex flex-col items-center text-center">
    <div class="inline-flex items-center gap-2 px-3 py-1 bg-purple-950/60 border border-purple-500/40 rounded-full text-xs font-semibold text-purple-300 tracking-wider uppercase mb-6">
      <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span> Focus Engine
    </div>

    <!-- Radial Progress Display -->
    <div class="relative w-56 h-56 flex items-center justify-center mb-6">
      <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
        <circle cx="50" cy="50" r="42" stroke="currentColor" stroke-width="6" class="text-slate-700/50" fill="transparent" />
        <circle id="progressCircle" cx="50" cy="50" r="42" stroke="currentColor" stroke-width="6" class="text-cyan-400 transition-all duration-300"
          stroke-dasharray="264" stroke-dashoffset="0" stroke-linecap="round" fill="transparent" />
      </svg>
      <div class="absolute flex flex-col items-center">
        <span id="timeDisplay" class="text-5xl font-mono font-bold tracking-tight text-white drop-shadow-[0_0_15px_rgba(56,189,248,0.5)]">25:00</span>
        <span id="statusLabel" class="text-xs uppercase tracking-widest text-slate-400 mt-1">Deep Work</span>
      </div>
    </div>

    <!-- Controls -->
    <div class="flex items-center gap-3 w-full justify-center">
      <button id="startBtn" onclick="toggleTimer()" class="flex-1 py-3 px-4 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold rounded-2xl shadow-lg shadow-cyan-500/20 active:scale-95 transition-all text-sm uppercase tracking-wide">
        Start
      </button>
      <button onclick="resetTimer()" class="py-3 px-4 bg-slate-700/60 hover:bg-slate-700 text-slate-300 font-semibold rounded-2xl border border-slate-600/50 active:scale-95 transition-all text-sm">
        Reset
      </button>
    </div>

    <!-- Session Counter -->
    <div class="mt-6 pt-4 border-t border-slate-700/60 w-full flex justify-between text-xs text-slate-400">
      <span>Completed Sessions:</span>
      <span id="sessionCount" class="font-bold text-cyan-400">0</span>
    </div>
  </div>
</div>
        """.trimIndent(),
        cssContent = """
@keyframes pulseGlow {
  0%, 100% { opacity: 0.8; }
  50% { opacity: 0.4; }
}
body {
  background: radial-gradient(circle at 50% 20%, #1e1b4b, #030712);
  min-height: 100vh;
  margin: 0;
  font-family: ui-sans-serif, system-ui, sans-serif;
}
        """.trimIndent(),
        jsContent = """
let totalSeconds = 25 * 60;
let remainingSeconds = totalSeconds;
let interval = null;
let isRunning = false;
let sessions = 0;

const timeDisplay = document.getElementById('timeDisplay');
const startBtn = document.getElementById('startBtn');
const progressCircle = document.getElementById('progressCircle');
const sessionCount = document.getElementById('sessionCount');
const circumference = 2 * Math.PI * 42; // ~263.89

function updateDisplay() {
  const m = Math.floor(remainingSeconds / 60).toString().padStart(2, '0');
  const s = (remainingSeconds % 60).toString().padStart(2, '0');
  timeDisplay.innerText = m + ":" + s;

  const fraction = remainingSeconds / totalSeconds;
  const offset = circumference * (1 - fraction);
  progressCircle.style.strokeDashoffset = offset;
}

function toggleTimer() {
  if (isRunning) {
    clearInterval(interval);
    isRunning = false;
    startBtn.innerText = "Resume";
    startBtn.classList.replace("from-amber-500", "from-cyan-500");
    console.log("Timer paused at: " + timeDisplay.innerText);
  } else {
    isRunning = true;
    startBtn.innerText = "Pause";
    startBtn.classList.replace("from-cyan-500", "from-amber-500");
    console.log("Timer started. Focus session active!");
    interval = setInterval(() => {
      if (remainingSeconds > 0) {
        remainingSeconds--;
        updateDisplay();
      } else {
        clearInterval(interval);
        isRunning = false;
        sessions++;
        sessionCount.innerText = sessions;
        console.log("Session completed! Well done. Total: " + sessions);
        alert("Pomodoro complete! Take a well deserved 5-minute break.");
        resetTimer();
      }
    }, 1000);
  }
}

function resetTimer() {
  clearInterval(interval);
  isRunning = false;
  remainingSeconds = totalSeconds;
  startBtn.innerText = "Start";
  updateDisplay();
  console.log("Timer reset to 25:00");
}

updateDisplay();
console.log("Neon Pomodoro Timer loaded and ready.");
        """.trimIndent()
    )

    val SPACE_DODGER = Project(
        title = "Retro Space Dodger",
        description = "2D Canvas arcade game with touch controls, particle explosions, and high scores",
        htmlContent = """
<div class="flex flex-col items-center justify-center min-h-screen p-4 select-none">
  <div class="w-full max-w-sm flex flex-col items-center">
    <div class="flex justify-between w-full mb-3 px-2 text-sm font-mono text-cyan-300">
      <div>SCORE: <span id="scoreEl" class="font-bold text-white">0</span></div>
      <div>BEST: <span id="highEl" class="font-bold text-emerald-400">0</span></div>
    </div>
    
    <div class="relative w-full aspect-[3/4] max-h-[460px] bg-slate-950 rounded-2xl overflow-hidden border-2 border-cyan-500/40 shadow-2xl">
      <canvas id="gameCanvas" class="w-full h-full block"></canvas>
      <div id="startOverlay" class="absolute inset-0 bg-slate-950/80 backdrop-blur-sm flex flex-col items-center justify-center p-6 text-center">
        <h1 class="text-3xl font-black text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-fuchsia-500 mb-2">SPACE DODGER</h1>
        <p class="text-xs text-slate-400 mb-6">Avoid asteroids. Drag finger left & right to navigate!</p>
        <button onclick="startGame()" class="px-6 py-3 bg-gradient-to-r from-cyan-500 to-fuchsia-500 text-slate-950 font-bold rounded-full shadow-lg shadow-cyan-500/30 active:scale-95 transition-all text-sm uppercase">
          Launch Ship
        </button>
      </div>
    </div>

    <!-- On-screen touch steers -->
    <div class="grid grid-cols-2 gap-3 w-full mt-4">
      <button onpointerdown="moveLeft=true" onpointerup="moveLeft=false" class="py-4 bg-slate-800/80 active:bg-slate-700 text-cyan-400 font-bold rounded-xl border border-slate-700 text-sm">
        ◀ STEER LEFT
      </button>
      <button onpointerdown="moveRight=true" onpointerup="moveRight=false" class="py-4 bg-slate-800/80 active:bg-slate-700 text-cyan-400 font-bold rounded-xl border border-slate-700 text-sm">
        STEER RIGHT ▶
      </button>
    </div>
  </div>
</div>
        """.trimIndent(),
        cssContent = """
body {
  background: #020617;
  color: #f8fafc;
  margin: 0;
  touch-action: none;
}
        """.trimIndent(),
        jsContent = """
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreEl = document.getElementById('scoreEl');
const highEl = document.getElementById('highEl');
const overlay = document.getElementById('startOverlay');

let width, height;
function resize() {
  width = canvas.width = canvas.parentElement.clientWidth;
  height = canvas.height = canvas.parentElement.clientHeight;
}
resize();
window.addEventListener('resize', resize);

let player = { x: 100, y: 350, w: 28, h: 28, speed: 6 };
let asteroids = [];
let particles = [];
let score = 0;
let highScore = parseInt(localStorage.getItem('space_high') || '0');
highEl.innerText = highScore;

let isPlaying = false;
let moveLeft = false;
let moveRight = false;

// Touch/drag tracking
canvas.addEventListener('touchmove', (e) => {
  if (!isPlaying) return;
  const rect = canvas.getBoundingClientRect();
  const touchX = e.touches[0].clientX - rect.left;
  player.x = Math.max(player.w/2, Math.min(width - player.w/2, touchX));
});

function startGame() {
  resize();
  player.x = width / 2;
  player.y = height - 50;
  asteroids = [];
  particles = [];
  score = 0;
  scoreEl.innerText = '0';
  isPlaying = true;
  overlay.classList.add('hidden');
  console.log("Space Dodger launched!");
  requestAnimationFrame(gameLoop);
}

let lastSpawn = 0;
function spawnAsteroid() {
  const size = 16 + Math.random() * 24;
  asteroids.push({
    x: Math.random() * (width - size),
    y: -size,
    size: size,
    speed: 3 + Math.random() * 3 + (score * 0.05)
  });
}

function gameLoop(timestamp) {
  if (!isPlaying) return;

  ctx.fillStyle = '#020617';
  ctx.fillRect(0, 0, width, height);

  // Move player with buttons
  if (moveLeft) player.x = Math.max(player.w / 2, player.x - player.speed);
  if (moveRight) player.x = Math.min(width - player.w / 2, player.x + player.speed);

  // Draw Player Ship
  ctx.fillStyle = '#38bdf8';
  ctx.shadowColor = '#38bdf8';
  ctx.shadowBlur = 12;
  ctx.beginPath();
  ctx.moveTo(player.x, player.y - player.h / 2);
  ctx.lineTo(player.x - player.w / 2, player.y + player.h / 2);
  ctx.lineTo(player.x + player.w / 2, player.y + player.h / 2);
  ctx.closePath();
  ctx.fill();
  ctx.shadowBlur = 0;

  // Spawn asteroids
  if (timestamp - lastSpawn > Math.max(350, 900 - score * 15)) {
    spawnAsteroid();
    lastSpawn = timestamp;
  }

  // Update asteroids
  for (let i = asteroids.length - 1; i >= 0; i--) {
    const a = asteroids[i];
    a.y += a.speed;

    ctx.fillStyle = '#e879f9';
    ctx.beginPath();
    ctx.arc(a.x + a.size/2, a.y + a.size/2, a.size/2, 0, Math.PI * 2);
    ctx.fill();

    // Check collision
    const dist = Math.hypot(player.x - (a.x + a.size/2), player.y - (a.y + a.size/2));
    if (dist < a.size/2 + player.w/2 - 2) {
      gameOver();
      return;
    }

    if (a.y > height + 50) {
      asteroids.splice(i, 1);
      score += 10;
      scoreEl.innerText = score;
    }
  }

  requestAnimationFrame(gameLoop);
}

function gameOver() {
  isPlaying = false;
  if (score > highScore) {
    highScore = score;
    localStorage.setItem('space_high', highScore);
    highEl.innerText = highScore;
    console.log("New high score achieved: " + highScore);
  }
  overlay.classList.remove('hidden');
  console.warn("Ship destroyed! Final score: " + score);
}

console.log("Space Dodger engine initialized.");
        """.trimIndent()
    )

    val GLASS_CALCULATOR = Project(
        title = "Glassmorphic Calculator",
        description = "Modern translucent calculator with history feed, keyboard and touch support",
        htmlContent = """
<div class="flex flex-col items-center justify-center min-h-screen p-4">
  <div class="w-full max-w-xs bg-slate-800/60 backdrop-blur-xl border border-white/10 rounded-3xl p-5 shadow-2xl flex flex-col">
    <!-- Display -->
    <div class="bg-black/40 rounded-2xl p-4 mb-4 text-right border border-white/5 flex flex-col justify-end min-h-[90px]">
      <div id="subDisplay" class="text-xs font-mono text-slate-400 h-5 overflow-hidden"></div>
      <div id="mainDisplay" class="text-4xl font-mono font-bold text-white tracking-tight overflow-x-auto">0</div>
    </div>

    <!-- Keypad -->
    <div class="grid grid-cols-4 gap-2.5">
      <button onclick="clearCalc()" class="btn-calc bg-rose-500/20 text-rose-300">C</button>
      <button onclick="toggleSign()" class="btn-calc bg-slate-700/40 text-slate-300">±</button>
      <button onclick="inputOp('%')" class="btn-calc bg-slate-700/40 text-slate-300">%</button>
      <button onclick="inputOp('/')" class="btn-calc bg-cyan-500/30 text-cyan-300 font-bold">÷</button>

      <button onclick="inputNum('7')" class="btn-calc">7</button>
      <button onclick="inputNum('8')" class="btn-calc">8</button>
      <button onclick="inputNum('9')" class="btn-calc">9</button>
      <button onclick="inputOp('*')" class="btn-calc bg-cyan-500/30 text-cyan-300 font-bold">×</button>

      <button onclick="inputNum('4')" class="btn-calc">4</button>
      <button onclick="inputNum('5')" class="btn-calc">5</button>
      <button onclick="inputNum('6')" class="btn-calc">6</button>
      <button onclick="inputOp('-')" class="btn-calc bg-cyan-500/30 text-cyan-300 font-bold">−</button>

      <button onclick="inputNum('1')" class="btn-calc">1</button>
      <button onclick="inputNum('2')" class="btn-calc">2</button>
      <button onclick="inputNum('3')" class="btn-calc">3</button>
      <button onclick="inputOp('+')" class="btn-calc bg-cyan-500/30 text-cyan-300 font-bold">+</button>

      <button onclick="inputNum('0')" class="btn-calc col-span-2">0</button>
      <button onclick="inputDot()" class="btn-calc">.</button>
      <button onclick="calculate()" class="btn-calc bg-gradient-to-r from-cyan-500 to-blue-600 text-slate-950 font-bold">=</button>
    </div>
  </div>
</div>
        """.trimIndent(),
        cssContent = """
.btn-calc {
  height: 54px;
  border-radius: 16px;
  font-size: 1.15rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(30, 41, 59, 0.5);
  color: #f8fafc;
  transition: all 0.1s ease;
  user-select: none;
}
.btn-calc:active {
  transform: scale(0.94);
  background: rgba(51, 65, 85, 0.8);
}
body {
  background: radial-gradient(circle at 80% 20%, #312e81, #0f172a);
}
        """.trimIndent(),
        jsContent = """
let currentInput = "0";
let previousInput = "";
let currentOp = null;

const mainDisplay = document.getElementById('mainDisplay');
const subDisplay = document.getElementById('subDisplay');

function updateUI() {
  mainDisplay.innerText = currentInput;
  subDisplay.innerText = previousInput + (currentOp ? " " + currentOp : "");
}

function inputNum(n) {
  if (currentInput === "0") {
    currentInput = n;
  } else {
    currentInput += n;
  }
  updateUI();
}

function inputDot() {
  if (!currentInput.includes('.')) {
    currentInput += '.';
    updateUI();
  }
}

function clearCalc() {
  currentInput = "0";
  previousInput = "";
  currentOp = null;
  updateUI();
  console.log("Calculator cleared");
}

function toggleSign() {
  currentInput = (parseFloat(currentInput) * -1).toString();
  updateUI();
}

function inputOp(op) {
  if (currentOp && previousInput) {
    calculate();
  }
  currentOp = op;
  previousInput = currentInput;
  currentInput = "0";
  updateUI();
}

function calculate() {
  if (!currentOp || !previousInput) return;
  const a = parseFloat(previousInput);
  const b = parseFloat(currentInput);
  let res = 0;
  switch (currentOp) {
    case '+': res = a + b; break;
    case '-': res = a - b; break;
    case '*': res = a * b; break;
    case '/': res = b === 0 ? "Error" : a / b; break;
    case '%': res = (a / 100) * b; break;
  }
  console.log(a + " " + currentOp + " " + b + " = " + res);
  currentInput = res.toString();
  previousInput = "";
  currentOp = null;
  updateUI();
}
console.log("Glassmorphic Calculator loaded.");
        """.trimIndent()
    )

    val ALL_TEMPLATES = listOf(CYBERPUNK_TIMER, SPACE_DODGER, GLASS_CALCULATOR)
}
