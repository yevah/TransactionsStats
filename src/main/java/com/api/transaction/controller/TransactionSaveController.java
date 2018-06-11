package com.api.transaction.controller;

import com.api.transaction.repository.BankTransaction;
import com.api.transaction.service.TransactionSaveService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.time.Instant;

/**
 *
 */
@RestController
public class TransactionSaveController {

    @Value("${statistics.periodinsec}")
    int statisticPeriodFromNow;
    @Inject
    TransactionSaveService transactionSaveService;


    @RequestMapping(path = "/transactions")
    public ResponseEntity saveBankTransaction(@RequestBody BankTransaction bankTransaction) {
        BankTransaction transaction = transactionSaveService.saveTransaction(bankTransaction);
        if (Instant.now().toEpochMilli() - transaction.getTimestamp() > statisticPeriodFromNow * 1000) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }

}
