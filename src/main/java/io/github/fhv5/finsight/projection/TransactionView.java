package io.github.fhv5.finsight.projection;

import io.github.fhv5.finsight.model.TransactionType;

import java.time.Instant;
import java.util.UUID;

public interface TransactionView {
    UUID getId();
    Instant getDateIssued();
    TransactionType getType();
    Long getAmount();
    String getDescription();
    UUID getOriginAccountId();
    String getOriginAccountName();
    UUID getDestinationAccountId();
    String getDestinationAccountName();
    UUID getCategoryId();
    String getCategoryName();
}
