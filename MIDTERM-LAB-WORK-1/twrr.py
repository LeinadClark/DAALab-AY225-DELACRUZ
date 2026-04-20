import argparse
import heapq
import tkinter as tk
from tkinter import ttk
from tkinter import messagebox


# Node code to location mapping.
LOCATIONS = {
    "A": "IMUS",
    "B": "BACOOR",
    "C": "DASMA",
    "D": "KAWIT",
    "E": "INDANG",
    "F": "SILANG",
    "G": "GENTRI",
    "H": "NOVELETA",
}

# Abbreviated node names shown as the primary node identifiers in the UI.
NODE_NAMES = {
    "A": "IMS",
    "B": "BCR",
    "C": "DAS",
    "D": "KWT",
    "E": "ING",
    "F": "SIL",
    "G": "GEN",
    "H": "NOV",
}
NODE_NAME_TO_CODE = {name: code for code, name in NODE_NAMES.items()}

# Edge dataset: (from, to, distance_km, time_min, fuel_liters)
# Every entry is bidirectional — both directions share the same D/T/F values.
_ONE_WAY = [
    ("A", "B", 10, 15, 1.2),
    ("B", "C", 12, 25, 1.5),
    ("C", "D", 12, 25, 1.5),
    ("D", "E", 12, 25, 1.2),
    ("E", "F", 14, 25, 1.5),
    ("F", "G", 10, 25, 1.3),
    ("G", "H", 10, 15, 1.5),
    ("H", "A", 10, 15, 1.2),
    ("B", "F", 10, 25, 1.3),
    ("C", "F", 12, 25, 1.5),
    ("H", "G", 10, 25, 1.3),
    ("H", "B", 10, 15, 1.2),
    ("F", "D", 14, 25, 1.2),
    ("A", "H", 10, 15, 1.2),
]
EDGE_ROWS = list({(min(a,b), max(a,b)): (a, b, d, t, f)
                  for a, b, d, t, f in _ONE_WAY}.values())
# Expand to both directions
EDGE_ROWS = [(a, b, d, t, f) for a, b, d, t, f in EDGE_ROWS] + \
            [(b, a, d, t, f) for a, b, d, t, f in EDGE_ROWS]

METRIC_KEYS = {
    "distance": "D",
    "time": "T",
    "fuel": "F",
}

NODE_POSITIONS = {
    "A": (120, 80),
    "B": (300, 70),
    "C": (470, 120),
    "D": (520, 270),
    "E": (370, 350),
    "F": (230, 280),
    "G": (130, 360),
    "H": (60, 220),
}


def build_graph(edge_rows):
    """Build adjacency-list graph from edge rows."""
    graph = {node: {} for node in LOCATIONS}
    for src, dst, distance, time, fuel in edge_rows:
        graph[src][dst] = {"D": distance, "T": time, "F": fuel}
    return graph


def node_label(code):
    """Format node label with letter and location name."""
    return f"{NODE_NAMES[code]} ({LOCATIONS[code]})"


def print_node_map(graph):
    """Print graph connections and edge attributes."""
    print("Node Map")
    print("-" * 64)
    for src in sorted(graph):
        print(node_label(src))
        for dst in sorted(graph[src]):
            edge = graph[src][dst]
            print(
                f"  -> {node_label(dst)} | "
                f"Distance={edge['D']} km, Time={edge['T']} min, Fuel={edge['F']} L"
            )
    print("-" * 64)


