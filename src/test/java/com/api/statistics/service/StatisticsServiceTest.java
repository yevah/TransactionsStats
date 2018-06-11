package com.api.statistics.service;

import com.api.transaction.repository.BankTransaction;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StatisticsServiceTest {

    private StatisticsService statisticsService;

    private ConcurrentHashMap<LocalDateTime, PeriodStatistics> recentStatistics;

    private int statisticsPeriodFromNow = 60;


    @Before
    public void setUp() {
        recentStatistics = new ConcurrentHashMap<>();
        statisticsService = new StatisticsService(recentStatistics, statisticsPeriodFromNow);

    }

    @Test
    public void givenFirstTransaction_whenUpdatingRecent_OneStatisticsAdded() {
        LocalDateTime transactionDate = LocalDateTime.now();
        long timestampInUTC = transactionDate.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        double amount = 10.0;
        BankTransaction transaction = new BankTransaction(amount, timestampInUTC);

        statisticsService.updateRecentStatistics(transaction);

        assertEquals(recentStatistics.size(), 1);
        LocalDateTime transactionSecond = transactionDate.withNano(0);
        assertNotNull(recentStatistics.get(transactionSecond));


    }

    @Test
    public void givenTransactionsInDifferentSeconds_whenUpdateRecentInParallel_2statisticsAdded() {
        LocalDateTime transactionDate = LocalDateTime.now();
        long timestampInUTC1 = transactionDate.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        double amount = 10.0;

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        IntStream.range(0, 10)
                .mapToObj(i -> new BankTransaction(amount + i, timestampInUTC1 - i * 1000))
                .map(transaction -> new Runnable() {

                    @Override
                    public void run() {
                        statisticsService.updateRecentStatistics(transaction);
                    }
                })
                .forEach(runnable -> executorService.submit(runnable));
        await().atMost(Duration.ONE_MINUTE).until(() -> recentStatistics.size() == 10);

        executorService.shutdown();

    }

    @Test
    public void givenTransactionInSameSecond_whenUpdateRecent_statisticsAreMerged() {
        LocalDateTime transactionDate = LocalDateTime.now();
        long timestampInUTC = transactionDate.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        double amount = 10.0;

        BankTransaction transaction1 = new BankTransaction(amount, timestampInUTC);
        BankTransaction transaction2 = new BankTransaction(amount - 5, timestampInUTC + 200);//less than 1 second ago
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> statisticsService.updateRecentStatistics(transaction1));
        executorService.execute(() -> statisticsService.updateRecentStatistics(transaction2));
        await().atMost(Duration.ONE_MINUTE).until(() -> recentStatistics.size() == 1);

        assertEquals(recentStatistics.size(), 1);
        PeriodStatistics statistics = recentStatistics.get(transactionDate.withNano(0));
        assertNotNull(statistics);
        assertEquals(statistics.getCount(), 2);
        assertEquals(statistics.getMax(), amount, 0);
        assertEquals(statistics.getMin(), amount - 5, 0);
        assertEquals(statistics.getAvg(), (amount + amount - 5) / 2, 0);
    }

    @Test
    public void givenPeriodStatisticsForLastMinute_whenGetStatisticsForLastMinute_correctStatisticReturned() {
        LocalDateTime transactionDate = LocalDateTime.now();
        int n = 59; // this amount of statistics we want to create in a loop.
        // statistics data is incremental from iteration step so later we can check by formulas
        IntStream.range(1, n + 1)
                .mapToObj(i -> new PeriodStatistics(i + 1, i, i, i, 2 * i))
                .forEach(periodStatistics ->
                        recentStatistics.put(transactionDate.minusSeconds(periodStatistics.getCount()), periodStatistics));

        PeriodStatistics periodStatistics = statisticsService.getStatisticsForLastSeconds();

        assertNotNull(periodStatistics);
        int totalCount = periodStatistics.getCount();
        assertEquals(periodStatistics.getMax(), n + 1, 0);
        assertEquals(periodStatistics.getMin(), 1, 0);
        assertEquals(totalCount, n * (n + 1) / 2);// sum of numbers from 1 to n
        assertEquals(periodStatistics.getAvg(), 2 * (n + 1) * n / (2 * totalCount), 0);
    }

    @Test
    public void givenOldStatistics_whenGetStatisticsForLastMinute_oldStatisticsExcluded() {
        LocalDateTime transactionDate = LocalDateTime.now();
        PeriodStatistics validPeriodStatistics = new PeriodStatistics(11, 10, 10, 2, 2 * 10);
        PeriodStatistics expiredPeriodStatistics = new PeriodStatistics(12, 14, 18, 7, 2 * 100);
        recentStatistics.put(transactionDate, validPeriodStatistics);
        recentStatistics.put(transactionDate.minusSeconds(100), expiredPeriodStatistics);

        PeriodStatistics periodStatistics = statisticsService.getStatisticsForLastSeconds();

        assertNotNull(periodStatistics);
        assertEquals(periodStatistics.getMax(), validPeriodStatistics.getMax(), 0);
        assertEquals(periodStatistics.getMin(), validPeriodStatistics.getMin(), 0);
        assertEquals(periodStatistics.getCount(), validPeriodStatistics.getCount());
        assertEquals(periodStatistics.getAvg(), validPeriodStatistics.getSum() / validPeriodStatistics.getCount(), 0);
    }

    @Test
    public void givenNoStatistics_whenGetStatisticsForLastMinute_countofStatisticsIs0() {
        // no period statistics
        PeriodStatistics periodStatistics = statisticsService.getStatisticsForLastSeconds();

        assertNotNull(periodStatistics);
        assertEquals(periodStatistics.getCount(), 0);
    }

    @Test
    public void givenOldStatisticsInRecentstatistics_whenCleanUpOldData_oldStatisticsIsRemoved() {
        LocalDateTime transactionDate = LocalDateTime.now();
        PeriodStatistics validPeriodStatistics = new PeriodStatistics(11, 10, 10, 2, 2 * 10);
        PeriodStatistics expiredPeriodStatistics = new PeriodStatistics(12, 14, 18, 7, 2 * 100);
        recentStatistics.put(transactionDate, validPeriodStatistics);
        recentStatistics.put(transactionDate.minusSeconds(100), expiredPeriodStatistics);

        statisticsService.cleanupOldData();

        assertEquals(recentStatistics.size(), 1);
        assertNotNull(recentStatistics.get(transactionDate));

    }

}