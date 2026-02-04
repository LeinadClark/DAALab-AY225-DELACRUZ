import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer; 

public class SortMasterFinal extends JFrame {

    // --- DARK THEME PALETTE ---
    private final Color COL_BG_MAIN     = new Color(32, 33, 36);    
    private final Color COL_BG_PANEL    = new Color(48, 50, 56);    
    private final Color COL_SIDEBAR     = new Color(40, 42, 48);    
    private final Color COL_TEXT_MAIN   = new Color(245, 245, 245); 
    private final Color COL_TEXT_SUB    = new Color(170, 172, 178); 
    private final Color COL_ACCENT      = new Color(100, 149, 237); // Blue
    private final Color COL_SUCCESS     = new Color(75, 181, 67);   // Green
    private final Color COL_DANGER      = new Color(220, 53, 69);   // Red
    private final Color COL_BAR_DEF     = new Color(70, 130, 180);  
    private final Color COL_BAR_ACTIVE  = new Color(255, 69, 58);   

    // --- COMPONENTS ---
    private JTabbedPane tabbedPane;
    
    // TAB 1: Benchmark
    private JTextArea benchLog;
    private JTextPane infoPanel; 
    private JLabel lblTimeResult;
    private JLabel lblCurrentFile;
    private ModernButton btnSave, btnBub, btnIns, btnMrg;
    private int[] benchmarkData = null;
    private int[] sortedData = null;
    
    // --- SMART FINDER VARIABLES ---
    private String currentFilename = "dataset.txt";
    private File actualFileRef = null; // <--- Stores the exact location of the found file

    // TAB 2: Visualizer
    private VisualPanel visualPanel;
    private int[] visualData;
    private volatile boolean isVisualRunning = false;
    private final int VISUAL_SIZE = 60; 
    private int animationDelay = 20; 
    private JLabel lblVisualStatus;
    private JLabel lblVisualTime;   
    private JLabel lblSpeedValue;   
    private long visualStartTime;
    private Timer visualStopwatch;  

    public SortMasterFinal() {
        setTitle("Sort Master Ultimate");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COL_BG_MAIN);

        UIManager.put("TabbedPane.selected", COL_BG_PANEL);
        UIManager.put("TabbedPane.border", BorderFactory.createEmptyBorder());
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(COL_BG_MAIN);
        tabbedPane.setForeground(COL_TEXT_MAIN);
        
        tabbedPane.addTab("  Benchmark & Analysis  ", createBenchmarkPanel());
        tabbedPane.addTab("  Real-Time Visualizer  ", createVisualizerPanel());

        add(tabbedPane);
        
        // Initial Load
        loadBenchmarkData("dataset.txt");

