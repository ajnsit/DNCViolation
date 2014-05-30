/*
 * Handle control and display for reporting DNC violations on Android.
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
// $Id: DNCViolationActivity.java 14 2011-09-20 19:47:00Z raju $

package kandalaya.dncviolation;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import kandalaya.dncviolation.DNCViolationDB;

//
// List handling and display mostly copied and enhanced from
// http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
//

public class DNCViolationActivity extends ListActivity
{
  /**
   * Display progress dialog while loading up the data from call/SMS records.
   */
  private ProgressDialog progressDialog = null;
  /**
   * List of call records found matching the query.
   */
  private ArrayList<LogItem> logItems = null;
  /**
   * ArrayAdapter extended for our display requirements.
   */
  private LogAdapter adapter;
  /**
   * Thread for populating data into logItems.
   */
  private Runnable viewLogs;
  /**
   * Contains one entry indexed by ID of each call/SMS log item selected by the user.
   * The data denotes the type of the log item: (C)all or (S)MS.
   */
  private static HashMap<String, String>
    selectedItems = new HashMap<String, String>();
  private static int
    selectedSMSCount = 0,
    selectedCallCount = 0;
  /**
   * Dialog for reporting selected violations.
   */
  Dialog
    doitDialog;
  // The list itself
  // private SimpleAdapter
  // callList;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    //
    // Create the bare list.
    // ListView list = (ListView) findViewById(R.id.list);
    //Log.d("DNCViolation", "new list");
    setContentView(R.layout.main);
    //Log.d("DNCViolation", "setContentView");
    //
    // Get the call details
    //Log.d("DNCViolation", "done map");
    //logItems = DNCViolationDB.makeCallList(this, true, 3);
    logItems = new ArrayList<LogItem>();
    this.adapter = new LogAdapter(this, R.layout.row, logItems);
    Log.d("DNCViolation", "done logadapter");
    setListAdapter(this.adapter);
    Log.d("DNCViolation", "create runnable");
    viewLogs = new Runnable()
    {
      //@Override
      public void run()
      {
        Log.d("DNCViolation", "about to run makeCallList");
        logItems = DNCViolationDB.makeCallList(DNCViolationActivity.this, true, getResources().getInteger(R.integer.default_number_of_days_to_go_back));
        Log.d("DNCViolation", "About to runonuithread");
        Log.d("DNCViolation", logItems.size()+" items");
        runOnUiThread(returnRes);
        Log.d("DNCViolation", "done runonuithread");
        Log.d("DNCViolation", "ran makeCallList, "+logItems.size()+" items");
      }
    };
    Thread thread = new Thread(null, viewLogs, "Populator");
    thread.start();
    progressDialog = ProgressDialog.show(DNCViolationActivity.this,
        "Please wait...", "Retrieving data ...", true);
    //progressDialog.dismiss();
    //selectedItems.clear();
    Log.d("DNCViolation", "done listadapter");
    /**
     * Set counts to zero, will get filled up in getView
     */
    //selectedSMSCount = 0;
    //selectedCallCount = 0;
    displayCounts();
    setReportButtonState();
  }

  private Runnable returnRes = new Runnable()
  {
    // @Override
    public void run()
    {
      if (logItems != null && logItems.size() > 0)
      {
        Log.d("DNCViolation", "returnRes: "+logItems.size());
        adapter.notifyDataSetChanged();
        for (int i = 0; i < logItems.size(); i++)
        {
          adapter.add(logItems.get(i));
        }
      }
      Log.d("DNCViolation", "returnRes: dismiss progress dialog, "+logItems.size()+" items");
      progressDialog.dismiss();
      adapter.notifyDataSetChanged();
      Log.d("DNCViolation", "returnRes: Done");
    }
  };

  public Runnable getReturnRes()
  {
    return returnRes;
  }

  private class LogAdapter extends ArrayAdapter<LogItem>
  {
    private ArrayList<LogItem> logItems;
    private Context context;

    public LogAdapter(Context context, int textViewResourceId,
        ArrayList<LogItem> items)
    {
      super(context, textViewResourceId, items);
      this.context = context;
      this.logItems = items;
    }
    /**
     * Inflate the row element, get data from the log item, massage into shape,
     * enable/disable buttons/images, set checked status and display the view. 
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      //Log.d("DNCViolation", "start getView");
      /**
       * Inflate the row view.
       */
      View
        v = convertView;
      if (v == null)
      {
        LayoutInflater
          vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.row, null);
      }
      LogItem item = logItems.get(position);
      //Log.d("DNCViolation", "getView: position "+position);
      if( item != null )
      {
        //Log.d("DNCViolation", "Got item "+item.getId());
        /**
         * Get the elements of the view.
         */
        CheckBox select_violation = (CheckBox)v.findViewById(R.id.selectViolation);
        TextView number_cell = (TextView)v.findViewById(R.id.NUMBER_CELL);
        TextView name_cell = (TextView)v.findViewById(R.id.NAME_CELL);
        TextView date_cell = (TextView)v.findViewById(R.id.DATE_CELL);
        TextView item_index = (TextView)v.findViewById(R.id.itemIndex);
        ImageView icon = (ImageView) v.findViewById(R.id.callIcon);
        Button viewButton = (Button) v.findViewById(R.id.smsViewButton);
        /**
         * Set button/image visibility.  SMSes have view button while calls have image.
         */
        if( item.getCall_type() == 'C' )
        {
          icon.setVisibility(View.VISIBLE);
          viewButton.setVisibility(View.GONE);
          //Log.d("DNCViolation", "done call layout");
        }
        else
        {
          icon.setVisibility(View.GONE);
          viewButton.setVisibility(View.VISIBLE);
          //Log.d("DNCViolation", "done SMS layout");
        }
        /**
         * Extract log item details and massage into shape for display.
         */
        if (number_cell != null)
        {
          number_cell.setText(item.getNumber());
        }
        else
        {
          Log.e("DNCViolation", "getView: NULL NUMBER");
        }
        if (name_cell != null)
        {
          String
            body = item.getName();
          if( item.getCall_type() == 'S' )
          {
            body = body.replace('\n', ' ');
            if( body.length() > 20 )
            {
              body = body.substring(0, 20) + "...";
            }
          }
          name_cell.setText(body);
        }
        else
        {
          Log.e("DNCViolation", "getView: NULL NAME");
        }
        if (date_cell != null)
        {
          date_cell.setText(new Date(item.getTimestamp()).toLocaleString());
        }
        else
        {
          Log.e("DNCViolation", "getView: NULL DATE");
        }
        if( item_index != null )
        {
          item_index.setText(item.getIndex()+"");
        }
        /**
         * Set the select checkbox on if this item has been selected.
         */
        if( selectedItems.containsKey(item.getId()) )
        {
          select_violation.setChecked(true);
          item.setSelected(true);
        }
        else
        {
          select_violation.setChecked(false);
          item.setSelected(false);
        }
      }
      //Log.d("DNCViolation", "end getView");
      return (v);
    }
  }
  /**
   * Handle clicks on SMS view buttons
   * @param v Parent view
   */
  public void viewButtonHandler(View v)
  {
    LinearLayout
      parentRow = (LinearLayout)v.getParent();
    int
      index = Integer.parseInt(((TextView)parentRow.findViewById(R.id.itemIndex)).getText().toString());
    Log.d("DNCViolation", "onClick: got index "+index);
    LogItem
      item = logItems.get(index);
    //Log.d("DNCViolation", "name: "+item.getName()+", number: "+item.getNumber()+", date: "+new Date(item.getTimestamp()).toLocaleString());
    new AlertDialog.Builder(this)
    .setTitle(item.getNumber())
    .setMessage(item.getName() + "\n\n" + new Date(item.getTimestamp()).toLocaleString())
    .setPositiveButton(R.string.sms_view_done, new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        { 
            // Do nothing
        }
    })
    .show();
    //initiatePopupWindow(item);
  }
  /**
   * Display the popup that will actually prompt the user to take the action. 
   * @param v Parent view
   */
  public void mainReportButtonHandler(View v)
  {
    // Make the dialog
    doitDialog = new Dialog(this);
    doitDialog.setContentView(R.layout.doit);
    doitDialog.setTitle("Report DNC Violations");
    // Populate the fields
    TextView
      summary = (TextView)doitDialog.findViewById(R.id.doit_summary);
    if( summary != null )
    {
      summary.setText(getDisplayCounts());
    }
    else
    {
      Log.e("DNCViolation", "summary is null");
    }
    CheckBox
      deleteAfterReport = (CheckBox)doitDialog.findViewById(R.id.doit_delete_sms);
    if( DncViolationSharedPreferences.getDeleteSmsOnReport(this) )
    {
      deleteAfterReport.setChecked(true);
    }
    else
    {
      deleteAfterReport.setChecked(false);
    }
    // Set onClick listeners for the buttons
    Button
      doReportButton = (Button)doitDialog.findViewById(R.id.doit_do_report);
    doReportButton.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        doitDoReportButtonHandler(v);
        doitDialog.dismiss();
      }
    });
    Button
      doCancelButton = (Button)doitDialog.findViewById(R.id.doit_cancel);
    doCancelButton.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        doitCancelButtonHandler(v);
        doitDialog.dismiss();
      }
    });
    // Show it
    doitDialog.setOwnerActivity(this);
    doitDialog.show();
  }
  /**
   * Handle clicks on the select button.  Remove the item's ID from the selectedItems map if the button is being switched off, add the ID if the button is being enabled.
   * 
   * @param v parent view.
   */
  public void selectButtonHandler(View v)
  {
    LinearLayout
      parentRow = (LinearLayout)v.getParent();
    CheckBox
      selectViolation = (CheckBox)v.findViewById(R.id.selectViolation); 
    int
      index = Integer.parseInt(((TextView)parentRow.findViewById(R.id.itemIndex)).getText().toString());
    Log.d("DNCViolation", "selectClick: got index "+index);
    LogItem
      item = logItems.get(index);
    if( selectViolation.isChecked() )
    {
      setSelected(item, true);
    }
    else
    {
      setSelected(item, false);
    }
    //Log.d("DNCViolation", "name: "+item.getName()+", number: "+item.getNumber()+", date: "+new Date(item.getTimestamp()).toLocaleString());
  }
  /**
   * Do the actual reporting (and possibly SMS deletion).  Get information from selected log items (loop through logItems) and send details of each item to SMS receiver.
   * 
   * @param v parent view
   */
  public void doitDoReportButtonHandler(View v)
  {
    for( LogItem item: logItems )
    {
      if( item.isSelected() )
      {
        Log.d("DNCViolation", "handling item "+item.getNumber());
        String
          toastText = "Reporting "+(item.getCall_type() == 'C' ?"call" :"SMS")+" "+item.getNumber()+" of "+(new Date(item.getTimestamp()).toLocaleString());
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        // From http://www.nccptrai.gov.in/nccpregistry/How%20to%20Register%20complaint.pdf
        //   COMP TEL NO XXXXXXXXXX, dd/mm/yy, Time hh:mm
        Format
          formatter;
        formatter = new SimpleDateFormat("dd/MM/yy");
        String
          formattedDate = formatter.format(item.getTimestamp());
        formatter = new SimpleDateFormat("HH:mm");
        String
          formattedTime = formatter.format(item.getTimestamp());
        String
          smsText = "COMP TEL NO "+item.getNumber()+", "+formattedDate+", Time "+formattedTime;
        // Send the SMS
        String
          destination =  DncViolationSharedPreferences.getReportingSmsNumber(this);
        Log.d("DNCViolation", "Got preferred number "+destination);
            //getResources().getString(R.string.default_reporting_sms_number);
        SmsManager
          sms = SmsManager.getDefault();
        sms.sendTextMessage(destination, null, smsText, null, null);
        Log.i("DNCViolation", "Sent SMS "+smsText+" to "+destination);
        //
        // Check for deletion
        if( item.isSMS() )
        {
          Log.d("DNCViolation", "view "+v.toString());
          CheckBox
            deleteSMSCheckBox = (CheckBox)v.getRootView().findViewById(R.id.doit_delete_sms);
          if (deleteSMSCheckBox != null)
          {
            boolean deleteSMS = deleteSMSCheckBox.isChecked();
            if (deleteSMS)
            {
              Log.i("DNCViolation", "Going to delete SMS item " + item.getIndex()
                  + " (ID " + item.getId() + ")");
              DNCViolationDB.deleteSMS(this, item.getId());
            }
          }
          else
          {
            Log.e("DNCViolation", "Cannot find the delete SMS after reporting checkbox");
          }
        }
      }
      setSelected(item, false);
    }
    adapter.notifyDataSetChanged();
  }
  public void doitCancelButtonHandler(View v)
  {
    Log.d("DNCViolation", "cancelling report dialog");
  }
  /**
   * Set/unset an item's selected state in selectedItems.  Update the text showing selected item count.
   * Probably should be in a different class, but let's wait for v2 for that.
   *
   * @param item The log item to be added to/removed from the selected list.
   * @param select Specify whether to add or remove the item.
   */
  private void setSelected( LogItem item, boolean select )
  {
    String
      id = item.getId();
    boolean
      old_state = item.isSelected();
    if( select )
    {
      item.setSelected(true);
      selectedItems.put(id, Character.toString(item.getCall_type()));
      selectedCallCount += (item.isCall() && old_state == false) ?1 :0;
      selectedSMSCount += (item.isSMS() && old_state == false) ?1 :0;
    }
    else
    {
      item.setSelected(false);
      selectedItems.remove(id);
      selectedCallCount -= (item.isCall() && old_state == true) ?1 :0;
      selectedSMSCount -= (item.isSMS() && old_state == true) ?1 :0;
    }
    // Display the counts 
    displayCounts();
    // Enable/disable the report button
    setReportButtonState();
  }
  /**
   * Display count of selected calls and SMSes in the selectedCounts TextView in main.
   */
  private void displayCounts()
  {
    /**
     * Get the TextView and populate it
     */
    TextView
      tv = (TextView)findViewById(R.id.selectedCounts);
    tv.setText(getDisplayCounts());
/*    for (Map.Entry<String, String> entry : selectedItems.entrySet())
      {
        String key = entry.getKey();
        String value = entry.getValue();
        // ...
      }*/
  }
  private String getDisplayCounts()
  {
    return( selectedCallCount+selectedSMSCount+" item(s) selected: "+selectedCallCount+" call(s); "+selectedSMSCount+" SMS(es)" );
  }
  /**
   * Set the state of the Report button on the main screen to disabled if no log items selected, enabled otherwise.
   */
  private void setReportButtonState()
  {
    Button
      reportButton = (Button)findViewById(R.id.main_report_button);
    if( selectedCallCount > 0 || selectedSMSCount > 0)
    {
      reportButton.setEnabled(true);
    }
    else
    {
      reportButton.setEnabled(false);
    }
  }
  /**
   * Create the minimal menu
   */
  //@Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main, menu);
      Intent
        prefsIntent = new Intent(getApplicationContext(), DncViolationPreferencesActivity.class);

      MenuItem
        preferences = menu.findItem(R.id.menu_option_settings);
      preferences.setIntent(prefsIntent);
      return true;
  }
  /**
   * Action to take on menu item selection
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
      // Handle item selection
      switch (item.getItemId()) {
      case R.id.menu_option_about:
        showAboutBox();
        return true;
      case R.id.menu_option_settings:
        startActivity(item.getIntent());
        return( true );
      default:
          return super.onOptionsItemSelected(item);
      }
  }
  /**
   * Show the about box.
   */
  void showAboutBox()
  {
    new AlertDialog.Builder(this)
    .setTitle(R.string.about_dncviolation)
    .setMessage("DNCViolation version " + getResources().getString(R.string.app_version) + "\n\n"
        +"Copyright (C) 2011, Raj Mathur <raju@kandalaya.org>.  "
        +"Available under the terms of the GNU General Public Licence v3 or later.\n\n"
        +"Please visit http://sourceforge.net/p/dncviolation/home/Home/ for more information.")
    .setPositiveButton(R.string.about_done,
        new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int which)
      {
        // Do nothing
      }
    })
    .show();
  }
  /*  private PopupWindow pw;
  private void initiatePopupWindow( LogItem item )
  {
    try
    {
        //We need to get the instance of the LayoutInflater, use the context of this activity
        LayoutInflater inflater = (LayoutInflater) DNCViolationActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //Inflate the view from a predefined XML layout
        View layout = inflater.inflate(R.layout.smsinfo,
                (ViewGroup) findViewById(R.id.smsInfo));
        // create a 300px width and 470px height PopupWindow
        pw = new PopupWindow(layout, 300, 470, true);
        // display the popup in the center
        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
 
        TextView
          sender = (TextView) layout.findViewById(R.id.smsSender);
        TextView
          date = (TextView) layout.findViewById(R.id.smsDate);
        TextView
          message = (TextView) layout.findViewById(R.id.smsText);
        sender.setText(item.getNumber());
        date.setText(new Date(item.getTimestamp()).toLocaleString());
        message.setText(item.getName());
        Button cancelButton = (Button) layout.findViewById(R.id.doneButton);
        cancelButton.setOnClickListener(done_button_click_listener);
 
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
 
  private OnClickListener
    done_button_click_listener = new OnClickListener()
    {
      public void onClick(View v) {
        pw.dismiss();
    }
  };*/
}