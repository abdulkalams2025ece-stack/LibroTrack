package com.proj.librotrack.Repository;

import com.proj.librotrack.Entity.IssueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IssueRecordRepository extends JpaRepository<IssueRecord, Integer> {

    // Books currently with a student (not yet returned)
    List<IssueRecord> findByStudentIdAndStatus(int studentId, String status);

    // Full borrowing history of a student
    List<IssueRecord> findByStudentId(int studentId);

    // All records with a given status (ISSUED / RETURNED)
    List<IssueRecord> findByStatus(String status);

    // Is this student already holding this same book?
    boolean existsByStudentIdAndBookIdAndStatus(int studentId, int bookId, String status);

    // Used to block deleting a book / student that has history
    boolean existsByBookId(int bookId);

    boolean existsByStudentId(int studentId);

    // Filter issues by issue date range
    List<IssueRecord> findByIssueDateBetween(LocalDate from, LocalDate to);

    // Books still out whose due date has passed
    List<IssueRecord> findByStatusAndDueDateBefore(String status, LocalDate date);
}
