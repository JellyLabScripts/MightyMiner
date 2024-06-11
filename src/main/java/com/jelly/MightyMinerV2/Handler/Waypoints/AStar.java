package com.jelly.MightyMinerV2.Handler.Waypoints;

import java.util.*;

public class AStar {
    private final Graph graph;

    public AStar(Graph graph) {
        this.graph = graph;
    }

    public List<Waypoint> findShortestPath(Waypoint start, Waypoint end) {
        Map<Waypoint, Waypoint> cameFrom = new HashMap<>();
        Map<Waypoint, Double> costSoFar = new HashMap<>();
        PriorityQueue<Waypoint> frontier = new PriorityQueue<>(Comparator.comparingDouble(w -> costSoFar.getOrDefault(w, Double.POSITIVE_INFINITY) + heuristicCostEstimate(w, end)));

        frontier.add(start);
        cameFrom.put(start, null);
        costSoFar.put(start, 0.0);

        while (!frontier.isEmpty()) {
            Waypoint current = frontier.poll();

            if (current.equals(end)) {
                return reconstructPath(cameFrom, start, end);
            }

            for (Edge edge : graph.getEdges(current)) {
                double newCost = costSoFar.get(current) + edge.getWeight();
                if (!costSoFar.containsKey(edge.getTarget()) || newCost < costSoFar.get(edge.getTarget())) {
                    costSoFar.put(edge.getTarget(), newCost);
                    cameFrom.put(edge.getTarget(), current);
                    frontier.add(edge.getTarget());
                }
            }
        }

        return Collections.emptyList();
    }

    private double heuristicCostEstimate(Waypoint start, Waypoint end) {
        // Implement a heuristic function that estimates the cost to reach the end waypoint from the start waypoint
        // For example, you can use the Euclidean distance between the two waypoints
        return start.getPosition().distanceTo(end.getPosition());
    }

    private List<Waypoint> reconstructPath(Map<Waypoint, Waypoint> cameFrom, Waypoint start, Waypoint end) {
        List<Waypoint> path = new ArrayList<>();
        Waypoint current = end;

        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}
