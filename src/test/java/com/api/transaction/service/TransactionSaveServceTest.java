package com.api.transaction.service;


import com.api.Application;
import com.api.statistics.service.PeriodStatistics;
import com.api.transaction.repository.BankTransaction;
import com.api.transaction.repository.BankTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionSaveServceTest {

    @Inject
    private TransactionSaveService transactionSaveService;

    @Inject
    BankTransactionRepository bankTransactionRepository;

    @Inject
    private ConcurrentHashMap<LocalDateTime, PeriodStatistics> recentStatistics;

    @Before
    public void setUp() {
        recentStatistics.clear();
    }

    @Test
    public void givenBankTransaction_whenSaveTransaction_transactionSaved() {
        BankTransaction transaction = new BankTransaction(15.0, System.currentTimeMillis());

        BankTransaction savedTransaction = transactionSaveService.saveTransaction(transaction);

        assertNotNull(savedTransaction);

        assertTrue(savedTransaction.getId() > 0);

        BankTransaction bankTransactionFromDatabase = bankTransactionRepository.findById(savedTransaction.getId())
                .orElse(null);
        assertNotNull(bankTransactionFromDatabase);
        assertEquals(bankTransactionFromDatabase.getTimestamp(), transaction.getTimestamp());
        assertEquals(bankTransactionFromDatabase.getAmount(), transaction.getAmount(), 0);

        //Statistical data is updated
        assertEquals(recentStatistics.size(), 1);
    }

}