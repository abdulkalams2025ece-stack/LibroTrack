package com.proj.librotrack.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Entity
@Table(name = "issue_record")
public class IssueRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;   // null = still with the student

    private double fineAmount;

    private String status;          // ISSUED or RETURNED

    public IssueRecord() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Fine calculated automatically (not a database column):
     *  - RETURNED book  -> the fine that was charged
     *  - ISSUED & late  -> fine so far = late days x Rs.2
     */
    public double getCurrentFine() {
        if ("ISSUED".equals(status) && dueDate != null && LocalDate.now().isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, LocalDate.now()) * 2.0;
        }
        return fineAmount;
    }

    public boolean isOverdue() {
        return "ISSUED".equals(status) && dueDate != null && LocalDate.now().isAfter(dueDate);
    }
}
