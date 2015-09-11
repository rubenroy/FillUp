/*
 * *****************************************************************************
 * Copyright 2013 William D. Kraemer
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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * A gasoline log entry to document the details of a gasoline purchase. 
 * It records: purchase date, odometer reading, amount of fuel purchased, etc.
 * Needs to be Serializable in order to pass between Activity instances via 
 * an Intent instance.
 */
/**
 *
 */
public class GasRecord implements Serializable {

	private static final long serialVersionUID = -2409546847119346248L;
	
	// define maximum values (for display reasons)
	public static final int MAX_ODOMETER = 9999999;
	public static final float MAX_GALLONS = 9999.999f;
	public static final double MAX_COST = 999999.999d;
	public static final double MAX_PRICE = 999999.999d;
	
    /// get a locale specific date formatter 
    private static final java.text.DateFormat dateFormatter = 
    		android.text.format.DateFormat.getDateFormat(App.getContext());
    
    /// get a locale specific time formatter 
    private static final java.text.DateFormat timeFormatter = 
    		android.text.format.DateFormat.getTimeFormat(App.getContext());
    
    /// get non-locale specific date/time formatters for internal CSV use
    private static final SimpleDateFormat csvDateFormatter = 
    		new SimpleDateFormat("MM/dd/yyyy",Locale.US);
    private static final SimpleDateFormat csvDateTimeFormatter = 
    		new SimpleDateFormat("MM/dd/yyyy HH:mm",Locale.US);
    
    /// record id for database use (primary key)
    private Integer id;
    
    /// id of the vehicle this record corresponds to (foreign key)
    private Integer vid;
    
	/// the date gasoline was purchased 
    private Date date;
	
    /// odometer reading at the time of purchase 
    private Integer odometer;
    
    /// gasoline price per gallon 
    private Double price;
    
    /// amount of gasoline purchased 
    private Float gallons;
    
    /// the total cost of the gasoline purchased
    private Double cost;
    
    /// textual notes about the purchase 
    private String notes;
    
    /// flag to indicate whether gas tank was full after purchase 
    private Boolean fulltank;
    
    /// flag to indicate whether the calculation for this record is hidden 
    private Boolean hidden;
    
    ///  gas mileage calculation (if the tank was full, null otherwise) 
    private MileageCalculation calc;
    
    /// array of record attributes included in hash code calculations
    private Object[] hash;
    
	/**
	 * Constructs a 'blank' instance of GasRecord.
	 */
	public GasRecord() {
		id = null;
		vid = null;
        date = new Date();
    	gallons = Float.valueOf(0.0f);
    	odometer = Integer.valueOf(0);
    	cost = Double.valueOf(0.0d);
    	price = Double.valueOf(0.0d);
    	notes = "";
        fulltank = false;
        hidden = false;
        calc = null;
	}
	
	/**
	 * Constructs a copy of an existing GasRecord.
	 * @param that The existing GasRecord instance to copy.
	 */
	public GasRecord(GasRecord that) {
		this.id = that.id;
		this.vid = that.vid;
		this.date = new Date();
		this.date.setTime(that.date.getTime());
		this.gallons = Float.valueOf(that.gallons);
		this.odometer = Integer.valueOf(that.odometer);
		this.cost = Double.valueOf(that.cost);
		this.price = Double.valueOf(that.price);
		this.notes = new String(that.notes);
		this.fulltank = Boolean.valueOf(that.fulltank);
		this.hidden = Boolean.valueOf(that.hidden);
		this.calc = null;
	}
	
	/**
	 * Constructs an GasRecord instance for a specific vehicle.
	 * @param vehicle The Vehicle that the record pertains to.
	 */
	public GasRecord(Vehicle vehicle) {
		this();
		vid = vehicle.getID();
	}
	
