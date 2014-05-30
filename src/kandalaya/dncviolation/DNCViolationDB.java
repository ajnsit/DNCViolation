/*
 * Get call/SMS logs for reporting DNC violations on Android.
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
// $Id: DNCViolationDB.java 8 2011-09-18 05:30:54Z raju $

package kandalaya.dncviolation;

import java.util.Collections;
import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.net.Uri;

import kandalaya.dncviolation.LogItem;
import kandalaya.dncviolation.LogItemComparator;

public class DNCViolationDB {
  /**
   * URI for the SMS inbox, needed to fetch SMSes.
   */
  static Uri
    smsInboxUri = Uri.parse("content://sms/inbox");
  /**
   * URI for SMS conversations, needed for deletion.
   */
  static Uri
    smsContentUri = Uri.parse("content://sms");
	/**
	 * Make a list of calls containing LogItems.
	 * 
	 * 	 @param activity The activity for which this instance was started (required for getContentProvider).
	 *   @param unknownOnly	Only include calls from numbers without cached names. 
	 *   @param nDays Only consider records newer than this many days.
	 */
	static ArrayList<LogItem>
	makeCallList(Activity activity, boolean unknownOnly, int nDays)
	{
	  Log.d("DNCViolation", "ndays = "+nDays);
		ArrayList<LogItem>
			mylist = new ArrayList<LogItem>();
		//
		// Set first day records from which we are interested in.
		//
    String
      strOrder;
    String
      dateFilter;
		//
		// Get call log
		//
		String[]
		       callStrFields = {
		    "_id",
				android.provider.CallLog.Calls.NUMBER, 
				android.provider.CallLog.Calls.CACHED_NAME,
				android.provider.CallLog.Calls.DATE,
		};
		String
			callerNameFilter = "";
		strOrder = android.provider.CallLog.Calls.DATE + " DESC";
		dateFilter = " and julianday('now') - julianday(" + android.provider.CallLog.Calls.DATE + "/1000, 'unixepoch') <= " + nDays;
		Log.d("DNCViolation", "date filter = <"+dateFilter+">");
		if( unknownOnly )
		{
			callerNameFilter = " and " + android.provider.CallLog.Calls.CACHED_NAME + " is null";
			Log.d("DNCViolation", "Set filter to null");
		}
		//Log.d("DNCViolation", "order: "+strOrder+", filter: "+callerNameFilter);
		Log.d("DNCViolation", "start query");
		Cursor
			callCursor = activity.getContentResolver().query(
					android.provider.CallLog.Calls.CONTENT_URI,
					callStrFields,
					android.provider.CallLog.Calls.TYPE + " = " + android.provider.CallLog.Calls.INCOMING_TYPE
						+ callerNameFilter
						+ dateFilter
						,
					null,
					null
			);
		// get start of cursor
		Log.d("DNCViolation", "start query");
		if( callCursor.moveToFirst() )
		{
			int
				i = 0;
			// loop through cursor 
			do
			{
			  int
			    id = callCursor.getInt(0);
				String
					number = callCursor.getString(1);
				String
					name = callCursor.getString(2);
				long
					timestamp = callCursor.getLong(3);
				LogItem
					item = new LogItem();
				item.setNumber(number);
				item.setName(name);
				item.setTimestamp(timestamp);
				//Log.d("DNCViolation", "timestamp " + timestamp);
				item.setSelected(false);
				item.setCall_type('C');
        item.setId("C", id);
				// Log.d("DNCViolation", number+", "+name+", "+date.toLocaleString());
				mylist.add(item);
				i++;
			} while (callCursor.moveToNext());
			Log.d("DNCViolation", "got " + i + " call records");
		}
		Log.d("DNCViolation", "done call query");
		//
		// Get SMS log
		//
		/*// What are the fields anyway?
		Cursor mCursor = activity.getContentResolver().query(smsUri, null, null, null, null);

		StringBuffer info = new StringBuffer();
		for( int i = 0; i < mCursor.getColumnCount(); i++) {
		    info.append("Column: " + mCursor.getColumnName(i) + "\n");
		}
		Log.d("DNCViolation", new String(info));*/
		//
		// Columns:
		// _id, thread_id, address, person, date, protocol, read, status,
		// type, reply_path_present, subject, body, service_center, locked,
		// error_core, seen, deletable, hidden, group_id, group_type,
		// delivery_date

		//
		// Get the SMSes
		String[]
		       smsStrFields =
		       {
		            "_id",
                "date",
                "address",
                "subject",
                "protocol",
                "body",
                "person",
		       };
    strOrder = "date DESC";
    dateFilter = "julianday('now') - julianday(date/1000, 'unixepoch') <= " + nDays;
    Log.d("DNCViolation", "SMS date filter = <"+dateFilter+">");
		Log.d("DNCViolation", "start SMS query");
		Cursor
			smsCursor = activity.getContentResolver().query(
					smsInboxUri,
					smsStrFields,
					dateFilter,
					null,
					strOrder
			);
		// get start of cursor
		Log.d("DNCViolation", "start SMS query");
		if( smsCursor.moveToFirst() )
		{
			int
				i = 0;
			// loop through cursor 
			do
			{
			  int
			    id = smsCursor.getInt(0);
				long
					timestamp = smsCursor.getLong(1);
				String
					address = smsCursor.getString(2);
				String
					subject = smsCursor.getString(3);
				String
					body = smsCursor.getString(5);
				LogItem
					item = new LogItem();
				item.setNumber(address);
				item.setName(address);
				item.setTimestamp(timestamp);
				item.setSelected(false);
				item.setCall_type('M');
				// Get extract of SMS to show in name field
				String
					name = new String("");
				if( subject != null )
				{
					name += subject + " | "; 
				}
				name += body;
				item.setNumber(address);
				item.setName(name);
				item.setCall_type('S');
				item.setSelected(false);
				item.setId("S", id);
				//Log.d("DNCViolation", address+", "+person+", "+protocol+", "+subject+", "+item.getName()+", "+date.toLocaleString());
				mylist.add(item);
				i++;
			} while (smsCursor.moveToNext());
			Log.d("DNCViolation", "got " + i + " SMS records");
		}
		Log.d("DNCViolation", "done SMS query");
		Collections.sort(mylist, new LogItemComparator());
		for( int i = 0; i < mylist.size(); i++ )
		{
		  mylist.get(i).setIndex(i);
		}
		return( mylist );
	}
	/**
	 * Delete the SMS given the internal Android ID.  Code copied from
	 * http://stackoverflow.com/questions/419184/how-to-delete-an-sms-from-the-inbox-in-android-programmatically
	 * 
	 * @param activity Activity (needed for getContentResolver).
	 * @param id The internal Android ID of the SMS.
	 * @return true on success, false on failure to delete.
	 */
	public static boolean deleteSMS(Activity activity, int id)
	{
	  //
	  // Get the thread ID
	  //
	  int
	    threadId;
	  String[]
	         smsStrFields =
	         {
	            "_id",
	            "thread_id"
	         };
    Log.d("DNCViolation", "start SMS thread query");
    Cursor
      smsCursor = activity.getContentResolver().query(
          smsInboxUri,
          smsStrFields,
          "_id = ?",
          new String[] {String.valueOf(id)},
          null
      );
    // get start of cursor
    Log.d("DNCViolation", "start SMS query");
    if( smsCursor.moveToFirst() )
    {
      threadId = smsCursor.getInt(1);
      Log.d("DNCViolation", "got SMS thread id "+threadId);
    }
    else
    {
      Log.d("DNCViolation", "Cannot find SMS with ID "+id);
      return( false );
    }
    //
    // Try to delete the specific SMS
    //
    Log.d("DNCViolation", "deleting SMS thread id "+threadId+" _id "+id);
    int
      deleted;
    deleted = activity.getContentResolver().delete(smsContentUri, "thread_id = ? and _id = ?", new String[] {String.valueOf(threadId), String.valueOf(id)});
    if( deleted > 0 )
    {
      Log.d("DNCViolation", deleted+" SMS deleted");
    }
    else
    {
      Log.d("DNCViolation", "Couldn't delete the SMS");
    }
    return( deleted > 0 );
	}
  /**
   * Delete the SMS given ID in our format.  Extract "NNNNN" from the "{S/C}-NNNNN" format
   * and pass to deleteSMS(Activity, int).
   * 
   * @param activity Activity (needed for getContentResolver).
   * @param id Our ID of the SMS.
   * @return true on success, false on failure to delete.
   */
	public static boolean deleteSMS( Activity activity, String id )
	{
	  String []
	    splitted = id.split("-");
	  String
	    idPart = splitted[1];
	  Log.d("DNCViolation", "Calling deleteSMS("+idPart+")");
	  
	  int
	    numericId = Integer.parseInt(idPart);
	  return( deleteSMS(activity, numericId) );
	}
}
