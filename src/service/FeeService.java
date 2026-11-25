package service;

import datastructures.SinglyLinkedList;
import datastructures.UndoAction;
import datastructures.UndoStack;
import filehandling.FileManager;
import model.Fee;
import model.Student;
import utility.InputHelper;

public class FeeService {

    private static final double MAX_FEE = 10000000.0;

    private final SinglyLinkedList<Fee> fees;
    private final StudentService studentService;
    private final UndoStack undoStack;
    private final FileManager fileManager;

    public FeeService(SinglyLinkedList<Fee> fees, StudentService studentService,
                      UndoStack undoStack, FileManager fileManager) {
        this.fees = fees;
        this.studentService = studentService;
        this.undoStack = undoStack;
        this.fileManager = fileManager;
    }

    public void menu() {
        while (true) {
            System.out.println();
            System.out.println("========== FEE MANAGEMENT ==========");
            System.out.println();
            System.out.println("1. Add Student Fee");
            System.out.println("2. View Fee Details");
            System.out.println("3. Pay Fee");
            System.out.println("4. Check Pending Fee");
            System.out.println("5. Check Paid Fee");
            System.out.println("6. Search Fee by Student ID");
            System.out.println("7. Update Fee");
            System.out.println("8. Delete Fee Record");
            System.out.println("9. Back to Main Menu");
            System.out.println();
            int choice = InputHelper.readChoice("Enter Your Choice: ");
            System.out.println();
            switch (choice) {
                case 1: addFee(); break;
                case 2: viewAll(); break;
                case 3: payFee(); break;
                case 4: checkPending(); break;
                case 5: checkPaid(); break;
                case 6: searchFee(); break;
                case 7: updateFee(); break;
                case 8: deleteFee(); break;
                case 9: return;
                default: System.out.println("Invalid choice! Please enter a number from 1 to 9.");
            }
        }
    }

