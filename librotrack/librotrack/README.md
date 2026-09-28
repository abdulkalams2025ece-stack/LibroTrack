# LibroTrack — Library Book Issue and Return Manager

Spring Boot backend (same setup as the `quiz` project from the expert session): Spring Web MVC, Spring Data JPA, Validation, MySQL. A simple web page for trying it out is included at `http://localhost:8080`.

---

## 1. Use case (project explanation)

**Problem:** A college library issues books to students by writing in a register. Students forget their return dates, the librarian has trouble tracking due dates, fines and how many copies are left, and there is no easy way to send reminders.

**Solution:** LibroTrack is a REST backend that keeps the book catalogue, records every issue and return, sets the due date automatically (issue date + 14 days) and calculates the overdue fine (Rs. 2 per late day) when a book is returned.

**Actors**

| Actor | What they do |
|---|---|
| Librarian | Adds/updates books and students, issues and returns books, views reports and the dashboard |
| Student (data only) | Borrows books; their current books and history can be looked up |

**Use cases**

| # | Use case | Endpoint |
|---|---|---|
| UC1 | Add / update a book (title, author, ISBN, category, copies) | `POST /api/books`, `PUT /api/books/{id}` |
| UC2 | Issue a book to a student, due date set to +14 days | `POST /api/issues` |
| UC3 | Return a book, calculate the fine if late | `PUT /api/issues/{id}/return` |
| UC4 | Search books by title, author or category | `GET /api/books/search/title?title=` (also `/author`, `/category`) |
| UC5 | List books currently issued to a student | `GET /api/issues/student/{id}/current` |
| Extra | Dashboard counts | `GET /api/dashboard` |
| Extra | Filter issues by date, overdue list | `GET /api/issues/filter?from=&to=`, `GET /api/issues/overdue` |

**Business rules (checked in the service layer before saving)**

1. A book cannot be issued if all copies are already checked out → `409 Conflict`
2. A fine accrues at a fixed rate (Rs. 2 per day) once the due date has passed
3. The same student cannot hold two copies of the same book → `409`
4. A book that has already been returned cannot be returned again → `409`
5. Duplicate ISBN or roll number is rejected → `409`
6. Total copies cannot be reduced below the number currently issued → `409`
7. A book or student with issue history cannot be deleted → `409`
8. Invalid input (blank title, copies < 1, bad email, missing bookId) → `400` with a list of field errors

---

## 2. Database design

```
 BOOK                    ISSUE_RECORD                   STUDENT
 ----------------        ------------------------       ----------------
 id (PK)          1 ──<  id (PK)                  >── 1  id (PK)
 title                   book_id (FK -> book)            name
 author                  student_id (FK -> student)      roll_number (unique)
 isbn (unique)           issue_date                      department
 category                due_date                        email
 total_copies            return_date (null = not yet)    phone
 available_copies        fine_amount
                         status (ISSUED / RETURNED)
```

* One **Book** can appear in many IssueRecords, and one **Student** can have many IssueRecords.
* In Java: `IssueRecord` has `@ManyToOne Book book` and `@ManyToOne Student student`.
* Tables are created automatically by Hibernate (`ddl-auto=update`).

---

## 3. Project structure

```
librotrack
├── pom.xml                         (same as quiz project: Boot 4.1.1, Java 25)
└── src/main
    ├── java/com/proj/librotrack
    │   ├── LibrotrackApplication.java      <- run this
    │   ├── Entity       Book, Student, IssueRecord
    │   ├── Repository   BookRepository, StudentRepository, IssueRecordRepository
    │   ├── Service      BookService, StudentService, IssueService  (business rules)
    │   ├── Controller   BookController, StudentController, IssueController, DashboardController
    │   ├── Dto          IssueRequest, DashboardResponse
    │   └── Exception    GlobalExceptionHandler (@RestControllerAdvice),
    │                    ResourceNotFoundException (404), BusinessRuleException (409), ErrorResponse
    └── resources
        ├── application.properties          <- MySQL username / password
        └── static/index.html               <- test web page
```

---

## 4. Run it in IntelliJ IDEA (step by step)

**Before you start:** MySQL Server is running, and the same JDK you used for the quiz project (JDK 25) is installed.

