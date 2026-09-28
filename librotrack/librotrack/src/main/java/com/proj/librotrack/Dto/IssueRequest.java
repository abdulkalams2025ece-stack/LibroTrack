package com.proj.librotrack.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * JSON body for issuing a book:  { "bookId": 1, "studentId": 1 }
 */
public class IssueRequest {

    @NotNull(message = "bookId is required")
    @Positive(message = "bookId must be a positive number")
    private Integer bookId;

    @NotNull(message = "studentId is required")
    @Positive(message = "studentId must be a positive number")
    private Integer studentId;

    public IssueRequest() {
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
}
