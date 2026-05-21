package io.github.fhv5.finsight.repository;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
        SELECT new io.github.fhv5.finsight.dto.TransactionDTOS.Response(
            t.id, t.dateIssued, t.type,  t.amount, t.description,
            c.id, c.name,
            oa.id, oa.name,
            da.id, da.name
        )
        FROM Transaction t
        JOIN Category C ON c.id = t.categoryId
        LEFT JOIN Account oa ON oa.id = t.originAccountId
        LEFT JOIN Account da ON da.id = t.destinationAccountId
    """
    )
    List<TransactionDTOS.Response> findAllByUserId(UUID userId);
}
