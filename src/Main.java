import datastructures.CourseGraph;
import datastructures.RequestQueue;
import datastructures.SinglyLinkedList;
import datastructures.UndoAction;
import datastructures.UndoStack;
import filehandling.FileManager;
import model.Course;
import model.Faculty;
import model.Fee;
import model.Student;
import service.CourseService;
import service.FacultyService;
import service.FeeService;
import service.ServiceRequest;
import service.StudentService;
import utility.InputHelper;

public class Main {

    private static final String DATA_FILE = "data";

    // Linked Lists for records
    private static final SinglyLinkedList<Student> students = new SinglyLinkedList<>();
    private static final SinglyLinkedList<Faculty> facultyList = new SinglyLinkedList<>();
    private static final SinglyLinkedList<Course> courses = new SinglyLinkedList<>();
    private static final SinglyLinkedList<Fee> fees = new SinglyLinkedList<>();

    // Graph, Queue and Stack
    private static final CourseGraph courseGraph = new CourseGraph();
    private static final RequestQueue requestQueue = new RequestQueue();
    private static final UndoStack undoStack = new UndoStack();

    private static FileManager fileManager;
    private static StudentService studentService;
    private static FacultyService facultyService;
    private static CourseService courseService;
    private static FeeService feeService;

    public static void main(String[] args) {
        fileManager = new FileManager(DATA_FILE, students, facultyList, courses, fees, courseGraph, requestQueue);
        fileManager.load();

        studentService = new StudentService(students, fees, undoStack, fileManager);
        facultyService = new FacultyService(facultyList, courses, undoStack, fileManager);
        courseService = new CourseService(courses, facultyService, courseGraph, undoStack, fileManager);
        feeService = new FeeService(fees, studentService, undoStack, fileManager);

        try {
            runMainMenu();
        } catch (InputHelper.InputClosedException e) {
            fileManager.saveAll();
            System.out.println();
            System.out.println("Input closed. All data is saved. Goodbye!");
        }
    }

