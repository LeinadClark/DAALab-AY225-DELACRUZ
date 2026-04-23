let allRows = [];
let topDebtorsChart = null;

// ── CHART INSTANCES ────────────────────────────────────────────────────
let regionChartInstance = null;
let compChartInstance   = null;
let trendChartInstance  = null;

// ── CHART DATA ─────────────────────────────────────────────────────────
const REGION_LABELS = ['E. Asia & Pacific', 'Latin Am. & Carib.', 'Sub-Saharan Africa', 'South Asia', 'Europe & C. Asia', 'Mid. East & N. Africa'];
const REGION_DATA   = [3400, 2050, 780, 720, 610, 290];
const REGION_COLORS = ['#3b82f6', '#f59e0b', '#14b8a6', '#f43f5e', '#a78bfa', '#22c55e'];

const COMP_LABELS = ['Public & PPG long-term', 'Private non-guaranteed', 'Short-term debt', 'IMF credit'];
const COMP_DATA   = [52, 28, 14, 6];
const COMP_COLORS = ['#3b82f6', '#f59e0b', '#14b8a6', '#a78bfa'];

const TREND_LABELS = ['2000', '2002', '2004', '2006', '2008', '2010', '2012', '2014', '2016', '2018', '2020', '2022'];
const TREND_DATA   = [2.1, 2.2, 2.5, 2.9, 3.7, 4.2, 5.0, 6.0, 6.8, 7.8, 8.7, 9.3];

// ── THEME HELPERS ──────────────────────────────────────────────────────
function CHART_TEXT() {
  return document.documentElement.classList.contains('light') ? '#4a3c28' : '#8b9ab5';
}
function CHART_GRID() {
  return document.documentElement.classList.contains('light') ? 'rgba(101,82,50,0.15)' : 'rgba(255,255,255,0.05)';
}

Chart.defaults.color = CHART_TEXT();
Chart.defaults.font.family = "'DM Sans', sans-serif";

// ── CHART BUILDERS ─────────────────────────────────────────────────────
function buildScalesXY(yFmt) {
  return {
    x: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 } } },
    y: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 }, callback: yFmt } }
  };
}

function buildRegionConfig(type) {
  const isRadarOrPolar = type === 'radar' || type === 'polarArea';
  const dataset = {
    label: 'External Debt (USD bn)',
    data: REGION_DATA,
    backgroundColor: isRadarOrPolar
      ? REGION_COLORS.map(c => c + 'bb')
      : REGION_COLORS,
    borderColor: isRadarOrPolar ? REGION_COLORS : undefined,
    borderWidth: isRadarOrPolar ? 1.5 : 0,
    borderRadius: type === 'bar' ? 5 : undefined,
    borderSkipped: false,
    fill: type === 'radar' ? true : undefined,
    tension: type === 'line' ? 0.4 : undefined,
    pointBackgroundColor: type === 'line' ? REGION_COLORS : undefined,
    pointRadius: type === 'line' ? 5 : undefined,
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    animation: { duration: 350, easing: 'easeInOutQuart' },
    plugins: {
      legend: {
        display: isRadarOrPolar || type === 'line',
        labels: { color: CHART_TEXT(), font: { size: 11 }, boxWidth: 10, padding: 10 }
      }
    }
  };

  if (type === 'bar' || type === 'line') {
    options.scales = buildScalesXY(v => '$' + v + 'B');
  } else if (type === 'radar') {
    options.scales = {
      r: {
        ticks: { color: CHART_TEXT(), font: { size: 10 }, backdropColor: 'transparent' },
        grid: { color: CHART_GRID() },
        pointLabels: { color: CHART_TEXT(), font: { size: 11 } }
      }
    };
  }

  return {
    type,
    data: { labels: REGION_LABELS, datasets: [dataset] },
    options
  };
}

function buildCompConfig(type) {
  const isSegmented = type === 'doughnut' || type === 'pie' || type === 'polarArea';
  const dataset = {
    label: 'Share (%)',
    data: COMP_DATA,
    backgroundColor: COMP_COLORS,
    borderColor: isSegmented ? 'transparent' : undefined,
    borderWidth: isSegmented ? 2 : 0,
    borderRadius: type === 'bar' ? 5 : undefined,
    borderSkipped: false,
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: type === 'doughnut' ? '65%' : undefined,
    animation: { duration: 350, easing: 'easeInOutQuart' },
    plugins: {
      legend: {
        display: isSegmented,
        position: 'bottom',
        labels: { color: CHART_TEXT(), font: { size: 11 }, boxWidth: 10, padding: 12 }
      }
    }
  };

  if (type === 'bar') {
    options.scales = buildScalesXY(v => v + '%');
  }

  return {
    type,
    data: { labels: COMP_LABELS, datasets: [dataset] },
    options
  };
}

