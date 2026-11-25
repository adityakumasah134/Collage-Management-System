package model;

public class Faculty {

    private String id;
    private String name;
    private String department;
    private String designation;
    private String email;
    private String phone;

    public Faculty(String id, String name, String department, String designation, String email, String phone) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.designation = designation;
        this.email = email;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public Faculty copy() {
        return new Faculty(id, name, department, designation, email, phone);
    }

    public void copyFrom(Faculty other) {
        this.name = other.name;
        this.department = other.department;
        this.designation = other.designation;
        this.email = other.email;
        this.phone = other.phone;
    }

    public String toFileString() {
        return id + "|" + name + "|" + department + "|" + designation + "|" + email + "|" + phone;
    }

    public static Faculty fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 6 || p[0].isEmpty()) {
            return null;
        }
        return new Faculty(p[0], p[1], p[2], p[3], p[4], p[5]);
    }

    public static String tableHeader() {
        return String.format("%-8s %-22s %-18s %-20s %-28s %-11s",
                "ID", "Name", "Department", "Designation", "Email", "Phone");
    }

    @Override
    public String toString() {
        return String.format("%-8s %-22s %-18s %-20s %-28s %-11s",
                id, name, department, designation, email, phone);
    }
}
