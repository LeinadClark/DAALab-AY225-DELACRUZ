# Final Project in DAALab-AY225

Group 16    https://www.kaggle.com/datasets/theworldbank/international-debt-statistics  

DELA CRUZ, LEINAD CLARK M.  
PARANE, JOCELYN B.


# International Debt Statistics (IDS) Dashboard

A single file web dashboard built with HTML, CSS, and JavaScript that visualizes World Bank external debt data for 125+ countries. Features live API data, interactive charts, and a dark/light theme toggle.

# How to
1. Save the file as `index.html`
2. Double-click the file
3. It opens directly in your browser — no setup needed


## 📁 Project Structure
 
```
index.html   ← The entire application (HTML + CSS + JS in one file)
README.md    ← This file
```
 
No additional files, folders, or installation needed.
 
---
 
## 🌐 Requirements
 
| Requirement | Details |
|---|---|
| Modern browser | Chrome, Firefox, Edge, or Safari |
| Internet connection | Required for charts (Chart.js CDN) and live data (World Bank API) |
| Python / Node.js | Only needed if live data tab isn't working |
 
---
 
## 📄 Pages & Features
 
| Page | What it does |
|---|---|
| **Overview** | Shows key stats and 3 illustrative charts (regional debt, debt composition, global trend) |
| **Categories** | Explains the 6 data categories and links to download CSV files from Kaggle |
| **Dataset** | Fetches real-time data from the World Bank API — click **"Load dataset ↗"** |
| **Indicators** | Reference guide for 12 key IDS indicator codes with definitions |
 
---
 
## 🔴 Live Data (Dataset Tab)
 
Click the **"Load dataset ↗"** button on the Dataset page. It will:
 
1. Connect to `https://api.worldbank.org/v2/`
2. Fetch external debt data for all countries (indicator `DT.DOD.DECT.CD`)
3. Populate the table and render the **Top 15 Debtors** chart
You can then search by country name using the search box.
 
---
 
## 🎨 Theme Toggle
 
Click the 🌙 / ☀️ toggle in the top-right corner to switch between:
- **Dark mode** — default navy/blue theme
- **Light mode** — vintage paper/amber theme
Your preference is saved automatically in your browser.
 
---
 
## 📦 External Dependencies (CDN — no install needed)
 
| Library | Version | Purpose |
|---|---|---|
| [Chart.js](https://www.chartjs.org/) | 4.4.1 | All charts and visualizations |
| [Google Fonts](https://fonts.google.com/) | — | Typography (Playfair Display, IBM Plex Mono, etc.) |
| [World Bank API](https://data.worldbank.org/) | v2 | Live debt data on the Dataset page |
 