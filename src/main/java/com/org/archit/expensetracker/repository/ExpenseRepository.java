package com.org.archit.expensetracker.repository;

import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find all expenses sorted by date descending
    List<Expense> findAllByOrderByDateDesc();

    // Find by category
    List<Expense> findByCategoryOrderByDateDesc(Category category);

    // Find by date range
    List<Expense> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);

    // Find by month and year
    @Query("SELECT e FROM Expense e WHERE MONTH(e.date) = :month AND YEAR(e.date) = :year ORDER BY e.date DESC")
    List<Expense> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Total spending by category for a given month/year
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
            "WHERE MONTH(e.date) = :month AND YEAR(e.date) = :year " +
            "GROUP BY e.category")
    List<Object[]> getTotalByCategoryForMonth(@Param("month") int month, @Param("year") int year);

    // Monthly totals for chart
    @Query("SELECT YEAR(e.date), MONTH(e.date), SUM(e.amount) FROM Expense e " +
            "WHERE e.date >= :startDate " +
            "GROUP BY YEAR(e.date), MONTH(e.date) " +
            "ORDER BY YEAR(e.date), MONTH(e.date)")
    List<Object[]> getMonthlyTotals(@Param("startDate") LocalDate startDate);

    // Total spending by category all time
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e GROUP BY e.category")
    List<Object[]> getTotalByCategory();

    // Sum for budget vs actual comparison
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.category = :category AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    BigDecimal getTotalForCategoryAndMonth(@Param("category") Category category,
                                           @Param("month") int month,
                                           @Param("year") int year);
}
