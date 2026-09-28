package com.proj.librotrack.Service;

import com.proj.librotrack.Entity.Student;
import com.proj.librotrack.Exception.BusinessRuleException;
import com.proj.librotrack.Exception.ResourceNotFoundException;
import com.proj.librotrack.Repository.IssueRecordRepository;
import com.proj.librotrack.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final IssueRecordRepository issueRecordRepository;

    public StudentService(StudentRepository studentRepository,
                          IssueRecordRepository issueRecordRepository) {
        this.studentRepository = studentRepository;
        this.issueRecordRepository = issueRecordRepository;
    }

    public Student createStudent(Student student) {
        if (studentRepository.existsByRollNumber(student.getRollNumber())) {
            throw new BusinessRuleException(
                    "A student with roll number " + student.getRollNumber() + " already exists");
        }
        student.setId(0);
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(int id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id " + id));
    }

    public Student updateStudent(int id, Student student) {
        Student existing = getStudentById(id);

        if (!existing.getRollNumber().equals(student.getRollNumber())
                && studentRepository.existsByRollNumber(student.getRollNumber())) {
            throw new BusinessRuleException(
                    "A student with roll number " + student.getRollNumber() + " already exists");
        }

        existing.setName(student.getName());
        existing.setRollNumber(student.getRollNumber());
        existing.setDepartment(student.getDepartment());
        existing.setEmail(student.getEmail());
        existing.setPhone(student.getPhone());

        return studentRepository.save(existing);
    }

    public String deleteStudent(int id) {
        getStudentById(id);
        if (issueRecordRepository.existsByStudentId(id)) {
            throw new BusinessRuleException(
                    "Cannot delete student: student has issue history in the library records");
        }
        studentRepository.deleteById(id);
        return "Student deleted successfully";
    }
}
