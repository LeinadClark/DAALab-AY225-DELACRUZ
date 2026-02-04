import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class SortMenu {

    // --- COLOR CONSTANTS ---
    public static final String RESET  = "\u001B[0m";
    public static final String RED    = "\u001B[31m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE   = "\u001B[34m";
    public static final String CYAN   = "\u001B[36m";
    public static final String WHITE_BOLD = "\u001B[1;37m";

    // --- CONFIGURATION ---
    public static final int MENU_WIDTH = 60; 
    static String currentFilename = "dataset.txt"; 

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        int choice;

        do {
            clearScreen();
            printHeader("MAIN MENU");
            
            printRow("Current File: " + YELLOW + currentFilename + RESET);
            printDivider();
            printRow(CYAN + "[1]" + RESET + " Run Bubble Sort");
            printRow(CYAN + "[2]" + RESET + " Run Insertion Sort");
            printRow(CYAN + "[3]" + RESET + " Run Merge Sort");
            printRow(CYAN + "[4]" + RESET + " DATASET OPTIONS (Select/Generate)");
            printRow(RED  + "[0]" + RESET + " Exit");
            printDivider();
            System.out.print(" Enter your choice: " + YELLOW);

            // Input Validation
            while (!console.hasNextInt()) {
                printError("Invalid input! Please enter a number.");
                System.out.print(RESET + " Enter your choice: " + YELLOW);
                console.next();
            }
            choice = console.nextInt();
            System.out.print(RESET); // Reset color after input
            console.nextLine(); // Consume newline

            if (choice == 0) {
                System.out.println("\n" + GREEN + " Exiting... Goodbye!" + RESET);
                break;
            } else if (choice == 4) {
                handleDatasetMenu(console);
            } else if (choice >= 1 && choice <= 3) {
                runSort(choice, currentFilename, console); 
            } else {
                printError("Invalid choice! Please select 0-4.");
                pressEnterToContinue(console);
            }

        } while (choice != 0);
        
        console.close();
    }

    // --- DATASET MENU ---
    public static void handleDatasetMenu(Scanner console) {
        clearScreen();
        printHeader("DATASET MANAGER");
        printRow(CYAN + "[1]" + RESET + " Select 'dataset.txt' (10k Unique)");
        printRow(CYAN + "[2]" + RESET + " Select 'dataset2.txt' (100k Unique)");
        printRow(CYAN + "[3]" + RESET + " GENERATE/SAVE (Overwrite old files)");
        printRow(RED  + "[0]" + RESET + " Back to Main Menu");
        printDivider();
        System.out.print(" Enter choice: " + YELLOW);

        int subChoice = -1;
        if(console.hasNextInt()) {
            subChoice = console.nextInt();
            console.nextLine(); 
        } else {
            console.next(); 
        }
        System.out.print(RESET);

        System.out.println(); 

        switch (subChoice) {
            case 1:
                currentFilename = "dataset.txt";
                printSuccess("Selected: dataset.txt");
                break;
            case 2:
                currentFilename = "dataset2.txt";
                printSuccess("Selected: dataset2.txt");
                break;
            case 3:
                System.out.println(YELLOW + " >> Generating UNIQUE shuffled numbers..." + RESET);
                System.out.print(" >> Writing dataset.txt (100k)... ");
                generateUniqueFile("dataset.txt", 100000); 
                System.out.println(GREEN + "Done." + RESET);
                
                System.out.print(" >> Writing dataset2.txt (10k)... ");
                generateUniqueFile("dataset2.txt", 10000); 
                System.out.println(GREEN + "Done." + RESET);
                break;
            case 0:
                return;
            default:
                printError("Invalid selection.");
        }
        pressEnterToContinue(console);
    }

    // --- SORT EXECUTION ---
    public static void runSort(int choice, String filename, Scanner console) {
        clearScreen();
        printHeader("PROCESSING");
        printRow("File: " + YELLOW + filename + RESET);
        printDivider();

        int[] data = loadDataset(filename);
        if (data == null) {
            pressEnterToContinue(console);
            return; 
        }

        System.out.println("\n >> Dataset loaded: " + GREEN + data.length + " valid numbers." + RESET);
        
        if (data.length > 50000 && (choice == 1 || choice == 2)) {
            System.out.println(YELLOW + " >> NOTE: Large file detected." + RESET);
            System.out.println(YELLOW + " >> Bubble/Insertion sort may take 10 - 30 seconds." + RESET);
        }

        System.out.println(" >> Sorting... Please wait...");

        long startTime = System.nanoTime();

        switch (choice) {
            case 1: bubbleSort(data); break;
            case 2: insertionSort(data); break;
            case 3: mergeSort(data, 0, data.length - 1); break;
        }

        long endTime = System.nanoTime();
        
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        double durationMillis = (endTime - startTime) / 1_000_000.0;

        // Results Display
        clearScreen();
        printHeader("RESULTS");
        printRow("Algorithm Used: " + GREEN + getAlgoName(choice) + RESET);
        printDivider();
        
        // We manually format the inner string to handle colors correctly
        String timeSec = String.format("%10.6f s", durationSeconds);
        String timeMs  = String.format("%10.4f ms", durationMillis);
        
        printRow(String.format("%-25s : %s", "Time (Seconds)", YELLOW + timeSec + RESET));
        printRow(String.format("%-25s : %s", "Time (Milliseconds)", YELLOW + timeMs + RESET));
        printDivider();
        
        System.out.print("\n Sorted Preview: ");
        printArray(data);
        System.out.println();

        // --- SAVE OPTION ---
        System.out.print("\n >> Do you want to save this sorted list? (y/n): " + YELLOW);
        String saveChoice = console.next();
        System.out.print(RESET);
        
        if (saveChoice.equalsIgnoreCase("y")) {
            saveSortedFile(data);
        } else {
            System.out.println(" >> Data not saved.");
        }
        console.nextLine(); 

        pressEnterToContinue(console);
    }

    public static String getAlgoName(int choice) {
        if (choice == 1) return "Bubble Sort";
        if (choice == 2) return "Insertion Sort";
        if (choice == 3) return "Merge Sort";
        return "Unknown";
    }

    // --- FILE HANDLING ---

    public static void saveSortedFile(int[] data) {
        String baseName = "sorted_numbers";
        String extension = ".txt";
        String finalName = baseName + extension;
        int counter = 2;

        File f = new File(finalName);
        while (f.exists()) {
            finalName = baseName + "_" + counter + extension;
            f = new File(finalName);
            counter++;
        }

        try {
            FileWriter writer = new FileWriter(finalName);
            for (int num : data) {
                writer.write(num + " ");
            }
            writer.close();
            printSuccess("Sorted data saved to '" + finalName + "'.");
        } catch (IOException e) {
            printError("Could not save file.");
        }
    }

public static int[] loadDataset(String filename) {
        ArrayList<Integer> list = new ArrayList<>();
        boolean foundGarbage = false;

        // --- SMART FILE FINDER (SEARCH PARTY) ---
        // 1. First, try the basic location (current folder)
        File file = new File(filename);

        // 2. If not found, search recursively in the current directory and sub-directories
        if (!file.exists()) {
            System.out.println(YELLOW + " >> Searching for '" + filename + "' in project folders..." + RESET);
            File currentDir = new File(System.getProperty("user.dir"));
            file = findFileRecursive(currentDir, filename);
        }
        
        // 3. If still null, we truly can't find it
        if (file == null || !file.exists()) {
             printError("File '" + filename + "' not found.");
             System.out.println(" >> Scanned directory: " + System.getProperty("user.dir"));
             System.out.println(" >> Please go to OPTIONS and Generate Files first.");
             return null;
        } else {
            // Optional: Tell the user where we found it, so they know it worked
            // System.out.println(GREEN + " >> Found file at: " + file.getAbsolutePath() + RESET);
        }
        // --- END SMART FINDER ---

        try {
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNext()) {
                if (fileReader.hasNextInt()) {
                    list.add(fileReader.nextInt());
                } else {
                    fileReader.next(); // Skip garbage
                    foundGarbage = true;
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            // This catch block is technically redundant now due to our checks above, 
            // but kept for safety.
            return null;
        }

        if (foundGarbage) {
            System.out.println(YELLOW + " >> Cleanup complete. Proceeding with valid numbers only..." + RESET);
        }

        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    // --- HELPER FUNCTION: RECURSIVE SEARCH ---
    // This helper looks inside folders, and folders inside folders...
    public static File findFileRecursive(File directory, String filename) {
        // Check if the file is in this directory
        File target = new File(directory, filename);
        if (target.exists()) {
            return target;
        }

        // Get a list of all files/folders here
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                // If we find a folder, dive into it (Recurse)
                // We skip hidden folders (starting with .) like .git or .vscode to be faster
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    File found = findFileRecursive(f, filename);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null; // Not found in this branch
    }

    public static void generateUniqueFile(String filename, int count) {
        try {
            List<Integer> numbers = new ArrayList<>(count);
            for (int i = 1; i <= count; i++) numbers.add(i);
            Collections.shuffle(numbers);
            FileWriter writer = new FileWriter(filename);
            for (int num : numbers) writer.write(num + " ");
            writer.close();
        } catch (IOException e) {
            printError("Error creating " + filename);
        }
    }

    // --- UI HELPERS (NOW WITH COLOR SUPPORT) ---

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void pressEnterToContinue(Scanner console) {
        System.out.println("\nPress [ENTER] to continue...");
        console.nextLine();
    }

    public static void printError(String msg) {
        System.out.println(RED + " >> ERROR: " + msg + RESET);
    }

    public static void printSuccess(String msg) {
        System.out.println(GREEN + " >> SUCCESS: " + msg + RESET);
    }

    public static void printDivider() {
        System.out.print(BLUE + "+");
        for (int i = 0; i < MENU_WIDTH; i++) System.out.print("-");
        System.out.println("+" + RESET);
    }

    public static void printHeader(String title) {
        printDivider();
        int leftPadding = (MENU_WIDTH - title.length()) / 2;
        int rightPadding = MENU_WIDTH - title.length() - leftPadding;
        System.out.printf(BLUE + "|%s" + WHITE_BOLD + "%s" + RESET + "%s" + BLUE + "|\n" + RESET, 
            " ".repeat(leftPadding), title, " ".repeat(rightPadding));
        printDivider();
    }

    public static void printRow(String text) {
        // We must calculate the "visible" length without color codes for padding
        int visibleLength = getVisibleLength(text);
        
        // Truncate if too long
        if (visibleLength > MENU_WIDTH - 2) {
            // This is a simple cut; precise cutting with colors is hard, so we assume short strings usually
            text = text.substring(0, MENU_WIDTH - 5) + "..."; 
            visibleLength = getVisibleLength(text);
        }
        
        int rightPadding = MENU_WIDTH - 1 - visibleLength;
        System.out.print(BLUE + "| " + RESET + text + " ".repeat(rightPadding) + BLUE + "|\n" + RESET);
    }

    // Helper to strip ANSI codes so we can measure string length correctly
    public static int getVisibleLength(String s) {
        String ansiRegex = "\u001B\\[[;\\d]*m";
        return s.replaceAll(ansiRegex, "").length();
    }

    public static void printArray(int[] arr) {
        int limit = Math.min(arr.length, 10);
        System.out.print(CYAN + "[ ");
        for (int i = 0; i < limit; i++) System.out.print(arr[i] + " ");
        if (arr.length > 10) System.out.print("... ");
        System.out.println("]" + RESET);
    }

    // --- ALGORITHMS ---
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = temp;
                }
            }
        }
    }
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; ++i) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j]; j = j - 1;
            }
            arr[j + 1] = key;
        }
    }
    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    public static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        int[] L = new int[n1];
        int[] R = new int[n2];
        for (int i = 0; i < n1; ++i) L[i] = arr[left + i];
        for (int j = 0; j < n2; ++j) R[j] = arr[mid + 1 + j];
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) { arr[k] = L[i]; i++; } else { arr[k] = R[j]; j++; } k++;
        }
        while (i < n1) { arr[k] = L[i]; i++; k++; }
        while (j < n2) { arr[k] = R[j]; j++; k++; }
    }
}