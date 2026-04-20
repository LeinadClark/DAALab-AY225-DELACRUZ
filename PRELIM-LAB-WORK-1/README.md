Empirical Analysis of Exchange Sorts (Bubble Sort)

Prelim Lab Work 1
**Student:** Leinad Clark M. Dela Cruz
**Course:** BSCS 2106L 
 
Overview
This console-based Java application implements **Bubble Sort** to analyze its performance on large datasets. It specifically allows for the generation and sorting of a randomized dataset containing **10,000 integers**, measuring the "cost" of nested iterations in execution time.

## ⚙️ Features
* **Algorithmic Analysis:** Implements Bubble Sort (Exchange Sort) alongside Insertion and Merge Sort for comparison.
* **Data Generation:** Built-in tool to generate random datasets:
    * `dataset.txt` (10,000 items)
    * `dataset2.txt` (100,000 items)
* **Precision Metrics:** Measures execution time using `System.nanoTime()` and converts it to seconds/milliseconds.
* **Verification:** Exports the fully sorted array to `sorted_numbers.txt` to verify correctness without cluttering the console.
* **Smart File Loading:** Automatically detects input files in the project directory.

## 🚀 How to Run

### Prerequisites
* Java Development Kit (JDK) 8 or higher.
* Terminal or Command Prompt.

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/LeinadClark/DAALab-AY225-DelaCruz.git
    ```
2.  Navigate to the project folder:
    ```bash
    cd DAALAB-AY225-DELACRUZ
    ```

### Execution Steps
1.  **Compile the code:**
    ```bash
    javac SortMenu.java
    ```
2.  **Run the application:**
    ```bash
    java SortMenu
    ```

## 🧪 Lab Walkthrough (Meeting Requirements)

To replicate the specific requirements of Prelim Lab Work 1 (10,000 elements):

1.  **Generate the Data:**
    * On the Main Menu, select **[4] DATASET OPTIONS**.
    * Select **[3] GENERATE/SAVE**.
    * *This creates `dataset2.txt` containing 10,000 unique random integers.*

2.  **Select the 10k Dataset:**
    * On the Dataset Menu, select **[2] Select 'dataset2.txt'**.
    * Return to the Main Menu.

3.  **Run Bubble Sort:**
    * Select **[1] Run Bubble Sort**.
    * Observe the **Time (Seconds)** output.

4.  **Verify Output:**
    * When asked *"Do you want to save this sorted list?"*, type **y**.
    * Open `sorted_numbers.txt` to verify the array is sorted correctly.

## 📂 File Structure
* `SortMenu.java`: The main source code containing the Bubble Sort logic.
* `dataset2.txt`: The target dataset with 10,000 random integers.
* `sorted_numbers.txt`: The output file generated after sorting.