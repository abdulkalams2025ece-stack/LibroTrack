package com.proj.librotrack.Repository;

import com.proj.librotrack.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    boolean existsByRollNumber(String rollNumber);
}