def dijkstra(graph, start, end, metric):
    """Find shortest path with deterministic tie-breakers.

    Tie-break priority:
    1) selected metric total
    2) the other two totals (in a fixed order per metric)
    3) fewer hops
    4) lexicographically smaller node-code path
    """

    def build_rank(distance_total, time_total, fuel_total, hops, path_tuple):
        if metric == "D":
            return (distance_total, time_total, fuel_total, hops, path_tuple)
        if metric == "T":
            return (time_total, distance_total, fuel_total, hops, path_tuple)
        return (fuel_total, distance_total, time_total, hops, path_tuple)

    start_path = (start,)
    start_rank = build_rank(0.0, 0.0, 0.0, 0, start_path)
    pq = [(start_rank, start, start_path, 0.0, 0.0, 0.0)]
    best = {start: start_rank}

    while pq:
        rank, node, path_tuple, distance_total, time_total, fuel_total = heapq.heappop(pq)

        if node == end:
            return list(path_tuple), rank[0]

        if rank != best.get(node):
            continue

        for neighbor, attrs in graph[node].items():
            next_distance = distance_total + attrs["D"]
            next_time = time_total + attrs["T"]
            next_fuel = fuel_total + attrs["F"]
            next_path = path_tuple + (neighbor,)
            next_rank = build_rank(next_distance, next_time, next_fuel, len(next_path) - 1, next_path)

            if next_rank < best.get(neighbor, (float("inf"),)):
                best[neighbor] = next_rank
                heapq.heappush(
                    pq,
                    (next_rank, neighbor, next_path, next_distance, next_time, next_fuel),
                )

    return None, None


def path_totals(graph, path):
    """Compute distance/time/fuel totals for a path."""
    totals = {"D": 0.0, "T": 0.0, "F": 0.0}
    for i in range(len(path) - 1):
        edge = graph[path[i]][path[i + 1]]
        totals["D"] += edge["D"]
        totals["T"] += edge["T"]
        totals["F"] += edge["F"]
    return totals


def format_node_map(graph):
    """Return a formatted string view of the node map."""
    lines = ["Node Map", "-" * 64]
    for src in sorted(graph):
        lines.append(node_label(src))
        for dst in sorted(graph[src]):
            edge = graph[src][dst]
            lines.append(
                f"  -> {node_label(dst)} | "
                f"Distance={edge['D']} km, Time={edge['T']} min, Fuel={edge['F']} L"
            )
    lines.append("-" * 64)
    return "\n".join(lines)


def format_shortest_path_result(graph, start, end, metric_name):
    """Return formatted shortest path result text."""
    metric_key = METRIC_KEYS[metric_name]
    path, optimized_total = dijkstra(graph, start, end, metric_key)

    if path is None:
        return f"No route found from {node_label(start)} to {node_label(end)}"

    totals = path_totals(graph, path)
    path_text = " -> ".join(node_label(node) for node in path)
    return "\n".join(
        [
            f"Shortest Path from {node_label(start)} to {node_label(end)}",
            f"Optimized by: {metric_name}",
            f"Path: {path_text}",
            f"Total Distance: {totals['D']} km",
            f"Total Time: {totals['T']} mins",
            f"Total Fuel: {totals['F']} Liters",
            f"Optimized metric total: {optimized_total}",
        ]
    )


