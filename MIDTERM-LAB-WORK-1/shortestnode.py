import heapq
import os
from typing import Dict, Tuple

# ANSI Escape Codes for Terminal Colors
class Color:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BOLD = '\033[1m'
    RESET = '\033[0m'

def get_shortest_paths(graph: Dict[int, Dict[int, float]], start_node: int) -> Tuple[Dict[int, float], float]:
    if not graph:
        raise ValueError("The provided graph is empty.")
    if start_node not in graph:
        raise KeyError(f"Start node '{start_node}' does not exist in the graph.")

    distances = {node: float('inf') for node in graph}
    distances[start_node] = 0.0
    pq = [(0.0, start_node)]

    while pq:
        current_dist, u = heapq.heappop(pq)

        if current_dist > distances[u]:
            continue

        if u not in graph:
            continue

        for v, weight in graph[u].items():
            if not isinstance(weight, (int, float)):
                raise TypeError(f"Invalid weight '{weight}' between node {u} and {v}. Must be a number.")

            new_dist = current_dist + weight

            if new_dist < distances.get(v, float('inf')):
                distances[v] = new_dist
                heapq.heappush(pq, (new_dist, v))

    rounded_distances = {
        n: round(d, 2) if d != float('inf') else d
        for n, d in distances.items()
    }

    total_sum = sum(
        d for n, d in rounded_distances.items()
        if n != start_node and d != float('inf')
    )

    return rounded_distances, round(total_sum, 2)


def evaluate_networks():
    # --- Graphs (Distance, Time, Fuel) ---
    graph_1 = {
        1: {2: 10, 6: 10},
        2: {6: 10, 3: 12, 5: 12, 1: 10},
        3: {6: 10, 5: 12, 4: 12, 2: 12},
        4: {3: 12, 5: 14},
        5: {6: 10, 4: 14, 3: 12, 2: 12},
        6: {1: 10, 2: 10, 5: 10, 3: 10}
    }

    graph_2 = {
        1: {2: 15, 6: 15},
        2: {6: 15, 3: 25, 5: 25, 1: 15},
        3: {6: 25, 5: 25, 4: 25, 2: 25},
        4: {3: 25, 5: 25},
        5: {6: 25, 4: 25, 3: 25, 2: 25},
        6: {1: 15, 2: 15, 5: 25, 3: 25}
    }

    graph_3 = {
        1: {2: 1.2, 6: 1.2},
        2: {6: 1.2, 3: 1.5, 5: 1.5, 1: 1.2},
        3: {6: 1.3, 5: 1.5, 4: 1.5, 2: 1.5},
        4: {3: 1.5, 5: 1.2},
        5: {6: 1.5, 4: 1.2, 3: 1.5, 2: 1.5},
        6: {1: 1.2, 2: 1.2, 5: 1.5, 3: 1.3}
    }

    graphs = [
        ("Distance", graph_1), 
        ("Time", graph_2), 
        ("Fuel", graph_3)
    ]

    for name, g in graphs:
        print(f"\n{Color.HEADER}{Color.BOLD}====================== Graph: {name} ======================{Color.RESET}")

        best_node = None
        best_total = float('inf')

        for node in g:
            try:
                dist_map, total = get_shortest_paths(g, node)
                
                print(f"{Color.CYAN}Start Node: {node}{Color.RESET}")
                # Changed to safe ASCII characters
                print(f"  |-- Distances: {dist_map}")
                print(f"  |-- Total Distance: {Color.YELLOW}{total}{Color.RESET}")

                if total < best_total:
                    best_total = total
                    best_node = node

            except Exception as e:
                print(f"{Color.RED}Error processing Node {node}: {e}{Color.RESET}")

        print(f"\n{Color.GREEN}{Color.BOLD}*** Shortest Overall Origin Node for {name} ***{Color.RESET}")
        # Changed to safe ASCII characters
        print(f"{Color.GREEN}-> Node: {best_node}{Color.RESET}")
        print(f"{Color.GREEN}-> Minimum Sum of Paths: {best_total}{Color.RESET}\n")

if __name__ == "__main__":
    # Force Windows to properly render ANSI escape sequences
    if os.name == 'nt':
        os.system('color')

    try:
        evaluate_networks()
    except KeyboardInterrupt:
        print(f"\n{Color.RED}Execution cancelled by user.{Color.RESET}")
    except Exception as e:
        print(f"\n{Color.RED}An unexpected error occurred: {e}{Color.RESET}")