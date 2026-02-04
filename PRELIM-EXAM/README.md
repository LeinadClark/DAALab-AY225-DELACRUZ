Here's an improved, well-structured README.md:

# SortMasterExam

A Java Swing application for benchmarking and visualizing sorting algorithms (Bubble Sort, Insertion Sort, Merge Sort) with CSV data generation and analysis capabilities.

## Features

- **Three Sorting Algorithms**: Benchmark Bubble Sort, Insertion Sort, and Merge Sort
- **CSV Data Management**: Generate, load, and export CSV files with customizable parameters
- **Data Visualization**: Real-time preview of sorted data with pagination
- **Performance Metrics**: Detailed timing and progress tracking for each algorithm
- **Auto-Benchmarking**: Run comprehensive benchmarks across different dataset sizes
- **Dark Modern UI**: Clean, professional interface with progress indicators

## Requirements

- Java 11 or newer
- No external dependencies required

## Quick Start

1. **Clone or download** the repository
2. **Compile** the application:
   ```bash
   javac SortMasterExam.java
   ```
3. **Run** the application:
   ```bash
   java SortMasterExam
   ```

## Usage Guide

### 1. Loading Data
- **Generate CSV**: Create new datasets with customizable row counts
- **Load CSV**: Import existing CSV files with ID, FirstName, LastName columns
- **Default File**: Automatically loads `generated_data.csv` if present

### 2. Configuration
- **Rows to Sort**: Select or enter the number of rows to process (1,000 - 100,000+)
- **Sort Column**: Choose to sort by ID, First Name, or Last Name
- **Preview Settings**: Adjust the number of rows displayed per page

### 3. Running Benchmarks
- **Individual Algorithms**: Run Bubble Sort, Insertion Sort, or Merge Sort separately
- **Auto Benchmark**: Comprehensive test across multiple dataset sizes
- **Warning System**: Alerts for potentially slow operations with large datasets

### 4. Exporting Results
- Export sorted subsets with options to:
  - Include/exclude header rows
  - Renumber IDs starting from 1
- Benchmark results automatically saved to `benchmark_results.csv`

## File Management

### Generated Files
- **CSV Files**: Saved in project folder with pattern `generated_data_<N>_YYYYMMDD_HHMMSS.csv`
- **Export Files**: User-defined names via save dialog
- **Benchmark Results**: Saved as `benchmark_results.csv`

### Ignored Files
- `.class` files are build artifacts and excluded from version control
- Regenerated automatically when compiling

## Performance Considerations

- **Small Datasets** (<10,000 rows): All algorithms perform well
- **Medium Datasets** (10,000-50,000 rows): Merge Sort recommended
- **Large Datasets** (>50,000 rows): O(n²) algorithms (Bubble/Insertion) may be slow
- **Progress Indicators**: Real-time progress tracking for all operations

## CSV Format

Required CSV format for import:
```
ID,FirstName,LastName
1,John,Smith
2,Jane,Doe
3,Alex,Johnson
```

Exported files maintain the same format with optional renumbering.

## Troubleshooting

### Common Issues

1. **"Could not find or load main class"**
   - Ensure you're in the correct directory containing `SortMasterExam.java`
   - Remove any `package` declaration if present
   - Compile before running: `javac SortMasterExam.java`

2. **CSV Loading Errors**
   - Verify file format matches required columns
   - Check for empty lines or malformed data
   - Ensure proper CSV quoting for names with commas

3. **Slow Performance**
   - For large datasets, prefer Merge Sort
   - Reduce preview size for better responsiveness
   - Close unnecessary applications to free memory

### Memory Requirements
- Minimum: 512MB RAM
- Recommended: 1GB+ RAM for large datasets (>100,000 rows)

## License

© 2023 Leinad Clark. All rights reserved.

## Support

For issues or questions:
1. Verify Java version: `java -version`
2. Check file permissions in the working directory
3. Ensure CSV files are properly formatted

---

**Note**: This tool is designed for educational purposes to demonstrate sorting algorithm performance differences in a visual, interactive environment.