function buildTrendConfig(type) {
  const isScatter = type === 'scatter';

  const scatterData = TREND_LABELS.map((l, i) => ({ x: +l, y: TREND_DATA[i] }));

  const dataset = isScatter
    ? {
        label: 'Debt (USD T)',
        data: scatterData,
        backgroundColor: '#3b82f6',
        pointRadius: 6,
        pointHoverRadius: 8,
      }
    : {
        label: 'External Debt (USD T)',
        data: TREND_DATA,
        borderColor: '#3b82f6',
        backgroundColor: type === 'line' ? 'rgba(59,130,246,0.08)' : '#3b82f6',
        fill: type === 'line',
        tension: type === 'line' ? 0.4 : undefined,
        borderWidth: type === 'line' ? 2 : 0,
        pointRadius: type === 'line' ? 3 : undefined,
        pointBackgroundColor: '#3b82f6',
        borderRadius: type === 'bar' ? 4 : undefined,
        borderSkipped: false,
      };

  const scatterScales = {
    x: {
      type: 'linear',
      grid: { color: CHART_GRID() },
      ticks: { color: CHART_TEXT(), font: { size: 11 }, callback: v => String(v) }
    },
    y: {
      grid: { color: CHART_GRID() },
      ticks: { color: CHART_TEXT(), font: { size: 11 }, callback: v => '$' + v + 'T' }
    }
  };

  return {
    type: isScatter ? 'scatter' : type,
    data: isScatter
      ? { datasets: [dataset] }
      : { labels: TREND_LABELS, datasets: [dataset] },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 350, easing: 'easeInOutQuart' },
      plugins: { legend: { display: false } },
      scales: isScatter ? scatterScales : buildScalesXY(v => '$' + v + 'T')
    }
  };
}

// ── SWITCHER HELPERS ───────────────────────────────────────────────────
function setActiveSwitcher(switcherId, clickedBtn) {
  document.querySelectorAll('#' + switcherId + ' .type-btn').forEach(b => b.classList.remove('active'));
  clickedBtn.classList.add('active');
}

// ── SWITCH FUNCTIONS (called from HTML onclick) ────────────────────────
function switchRegion(type, btn) {
  setActiveSwitcher('region-switcher', btn);
  if (regionChartInstance) regionChartInstance.destroy();
  regionChartInstance = new Chart(document.getElementById('regionChart'), buildRegionConfig(type));
}

function switchComp(type, btn) {
  setActiveSwitcher('comp-switcher', btn);
  if (compChartInstance) compChartInstance.destroy();
  compChartInstance = new Chart(document.getElementById('compChart'), buildCompConfig(type));
}

function switchTrend(type, btn) {
  setActiveSwitcher('trend-switcher', btn);
  if (trendChartInstance) trendChartInstance.destroy();
  trendChartInstance = new Chart(document.getElementById('trendChart'), buildTrendConfig(type));
}

// ── INITIAL CHART RENDER ───────────────────────────────────────────────
regionChartInstance = new Chart(document.getElementById('regionChart'), buildRegionConfig('bar'));
compChartInstance   = new Chart(document.getElementById('compChart'),   buildCompConfig('doughnut'));
trendChartInstance  = new Chart(document.getElementById('trendChart'),  buildTrendConfig('line'));

// ── LIVE DATA LOADING ──────────────────────────────────────────────────
async function loadData() {
  const btn    = document.getElementById('load-btn');
  const status = document.getElementById('api-status');
  const tbody  = document.getElementById('table-body');

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Loading...';
  status.textContent = 'Connecting to World Bank API...';
  tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;"><span class="spinner"></span> Fetching data...</td></tr>';

  try {
    const url = 'https://api.worldbank.org/v2/country/all/indicator/DT.DOD.DECT.CD?format=json&mrv=1&per_page=200&date=2020:2022';
    const res = await fetch(url);
    if (!res.ok) throw new Error('API error ' + res.status);
    const json = await res.json();
    const records = json[1];

    if (!records || records.length === 0) throw new Error('No data returned');

    allRows = records
      .filter(r => r.value !== null && r.countryiso3code && r.country)
      .map(r => ({
        country:   r.country.value,
        code:      r.countryiso3code,
        indicator: 'External debt stocks, total',
        year:      r.date,
        value:     r.value
      }));

    status.textContent = allRows.length + ' records loaded · ' + new Date().toLocaleTimeString();
    renderTable(allRows);
    renderTopDebtors(allRows);
    btn.innerHTML = 'Reload ↗';
    btn.disabled = false;

  } catch (err) {
    status.textContent = 'Error: ' + err.message;
    tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;color:var(--rose);">Failed to load — World Bank API may be temporarily unavailable. Try again.</td></tr>';
    btn.innerHTML = 'Retry ↗';
    btn.disabled = false;
  }
}

