/**
 * Apex Matrix Calculator - Frontend Controller
 * Communicates with Spring Boot Backend REST API
 */

(function () {
  'use strict';

  // State
  let currentInput = '0';
  let lastEvaluatedExpr = '';
  let shouldResetInput = false;
  let isSoundEnabled = true;
  let audioCtx = null;

  // DOM Elements
  const displayMain = document.getElementById('display-main');
  const displayHistory = document.getElementById('display-history');
  const displayPreview = document.getElementById('display-preview');
  const calcDisplay = document.getElementById('calc-display');
  const serverStatusDot = document.getElementById('server-status-dot');
  const serverStatusText = document.getElementById('server-status-text');
  const serverLatency = document.getElementById('server-latency');
  const historyList = document.getElementById('history-list');
  const emptyHistoryPlaceholder = document.getElementById('empty-history-placeholder');
  const btnClearHistory = document.getElementById('btn-clear-history');
  const btnSoundToggle = document.getElementById('btn-sound-toggle');
  const btnHistoryToggle = document.getElementById('btn-history-toggle');
  const historyContainer = document.getElementById('history-container');

  // Sound Synthesizer (Web Audio API)
  function initAudio() {
    if (!audioCtx) {
      const AudioContextClass = window.AudioContext || window.webkitAudioContext;
      if (AudioContextClass) {
        audioCtx = new AudioContextClass();
      }
    }
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
  }

  function playClickSound(freq = 600, duration = 0.035, type = 'sine') {
    if (!isSoundEnabled) return;
    try {
      initAudio();
      if (!audioCtx) return;
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();

      osc.type = type;
      osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(150, audioCtx.currentTime + duration);

      gain.gain.setValueAtTime(0.08, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);

      osc.connect(gain);
      gain.connect(audioCtx.destination);

      osc.start();
      osc.stop(audioCtx.currentTime + duration);
    } catch (e) {
      // Audio autoplay policy or unavailable, silently continue
    }
  }

  function playSuccessChord() {
    if (!isSoundEnabled) return;
    playClickSound(880, 0.08, 'triangle');
  }

  function playErrorThud() {
    if (!isSoundEnabled) return;
    playClickSound(180, 0.15, 'sawtooth');
  }

  // Display Updates
  function updateDisplay() {
    displayMain.textContent = currentInput || '0';
    updateLivePreview();
  }

  function triggerError(msg) {
    playErrorThud();
    displayPreview.textContent = msg || 'Math Error';
    displayPreview.style.color = 'var(--danger)';
    calcDisplay.classList.remove('error-shake');
    void calcDisplay.offsetWidth; // Trigger reflow
    calcDisplay.classList.add('error-shake');
    setTimeout(() => {
      displayPreview.style.color = '#38bdf8';
    }, 2000);
  }

  // Client-side quick preview
  function updateLivePreview() {
    if (!currentInput || currentInput === '0' || shouldResetInput) {
      displayPreview.textContent = '';
      return;
    }

    // Try a lightweight sanitized eval for instant preview
    try {
      let sanitized = currentInput
        .replace(/×/g, '*')
        .replace(/÷/g, '/')
        .replace(/\^/g, '**');

      // Only attempt preview if it has operators and doesn't end with an operator
      if (/[\+\-\*\/\%]/i.test(sanitized) && !/[\+\-\*\/\%\(]$/.test(sanitized.trim())) {
        // Prevent arbitrary code execution by checking allowed tokens only
        if (/^[0-9\+\-\*\/\%\.\(\)\s\*\*]+$/.test(sanitized)) {
          // eslint-disable-next-line no-eval
          const val = Function('"use strict";return (' + sanitized + ')')();
          if (typeof val === 'number' && !isNaN(val) && isFinite(val)) {
            const formatted = val % 1 === 0 ? val.toString() : parseFloat(val.toFixed(6)).toString();
            displayPreview.textContent = '= ' + formatted;
            return;
          }
        }
      }
      displayPreview.textContent = '';
    } catch (e) {
      displayPreview.textContent = '';
    }
  }

  // Key Actions
  function insertValue(val) {
    playClickSound(520, 0.03);

    if (shouldResetInput) {
      // If user starts with an operator right after equals, continue with previous result
      if ([' + ', ' - ', ' × ', ' ÷ ', '^', '%'].includes(val)) {
        shouldResetInput = false;
      } else {
        currentInput = '';
        shouldResetInput = false;
      }
    }

    if (currentInput === '0' && val !== '.' && ![' + ', ' - ', ' × ', ' ÷ ', '^', '%', ')'].includes(val)) {
      currentInput = val;
    } else {
      currentInput += val;
    }
    updateDisplay();
  }

  function allClear() {
    playClickSound(350, 0.05);
    currentInput = '0';
    lastEvaluatedExpr = '';
    displayHistory.innerHTML = '&nbsp;';
    displayPreview.textContent = '';
    shouldResetInput = false;
    updateDisplay();
  }

  function backspace() {
    playClickSound(400, 0.03);
    if (shouldResetInput) {
      allClear();
      return;
    }
    if (currentInput.length <= 1 || currentInput === '0') {
      currentInput = '0';
    } else {
      // If ends with spaced operator like ' + ', trim all 3 chars
      if (/\s[\+\-×÷]\s$/.test(currentInput)) {
        currentInput = currentInput.slice(0, -3);
      } else {
        currentInput = currentInput.slice(0, -1);
      }
    }
    updateDisplay();
  }

  function toggleNegate() {
    playClickSound(480, 0.03);
    if (currentInput === '0') return;
    if (currentInput.startsWith('-(') && currentInput.endsWith(')')) {
      currentInput = currentInput.slice(2, -1);
    } else if (currentInput.startsWith('-')) {
      currentInput = currentInput.slice(1);
    } else {
      currentInput = '-(' + currentInput + ')';
    }
    updateDisplay();
  }

  function applySquare() {
    playClickSound(550, 0.03);
    if (currentInput === '0') return;
    currentInput = '(' + currentInput + ')^2';
    updateDisplay();
  }

  function applyReciprocal() {
    playClickSound(550, 0.03);
    if (currentInput === '0') {
      triggerError('Cannot divide by zero');
      return;
    }
    currentInput = '1/(' + currentInput + ')';
    updateDisplay();
  }

  // Backend Spring Boot API Call
  async function calculateWithServer() {
    if (!currentInput || currentInput === '0') return;

    playClickSound(750, 0.04);
    const expressionToSend = currentInput;
    const startTime = performance.now();

    try {
      const response = await fetch('/api/calculator/calculate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ expression: expressionToSend })
      });

      const latencyMs = Math.round(performance.now() - startTime);
      serverLatency.textContent = latencyMs + ' ms';
      setBackendStatus(true);

      const data = await response.json();

      if (data.success) {
        playSuccessChord();
        displayHistory.textContent = expressionToSend + ' =';
        lastEvaluatedExpr = expressionToSend;
        currentInput = data.formattedResult;
        displayPreview.textContent = '';
        shouldResetInput = true;
        updateDisplay();
        fetchHistory();
      } else {
        triggerError(data.errorMessage || 'Invalid expression');
      }
    } catch (err) {
      console.warn('Backend unavailable, evaluating locally fallback:', err);
      setBackendStatus(false);
      evaluateFallback(expressionToSend);
    }
  }

  // Fallback if backend is momentarily unreachable
  function evaluateFallback(expr) {
    try {
      let sanitized = expr
        .replace(/×/g, '*')
        .replace(/÷/g, '/')
        .replace(/\^/g, '**');

      // eslint-disable-next-line no-eval
      const result = Function('"use strict";return (' + sanitized + ')')();
      if (typeof result === 'number' && !isNaN(result)) {
        playSuccessChord();
        displayHistory.textContent = expr + ' =';
        currentInput = (result % 1 === 0 ? result.toString() : parseFloat(result.toFixed(8)).toString());
        shouldResetInput = true;
        updateDisplay();
      } else {
        triggerError('Calculation error');
      }
    } catch (e) {
      triggerError('Syntax error');
    }
  }

  // Server History Integration
  async function fetchHistory() {
    try {
      const res = await fetch('/api/calculator/history');
      if (!res.ok) return;
      const historyItems = await res.json();
      renderHistory(historyItems);
    } catch (e) {
      // Server offline or polling error
    }
  }

  function renderHistory(items) {
    if (!items || items.length === 0) {
      historyList.innerHTML = '';
      historyList.appendChild(emptyHistoryPlaceholder);
      return;
    }

    historyList.innerHTML = '';
    items.forEach((item) => {
      const div = document.createElement('div');
      div.className = 'history-item';
      div.setAttribute('tabindex', '0');
      div.setAttribute('role', 'button');
      div.setAttribute('title', 'Click to recall this calculation');

      div.innerHTML = `
        <div class="item-top">
          <span>${escapeHtml(item.expression)}</span>
          <span class="item-time">${escapeHtml(item.timestamp || '')}</span>
        </div>
        <div class="item-result">= ${escapeHtml(item.formattedResult)}</div>
      `;

      div.addEventListener('click', () => {
        playClickSound(620, 0.03);
        currentInput = item.formattedResult;
        displayHistory.textContent = item.expression + ' =';
        shouldResetInput = true;
        updateDisplay();
      });

      historyList.appendChild(div);
    });
  }

  async function clearHistory() {
    try {
      playClickSound(300, 0.04);
      await fetch('/api/calculator/history', { method: 'DELETE' });
      fetchHistory();
    } catch (e) {
      console.error('Failed to clear history:', e);
    }
  }

  function escapeHtml(text) {
    if (!text) return '';
    return text.toString()
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  // Health Check
  async function checkServerHealth() {
    const t0 = performance.now();
    try {
      const res = await fetch('/api/calculator/health');
      const latency = Math.round(performance.now() - t0);
      if (res.ok) {
        setBackendStatus(true);
        serverLatency.textContent = latency + ' ms';
      } else {
        setBackendStatus(false);
      }
    } catch (e) {
      setBackendStatus(false);
    }
  }

  function setBackendStatus(isOnline) {
    if (isOnline) {
      serverStatusDot.className = 'status-dot';
      serverStatusText.textContent = 'Spring Boot: Online';
    } else {
      serverStatusDot.className = 'status-dot offline';
      serverStatusText.textContent = 'Spring Boot: Offline';
      serverLatency.textContent = '--';
    }
  }

  // Button Click Handlers
  document.querySelectorAll('.key-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const action = btn.getAttribute('data-action');
      const value = btn.getAttribute('data-value');

      switch (action) {
        case 'insert':
          insertValue(value);
          break;
        case 'all-clear':
          allClear();
          break;
        case 'backspace':
          backspace();
          break;
        case 'fn-negate':
          toggleNegate();
          break;
        case 'fn-square':
          applySquare();
          break;
        case 'fn-reciprocal':
          applyReciprocal();
          break;
        case 'equals':
          calculateWithServer();
          break;
        default:
          break;
      }
    });
  });

  // Sound toggle button
  btnSoundToggle.addEventListener('click', () => {
    isSoundEnabled = !isSoundEnabled;
    btnSoundToggle.classList.toggle('active', isSoundEnabled);
    btnSoundToggle.textContent = isSoundEnabled ? '🔊' : '🔇';
    if (isSoundEnabled) {
      initAudio();
      playClickSound(600, 0.05);
    }
  });

  // History panel toggle for compact views
  btnHistoryToggle.addEventListener('click', () => {
    const isHidden = historyContainer.style.display === 'none';
    historyContainer.style.display = isHidden ? 'flex' : 'none';
    btnHistoryToggle.classList.toggle('active', isHidden);
  });

  btnClearHistory.addEventListener('click', clearHistory);

  // Keyboard Navigation & Shortcuts
  window.addEventListener('keydown', (e) => {
    // Avoid interfering if focus is inside an input (if any)
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

    const key = e.key;

    // Visual button ripple helper
    const highlightKey = (selector) => {
      const el = document.querySelector(selector);
      if (el) {
        el.classList.add('active-key');
        setTimeout(() => el.classList.remove('active-key'), 120);
      }
    };

    if (key >= '0' && key <= '9') {
      highlightKey(`.key-btn[data-value="${key}"]`);
      insertValue(key);
      e.preventDefault();
    } else if (key === '.') {
      highlightKey('.key-btn[data-value="."]');
      insertValue('.');
      e.preventDefault();
    } else if (key === '+') {
      highlightKey('.key-btn[data-value=" + "]');
      insertValue(' + ');
      e.preventDefault();
    } else if (key === '-') {
      highlightKey('.key-btn[data-value=" - "]');
      insertValue(' - ');
      e.preventDefault();
    } else if (key === '*') {
      highlightKey('.key-btn[data-value=" × "]');
      insertValue(' × ');
      e.preventDefault();
    } else if (key === '/') {
      highlightKey('.key-btn[data-value=" ÷ "]');
      insertValue(' ÷ ');
      e.preventDefault();
    } else if (key === '^') {
      highlightKey('.key-btn[data-value="^"]');
      insertValue('^');
      e.preventDefault();
    } else if (key === '%') {
      highlightKey('.key-btn[data-value="%"]');
      insertValue('%');
      e.preventDefault();
    } else if (key === '(' || key === ')') {
      highlightKey(`.key-btn[data-value="${key}"]`);
      insertValue(key);
      e.preventDefault();
    } else if (key === 'Enter' || key === '=') {
      highlightKey('#key-equals');
      calculateWithServer();
      e.preventDefault();
    } else if (key === 'Backspace') {
      highlightKey('#key-backspace');
      backspace();
      e.preventDefault();
    } else if (key === 'Escape') {
      highlightKey('#key-ac');
      allClear();
      e.preventDefault();
    }
  });

  // Initialization
  checkServerHealth();
  fetchHistory();
  setInterval(checkServerHealth, 15000);
})();
