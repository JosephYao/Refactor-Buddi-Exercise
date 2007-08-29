/*
 * Created on Apr 11, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.plugin.api.util;

import java.text.DateFormat;

import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.plugin.api.model.ImmutableAccount;
import org.homeunix.thecave.buddi.plugin.api.model.ImmutableBudgetCategory;
import org.homeunix.thecave.buddi.plugin.api.model.ImmutableSource;
import org.homeunix.thecave.buddi.plugin.api.model.ImmutableTransaction;
import org.homeunix.thecave.buddi.plugin.api.model.ImmutableAccountType;
import org.homeunix.thecave.buddi.util.InternalFormatter;
import org.homeunix.thecave.moss.util.Formatter;

/**
 * Gives a common framework for formatting text.  It is recommended that all plugins which
 * display amounts, accounts, transactions, etc. use this class to ensure common formatting
 * is used for all areas of Buddi.
 * 
 * @author wyatt
 *
 */
public class TextFormatter {
	/**
	 * Get the date format which the user has specified.  You can format dates
	 * by using the getDateFormat().format(Date d) method.
	 * @return
	 */
	public static DateFormat getDateFormat(){
		return Formatter.getDateFormat(PrefsModel.getInstance().getDateFormat());
	}
	
	/**
	 * Return the translation for the given key.
	 * @param key
	 * @return
	 */
	public static String getTranslation(String key){
		return PrefsModel.getInstance().getTranslator().get(key);
	}
	
	/**
	 * Return the translation for the given key.
	 * @param key
	 * @return
	 */
	public static String getTranslation(Enum key){
		return PrefsModel.getInstance().getTranslator().get(key);
	}

	public static String getHtmlWrapper(String htmlToWrap){
		return "<html>" + htmlToWrap + "</html>";
	}
	
	/**
	 * Converts a long value (in cents: 10000 == $100.00) to a string
	 * with proper decimal values, with the user's desired currency
	 * sign in the user's specified position (whether behind or in front
	 * of the amount).  It is highly recommended that you use this method
	 * to output monetary values, as it presents the user with a constant
	 * look for currency.
	 * 
	 * @param value The currency amount, in cents (as per Buddi's internal 
	 * representation of currency).  For instance, to represent the value
	 * $123.45, you would pass in 12345.
	 * @param red Wrap the return string in HTML font tags to make it red.
	 * @param negate Multiply the value by *1 before rendering.  
	 * @return A string with proper decimal places, plus the user's defined 
	 * currency symbol in the correct position (whether before or after the
	 * amount).  Optionally it will be wrapped in red font tags.
	 */
	public static String getFormattedCurrency(long value, boolean isRed){
		return getFormattedCurrency(value, isRed, false);
	}
	
	/**
	 * Returns the given value formatted as a currency.
	 * @param value
	 * @return
	 */
	public static String getFormattedCurrency(long value){
		return getFormattedCurrency(value, value < 0);
	}
	
	/**
	 * Converts a long value (in cents: 10000 == $100.00) to a string
	 * with proper decimal values, with the user's desired currency
	 * sign in the user's specified position (whether behind or in front
	 * of the amount).  It is highly recommended that you use this method
	 * to output monetary values, as it presents the user with a constant
	 * look for currency.
	 * 
	 * Note that this method uses HTML font tags to set the color.  This
	 * means that you must wrap all code which uses this with HTML start 
	 * and end tags - you can use the getHtmlWrapper() method to do this.
	 * 
	 * @param value The currency amount, in cents (as per Buddi's internal 
	 * representation of currency).  For instance, to represent the value
	 * $123.45, you would pass in 12345.
	 * @param isRed Wrap the return string in HTML font tags to make it red.
	 * @param negate Multiply the value by *1 before rendering.  
	 * @return A string with proper decimal places, plus the user's defined 
	 * currency symbol in the correct position (whether before or after the
	 * amount).  Optionally it will be wrapped in red font tags.
	 */
	public static String getFormattedCurrency(long value, boolean isRed, boolean negate){
		if (negate)
			value *= -1;

		boolean symbolAfterAmount = PrefsModel.getInstance().isShowCurrencyAfterAmount();
		String symbol = PrefsModel.getInstance().getCurrencySign();

		
		String formatted = 
			(isRed ? "<font color='red'>" : "")
			+ (symbolAfterAmount ? "" : symbol)
			+ Formatter.getDecimalFormat().format((double) value / 100.0)  
			+ (symbolAfterAmount ? " " + symbol : "")
			+ (isRed ? "</font>" : "");

		return formatted;
	}

	/**
	 * Returns the name, formatted as a String.  This method complies
	 * with the Buddi Formatting Standard, and should be used whenever
	 * possible.
	 * @param a
	 * @return
	 */
	public static String getFormattedNameForAccount(Account a){
		return getFormattedNameGeneric(a.toString(), a.getType().isCredit());
	}

	/**
	 * Returns the name, formatted as a String.  This method complies
	 * with the Buddi Formatting Standard, and should be used whenever
	 * possible.
	 * @param c
	 * @return
	 */
	public static String getFormattedNameForCategory(BudgetCategory c){
		return getFormattedNameGeneric(c.toString(), !c.isIncome());
	}

	/**
	 * Returns the name, formatted as a String.  This method complies
	 * with the Buddi Formatting Standard, and should be used whenever
	 * possible.
	 * @param t
	 * @return
	 */
	public static String getFormattedNameForType(AccountType t){
		return getFormattedNameGeneric(t.toString(), t.isCredit());
	}

	/**
	 * Returns the given string, optionally wrapped in red font tags.
	 * @param name
	 * @param isRed
	 * @return
	 */
	public static String getFormattedNameGeneric(String name, boolean isRed){
		String formatted = 
			(isRed ? "<font color='red'>" : "")
			+ PrefsModel.getInstance().getTranslator().get(name)  
			+ (isRed ? "</font>" : "");

		return formatted;
	}
	
	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(ImmutableSource s){
		return InternalFormatter.isRed(s.getSource());
	}

	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(ImmutableAccount a, long value){
		return InternalFormatter.isRed(a.getAccount(), value);
	}
	
	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(ImmutableBudgetCategory c, long value){
		return InternalFormatter.isRed(c.getBudgetCategory(), value);
	}

	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(ImmutableAccountType t){
		return InternalFormatter.isRed(t.getType());
	}

	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(ImmutableAccountType t, long value){
		return InternalFormatter.isRed(t.getType(), value);
	}
	
	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(ImmutableTransaction t){
		return InternalFormatter.isRed(t.getTransaction());
	}
	
	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(ImmutableTransaction t, boolean toSelectedAccount){
		return InternalFormatter.isRed(t.getTransaction(), toSelectedAccount);
	}

	/**
	 * Should the arguments be colored in red text when displayed?
	 * @param s
	 * @return
	 */
	public static boolean isRed(long value){
		return InternalFormatter.isRed(value);
	}
}
