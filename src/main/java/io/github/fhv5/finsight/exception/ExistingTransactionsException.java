package io.github.fhv5.finsight.exception;

public class ExistingTransactionsException extends RuntimeException {
    public ExistingTransactionsException(String message) {
        super(message);
    }
}
