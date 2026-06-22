package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/transactions", version = "1")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDTOS.Response>> getTransactionsForCurrentUser(
            @AuthenticationPrincipal SecurityUser securityUser) {
        return  new ResponseEntity<>(
                transactionService.getTransactionsForCurrentUser(securityUser.getId()),
                HttpStatus.OK);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTOS.Response> getTransactionById(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID transactionId) {
        return  new ResponseEntity<>(
                transactionService.getTransactionById(transactionId, securityUser.getId()),
                HttpStatus.OK);
    }

    @PostMapping("/ingreso")
    public ResponseEntity<TransactionDTOS.Response> createIngreso(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody TransactionDTOS.CreateIngresoRequest request) {
        return new ResponseEntity<>(
                transactionService.createIngreso(securityUser.getId(), request),
                HttpStatus.CREATED);
    }

    @PostMapping("/gasto")
    public ResponseEntity<TransactionDTOS.Response> createGasto(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody TransactionDTOS.CreateGastoRequest request) {
        return new ResponseEntity<>(
                transactionService.createGasto(securityUser.getId(), request),
                HttpStatus.CREATED);
    }

    @PostMapping("/transferencia")
    public ResponseEntity<TransactionDTOS.Response> createTransferencia(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody TransactionDTOS.CreateTransferenciaRequest request) {
        return new ResponseEntity<>(
                transactionService.createTransferencia(securityUser.getId(), request),
                HttpStatus.CREATED);
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionDTOS.Response> updateTransaction(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody TransactionDTOS.UpdateRequest request,
            @PathVariable UUID transactionId) {
        return new ResponseEntity<>(
                transactionService.updateTransaction(securityUser.getId(), transactionId, request),
                HttpStatus.OK);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID transactionId) {
        transactionService.deleteTransaction(securityUser.getId(), transactionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
