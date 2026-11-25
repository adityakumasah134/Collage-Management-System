package model;

public class Course {

    public static final String NO_FACULTY = "NONE";

    private String id;
    private String name;
    private int credits;
    private String department;
    private String facultyId;

    public Course(String id, String name, int credits, String department, String facultyId) {
        this.id = id;
        this.name = name;
        this.credits = credits;
        this.department = department;
        this.facultyId = (facultyId == null || facultyId.isEmpty()) ? NO_FACULTY : facultyId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCredits() { return credits; }
    public String getDepartment() { return department; }
    public String getFacultyId() { return facultyId; }

    public void setName(String name) { this.name = name; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setDepartment(String department) { this.department = department; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public Course copy() {
        return new Course(id, name, credits, department, facultyId);
    }

    public void copyFrom(Course other) {
        this.name = other.name;
        this.credits = other.credits;
        this.department = other.department;
        this.facultyId = other.facultyId;
    }

    public String toFileString() {
        return id + "|" + name + "|" + credits + "|" + department + "|" + facultyId;
    }

    public static Course fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 5 || p[0].isEmpty()) {
            return null;
        }
        try {
            return new Course(p[0], p[1], Integer.parseInt(p[2].trim()), p[3], p[4]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String tableHeader() {
        return String.format("%-8s %-28s %-8s %-18s %-10s",
                "ID", "Course Name", "Credits", "Department", "Faculty");
    }

    @Override
    public String toString() {
        return String.format("%-8s %-28s %-8d %-18s %-10s",
                id, name, credits, department, facultyId);
    }
}
