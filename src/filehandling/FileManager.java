package filehandling;

import datastructures.CourseGraph;
import datastructures.RequestQueue;
import datastructures.SinglyLinkedList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import model.Course;
import model.Faculty;
import model.Fee;
import model.Student;
import service.ServiceRequest;

/**
 * Stores each module in its own text file inside the data directory.
 * Every save rewrites all files, keeping the in-memory records synchronized.
 */
public class FileManager {
    private final Path dataDirectory;
    private final SinglyLinkedList<Student> students;
    private final SinglyLinkedList<Faculty> faculty;
    private final SinglyLinkedList<Course> courses;
    private final SinglyLinkedList<Fee> fees;
    private final CourseGraph graph;
    private final RequestQueue requests;

    public FileManager(String path,
                       SinglyLinkedList<Student> students,
                       SinglyLinkedList<Faculty> faculty,
                       SinglyLinkedList<Course> courses,
                       SinglyLinkedList<Fee> fees,
                       CourseGraph graph,
                       RequestQueue requests) {
        this.dataDirectory = Paths.get(path);
        this.students = students;
        this.faculty = faculty;
        this.courses = courses;
        this.fees = fees;
        this.graph = graph;
        this.requests = requests;
    }

    public void load() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            System.out.println("ERROR: Could not create data directory: " + e.getMessage());
            return;
        }

        loadStudents();
        loadFaculty();
        loadCourses();
        loadFees();
        loadRelationships();
        loadRequests();
    }

    private List<String> readLines(String filename) {
        Path path = dataDirectory.resolve(filename);
        if (!Files.exists(path)) return java.util.Collections.emptyList();
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Warning: Could not read " + filename + ": " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    private void loadStudents() {
        for (String line : readLines("students.txt")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            Student s = Student.fromFileString(line);
            if (s != null && !findStudent(s.getId())) students.addLast(s);
        }
    }

    private void loadFaculty() {
        for (String line : readLines("faculty.txt")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            Faculty f = Faculty.fromFileString(line);
            if (f != null && !findFaculty(f.getId())) faculty.addLast(f);
        }
    }

    private void loadCourses() {
        for (String line : readLines("courses.txt")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            Course c = Course.fromFileString(line);
            if (c != null && !findCourse(c.getId())) courses.addLast(c);
        }
    }

    private void loadFees() {
        for (String line : readLines("fees.txt")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            Fee f = Fee.fromFileString(line);
            if (f != null && !findFee(f.getStudentId())) fees.addLast(f);
        }
    }

    private void loadRelationships() {
        for (String line : readLines("course_relationships.txt")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\\\|", -1);
            if (p.length >= 2 && !p[0].isEmpty() && !p[1].isEmpty()) {
                graph.addEdge(p[0], p[1]);
            }
        }
    }

    private void loadRequests() {
        for (String line : readLines("service_requests.txt")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("LAST_ID|")) {
                try { requests.setLastIssuedNumber(Integer.parseInt(line.substring(8).trim())); }
                catch (NumberFormatException ignored) {}
                continue;
            }
            ServiceRequest r = ServiceRequest.fromFileString(line);
            if (r != null && !findRequest(r.getRequestId())) {
                requests.enqueue(r);
                requests.setLastIssuedNumber(Math.max(requests.getLastIssuedNumber(), extractRequestNumber(r.getRequestId())));
            }
        }
    }

    public void saveAll() {
        StringBuilder studentText = new StringBuilder("# id|name|age|department|email|phone\n");
        for (Student s : students) studentText.append(s.toFileString()).append('\n');

        StringBuilder facultyText = new StringBuilder("# id|name|department|designation|email|phone\n");
        for (Faculty f : faculty) facultyText.append(f.toFileString()).append('\n');

        StringBuilder courseText = new StringBuilder("# id|name|credits|department|facultyId\n");
        for (Course c : courses) courseText.append(c.toFileString()).append('\n');

        StringBuilder feeText = new StringBuilder("# studentId|totalFee|paidFee|remainingFee|status\n");
        for (Fee f : fees) feeText.append(f.toFileString()).append('\n');

        StringBuilder relationshipText = new StringBuilder("# courseId|relatedCourseId (each relationship once)\n");
        for (String courseId : graph.getAllVertices()) {
            for (String neighborId : graph.getNeighbors(courseId)) {
                if (courseId.compareTo(neighborId) < 0) {
                    relationshipText.append(courseId).append('|').append(neighborId).append('\n');
                }
            }
        }

        StringBuilder requestText = new StringBuilder("# LAST_ID|number; then requestId|studentId|description|date\n");
        requestText.append("LAST_ID|").append(requests.getLastIssuedNumber()).append('\n');
        for (ServiceRequest r : requests) requestText.append(r.toFileString()).append('\n');

        try {
            Files.createDirectories(dataDirectory);
            write("students.txt", studentText.toString());
            write("faculty.txt", facultyText.toString());
            write("courses.txt", courseText.toString());
            write("fees.txt", feeText.toString());
            write("course_relationships.txt", relationshipText.toString());
            write("service_requests.txt", requestText.toString());
            System.out.println("[SAVE] All module files updated in: " + dataDirectory.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("ERROR: Could not save data files: " + e.getMessage());
        }
    }

    private void write(String filename, String content) throws IOException {
        Files.write(dataDirectory.resolve(filename), content.getBytes(StandardCharsets.UTF_8));
    }

    private boolean findStudent(String id) {
        for (Student s : students) if (s.getId().equals(id)) return true;
        return false;
    }
    private boolean findFaculty(String id) {
        for (Faculty f : faculty) if (f.getId().equals(id)) return true;
        return false;
    }
    private boolean findCourse(String id) {
        for (Course c : courses) if (c.getId().equals(id)) return true;
        return false;
    }
    private boolean findFee(String studentId) {
        for (Fee f : fees) if (f.getStudentId().equals(studentId)) return true;
        return false;
    }
    private boolean findRequest(String requestId) {
        for (ServiceRequest r : requests) if (r.getRequestId().equals(requestId)) return true;
        return false;
    }
    private int extractRequestNumber(String requestId) {
        if (requestId.startsWith("REQ")) {
            try { return Integer.parseInt(requestId.substring(3)); }
            catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}
