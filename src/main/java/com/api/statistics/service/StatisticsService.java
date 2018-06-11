package com.api.statistics.service;

import com.api.transaction.repository.BankTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service work with statistical data for transactions
 */
@Component
public class StatisticsService {
    /**
     * In this Map we keep period statistics for each period for 1 sec.
     * For the given conditions of problem this will be Map containing exactly 60 entries each for one of last 60 second.
     * Key for the Map is LocalDateTime of that second. E.g if we assume that current time is 2018-05-04T12:00:00, Map will contain keys
     * from 2018-05-04T11:59:59 to 2018-05-04T12:00:00. Each Value will be updated each time new statistics arrived for that second.
     */
    private ConcurrentHashMap<LocalDateTime, PeriodStatistics> recentStatistics;


    /**
     * How many seconds from now back statistic data should ba kept and shown
     */
    private int statisticPeriodFromNow;

    @Inject
    public StatisticsService(ConcurrentHashMap<LocalDateTime, PeriodStatistics> recentStatistics, @Value("${statistics.periodinsec}") int statisticPeriodFromNow) {
        this.recentStatistics = recentStatistics;
        this.statisticPeriodFromNow = statisticPeriodFromNow;
    }

    /**
     * Updates statistics data for the DateTime(we clear nanoseconds) when bank transaction happened.
     * For old transactions update will not happen. After each insert old data(before given timeframe from now) will be cleared
     * So this way we will make sure that Map always contains constant amount of elements which is equals in a given problem conditions 60
     * Here when updating {@link ConcurrentHashMap} we use {@link ConcurrentHashMap#merge} to stop other threads accessing this data until we update it.
     * Operations are very short though.
     *
     * @param bankTransaction
     */
    public void updateRecentStatistics(BankTransaction bankTransaction) {
        if (Instant.now().toEpochMilli() - bankTransaction.getTimestamp() > statisticPeriodFromNow * 1000) {
            return;
        }
        LocalDateTime transactionDateTime = Instant.ofEpochMilli(bankTransaction.getTimestamp())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime transactionSecond = transactionDateTime.withNano(0);
        double amount = bankTransaction.getAmount();
        PeriodStatistics periodStatisticsIfAbsent = new PeriodStatistics(amount, amount, amount, 1, amount);
        recentStatistics.merge(transactionSecond, periodStatisticsIfAbsent,
                (oldStatistics, newValue) -> {
                    int oldCount = oldStatistics.getCount();
                    double oldMax = oldStatistics.getMax();
                    double oldMin = oldStatistics.getMin();
                    double oldSum = oldStatistics.getSum();
                    oldStatistics.setCount(oldCount + 1);
                    oldStatistics.setMax(oldMax > newValue.getMax() ? oldMax : newValue.getMax());
                    oldStatistics.setMin(oldMin < newValue.getMin() ? oldMin : newValue.getMin());
                    oldStatistics.setSum(oldSum + newValue.getSum());
                    oldStatistics.setAvg(oldStatistics.getSum() / oldStatistics.getCount());
                    return oldStatistics;
                });
        cleanupOldData();

    }

    /**
     * Returns accumulated data for Transactions happened in a given timeframe from now ago.
     * It will iterate through Map containing statistical data for each second and create accumulated statistics.
     * As far as Map contains exacly #statisticPeriodFromNow = 60 amount of records, this method is always doing not more than #statisticPeriodFromNow steps
     * So we have constant memory usage(Map contains 60 elements) and constant running time(60 iterations)
     *
     * @return
     */
    public PeriodStatistics getStatisticsForLastSeconds() {
        return recentStatistics.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAfter(LocalDateTime.now().minusSeconds(statisticPeriodFromNow)))
                .map(entry -> entry.getValue())
                .collect(PeriodStatistics::new, (oldStatistics, newValue) -> {
                    int oldCount = oldStatistics.getCount();
                    double oldMax = oldStatistics.getMax();
                    double oldMin = oldStatistics.getMin();
                    double oldSum = oldStatistics.getSum();
                    oldStatistics.setCount(oldCount + newValue.getCount());
                    oldStatistics.setMax(oldMax > newValue.getMax() ? oldMax : newValue.getMax());
                    oldStatistics.setMin(oldMin < newValue.getMin() ? oldMin : newValue.getMin());
                    oldStatistics.setSum(oldSum + newValue.getSum());
                    oldStatistics.setAvg(oldStatistics.getSum() / oldStatistics.getCount());
                }, (u, v) -> {
                });
    }

    /**
     * Clean up data which is older than we need to keep statistics Map in constant size
     */

    public void cleanupOldData() {
        recentStatistics.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isBefore(LocalDateTime.now().minusSeconds(statisticPeriodFromNow)))
                .forEach(entry -> recentStatistics.remove(entry.getKey()));
    }
}
