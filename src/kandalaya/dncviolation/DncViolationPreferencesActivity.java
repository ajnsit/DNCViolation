/*
 * User interface for editing preferences for reporting DNC violations on Android.
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
// $Id: DNCViolationActivity.java 8 2011-09-18 05:30:54Z raju $

package kandalaya.dncviolation;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DncViolationPreferencesActivity extends PreferenceActivity
{
  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate(savedInstanceState);
    getPreferenceManager().setSharedPreferencesName(
        DncViolationSharedPreferences.PREFERENCES_NAME);
    addPreferencesFromResource(R.xml.preferences);
  }
}
