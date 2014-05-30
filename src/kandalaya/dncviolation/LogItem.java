/*
 * Individual call/SMS log items for reporting DNC violations on Android.
 * 
 * Copyright (C) 2011, Raj Mathur <raju@kandalaya.org>
 * 
 * This file is part of DNCViolation.
 * 
 * DNCViolation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DNCViolation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DNCViolation.  If not, see <http://www.gnu.org/licenses/>.
 */
// %Id$

package kandalaya.dncviolation;

//import kandalaya.dncviolation.DNCViolationID;

/**
 * A single item in the call/SMS log.  Contains the base data from the phone plus additional state and type information.
 * 
 * @author raju
 *
 */
public class LogItem {
	/**
	 * Phone number from which the call/SMS originated.
	 */
	private String
		number = null;
	/**
	 * Cached name of the caller/sender.
	 */
	private String
		name = null;
	/**
	 * Timestamp of the call/SMS in milliseconds since the epoch.
	 */
	private long
		timestamp = -1;
	/**
	 * Whether selected or not in this run.
	 */
	private boolean
		selected;
	/**
	 * Type of record ((C)all or (S)MS)
	 */
	private char
		call_type;
	//private DNCViolationID
	//  id;
	private String
	  id;
	private int
	  index;
	//
	// All we need are getters and setters
	//
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public char getCall_type() {
		return call_type;
	}
  public boolean isCall()
  {
    return( call_type == 'C' );
  }
  public boolean isSMS()
  {
    return( call_type == 'S' );
  }
	public void setCall_type(char callType) {
		call_type = callType;
	}
  /**
   * Set the ID based on the number, the name if the number is null, and the timestamp.
   */
	public void setId(String logType, int id)
	{
	  //this.id = new DNCViolationID(makeId(number, name, timestamp));
	  this.id = logType + "-" + id;
	}
	/**
	 * Get the ID of this log item as a string.
	 * @return The hex representation of the ID.
	 */
	public String getId()
	{
	  //return( id.toString() );
	  return( id );
	}
	/**
   * @return the index
   */
  public int getIndex()
  {
    return index;
  }
  /**
   * @param index the index to set
   */
  public void setIndex(int index)
  {
    this.index = index;
  }
  /**
	 * Make a ID hex string out of the given parameters.
	 * @param number The number from which the call/SMS originated.
	 * @param name The cached name of the caller.
	 * @param timestamp Timestamp of the call/SMS.
	 * @return The unique ID of this call/SMS.
	 */
	//public static String makeId( String number, String name, long timestamp )
	//{
	//  return( new DNCViolationID((number == null ?number :name) + timestamp).toString() );
	//}
}
