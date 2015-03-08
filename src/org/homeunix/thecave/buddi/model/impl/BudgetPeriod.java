package org.homeunix.thecave.buddi.model.impl;

import org.homeunix.thecave.buddi.model.BudgetCategoryType;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

    public long getDayCount() {
        return this.period.getDayCount();
    }

	public List<BudgetPeriod> createBudgetPeriodsTill(BudgetPeriod endBudgetPeriod){
        List<BudgetPeriod> budgetPeriods = new LinkedList<BudgetPeriod>();

        BudgetPeriod current = this;

		while (current.getStartDate().before(endBudgetPeriod.getEndDate())){
            budgetPeriods.add(current);
            current = current.nextBudgetPeriod();
		}

		return budgetPeriods;
	}
}
