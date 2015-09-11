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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents data for a trip that spans start and end points defined
 * by two gas records.
 */
public class TripRecord {
	
	/// the date the trip started
	private Date startDate;
	
	/// the date the trip ended
	private Date endDate;
	
    /// the distance driven 
    private Integer distance;
    
    /// the amount of gasoline purchased 
    private Float gallons;
    
    /// the cost of the gasoline purchased
    private Double cost;
    
    // the set of gas records that the trip represents
    private Set<GasRecord> records = new HashSet<GasRecord>();
    
    /**
     * Constructs an empty instance of TripRecord for a specified date
     * @param date The start/end date for the trip.
     */
    public TripRecord(Date date) {
    	startDate = date;
    	endDate = date;
    	distance = 0;
    	gallons = 0f;
    	cost = 0d;
    }

    /**
     * Constructs an instance of TripRecord reflecting a trip
     * between two gas stops.
     * @param start GasRecord marking the start of the trip.
     * @param end GasRecord marking the end of the trip.
     */
    public TripRecord(GasRecord start, GasRecord end) {
    	startDate = start.getDate();
    	endDate = end.getDate();
    	distance = end.getOdometer() - start.getOdometer();
    	gallons = end.getGallons();
    	cost = end.getCost();
    	//records.add(start);
    	records.add(end);
    }
    
    /**
     * Append the data for another trip to this trip record, such
     * that this trip record now reflects the totals for both trips.
     * @param that The TripRecord to append.
     */
    public void append(TripRecord that) {

    	// take the earliest start date
    	if (that.startDate.before(this.startDate)) {
    		this.startDate = that.startDate;
    	}
    	
    	// take the latest end date
    	if (that.endDate.after(this.endDate)) {
    		this.endDate = that.endDate;
    	}

    	// calculate totals for both trips
    	this.distance += that.distance;
    	this.gallons += that.gallons;
    	this.cost += that.cost;
    	this.records.addAll(that.records);
    }
    
	/**
	 * Getter method for the start date attribute.
	 * @return The Date that the trip started.
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Getter method for the end date attribute.
	 * @return The Date that the trip ended.
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Getter method for the distance attribute.
	 * @return The distance driven during the trip.
	 */
	public Integer getDistance() {
		return distance;
	}

	/**
	 * Getter method for the gallons attribute.
	 * @return The amount of gas purchased during the trip.
	 */
	public Float getGallons() {
		return gallons;
	}

	/**
	 * Getter method for the cost attribute.
	 * @return The total cost of gas purchased during the trip.
	 */
	public Double getCost() {
		return cost;
	}
	
	/**
	 * Getter method for the gas record set attribute.
	 * @return The set of gas records that the trip represents.
	 */
	public Set<GasRecord> getGasRecords() {
		return records;
	}

	/**
	 * Getter method for average price per gallon paid for fuel
	 * over the trip period.
	 * @return The price of fuel per gallon.
	 */
	public Double getPrice() {
		Double price = 0.0d;
		if (gallons > 0) {
			price = cost/gallons;
		}
		return price;
	}
	
}
