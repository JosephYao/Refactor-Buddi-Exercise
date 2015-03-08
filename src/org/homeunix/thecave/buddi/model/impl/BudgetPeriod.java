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

    public BudgetPeriod nextBudgetPeriod() {
        return new BudgetPeriod(type, type.getBudgetPeriodOffset(period.getStartDate(), 1));
    }

    public Date getStartDate() {
        return this.period.getStartDate();
    }

    public long getDayCount() {
        return this.period.getDayCount();
    }

	public List<BudgetPeriod> createBudgetPeriodsTill(BudgetPeriod endBudgetPeriod){
        List<BudgetPeriod> budgetPeriods = new LinkedList<BudgetPeriod>();

        BudgetPeriod current = this;

        while (current.getStartDate().before(endBudgetPeriod.period.getEndDate())){
            budgetPeriods.add(current);
            current = current.nextBudgetPeriod();
		}

		return budgetPeriods;
	}

    public Period getPeriod() {
        return period;
    }
}