	/**
	 * Constructs an instance of GasRecord reflecting values specified
	 * by an ASCII comma-separated-values (CSV) String.<p>
	 * 
	 * NOTES:<p>
	 * <ol>
	 * <li>This method needs to be backwards compatible with CSV 
	 * data created with previous versions of the application database.
	 *     <ul>
	 *     <li>formatting prior to database version 5 was:<br>
	 *     <pre>date,odometer,gallons,fulltank,hidden,[calculation]</pre>
	 *     <li>formatting for database version 5 is:<br>
	 *     <pre>date,odometer,gallons,fulltank,hidden,cost,notes,[calculation]</pre>
	 *     </ul>
	 * <li>Calculation values are for user information only in the CSV. They
	 * are ignored in this constructor and re-calculated later if necessary. 
	 * </ol>
	 * 
	 * @param csv The ASCI CSV String that specified the record's values.
	 * @throws ParseException if parse of CSV fails.
	 * @throws NumberFormatException if CSV contains invalid numeric values.
	 */
	public GasRecord(String csv) throws ParseException, NumberFormatException {
		this();
		String[] values = csv.split(",");

		switch(values.length){
		case 8:  // db_version=5 with calculation
		case 7:  // db_version=5
			setCsvDateTime(values[0]);
			setOdometer(values[1]);
			setGallons(values[2]);
			setFullTank(values[3]);
			setHiddenCalculation(values[4]);
			setCost(values[5]);
			setNotes(values[6]);
			calculatePrice();
			break;
			
		case 6:  // db_version<5 with calculation
		case 5:  // db_version<5
			setCsvDateTime(values[0]);
			setOdometer(values[1]);
			setGallons(values[2]);
			setFullTank(values[3]);
			setHiddenCalculation(values[4]);
			setCost(0d);
			setNotes("");
			calculatePrice();
			break;
			
		default:
			throw new ParseException("Invalid CSV length",values.length);
		}
	}
	
	/**
	 * Calculates price based on the current values for cost and gallons.
 	 * @throws NumberFormatException if the calculated value is not a valid price value.
	 */
	public void calculatePrice() {
		Double value = 0d;
		if (gallons != 0) {
			value = (double)(cost/gallons);
		}
		setPrice(value.toString());
	}
	
	/**
	 * Calculates gallons based on the current values for cost and price.
	 * @throws NumberFormatException if the calculated value is not a valid gallons value.
	 */
	public void calculateGallons() {
		Float value = 0f;
		if (price != 0) {
			value = (float)(cost/price);
		}
		setGallons(value.toString());
	}

	/**
	 * Calculates cost based on the current values for gallons and price.
	 * @throws NumberFormatException if the calculated value is not a valid cost value.
	 */
	public void calculateCost() {
		Double value = price * gallons;
		setCost(value.toString());
	}
	
	/**
	 * Getter method for the id attribute.
	 * @return The id value.
	 */
	public Integer getID() {
		return id;
	}
	
	/**
	 * Setter method for the id attribute.
	 * @param id The Integer id value.
	 */
	public void setID(Integer id) {
		this.id = id;
	}
	
	/**
	 * Getter method for the vehicle id attribute.
	 * @return The vehicle id value.
	 */
	public Integer getVehicleID() {
		return vid;
	}
	
	/**
	 * Setter method for the vehicle id attribute.
	 * @param vid The Integer vehicle id value.
	 */
	public void setVehicleID(Integer vid) {
		this.vid = vid;
	}
	
	/**
	 * Getter method for the date attribute.
	 * @return Date The date value
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * Getter method for the date attribute as a String value.
	 * @return The date value (MM/dd/yyyy).
	 */
	public String getDateString() {
		return dateFormatter.format(date);
	}
	
