package io.github.fhv5.finsight.repository;

import io.github.fhv5.finsight.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByJti(UUID jti);
}
