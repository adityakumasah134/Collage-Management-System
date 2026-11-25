package model;

public class Fee {

    public static final String PAID = "PAID";
    public static final String PARTIAL = "PARTIAL";
    public static final String UNPAID = "UNPAID";

    private String studentId;
    private double totalFee;
    private double paidFee;

    public Fee(String studentId, double totalFee, double paidFee) {
        this.studentId = studentId;
        this.totalFee = round(totalFee);
        this.paidFee = round(paidFee);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getStudentId() { return studentId; }
    public double getTotalFee() { return totalFee; }
    public double getPaidFee() { return paidFee; }

    /** Remaining fee is always calculated, never entered by hand. */
    public double getRemainingFee() {
        return Math.max(0, round(totalFee - paidFee));
    }

    /** Payment status is always calculated from the paid and remaining amounts. */
    public String getStatus() {
        if (paidFee <= 0) {
            return UNPAID;
        }
        if (getRemainingFee() <= 0) {
            return PAID;
        }
        return PARTIAL;
    }

    public void setTotalFee(double totalFee) { this.totalFee = round(totalFee); }

    public void pay(double amount) { this.paidFee = round(this.paidFee + amount); }

    public Fee copy() {
        return new Fee(studentId, totalFee, paidFee);
    }

    public void copyFrom(Fee other) {
        this.totalFee = other.totalFee;
        this.paidFee = other.paidFee;
    }

    public String toFileString() {
        return studentId + "|" + String.format("%.2f", totalFee) + "|" + String.format("%.2f", paidFee)
                + "|" + String.format("%.2f", getRemainingFee()) + "|" + getStatus();
    }

    /** Only student ID, total and paid are read; remaining and status are recalculated. */
    public static Fee fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 3 || p[0].isEmpty()) {
            return null;
        }
        try {
            double total = Double.parseDouble(p[1].trim());
            double paid = Double.parseDouble(p[2].trim());
            if (total < 0 || paid < 0) {
                return null;
            }
            return new Fee(p[0], total, paid);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String tableHeader() {
        return String.format("%-10s %-12s %-12s %-12s %-8s",
                "Student ID", "Total Fee", "Paid Fee", "Remaining", "Status");
    }

    @Override
    public String toString() {
        return String.format("%-10s %-12.2f %-12.2f %-12.2f %-8s",
                studentId, totalFee, paidFee, getRemainingFee(), getStatus());
    }
}
