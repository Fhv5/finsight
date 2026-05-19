package io.github.fhv5.finsight.repository;

import io.github.fhv5.finsight.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByUserIdAndName(UUID userId, String name);


    List<Account> findByUserId(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}
