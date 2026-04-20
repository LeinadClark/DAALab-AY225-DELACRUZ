import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class SortMasterExam extends JFrame {

    // ==========================================
    // INNER CLASS: Person
    // ==========================================
    private static class Person {
        int id;
        String firstName;
        String lastName;

        public Person(int id, String fName, String lName) {
            this.id = id;
            this.firstName = fName;
            this.lastName = lName;
        }

        @Override
        public String toString() {
            return String.format("%d %s %s", id, firstName, lastName);
        }
    }

    // --- THEME PALETTE ---
    private final Color COL_BG        = new Color(30, 30, 35);
    private final Color COL_PANEL     = new Color(40, 40, 45);
    private final Color COL_ACCENT    = new Color(50, 150, 255); // Nice Blue
    private final Color COL_BTN_MAIN  = new Color(60, 60, 65);
    private final Color COL_HEADER    = new Color(50, 50, 55);
    private final Color COL_TEXT      = new Color(240, 240, 240);
    private final Color COL_SUCCESS   = new Color(46, 204, 113); // Emerald
    private final Color COL_WARNING   = new Color(241, 196, 15); // Yellow

    // --- DATA STATE ---
    private List<Person> masterData = new ArrayList<>();
    private Person[] workingData = null;
    private File currentFile = null;

    // --- COMPONENTS ---
    private JTextArea logArea;
    private JLabel lblStatus;
    private JTable previewTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboN, comboSortBy, comboPreviewCount;
    private ActionButton btnBub, btnIns, btnMrg, btnAutoBench;
    private JButton btnPrevPage, btnNextPage, btnExportSlice;
    private JLabel lblPageInfo;
    private int currentPage = 1;
    private double lastLoadSeconds = 0.0;

    // Progress tracking
    private volatile int progressCounter = 0;
    private volatile int progressTotal = 1;
    private volatile int progressNextPct = 0;

    private static final Dimension MIN_APP_SIZE = new Dimension(1100, 700);

    public SortMasterExam() {
        setTitle("Design & Analysis of Algorithms: Prelim Exam Tool");
        setSize(1200, 800);
        setMinimumSize(MIN_APP_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COL_BG);
        setLayout(new BorderLayout(15, 15));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension min = MIN_APP_SIZE;
                int w = Math.max(min.width, getWidth());
                int h = Math.max(min.height, getHeight());
                if (w != getWidth() || h != getHeight()) setSize(w, h);
                SwingUtilities.invokeLater(() -> {
                    adjustTableColumns();
                    revalidate();
                    repaint();
                });
            }
        });

        // Layout Structure
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);

        // ============================================================
        // FIX: Run Smart Scan on startup to find the file automatically
        // ============================================================
        smartFileScan("generated_data.csv");
    }

    // ================== GUI SECTIONS ==================

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COL_PANEL);
        p.setBorder(new EmptyBorder(20, 30, 30, 30));

        JLabel title = new JLabel("Sorting Algorithm Stress Test");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COL_ACCENT);
        title.setPreferredSize(new Dimension(560, title.getPreferredSize().height));
        title.setMaximumSize(new Dimension(580, title.getPreferredSize().height));

        JLabel subtitle = new JLabel("Prelim Exam • Data Structures & Algorithms");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setPreferredSize(new Dimension(560, subtitle.getPreferredSize().height));
        subtitle.setMaximumSize(new Dimension(580, subtitle.getPreferredSize().height));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        // File Controls
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filePanel.setOpaque(false);
        filePanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        filePanel.setMinimumSize(new Dimension(220, 40));

        ActionButton btnGen = new ActionButton("Generate CSV", COL_BTN_MAIN);
        ActionButton btnLoad = new ActionButton("Load CSV", COL_BTN_MAIN);

        btnGen.addActionListener(e -> generateDummyCSV());
        btnLoad.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            
            // Smart directory start
            File startDir = new File(".");
            if (currentFile != null && currentFile.getParentFile() != null && currentFile.getParentFile().exists()) {
                startDir = currentFile.getParentFile();
            } else {
                // Try to guess data folder
                File dataDir = new File("data");
                if (dataDir.exists()) startDir = dataDir;
            }
            fc.setCurrentDirectory(startDir);
            
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                loadData();
            }
        });

        filePanel.add(btnGen);
        filePanel.add(btnLoad);

        p.add(textPanel, BorderLayout.WEST);
        p.add(filePanel, BorderLayout.EAST);
        return p;
    }

    private JScrollPane previewScrollPane;
    private JPanel createMainContent() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COL_BG);
        p.setBorder(new EmptyBorder(28, 30, 0, 30));

        // LEFT: Configuration & Logs
        JPanel left = new JPanel(new BorderLayout(0, 15));
        left.setOpaque(true); left.setBackground(COL_BG);

        // Step 1: Config Box
        JPanel configBox = createSectionPanel("1. Configuration");
        configBox.setLayout(new GridLayout(2, 2, 10, 10));
        configBox.setPreferredSize(new Dimension(0, 120));
        configBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        configBox.add(createLabel("Rows to Sort (N):"));
        comboN = new JComboBox<>(new String[]{"1000", "10000", "50000", "100000"});
        comboN.setEditable(true);
        comboN.setPreferredSize(new Dimension(120, 24));
        Component editorComp = comboN.getEditor().getEditorComponent();
        if (editorComp instanceof javax.swing.JTextField) {
            ((javax.swing.JTextField) editorComp).addActionListener(e -> {
                String v = ((javax.swing.JTextField) editorComp).getText();
                comboN.setSelectedItem(v);
                currentPage = 1;
                if (workingData != null) updateTable(workingData);
                else if (!masterData.isEmpty()) updateTable(masterData.toArray(new Person[0]));
            });
        }
        configBox.add(comboN);

        configBox.add(createLabel("Sort Column:"));
        comboSortBy = new JComboBox<>(new String[]{"ID", "FirstName", "LastName"});
        configBox.add(comboSortBy);

        // Step 2: Logs
        JPanel logBox = createSectionPanel("2. Benchmark Logs");
        logBox.setLayout(new BorderLayout());
        logBox.setPreferredSize(new Dimension(0, 420));
        logBox.setMinimumSize(new Dimension(0, 300));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(35, 35, 40));
        logArea.setForeground(COL_SUCCESS);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(6,6,6,6));

        JScrollPane logSp = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logSp.setOpaque(true); logSp.setBackground(new Color(35,35,40)); logSp.getViewport().setBackground(new Color(35,35,40));
        logSp.setBorder(null);
        JScrollBar lvsb = logSp.getVerticalScrollBar(); if (lvsb != null) { lvsb.setUI(new DarkScrollBarUI()); lvsb.setUnitIncrement(16); lvsb.setBackground(new Color(35,35,40)); lvsb.setOpaque(true); }

        logBox.add(logSp, BorderLayout.CENTER);

        left.add(configBox, BorderLayout.NORTH);
        left.add(logBox, BorderLayout.CENTER);

        // RIGHT: Data Preview
        JPanel right = createSectionPanel("3. Data Verification (Preview)");
        right.setLayout(new BorderLayout());

        String[] cols = {"ID", "First Name", "Last Name"};
        tableModel = new DefaultTableModel(cols, 0);
        previewTable = new JTable(tableModel);
        previewTable.setRowHeight(28);
        previewTable.setFont(new Font("Consolas", Font.PLAIN, 13));
        previewTable.setShowGrid(false);
        previewTable.setFillsViewportHeight(true);
        previewTable.setIntercellSpacing(new Dimension(0, 0));
        previewTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        previewTable.getTableHeader().setBackground(COL_HEADER);
        previewTable.getTableHeader().setForeground(COL_TEXT);
        previewTable.getTableHeader().setOpaque(true);
        previewTable.getTableHeader().setBorder(new EmptyBorder(8, 8, 8, 8));

        previewTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground((row % 2 == 0) ? new Color(45, 45, 50) : new Color(38, 38, 42));
                c.setForeground(COL_TEXT);
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });

        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        previewTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        previewTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        previewTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        comboPreviewCount = new JComboBox<>(new String[]{"10","25","50","100"});
        comboPreviewCount.setSelectedIndex(0);
        comboPreviewCount.setPreferredSize(new Dimension(70, 24));
        comboPreviewCount.addActionListener(e -> {
            currentPage = 1;
            if (workingData != null) { updateTable(workingData); }
            else if (!masterData.isEmpty()) { updateTable(masterData.toArray(new Person[0])); }
        });

        btnPrevPage = new JButton("< Prev");
        btnPrevPage.setEnabled(false);
        btnPrevPage.setPreferredSize(new Dimension(90, 28));

        btnNextPage = new JButton("Next >");
        btnNextPage.setEnabled(false);
        btnNextPage.setPreferredSize(new Dimension(90, 28));

        lblPageInfo = new JLabel("Page 0 / 0"); lblPageInfo.setForeground(Color.LIGHT_GRAY);
        lblPageInfo.setPreferredSize(new Dimension(120, 24));
        lblPageInfo.setHorizontalAlignment(SwingConstants.CENTER);

        // --- FULL EXPORT LOGIC RESTORED ---
        btnExportSlice = new JButton("Export CSV");
        btnExportSlice.addActionListener(ev -> {
            int n = getRequestedRowCount(); if (n == -1) return;
            final Person[] dataToExport = (workingData != null) ? workingData : getSlice(n);
            
            // Checkbox options
            JCheckBox chkHeader = new JCheckBox("Include header row (ID,FirstName,LastName)", true);
            JCheckBox chkRenumber = new JCheckBox("Renumber IDs starting at 1", false);
            JPanel opt = new JPanel(new GridLayout(0,1)); opt.add(chkHeader); opt.add(chkRenumber);
            
            int ok = JOptionPane.showConfirmDialog(this, opt, "Export Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save Exported CSV");
            File defaultDir = (currentFile != null && currentFile.getParentFile() != null) ? currentFile.getParentFile() : new File(System.getProperty("user.dir"));
            fc.setCurrentDirectory(defaultDir);
            String df = "export_slice_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
            fc.setSelectedFile(new File(defaultDir, df));
            int res = fc.showSaveDialog(this);
            if (res != JFileChooser.APPROVE_OPTION) return;
            File out = fc.getSelectedFile();
            if (out.exists()) { int o = JOptionPane.showConfirmDialog(this, "File exists. Overwrite?", "Confirm", JOptionPane.YES_NO_OPTION); if (o != JOptionPane.YES_OPTION) return; }
            
            final boolean includeHeader = chkHeader.isSelected();
            final boolean renumber = chkRenumber.isSelected();
            
            new Thread(() -> {
                try {
                    exportSlice(dataToExport, out, includeHeader, renumber);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Exported " + dataToExport.length + " rows to " + out.getName()));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error exporting: " + ex.getMessage()));
                }
            }).start();
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JPanel centerBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        centerBar.setOpaque(false);
        JLabel lblPreviewRows = new JLabel("Preview rows:");
        lblPreviewRows.setForeground(COL_TEXT);
        lblPreviewRows.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        centerBar.add(lblPreviewRows);
        centerBar.add(comboPreviewCount);

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pager.setOpaque(false);
        pager.add(btnPrevPage);
        pager.add(lblPageInfo);
        pager.add(btnNextPage);

        btnPrevPage.addActionListener(e -> {
            Person[] data = (workingData != null) ? workingData : masterData.toArray(new Person[0]);
            int pageCount = getPageCount(data.length);
            if (currentPage > 1) {
                currentPage = Math.max(1, currentPage - 1);
                updateTable(data);
            }
        });
        btnNextPage.addActionListener(e -> {
            Person[] data = (workingData != null) ? workingData : masterData.toArray(new Person[0]);
            int pageCount = getPageCount(data.length);
            if (currentPage < pageCount) {
                currentPage = Math.min(pageCount, currentPage + 1);
                updateTable(data);
            }
        });

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBar.setOpaque(false);
        rightBar.setMinimumSize(new Dimension(240, 36));
        rightBar.add(btnExportSlice);

        topBar.add(centerBar, BorderLayout.CENTER);
        topBar.add(rightBar, BorderLayout.EAST);

        right.add(topBar, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(previewTable);
        previewScrollPane = sp;
        sp.setOpaque(true);
        sp.setBackground(new Color(40,40,45));
        sp.getViewport().setBackground(new Color(40,40,45));
        sp.setViewportBorder(new EmptyBorder(8, 8, 8, 8));
        sp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(50,50,55)), new EmptyBorder(0,0,0,0)));
        JScrollBar vsb = sp.getVerticalScrollBar(); if (vsb != null) { vsb.setBackground(new Color(40,40,45)); vsb.setOpaque(true); vsb.setUnitIncrement(16); vsb.setUI(new DarkScrollBarUI()); }
        JScrollBar hsb = sp.getHorizontalScrollBar(); if (hsb != null) { hsb.setBackground(new Color(40,40,45)); hsb.setOpaque(true); hsb.setUnitIncrement(16); hsb.setUI(new DarkScrollBarUI()); }
        previewTable.setBackground(new Color(40,40,45));
        previewTable.setOpaque(true);
        previewTable.getTableHeader().setBorder(new EmptyBorder(10, 8, 10, 8));
        right.add(sp, BorderLayout.CENTER);

        JPanel pagerBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        pagerBar.setOpaque(false);
        pagerBar.add(btnPrevPage);
        pagerBar.add(lblPageInfo);
        pagerBar.add(btnNextPage);
        right.add(pagerBar, BorderLayout.SOUTH);

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 20, 0));
        twoCol.setOpaque(false);
        twoCol.add(left);
        twoCol.add(right);
        p.add(twoCol, BorderLayout.CENTER);

        return p;
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COL_PANEL);
        p.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel centerButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        centerButtons.setOpaque(false);
        btnBub = new ActionButton("Run Bubble Sort", COL_BTN_MAIN);
        btnIns = new ActionButton("Run Insertion Sort", COL_BTN_MAIN);
        btnMrg = new ActionButton("Run Merge Sort", COL_ACCENT);
        btnAutoBench = new ActionButton("Auto Benchmark", new Color(39, 174, 96));
        centerButtons.add(btnBub);
        centerButtons.add(btnIns);
        centerButtons.add(btnMrg);
        centerButtons.add(btnAutoBench);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        p.add(centerButtons, BorderLayout.CENTER);
        p.add(rightPanel, BorderLayout.EAST);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 10, 6, 10));
        lblStatus = new JLabel("Ready");
        lblStatus.setForeground(Color.LIGHT_GRAY);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.add(lblStatus, BorderLayout.WEST);

        JLabel lblCopyright = new JLabel(String.format("© %d Leinad Clark. All rights reserved.", java.time.Year.now().getValue()));
        lblCopyright.setForeground(new Color(180, 180, 180));
        lblCopyright.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel cPanel = new JPanel(new GridBagLayout());
        cPanel.setOpaque(false);
        cPanel.add(lblCopyright);
        footer.add(cPanel, BorderLayout.CENTER);

        p.add(footer, BorderLayout.SOUTH);

        btnBub.addActionListener(e -> runBenchmark("Bubble"));
        btnIns.addActionListener(e -> runBenchmark("Insertion"));
        btnMrg.addActionListener(e -> runBenchmark("Merge"));
        btnAutoBench.addActionListener(e -> runAutoBenchmark());

        return p;
    }

    // ================== LOGIC: BENCHMARK ==================

    private void runBenchmark(String type) {
        // Prevent running if data isn't loaded
        if (masterData.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Data not loaded.\nPlease ensure 'generated_data.csv' is in the 'data' folder or use Load CSV."); 
            return; 
        }

        int requested = getRequestedRowCount(); if (requested == -1) return;
        prepareSlice();
        if (workingData == null) return;

        int actual = workingData.length;
        if (requested > actual) {
            int opt = JOptionPane.showConfirmDialog(this, String.format("Requested %d rows but only %d available. Proceed sorting %d rows?", requested, actual, actual), "Rows Limited", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        if (actual >= 50000 && (type.equals("Bubble") || type.equals("Insertion"))) {
            SwingUtilities.invokeLater(() -> { lblStatus.setText("Warning: Large N & O(n²) algorithm"); lblStatus.setForeground(COL_WARNING); });
            int opt = JOptionPane.showConfirmDialog(this, "Sorting " + actual + " rows with O(n²) algorithms is very slow.\nProceed?", "Stress Test Warning", JOptionPane.YES_NO_OPTION);
            SwingUtilities.invokeLater(() -> { lblStatus.setText("Ready"); lblStatus.setForeground(Color.GRAY); });
            if (opt != JOptionPane.YES_OPTION) return;
        }

        Comparator<Person> comp = getComparator();

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                lblStatus.setText("Running " + type + "...");
                toggleButtons(false);
            });

            if (type.equals("Merge")) {
                int logN = (int) Math.ceil(Math.log(Math.max(actual, 2)) / Math.log(2));
                int totalWork = actual * (logN + 1);
                startProgress(totalWork, "Sorting (Merge):");
            }

            long start = System.nanoTime();
            if (type.equals("Bubble")) bubbleSort(workingData, comp);
            else if (type.equals("Insertion")) insertionSort(workingData, comp);
            else if (type.equals("Merge")) mergeSort(workingData, 0, workingData.length - 1, comp);
            long end = System.nanoTime();
            double sec = (end - start) / 1_000_000_000.0;

            SwingUtilities.invokeLater(() -> {
                updateTable(workingData);
                double total = sec + lastLoadSeconds;
                lblStatus.setText(String.format("Sort: %.4f s (Total: %.4f s)", sec, total));
                lblStatus.setForeground(COL_ACCENT);
                log(String.format("%s (%d rows): Sort=%.4f s | Load=%.4f s | Total=%.4f s", type, actual, sec, lastLoadSeconds, total));
                toggleButtons(true);
            });
        }).start();
    }

    private void prepareSlice() {
        if (masterData.isEmpty()) { JOptionPane.showMessageDialog(this, "Load data first."); return; }
        int n = getRequestedRowCount(); if (n == -1) return;
        if (n > masterData.size()) n = masterData.size();
        workingData = new Person[n];
        for(int i=0; i<n; i++) workingData[i] = masterData.get(i);
    }

    private Comparator<Person> getComparator() {
        String col = (String) comboSortBy.getSelectedItem();
        return (p1, p2) -> {
            switch (col) {
                case "ID": return Integer.compare(p1.id, p2.id);
                case "FirstName": {
                    int r = p1.firstName.compareToIgnoreCase(p2.firstName);
                    return (r != 0) ? r : Integer.compare(p1.id, p2.id);
                }
                case "LastName": {
                    int r = p1.lastName.compareToIgnoreCase(p2.lastName);
                    return (r != 0) ? r : Integer.compare(p1.id, p2.id);
                }
                default: return 0;
            }
        };
    }

    private int getRequestedRowCount() {
        Object sel = comboN.isEditable() ? comboN.getEditor().getItem() : comboN.getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Please select or enter the number of rows."); return -1; }
        String s = sel.toString().trim();
        try {
            int n = Integer.parseInt(s);
            if (n <= 0) { JOptionPane.showMessageDialog(this, "Number of rows must be positive."); return -1; }
            return n;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer for the number of rows.");
            return -1;
        }
    }

    private Person[] getSlice(int n) {
        if (masterData.isEmpty()) return new Person[0];
        if (n > masterData.size()) n = masterData.size();
        Person[] arr = new Person[n];
        for (int i = 0; i < n; i++) arr[i] = masterData.get(i);
        return arr;
    }

    private int getPageCount(int total) {
        int pageSize = 10;
        try { pageSize = Integer.parseInt((String) comboPreviewCount.getSelectedItem()); } catch(Exception ex) {}
        if (pageSize <= 0) return 0;
        return (total + pageSize - 1) / pageSize;
    }

    private String csvQuote(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private void exportSlice(Person[] data, String filename) throws IOException {
        exportSlice(data, new File(filename), true, false);
    }
    private void exportSlice(Person[] data, File outFile, boolean includeHeader, boolean renumber) throws IOException {
        try (FileWriter fw = new FileWriter(outFile)) {
            if (includeHeader) fw.write("ID,FirstName,LastName\n");
            for (int i = 0; i < data.length; i++) {
                Person p = data[i];
                int id = renumber ? (i + 1) : p.id;
                fw.write(String.format("%d,%s,%s\n", id, csvQuote(p.firstName), csvQuote(p.lastName)));
            }
        }
    }

    private void runAutoBenchmark() {
        if (masterData.isEmpty()) { JOptionPane.showMessageDialog(this, "Load data first."); return; }
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> { toggleButtons(false); btnAutoBench.setEnabled(false); lblStatus.setText("Running Auto Benchmarks..."); });
            int[] Ns = {1000, 10000, 100000};
            String[] algs = {"Bubble", "Insertion", "Merge"};
            List<String> results = new ArrayList<>();

            for (int n : Ns) {
                for (String alg : algs) {
                    if ((alg.equals("Bubble") || alg.equals("Insertion")) && n >= 50000) {
                        log("Skipping " + alg + " for n=" + n + " (too slow)");
                        results.add(alg + "," + n + ",SKIPPED");
                        continue;
                    }
                    Person[] arr = getSlice(n);
                    Comparator<Person> comp = getComparator();
                    Person[] work = Arrays.copyOf(arr, arr.length);
                    log("Running " + alg + " for n=" + n + "...");
                    long s = System.nanoTime();
                    if (alg.equals("Bubble")) bubbleSort(work, comp);
                    else if (alg.equals("Insertion")) insertionSort(work, comp);
                    else if (alg.equals("Merge")) {
                        int n1 = work.length;
                        int est = Math.max(1, (int) (n1 * (Math.ceil(Math.log(Math.max(n1,2)) / Math.log(2)) + 1)));
                        startProgress(est, "Sorting (Merge):");
                        mergeSort(work, 0, work.length - 1, comp);
                    }
                    long e = System.nanoTime();
                    double t = (e - s) / 1_000_000_000.0;
                    log(String.format("%s (%d): %.4f s", alg, n, t));
                    results.add(alg + "," + n + "," + String.format("%.4f", t));
                }
            }

            try (FileWriter fw = new FileWriter("benchmark_results.csv")) {
                fw.write("Algorithm,N,TimeSec\n");
                for (String r : results) fw.write(r + "\n");
            } catch (Exception ex) { log("Error writing benchmark_results.csv: " + ex.getMessage()); }

            SwingUtilities.invokeLater(() -> {
                toggleButtons(true); btnAutoBench.setEnabled(true); lblStatus.setText("Ready");
                try { showBenchmarkChart(results); } catch (Exception ex) { log("Error showing chart: " + ex.getMessage()); }
                JOptionPane.showMessageDialog(this, "Auto-benchmarks complete. Results saved to benchmark_results.csv");
            });
        }).start();
    }

    // ================== ALGORITHMS ==================
    public void bubbleSort(Person[] arr, Comparator<Person> c) {
        int n = arr.length;
        startProgress(n, "Sorting (Bubble):");
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++)
                if (c.compare(arr[j], arr[j + 1]) > 0) swap(arr, j, j + 1);
            advanceProgress(1, "Sorting (Bubble):");
        }
        finishProgress();
    }
    public void insertionSort(Person[] arr, Comparator<Person> c) {
        int n = arr.length;
        startProgress(n, "Sorting (Insertion):");
        for (int i = 1; i < n; ++i) {
            Person k = arr[i]; int j = i - 1;
            while (j >= 0 && c.compare(arr[j], k) > 0) { arr[j + 1] = arr[j]; j--; }
            arr[j + 1] = k;
            advanceProgress(1, "Sorting (Insertion):");
        }
        finishProgress();
    }
    public void mergeSort(Person[] arr, int l, int r, Comparator<Person> c) {
        if (l < r) { int m = l+(r-l)/2; mergeSort(arr, l, m, c); mergeSort(arr, m+1, r, c); merge(arr, l, m, r, c); }
    }
    public void merge(Person[] arr, int l, int m, int r, Comparator<Person> c) {
        int n1=m-l+1, n2=r-m; Person[] L=new Person[n1], R=new Person[n2];
        for(int i=0; i<n1; ++i) L[i]=arr[l+i]; for(int j=0; j<n2; ++j) R[j]=arr[m+1+j];
        int i=0, j=0, k=l;
        while(i<n1 && j<n2) { if(c.compare(L[i], R[j])<=0) arr[k++]=L[i++]; else arr[k++]=R[j++]; }
        while(i<n1) arr[k++]=L[i++]; while(j<n2) arr[k++]=R[j++];
        int merged = r - l + 1;
        advanceProgress(merged, "Sorting (Merge):");
    }
    private void swap(Person[] arr, int i, int j) { Person t = arr[i]; arr[i] = arr[j]; arr[j] = t; }

    // ================== FILE HANDLING (FIXED & ROBUST) ==================

    /**
     * Tries to find the filename in the current directory, the 'data' subdirectory,
     * or the '../data' relative directory.
     */
