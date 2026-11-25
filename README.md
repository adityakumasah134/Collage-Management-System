# College Management System (PS No. 58)

Console-based, menu-driven Java application. No GUI, no database.
All data is stored in separate text files inside the `data/` folder (auto-saved after every change, auto-loaded at start).

## Requirements
- JDK 11 or newer (`javac -version`)

## Compile and run
Run these commands from inside the `CollegeManagementSystem` folder.

**Linux / macOS**
```
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out Main
```

**Windows (Command Prompt)**
```
mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out Main
```

Always start the program from the project root so that the `data/` folder is found.

## Data structures used
| Structure | Where | Purpose |
|---|---|---|
| Linked List | `datastructures/SinglyLinkedList.java` | Student, faculty, course and fee records |
| Stack | `datastructures/UndoStack.java` | Undo Last Operation |
| Queue | `datastructures/RequestQueue.java` | Service requests (first in, first processed) |
| Graph | `datastructures/CourseGraph.java` | Related courses (undirected, adjacency list, BFS search) |

## Sample data
`data/college_data.txt` ships with 3 students, 2 faculty, 4 courses, 3 fee records
(PAID / PARTIAL / UNPAID), 2 course relationships and 2 pending service requests.
The `data/` folder contains separate files: `students.txt`, `faculty.txt`, `courses.txt`, `fees.txt`, `course_relationships.txt`, and `service_requests.txt`.

## Rules worth knowing
- IDs are generated automatically: `STU1001`, `FAC101`, `CRS101`, `REQ1001`. Numbers never repeat.
- A fee record needs an existing student, and only one fee record per student is allowed.
- A student with a fee record, or a faculty member assigned to a course, cannot be deleted until that link is removed.
- Deleting a course also removes its relationships; Undo restores them.
- Undo supports add, update and delete of students, faculty, courses and fee records, fee payments, and adding course relationships. Service requests are not undoable (processing a request is final).
- Remaining fee and status (PAID / PARTIAL / UNPAID) are always calculated, never typed in.
- Pressing Enter during an update keeps the current value.

## Testing checklist (all nine main menu options)
1. Student Management: add, view, search, update, delete
2. Faculty Management: same five operations
3. Course Management: same five operations
4. Fee Management: add, pay (partial then full), pending, paid, search, update, delete
5. Course Relationships: add, display, search connected courses
6. Service Requests: add, view, process until the queue is empty
7. Undo Last Operation: repeat until "undo stack is empty" appears
8. Display All Records
9. Exit, then start again and confirm the data is still there
