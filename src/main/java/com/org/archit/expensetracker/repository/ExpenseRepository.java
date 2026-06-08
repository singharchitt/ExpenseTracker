package com.org.archit.expensetracker.repository;

import com.org.archit.expensetracker.model.Category;
import com.org.archit.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Ensure expense belongs to user
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    // All expenses for a user (paginated)
    Page<Expense> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    // By category for a user
    Page<Expense> findByUserIdAndCategoryOrderByDateDesc(Long userId, Category category, Pageable pageable);

    // By date range for a user
    Page<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate start, LocalDate end, Pageable pageable);

    // By month and year for a user
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
           "AND MONTH(e.date) = :month AND YEAR(e.date) = :year ORDER BY e.date DESC")
    List<Expense> findByUserIdAndMonthAndYear(@Param("userId") Long userId,
                                              @Param("month") int month,
                                              @Param("year") int year);

    // Count for a user in a given month (avoids loading all objects just to count)
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId " +
           "AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    int countByUserIdAndMonthAndYear(@Param("userId") Long userId,
                                     @Param("month") int month,
                                     @Param("year") int year);

    // Total by category for a month (for analytics pie chart)
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
           "WHERE e.user.id = :userId AND MONTH(e.date) = :month AND YEAR(e.date) = :year " +
           "GROUP BY e.category")
    List<Object[]> getTotalByCategoryForMonth(@Param("userId") Long userId,
                                              @Param("month") int month,
                                              @Param("year") int year);

    // Monthly totals for chart
    @Query("SELECT YEAR(e.date), MONTH(e.date), SUM(e.amount) FROM Expense e " +
           "WHERE e.user.id = :userId AND e.date >= :startDate " +
           "GROUP BY YEAR(e.date), MONTH(e.date) ORDER BY YEAR(e.date), MONTH(e.date)")
    List<Object[]> getMonthlyTotals(@Param("userId") Long userId,
                                    @Param("startDate") LocalDate startDate);

    // Total for a specific category and month (budget vs actual)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId AND e.category = :category " +
           "AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    BigDecimal getTotalForCategoryAndMonth(@Param("userId") Long userId,
                                           @Param("category") Category category,
                                           @Param("month") int month,
                                           @Param("year") int year);

    // All category totals in one query (avoids N+1 in analytics)
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
           "WHERE e.user.id = :userId AND MONTH(e.date) = :month AND YEAR(e.date) = :year " +
           "GROUP BY e.category")
    List<Object[]> getAllCategoryTotalsForMonth(@Param("userId") Long userId,
                                                @Param("month") int month,
                                                @Param("year") int year);

    // Year total
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId AND YEAR(e.date) = :year")
    BigDecimal getTotalForYear(@Param("userId") Long userId, @Param("year") int year);
}
