/*
 * *****************************************************************************
 * Copyright 2014 William D. Kraemer
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *    
 * ****************************************************************************
 */

package com.github.wdkapps.fillup;

import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Manages currency settings and the formatting of currency values for 
 * specific locales.
 */
public class CurrencyManager implements OnSharedPreferenceChangeListener {
	
	/// for logging
	private static final String TAG = CurrencyManager.class.getName();
	
	/// key string for default currency (application locale currency)
	public static final String DEFAULT_CURRENCY = App.getContext().getString(R.string.default_currency);
	
	/// singleton instance
	private static CurrencyManager instance = null;
	
    /// map of currency key strings to currency locales
    private static Map<String,Locale> localeMap = new HashMap<String,Locale>();
    static {
    	getAvailableCurrencies();
    	initialize();
    }
    
    /// locale for currently selected currency
    private Locale locale;
    
    /// a formatter for currency values as strings including appropriate currency symbols
    private CurrencyFormatter symbolicFormatter = null;
	
    /// a formatter for currency values as strings NOT including appropriate currency symbols
	private CurrencyFormatter numericFormatter = null;
	
    /// a formatter for fractional currency values as strings including appropriate currency symbols
	private CurrencyFormatter symbolicFractionalFormatter = null;
	
    /// a formatter for fractional currency values as strings NOT including appropriate currency symbols
	private CurrencyFormatter numericFractionalFormatter = null;
	
	/**
	 * Obtains a singleton instance of the CurrencyManager
	 * @return CurrencyManager instance.
	 */
	public static CurrencyManager getInstance() {
		if (instance == null) {
			instance = new CurrencyManager();
		}
		return instance;
	}
	
	/**
	 * Returns a key string for a specific currency/locale.
     * @param locale Locale
	 * @param currency Currency
	 * @return String
	 */
	private static String getKey(Locale locale, Currency currency) {
    	return String.format("%s - %s",
				currency.getCurrencyCode(),
				locale.getDisplayName(App.getLocale()));
	}

	/**
	 * Initializes the currency setting to a default value if no other
	 * value has been selected yet. 
	 */
	private static void initialize() {
		if (Settings.getString(Settings.KEY_CURRENCY,null) == null) {
			Settings.setString(Settings.KEY_CURRENCY,DEFAULT_CURRENCY);
		}
	}
	
	/**
	 * Obtains a list of available currencies and generates a map
	 * to define key/value pairs.
	 */
	private static void getAvailableCurrencies() {
		
		final String tag = TAG+".getAvailableCurrencies()";
		
	    Locale[] locales = Locale.getAvailableLocales();

	    for(Locale locale : locales) {
	        try {
	        	
	        	Log.d(tag, locale.getLanguage() + " " + locale.getCountry() );
	        	
	        	Currency currency = Currency.getInstance(locale); 
	        	
	        	String key = getKey(locale,currency);

	        	localeMap.put(key,locale);
	        	
	        } catch (IllegalArgumentException ex)
	        {
	    		Log.d(tag,"locale's country is not a supported ISO 3166 country: "+locale.getCountry());
	        }
	    }
	    
	    localeMap.put(DEFAULT_CURRENCY,App.getLocale());
	}
	
	/**
	 * Constructs an instance of CurrencyManager.
	 */
	private CurrencyManager() {
		
		// get locale for preferred currency
		getCurrencyLocale();
		
        // setup to be notified when shared preferences change
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	/**
	 * Get locale for preferred currency.
	 */
	private void getCurrencyLocale() {
		try {
			String key = Settings.getString(Settings.KEY_CURRENCY,DEFAULT_CURRENCY);
			locale = localeMap.get(key);
			if (locale == null) throw new Exception("map get failed!");
		} catch (Throwable t) {
			Log.e(TAG+".getCurrencyLocale()","unable to initialize preferred currency, using app locale",t);
			locale = App.getLocale();
			Settings.setString(Settings.KEY_CURRENCY,DEFAULT_CURRENCY);
		}
	}
	
	/**
	 * Returns the symbol for the currently selected currency/locale.
	 * @return Currency symbol as a String
	 */
	public String getCurrencySymbol() {
		String symbol = "?";
		try {
			symbol = Currency.getInstance(locale).getSymbol(locale);
		} catch (IllegalArgumentException e) {
			Log.e(TAG+".getCurrencySymbol()","unable to get symbol",e);
		}
		return symbol;
	}
	
	/**
	 * Returns a list of preference entry strings for selection as
	 * a currency setting.
	 * @return Array of entry Strings
	 */
	public String[] getPrefEntries() {
		Set<String> keyset = localeMap.keySet();
		String[] entries = keyset.toArray(new String[keyset.size()]);
		Arrays.sort(entries);
		return entries;
	}
	
	/**
	 * Returns a list of preference entry value strings for selection as
	 * a currency setting.
	 * @return Array of entry value Strings
	 */
	public String[] getPrefEntryValues() {
		return getPrefEntries();
	}
	
	/**
	 * Returns a summary string to describe the current currency setting.
	 * @return Summary String
	 */
	public String getPrefSummary() {
		return Settings.getString(Settings.KEY_CURRENCY, DEFAULT_CURRENCY);
	}
	
	/**
	 * Obtains an instance of a currency formatter for display
	 * of currency values with currency symbol.
	 * @return CurrencyFormatter
	 */
	public CurrencyFormatter getSymbolicFormatter() {
		
		if (symbolicFormatter == null) {
			symbolicFormatter = new CurrencyFormatter(false);
			symbolicFormatter.setLocale(locale);
		}

		return symbolicFormatter;
	}
	
	/**
	 * Obtains an instance of a currency formatter for display
	 * of currency values without currency symbol.
	 * @return CurrencyFormatter
	 */
	public CurrencyFormatter getNumericFormatter() {
		
		if (numericFormatter == null) {
			numericFormatter = new CurrencyFormatter(true);
			numericFormatter.setLocale(locale);
		}

		return numericFormatter;
	}

	/**
	 * Obtains an instance of a currency formatter for display
	 * of fractional currency values with currency symbol.
	 * @return CurrencyFormatter
	 */
	public CurrencyFormatter getSymbolicFractionalFormatter() {
		
		if (symbolicFractionalFormatter == null) {
			symbolicFractionalFormatter = new FractionalCurrencyFormatter(false);
			symbolicFractionalFormatter.setLocale(locale);
		}

		return symbolicFractionalFormatter;
	}
	
	/**
	 * Obtains an instance of a currency formatter for display
	 * of fractional currency values without currency symbol.
	 * @return PriceFormatter
	 */
	public CurrencyFormatter getNumericFractionalFormatter() {
		
		if (numericFractionalFormatter == null) {
			numericFractionalFormatter = new FractionalCurrencyFormatter(true);
			numericFractionalFormatter.setLocale(locale);
		}

		return numericFractionalFormatter;
	}
	
	/**
	 * Called when Settings have changed. Updates the formatters to utilize the 
	 * selected currency/locale. 
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(Settings.KEY_CURRENCY)) {
			getCurrencyLocale();
			if (symbolicFormatter != null) symbolicFormatter.setLocale(locale);
			if (numericFormatter != null) numericFormatter.setLocale(locale);
			if (symbolicFractionalFormatter != null) symbolicFractionalFormatter.setLocale(locale);
			if (numericFractionalFormatter != null) numericFractionalFormatter.setLocale(locale);
		} 
		
	}
	
}
