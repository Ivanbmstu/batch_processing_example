package ru.example.model;

import lombok.Builder;
import lombok.Value;
import ru.example.repository.entity.Transaction;

@Value
@Builder
public class VerifyTransactionResult {

    private final SourceTransaction sourceTransaction;
    private final Transaction storedTransaction;
    private final VerifyStatus verifyStatus;
}
