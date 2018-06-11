package com.api.statistics.service;

/**
 * Bean containing data for any period. It is used to keep statistical data for transactions in a given second.
 * This class is also used when we accumulate statistical data from now back to some period(for given problem 60 seconds)
 */
public class PeriodStatistics {
    private double max;

    private double min = Double.MAX_VALUE;

    private double avg;

    private double sum;

    private int count;

    public PeriodStatistics() {
    }

    public PeriodStatistics(double max, double min, double avg, int count, double sum) {
        this.max = max;
        this.min = min;
        this.avg = avg;
        this.count = count;
        this.sum = sum;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public static class PeriodStatisticsBuilder {
        private double max;

        private double min;

        private double avg;

        private int count;

        private double sum;

        public PeriodStatisticsBuilder withMax(double max) {
            this.max = max;
            return this;
        }

        public PeriodStatisticsBuilder withMin(double min) {
            this.min = min;
            return this;
        }

        public PeriodStatisticsBuilder withAvg(double avg) {
            this.avg = avg;
            return this;
        }

        public PeriodStatisticsBuilder withCount(int count) {
            this.count = count;
            return this;
        }

        public PeriodStatisticsBuilder withSum(double sum) {
            this.sum = sum;
            return this;
        }

        public PeriodStatistics buid() {
            return new PeriodStatistics(max, min, avg, count, sum);
        }
    }
}
