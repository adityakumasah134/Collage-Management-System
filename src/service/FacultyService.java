package service;

import datastructures.SinglyLinkedList;
import datastructures.UndoAction;
import datastructures.UndoStack;
import filehandling.FileManager;
import model.Course;
import model.Faculty;
import utility.InputHelper;

public class FacultyService {

    private final SinglyLinkedList<Faculty> facultyList;
    private final SinglyLinkedList<Course> courses;
    private final UndoStack undoStack;
    private final FileManager fileManager;

    public FacultyService(SinglyLinkedList<Faculty> facultyList, SinglyLinkedList<Course> courses,
                          UndoStack undoStack, FileManager fileManager) {
        this.facultyList = facultyList;
        this.courses = courses;
        this.undoStack = undoStack;
        this.fileManager = fileManager;
    }

    public void menu() {
        while (true) {
            System.out.println();
            System.out.println("========== FACULTY MANAGEMENT ==========");
            System.out.println();
            System.out.println("1. Add Faculty");
            System.out.println("2. View All Faculty");
            System.out.println("3. Search Faculty");
            System.out.println("4. Update Faculty");
            System.out.println("5. Delete Faculty");
            System.out.println("6. Back to Main Menu");
            System.out.println();
            int choice = InputHelper.readChoice("Enter Your Choice: ");
            System.out.println();
            switch (choice) {
                case 1: addFaculty(); break;
                case 2: viewAll(); break;
                case 3: searchFaculty(); break;
                case 4: updateFaculty(); break;
                case 5: deleteFaculty(); break;
                case 6: return;
                default: System.out.println("Invalid choice! Please enter a number from 1 to 6.");
            }
        }
    }

    public boolean exists(String id) {
        return indexOf(id) >= 0;
    }

    public Faculty findById(String id) {
        int index = indexOf(id);
        return index >= 0 ? facultyList.get(index) : null;
    }

    private int indexOf(String id) {
        int index = 0;
        for (Faculty f : facultyList) {
            if (f.getId().equals(id)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private String generateId() {
        int max = 100;
        for (Faculty f : facultyList) {
            max = Math.max(max, InputHelper.extractNumber(f.getId(), "FAC"));
        }
        return "FAC" + (max + 1);
    }

    private boolean teachesCourse(String facultyId) {
        for (Course course : courses) {
            if (course.getFacultyId().equals(facultyId)) {
                return true;
            }
        }
        return false;
    }

    private void addFaculty() {
        System.out.println("--- Add Faculty ---");
        String name = InputHelper.readName("Enter Name: ");
        String department = InputHelper.readNonEmpty("Enter Department: ");
        String designation = InputHelper.readNonEmpty("Enter Designation (e.g. Professor): ");
        String email = InputHelper.readEmail("Enter Email: ");
        String phone = InputHelper.readPhone("Enter Phone (10 digits): ");

        Faculty member = new Faculty(generateId(), name, department, designation, email, phone);
        facultyList.addLast(member);

        final String id = member.getId();
        undoStack.push(new UndoAction("Added faculty " + id, () -> {
            int index = indexOf(id);
            if (index >= 0) {
                facultyList.removeAt(index);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Faculty added with ID " + id + ".");
    }

    private void viewAll() {
        System.out.println("--- All Faculty ---");
        if (facultyList.isEmpty()) {
            System.out.println("No faculty records found.");
            return;
        }
        System.out.println(Faculty.tableHeader());
        System.out.println("-".repeat(112));
        for (Faculty f : facultyList) {
            System.out.println(f);
        }
        System.out.println("Total faculty: " + facultyList.size());
    }

    private void searchFaculty() {
        System.out.println("--- Search Faculty ---");
        if (facultyList.isEmpty()) {
            System.out.println("No faculty records found.");
            return;
        }
        String id = InputHelper.readId("Enter Faculty ID: ");
        Faculty member = findById(id);
        if (member == null) {
            System.out.println("Error: No faculty found with ID " + id + ".");
            return;
        }
        System.out.println(Faculty.tableHeader());
        System.out.println("-".repeat(112));
        System.out.println(member);
    }

    private void updateFaculty() {
        System.out.println("--- Update Faculty ---");
        if (facultyList.isEmpty()) {
            System.out.println("No faculty records found.");
            return;
        }
        String id = InputHelper.readId("Enter Faculty ID to update: ");
        Faculty member = findById(id);
        if (member == null) {
            System.out.println("Error: No faculty found with ID " + id + ".");
            return;
        }
        System.out.println("Press Enter to keep the current value.");
        Faculty before = member.copy();

        String name = InputHelper.readOptionalName("Name [" + member.getName() + "]: ");
        String department = InputHelper.readOptional("Department [" + member.getDepartment() + "]: ");
        String designation = InputHelper.readOptional("Designation [" + member.getDesignation() + "]: ");
        String email = InputHelper.readOptionalEmail("Email [" + member.getEmail() + "]: ");
        String phone = InputHelper.readOptionalPhone("Phone [" + member.getPhone() + "]: ");

        if (!name.isEmpty()) member.setName(name);
        if (!department.isEmpty()) member.setDepartment(department);
        if (!designation.isEmpty()) member.setDesignation(designation);
        if (!email.isEmpty()) member.setEmail(email);
        if (!phone.isEmpty()) member.setPhone(phone);

        undoStack.push(new UndoAction("Updated faculty " + id, () -> {
            Faculty current = findById(id);
            if (current != null) {
                current.copyFrom(before);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Faculty " + id + " updated.");
    }

    private void deleteFaculty() {
        System.out.println("--- Delete Faculty ---");
        if (facultyList.isEmpty()) {
            System.out.println("No faculty records found.");
            return;
        }
        String id = InputHelper.readId("Enter Faculty ID to delete: ");
        int index = indexOf(id);
        if (index < 0) {
            System.out.println("Error: No faculty found with ID " + id + ".");
            return;
        }
        if (teachesCourse(id)) {
            System.out.println("Error: Faculty " + id + " is assigned to a course. Reassign or delete that course first.");
            return;
        }
        if (!InputHelper.confirm("Delete faculty " + facultyList.get(index).getName() + " (" + id + ")?")) {
            System.out.println("Delete cancelled.");
            return;
        }
        final Faculty removed = facultyList.removeAt(index);
        final int position = index;
        undoStack.push(new UndoAction("Deleted faculty " + id, () ->
                facultyList.addAt(Math.min(position, facultyList.size()), removed)));
        fileManager.saveAll();
        System.out.println("Success: Faculty " + id + " deleted.");
    }
}
