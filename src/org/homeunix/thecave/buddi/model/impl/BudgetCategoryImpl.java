/*
 * Created on Jul 29, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.model.impl;

import ca.digitalcave.moss.collections.SortedArrayList;
import ca.digitalcave.moss.common.DateUtil;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.ModelObject;
import org.homeunix.thecave.buddi.plugin.api.exception.DataModelProblemException;
import org.homeunix.thecave.buddi.plugin.api.exception.InvalidValueException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of an BudgetCategory.  You should not create this object directly; 
 * instead, please use the ModelFactory to create it, as this will ensure that all
 * required fields are correctly set.
 * @author wyatt
 *
 */
public class BudgetCategoryImpl extends SourceImpl implements BudgetCategory {
	private boolean income;
	private boolean expanded;
	private BudgetCategoryType periodType;
	private BudgetCategory parent;
	private Map<String, Long> amounts;
	private List<BudgetCategory> children;
	private List<BudgetCategory> allChildren;
    private Map<BudgetPeriod, BudgetPeriod> budgetPeriods;

    public Map<String, Long> getBudgetPeriods() {
		if (amounts == null) {
            amounts = new HashMap<String, Long>();
            budgetPeriods = new HashMap<>();
        }
		return amounts;
	}
	public void setBudgetPeriods(Map<String, Long> amounts) {
		this.amounts = amounts;
	}
	/**
	 * Returns the budgeted amount associated with the given budget category, for 
	 * the date in which the given period date exists.
	 * @param periodDate
	 * @return
	 */
	public long getAmountOfBudgetPeriod(Date periodDate){
		Long l = getBudgetPeriods().get(getPeriodKey(periodDate));
        BudgetPeriod budgetPeriodWithAmount = budgetPeriods.get(new BudgetPeriod(getBudgetPeriodType(), periodDate));
		if (l == null || budgetPeriodWithAmount == null)
			return 0;
		return budgetPeriodWithAmount.getAmount();
	}
	
	@Override
	public void setDeleted(boolean deleted) throws InvalidValueException {
		if (getDocument() != null)
			getDocument().startBatchChange();
		//We need to delete / undelete ancestors / descendents as needed.  The rule to follow is that
		// we cannot have any account which is not deleted which has a parent which is deleted.
		//We also cannot set any children deleted if the document is not set.  This should be fine
		// for almost all operations - the only potential problem would be if you create a budget
		// category, add some children to it, delete the parent, and then add the parent.  
		// TODO Check for this condition
		if (getDocument() != null){
			if (deleted){
				//If we delete this one, we must also delete all children.
				for (BudgetCategory bc : getChildren()) {
					bc.setDeleted(deleted);
				}
			}
			else {
				//If we undelete this one, we must also undelete all ancestors.  
				if (getParent() != null)
					getParent().setDeleted(deleted);
			}
		}
		
		setChanged();
		
		super.setDeleted(deleted);
		
		if (getDocument() != null)
			getDocument().finishBatchChange();
	}
	
	public List<BudgetCategory> getChildren() {
		if (children == null)
			children = new FilteredLists.BudgetCategoryListFilteredByDeleted(getDocument(), new FilteredLists.BudgetCategoryListFilteredByParent(getDocument(), getDocument().getBudgetCategories(), this));
		return children;
	}
	
	public List<BudgetCategory> getAllChildren() {
		if (allChildren == null)
			allChildren = new FilteredLists.BudgetCategoryListFilteredByParent(getDocument(), getDocument().getBudgetCategories(), this);
		return allChildren;
	}	
	
	public long getAmount(Date startDate, Date endDate){
		if (startDate.after(endDate))
			throw new RuntimeException("Start date cannot be before End Date!");

        return getAmountWithValidatePeriod(new Period(startDate, endDate));
    }

    private long getAmountWithValidatePeriod(final Period period) {
		return (long) firstBudgetPeriod(period).createBudgetPeriodsTill(lastBudgetPeriod(period)).stream()
				.mapToDouble(budgetPeriod -> getAmountOfOverlappingDays(period, budgetPeriod))
                .sum();
    }

	private BudgetPeriod lastBudgetPeriod(Period period) {
		return new BudgetPeriod(getBudgetPeriodType(), period.getEndDate());
	}

	private BudgetPeriod firstBudgetPeriod(Period period) {
		return new BudgetPeriod(getBudgetPeriodType(), period.getStartDate());
	}

	private double getAmountOfOverlappingDays(Period period, BudgetPeriod budgetPeriod) {
        BudgetPeriod budgetPeriodWithAmount = budgetPeriods.get(budgetPeriod);
        return budgetPeriodWithAmount.getOverlappingAmountWithPeriod(period);
    }

