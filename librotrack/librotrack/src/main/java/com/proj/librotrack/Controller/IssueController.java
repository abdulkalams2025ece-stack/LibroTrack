package com.proj.librotrack.Controller;

import com.proj.librotrack.Dto.IssueRequest;
import com.proj.librotrack.Entity.IssueRecord;
import com.proj.librotrack.Service.IssueService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }


    @PostMapping
    public ResponseEntity<IssueRecord> issueBook(@Valid @RequestBody IssueRequest request) {
        IssueRecord record = issueService.issueBook(request.getBookId(), request.getStudentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<IssueRecord> returnBook(
            @PathVariable int id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {
        return ResponseEntity.ok(issueService.returnBook(id, returnDate));
    }


    @GetMapping
    public ResponseEntity<List<IssueRecord>> getAllIssues() {
        return ResponseEntity.ok(issueService.getAllIssues());
    }


    @GetMapping("/{id}")
    public ResponseEntity<IssueRecord> getIssueById(@PathVariable int id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }


    @GetMapping("/current")
    public ResponseEntity<List<IssueRecord>> getAllCurrentIssues() {
        return ResponseEntity.ok(issueService.getAllCurrentIssues());
    }


    @GetMapping("/student/{studentId}/current")
    public ResponseEntity<List<IssueRecord>> getCurrentIssuesByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(issueService.getCurrentIssuesByStudent(studentId));
    }


    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<List<IssueRecord>> getHistoryByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(issueService.getHistoryByStudent(studentId));
    }


    @GetMapping("/overdue")
    public ResponseEntity<List<IssueRecord>> getOverdueIssues() {
        return ResponseEntity.ok(issueService.getOverdueIssues());
    }


    @GetMapping("/filter")
    public ResponseEntity<List<IssueRecord>> filterByIssueDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(issueService.filterByIssueDate(from, to));
    }
}
