package org.homeunix.thecave.buddi.model.impl;

import org.homeunix.thecave.buddi.model.BudgetCategoryType;

import java.util.Date;

public class BudgetPeriod {
    private final Period period;
    private final long amount;

    public BudgetPeriod(BudgetCategoryType budgetPeriodType, Date date, long amount) {
        this.period = new Period(budgetPeriodType.getStartOfBudgetPeriod(date), budgetPeriodType.getEndOfBudgetPeriod(date));
        this.amount = amount;
    }

    public double getOverlappingAmountWithPeriod(Period overlappingPeriod) {
        return (double) amount / (double) period.getDayCount() * overlappingDayCount(overlappingPeriod);
    }

    private long overlappingDayCount(Period overlappingPeriod) {
        return overlappingPeriod.getOverlappingDayCount(period);
    }
}
