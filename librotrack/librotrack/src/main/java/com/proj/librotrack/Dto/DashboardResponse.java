package com.proj.librotrack.Dto;

/**
 * Summary counts shown on the dashboard.
 */
public class DashboardResponse {

    private long totalBookTitles;
    private long totalCopies;
    private long availableCopies;
    private long totalStudents;
    private long activeIssues;
    private long overdueIssues;
    private double totalFinesCollected;

    public DashboardResponse() {
    }

    public long getTotalBookTitles() {
        return totalBookTitles;
    }

    public void setTotalBookTitles(long totalBookTitles) {
        this.totalBookTitles = totalBookTitles;
    }

    public long getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(long totalCopies) {
        this.totalCopies = totalCopies;
    }

    public long getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(long availableCopies) {
        this.availableCopies = availableCopies;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getActiveIssues() {
        return activeIssues;
    }

    public void setActiveIssues(long activeIssues) {
        this.activeIssues = activeIssues;
    }

    public long getOverdueIssues() {
        return overdueIssues;
    }

    public void setOverdueIssues(long overdueIssues) {
        this.overdueIssues = overdueIssues;
    }

    public double getTotalFinesCollected() {
        return totalFinesCollected;
    }

    public void setTotalFinesCollected(double totalFinesCollected) {
        this.totalFinesCollected = totalFinesCollected;
    }
}