1. **Unzip** `librotrack.zip` (e.g. into `Downloads\librotrack`).
2. Open IntelliJ IDEA → **File → Open** → select the `librotrack` folder (the one that has `pom.xml`) → **Open** → **Trust Project**.
3. Wait for the bottom-right progress bar to finish ("Resolving Maven dependencies"). If it asks, click **Load Maven Project**.
4. **Set the JDK:** File → Project Structure → Project → SDK = **25** (same as quiz). Language level = SDK default.
5. **Set your MySQL password:** open `src/main/resources/application.properties` and change
   ```
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
   The database `librotrack_db` is created automatically — you don't need to create it.
6. Open `LibrotrackApplication.java` → click the green ▶ next to `main` → **Run**.
7. When the console shows `Started LibrotrackApplication`, open **http://localhost:8080** in your browser to use the test page, or use Postman (below).

**If you see an error**

| Error in console | Fix |
|---|---|
| `Access denied for user 'root'@'localhost'` | Wrong MySQL password in `application.properties` |
| `Communications link failure` | MySQL service is not running — start it (Services → MySQL80 → Start) |
| `Port 8080 was already in use` | Stop the quiz app (red ■ button), or set `server.port=8081` |
| `release version 25 not supported` | Project SDK is not 25 — fix step 4, or change `<java.version>` in `pom.xml` to the JDK you have (21 works too) |
| Red imports / `cannot find symbol` | Right-click `pom.xml` → Maven → **Reload Project** |

---

## 5. Test with Postman

Import `LibroTrack.postman_collection.json` (Postman → Import → select the file). Run the requests in order:

1. **Add Book** — `POST http://localhost:8080/api/books`
   ```json
   { "title": "Head First Java", "author": "Kathy Sierra", "isbn": "978-0596009205", "category": "Programming", "totalCopies": 2 }
   ```
   → `201 Created`, `availableCopies` = 2
2. **Add Student** — `POST http://localhost:8080/api/students`
   ```json
   { "name": "Anu Priya", "rollNumber": "21CS001", "department": "CSE", "email": "anu@college.edu", "phone": "9876543210" }
   ```
3. **Issue Book** — `POST http://localhost:8080/api/issues` → `{ "bookId": 1, "studentId": 1 }` → `201`, `dueDate` = today + 14
4. **Issue until copies run out** → the 3rd issue of a 2-copy book (to different students) gives
   `409 { "message": "Book 'Head First Java' cannot be issued: all copies are already checked out" }`
5. **Late return (fine demo)** — `PUT http://localhost:8080/api/issues/1/return?returnDate=2026-12-31` → `fineAmount` = late days × 2
6. **Return again** → `409 already returned`
7. **Validation error** — add a book with `"title": ""` and `"totalCopies": 0` → `400` with `fieldErrors`
8. **Not found** — `GET http://localhost:8080/api/books/999` → `404`
9. **Search** — `GET /api/books/search/title?title=java`
10. **Current books of a student** — `GET /api/issues/student/1/current`
11. **Dashboard** — `GET /api/dashboard`

## 6. All endpoints

| Method | URL | Purpose | Success |
|---|---|---|---|
| POST | /api/books | Add book | 201 |
| GET | /api/books | All books | 200 |
| GET | /api/books/{id} | One book | 200 / 404 |
| PUT | /api/books/{id} | Update book | 200 |
| DELETE | /api/books/{id} | Delete book (no history) | 200 / 409 |
| GET | /api/books/search/title?title= | Search by title | 200 |
| GET | /api/books/search/author?author= | Search by author | 200 |
| GET | /api/books/search/category?category= | Search by category | 200 |
| POST | /api/students | Add student | 201 |
| GET | /api/students , /api/students/{id} | List / get | 200 |
| PUT | /api/students/{id} | Update | 200 |
| DELETE | /api/students/{id} | Delete (no history) | 200 / 409 |
| POST | /api/issues | Issue a book | 201 / 409 |
| PUT | /api/issues/{id}/return[?returnDate=yyyy-MM-dd] | Return + fine | 200 / 409 |
| GET | /api/issues , /api/issues/{id} | All / one issue record | 200 |
| GET | /api/issues/current | All books currently out | 200 |
| GET | /api/issues/student/{id}/current | Books a student has now | 200 |
| GET | /api/issues/student/{id}/history | Student's full history | 200 |
| GET | /api/issues/overdue | Overdue books with fine so far | 200 |
| GET | /api/issues/filter?from=&to= | Filter by issue date | 200 |
| GET | /api/dashboard | Summary counts | 200 |
