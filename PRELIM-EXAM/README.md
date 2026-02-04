# SortMasterExam 📊

![Java](https://img.shields.io/badge/Java-11%2B-orange)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-blue)

A Java Swing application for benchmarking and visualizing sorting algorithms (Bubble Sort, Insertion Sort, Merge Sort) with CSV data generation and analysis capabilities.

---

## 🚀 Features

* *Three Sorting Algorithms*: Benchmark Bubble Sort, Insertion Sort, and Merge Sort.
* *CSV Data Management*: Generate, load, and export CSV files with customizable parameters.
* *Data Visualization*: Real-time preview of sorted data with pagination.
* *Performance Metrics*: Detailed timing and progress tracking for each algorithm.
* *Auto-Benchmarking*: Run comprehensive benchmarks across different dataset sizes.
* *Dark Modern UI*: Clean, professional interface with progress indicators.

---

## 🛠️ Requirements

* Java 11 or newer.
* No external dependencies required.

---

## 📥 Quick Start

1.  *Clone or download* the repository.
2.  *Compile* the application:
    
    javac SortMasterExam.java
    
3.  *Run* the application:
    
    java SortMasterExam
    

---

## 📖 Usage Guide

### 1. Loading Data
* *Generate CSV*: Create new datasets with customizable row counts.
* *Load CSV*: Import existing CSV files with ID, FirstName, LastName columns.
* *Default File*: Automatically loads generated_data.csv if present.

### 2. Configuration
* *Rows to Sort*: Select or enter the number of rows to process (1,000 - 100,000+).
* *Sort Column*: Choose to sort by ID, First Name, or Last Name.
* *Preview Settings*: Adjust the number of rows displayed per page.

### 3. Running Benchmarks
* *Individual Algorithms*: Run Bubble Sort, Insertion Sort, or Merge Sort separately.
* *Auto Benchmark*: Comprehensive test across multiple dataset sizes.
* *Warning System*: Alerts for potentially slow operations with large datasets.

### 4. Exporting Results
* Export sorted subsets with options to:
    * Include/exclude header rows.
    * Renumber IDs starting from 1.
* Benchmark results automatically saved to benchmark_results.csv.

---

## 📊 Performance Analysis



| Algorithm | Best Case | Average Case | Worst Case | Memory | Performance |
| :--- | :--- | :--- | :--- | :--- | :--- |
| *Bubble Sort* | $O(n)$ | $O(n^2)$ | $O(n^2)$ | $O(1)$ | Slow on large sets |
| *Insertion Sort* | $O(n)$ | $O(n^2)$ | $O(n^2)$ | $O(1)$ | Good for small sets |
| *Merge Sort* | $O(n \log n)$ | $O(n \log n)$ | $O(n \log n)$ | $O(n)$ | Best for large sets |

---

## 📁 File Management

### Generated Files
* *CSV Files*: Saved in project folder with pattern generated_data_<N>_YYYYMMDD_HHMMSS.csv.
* *Export Files*: User-defined names via save dialog.
* *Benchmark Results*: Saved as benchmark_results.csv.

### Ignored Files
* .class files are build artifacts and excluded from version control.
* Regenerated automatically when compiling.

---

## 🔧 Troubleshooting

### Common Issues
1.  *"Could not find or load main class"*
    * Ensure you're in the correct directory containing SortMasterExam.java.
    * Remove any package declaration if present.
    * Compile before running: javac SortMasterExam.java.
2.  *CSV Loading Errors*
    * Verify file format matches required columns.
    * Check for empty lines or malformed data.
3.  *Slow Performance*
    * For large datasets, prefer Merge Sort.
    * Reduce preview size for better responsiveness.

### Memory Requirements
* Minimum: 512MB RAM.
* Recommended: 1GB+ RAM for large datasets (>100,000 rows).

---

## 📜 License & Support

© 2026 Leinad Clark. All rights reserved.

*Support*:
1.  Verify Java version: java -version.
2.  Check file permissions in the working directory.
3.  Ensure CSV files are properly formatted.

---

*Note*: This tool is designed for educational purposes to demonstrate sorting algorithm performance differences in a visual, interactive environment.