package io.github.fhv5.finsight.repository;

import io.github.fhv5.finsight.model.Transaction;
import io.github.fhv5.finsight.projection.TransactionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
        SELECT t.id as id,
               t.dateIssued as dateIssued,
               t.type as type,
               t.amount as amount,
               t.description as description,
               c.id as categoryId,
               c.name as categoryName,
               oa.id as originAccountId,
               oa.name as originAccountName,
               da.id as destinationAccountId,
               da.name as destinationAccountName
        FROM Transaction t
        LEFT JOIN Category c ON c.id = t.categoryId
        LEFT JOIN Account oa ON oa.id = t.originAccountId
        LEFT JOIN Account da ON da.id = t.destinationAccountId
        WHERE t.userId = :userId
    """)
    List<TransactionView> findAllByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT t.id as id,
               t.dateIssued as dateIssued,
               t.type as type,
               t.amount as amount,
               t.description as description,
               c.id as categoryId,
               c.name as categoryName,
               oa.id as originAccountId,
               oa.name as originAccountName,
               da.id as destinationAccountId,
               da.name as destinationAccountName
        FROM Transaction t
        LEFT JOIN Category c ON c.id = t.categoryId
        LEFT JOIN Account oa ON oa.id = t.originAccountId
        LEFT JOIN Account da ON da.id = t.destinationAccountId
        WHERE t.id = :id AND t.userId = :userId
    """)
    Optional<TransactionView> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Transaction> findEntityByIdAndUserId(UUID id, UUID userId);
}