	/**
	 * Getter method for the date attribute as a date/time String value.
	 * @return The date/time value.
	 */
	public String getDateTimeString() {
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormatter.format(date));
		sb.append(" ");
		sb.append(timeFormatter.format(date));
		return sb.toString();
	}
	
	/**
	 * Getter method for the date attribute as a String value.
	 * @return The date/time value (MM/dd/yyyy HH:mm).
	 */
	private String getCsvDateTimeString() {
		return csvDateTimeFormatter.format(date);
	}

	/**
	 * Setter method for the date attribute.
	 * @param date The Date value.
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * Setter method for the date attribute.
	 * @param date The date/time as a String value.
	 * @throws ParseException if the String is not a valid date/time or date.
	 */
	private void setCsvDateTime(String date) throws ParseException {
		try {
			// assume date & time specified in string
			this.date = csvDateTimeFormatter.parse(date);
		} catch (ParseException e) {
			//...but also allow just date 
			this.date = csvDateFormatter.parse(date);
		}
	}
	
	/**
	 * Getter method for the odometer attribute.
	 * @return The odometer value.
	 */
	public Integer getOdometer() {
		return odometer;
	}
	
	/**
	 * Getter method for the odometer attribute as a String value.
	 * @return The odometer value. 
	 */
	public String getOdometerString() {
		return odometer.toString();
	}

	/**
	 * Setter method for the odometer attribute.
	 * @param odometer The odometer value as an Integer.
	 */
	public void setOdometer(Integer odometer) {
		this.odometer = odometer;
	}
	
	/**
	 * Setter method for the odometer attribute as a String value.
	 * @param odometer The odometer String value.
	 * @throws NumberFormatException if the String is not a valid odometer value.
	 */
	public void setOdometer(String odometer) throws NumberFormatException {
		Integer value = Integer.valueOf(odometer);
		if ((value < 0) || (value > MAX_ODOMETER)) {
			throw new NumberFormatException("Value out of range.");
		}
		setOdometer(value);
	}

	/**
	 * Getter method for the gallons attribute.
	 * @return The gallons value.
	 */
	public Float getGallons() {
		return gallons;
	}
	
	/**
	 * Getter method for the gallons attribute as a String value.
	 * @return The gallons value.
	 */
	public String getGallonsString() {
		return String.format(App.getLocale(),"%.3f",gallons);
	}

	/**
	 * Setter method for the gallons attribute.
	 * @param gallons The gallons value as a Float.
	 */
	public void setGallons(Float gallons) {
		this.gallons = gallons;
	}
	
	/**
	 * Setter method for the gallons attribute.
	 * @param gallons The gallons attribute as a String value.
	 * @throws NumberFormatException if the String is not a valid gallons value.
	 */
	public void setGallons(String gallons) throws NumberFormatException {
		this.gallons = 0f;
		Float value = Float.valueOf(gallons.replace(',','.'));
		if ((value <= 0) || (value > MAX_GALLONS)) {
			throw new NumberFormatException("Value out of range.");
		}
		setGallons(value);
	}

	/**
	 * Getter method for the cost attribute.
	 * @return The cost value.
	 */
	public Double getCost() {
		return cost;
	}
	
	/**
	 * Getter method for the cost attribute as a String value.
	 * @return The cost value.
	 */
	public String getCostString() {
		return CurrencyManager.getInstance().getNumericFormatter().format(cost);
	}

	/**
	 * Setter method for the cost attribute.
	 * @param cost The cost value as a Double.
	 */
	public void setCost(Double cost) {
		this.cost = cost;
	}
	
	/**
	 * Setter method for the cost attribute.
	 * @param cost The cost attribute as a String value.
	 * @throws NumberFormatException if the String is not a valid cost value.
	 */
	public void setCost(String cost) throws NumberFormatException {
		this.cost = 0d;
		Double value = Double.valueOf(cost.replace(',','.'));
		if ((value < 0) || (value > MAX_COST)) {
			throw new NumberFormatException("Value out of range.");
		}
		setCost(value);
	}

	/**
	 * Getter method for the calculated price per gallon value.
	 * @return The cost per gallon value.
	 */
	public Double getPrice() {
		return price;
	}
	
	/**
	 * Getter method for the price per gallon attribute as a String value.
	 * @return The price value.
	 */
	public String getPriceString() {
		return CurrencyManager.getInstance().getNumericFractionalFormatter().format(price);
	}

	/**
	 * Setter method for the price per gallon attribute.
	 * <p>
	 * NOTE: 
	 * Price is not a persistent attribute. It is a calculated  
	 * ratio of cost and gallons. This setter method is provided
	 * to assist data entry.
	 * </p>
	 * @param price The price attribute as a Double.
	 */
	private void setPrice(Double price) {
		this.price = price;
	}
	
	/**
	 * Setter method for the price per gallon attribute.
	 * <p>
	 * NOTE: 
	 * Price is not a persistent attribute. It is a calculated  
	 * ratio of cost and gallons. This setter method is provided
	 * to assist data entry.
	 * </p>
	 * @param price The price attribute as a String value.
	 * @throws NumberFormatException if the String is not a valid price value.
	 */
	public void setPrice(String price) {
		this.price = 0d;
		Double value = Double.valueOf(price.replace(',','.'));
		if ((value < 0) || (value > MAX_PRICE)) {
			throw new NumberFormatException("Value out of range.");
		}
		setPrice(value);
	}
	
	/**
	 * Getter method for the notes attribute.
	 * @return The notes attribute value.
	 */
	public String getNotes() {
		return this.notes;
	}

	/**
	 * Setter method for the notes attribute.
	 * @param notes The notes value as a String.
	 */
	public void setNotes(String notes) {
		if (notes == null) notes = "";
		this.notes = notes;
	}
	
	/**
	 * Getter method for the full tank attribute. 
	 * @return Indicates whether the tank was full after purchase (true=full)
	 */
	public Boolean isFullTank() {
		return fulltank;
	}

	/**
	 * Setter method for the full tank attribute.
	 * @param fulltank The Boolean full tank value.
	 */
	public void setFullTank(Boolean fulltank) {
		this.fulltank = fulltank;
	}

	/**
	 * Setter method for the full tank attribute. 
	 * @param fulltank The full tank attribute as a String value ("true","false")
	 */
	public void setFullTank(String fulltank) {
		this.fulltank = Boolean.parseBoolean(fulltank);
	}

	/**
	 * Getter method for the calculation hidden attribute. 
	 * @return Indicates whether the calculation is hidden (true=hidden)
	 */
	public Boolean isCalculationHidden() {
		return hidden;
	}

	/**
	 * Setter method for the calculation hidden attribute. 
	 * @param hidden The Boolean hidden value.
	 */
	public void setHiddenCalculation(Boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Setter method for the calculation hidden attribute. 
	 * @param hidden The hidden attribute as a String value ("true","false")
	 */
	public void setHiddenCalculation(String hidden) {
		this.hidden = Boolean.parseBoolean(hidden);
	}
	
	/**
	 * Getter method for the mileage calculation attribute.
	 * @return The MileageCalculation instance (null=no calculation).
	 */
	public MileageCalculation getCalculation() {
		return calc;
	}
	
	/**
	 * Setter method for the mileage calculation attribute.
	 * @param calc The MileageCalculation instance.
	 */
	public void setCalculation(MileageCalculation calc) {
		this.calc = calc;
	}
	
	/**
	 * Indicates whether the record has a mileage calculation.
	 * @return true if mileage calculation is available.
	 */
	public boolean hasCalculation() {
		return (calc != null);
	}

	/**
	 * Returns an ASCII CSV String representation of the record.
	 * @return String reflecting record attribute values.
	 */
	public String toStringCSV() {
		
		String csv = 
				getCsvDateTimeString() + "," + 
				getOdometerString() + "," + 
				getGallonsString().replace(',','.') + "," + 
				isFullTank() + "," + 
				isCalculationHidden() + "," +
				getCostString().replace(',','.') + "," + 
				getNotes().replace(',',' ').replace('\n',' ');
		
		if (hasCalculation()) {
			csv += "," + calc.getMileageString().replace(',','.');
		}
		
		return csv;
	}
	
	/**
	 * Returns a String representation of the record for debug/logging purposes.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GasRecord [id=" + id + 
				", vid=" + vid + 
				", date=" + getDateString() +
				", odometer=" + odometer + 
				", gallons=" + gallons +
				", fulltank=" + fulltank + 
				", hidden=" + hidden +
				", cost=" + cost +
				", notes=" + notes + 
				", calc=" + calc + 
				", price=" + price +
				"]";
	}

	/**
	 * Constructs an array of all attributes to be considered in
	 * hash code calculations for this record.
	 * @return An Object[] of attributes to be hashed.
	 */
	private Object[] getHashArray() {

		// create the array (only once for performance)
		if (hash == null) {
			hash = new Object[10];
		}
		
		// populate the array
		hash[0] = id;
		hash[1] = vid;
        hash[2] = date;
    	hash[3] = gallons;
    	hash[4] = odometer;
    	hash[5] = cost;
    	hash[6] = notes;
        hash[7] = fulltank;
        hash[8] = hidden;
        hash[9] = price;
        return hash;
	}

	/**
	 * Calculates an integer hash code for this record. 
	 * @see java.lang.Object#hashCode()
	 * @return The integer hash code.
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(getHashArray());
	}

	/**
	 * Compares this instance with the specified object and indicates 
	 * if they are equal. 
	 * @return true if the specified object is equal to this record; false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof GasRecord))
			return false;

		GasRecord that = (GasRecord)obj;
		return Arrays.equals(this.getHashArray(),that.getHashArray());
	}
}
