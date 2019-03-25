package ru.example.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.example.model.SourceTransaction;
import ru.example.model.VerifyStatus;
import ru.example.model.VerifyTransactionResult;
import ru.example.repository.entity.Transaction;
import ru.example.repository.TransactionRepository;
import org.springframework.batch.item.ItemProcessor;


@Slf4j
@RequiredArgsConstructor
public class TransactionProcessor implements ItemProcessor<SourceTransaction, VerifyTransactionResult> {

    private final TransactionRepository transactionRepository;

    @Override
    public VerifyTransactionResult process(SourceTransaction sourceTransaction) throws Exception {
        Transaction transaction = transactionRepository.findById(sourceTransaction.getPid())
                .orElse(null);

        VerifyStatus verifyStatus = verifySourceTransaction(sourceTransaction, transaction);
        log.debug("Verify status for source transaction {} and stored transaction {} is {}", sourceTransaction,
                transaction, verifyStatus);
        return VerifyTransactionResult.builder()
                .sourceTransaction(sourceTransaction)
                .storedTransaction(transaction)
                .verifyStatus(verifyStatus)
                .build();
    }

    private VerifyStatus verifySourceTransaction(SourceTransaction sourceTransaction, Transaction transaction) {
        if (transaction == null) {
            return VerifyStatus.NOT_FOUND_STORED_TRANSACTION;
        }
        if (transaction.getAmount().compareTo(sourceTransaction.getPamount()) != 0) {
            return VerifyStatus.SUM_MISMATCH;
        }
        return VerifyStatus.MATCHED;
    }
}
