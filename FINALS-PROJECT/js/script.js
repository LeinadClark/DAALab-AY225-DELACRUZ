/**
 * GLOBAL STATE & CONSTANTS
 * Stores fetched data and chart instances for global access.
 */
let allRows = [];
let topDebtorsChart = null;

// Global references for the three main interactive charts
let regionChartInstance = null;
let compChartInstance   = null;
let trendChartInstance  = null;

/** * STATIC DATA FOR INITIAL RENDERING
 * Used to populate the dashboard before the user clicks "Load Data".
 */
const REGION_LABELS = ['E. Asia & Pacific', 'Latin Am. & Carib.', 'Sub-Saharan Africa', 'South Asia', 'Europe & C. Asia', 'Mid. East & N. Africa'];
const REGION_DATA   = [3400, 2050, 780, 720, 610, 290];
const REGION_COLORS = ['#3b82f6', '#f59e0b', '#14b8a6', '#f43f5e', '#a78bfa', '#22c55e'];

const COMP_LABELS = ['Public & PPG long-term', 'Private non-guaranteed', 'Short-term debt', 'IMF credit'];
const COMP_DATA   = [52, 28, 14, 6];
const COMP_COLORS = ['#3b82f6', '#f59e0b', '#14b8a6', '#a78bfa'];

const TREND_LABELS = ['2000', '2002', '2004', '2006', '2008', '2010', '2012', '2014', '2016', '2018', '2020', '2022'];
const TREND_DATA   = [2.1, 2.2, 2.5, 2.9, 3.7, 4.2, 5.0, 6.0, 6.8, 7.8, 8.7, 9.3];

/**
 * THEME HELPERS
 * Dynamic color selection based on the presence of the '.light' class on <html>.
 */
function CHART_TEXT() {
  return document.documentElement.classList.contains('light') ? '#4a3c28' : '#8b9ab5';
}
function CHART_GRID() {
  return document.documentElement.classList.contains('light') ? 'rgba(101,82,50,0.15)' : 'rgba(255,255,255,0.05)';
}

// Set global Chart.js defaults
Chart.defaults.color = CHART_TEXT();
Chart.defaults.font.family = "'DM Sans', sans-serif";

/**
 * CHART BUILDER: SCALES
 * Reusable scale configuration for XY charts (Bar, Line, etc.).
 */
function buildScalesXY(yFmt) {
  return {
    x: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 } } },
    y: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 }, callback: yFmt } }
  };
}

/**
 * CHART BUILDER: REGIONAL DEBT
 * Configures the Regional Distribution chart (Bar, Line, Radar, or Polar).
 */
function buildRegionConfig(type) {
  const isRadarOrPolar = type === 'radar' || type === 'polarArea';
  const dataset = {
    label: 'External Debt (USD bn)',
    data: REGION_DATA,
    backgroundColor: isRadarOrPolar ? REGION_COLORS.map(c => c + 'bb') : REGION_COLORS,
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

  // Assign specific scales based on chart type
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

  return { type, data: { labels: REGION_LABELS, datasets: [dataset] }, options };
}

/**
 * CHART BUILDER: COMPOSITION
 * Configures the Debt Composition chart (Doughnut, Pie, or Bar).
 */
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

  if (type === 'bar') options.scales = buildScalesXY(v => v + '%');

  return { type, data: { labels: COMP_LABELS, datasets: [dataset] }, options };
}

/**
 * CHART BUILDER: HISTORICAL TREND
 * Configures the timeline chart (Line, Bar, or Scatter).
 */
function buildTrendConfig(type) {
  const isScatter = type === 'scatter';
  // Transform labels/data into {x, y} objects if Scatter type is chosen
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
    data: isScatter ? { datasets: [dataset] } : { labels: TREND_LABELS, datasets: [dataset] },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 350, easing: 'easeInOutQuart' },
      plugins: { legend: { display: false } },
      scales: isScatter ? scatterScales : buildScalesXY(v => '$' + v + 'T')
    }
  };
}

/**
 * SWITCHER LOGIC
 * Handles UI button active states and destroys/recreates charts on type change.
 */
function setActiveSwitcher(switcherId, clickedBtn) {
  document.querySelectorAll('#' + switcherId + ' .type-btn').forEach(b => b.classList.remove('active'));
  clickedBtn.classList.add('active');
}

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

/**
 * INITIAL RENDER
 * Create the charts for the first time on page load.
 */
