package org.homeunix.thecave.buddi.model.impl;

import org.homeunix.thecave.buddi.model.BudgetCategoryType;

import java.util.Date;

public class BudgetPeriod {
    private final Period period;
    private final BudgetCategoryType type;

    public BudgetPeriod(BudgetCategoryType budgetPeriodType, Date date) {
        this.type = budgetPeriodType;
        this.period = new Period(budgetPeriodType.getStartOfBudgetPeriod(date), budgetPeriodType.getEndOfBudgetPeriod(date));
    }

    public boolean equals(Object obj) {
        BudgetPeriod anotherBudgetPeriod = (BudgetPeriod) obj;
        return this.period.equals(anotherBudgetPeriod.period);
    }

    public BudgetPeriod nextBudgetPeriod() {
        return new BudgetPeriod(type, type.getStartOfNextBudgetPeriod(period.getStartDate()));
    }

    public Date getStartDate() {
        return this.period.getStartDate();
    }

    public BudgetPeriod previousBudgetPeriod() {
        return new BudgetPeriod(type, type.getStartOfPreviousBudgetPeriod(period.getStartDate()));
    }

    public Date getEndDate() {
        return this.period.getEndDate();
    }
}