private void smartFileScan(String filename) {
        log("--- Deep Search Started ---");
        
        // Start searching from the place the program was launched
        File currentDir = new File(System.getProperty("user.dir"));
        File foundFile = null;

        // Search UP to 4 levels of parent directories
        for (int i = 0; i < 4; i++) {
            if (currentDir == null) break;

            log("Scanning: " + currentDir.getAbsolutePath());

            // 1. Check if file is directly in this folder
            File tryDirect = new File(currentDir, filename);
            if (tryDirect.exists()) { foundFile = tryDirect; break; }

            // 2. Check if file is in a "data" subfolder here
            File tryData = new File(currentDir, "data/" + filename);
            if (tryData.exists()) { foundFile = tryData; break; }
            
            // 3. Check specific "PRELIM-EXAM/data" path
            File tryPrelim = new File(currentDir, "PRELIM-EXAM/data/" + filename);
            if (tryPrelim.exists()) { foundFile = tryPrelim; break; }

            // Move up one directory level
            currentDir = currentDir.getParentFile();
        }

        if (foundFile != null) {
            log("SUCCESS: Found file at: " + foundFile.getAbsolutePath());
            currentFile = foundFile;
            loadData();
        } else {
            log("FAILED: Could not find '" + filename + "' nearby.");
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, 
                "Could not auto-locate '" + filename + "'.\n" +
                "The app searched 4 levels of folders but couldn't find it.\n" +
                "Please make sure you downloaded the 'data' folder.",
                "File Not Found", JOptionPane.WARNING_MESSAGE));
        }
    }
    private void loadData() {
        if (currentFile == null || !currentFile.exists()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "No CSV file selected or file does not exist. Use Load CSV to pick a file."));
            return;
        }
        new Thread(() -> {
            log("Loading data from " + currentFile.getName() + "...");
            masterData.clear();
            long start = System.nanoTime();
            try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
                br.readLine(); 
                String line;
                while ((line = br.readLine()) != null) {
                    List<String> p = parseCSVLine(line);
                    if (p.size() >= 3) {
                        String idStr = p.get(0).trim();
                        String fName = p.get(1).trim();
                        String lName = p.get(2).trim();
                        try { masterData.add(new Person(Integer.parseInt(idStr.trim()), fName, lName)); } catch (NumberFormatException nfe) { /* skip bad row */ }
                    }
                }
                long end = System.nanoTime();
                double sec = (end - start) / 1_000_000_000.0;
                lastLoadSeconds = sec;
                SwingUtilities.invokeLater(() -> {
                    log(String.format("Loaded %d records in %.4f s.", masterData.size(), sec));
                    lblStatus.setText(String.format("Load: %.4f s", sec));
                    lblStatus.setForeground(COL_ACCENT);
                    currentPage = 1;
                    updateTable(masterData.toArray(new Person[0]));
                });
            } catch (Exception e) { log("Error: " + e.getMessage()); SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage())); }
        }).start();
    }

    private void generateDummyCSV() {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> log("Preparing to generate CSV..."));
            try {
                String s = (String) JOptionPane.showInputDialog(this, "How many rows to generate?", "Rows", JOptionPane.PLAIN_MESSAGE, null, null, "100000");
                if (s == null) { log("Generation cancelled by user."); return; }
                int n = 100000;
                try { n = Integer.parseInt(s.trim()); } catch (Exception ex) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Please enter a valid integer for rows.")); return; }
                if (n <= 0) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Number of rows must be positive.")); return; }
                final int nFinal = n;

                SwingUtilities.invokeLater(() -> log("Generating " + nFinal + " records..."));
                String defaultName = "generated_data_" + nFinal + "_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
                
                // Save to 'data' folder if possible
                File defaultDir = new File("data");
                if (!defaultDir.exists()) defaultDir = new File(".");
                
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save Generated CSV");
                fc.setCurrentDirectory(defaultDir);
                fc.setSelectedFile(new File(defaultDir, defaultName));
                
                int res = fc.showSaveDialog(this);
                if (res != JFileChooser.APPROVE_OPTION) { SwingUtilities.invokeLater(() -> log("Generation cancelled.")); return; }
                File outFile = fc.getSelectedFile();
                
                try (FileWriter fw = new FileWriter(outFile)) {
                    fw.write("ID,FirstName,LastName\n");
                    String[] f = {"John","Jane","Alex","Chris","Katie","Mike"};
                    String[] l = {"Smith","Doe","Johnson","Brown","Davis","Wilson"};
                    Random r = new Random();
                    for (int i = 1; i <= nFinal; i++) fw.write(i + "," + f[r.nextInt(f.length)] + "," + l[r.nextInt(l.length)] + "\n");
                }
                SwingUtilities.invokeLater(() -> log("File generated: " + outFile.getName()));
                currentFile = outFile;
                loadData();
            } catch (Exception e) { SwingUtilities.invokeLater(() -> log("Error generating: " + e.getMessage())); }
        }).start();
    }

    // ================== UTILS ==================

    private void startProgress(int total, String label) {
        progressCounter = 0;
        progressTotal = Math.max(1, total);
        progressNextPct = 1;
        SwingUtilities.invokeLater(() -> { lblStatus.setText(label + " 0%"); lblStatus.setForeground(COL_ACCENT); });
    }
    private void advanceProgress(int delta, String label) {
        if (delta <= 0) return;
        progressCounter += delta;
        int pct = (int) ((progressCounter * 100L) / progressTotal);
        if (pct >= progressNextPct) {
            progressNextPct = pct + 1;
            SwingUtilities.invokeLater(() -> { lblStatus.setText(label + " " + pct + "%"); lblStatus.setForeground(COL_ACCENT); });
        }
    }
    private void finishProgress() {
        SwingUtilities.invokeLater(() -> { lblStatus.setText("Ready"); lblStatus.setForeground(Color.LIGHT_GRAY); });
        progressCounter = 0; progressTotal = 1; progressNextPct = 0;
    }

    private List<String> parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(cur.toString()); cur.setLength(0);
            } else cur.append(c);
        }
        tokens.add(cur.toString());
        return tokens;
    }

    private void updateTable(Person[] data) {
        tableModel.setRowCount(0);
        int pageSize = 10;
        try { pageSize = Integer.parseInt((String) comboPreviewCount.getSelectedItem()); } catch(Exception ex) {}
        int total = data.length;
        int pageCount = getPageCount(total);
        if (currentPage > pageCount) currentPage = pageCount == 0 ? 1 : pageCount;
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        int rows = Math.max(0, end - start);
        Object[][] chunk = new Object[rows][3];
        for (int i = 0; i < rows; i++) {
            Person p1 = data[start + i];
            chunk[i][0] = p1.id; chunk[i][1] = p1.firstName; chunk[i][2] = p1.lastName;
        }
        String[] cols = {"ID","First Name","Last Name"};
        tableModel.setDataVector(chunk, cols);
        
        // --- RESTORED TABLE RENDERER ---
        previewTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground((row % 2 == 0) ? new Color(45, 45, 50) : new Color(38, 38, 42));
                c.setForeground(COL_TEXT);
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });
        
        lblPageInfo.setText(String.format("Page %d / %d", (pageCount == 0 ? 0 : currentPage), pageCount));
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < pageCount);

        adjustTableColumns();
        previewTable.revalidate();
        SwingUtilities.invokeLater(() -> { previewTable.revalidate(); previewTable.repaint(); });
    }
    
    private void adjustTableColumns() {
        if (previewScrollPane == null || previewTable == null) return;
        int w = previewScrollPane.getViewport().getWidth();
        if (w <= 0) return;
        int idCol = 60;
        int padding = 24; 
        int remaining = Math.max(200, w - idCol - padding);
        int col1 = remaining / 2;
        int col2 = remaining - col1;
        try {
            previewTable.getColumnModel().getColumn(0).setPreferredWidth(idCol);
            previewTable.getColumnModel().getColumn(1).setPreferredWidth(col1);
            previewTable.getColumnModel().getColumn(2).setPreferredWidth(col2);
        } catch (Exception ex) {}
    }

    private void toggleButtons(boolean on) { btnBub.setEnabled(on); btnIns.setEnabled(on); btnMrg.setEnabled(on); }
    private void log(String s) { logArea.append(">> " + s + "\n"); }
    
    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(COL_PANEL);
        Border outer = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), 
            title, TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), COL_TEXT);
        p.setBorder(BorderFactory.createCompoundBorder(outer, new EmptyBorder(18, 12, 12, 12)));
        return p;
    }
    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t); l.setForeground(COL_TEXT); return l;
    }

    // ================== CUSTOM COMPONENTS ==================
    class ActionButton extends JButton {
        private Color baseColor;
        public ActionButton(String text, Color bg) {
            super(text);
            baseColor = bg;
            setBackground(baseColor); setForeground(Color.WHITE);
            setFocusPainted(false); setBorder(BorderFactory.createLineBorder(baseColor.darker(), 1));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setPreferredSize(new Dimension(180, 45));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(true);
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { setBackground(baseColor.brighter()); }
                public void mouseExited(java.awt.event.MouseEvent e) { setBackground(baseColor); }
            });
        }
    }

    class DarkScrollBarUI extends BasicScrollBarUI {
        private final Color THUMB_COLOR = new Color(70, 70, 70);
        private final Color TRACK_COLOR = new Color(40, 40, 45);
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0));
            b.setOpaque(false); b.setBorder(null);
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(THUMB_COLOR);
            g2.fillRoundRect(r.x + 2, r.y + 2, Math.max(6, r.width - 4), Math.max(6, r.height - 4), 8, 8);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(TRACK_COLOR);
            g2.fillRect(r.x, r.y, r.width, r.height);
            g2.dispose();
        }
    }

    // --- FULL GRAPHICAL CHART RESTORED ---
    private void showBenchmarkChart(List<String> csvResults) {
        if (csvResults == null || csvResults.isEmpty()) return;
        List<String> labels = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        double max = 0.0;
        for (String line : csvResults) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                if (parts[2].equalsIgnoreCase("SKIPPED")) continue;
                try { double t = Double.parseDouble(parts[2]); labels.add(parts[0] + " (n=" + parts[1] + ")"); times.add(t); if (t > max) max = t; } catch (Exception ex) { }
            }
        }
        if (labels.isEmpty()) return;
        final List<String> labelsF = new ArrayList<>(labels);
        final List<Double> timesF = new ArrayList<>(times);
        final double maxVal = max;
        JDialog d = new JDialog(this, "Benchmark Results", true);
        d.setSize(700, Math.min(120 + labelsF.size() * 30, 900));
        d.setLocationRelativeTo(this);
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth();
                int pad = 40; int barMaxW = w - pad * 3 - 200;
                int y = 20;
                for (int i = 0; i < labelsF.size(); i++) {
                    String lbl = labelsF.get(i);
                    double t = timesF.get(i);
                    double ratio = (maxVal > 0) ? (t / maxVal) : 0.0;
                    int bw = (int) (ratio * barMaxW);
                    Color col = (t < 0.1) ? new Color(46,204,113) : (t < 1.0) ? new Color(241,196,15) : new Color(231,76,60);
                    g2.setColor(Color.WHITE);
                    g2.drawString(String.format("%s: %.4f s", lbl, t), pad, y + 12);
                    g2.setColor(col);
                    g2.fillRect(pad + 200, y, Math.max(4, bw), 16);
                    y += 28;
                }
            }
        };
        panel.setBackground(new Color(38,38,42));
        d.add(new JScrollPane(panel));
        d.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SortMasterExam().setVisible(true));
    }
}