regionChartInstance = new Chart(document.getElementById('regionChart'), buildRegionConfig('bar'));
compChartInstance   = new Chart(document.getElementById('compChart'),   buildCompConfig('doughnut'));
trendChartInstance  = new Chart(document.getElementById('trendChart'),  buildTrendConfig('line'));

/**
 * API DATA LOADING
 * Fetches "External Debt Stocks" from World Bank API and updates the table/top debtors chart.
 */
async function loadData() {
  const btn    = document.getElementById('load-btn');
  const status = document.getElementById('api-status');
  const tbody  = document.getElementById('table-body');

  // UI Feedback: Loading state
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> Loading...';
  status.textContent = 'Connecting to World Bank API...';
  tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;"><span class="spinner"></span> Fetching data...</td></tr>';

  try {
    // API URL: Most recent values for external debt total (DT.DOD.DECT.CD)
    const url = 'https://api.worldbank.org/v2/country/all/indicator/DT.DOD.DECT.CD?format=json&mrv=1&per_page=200&date=2020:2022';
    const res = await fetch(url);
    if (!res.ok) throw new Error('API error ' + res.status);
    
    const json = await res.json();
    const records = json[1]; // Index 1 contains the actual data array

    if (!records || records.length === 0) throw new Error('No data returned');

    // Clean and map the data
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
    tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;color:var(--rose);">Failed to load data.</td></tr>';
    btn.innerHTML = 'Retry ↗';
    btn.disabled = false;
  }
}

/**
 * FORMATTER
 * Converts large numbers into readable currency strings (e.g., $1.2T, $500B).
 */
function fmt(val) {
  if (val === null || val === undefined) return '—';
  if (val >= 1e12) return '$' + (val / 1e12).toFixed(2) + 'T';
  if (val >= 1e9)  return '$' + (val / 1e9).toFixed(1)  + 'B';
  if (val >= 1e6)  return '$' + (val / 1e6).toFixed(0)  + 'M';
  return '$' + val.toLocaleString();
}

/**
 * TABLE RENDERING
 * Injects row data into the HTML table body.
 */
function renderTable(rows) {
  const tbody = document.getElementById('table-body');
  if (rows.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No results match</td></tr>';
    document.getElementById('row-count').textContent = '0 rows';
    return;
  }
  const display = rows.slice(0, 200); // Limit display to 200 rows for performance
  const headers = Object.keys(display[0]);
  
  tbody.innerHTML = display.map(r => `
    <tr>
      ${headers.map(header => `<td>${r[header] || '—'}</td>`).join('')}
    </tr>
  `).join('');
  document.getElementById('row-count').textContent = rows.length + ' records' + (rows.length > 200 ? ' (showing 200)' : '');
}

/**
 * SEARCH FILTER
 * Filters the stored allRows array based on user input.
 */
function filterTable() {
  if (allRows.length === 0) return;
  const q = document.getElementById('search-input').value.toLowerCase();
  const filtered = allRows.filter(r => {
    return !q || Object.values(r).some(val => String(val).toLowerCase().includes(q));
  });
  renderTable(filtered);
}

/**
 * TOP DEBTORS CHART
 * Creates a horizontal bar chart of the top 15 debt holders.
 */
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
          if (i === 0) return light ? '#b8860b' : '#f59e0b'; // Winner color
          if (i < 3)   return light ? '#8b4513' : '#3b82f6'; // Runners up
          return light ? '#c8b89a' : '#1e3a5f';              // Others
        }),
        borderRadius: 4,
        borderSkipped: false
      }]
    },
    options: {
      indexAxis: 'y', // Makes it horizontal
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

/**
 * THEME TOGGLE & PERSISTENCE
 * Switches between dark/light mode and updates existing chart instances without full reload.
 */
function toggleTheme() {
  const isLight = document.documentElement.classList.toggle('light');
  localStorage.setItem('ids-theme', isLight ? 'light' : 'dark');
  updateChartTheme(isLight);
}

function updateChartTheme(isLight) {
  const TEXT = isLight ? '#4a3c28' : '#8b9ab5';
  const GRID = isLight ? 'rgba(101,82,50,0.15)' : 'rgba(255,255,255,0.05)';
  Chart.defaults.color = TEXT;

  // Loop through all active Chart.js instances and update their colors
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
    chart.update('none'); // Update without animation for a snappier feel
  });
}

/**
 * CSV FILE UPLOAD
 * Handles file selection, validation, parsing, and visualization of uploaded CSV files.
 */