function fmt(val) {
  if (val === null || val === undefined) return '—';
  if (val >= 1e12) return '$' + (val / 1e12).toFixed(2) + 'T';
  if (val >= 1e9)  return '$' + (val / 1e9).toFixed(1)  + 'B';
  if (val >= 1e6)  return '$' + (val / 1e6).toFixed(0)  + 'M';
  return '$' + val.toLocaleString();
}

function renderTable(rows) {
  const tbody = document.getElementById('table-body');
  if (rows.length === 0) {
    tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;">No results match your filter</td></tr>';
    document.getElementById('row-count').textContent = '0 rows';
    return;
  }
  const display = rows.slice(0, 200);
  tbody.innerHTML = display.map(r => `
    <tr>
      <td class="country">${r.country}</td>
      <td class="year">${r.code}</td>
      <td class="indicator">${r.indicator}</td>
      <td class="year">${r.year}</td>
      <td class="value">${fmt(r.value)}</td>
    </tr>
  `).join('');
  document.getElementById('row-count').textContent = rows.length + ' records' + (rows.length > 200 ? ' (showing 200)' : '');
}

function filterTable() {
  if (allRows.length === 0) return;
  const q = document.getElementById('search-input').value.toLowerCase();
  const filtered = allRows.filter(r => {
    return !q || r.country.toLowerCase().includes(q) || r.indicator.toLowerCase().includes(q);
  });
  renderTable(filtered);
}

function renderTopDebtors(rows) {
  const sorted = [...rows].sort((a, b) => b.value - a.value).slice(0, 15);
  const canvas = document.getElementById('topDebtorsChart');
  if (topDebtorsChart) topDebtorsChart.destroy();
  topDebtorsChart = new Chart(canvas, {
    type: 'bar',
    data: {
      labels: sorted.map(r => r.country),
      datasets: [{
        label: 'External Debt (USD)',
        data: sorted.map(r => +(r.value / 1e9).toFixed(1)),
        backgroundColor: sorted.map((_, i) => {
          const light = document.documentElement.classList.contains('light');
          if (i === 0) return light ? '#b8860b' : '#f59e0b';
          if (i < 3)  return light ? '#8b4513' : '#3b82f6';
          return light ? '#c8b89a' : '#1e3a5f';
        }),
        borderRadius: 4,
        borderSkipped: false
      }]
    },
    options: {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), callback: v => '$' + v + 'B', font: { size: 11 } } },
        y: { grid: { color: 'transparent' }, ticks: { color: CHART_TEXT(), font: { size: 12 } } }
      }
    }
  });
}

// ── THEME TOGGLE ──────────────────────────────────────────────────────
function toggleTheme() {
  const isLight = document.documentElement.classList.toggle('light');
  localStorage.setItem('ids-theme', isLight ? 'light' : 'dark');
  updateChartTheme(isLight);
}

function updateChartTheme(isLight) {
  const TEXT = isLight ? '#4a3c28' : '#8b9ab5';
  const GRID = isLight ? 'rgba(101,82,50,0.15)' : 'rgba(255,255,255,0.05)';
  Chart.defaults.color = TEXT;

  Chart.instances && Object.values(Chart.instances).forEach(chart => {
    if (!chart) return;
    if (chart.options.scales) {
      ['x', 'y', 'r'].forEach(ax => {
        if (chart.options.scales[ax]) {
          chart.options.scales[ax].grid  = chart.options.scales[ax].grid  || {};
          chart.options.scales[ax].ticks = chart.options.scales[ax].ticks || {};
          chart.options.scales[ax].grid.color  = ax === 'y' && chart.options.indexAxis === 'y' ? 'transparent' : GRID;
          chart.options.scales[ax].ticks.color = TEXT;
          if (ax === 'r') {
            chart.options.scales[ax].pointLabels = chart.options.scales[ax].pointLabels || {};
            chart.options.scales[ax].pointLabels.color = TEXT;
          }
        }
      });
    }
    if (chart.options.plugins?.legend?.labels) {
      chart.options.plugins.legend.labels.color = TEXT;
    }
    chart.update('none');
  });
}

// ── RESTORE THEME ON LOAD ─────────────────────────────────────────────
(function () {
  const saved   = localStorage.getItem('ids-theme');
  const isLight = saved === 'light';
  if (isLight) document.documentElement.classList.add('light');
  setTimeout(() => updateChartTheme(isLight), 100);
})();