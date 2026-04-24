
---

# 📊 International Debt Statistics (IDS) Dashboard
> A data visualization dashboard for analyzing global external debt trends using real-time World Bank data.

[![Repository](https://img.shields.io/badge/Repository-GitHub-black?style=for-the-badge\&logo=github)](https://github.com/LeinadClark/DAALab-AY225-DELACRUZ)

## **DAALab-AY225 Final Project | Group 16**  
![Team Work](https://img.shields.io/badge/Team-2%20Developers-yellow?style=for-the-badge)   
**DELA CRUZ, LEINAD CLARK M.**  ![Contributor](https://img.shields.io/badge/LeinadClark-Repo%20Owner,%20lead%20developer%20&%20%20bug%20fixing-blue?style=for-the-badge)    
**PARANE, JOCELYN B.**
![Contributor](https://img.shields.io/badge/JocelynBaylon-collaborator,%20UI%2FUX%20%26%20Data%20Design-pink?style=for-the-badge)



This project was developed through a **shared Git repository workflow**, following collaborative development practices between team members.

![Version Control](https://img.shields.io/badge/Version%20Control-Git-black?style=for-the-badge\&logo=github)
![Git Collaboration](https://img.shields.io/badge/Git-Collaborative%20Project-blue?style=for-the-badge\&logo=git)

## 🛠️ Tech Stack

![HTML5](https://img.shields.io/badge/HTML5-%23E34F26.svg?style=for-the-badge\&logo=html5\&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-%231572B6.svg?style=for-the-badge\&logo=css3\&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-%23F7DF1E.svg?style=for-the-badge\&logo=javascript\&logoColor=black)
![Chart.js](https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge\&logo=chartdotjs\&logoColor=white)
![REST API](https://img.shields.io/badge/REST%20API-02569B.svg?style=for-the-badge\&logo=api\&logoColor=white)
![LocalStorage](https://img.shields.io/badge/LocalStorage-F7DF1E.svg?style=for-the-badge\&logo=javascript\&logoColor=black)

---

## 🧩 Architecture

![Frontend Only](https://img.shields.io/badge/Architecture-Frontend--Only-blue?style=for-the-badge)
![Client Side Rendering](https://img.shields.io/badge/Rendering-Client--Side-green?style=for-the-badge)
![No Backend](https://img.shields.io/badge/Backend-None-critical?style=for-the-badge)

---

## 📌 Project Metadata

![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)
![Project Type](https://img.shields.io/badge/Project-Academic-blueviolet?style=for-the-badge)

---

## 📊 Data Source

![Data Source](https://img.shields.io/badge/Data-World%20Bank%20API-orange?style=for-the-badge)

[![Dataset](https://img.shields.io/badge/Dataset-World%20Bank%20IDS-orange?style=for-the-badge\&logo=kaggle)](https://www.kaggle.com/datasets/theworldbank/international-debt-statistics)

---


## ⚡ Key Features

* 📡 Real-time data integration from World Bank API
* 📊 Interactive charts powered by Chart.js
* 🔀 Chart type switcher — toggle between multiple visualization types per chart
* 🌍 Coverage of 125+ countries
* 🔍 Searchable and filterable dataset
* 🌗 Persistent dark/light theme (localStorage)
* ⚡ Fully client-side (no backend required)

---

## 🧠 Project Overview

This project visualizes **external debt statistics** across low- and middle-income countries using dynamic, real-time data.

It enables users to:

* Analyze global debt distribution
* Compare countries based on debt levels
* Explore economic trends through interactive charts
* Switch chart visualization types on the fly for deeper data exploration



The dashboard highlights patterns in **debt stocks, composition, and growth**, offering insights into global financial structures and disparities.

---

## 📁 Project Structure

```text
FINALS-PROJECT/
├── index.html          # Landing / Overview
├── finals-proj.html    # Main dashboard page
├── dataset.html        # Live dataset + charts
├── categories.html     # IDS categories breakdown
├── indicators.html     # Indicator reference guide
├── css/
│   └── styles.css      # Global styles
    └── chart-switcher.css  # Chart type switcher styles
├── js/
│   └── script.js       # Core logic + API handling
└── README.md
```

---

## 🚀 How to Run

1. Clone or download the repository
2. Open `index.html` in any modern browser
3. Navigate through the dashboard pages

> ⚠️ No installation or setup required.

---

## 🌐 Requirements

| Requirement         | Description                                           |
| ------------------- | ----------------------------------------------------- |
| Modern Browser      | Chrome, Firefox, Edge, Safari                         |
| Internet Connection | Required for API requests and Chart.js CDN            |
| Optional Runtime    | Python/Node.js (if browser restricts local API calls) |

---

## 📄 Pages & Features

| Page               | Description                                                                           |
| ------------------ | ------------------------------------------------------------------------------------- |
| **Overview**       | Displays global summaries and key visualizations (regional debt, composition, trends) |
| **Categories**     | Explains the 6 IDS data groups with downloadable dataset links                        |
| **Dataset (Live)** | Fetches real-time data and renders charts + searchable table                          |
| **Indicators**     | Technical reference for key IDS indicator codes                                       |

---

## 🔴 Live Data Integration

This feature allows the system to fetch and display real-time external debt data from an online API.

### How to load the dataset:

1. Navigate to the **Dataset** page  
2. Click the **"Load Dataset ↗"** button  
3. The system will automatically:

   - Connect to the World Bank API (`https://api.worldbank.org/v2/`)
   - Retrieve external debt data (`DT.DOD.DECT.CD`)
   - Populate the data table with fetched results  
   - Generate a **Top 15 Debtors** visualization chart  

---


## 🎨 UI & Theme System

The dashboard includes a persistent theme toggle stored in `localStorage`:

* 🌙 **Dark Mode (Default):** High-contrast navy/blue interface
* ☀️ **Light Mode:** Vintage-inspired amber/paper aesthetic

Designed for both readability and visual appeal.



## 📦 External Dependencies

| Library        | Version | Purpose            |
| -------------- | ------- | ------------------ |
| Chart.js       | 4.4.1   | Data visualization |
| Google Fonts   | Latest  | Typography         |
| World Bank API | v2      | Live data source   |

---

## 📌 Key Indicator Example

* `DT.DOD.DECT.CD` → External debt stocks (current US$)
  Used to rank countries and generate comparative visualizations.

---

## 🔧 Key Contributions & Skills Applied
* API integration with live World Bank data
* Data visualization using Chart.js
* Modular front-end architecture design
* Real-world dataset exploration and analysis
* Collaborative development using Git workflows

It reflects real-world development practices, including shared version control, coordinated feature development, and iterative integration.

---




## 🌿 Git Workflow

![Branching](https://img.shields.io/badge/Workflow-Feature%20Branching-lightgrey?style=for-the-badge)
![Integration](https://img.shields.io/badge/Integration-Manual%20Merge-yellow?style=for-the-badge)

* Main branch used for stable builds
* Feature-based development per module
* Regular commits for incremental progress
* Manual review before final integration

---