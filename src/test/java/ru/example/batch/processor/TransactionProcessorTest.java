package ru.example.batch.processor;

import ru.example.model.SourceTransaction;
import ru.example.model.VerifyStatus;
import ru.example.model.VerifyTransactionResult;
import ru.example.repository.TransactionRepository;
import ru.example.repository.entity.Transaction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionProcessorTest {


    private static Transaction makeTransaction(Integer id, Double amount) {
        Transaction t = new Transaction();
        t.setAmount(BigDecimal.valueOf(amount));
        t.setId(id);
        return t;
    }

    private static SourceTransaction makeSourceTransaction(Integer id, Double amount) {
        SourceTransaction t = new SourceTransaction();
        t.setPamount(BigDecimal.valueOf(amount));
        t.setPid(id);
        return t;
    }



    static Stream<Arguments> testVerifyTransactions() {
            return Stream.of(
                    Arguments.of(makeSourceTransaction(1, 1d), VerifyStatus.MATCHED),
                    Arguments.of(makeSourceTransaction(2, 22.2d), VerifyStatus.SUM_MISMATCH),
                    Arguments.of(makeSourceTransaction(3, 1d), VerifyStatus.NOT_FOUND_STORED_TRANSACTION),
                    Arguments.of(makeSourceTransaction(5, 22.91000d), VerifyStatus.MATCHED)
            );
    }

    @MethodSource
    @ParameterizedTest
    public void testVerifyTransactions(SourceTransaction sourceTransaction, VerifyStatus verifyStatus) throws Exception {
        Map<Integer, Transaction> transactions = Stream.of(
                makeTransaction(1, 1d),
                makeTransaction(2, 22.25),
                makeTransaction(4, 123d),
                makeTransaction(5, 22.910)
        ).collect(Collectors.toMap(Transaction::getId, Function.identity()));
        TransactionRepository repository = mock(TransactionRepository.class);
        when(repository.findById(eq(sourceTransaction.getPid()))).thenAnswer((Answer<Optional<Transaction>>) invocation -> {
            Integer id = invocation.getArgument(0);
            return Optional.ofNullable(transactions.get(id));
        });
        TransactionProcessor processor = new TransactionProcessor(repository);

        VerifyTransactionResult verifyTransactionResult = processor.process(sourceTransaction);

        assertEquals(verifyTransactionResult.getVerifyStatus(), verifyStatus);
    }

}