    private int indexOf(String studentId) {
        int index = 0;
        for (Fee f : fees) {
            if (f.getStudentId().equals(studentId)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private Fee findByStudentId(String studentId) {
        int index = indexOf(studentId);
        return index >= 0 ? fees.get(index) : null;
    }

    private void printHeader() {
        System.out.println(Fee.tableHeader());
        System.out.println("-".repeat(60));
    }

    private void addFee() {
        System.out.println("--- Add Student Fee ---");
        String studentId = InputHelper.readId("Enter Student ID: ");
        Student student = studentService.findById(studentId);
        if (student == null) {
            System.out.println("Error: No student found with ID " + studentId + ".");
            return;
        }
        if (findByStudentId(studentId) != null) {
            System.out.println("Error: A fee record already exists for " + studentId + ". Use Update Fee instead.");
            return;
        }
        System.out.println("Student: " + student.getName());
        double total = InputHelper.readAmount("Enter Total Fee: ", 1, MAX_FEE);

        fees.addLast(new Fee(studentId, total, 0));
        undoStack.push(new UndoAction("Added fee record for " + studentId, () -> {
            int index = indexOf(studentId);
            if (index >= 0) {
                fees.removeAt(index);
            }
        }));
        fileManager.saveAll();
        System.out.println("Success: Fee record added for " + studentId + " (Status: UNPAID).");
    }

    private void viewAll() {
        System.out.println("--- Fee Details ---");
        if (fees.isEmpty()) {
            System.out.println("No fee records found.");
            return;
        }
        printHeader();
        for (Fee f : fees) {
            System.out.println(f);
        }
        System.out.println("Total fee records: " + fees.size());
    }

    private void payFee() {
        System.out.println("--- Pay Fee ---");
        if (fees.isEmpty()) {
            System.out.println("No fee records found.");
            return;
        }
        String studentId = InputHelper.readId("Enter Student ID: ");
        Fee fee = findByStudentId(studentId);
        if (fee == null) {
            System.out.println("Error: No fee record found for student " + studentId + ".");
            return;
        }
        if (fee.getRemainingFee() <= 0) {
            System.out.println("This fee is already fully paid.");
            return;
        }
        System.out.printf("Total: %.2f | Paid: %.2f | Remaining: %.2f%n",
                fee.getTotalFee(), fee.getPaidFee(), fee.getRemainingFee());
        double amount = InputHelper.readAmount("Enter Amount to Pay: ", 0.01, fee.getRemainingFee());

        Fee before = fee.copy();
        fee.pay(amount);

        undoStack.push(new UndoAction("Fee payment of " + String.format("%.2f", amount) + " for " + studentId, () -> {
            Fee current = findByStudentId(studentId);
            if (current != null) {
                current.copyFrom(before);
            }
        }));
        fileManager.saveAll();
        System.out.printf("Success: Payment of %.2f received.%n", amount);
        System.out.printf("Paid: %.2f | Remaining: %.2f | Status: %s%n",
                fee.getPaidFee(), fee.getRemainingFee(), fee.getStatus());
    }

    private void checkPending() {
        System.out.println("--- Pending Fees (UNPAID and PARTIAL) ---");
        int count = 0;
        double totalPending = 0;
        for (Fee f : fees) {
            if (!f.getStatus().equals(Fee.PAID)) {
                if (count == 0) {
                    printHeader();
                }
                System.out.println(f);
                totalPending += f.getRemainingFee();
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No pending fees.");
        } else {
            System.out.printf("Students with pending fee: %d | Total pending amount: %.2f%n", count, totalPending);
        }
    }

    private void checkPaid() {
        System.out.println("--- Fully Paid Fees ---");
        int count = 0;
        for (Fee f : fees) {
            if (f.getStatus().equals(Fee.PAID)) {
                if (count == 0) {
                    printHeader();
                }
                System.out.println(f);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No fully paid fee records.");
        } else {
            System.out.println("Fully paid records: " + count);
        }
    }

    private void searchFee() {
        System.out.println("--- Search Fee by Student ID ---");
        if (fees.isEmpty()) {
            System.out.println("No fee records found.");
            return;
        }
        String studentId = InputHelper.readId("Enter Student ID: ");
        Fee fee = findByStudentId(studentId);
        if (fee == null) {
            System.out.println("Error: No fee record found for student " + studentId + ".");
            return;
        }
        printHeader();
        System.out.println(fee);
    }

    private void updateFee() {
        System.out.println("--- Update Fee ---");
        if (fees.isEmpty()) {
            System.out.println("No fee records found.");
            return;
        }
        String studentId = InputHelper.readId("Enter Student ID: ");
        Fee fee = findByStudentId(studentId);
        if (fee == null) {
            System.out.println("Error: No fee record found for student " + studentId + ".");
            return;
        }
        System.out.printf("Current Total Fee: %.2f | Paid: %.2f%n", fee.getTotalFee(), fee.getPaidFee());
        double newTotal = InputHelper.readAmount("Enter New Total Fee: ", 1, MAX_FEE);
        if (newTotal < fee.getPaidFee()) {
            System.out.printf("Error: New total fee cannot be less than the amount already paid (%.2f).%n", fee.getPaidFee());
            return;
        }
        Fee before = fee.copy();
        fee.setTotalFee(newTotal);

        undoStack.push(new UndoAction("Updated fee for " + studentId, () -> {
            Fee current = findByStudentId(studentId);
            if (current != null) {
                current.copyFrom(before);
            }
        }));
        fileManager.saveAll();
        System.out.printf("Success: Fee updated. Remaining: %.2f | Status: %s%n",
                fee.getRemainingFee(), fee.getStatus());
    }

    private void deleteFee() {
        System.out.println("--- Delete Fee Record ---");
        if (fees.isEmpty()) {
            System.out.println("No fee records found.");
            return;
        }
        String studentId = InputHelper.readId("Enter Student ID: ");
        int index = indexOf(studentId);
        if (index < 0) {
            System.out.println("Error: No fee record found for student " + studentId + ".");
            return;
        }
        if (!InputHelper.confirm("Delete the fee record of " + studentId + "?")) {
            System.out.println("Delete cancelled.");
            return;
        }
        final Fee removed = fees.removeAt(index);
        final int position = index;
        undoStack.push(new UndoAction("Deleted fee record of " + studentId, () ->
                fees.addAt(Math.min(position, fees.size()), removed)));
        fileManager.saveAll();
        System.out.println("Success: Fee record of " + studentId + " deleted.");
    }
}
