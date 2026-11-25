package service;

import java.time.LocalDate;

/** A single service request (e.g. bonafide certificate, ID card) raised by a student. */
public class ServiceRequest {

    private final String requestId;
    private final String studentId;
    private final String description;
    private final String date;

    public ServiceRequest(String requestId, String studentId, String description, String date) {
        this.requestId = requestId;
        this.studentId = studentId;
        this.description = description;
        this.date = date;
    }

    public ServiceRequest(String requestId, String studentId, String description) {
        this(requestId, studentId, description, LocalDate.now().toString());
    }

    public String getRequestId() { return requestId; }
    public String getStudentId() { return studentId; }
    public String getDescription() { return description; }
    public String getDate() { return date; }

    public String toFileString() {
        return requestId + "|" + studentId + "|" + description + "|" + date;
    }

    public static ServiceRequest fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 4 || p[0].isEmpty()) {
            return null;
        }
        return new ServiceRequest(p[0], p[1], p[2], p[3]);
    }

    public static String tableHeader() {
        return String.format("%-9s %-10s %-40s %-10s", "Request", "Student", "Description", "Date");
    }

    @Override
    public String toString() {
        return String.format("%-9s %-10s %-40s %-10s", requestId, studentId, description, date);
    }
}
