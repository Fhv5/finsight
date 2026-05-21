package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public List<TransactionDTOS.Response> getTransactionsForCurrentUser(UUID userId) {
        return  transactionRepository.findAllByUserId(userId);
    }
}