    private static void runMainMenu() {
        boolean firstTime = true;
        while (true) {
            if (!firstTime) {
                System.out.println();
            }
            firstTime = false;
            printMainMenu();
            int choice = InputHelper.readChoice("Enter Your Choice: ");
            switch (choice) {
                case 1: studentService.menu(); break;
                case 2: facultyService.menu(); break;
                case 3: courseService.menu(); break;
                case 4: feeService.menu(); break;
                case 5: courseRelationshipMenu(); break;
                case 6: serviceRequestMenu(); break;
                case 7: undoLastOperation(); break;
                case 8: displayAllRecords(); break;
                case 9:
                    fileManager.saveAll();
                    System.out.println("All records have been saved to the data folder.");
                    break;
                case 10:
                    fileManager.saveAll();
                    System.out.println();
                    System.out.println("All data saved. Thank you for using the College Management System. Goodbye!");
                    return;
                default:
                    System.out.println();
                    System.out.println("Invalid choice! Please enter a number from 1 to 10.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("========================================");
        System.out.println("       COLLEGE MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println();
        System.out.println("1. Student Management");
        System.out.println("2. Faculty Management");
        System.out.println("3. Course Management");
        System.out.println("4. Fee Management");
        System.out.println("5. Course Relationships");
        System.out.println("6. Service Requests");
        System.out.println("7. Undo Last Operation");
        System.out.println("8. Display All Records");
        System.out.println("9. Save Data Now");
        System.out.println("10. Exit");
        System.out.println();
    }

    // ------------------------------------------------------------ UNDO (Stack)

    private static void undoLastOperation() {
        System.out.println();
        System.out.println("--- Undo Last Operation ---");
        UndoAction action = undoStack.pop();
        if (action == null) {
            System.out.println("Nothing to undo. The undo stack is empty.");
            return;
        }
        action.execute();
        fileManager.saveAll();
        System.out.println("Success: Undone -> " + action.getDescription());
        System.out.println("Operations still available to undo: " + undoStack.size());
    }

    // ------------------------------------------------------------ GRAPH

    private static void courseRelationshipMenu() {
        while (true) {
            System.out.println();
            System.out.println("========== COURSE RELATIONSHIPS ==========");
            System.out.println();
            System.out.println("1. Add Course Relationship");
            System.out.println("2. Display Course Relationships");
            System.out.println("3. Search Connected Courses");
            System.out.println("4. Back to Main Menu");
            System.out.println();
            int choice = InputHelper.readChoice("Enter Your Choice: ");
            System.out.println();
            switch (choice) {
                case 1: addRelationship(); break;
                case 2: displayRelationships(); break;
                case 3: searchConnectedCourses(); break;
                case 4: return;
                default: System.out.println("Invalid choice! Please enter a number from 1 to 4.");
            }
        }
    }

    private static void addRelationship() {
        System.out.println("--- Add Course Relationship ---");
        if (courses.isEmpty()) {
            System.out.println("No courses available. Add courses first.");
            return;
        }
        String first = InputHelper.readId("Enter First Course ID: ");
        if (!courseService.exists(first)) {
            System.out.println("Error: No course found with ID " + first + ".");
            return;
        }
        String second = InputHelper.readId("Enter Second Course ID: ");
        if (!courseService.exists(second)) {
            System.out.println("Error: No course found with ID " + second + ".");
            return;
        }
        if (first.equals(second)) {
            System.out.println("Error: A course cannot be related to itself.");
            return;
        }
        if (!courseGraph.addEdge(first, second)) {
            System.out.println("Error: These courses are already connected.");
            return;
        }
        undoStack.push(new UndoAction("Added relationship " + first + " <-> " + second,
                () -> courseGraph.removeEdge(first, second)));
        fileManager.saveAll();
        System.out.println("Success: " + courseService.getName(first) + " is now connected to "
                + courseService.getName(second) + ".");
    }

    private static void displayRelationships() {
        System.out.println("--- Course Relationships ---");
        if (courseGraph.isEmpty()) {
            System.out.println("No course relationships found.");
            return;
        }
        for (String courseId : courseGraph.getAllVertices()) {
            StringBuilder line = new StringBuilder();
            line.append(courseId).append(" (").append(courseService.getName(courseId)).append(") -> ");
            boolean first = true;
            for (String neighborId : courseGraph.getNeighbors(courseId)) {
                if (!first) {
                    line.append(", ");
                }
                line.append(neighborId).append(" (").append(courseService.getName(neighborId)).append(")");
                first = false;
            }
            System.out.println(line);
        }
    }

    private static void searchConnectedCourses() {
        System.out.println("--- Search Connected Courses ---");
        if (courses.isEmpty()) {
            System.out.println("No courses available.");
            return;
        }
        String id = InputHelper.readId("Enter Course ID: ");
        if (!courseService.exists(id)) {
            System.out.println("Error: No course found with ID " + id + ".");
            return;
        }
        SinglyLinkedList<String> direct = courseGraph.getNeighbors(id);
        if (direct.isEmpty()) {
            System.out.println(id + " (" + courseService.getName(id) + ") has no related courses.");
            return;
        }
        System.out.println("Directly connected to " + id + " (" + courseService.getName(id) + "):");
        for (String neighborId : direct) {
            System.out.println("  - " + neighborId + " (" + courseService.getName(neighborId) + ")");
        }
        boolean printedHeader = false;
        for (String reachableId : courseGraph.breadthFirstSearch(id)) {
            if (!reachableId.equals(id) && !direct.contains(reachableId)) {
                if (!printedHeader) {
                    System.out.println("Indirectly connected (through other courses):");
                    printedHeader = true;
                }
                System.out.println("  - " + reachableId + " (" + courseService.getName(reachableId) + ")");
            }
        }
    }

    // ------------------------------------------------------------ QUEUE

    private static void serviceRequestMenu() {
        while (true) {
            System.out.println();
            System.out.println("========== SERVICE REQUESTS ==========");
            System.out.println();
            System.out.println("1. Add Service Request");
            System.out.println("2. View Pending Requests");
            System.out.println("3. Process Next Request");
            System.out.println("4. Back to Main Menu");
            System.out.println();
            int choice = InputHelper.readChoice("Enter Your Choice: ");
            System.out.println();
            switch (choice) {
                case 1: addServiceRequest(); break;
                case 2: viewPendingRequests(); break;
                case 3: processNextRequest(); break;
                case 4: return;
                default: System.out.println("Invalid choice! Please enter a number from 1 to 4.");
            }
        }
    }

    private static void addServiceRequest() {
        System.out.println("--- Add Service Request ---");
        String studentId = InputHelper.readId("Enter Student ID: ");
        Student student = studentService.findById(studentId);
        if (student == null) {
            System.out.println("Error: No student found with ID " + studentId + ".");
            return;
        }
        String description = InputHelper.readNonEmpty("Enter Request Description: ");
        ServiceRequest request = new ServiceRequest(requestQueue.generateId(), studentId, description);
        requestQueue.enqueue(request);
        fileManager.saveAll();
        System.out.println("Success: Request " + request.getRequestId() + " added for " + student.getName()
                + ". Pending requests: " + requestQueue.size());
    }

    private static void viewPendingRequests() {
        System.out.println("--- Pending Service Requests ---");
        if (requestQueue.isEmpty()) {
            System.out.println("No service requests are pending.");
            return;
        }
        System.out.println(ServiceRequest.tableHeader());
        System.out.println("-".repeat(73));
        for (ServiceRequest request : requestQueue) {
            System.out.println(request);
        }
        System.out.println("Pending requests: " + requestQueue.size() + " (oldest is processed first)");
    }

    private static void processNextRequest() {
        System.out.println("--- Process Next Request ---");
        ServiceRequest request = requestQueue.dequeue();
        if (request == null) {
            System.out.println("No service requests are pending.");
            return;
        }
        fileManager.saveAll();
        System.out.println("Processing request " + request.getRequestId() + " from student "
                + request.getStudentId() + ": " + request.getDescription());
        System.out.println("Success: Request " + request.getRequestId() + " processed. Pending requests left: "
                + requestQueue.size());
    }

    // ------------------------------------------------------------ DISPLAY ALL

    private static void displayAllRecords() {
        System.out.println();
        System.out.println("============== ALL RECORDS ==============");

        System.out.println();
        System.out.println("--- STUDENTS (" + students.size() + ") ---");
        if (students.isEmpty()) {
            System.out.println("No student records.");
        } else {
            System.out.println(Student.tableHeader());
            for (Student s : students) {
                System.out.println(s);
            }
        }

        System.out.println();
        System.out.println("--- FACULTY (" + facultyList.size() + ") ---");
        if (facultyList.isEmpty()) {
            System.out.println("No faculty records.");
        } else {
            System.out.println(Faculty.tableHeader());
            for (Faculty f : facultyList) {
                System.out.println(f);
            }
        }

        System.out.println();
        System.out.println("--- COURSES (" + courses.size() + ") ---");
        if (courses.isEmpty()) {
            System.out.println("No course records.");
        } else {
            System.out.println(Course.tableHeader());
            for (Course c : courses) {
                System.out.println(c);
            }
        }

        System.out.println();
        System.out.println("--- FEES (" + fees.size() + ") ---");
        if (fees.isEmpty()) {
            System.out.println("No fee records.");
        } else {
            System.out.println(Fee.tableHeader());
            for (Fee f : fees) {
                System.out.println(f);
            }
        }

        System.out.println();
        System.out.println("--- COURSE RELATIONSHIPS ---");
        displayRelationships();

        System.out.println();
        System.out.println("--- PENDING SERVICE REQUESTS (" + requestQueue.size() + ") ---");
        if (requestQueue.isEmpty()) {
            System.out.println("No service requests are pending.");
        } else {
            System.out.println(ServiceRequest.tableHeader());
            for (ServiceRequest request : requestQueue) {
                System.out.println(request);
            }
        }
    }
}
