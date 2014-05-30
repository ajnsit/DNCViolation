/*
 * Handle preferences for reporting DNC violations on Android.
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
// $Id$

package kandalaya.dncviolation;

import android.content.Context;
import android.content.SharedPreferences;

public class DncViolationSharedPreferences
{
  /**
   * Name of preferences.
   */
  public final static String
    PREFERENCES_NAME = "DncViolationPreferences";
  /**
   * Get preference for deleting SMSes after reporting them.  true: delete after reporting; false: keep after reporting. 
   * @param context Context.
   * @return The user preference value.
   */
  public static boolean getDeleteSmsOnReport( Context context )
  {
    SharedPreferences
      preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
    boolean
      deleteSmsOnReport = preferences.getBoolean(context.getString(R.string.pref_key_delete_sms_on_report), false);
    return( deleteSmsOnReport );
  }
  /**
   * Get the number to report violations via SMS to.
   * @param context Context.
   * @return The number to report to.
   */
  public static String getReportingSmsNumber( Context context )
  {
    SharedPreferences
      preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
    String
      reportingSmsNumber = preferences.getString(context.getString(R.string.pref_key_reporting_sms_number), context.getString(R.string.default_reporting_sms_number));
    return( reportingSmsNumber );
  }
}
