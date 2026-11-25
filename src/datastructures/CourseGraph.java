package datastructures;

/**
 * Undirected graph (adjacency list) that connects related courses.
 * A course appears as a vertex only while it has at least one relationship.
 */
public class CourseGraph {

    private static class Vertex {
        final String courseId;
        final SinglyLinkedList<String> neighbors = new SinglyLinkedList<>();

        Vertex(String courseId) {
            this.courseId = courseId;
        }
    }

    private final SinglyLinkedList<Vertex> vertices = new SinglyLinkedList<>();

    private Vertex find(String courseId) {
        for (Vertex vertex : vertices) {
            if (vertex.courseId.equals(courseId)) {
                return vertex;
            }
        }
        return null;
    }

    private Vertex findOrCreate(String courseId) {
        Vertex vertex = find(courseId);
        if (vertex == null) {
            vertex = new Vertex(courseId);
            vertices.addLast(vertex);
        }
        return vertex;
    }

    private void removeNeighbor(Vertex vertex, String neighborId) {
        int index = vertex.neighbors.indexOf(neighborId);
        if (index >= 0) {
            vertex.neighbors.removeAt(index);
        }
    }

    private void removeIfIsolated(String courseId) {
        Vertex vertex = find(courseId);
        if (vertex != null && vertex.neighbors.isEmpty()) {
            vertices.removeAt(vertices.indexOf(vertex));
        }
    }

    public boolean hasEdge(String a, String b) {
        Vertex vertex = find(a);
        return vertex != null && vertex.neighbors.contains(b);
    }

    /** Connects two courses. Returns false if they are the same or already connected. */
    public boolean addEdge(String a, String b) {
        if (a.equals(b) || hasEdge(a, b)) {
            return false;
        }
        findOrCreate(a).neighbors.addLast(b);
        findOrCreate(b).neighbors.addLast(a);
        return true;
    }

    public boolean removeEdge(String a, String b) {
        if (!hasEdge(a, b)) {
            return false;
        }
        removeNeighbor(find(a), b);
        removeNeighbor(find(b), a);
        removeIfIsolated(a);
        removeIfIsolated(b);
        return true;
    }

    /** Removes a course and all its relationships. Returns the courses it was connected to. */
    public SinglyLinkedList<String> removeCourse(String courseId) {
        SinglyLinkedList<String> formerNeighbors = getNeighbors(courseId);
        Vertex vertex = find(courseId);
        if (vertex == null) {
            return formerNeighbors;
        }
        for (String neighborId : formerNeighbors) {
            removeNeighbor(find(neighborId), courseId);
            removeIfIsolated(neighborId);
        }
        vertices.removeAt(vertices.indexOf(vertex));
        return formerNeighbors;
    }

    /** Returns a copy of the direct neighbors of a course (empty if none). */
    public SinglyLinkedList<String> getNeighbors(String courseId) {
        SinglyLinkedList<String> copy = new SinglyLinkedList<>();
        Vertex vertex = find(courseId);
        if (vertex != null) {
            for (String neighborId : vertex.neighbors) {
                copy.addLast(neighborId);
            }
        }
        return copy;
    }

    /** Returns a copy of all course IDs that currently have relationships. */
    public SinglyLinkedList<String> getAllVertices() {
        SinglyLinkedList<String> copy = new SinglyLinkedList<>();
        for (Vertex vertex : vertices) {
            copy.addLast(vertex.courseId);
        }
        return copy;
    }

    /**
     * Breadth-first search: returns every course reachable from the start course
     * (including the start course itself as the first element).
     */
    public SinglyLinkedList<String> breadthFirstSearch(String startId) {
        SinglyLinkedList<String> visited = new SinglyLinkedList<>();
        if (find(startId) == null) {
            return visited;
        }
        visited.addLast(startId);
        int position = 0;
        while (position < visited.size()) {
            String current = visited.get(position);
            position++;
            for (String neighborId : getNeighbors(current)) {
                if (!visited.contains(neighborId)) {
                    visited.addLast(neighborId);
                }
            }
        }
        return visited;
    }

    public boolean isEmpty() {
        return vertices.isEmpty();
    }

    public void clear() {
        vertices.clear();
    }
}