class TSPGuiApp:
    """GUI with visual node map and route tracking animation."""

    def __init__(self, root):
        self.root = root
        self.root.title("Route Studio - Node Map and Tracking")
        self.root.geometry("1030x640")
        self.root.minsize(860, 560)
        self.root.configure(bg="#111827")

        self.graph = build_graph(EDGE_ROWS)
        self.node_items = {}
        self.edge_items = {}
        self.current_path = []
        self.current_metric = self.metric_var.get() if hasattr(self, "metric_var") else "distance"
        self.last_totals = None
        self.animation_ids = []

        self._setup_styles()

        self.distance_var = tk.StringVar(value="Distance: -- km")
        self.time_var = tk.StringVar(value="Time: -- mins")
        self.fuel_var = tk.StringVar(value="Fuel: -- L")
        self.status_var = tk.StringVar(value="Ready. Select nodes and compute shortest path.")
        self.route_title_var = tk.StringVar(value="No route selected")
        self.route_path_var = tk.StringVar(value="Path: --")
        self.metric_total_var = tk.StringVar(value="Optimized total: --")

        container = ttk.Frame(root, padding=8)
        container.pack(fill=tk.BOTH, expand=True)

        header = ttk.Frame(container)
        header.pack(fill=tk.X, pady=(0, 4))
        ttk.Label(header, text="Route Studio", style="Title.TLabel").pack(anchor="w")
        ttk.Label(
            header,
            text="Plan cleaner routes with live map highlighting and step-by-step tracking.",
            style="Subtle.TLabel",
        ).pack(anchor="w")

        main_split = ttk.Frame(container)
        main_split.pack(fill=tk.BOTH, expand=True, pady=(6, 0))

        sidebar = ttk.Frame(main_split, width=390)
        sidebar.pack(side=tk.LEFT, fill=tk.Y, padx=(0, 8))
        sidebar.pack_propagate(False)

        controls = ttk.LabelFrame(sidebar, text="Route Planner", padding=8)
        controls.pack(fill=tk.X)

        node_values = list(sorted(LOCATIONS.keys()))
        metric_values = ["distance", "time", "fuel"]

        self.start_var = tk.StringVar(value="A")
        self.end_var = tk.StringVar(value="E")
        self.metric_var = tk.StringVar(value="distance")

        ttk.Label(controls, text="Start Node", style="SectionLabel.TLabel").grid(row=0, column=0, padx=4, pady=(1, 3), sticky="w")
        self.start_combo = ttk.Combobox(
            controls,
            width=34,
            values=[f"{NODE_NAMES[n]} - {LOCATIONS[n]}" for n in node_values],
            state="readonly",
        )
        self.start_combo.grid(row=1, column=0, padx=4, pady=(0, 6), sticky="ew")
        self.start_combo.set("IMS - IMUS")
        self.start_combo.bind("<<ComboboxSelected>>", self.on_selection_change)

        ttk.Label(controls, text="End Node", style="SectionLabel.TLabel").grid(row=2, column=0, padx=4, pady=(1, 3), sticky="w")
        self.end_combo = ttk.Combobox(
            controls,
            width=34,
            values=[f"{NODE_NAMES[n]} - {LOCATIONS[n]}" for n in node_values],
            state="readonly",
        )
        self.end_combo.grid(row=3, column=0, padx=4, pady=(0, 6), sticky="ew")
        self.end_combo.set("ING - INDANG")
        self.end_combo.bind("<<ComboboxSelected>>", self.on_selection_change)

        ttk.Label(controls, text="Metric", style="SectionLabel.TLabel").grid(row=4, column=0, padx=4, pady=(1, 3), sticky="w")
        self.metric_combo = ttk.Combobox(
            controls, width=12, values=metric_values, textvariable=self.metric_var, state="readonly"
        )
        self.metric_combo.grid(row=5, column=0, padx=4, pady=(0, 8), sticky="ew")
        self.metric_combo.bind("<<ComboboxSelected>>", self.on_selection_change)

        actions = ttk.Frame(controls)
        actions.grid(row=6, column=0, sticky="ew", padx=4, pady=(0, 6))
        ttk.Button(actions, text="Track", command=self.track_route, style="Track.TButton").grid(
            row=0, column=0, padx=(0, 6), sticky="ew"
        )
        ttk.Button(actions, text="Show Map", command=self.show_node_map, style="Map.TButton").grid(
            row=0, column=1, padx=(0, 6), sticky="ew"
        )
        ttk.Button(actions, text="Swap Nodes", command=self.swap_nodes, style="Swap.TButton").grid(
            row=0, column=2, sticky="ew"
        )
        for col in range(3):
            actions.grid_columnconfigure(col, weight=1)

        ttk.Label(
            controls,
            text="Tip: Route updates automatically. Ctrl+T = Track, Ctrl+R = Reset map",
            style="Subtle.TLabel",
        ).grid(row=7, column=0, sticky="w", padx=4, pady=(0, 6))

        controls.grid_columnconfigure(0, weight=1)

        stats = ttk.Frame(controls)
        stats.grid(row=8, column=0, sticky="ew", padx=4, pady=(0, 1))
        stats.grid_columnconfigure(0, weight=1)

        metric_card_1 = ttk.Frame(stats, style="Card.TFrame", padding=(8, 6))
        metric_card_1.grid(row=0, column=0, sticky="ew", pady=(0, 4))
        metric_card_2 = ttk.Frame(stats, style="Card.TFrame", padding=(8, 6))
        metric_card_2.grid(row=1, column=0, sticky="ew", pady=(0, 4))
        metric_card_3 = ttk.Frame(stats, style="Card.TFrame", padding=(8, 6))
        metric_card_3.grid(row=2, column=0, sticky="ew")
        ttk.Label(metric_card_1, textvariable=self.distance_var, style="Stat.TLabel").pack(anchor="w")
        ttk.Label(metric_card_2, textvariable=self.time_var, style="Stat.TLabel").pack(anchor="w")
        ttk.Label(metric_card_3, textvariable=self.fuel_var, style="Stat.TLabel").pack(anchor="w")
        ttk.Label(stats, textvariable=self.status_var, style="Subtle.TLabel", wraplength=340, justify="left").grid(
            row=3, column=0, sticky="w", pady=(6, 0)
        )

        map_frame = ttk.LabelFrame(main_split, text="Node Map Visualizer", padding=8)
        map_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        self.canvas = tk.Canvas(
            map_frame,
            bg="#0b1220",
            width=560,
            height=440,
            highlightthickness=0,
        )
        self.canvas.pack(fill=tk.BOTH, expand=True)

        ttk.Label(
            map_frame,
            text="Legend: Blue nodes = graph, Orange edges = selected route, Red dot = tracker",
            style="Subtle.TLabel",
        ).pack(anchor="w", pady=(6, 0))

        details_actions = ttk.LabelFrame(sidebar, text="Route Details", padding=8)
        details_actions.pack(fill=tk.X, pady=(8, 0))
        ttk.Button(
            details_actions,
            text="View Detailed Summary",
            command=self.show_route_details_popup,
            style="Track.TButton",
        ).pack(fill=tk.X)

        steps_frame = ttk.LabelFrame(sidebar, text="Tracking Steps", padding=6)
        steps_frame.pack(fill=tk.BOTH, expand=True)

        self.steps_list = tk.Listbox(
            steps_frame,
            font=("Segoe UI", 10),
            activestyle="none",
            borderwidth=0,
            highlightthickness=1,
            highlightbackground="#3a465c",
            bg="#111827",
            fg="#dbe3f2",
            selectbackground="#2563eb",
            selectforeground="#f8fafc",
        )
        steps_scroll = ttk.Scrollbar(steps_frame, orient="vertical", command=self.steps_list.yview)
        self.steps_list.configure(yscrollcommand=steps_scroll.set)
        self.steps_list.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        steps_scroll.pack(side=tk.RIGHT, fill=tk.Y)

        self._populate_steps([])

        self.draw_map()
        self.root.bind("<Control-t>", lambda _e: self.track_route())
        self.root.bind("<Control-r>", lambda _e: self.show_node_map())
        self.root.after(150, self.compute_path)

    def _setup_styles(self):
        style = ttk.Style(self.root)
        style.theme_use("clam")
        style.configure("TFrame", background="#111827")
        style.configure("TLabelframe", background="#111827", borderwidth=0)
        style.configure("TLabelframe.Label", background="#111827", foreground="#dbe3f2", font=("Segoe UI", 10, "bold"))
        style.configure("TLabel", background="#111827", foreground="#cbd5e1")
        style.configure("Title.TLabel", font=("Segoe UI", 20, "bold"), foreground="#f8fafc")
        style.configure("Subtle.TLabel", font=("Segoe UI", 10), foreground="#94a3b8")
        style.configure("SectionLabel.TLabel", font=("Segoe UI", 10, "bold"), foreground="#cbd5e1")
        style.configure("Stat.TLabel", font=("Segoe UI", 11, "bold"), foreground="#e2e8f0")
        style.configure("CardTitle.TLabel", font=("Segoe UI", 13, "bold"), foreground="#f8fafc")
        style.configure("Card.TFrame", background="#1f2937")
        style.configure("TCombobox", padding=6, fieldbackground="#1f2937", background="#1f2937", foreground="#f8fafc")
        style.map(
            "TCombobox",
            fieldbackground=[("readonly", "#1f2937")],
            foreground=[("readonly", "#f8fafc")],
            selectbackground=[("readonly", "#1d4ed8")],
            selectforeground=[("readonly", "#f8fafc")],
        )
        style.configure("TButton", padding=(12, 7), font=("Segoe UI", 10, "bold"), foreground="#f8fafc", background="#334155")
        style.map("TButton", background=[("active", "#475569"), ("pressed", "#1e293b")])

        style.configure("Compute.TButton", background="#16a34a", foreground="#f8fafc")
        style.map("Compute.TButton", background=[("active", "#15803d"), ("pressed", "#166534")])

        style.configure("Track.TButton", background="#2563eb", foreground="#f8fafc")
        style.map("Track.TButton", background=[("active", "#1d4ed8"), ("pressed", "#1e40af")])

        style.configure("Map.TButton", background="#d97706", foreground="#fefce8")
        style.map("Map.TButton", background=[("active", "#b45309"), ("pressed", "#92400e")])

        style.configure("Swap.TButton", background="#7c3aed", foreground="#f5f3ff")
        style.map("Swap.TButton", background=[("active", "#6d28d9"), ("pressed", "#5b21b6")])

    def _extract_node(self, combo_value):
        prefix = combo_value.split(" - ")[0].strip().upper()
        return NODE_NAME_TO_CODE.get(prefix, prefix)

    def _populate_steps(self, path):
        self.steps_list.delete(0, tk.END)
        if not path:
            self.steps_list.insert(tk.END, "1. Select start/end and metric to auto-compute route.")
            self.steps_list.insert(tk.END, "2. Press Track to animate movement.")
            return

        for idx, node in enumerate(path, start=1):
            self.steps_list.insert(tk.END, f"{idx}. {NODE_NAMES[node]} - {LOCATIONS[node]}")

    def _show_compact_message(self, title, status_text):
        self.route_title_var.set(title)
        self.route_path_var.set("Path: --")
        self.metric_total_var.set("Optimized total: --")
        self.last_totals = None
        self._set_summary(status_text=status_text)
        self._populate_steps([])

    def _set_summary(self, distance=None, time_val=None, fuel=None, status_text=None, selected_metric=None):
        selected = (selected_metric or self.metric_var.get().strip().lower())

        distance_text = ""
        time_text = ""
        fuel_text = ""

        if selected == "distance":
            value = distance if distance is not None else ""
            distance_text = f"{value} km" if value != "" else ""
        elif selected == "time":
            value = time_val if time_val is not None else ""
            time_text = f"{value} mins" if value != "" else ""
        elif selected == "fuel":
            value = fuel if fuel is not None else ""
            fuel_text = f"{value} L" if value != "" else ""

        self.distance_var.set(f"Distance: {distance_text}")
        self.time_var.set(f"Time: {time_text}")
        self.fuel_var.set(f"Fuel: {fuel_text}")
        if status_text:
            self.status_var.set(status_text)

    def on_selection_change(self, _event=None):
        self.compute_path()

    def show_node_map(self):
        self.clear_highlights()
        self._set_summary(status_text="Map view reset.", selected_metric=self.metric_var.get())
        self.route_title_var.set("Node map ready")
        self.route_path_var.set("Path: Select start/end and metric")
        self.metric_total_var.set("Optimized total: --")
        self.current_metric = self.metric_var.get().strip().lower()
        self.last_totals = None
        self._populate_steps([])

    def swap_nodes(self):
        start = self.start_combo.get()
        end = self.end_combo.get()
        self.start_combo.set(end)
        self.end_combo.set(start)
        self.status_var.set("Start and end nodes swapped.")
        self.compute_path()

    def draw_map(self):
        self.canvas.delete("all")
        self.edge_items.clear()
        self.node_items.clear()

        # Draw directed edges first so nodes are rendered on top.
        for src, neighbors in self.graph.items():
            x1, y1 = NODE_POSITIONS[src]
            for dst, edge in neighbors.items():
                x2, y2 = NODE_POSITIONS[dst]
                line = self.canvas.create_line(
                    x1,
                    y1,
                    x2,
                    y2,
                    fill="#475569",
                    width=2,
                    arrow=tk.LAST,
                    arrowshape=(12, 14, 5),
                )
                mx, my = (x1 + x2) / 2, (y1 + y2) / 2
                label = self.canvas.create_text(
                    mx,
                    my,
                    text=f"{edge['D']}km/{edge['T']}m/{edge['F']}L",
                    fill="#94a3b8",
                    font=("Segoe UI", 9, "bold"),
                )
                self.edge_items[(src, dst)] = (line, label)

        for node, (x, y) in NODE_POSITIONS.items():
            circle = self.canvas.create_oval(
                x - 22,
                y - 22,
                x + 22,
                y + 22,
                fill="#2563eb",
                outline="#93c5fd",
                width=2,
            )
            text = self.canvas.create_text(
                x,
                y,
                text=NODE_NAMES[node],
                fill="white",
                font=("Segoe UI", 11, "bold"),
            )
            name = self.canvas.create_text(
                x,
                y + 34,
                text=LOCATIONS[node],
                fill="#cbd5e1",
                font=("Segoe UI", 9),
            )
            self.node_items[node] = (circle, text, name)

    def clear_highlights(self):
        for line, label in self.edge_items.values():
            self.canvas.itemconfig(line, fill="#475569", width=2)
            self.canvas.itemconfig(label, fill="#94a3b8")
        for circle, _, _ in self.node_items.values():
            self.canvas.itemconfig(circle, fill="#2563eb", outline="#93c5fd", width=2)
        for aid in self.animation_ids:
            self.root.after_cancel(aid)
        self.animation_ids.clear()
        self.canvas.delete("tracker")

    def highlight_path(self, path):
        self.clear_highlights()
        for node in path:
            circle, _, _ = self.node_items[node]
            self.canvas.itemconfig(circle, fill="#0f766e", outline="#ccfbf1", width=3)
        for i in range(len(path) - 1):
            key = (path[i], path[i + 1])
            if key in self.edge_items:
                line, label = self.edge_items[key]
                self.canvas.itemconfig(line, fill="#ea580c", width=4)
                self.canvas.itemconfig(label, fill="#b45309")

    def animate_tracker(self, path):
        if len(path) < 2:
            return

        step_delay = 320
        start_delay = 120
        self.canvas.delete("tracker")

        for i in range(len(path) - 1):
            src = path[i]
            dst = path[i + 1]
            x1, y1 = NODE_POSITIONS[src]
            x2, y2 = NODE_POSITIONS[dst]

            for frame in range(1, 13):
                progress = frame / 12.0
                x = x1 + (x2 - x1) * progress
                y = y1 + (y2 - y1) * progress
                delay = start_delay + i * step_delay + frame * 22
                aid = self.root.after(delay, self._draw_tracker, x, y)
                self.animation_ids.append(aid)

    def _draw_tracker(self, x, y):
        self.canvas.delete("tracker")
        self.canvas.create_oval(
            x - 8,
            y - 8,
            x + 8,
            y + 8,
            fill="#dc2626",
            outline="white",
            width=2,
            tags="tracker",
        )

    def compute_path(self):
        start = self._extract_node(self.start_combo.get())
        end = self._extract_node(self.end_combo.get())
        metric = self.metric_var.get().strip().lower()

        if start not in LOCATIONS or end not in LOCATIONS:
            self._show_compact_message("Invalid selection", "Invalid node selection.")
            return

        if metric not in METRIC_KEYS:
            self._show_compact_message("Invalid metric", "Invalid metric selection.")
            return

        metric_key = METRIC_KEYS[metric]
        path, _ = dijkstra(self.graph, start, end, metric_key)
        if path is None:
            self._show_compact_message("No route found", "No route found.")
            self.clear_highlights()
            return

        self.current_path = path
        self.current_metric = metric
        self.highlight_path(path)
        totals = path_totals(self.graph, path)
        self.last_totals = totals
        path_codes = " -> ".join(NODE_NAMES[node] for node in path)
        self.route_title_var.set(f"{node_label(start)} to {node_label(end)}")
        self.route_path_var.set(f"Path: {path_codes}")
        self.metric_total_var.set(f"Optimized total ({metric}): {round(totals[METRIC_KEYS[metric]], 2)}")
        self._populate_steps(path)
        self._set_summary(
            distance=totals["D"],
            time_val=totals["T"],
            fuel=totals["F"],
            status_text=f"Route computed: {start} to {end} by {metric}.",
            selected_metric=metric,
        )

    def show_route_details_popup(self):
        if not self.current_path or not self.last_totals:
            messagebox.showinfo("Route Details", "No computed route yet. Select nodes and metric first.")
            return

        start = self.current_path[0]
        end = self.current_path[-1]
        path_codes = " -> ".join(NODE_NAMES[node] for node in self.current_path)
        summary = "\n".join(
            [
                f"Route: {node_label(start)} to {node_label(end)}",
                f"Metric: {self.current_metric}",
                f"Path: {path_codes}",
                f"Total Distance: {self.last_totals['D']} km",
                f"Total Time: {self.last_totals['T']} mins",
                f"Total Fuel: {self.last_totals['F']} L",
                f"Optimized total ({self.current_metric}): {round(self.last_totals[METRIC_KEYS[self.current_metric]], 2)}",
            ]
        )
        messagebox.showinfo("Route Details", summary)

    def track_route(self):
        if not self.current_path:
            self._show_compact_message("No route to track", "Compute route first before tracking.")
            return
        self.highlight_path(self.current_path)
        self.animate_tracker(self.current_path)
        self.status_var.set("Tracking route animation...")


def run_gui():
    root = tk.Tk()
    app = TSPGuiApp(root)
    root.mainloop()


def main():
    parser = argparse.ArgumentParser(
        description="Node map + shortest path using letter-coded locations"
    )
    parser.add_argument("--start", default="A", help="Start node letter (default: A)")
    parser.add_argument("--end", default="E", help="End node letter (default: E)")
    parser.add_argument(
        "--metric",
        choices=("distance", "time", "fuel"),
        default="distance",
        help="Optimization metric",
    )
    parser.add_argument("--cli", action="store_true", help="Run in console mode")
    args = parser.parse_args()

    # Window-first behavior: open visualizer unless CLI mode is explicitly requested.
    if not args.cli:
        run_gui()
        return

    start = args.start.upper()
    end = args.end.upper()

    if start not in LOCATIONS or end not in LOCATIONS:
        print("Invalid node letter. Use one of:", ", ".join(sorted(LOCATIONS)))
        return

    graph = build_graph(EDGE_ROWS)
    print_node_map(graph)

    print(format_shortest_path_result(graph, start, end, args.metric))


if __name__ == "__main__":
    main()