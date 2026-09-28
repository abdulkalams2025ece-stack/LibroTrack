package com.proj.librotrack.Controller;

import com.proj.librotrack.Dto.DashboardResponse;
import com.proj.librotrack.Service.IssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IssueService issueService;

    public DashboardController(IssueService issueService) {
        this.issueService = issueService;
    }


    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(issueService.getDashboard());
    }
}
