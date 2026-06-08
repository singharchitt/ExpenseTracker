package com.org.archit.expensetracker.repository;

import com.org.archit.expensetracker.model.Budget;
import com.org.archit.expensetracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findByUserIdAndMonthAndYear(Long userId, int month, int year);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(Long userId, Category category, int month, int year);

    List<Budget> findByUserIdAndCategoryOrderByYearDescMonthDesc(Long userId, Category category);
}