function handleFileUpload(event) {
  const file = event.target.files[0];
  const status = document.getElementById('api-status');
  const tbody = document.getElementById('table-body');

  if (!file) return;

  // Validate file type - only accept CSV files
  if (file.type !== 'text/csv' && !file.name.endsWith('.csv')) {
    status.textContent = 'Error: Only CSV files are allowed';
    status.style.color = 'var(--rose)';
    tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;color:var(--rose);">Invalid file type. Please upload a CSV file.</td></tr>';
    event.target.value = '';
    return;
  }

  status.textContent = 'Reading CSV file...';
  status.style.color = 'var(--text3)';
  tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;"><span class="spinner"></span> Parsing CSV...</td></tr>';

  const reader = new FileReader();
  reader.onload = function(e) {
    try {
      const csvText = e.target.result;
      const rows = parseCSV(csvText);
      
      if (rows.length === 0) {
        throw new Error('No data found in CSV');
      }

      // Keep original CSV data structure for table display
      allRows = rows;

      status.textContent = allRows.length + ' records loaded from CSV · ' + new Date().toLocaleTimeString();
      status.style.color = 'var(--green)';
      renderTable(allRows);
      
      // Update table headers to match CSV structure
      updateTableHeadersFromCSV(rows[0]);
      
      // Generate visualizations from uploaded CSV data
      renderCSVVisualizations(rows);

    } catch (err) {
      status.textContent = 'Error parsing CSV: ' + err.message;
      status.style.color = 'var(--rose)';
      tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;color:var(--rose);">Failed to parse CSV file. Please check the file format.</td></tr>';
    }
  };

  reader.onerror = function() {
    status.textContent = 'Error reading file';
    status.style.color = 'var(--rose)';
    tbody.innerHTML = '<tr class="loading-row"><td colspan="5" style="text-align:center;padding:2rem;color:var(--rose);">Failed to read the file.</td></tr>';
  };

  reader.readAsText(file);
}

function parseCSV(csvText) {
  const lines = csvText.split('\n').filter(line => line.trim());
  if (lines.length === 0) return [];

  const headers = parseCSVLine(lines[0]);
  const data = [];

  for (let i = 1; i < lines.length; i++) {
    const values = parseCSVLine(lines[i]);
    if (values.length === headers.length) {
      const row = {};
      headers.forEach((header, index) => {
        row[header] = values[index];
      });
      data.push(row);
    }
  }

  return data;
}

function parseCSVLine(line) {
  const result = [];
  let current = '';
  let inQuotes = false;

  for (let i = 0; i < line.length; i++) {
    const char = line[i];
    if (char === '"') {
      inQuotes = !inQuotes;
    } else if (char === ',' && !inQuotes) {
      result.push(current.trim());
      current = '';
    } else {
      current += char;
    }
  }
  result.push(current.trim());
  return result;
}

function updateTableHeadersFromCSV(sampleRow) {
  const thead = document.querySelector('#data-table thead tr');
  const headers = Object.keys(sampleRow);
  
  thead.innerHTML = headers.map(header => 
    `<th>${header}</th>`
  ).join('');
}

