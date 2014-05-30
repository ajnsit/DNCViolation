/*
 * Compare log entries by timestamp for reporting DNC violations on Android.
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
// $Id: LogItemComparator.java 8 2011-09-18 05:30:54Z raju $

package kandalaya.dncviolation;

import java.util.Comparator;

	public class LogItemComparator implements Comparator<LogItem>
	{
	  //@Override
		public int compare( LogItem i1, LogItem i2 )
		{
		  long
		    t1 = i1.getTimestamp();
		  long
		    t2 = i2.getTimestamp();
		  if( t2 - t1 > 0 )
		  {
		    return( 1 );
		  }
		  else if( t1 == t2 )
		  {
		    return 0;
		  }
		  else
		  {
		    return( -1 );
		  }
		}
		public boolean equals(LogItem item1, LogItem item2)
		{
		  return( item1.getId().equals(item2.getId()) );
		}
	}
	
