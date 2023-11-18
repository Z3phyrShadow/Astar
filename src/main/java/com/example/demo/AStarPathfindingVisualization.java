package com.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;

public class AStarPathfindingVisualization extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int ROWS = 20;
    private static final int COLS = 25;
    private static final int RECT_WIDTH = WIDTH / COLS;
    private static final int RECT_HEIGHT = HEIGHT / ROWS;

    private Pane root;
    private Rectangle[][] grid = new Rectangle[COLS][ROWS];
    private boolean[][] isObstacle = new boolean[COLS][ROWS];
    private Point start;
    private Point end;
    private List<Point> lastPath = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        BorderPane borderPane = new BorderPane(root);
        Scene scene = new Scene(borderPane, WIDTH, HEIGHT);

        initializeGrid();

        root.setOnMouseClicked(event -> {
            int mouseX = (int) event.getX() / RECT_WIDTH;
            int mouseY = (int) event.getY() / RECT_HEIGHT;

            if (event.getButton() == MouseButton.PRIMARY) {
                setStart(mouseX, mouseY);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                setEnd(mouseX, mouseY);
            }

            if (start != null && end != null) {
                clearPreviousPath();
                List<Point> path = aStar(start, end);
                if (path != null) {
                    lastPath = path;
                    animatePath(path);
                } else {
                    System.out.println("No path found!");
                }
            }
        });

        primaryStage.setTitle("A* Pathfinding Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeGrid() {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                Rectangle rect = new Rectangle(x * RECT_WIDTH, y * RECT_HEIGHT, RECT_WIDTH, RECT_HEIGHT);
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.BLACK);
                root.getChildren().add(rect);
                grid[x][y] = rect;
            }
        }

        // Adding obstacles (randomly for demonstration)
        addObstacles();
    }

    private void addObstacles() {
        // Logic to add obstacles (for demonstration, we'll add some randomly)
        for (int i = 0; i < ROWS * COLS * 0.3; i++) {
            int randomRow = (int) (Math.random() * ROWS);
            int randomCol = (int) (Math.random() * COLS);
            Rectangle obstacle = grid[randomCol][randomRow];
            obstacle.setFill(Color.BLACK);
            isObstacle[randomCol][randomRow] = true;
        }
    }

    private void setStart(int x, int y) {
        clearPreviousPath();
        start = new Point(x, y);
        grid[x][y].setFill(Color.BLUE);
    }

    private void setEnd(int x, int y) {
        clearPreviousPath();
        end = new Point(x, y);
        grid[x][y].setFill(Color.RED);
    }

    private void clearPreviousPath() {
        for (Point point : lastPath) {
            Rectangle rect = grid[point.x][point.y];
            rect.setFill(Color.WHITE);
        }
    }

    private List<Point> aStar(Point start, Point end) {
        Set<Point> visited = new HashSet<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        Map<Point, Integer> costSoFar = new HashMap<>();
        Map<Point, Point> cameFrom = new HashMap<>();

        queue.add(new Node(start, 0));
        costSoFar.put(start, 0);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            Point currentPoint = current.point;

            if (currentPoint.equals(end)) {
                return reconstructPath(cameFrom, start, end);
            }

            visited.add(currentPoint);

            List<Point> neighbors = getNeighbors(currentPoint);
            for (Point next : neighbors) {
                if (!visited.contains(next) && !isObstacle[next.x][next.y]) {
                    int newCost = costSoFar.get(currentPoint) + 1;
                    if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
                        costSoFar.put(next, newCost);
                        int priority = newCost + heuristic(next, end);
                        queue.add(new Node(next, priority));
                        cameFrom.put(next, currentPoint);
                    }
                }
            }
        }
        return null; // If no path found
    }

    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point start, Point end) {
        List<Point> path = new ArrayList<>();
        Point current = end;
        while (!current.equals(start)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    private int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private List<Point> getNeighbors(Point point) {
        List<Point> neighbors = new ArrayList<>();
        int x = point.x;
        int y = point.y;
        if (x > 0) neighbors.add(new Point(x - 1, y));
        if (x < COLS - 1) neighbors.add(new Point(x + 1, y));
        if (y > 0) neighbors.add(new Point(x, y - 1));
        if (y < ROWS - 1) neighbors.add(new Point(x, y + 1));
        return neighbors;
    }

    private void animatePath(List<Point> path) {
        int delay = 100; // Milliseconds delay between each step
        new Thread(() -> {
            for (Point point : path) {
                Rectangle rect = grid[point.x][point.y];
                rect.setFill(Color.GREEN);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class Point {
        int x;
        int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private static class Node {
        Point point;
        int cost;

        Node(Point point, int cost) {
            this.point = point;
            this.cost = cost;
        }
    }
}

