package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/transactions", version = "1")
public class TransactionController {
    private final TransactionService transactionService;
}
