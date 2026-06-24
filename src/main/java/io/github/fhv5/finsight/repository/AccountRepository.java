package io.github.fhv5.finsight.repository;

import io.github.fhv5.finsight.model.Account;
import io.github.fhv5.finsight.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByUserIdAndName(UUID userId, String name);


    List<Account> findAllByUserId(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    Optional<Account>  findByIdAndUserIdAndType(UUID id, UUID userId, AccountType type);
}
