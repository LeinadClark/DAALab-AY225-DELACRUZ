import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

public class MidtermLab2_DelaCruz extends JFrame {

    // --- DATA STRUCTURES ---
    static class Edge {
        String destination;
        double distance, time, fuel;

        public Edge(String destination, double distance, double time, double fuel) {
            this.destination = destination;
            this.distance = distance;
            this.time = time;
            this.fuel = fuel;
        }
    }

    static class PQNode implements Comparable<PQNode> {
        String cityName;
        double cost;

        public PQNode(String cityName, double cost) {
            this.cityName = cityName;
            this.cost = cost;
        }

        @Override
        public int compareTo(PQNode other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    private Map<String, List<Edge>> graph = new HashMap<>();
    private String[] cities = {"IMUS", "BACOOR", "DASMA", "KAWIT", "INDANG", "SILANG", "GENTRI", "NOVELETA"};

    // --- GUI COMPONENTS ---
    private JComboBox<String> startCombo, endCombo, criteriaCombo;
    private JTextArea resultArea;
    private GraphPanel graphPanel;

    public MidtermLab2_DelaCruz() {
        // Setup Native Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }

        setTitle("Cavite Network Optimizer (Midterm Lab 2)");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        loadData();

        // 1. Left Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        controlPanel.setPreferredSize(new Dimension(350, 0));
        controlPanel.setBackground(new Color(240, 244, 248));

        JLabel titleLabel = new JLabel("Route Calculator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(titleLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        controlPanel.add(new JLabel("Optimize For:"));
        criteriaCombo = new JComboBox<>(new String[]{"Distance", "Time", "Fuel"});
        criteriaCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        controlPanel.add(criteriaCombo);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        controlPanel.add(new JLabel("Origin City:"));
        startCombo = new JComboBox<>(cities);
        startCombo.setSelectedItem("IMUS");
        startCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        controlPanel.add(startCombo);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        controlPanel.add(new JLabel("Destination City:"));
        endCombo = new JComboBox<>(cities);
        endCombo.setSelectedItem("SILANG");
        endCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        controlPanel.add(endCombo);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton calcButton = new JButton("Find Shortest Path");
        calcButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcButton.setBackground(new Color(52, 152, 219));
        calcButton.setForeground(Color.WHITE);
        calcButton.setFocusPainted(false);
        calcButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        calcButton.addActionListener(e -> calculateAndDisplay());
        controlPanel.add(calcButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        controlPanel.add(scrollPane);

        add(controlPanel, BorderLayout.WEST);

        // 2. Right Canvas Panel (The Visual Node Map)
        graphPanel = new GraphPanel(graph);
        add(graphPanel, BorderLayout.CENTER);
    }

    private void loadData() {
        addConnection("IMUS", "BACOOR", 10, 15, 1.2);
        addConnection("BACOOR", "DASMA", 12, 25, 1.5);
        addConnection("DASMA", "KAWIT", 12, 25, 1.5);
        addConnection("KAWIT", "INDANG", 12, 25, 1.2);
        addConnection("INDANG", "SILANG", 14, 25, 1.5);
        addConnection("SILANG", "GENTRI", 10, 25, 1.3);
        addConnection("GENTRI", "NOVELETA", 10, 25, 1.5);
        addConnection("NOVELETA", "IMUS", 10, 15, 1.2);
        addConnection("BACOOR", "SILANG", 10, 25, 1.3);
        addConnection("DASMA", "SILANG", 12, 25, 1.5);
        addConnection("SILANG", "BACOOR", 10, 25, 1.3);
        addConnection("NOVELETA", "BACOOR", 10, 15, 1.2);
        addConnection("SILANG", "KAWIT", 14, 25, 1.2);
        addConnection("IMUS", "NOVELETA", 10, 15, 1.2);
    }

    private void addConnection(String from, String to, double d, double t, double f) {
        graph.putIfAbsent(from, new ArrayList<>());
        graph.get(from).add(new Edge(to, d, t, f));
    }

    // --- ALGORITHM LOGIC ---
    private void calculateAndDisplay() {
        String start = (String) startCombo.getSelectedItem();
        String end = (String) endCombo.getSelectedItem();
        String criteria = ((String) criteriaCombo.getSelectedItem()).toLowerCase();

        if (start.equals(end)) {
            resultArea.setText("Origin and Destination are the same!");
            graphPanel.setHighlightedPath(new ArrayList<>());
            return;
        }

        Map<String, Double> costs = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<PQNode> pq = new PriorityQueue<>();

        for (String node : graph.keySet()) costs.put(node, Double.MAX_VALUE);
        for (List<Edge> edges : graph.values()) {
            for (Edge e : edges) costs.putIfAbsent(e.destination, Double.MAX_VALUE);
        }

        costs.put(start, 0.0);
        pq.add(new PQNode(start, 0.0));

        while (!pq.isEmpty()) {
            PQNode current = pq.poll();
            String u = current.cityName;

            if (u.equals(end)) break;
            if (current.cost > costs.get(u)) continue;

            if (graph.containsKey(u)) {
                for (Edge edge : graph.get(u)) {
                    double weight = criteria.equals("distance") ? edge.distance :
                                    criteria.equals("time") ? edge.time : edge.fuel;

                    double newCost = costs.get(u) + weight;
                    if (newCost < costs.get(edge.destination)) {
                        costs.put(edge.destination, newCost);
                        previous.put(edge.destination, u);
                        pq.add(new PQNode(edge.destination, newCost));
                    }
                }
            }
        }

        // Trace Path
        List<String> path = new ArrayList<>();
        String curr = end;
        while (curr != null) {
            path.add(curr);
            curr = previous.get(curr);
        }
        Collections.reverse(path);

        if (path.isEmpty() || !path.get(0).equals(start)) {
            resultArea.setText("No valid path found from " + start + " to " + end + ".");
            graphPanel.setHighlightedPath(new ArrayList<>());
            return;
        }

        // Calculate Totals
        double tDist = 0, tTime = 0, tFuel = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String u = path.get(i), v = path.get(i + 1);
            for (Edge e : graph.get(u)) {
                if (e.destination.equals(v)) {
                    tDist += e.distance; tTime += e.time; tFuel += e.fuel;
                    break;
                }
            }
        }

        // Display exact format requested by rubric
        StringBuilder sb = new StringBuilder();
        sb.append("Shortest Path from Node ").append(start).append(" to Node ").append(end).append(":\n\n");
        sb.append("Path: ").append(String.join(" -> ", path)).append("\n\n");
        sb.append("Total Distance: ").append(tDist).append(" km\n");
        sb.append("Total Time: ").append(tTime).append(" mins\n");
        sb.append(String.format("Total Fuel: %.2f Liters\n", tFuel));
        
        resultArea.setText(sb.toString());

        // Update visual map
        graphPanel.setHighlightedPath(path);
    }

    // --- INNER CLASS: CUSTOM DRAWING PANEL FOR NODE MAP ---
    class GraphPanel extends JPanel {
        private Map<String, List<Edge>> graphData;
        private Map<String, Point> nodeLocations = new HashMap<>();
        private List<String> highlightedPath = new ArrayList<>();

        public GraphPanel(Map<String, List<Edge>> graph) {
            this.graphData = graph;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));

            // Hardcode coordinate positions for a clean layout
            nodeLocations.put("NOVELETA", new Point(150, 150));
            nodeLocations.put("IMUS", new Point(350, 100));
            nodeLocations.put("BACOOR", new Point(550, 150));
            nodeLocations.put("KAWIT", new Point(100, 350));
            nodeLocations.put("DASMA", new Point(550, 350));
            nodeLocations.put("GENTRI", new Point(250, 450));
            nodeLocations.put("SILANG", new Point(450, 500));
            nodeLocations.put("INDANG", new Point(250, 550));
        }

        public void setHighlightedPath(List<String> path) {
            this.highlightedPath = path;
            repaint(); // Trigger a redraw of the canvas
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Draw Edges
            g2.setStroke(new BasicStroke(2.0f));
            for (String u : graphData.keySet()) {
                Point p1 = nodeLocations.get(u);
                if (p1 == null) continue;

                for (Edge e : graphData.get(u)) {
                    Point p2 = nodeLocations.get(e.destination);
                    if (p2 == null) continue;

                    // Check if this edge is part of the highlighted path
                    boolean isHighlighted = false;
                    for (int i = 0; i < highlightedPath.size() - 1; i++) {
                        if (highlightedPath.get(i).equals(u) && highlightedPath.get(i+1).equals(e.destination)) {
                            isHighlighted = true;
                            break;
                        }
                    }

                    if (isHighlighted) {
                        g2.setColor(new Color(46, 204, 113)); // Bright Green
                        g2.setStroke(new BasicStroke(4.0f));
                    } else {
                        g2.setColor(new Color(189, 195, 199)); // Light Gray
                        g2.setStroke(new BasicStroke(1.5f));
                    }

                    g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }

            // 2. Draw Nodes
            for (String city : nodeLocations.keySet()) {
                Point p = nodeLocations.get(city);
                boolean inPath = highlightedPath.contains(city);

                // Node circle
                int radius = 25;
                g2.setColor(inPath ? new Color(39, 174, 96) : new Color(41, 128, 185)); // Green if path, Blue otherwise
                g2.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

                // Node text label
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(city);
                g2.drawString(city, p.x - (textWidth / 2), p.y - radius - 8);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MidtermLab2_DelaCruz app = new MidtermLab2_DelaCruz();
            app.setVisible(true);
        });
    }
}