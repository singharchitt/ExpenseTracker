package com.org.archit.expensetracker.repository;

import com.org.archit.expensetracker.model.Budget;
import com.org.archit.expensetracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Find all budgets for a specific month/year
    List<Budget> findByMonthAndYear(int month, int year);

    // Find a specific budget by category + month + year
    Optional<Budget> findByCategoryAndMonthAndYear(Category category, int month, int year);

    // Find all budgets for a category across all months
    List<Budget> findByCategoryOrderByYearDescMonthDesc(Category category);
}