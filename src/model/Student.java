package model;

public class Student {

    private String id;
    private String name;
    private int age;
    private String department;
    private String email;
    private String phone;

    public Student(String id, String name, int age, String department, String email, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.department = department;
        this.email = email;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setDepartment(String department) { this.department = department; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    /** Returns an independent copy (used to remember the old state for Undo). */
    public Student copy() {
        return new Student(id, name, age, department, email, phone);
    }

    public void copyFrom(Student other) {
        this.name = other.name;
        this.age = other.age;
        this.department = other.department;
        this.email = other.email;
        this.phone = other.phone;
    }

    public String toFileString() {
        return id + "|" + name + "|" + age + "|" + department + "|" + email + "|" + phone;
    }

    /** Builds a Student from a saved line, or returns null if the line is damaged. */
    public static Student fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 6 || p[0].isEmpty()) {
            return null;
        }
        try {
            return new Student(p[0], p[1], Integer.parseInt(p[2].trim()), p[3], p[4], p[5]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String tableHeader() {
        return String.format("%-9s %-22s %-4s %-18s %-28s %-11s",
                "ID", "Name", "Age", "Department", "Email", "Phone");
    }

    @Override
    public String toString() {
        return String.format("%-9s %-22s %-4d %-18s %-28s %-11s",
                id, name, age, department, email, phone);
    }
}
