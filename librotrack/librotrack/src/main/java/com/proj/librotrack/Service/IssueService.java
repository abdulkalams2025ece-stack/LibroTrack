package com.proj.librotrack.Service;

import com.proj.librotrack.Dto.DashboardResponse;
import com.proj.librotrack.Entity.Book;
import com.proj.librotrack.Entity.IssueRecord;
import com.proj.librotrack.Entity.Student;
import com.proj.librotrack.Exception.BusinessRuleException;
import com.proj.librotrack.Exception.ResourceNotFoundException;
import com.proj.librotrack.Repository.BookRepository;
import com.proj.librotrack.Repository.IssueRecordRepository;
import com.proj.librotrack.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * All issue / return business logic lives here.
 * Rules are checked BEFORE saving, so bad data never reaches the database.
 */
@Service
public class IssueService {

    public static final int LOAN_DAYS = 14;          // due date = issue date + 14 days
    public static final double FINE_PER_DAY = 2.0;   // Rs. 2 per late day

    public static final String ISSUED = "ISSUED";
    public static final String RETURNED = "RETURNED";

    private final IssueRecordRepository issueRecordRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;

    public IssueService(IssueRecordRepository issueRecordRepository,
                        BookRepository bookRepository,
                        StudentRepository studentRepository) {
        this.issueRecordRepository = issueRecordRepository;
        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
    }

    // Feature 2: Issue a book to a student, due date set automatically
    @Transactional
    public IssueRecord issueBook(int bookId, int studentId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id " + bookId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id " + studentId));

        // RULE: a book cannot be issued if all copies are already checked out
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("Book '" + book.getTitle()
                    + "' cannot be issued: all copies are already checked out");
        }

        // RULE: same student cannot hold two copies of the same book
        if (issueRecordRepository.existsByStudentIdAndBookIdAndStatus(studentId, bookId, ISSUED)) {
            throw new BusinessRuleException("Student " + student.getName()
                    + " already has a copy of '" + book.getTitle() + "'");
        }

        LocalDate today = LocalDate.now();

        IssueRecord record = new IssueRecord();
        record.setBook(book);
        record.setStudent(student);
        record.setIssueDate(today);
        record.setDueDate(today.plusDays(LOAN_DAYS));
        record.setFineAmount(0);
        record.setStatus(ISSUED);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return issueRecordRepository.save(record);
    }

    // Feature 3: Return a book and calculate the fine if late.
    // returnDate is optional - if not given, today's date is used.
    // (Passing a future date lets you demo the fine in Postman.)
    @Transactional
    public IssueRecord returnBook(int issueId, LocalDate returnDate) {

        IssueRecord record = getIssueById(issueId);

        if (RETURNED.equals(record.getStatus())) {
            throw new BusinessRuleException(
                    "Issue record " + issueId + " has already been returned");
        }

        LocalDate actualReturn = (returnDate != null) ? returnDate : LocalDate.now();

        if (actualReturn.isBefore(record.getIssueDate())) {
            throw new BusinessRuleException("Return date cannot be before the issue date ("
                    + record.getIssueDate() + ")");
        }

        record.setReturnDate(actualReturn);
        record.setFineAmount(calculateFine(record.getDueDate(), actualReturn));
        record.setStatus(RETURNED);

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return issueRecordRepository.save(record);
    }

    // RULE: fine accrues per day at a fixed rate once the due date has passed
    public double calculateFine(LocalDate dueDate, LocalDate onDate) {
        long lateDays = ChronoUnit.DAYS.between(dueDate, onDate);
        return lateDays > 0 ? lateDays * FINE_PER_DAY : 0;
    }

    public List<IssueRecord> getAllIssues() {
        return issueRecordRepository.findAll();
    }

    public IssueRecord getIssueById(int id) {
        return issueRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Issue record not found with id " + id));
    }

    // Feature 5: List currently issued books per student
    public List<IssueRecord> getCurrentIssuesByStudent(int studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id " + studentId);
        }
        return issueRecordRepository.findByStudentIdAndStatus(studentId, ISSUED);
    }

    public List<IssueRecord> getHistoryByStudent(int studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id " + studentId);
        }
        return issueRecordRepository.findByStudentId(studentId);
    }

    public List<IssueRecord> getAllCurrentIssues() {
        return issueRecordRepository.findByStatus(ISSUED);
    }

    // Overdue books: still ISSUED and due date already passed.
    // Each record shows "currentFine" = fine so far (see IssueRecord.getCurrentFine()).
    public List<IssueRecord> getOverdueIssues() {
        return issueRecordRepository.findByStatusAndDueDateBefore(ISSUED, LocalDate.now());
    }

    // Go further: filter by issue date
    public List<IssueRecord> filterByIssueDate(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessRuleException("'to' date cannot be before 'from' date");
        }
        return issueRecordRepository.findByIssueDateBetween(from, to);
    }

    // Go further: dashboard counts
    public DashboardResponse getDashboard() {
        List<Book> books = bookRepository.findAll();
        List<IssueRecord> records = issueRecordRepository.findAll();
        LocalDate today = LocalDate.now();

        DashboardResponse d = new DashboardResponse();
        d.setTotalBookTitles(books.size());
        d.setTotalCopies(books.stream().mapToLong(Book::getTotalCopies).sum());
        d.setAvailableCopies(books.stream().mapToLong(Book::getAvailableCopies).sum());
        d.setTotalStudents(studentRepository.count());
        d.setActiveIssues(records.stream()
                .filter(r -> ISSUED.equals(r.getStatus())).count());
        d.setOverdueIssues(records.stream()
                .filter(r -> ISSUED.equals(r.getStatus()) && r.getDueDate().isBefore(today))
                .count());
        d.setTotalFinesCollected(records.stream()
                .filter(r -> RETURNED.equals(r.getStatus()))
                .mapToDouble(IssueRecord::getFineAmount).sum());
        return d;
    }
}
