package service;

import datastructures.CourseGraph;
import datastructures.SinglyLinkedList;
import datastructures.UndoAction;
import datastructures.UndoStack;
import filehandling.FileManager;
import model.Course;
import utility.InputHelper;

public class CourseService {

    private final SinglyLinkedList<Course> courses;
    private final FacultyService facultyService;
    private final CourseGraph graph;
    private final UndoStack undoStack;
    private final FileManager fileManager;

    public CourseService(SinglyLinkedList<Course> courses, FacultyService facultyService,
                         CourseGraph graph, UndoStack undoStack, FileManager fileManager) {
        this.courses = courses;
        this.facultyService = facultyService;
        this.graph = graph;
        this.undoStack = undoStack;
        this.fileManager = fileManager;
    }

    public void menu() {
        while (true) {
            System.out.println();
            System.out.println("========== COURSE MANAGEMENT ==========");
            System.out.println();
            System.out.println("1. Add Course");
            System.out.println("2. View All Courses");
            System.out.println("3. Search Course");
            System.out.println("4. Update Course");
            System.out.println("5. Delete Course");
            System.out.println("6. Back to Main Menu");
            System.out.println();
            int choice = InputHelper.readChoice("Enter Your Choice: ");
            System.out.println();
            switch (choice) {
                case 1: addCourse(); break;
                case 2: viewAll(); break;
                case 3: searchCourse(); break;
                case 4: updateCourse(); break;
                case 5: deleteCourse(); break;
                case 6: return;
                default: System.out.println("Invalid choice! Please enter a number from 1 to 6.");
            }
        }
    }

    public boolean exists(String id) {
        return indexOf(id) >= 0;
    }

    public Course findById(String id) {
        int index = indexOf(id);
        return index >= 0 ? courses.get(index) : null;
    }

    /** Returns the course name, or "Unknown course" if the ID does not exist. */
    public String getName(String id) {
        Course course = findById(id);
        return course == null ? "Unknown course" : course.getName();
    }

    private int indexOf(String id) {
        int index = 0;
        for (Course c : courses) {
            if (c.getId().equals(id)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private String generateId() {
        int max = 100;
        for (Course c : courses) {
            max = Math.max(max, InputHelper.extractNumber(c.getId(), "CRS"));
        }
        return "CRS" + (max + 1);
    }

    private void addCourse() {
        System.out.println("--- Add Course ---");
        String name = InputHelper.readNonEmpty("Enter Course Name: ");
        int credits = InputHelper.readInt("Enter Credits (1-10): ", 1, 10);
        String department = InputHelper.readNonEmpty("Enter Department: ");
        String facultyId = readFacultyIdForAdd();

        Course course = new Course(generateId(), name, credits, department, facultyId);
        courses.addLast(course);

        final String id = course.getId();
        undoStack.push(new UndoAction("Added course " + id, () -> {
            int index = indexOf(id);
            if (index >= 0) {
                courses.removeAt(index);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Course added with ID " + id + ".");
    }

    private String readFacultyIdForAdd() {
        while (true) {
            String id = InputHelper.readOptional("Enter Faculty ID (press Enter to skip): ").toUpperCase();
            if (id.isEmpty() || id.equals(Course.NO_FACULTY)) {
                return Course.NO_FACULTY;
            }
            if (facultyService.exists(id)) {
                return id;
            }
            System.out.println("Error: No faculty found with ID " + id + ". Try again or press Enter to skip.");
        }
    }

    private void viewAll() {
        System.out.println("--- All Courses ---");
        if (courses.isEmpty()) {
            System.out.println("No course records found.");
            return;
        }
        System.out.println(Course.tableHeader());
        System.out.println("-".repeat(76));
        for (Course c : courses) {
            System.out.println(c);
        }
        System.out.println("Total courses: " + courses.size());
    }

    private void searchCourse() {
        System.out.println("--- Search Course ---");
        if (courses.isEmpty()) {
            System.out.println("No course records found.");
            return;
        }
        String keyword = InputHelper.readNonEmpty("Enter Course ID or part of the Course Name: ");
        String lower = keyword.toLowerCase();
        int found = 0;
        for (Course c : courses) {
            if (c.getId().equalsIgnoreCase(keyword) || c.getName().toLowerCase().contains(lower)) {
                if (found == 0) {
                    System.out.println(Course.tableHeader());
                    System.out.println("-".repeat(76));
                }
                System.out.println(c);
                found++;
            }
        }
        if (found == 0) {
            System.out.println("Error: No course found matching \"" + keyword + "\".");
        }
    }

    private void updateCourse() {
        System.out.println("--- Update Course ---");
        if (courses.isEmpty()) {
            System.out.println("No course records found.");
            return;
        }
        String id = InputHelper.readId("Enter Course ID to update: ");
        Course course = findById(id);
        if (course == null) {
            System.out.println("Error: No course found with ID " + id + ".");
            return;
        }
        System.out.println("Press Enter to keep the current value.");
        Course before = course.copy();

        String name = InputHelper.readOptional("Course Name [" + course.getName() + "]: ");
        Integer credits = InputHelper.readOptionalInt("Credits [" + course.getCredits() + "]: ", 1, 10);
        String department = InputHelper.readOptional("Department [" + course.getDepartment() + "]: ");
        String facultyId = readFacultyIdForUpdate(course.getFacultyId());

        if (!name.isEmpty()) course.setName(name);
        if (credits != null) course.setCredits(credits);
        if (!department.isEmpty()) course.setDepartment(department);
        course.setFacultyId(facultyId);

        undoStack.push(new UndoAction("Updated course " + id, () -> {
            Course current = findById(id);
            if (current != null) {
                current.copyFrom(before);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Course " + id + " updated.");
    }

    private String readFacultyIdForUpdate(String currentId) {
        while (true) {
            String id = InputHelper.readOptional("Faculty ID [" + currentId + "] (type NONE to unassign): ").toUpperCase();
            if (id.isEmpty()) {
                return currentId;
            }
            if (id.equals(Course.NO_FACULTY) || facultyService.exists(id)) {
                return id;
            }
            System.out.println("Error: No faculty found with ID " + id + ".");
        }
    }

    private void deleteCourse() {
        System.out.println("--- Delete Course ---");
        if (courses.isEmpty()) {
            System.out.println("No course records found.");
            return;
        }
        String id = InputHelper.readId("Enter Course ID to delete: ");
        int index = indexOf(id);
        if (index < 0) {
            System.out.println("Error: No course found with ID " + id + ".");
            return;
        }
        if (!InputHelper.confirm("Delete course " + courses.get(index).getName() + " (" + id + ")?")) {
            System.out.println("Delete cancelled.");
            return;
        }
        final Course removed = courses.removeAt(index);
        final int position = index;
        // Remember which courses it was related to, so Undo can restore those relationships too.
        final SinglyLinkedList<String> relatedCourses = graph.removeCourse(id);

        undoStack.push(new UndoAction("Deleted course " + id, () -> {
            courses.addAt(Math.min(position, courses.size()), removed);
            for (String relatedId : relatedCourses) {
                graph.addEdge(id, relatedId);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Course " + id + " deleted.");
        if (!relatedCourses.isEmpty()) {
            System.out.println("Its course relationships were removed as well.");
        }
    }
}
