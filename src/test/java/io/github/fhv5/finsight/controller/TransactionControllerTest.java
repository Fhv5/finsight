package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.model.TransactionType;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private SecurityUser securityUser;
    private UUID userId;
    private UUID transactionId;
    private TransactionDTOS.Response mockResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        securityUser = new SecurityUser(user);

        mockResponse = TransactionDTOS.Response.builder()
                .id(transactionId)
                .type(TransactionType.INGRESO)
                .amount(500L)
                .description("Test")
                .build();
    }

    @Test
    void getTransactionsForCurrentUser_ShouldReturnOkStatusAndList() {
        List<TransactionDTOS.Response> expectedResponse = List.of(mockResponse);
        when(transactionService.getTransactionsForCurrentUser(userId)).thenReturn(expectedResponse);

        ResponseEntity<List<TransactionDTOS.Response>> result =
                transactionController.getTransactionsForCurrentUser(securityUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(transactionService).getTransactionsForCurrentUser(userId);
    }

    @Test
    void getTransactionById_ShouldReturnOkStatusAndTransaction() {
        when(transactionService.getTransactionById(transactionId, userId)).thenReturn(mockResponse);

        ResponseEntity<TransactionDTOS.Response> result =
                transactionController.getTransactionById(securityUser, transactionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockResponse, result.getBody());
        verify(transactionService).getTransactionById(transactionId, userId);
    }

    @Test
    void createIngreso_ShouldReturnCreatedStatusAndTransaction() {
        TransactionDTOS.CreateIngresoRequest request = new TransactionDTOS.CreateIngresoRequest(
                Instant.now(), 500L, "Paycheck", UUID.randomUUID(), UUID.randomUUID());

        when(transactionService.createIngreso(eq(userId), any(TransactionDTOS.CreateIngresoRequest.class)))
                .thenReturn(mockResponse);

        ResponseEntity<TransactionDTOS.Response> result =
                transactionController.createIngreso(securityUser, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(mockResponse, result.getBody());
        verify(transactionService).createIngreso(eq(userId), any(TransactionDTOS.CreateIngresoRequest.class));
    }

    @Test
    void createGasto_ShouldReturnCreatedStatusAndTransaction() {
        TransactionDTOS.CreateGastoRequest request = new TransactionDTOS.CreateGastoRequest(
                Instant.now(), 200L, "Supermarket", UUID.randomUUID(), UUID.randomUUID());

        TransactionDTOS.Response gastoResponse = TransactionDTOS.Response.builder()
                .id(transactionId)
                .type(TransactionType.GASTO)
                .amount(200L)
                .description("Supermarket")
                .build();

        when(transactionService.createGasto(eq(userId), any(TransactionDTOS.CreateGastoRequest.class)))
                .thenReturn(gastoResponse);

        ResponseEntity<TransactionDTOS.Response> result =
                transactionController.createGasto(securityUser, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(gastoResponse, result.getBody());
        verify(transactionService).createGasto(eq(userId), any(TransactionDTOS.CreateGastoRequest.class));
    }

    @Test
    void createTransferencia_ShouldReturnCreatedStatusAndTransaction() {
        TransactionDTOS.CreateTransferenciaRequest request = new TransactionDTOS.CreateTransferenciaRequest(
                Instant.now(), 300L, "Transfer", UUID.randomUUID(), UUID.randomUUID(), null);

        TransactionDTOS.Response transferenciaResponse = TransactionDTOS.Response.builder()
                .id(transactionId)
                .type(TransactionType.TRANSFERENCIA)
                .amount(300L)
                .description("Transfer")
                .build();

        when(transactionService.createTransferencia(eq(userId), any(TransactionDTOS.CreateTransferenciaRequest.class)))
                .thenReturn(transferenciaResponse);

        ResponseEntity<TransactionDTOS.Response> result =
                transactionController.createTransferencia(securityUser, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(transferenciaResponse, result.getBody());
        verify(transactionService).createTransferencia(eq(userId), any(TransactionDTOS.CreateTransferenciaRequest.class));
    }

    @Test
    void updateTransaction_ShouldReturnOkStatusAndUpdatedTransaction() {
        TransactionDTOS.UpdateRequest request = new TransactionDTOS.UpdateRequest(
                null, null, 750L, "Updated", null, null, null);

        TransactionDTOS.Response updatedResponse = TransactionDTOS.Response.builder()
                .id(transactionId)
                .type(TransactionType.INGRESO)
                .amount(750L)
                .description("Updated")
                .build();

        when(transactionService.updateTransaction(eq(userId), eq(transactionId), any(TransactionDTOS.UpdateRequest.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<TransactionDTOS.Response> result =
                transactionController.updateTransaction(securityUser, request, transactionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updatedResponse, result.getBody());
        verify(transactionService).updateTransaction(eq(userId), eq(transactionId), any(TransactionDTOS.UpdateRequest.class));
    }

    @Test
    void deleteTransaction_ShouldReturnNoContentStatus() {
        ResponseEntity<Void> result = transactionController.deleteTransaction(securityUser, transactionId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(transactionService).deleteTransaction(userId, transactionId);
    }
}
