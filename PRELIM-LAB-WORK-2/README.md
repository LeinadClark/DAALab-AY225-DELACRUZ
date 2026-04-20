# 🚀 Sort Master Ultimate (GUI)

Prelim Lab Work 2
**Student:** Leinad Clark M. Dela Cruz
**Course:** BSCS 2106L 
 

## 📋 Overview
**Sort Master Ultimate** is a robust Java Swing application designed to analyze the performance of sorting algorithms. Unlike simple console programs, this tool offers a graphical interface to generate datasets, visualize sorting in real-time, and benchmark execution speeds with high precision.

This project specifically targets the analysis of **Bubble Sort** on large datasets (10,000+ integers) to demonstrate the computational cost of nested iterations.

## ✨ Key Features

### 📊 Benchmark & Analysis Tab
* **Precision Timing:** Captures execution time in seconds (e.g., `0.5120 s`).
* **Smart File Finder:** Automatically searches project subdirectories for `dataset.txt` so the app works immediately after cloning.
* **Data Generator:** Built-in tool to create random datasets of any size (e.g., 10,000 items).
* **Verification:** Exports the sorted array to a file (`sorted_numbers.txt`) to prove correctness without freezing the UI.

### 🎬 Real-Time Visualizer Tab
* **Algorithm Animation:** Watch Bubble Sort, Insertion Sort, and Merge Sort rearrange data bars in real-time.
* **Interactive Controls:** Shuffle data, adjust animation speed, and switch algorithms on the fly.

## 🛠️ Requirements
* **Java Development Kit (JDK):** Version 8 or higher.
* **System:** Any operating system with a Graphical User Interface (Windows, macOS, Linux).

## ⚡ How to Run

### 1. Compilation
Open your terminal or command prompt in the project folder and run:
```bash
javac SortMasterFinal.java