function renderCSVVisualizations(rows) {
  if (!rows || rows.length === 0) return;
  
  const headers = Object.keys(rows[0]);
  const numericColumns = headers.filter(header => {
    return rows.some(row => !isNaN(parseFloat(row[header])) && row[header] !== '');
  });
  
  const textColumns = headers.filter(header => {
    return rows.some(row => isNaN(parseFloat(row[header])) || row[header] === '');
  });
  
  const labelColumn = textColumns[0] || headers[0];
  
  // If we have numeric data, create numeric visualizations
  if (numericColumns.length > 0) {
    const valueColumn = numericColumns[0];
    const sorted = [...rows]
      .filter(row => !isNaN(parseFloat(row[valueColumn])))
      .sort((a, b) => parseFloat(b[valueColumn]) - parseFloat(a[valueColumn]))
      .slice(0, 15);
    
    if (sorted.length > 0) {
      const canvas = document.getElementById('topDebtorsChart');
      if (topDebtorsChart) topDebtorsChart.destroy();
      
      topDebtorsChart = new Chart(canvas, {
        type: 'bar',
        data: {
          labels: sorted.map(r => r[labelColumn]),
          datasets: [{
            label: valueColumn,
            data: sorted.map(r => parseFloat(r[valueColumn])),
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
          plugins: { 
            legend: { display: false },
            title: {
              display: true,
              text: `Top 15 by ${valueColumn}`,
              color: CHART_TEXT(),
              font: { size: 14 }
            }
          },
          scales: {
            x: { 
              grid: { color: CHART_GRID() }, 
              ticks: { color: CHART_TEXT(), font: { size: 11 } } 
            },
            y: { 
              grid: { color: 'transparent' }, 
              ticks: { color: CHART_TEXT(), font: { size: 12 } } 
            }
          }
        }
      });
    }
    
    // Create a distribution chart if we have multiple numeric columns
    if (numericColumns.length >= 2) {
      const canvas = document.getElementById('regionChart');
      if (regionChartInstance) regionChartInstance.destroy();
      
      const labels = rows.slice(0, 10).map(r => r[labelColumn]);
      const datasets = numericColumns.slice(0, 3).map((col, idx) => ({
        label: col,
        data: rows.slice(0, 10).map(r => parseFloat(r[col]) || 0),
        backgroundColor: [REGION_COLORS[idx % REGION_COLORS.length]],
        borderRadius: 4,
        borderSkipped: false
      }));
      
      regionChartInstance = new Chart(canvas, {
        type: 'bar',
        data: { labels, datasets },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              labels: { color: CHART_TEXT(), font: { size: 11 }, boxWidth: 10, padding: 10 }
            },
            title: {
              display: true,
              text: 'Data Distribution',
              color: CHART_TEXT(),
              font: { size: 14 }
            }
          },
          scales: {
            x: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 } } },
            y: { grid: { color: CHART_GRID() }, ticks: { color: CHART_TEXT(), font: { size: 11 } } }
          }
        }
      });
    }
  } else {
    // For text-only data (like IDSSeries-Time.csv), create categorical visualizations
    const canvas = document.getElementById('topDebtorsChart');
    if (topDebtorsChart) topDebtorsChart.destroy();
    
    // Count occurrences of unique values in the first text column
    const valueCounts = {};
    rows.forEach(row => {
      const val = row[labelColumn] || 'Unknown';
      valueCounts[val] = (valueCounts[val] || 0) + 1;
    });
    
    const sortedLabels = Object.entries(valueCounts)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 15);
    
    if (sortedLabels.length > 0) {
      topDebtorsChart = new Chart(canvas, {
        type: 'bar',
        data: {
          labels: sortedLabels.map(([label]) => label),
          datasets: [{
            label: 'Count',
            data: sortedLabels.map(([, count]) => count),
            backgroundColor: sortedLabels.map((_, i) => {
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
          plugins: { 
            legend: { display: false },
            title: {
              display: true,
              text: `Distribution by ${labelColumn}`,
              color: CHART_TEXT(),
              font: { size: 14 }
            }
          },
          scales: {
            x: { 
              grid: { color: CHART_GRID() }, 
              ticks: { color: CHART_TEXT(), font: { size: 11 } } 
            },
            y: { 
              grid: { color: 'transparent' }, 
              ticks: { color: CHART_TEXT(), font: { size: 12 } } 
            }
          }
        }
      });
    }
    
    // Create a pie chart for column distribution if we have multiple text columns
    if (textColumns.length >= 2) {
      const canvas = document.getElementById('regionChart');
      if (regionChartInstance) regionChartInstance.destroy();
      
      const secondColumnCounts = {};
      rows.forEach(row => {
        const val = row[textColumns[1]] || 'Unknown';
        secondColumnCounts[val] = (secondColumnCounts[val] || 0) + 1;
      });
      
      const pieLabels = Object.keys(secondColumnCounts).slice(0, 8);
      const pieData = pieLabels.map(label => secondColumnCounts[label]);
      
      regionChartInstance = new Chart(canvas, {
        type: 'doughnut',
        data: {
          labels: pieLabels,
          datasets: [{
            data: pieData,
            backgroundColor: pieLabels.map((_, i) => REGION_COLORS[i % REGION_COLORS.length]),
            borderWidth: 2,
            borderColor: 'transparent'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              position: 'right',
              labels: { color: CHART_TEXT(), font: { size: 11 }, boxWidth: 10, padding: 10 }
            },
            title: {
              display: true,
              text: `Distribution by ${textColumns[1]}`,
              color: CHART_TEXT(),
              font: { size: 14 }
            }
          }
        }
      });
    }
  }
}