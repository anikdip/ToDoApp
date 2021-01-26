package com.task.todoapp.Repository;

import com.task.todoapp.Entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepo extends JpaRepository<Todo, Long> {

    List<Todo> findByItemNameStartsWithIgnoreCase(String itemName);
}