    /**
	 * Sets the budgeted amount for the given time period.
	 * @param periodDate
	 * @param amount
	 */
	public void setAmount(Date periodDate, long amount){
		if (getAmountOfBudgetPeriod(periodDate) != amount)
			setChanged();
		getBudgetPeriods().put(getPeriodKey(periodDate), amount);
        budgetPeriods.put(new BudgetPeriod(getBudgetPeriodType(), periodDate), new BudgetPeriod(getBudgetPeriodType(), periodDate, amount));
	}
	public BudgetCategoryType getPeriodType() {
		return periodType;
	}
	public void setPeriodType(BudgetCategoryType periodType) {
		this.periodType = periodType;
		setChanged();
	}
	public boolean isIncome() {
		return income;
	}
	public void setIncome(boolean income) {
		this.income = income;
		setChanged();
	}
	@Override
	public void setName(String name) throws InvalidValueException {
//		if (getDocument() != null){
//			for (BudgetCategory bc : ((Document) getDocument()).getBudgetCategories()) {
//				if (bc.getName().equalsIgnoreCase(name)
//						&& !bc.equals(this)
//						&& ((bc.getParent() == null && this.getParent() == null)
//								|| (bc.getParent() != null && this.getParent() != null && bc.getParent().equals(this.getParent()))))
//					throw new InvalidValueException("The budget category name must be unique for nodes which share the same parent");
//			}
//		}
		super.setName(name);
	}
	public BudgetCategory getParent() {
		return parent;
	}
	public void setParent(BudgetCategory parent) throws InvalidValueException {
		
		if (children != null) {
			boolean invalidParent = isChild(parent) || parent == this;
			if (invalidParent) {
				throw new InvalidValueException();
			}
		}
		
		this.parent = parent;
		setChanged();
	}
	
	private boolean isChild(BudgetCategory category) {
		
		boolean isChild = children.contains(category);
		
		for (BudgetCategory child : children) {
			isChild = isChild || ((BudgetCategoryImpl) child).isChild(category);
		}
		
		return isChild;
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	/**
	 * Returns the key which is associated with the date contained within the
	 * current budget period.  The string is constructed as follows:
	 * 
	 * <code>String periodKey = getPeriodType() + ":" + getStartOfBudgetPeriod(periodDate).getTime();</code>
	 * 
	 * @param periodDate
	 * @return
	 */
	private String getPeriodKey(Date periodDate){
		Date d = getBudgetPeriodType().getStartOfBudgetPeriod(periodDate); 
		return getBudgetPeriodType().getName() + ":" + DateUtil.getYear(d) + ":" + DateUtil.getMonth(d) + ":" + DateUtil.getDay(d);
	}
	/**
	 * Parses a periodKey to get the date 
	 * @param periodKey
	 * @return
	 */
	private Date getPeriodDate(String periodKey){
		String[] splitKey = periodKey.split(":");
		if (splitKey.length == 4){
			int year = Integer.parseInt(splitKey[1]);
			int month = Integer.parseInt(splitKey[2]);
			int day = Integer.parseInt(splitKey[3]);
			return getBudgetPeriodType().getStartOfBudgetPeriod(DateUtil.getDate(year, month, day));
		}

		throw new DataModelProblemException("Cannot parse date from key " + periodKey);
	}
	public String getFullName(){
		if (getDocument() != null && getParent() != null && !getParent().equals(this)){
			for (BudgetCategory bc : getDocument().getBudgetCategories()) {
				if (bc.getName().equals(this.getName())
						&& !bc.equals(this))
					return this.getName() + " (" + this.getParent().getName() + ")";
			}
		}
		return this.getName();
	}
	/**
	 * Returns the Budget Period type.  One of the values in Enum BudgePeriodKeys.
	 * @return
	 */
	public BudgetCategoryType getBudgetPeriodType() {
		if (getPeriodType() == null)
			setPeriodType(new BudgetCategoryTypeMonthly());
		return getPeriodType();
	}
	@Override
	public int compareTo(ModelObject arg0) {
		if (arg0 instanceof BudgetCategoryImpl){
			BudgetCategoryImpl c = (BudgetCategoryImpl) arg0;
			if (this.isIncome() != c.isIncome())
				return -1 * Boolean.valueOf(this.isIncome()).compareTo(Boolean.valueOf(c.isIncome()));
			return this.getFullName().compareTo(c.getFullName());
		}
		return super.compareTo(arg0);
	}
	
	public List<Date> getBudgetedDates() {
		List<Date> budgetedDates = new SortedArrayList<Date>();
		
		Map<String, Long> amounts = getBudgetPeriods();
		for (String key : amounts.keySet()){
			if (amounts.get(key) != null && amounts.get(key) != 0)
				budgetedDates.add(getPeriodDate(key));
		}
		
		return budgetedDates;
	}
	
	BudgetCategory clone(Map<ModelObject, ModelObject> originalToCloneMap) throws CloneNotSupportedException {

		if (originalToCloneMap.get(this) != null)
			return (BudgetCategory) originalToCloneMap.get(this);
		
		BudgetCategoryImpl b = new BudgetCategoryImpl();
		originalToCloneMap.put(this, b);

		b.document = (Document) originalToCloneMap.get(document);
		b.expanded = expanded;
		b.income = income;
		b.periodType = periodType;
		b.deleted = isDeleted();
		b.modifiedTime = new Time(modifiedTime);
		b.name = name;
		b.notes = notes;
		if (parent != null)
			b.parent = (BudgetCategory) ((BudgetCategoryImpl) parent).clone(originalToCloneMap);
		b.amounts = new HashMap<String, Long>();
		if (amounts != null){
			for (String s : amounts.keySet()) {
				b.amounts.put(s, amounts.get(s).longValue());
			}
		}
		
		return b;
	}
}