        visualStopwatch = new Timer(100, e -> {
            if (isVisualRunning) {
                long elapsed = System.currentTimeMillis() - visualStartTime;
                lblVisualTime.setText(String.format("Time: %.1fs", elapsed / 1000.0));
            }
        });
    }

    // ==========================================
    //       TAB 1: BENCHMARK PANEL
    // ==========================================
    private JPanel createBenchmarkPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(COL_BG_MAIN);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // -- TOP --
        JPanel topBox = new JPanel(new BorderLayout());
        topBox.setBackground(COL_BG_MAIN);

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1));
        headerTextPanel.setBackground(COL_BG_MAIN);

        JLabel title = new JLabel("Performance Analyzer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COL_TEXT_MAIN);
        
        lblCurrentFile = new JLabel("Current File: None");
        lblCurrentFile.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentFile.setForeground(COL_ACCENT);
        lblCurrentFile.setBorder(new EmptyBorder(5, 0, 0, 0));

        headerTextPanel.add(title);
        headerTextPanel.add(lblCurrentFile);
        
        JPanel fileControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        fileControls.setBackground(COL_BG_MAIN);
        
        ModernButton btnLoad = new ModernButton("Load File", COL_BG_PANEL, COL_TEXT_MAIN);
        ModernButton btnGen  = new ModernButton("Generate Custom Data", COL_ACCENT, Color.WHITE);
        ModernButton btnView = new ModernButton("View Raw Data", COL_BG_PANEL, COL_TEXT_MAIN);

        fileControls.add(btnView);
        fileControls.add(btnLoad);
        fileControls.add(btnGen);
        topBox.add(headerTextPanel, BorderLayout.WEST);
        topBox.add(fileControls, BorderLayout.EAST);

        // -- CENTER --
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5); 
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setBackground(COL_BG_MAIN);

        // LEFT
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(COL_BG_PANEL);
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBackground(COL_BG_PANEL);
        
        JLabel lblLogTitle = new JLabel("Activity Log");
        lblLogTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLogTitle.setForeground(COL_TEXT_SUB);
        
        JButton btnClearLog = new JButton("Clear Log");
        btnClearLog.setContentAreaFilled(false);
        btnClearLog.setBorderPainted(false);
        btnClearLog.setFocusPainted(false);
        btnClearLog.setForeground(COL_DANGER);
        btnClearLog.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearLog.addActionListener(e -> benchLog.setText(""));

        logHeader.add(lblLogTitle, BorderLayout.WEST);
        logHeader.add(btnClearLog, BorderLayout.EAST);
        
        benchLog = new JTextArea();
        benchLog.setEditable(false);
        benchLog.setFont(new Font("Consolas", Font.PLAIN, 13)); 
        benchLog.setBackground(new Color(40, 42, 48));
        benchLog.setForeground(COL_SUCCESS);
        benchLog.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblTimeResult = new JLabel("0.000 s", SwingConstants.CENTER);
        lblTimeResult.setFont(new Font("Segoe UI", Font.BOLD, 50));
        lblTimeResult.setForeground(COL_TEXT_MAIN);
        lblTimeResult.setBorder(new EmptyBorder(20, 0, 10, 0));

        leftPanel.add(logHeader, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(benchLog), BorderLayout.CENTER);
        leftPanel.add(lblTimeResult, BorderLayout.SOUTH);

        // RIGHT
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(COL_BG_PANEL);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblInfoTitle = new JLabel("Algorithm Simplified");
        lblInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfoTitle.setForeground(COL_TEXT_SUB);

        infoPanel = new JTextPane();
        infoPanel.setEditorKit(new HTMLEditorKit());
        infoPanel.setEditable(false);
        infoPanel.setBackground(COL_BG_PANEL);
        infoPanel.setBorder(null);
        updateInfoPanel("None"); 

        rightPanel.add(lblInfoTitle, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(infoPanel), BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        // -- BOTTOM --
        JPanel bottomBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomBox.setBackground(COL_BG_MAIN);

        btnBub = new ModernButton("Bubble Sort", COL_BG_PANEL, COL_TEXT_MAIN);
        btnIns = new ModernButton("Insertion Sort", COL_BG_PANEL, COL_TEXT_MAIN);
        btnMrg = new ModernButton("Merge Sort", COL_BG_PANEL, COL_TEXT_MAIN);
        btnSave = new ModernButton("Save Result", COL_SUCCESS, Color.WHITE);
        btnSave.setEnabled(false);

        bottomBox.add(btnBub);
        bottomBox.add(btnIns);
        bottomBox.add(btnMrg);
        bottomBox.add(btnSave);

        // Listeners
        btnLoad.addActionListener(e -> loadCustomFile());
        btnGen.addActionListener(e -> generateCustomData());
        btnView.addActionListener(e -> viewCurrentFile()); 
        
        btnBub.addActionListener(e -> { updateInfoPanel("Bubble"); runBenchmarkSort(1); });
        btnIns.addActionListener(e -> { updateInfoPanel("Insertion"); runBenchmarkSort(2); });
        btnMrg.addActionListener(e -> { updateInfoPanel("Merge"); runBenchmarkSort(3); });
        btnSave.addActionListener(e -> saveResult());

        panel.add(topBox, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(bottomBox, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    //       VIEW FILE (UPDATED)
    // ==========================================
    private void viewCurrentFile() {
        if (actualFileRef == null || !actualFileRef.exists()) {
            JOptionPane.showMessageDialog(this, "No file loaded!");
            return;
        }

        JDialog viewDialog = new JDialog(this, "Viewing: " + actualFileRef.getName(), true);
        viewDialog.setSize(600, 500);
        viewDialog.setLocationRelativeTo(this);
        viewDialog.setLayout(new BorderLayout());

        JTextArea contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        contentArea.setBackground(new Color(40, 42, 48));
        contentArea.setForeground(Color.WHITE);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Read file content using the actualFileRef
        try {
            Scanner sc = new Scanner(actualFileRef);
            StringBuilder sb = new StringBuilder();
            int count = 0;
            while (sc.hasNext()) {
                sb.append(sc.next()).append(" ");
                count++;
                if (count > 2000) { 
                    sb.append("\n\n[... Display limited to first 2000 items ...]");
                    break; 
                }
            }
            sc.close();
            contentArea.setText(sb.toString());
        } catch (Exception e) {
            contentArea.setText("Error reading file.");
        }

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);

        JLabel lblTitle = new JLabel("  Raw Data Preview (Showing " + actualFileRef.getName() + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(COL_TEXT_MAIN);
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(COL_BG_PANEL);

        viewDialog.add(lblTitle, BorderLayout.NORTH);
        viewDialog.add(scroll, BorderLayout.CENTER);
        viewDialog.setVisible(true);
    }

    // ==========================================
    //       TAB 2: VISUALIZER
    // ==========================================
    private JPanel createVisualizerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COL_BG_MAIN);

        visualPanel = new VisualPanel();
        resetVisualData();

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COL_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Control Panel");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COL_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea tutorial = new JTextArea(
            "GUIDE:\n\n" +
            "1. Shuffle: Mixes the numbers.\n" +
            "2. Speed: Drag left to slow down.\n" +
            "3. Choose an Algorithm to watch.\n"
        );
        tutorial.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tutorial.setForeground(COL_TEXT_SUB);
        tutorial.setBackground(COL_SIDEBAR);
        tutorial.setEditable(false);
        tutorial.setAlignmentX(Component.LEFT_ALIGNMENT);
        tutorial.setBorder(new EmptyBorder(15, 0, 15, 0));

        ModernButton btnShuffle = new ModernButton("Shuffle Data", COL_DANGER, Color.WHITE);
        
        lblSpeedValue = new JLabel("Animation Speed: 50%");
        lblSpeedValue.setForeground(COL_TEXT_MAIN);
        lblSpeedValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JSlider speedSlider = new JSlider(0, 100, 50);
        speedSlider.setBackground(COL_SIDEBAR);
        speedSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedSlider.addChangeListener(e -> {
            int val = speedSlider.getValue();
            animationDelay = 105 - val;
            lblSpeedValue.setText("Animation Speed: " + val + "%");
        });

        ModernButton btnRunBub = new ModernButton("Play Bubble Sort", COL_ACCENT, Color.WHITE);
        ModernButton btnRunIns = new ModernButton("Play Insertion Sort", COL_ACCENT, Color.WHITE);
        ModernButton btnRunMrg = new ModernButton("Play Merge Sort", COL_ACCENT, Color.WHITE);

        lblVisualStatus = new JLabel("Status: Ready");
        lblVisualStatus.setForeground(COL_SUCCESS);
        lblVisualStatus.setFont(new Font("Consolas", Font.BOLD, 12));
        lblVisualStatus.setBorder(new EmptyBorder(20, 0, 0, 0));

        lblVisualTime = new JLabel("Time: 0.0s");
        lblVisualTime.setForeground(Color.YELLOW);
        lblVisualTime.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVisualTime.setBorder(new EmptyBorder(10, 0, 0, 0));

        sidebar.add(lblTitle);
        sidebar.add(tutorial);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnShuffle);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(lblSpeedValue); 
        sidebar.add(speedSlider);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(btnRunBub);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnRunIns);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnRunMrg);
        sidebar.add(lblVisualStatus);
        sidebar.add(lblVisualTime); 
        
        btnShuffle.addActionListener(e -> { 
            resetVisualData(); 
            lblVisualStatus.setText("Status: Shuffled");
            lblVisualTime.setText("Time: 0.0s");
        });
        btnRunBub.addActionListener(e -> runVisualSort(1));
        btnRunIns.addActionListener(e -> runVisualSort(2));
        btnRunMrg.addActionListener(e -> runVisualSort(3));

        panel.add(visualPanel, BorderLayout.CENTER);
        panel.add(sidebar, BorderLayout.EAST);
        return panel;
    }

    // ==========================================
    //       LOGIC
    // ==========================================
    
    private void updateInfoPanel(String algo) {
        String css = "<style>"
                + "body { font-family: Segoe UI, sans-serif; color: #E0E0E0; background-color: #303238; font-size: 13px; }"
                + "h2 { color: #6495ED; margin-bottom: 5px; }"
                + "b { color: #4BD143; }" 
                + "span.bad { color: #FF453A; }"
                + "p { margin-bottom: 10px; line-height: 1.4; }"
                + "li { margin-bottom: 8px; }"
                + "</style>";

        String content = "";
        if (algo.equals("Bubble")) {
            content = "<h2>Bubble Sort</h2>"
                    + "<p>Imagine air bubbles rising to the top of water. The largest numbers 'bubble' to the end of the list one by one.</p>"
                    + "<ul>"
                    + "<li><b>Speed Rating:</b> <span class='bad'>Slow (O(n2))</span></li>"
                    + "<li><b>Good for:</b> Teaching how sorting works.</li>"
                    + "<li><b>Bad for:</b> Real apps or large files.</li>"
                    + "</ul>";
        } else if (algo.equals("Insertion")) {
            content = "<h2>Insertion Sort</h2>"
                    + "<p>Like sorting a hand of playing cards. You pick up one card and slide it into the perfect spot among the cards you already hold.</p>"
                    + "<ul>"
                    + "<li><b>Speed Rating:</b> <span class='bad'>Slow (O(n2))</span></li>"
                    + "<li><b>Good for:</b> Small lists or lists that are <i>almost</i> sorted.</li>"
                    + "</ul>";
        } else if (algo.equals("Merge")) {
            content = "<h2>Merge Sort</h2>"
                    + "<p>The 'Divide and Conquer' method. It splits a huge list into tiny single items, then intelligently zips them back together in order.</p>"
                    + "<ul>"
                    + "<li><b>Speed Rating:</b> <b>Fast (O(n log n))</b></li>"
                    + "<li><b>Good for:</b> Huge files (millions of items).</li>"
                    + "<li><b>Trade-off:</b> Uses a bit more memory (RAM).</li>"
                    + "</ul>";
        } else {
            content = "<p style='padding-top:20px; color:#888;'>Click a sort button to learn how it works.</p>";
        }
        infoPanel.setText("<html>" + css + "<body>" + content + "</body></html>");
    }

    private void runBenchmarkSort(int type) {
        if (benchmarkData == null) { log("No data loaded."); return; }
        if (benchmarkData.length > 50000 && type < 3) {
            int opt = JOptionPane.showConfirmDialog(this, "Sorting huge data with this algorithm is slow.\nContinue?", "Warning", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                lblTimeResult.setText("Working...");
                lblTimeResult.setForeground(COL_ACCENT);
                btnSave.setEnabled(false);
            });

            int[] dataCopy = benchmarkData.clone();
            long start = System.nanoTime();
            if (type == 1) bubbleSort(dataCopy);
            else if (type == 2) insertionSort(dataCopy);
            else if (type == 3) mergeSort(dataCopy, 0, dataCopy.length - 1);
            long end = System.nanoTime();
            double seconds = (end - start) / 1_000_000_000.0;

            SwingUtilities.invokeLater(() -> {
                sortedData = dataCopy;
                String timeStr = String.format("%.4f s", seconds);
                lblTimeResult.setText(timeStr);
                lblTimeResult.setForeground(COL_SUCCESS);
                log("Finished: " + timeStr);
                btnSave.setEnabled(true);
            });
        }).start();
    }

    private void generateCustomData() {
        String input = JOptionPane.showInputDialog(this, "How many items? (e.g. 100000):");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int count = Integer.parseInt(input.trim());
                new Thread(() -> {
                    log("Generating " + count + " items...");
                    createFile("dataset_custom.txt", count);
                    SwingUtilities.invokeLater(() -> {
                        currentFilename = "dataset_custom.txt";
                        loadBenchmarkData(currentFilename);
                    });
                }).start();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Invalid Number"); }
        }
    }

    private void loadCustomFile() {
        String filename = JOptionPane.showInputDialog(this, "Filename:", currentFilename);
        if (filename != null) { 
            currentFilename = filename; 
            loadBenchmarkData(filename); 
        }
    }

    // --- SMART FILE LOADING ---
    private void loadBenchmarkData(String filename) {
        try {
            // 1. Try default location
            File f = new File(filename);
            
            // 2. SMART SEARCH: If not in default location, search project recursively
            if (!f.exists()) {
                log("Searching for '" + filename + "' in project...");
                File currentDir = new File(System.getProperty("user.dir"));
                f = findFileRecursive(currentDir, filename);
            }

            // 3. Final check
            if (f == null || !f.exists()) {
                log("File not found: " + filename);
                lblCurrentFile.setText("No File Loaded");
                lblCurrentFile.setForeground(COL_DANGER);
                toggleSortButtons(false);
                return;
            }

            // Store the actual file reference (important for Viewing later)
            this.actualFileRef = f;

            Scanner sc = new Scanner(f);
            ArrayList<Integer> list = new ArrayList<>();
            while (sc.hasNext()) { if(sc.hasNextInt()) list.add(sc.nextInt()); else sc.next(); }
            sc.close();
            
            benchmarkData = new int[list.size()];
            for (int i=0; i<list.size(); i++) benchmarkData[i] = list.get(i);
            
            // Log success with full path info if it was found elsewhere
            if (!f.getName().equals(filename) || !f.getParent().equals(System.getProperty("user.dir"))) {
                 log("Found at: " + f.getAbsolutePath());
            } else {
                 log("Loaded " + filename + " (" + benchmarkData.length + " items)");
            }
            
            lblCurrentFile.setText("File: " + f.getName());
            lblCurrentFile.setForeground(COL_ACCENT);
            toggleSortButtons(true);
            lblTimeResult.setText("Ready");
            
        } catch (Exception e) { log("Error loading file."); }
    }

    // --- RECURSIVE HELPER ---
    private File findFileRecursive(File directory, String filename) {
        File target = new File(directory, filename);
        if (target.exists()) return target;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && !f.getName().startsWith(".")) { // Skip hidden folders
                    File found = findFileRecursive(f, filename);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }
    // ------------------------

    private void toggleSortButtons(boolean enabled) {
        btnBub.setEnabled(enabled); btnIns.setEnabled(enabled); btnMrg.setEnabled(enabled);
    }

    private void createFile(String name, int count) {
        try {
            List<Integer> nums = new ArrayList<>();
            for(int i=1; i<=count; i++) nums.add(i);
            Collections.shuffle(nums);
            // Optional: Save file to where the previous one was found, otherwise default root
            File saveLoc = (actualFileRef != null) ? new File(actualFileRef.getParent(), name) : new File(name);
            FileWriter w = new FileWriter(saveLoc);
            for(int n : nums) w.write(n + " ");
            w.close();
        } catch(Exception e) {}
    }

    private void saveResult() {
        if(sortedData == null) return;
        try {
            String name = "sorted_numbers.txt";
            int c = 2; 
            // Try to save next to the original file
            File dir = (actualFileRef != null) ? actualFileRef.getParentFile() : new File(".");
            File f = new File(dir, name);
            
            while(f.exists()) { name = "sorted_numbers_" + c + ".txt"; f = new File(dir, name); c++; }
            
            FileWriter w = new FileWriter(f);
            for(int n : sortedData) w.write(n + " "); w.close();
            JOptionPane.showMessageDialog(this, "Saved to: " + f.getAbsolutePath());
        } catch(Exception e) { log("Save failed."); }
    }

    private void log(String s) { benchLog.append(s + "\n"); }

    private void resetVisualData() {
        if (isVisualRunning) return;
        visualData = new int[VISUAL_SIZE];
        Random r = new Random();
        for (int i=0; i<VISUAL_SIZE; i++) visualData[i] = r.nextInt(400) + 20;
        visualPanel.repaint();
    }

    private void runVisualSort(int type) {
        if (isVisualRunning) return;
        isVisualRunning = true;
        visualStartTime = System.currentTimeMillis(); 
        visualStopwatch.start(); 
        
        new Thread(() -> {
            try {
                if (type==1) { // Bubble
                    for (int i=0; i<visualData.length-1; i++) {
                        for (int j=0; j<visualData.length-i-1; j++) {
                            visualPanel.activeIdx = j+1;
                            setStatus("Bubble: Comparing " + j + " & " + (j+1));
                            if (visualData[j] > visualData[j+1]) {
                                int t=visualData[j]; visualData[j]=visualData[j+1]; visualData[j+1]=t;
                            }
                            updateVis();
                        }
                    }
                } else if (type==2) { // Insertion
                    for (int i=1; i<visualData.length; i++) {
                        int k=visualData[i]; int j=i-1;
                        visualPanel.activeIdx = i;
                        setStatus("Insertion: Placing " + k);
                        while(j>=0 && visualData[j]>k) { visualData[j+1]=visualData[j]; j--; updateVis(); }
                        visualData[j+1]=k; updateVis();
                    }
                } else if (type==3) { // Merge
                    mergeSortVisual(0, visualData.length-1);
                }
            } finally {
                visualPanel.activeIdx = -1; 
                setStatus("Sort Complete!");
                visualPanel.repaint(); 
                isVisualRunning = false;
                visualStopwatch.stop(); 
            }
        }).start();
    }

    private void mergeSortVisual(int l, int r) {
        if (l < r) {
            int m = l + (r - l) / 2;
            mergeSortVisual(l, m);
            mergeSortVisual(m + 1, r);
            mergeVisual(l, m, r);
        }
    }

    private void mergeVisual(int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;
        int[] L = new int[n1];
        int[] R = new int[n2];
        for(int i=0; i<n1; ++i) L[i] = visualData[l + i];
        for(int j=0; j<n2; ++j) R[j] = visualData[m + 1 + j];

        int i=0, j=0, k=l;
        while(i<n1 && j<n2) {
            visualPanel.activeIdx = k;
            setStatus("Merging index " + k);
            if(L[i] <= R[j]) { visualData[k] = L[i]; i++; } else { visualData[k] = R[j]; j++; }
            k++;
            updateVis();
        }
        while(i<n1) { visualPanel.activeIdx = k; visualData[k] = L[i]; i++; k++; updateVis(); }
        while(j<n2) { visualPanel.activeIdx = k; visualData[k] = R[j]; j++; k++; updateVis(); }
    }
    
    private void setStatus(String msg) { SwingUtilities.invokeLater(() -> lblVisualStatus.setText(msg)); }
    private void updateVis() { visualPanel.repaint(); try { Thread.sleep(animationDelay); } catch(Exception e) {} }

    class VisualPanel extends JPanel {
        int activeIdx = -1;
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(visualData == null) return;
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COL_BG_MAIN); g2.fillRect(0,0,getWidth(),getHeight());
            int w = getWidth()/VISUAL_SIZE;
            for(int i=0; i<visualData.length; i++) {
                int h = visualData[i];
                g2.setColor(i==activeIdx ? COL_BAR_ACTIVE : COL_BAR_DEF);
                g2.fillRoundRect(i*w + 1, getHeight()-h, w-2, h, 4, 4);
            }
        }
    }

    public void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i=0; i<n-1; i++) 
            for (int j=0; j<n-i-1; j++) 
                if (arr[j] > arr[j+1]) { int t=arr[j]; arr[j]=arr[j+1]; arr[j+1]=t; }
    }
    public void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i=1; i<n; ++i) {
            int k=arr[i]; int j=i-1;
            while (j>=0 && arr[j]>k) { arr[j+1]=arr[j]; j--; }
            arr[j+1]=k;
        }
    }
    public void mergeSort(int[] arr, int l, int r) {
        if (l < r) { int m = l+(r-l)/2; mergeSort(arr, l, m); mergeSort(arr, m+1, r); merge(arr, l, m, r); }
    }
    public void merge(int[] arr, int l, int m, int r) {
        int n1=m-l+1, n2=r-m; 
        int[] L=new int[n1], R=new int[n2];
        for(int i=0; i<n1; ++i) L[i]=arr[l+i]; for(int j=0; j<n2; ++j) R[j]=arr[m+1+j];
        int i=0, j=0, k=l;
        while(i<n1 && j<n2) { if(L[i]<=R[j]) arr[k++]=L[i++]; else arr[k++]=R[j++]; }
        while(i<n1) arr[k++]=L[i++]; while(j<n2) arr[k++]=R[j++];
    }

    class ModernButton extends JButton {
        private Color bgColor, txtColor;
        public ModernButton(String text, Color bg, Color txt) {
            super(text); this.bgColor = bg; this.txtColor = txt;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(txt); setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(bgColor.brighter()); }
                public void mouseExited(MouseEvent e) { setBackground(bgColor); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bgColor.brighter() : bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SortMasterFinal().setVisible(true));
    }
}