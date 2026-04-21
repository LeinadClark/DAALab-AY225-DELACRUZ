let allRows = [];
let topDebtorsChart = null;

// ── CHARTS (static/illustrative) ──────────────────────────────────────
function CHART_TEXT() {
  return document.documentElement.classList.contains('light') ? '#4a3c28' : '#8b9ab5';
}
function CHART_GRID() {
  return document.documentElement.classList.contains('light') ? 'rgba(101,82,50,0.15)' : 'rgba(255,255,255,0.05)';
}

Chart.defaults.color = CHART_TEXT();
Chart.defaults.font.family = "'DM Sans', sans-serif";

new Chart(document.getElementById('regionChart'), {
  type: 'bar',
  data: {
    labels: ['East Asia\n& Pacific', 'Latin Am.\n& Carib.', 'Sub-Saharan\nAfrica', 'South\nAsia', 'Europe\n& C.Asia', 'Mid.East\n& N.Africa'],
    datasets: [{
      label: 'External Debt (USD bn)',
      data: [3400, 2050, 780, 720, 610, 290],
      backgroundColor: ['#3b82f6','#f59e0b','#14b8a6','#f43f5e','#a78bfa','#22c55e'],
      borderRadius: 5,
      borderSkipped: false
    }]
  },
  options: {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 } } },
      y: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), callback: v => '$' + v + 'B', font: { size: 11 } } }
    }
  }
});

new Chart(document.getElementById('compChart'), {
  type: 'doughnut',
  data: {
    labels: ['Public & PPG long-term', 'Private non-guaranteed', 'Short-term debt', 'IMF credit'],
    datasets: [{
      data: [52, 28, 14, 6],
      backgroundColor: ['#3b82f6','#f59e0b','#14b8a6','#a78bfa'],
      borderColor: '#161d2e',
      borderWidth: 3,
      hoverBorderColor: '#1e2a40'
    }]
  },
  options: {
    responsive: true, maintainAspectRatio: false,
    cutout: '65%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: { color: CHART_TEXT(), font: { size: 11 }, boxWidth: 10, padding: 12 }
      }
    }
  }
});

new Chart(document.getElementById('trendChart'), {
  type: 'line',
  data: {
    labels: ['2000','2002','2004','2006','2008','2010','2012','2014','2016','2018','2020','2022'],
    datasets: [{
      label: 'External Debt (USD T)',
      data: [2.1, 2.2, 2.5, 2.9, 3.7, 4.2, 5.0, 6.0, 6.8, 7.8, 8.7, 9.3],
      borderColor: '#3b82f6',
      backgroundColor: 'rgba(59,130,246,0.08)',
      fill: true,
      tension: 0.4,
      borderWidth: 2,
      pointRadius: 3,
      pointBackgroundColor: '#3b82f6'
    }]
  },
  options: {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 } } },
      y: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), callback: v => '$' + v + 'T', font: { size: 11 } } }
    }
  }
});

// ── LIVE DATA LOADING ──────────────────────────────────────────────────
async function loadData() {
  const btn = document.getElementById('load-btn');
  const status = document.getElementById('api-status');
  const tbody = document.getElementById('table-body');

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Loading...';
  status.textContent = 'Connecting to World Bank API...';
  tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;"><span class="spinner"></span> Fetching data...</td></tr>';

  try {
    // Fetch total external debt stocks for all countries, most recent year
    const url = 'https://api.worldbank.org/v2/country/all/indicator/DT.DOD.DECT.CD?format=json&mrv=1&per_page=200&date=2020:2022';
    const res = await fetch(url);
    if (!res.ok) throw new Error('API error ' + res.status);
    const json = await res.json();
    const records = json[1];

    if (!records || records.length === 0) throw new Error('No data returned');

    allRows = records
      .filter(r => r.value !== null && r.countryiso3code && r.country)
      .map(r => ({
        country: r.country.value,
        code: r.countryiso3code,
        indicator: 'External debt stocks, total',
        year: r.date,
        value: r.value
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
  if (val >= 1e9) return '$' + (val / 1e9).toFixed(1) + 'B';
  if (val >= 1e6) return '$' + (val / 1e6).toFixed(0) + 'M';
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
  const region = document.getElementById('region-filter').value.toLowerCase();
  const filtered = allRows.filter(r => {
    const matchQ = !q || r.country.toLowerCase().includes(q) || r.indicator.toLowerCase().includes(q);
    return matchQ;
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
      responsive: true, maintainAspectRatio: false,
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
  const TEXT  = isLight ? '#4a3c28' : '#8b9ab5';
  const GRID  = isLight ? 'rgba(101,82,50,0.15)' : 'rgba(255,255,255,0.05)';
  Chart.defaults.color = TEXT;

  Chart.instances && Object.values(Chart.instances).forEach(chart => {
    if (!chart) return;
    if (chart.options.scales) {
      ['x','y'].forEach(ax => {
        if (chart.options.scales[ax]) {
          chart.options.scales[ax].grid  = chart.options.scales[ax].grid  || {};
          chart.options.scales[ax].ticks = chart.options.scales[ax].ticks || {};
          chart.options.scales[ax].grid.color  = GRID;
          chart.options.scales[ax].ticks.color = TEXT;
        }
      });
    }
    chart.update('none');
  });
}

// Restore saved preference on load
(function() {
  const saved = localStorage.getItem('ids-theme');
  if (saved === 'light') document.documentElement.classList.add('light');
})();
