package org.homeunix.thecave.buddi.model.impl;

import org.homeunix.thecave.buddi.model.BudgetCategoryType;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class BudgetPeriod {
    private final Period period;
    private final BudgetCategoryType type;
    private final long amount;

    public BudgetPeriod(BudgetCategoryType budgetPeriodType, Date date) {
        this(budgetPeriodType, date, 0);
    }

    public BudgetPeriod(BudgetCategoryType budgetPeriodType, Date date, long amount) {
        this.type = budgetPeriodType;
        this.period = new Period(budgetPeriodType.getStartOfBudgetPeriod(date), budgetPeriodType.getEndOfBudgetPeriod(date));
        this.amount = amount;
    }

    private BudgetPeriod nextBudgetPeriod() {
        return new BudgetPeriod(type, type.getBudgetPeriodOffset(period.getStartDate(), 1));
    }

    public List<BudgetPeriod> createBudgetPeriodsTill(BudgetPeriod endBudgetPeriod){
        List<BudgetPeriod> budgetPeriods = new LinkedList<>();

        BudgetPeriod current = this;

        while (current.period.getStartDate().before(endBudgetPeriod.period.getEndDate())){
            budgetPeriods.add(current);
            current = current.nextBudgetPeriod();
		}

		return budgetPeriods;
	}

    public long getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        return period.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        BudgetPeriod another = (BudgetPeriod) obj;
        return period.equals(another.period);
    }

    public double getOverlappingAmountWithPeriod(Period overlappingPeriod) {
        long overlappingDayCount = overlappingPeriod.getOverlappingDayCount(period);
        return (double) amount / (double) period.getDayCount() * overlappingDayCount;
    }
}
