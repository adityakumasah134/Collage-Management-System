package service;

import datastructures.SinglyLinkedList;
import datastructures.UndoAction;
import datastructures.UndoStack;
import filehandling.FileManager;
import model.Fee;
import model.Student;
import utility.InputHelper;

public class StudentService {

    private final SinglyLinkedList<Student> students;
    private final SinglyLinkedList<Fee> fees;
    private final UndoStack undoStack;
    private final FileManager fileManager;

    public StudentService(SinglyLinkedList<Student> students, SinglyLinkedList<Fee> fees,
                          UndoStack undoStack, FileManager fileManager) {
        this.students = students;
        this.fees = fees;
        this.undoStack = undoStack;
        this.fileManager = fileManager;
    }

    public void menu() {
        while (true) {
            System.out.println();
            System.out.println("========== STUDENT MANAGEMENT ==========");
            System.out.println();
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search Student");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Back to Main Menu");
            System.out.println();
            int choice = InputHelper.readChoice("Enter Your Choice: ");
            System.out.println();
            switch (choice) {
                case 1: addStudent(); break;
                case 2: viewAll(); break;
                case 3: searchStudent(); break;
                case 4: updateStudent(); break;
                case 5: deleteStudent(); break;
                case 6: return;
                default: System.out.println("Invalid choice! Please enter a number from 1 to 6.");
            }
        }
    }

    // ---- helpers used by other services ----

    public boolean exists(String id) {
        return indexOf(id) >= 0;
    }

    public Student findById(String id) {
        int index = indexOf(id);
        return index >= 0 ? students.get(index) : null;
    }

    public SinglyLinkedList<Student> getAll() {
        return students;
    }

    private int indexOf(String id) {
        int index = 0;
        for (Student s : students) {
            if (s.getId().equals(id)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private String generateId() {
        int max = 1000;
        for (Student s : students) {
            max = Math.max(max, InputHelper.extractNumber(s.getId(), "STU"));
        }
        return "STU" + (max + 1);
    }

    private boolean hasFeeRecord(String studentId) {
        for (Fee fee : fees) {
            if (fee.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }

    // ---- operations ----

    private void addStudent() {
        System.out.println("--- Add Student ---");
        String name = InputHelper.readName("Enter Name: ");
        int age = InputHelper.readInt("Enter Age (16-60): ", 16, 60);
        String department = InputHelper.readNonEmpty("Enter Department: ");
        String email = InputHelper.readEmail("Enter Email: ");
        String phone = InputHelper.readPhone("Enter Phone (10 digits): ");

        Student student = new Student(generateId(), name, age, department, email, phone);
        students.addLast(student);

        final String id = student.getId();
        undoStack.push(new UndoAction("Added student " + id, () -> {
            int index = indexOf(id);
            if (index >= 0) {
                students.removeAt(index);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Student added with ID " + id + ".");
    }

    private void viewAll() {
        System.out.println("--- All Students ---");
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        System.out.println(Student.tableHeader());
        System.out.println("-".repeat(96));
        for (Student s : students) {
            System.out.println(s);
        }
        System.out.println("Total students: " + students.size());
    }

    private void searchStudent() {
        System.out.println("--- Search Student ---");
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        String id = InputHelper.readId("Enter Student ID: ");
        Student student = findById(id);
        if (student == null) {
            System.out.println("Error: No student found with ID " + id + ".");
            return;
        }
        System.out.println(Student.tableHeader());
        System.out.println("-".repeat(96));
        System.out.println(student);
    }

    private void updateStudent() {
        System.out.println("--- Update Student ---");
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        String id = InputHelper.readId("Enter Student ID to update: ");
        Student student = findById(id);
        if (student == null) {
            System.out.println("Error: No student found with ID " + id + ".");
            return;
        }
        System.out.println("Press Enter to keep the current value.");
        Student before = student.copy();

        String name = InputHelper.readOptionalName("Name [" + student.getName() + "]: ");
        Integer age = InputHelper.readOptionalInt("Age [" + student.getAge() + "]: ", 16, 60);
        String department = InputHelper.readOptional("Department [" + student.getDepartment() + "]: ");
        String email = InputHelper.readOptionalEmail("Email [" + student.getEmail() + "]: ");
        String phone = InputHelper.readOptionalPhone("Phone [" + student.getPhone() + "]: ");

        if (!name.isEmpty()) student.setName(name);
        if (age != null) student.setAge(age);
        if (!department.isEmpty()) student.setDepartment(department);
        if (!email.isEmpty()) student.setEmail(email);
        if (!phone.isEmpty()) student.setPhone(phone);

        undoStack.push(new UndoAction("Updated student " + id, () -> {
            Student current = findById(id);
            if (current != null) {
                current.copyFrom(before);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Student " + id + " updated.");
    }

    private void deleteStudent() {
        System.out.println("--- Delete Student ---");
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        String id = InputHelper.readId("Enter Student ID to delete: ");
        int index = indexOf(id);
        if (index < 0) {
            System.out.println("Error: No student found with ID " + id + ".");
            return;
        }
        if (hasFeeRecord(id)) {
            System.out.println("Error: Student " + id + " has a fee record. Delete the fee record first.");
            return;
        }
        if (!InputHelper.confirm("Delete student " + students.get(index).getName() + " (" + id + ")?")) {
            System.out.println("Delete cancelled.");
            return;
        }
        final Student removed = students.removeAt(index);
        final int position = index;
        undoStack.push(new UndoAction("Deleted student " + id, () ->
                students.addAt(Math.min(position, students.size()), removed)));
        fileManager.saveAll();
        System.out.println("Success: Student " + id + " deleted.");
    }
}
