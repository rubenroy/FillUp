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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

/**
 * A dialog allowing a user to select preferred units of measurement
 * from a list of possible values.
 */
public class UnitsDialog {
	
	/// a result code to be returned to the listener
	enum Result {RESULT_SELECTED, RESULT_CANCEL};
	
    /**
	 * The activity that creates an instance of this dialog must
     * implement this interface in order to receive event callbacks.
     */
    public interface Listener {
        /**
         * Called when the dialog closes to report the response to the listener.
		 * @param id The id value specified when the dialog was created.
         * @param value The selected Units value (null = no selection).
         */
        public void onUnitsDialogResponse(int id, Result result, String value);
    }
    
    /**
     * Creates an instance of the dialog.
     * @param context The Context of the activity/application creating the dialog.
     * @param listener A Listener to notify of dialog events.
     * @param id An integer identifying the dialog (meaningful only to the owner).
     * @return The Dialog.
     */
    public static Dialog create(
    		Context context, 
    		final Listener listener, 
    		final int id) {

    	final Resources res = context.getResources();
    	
        // Build the dialog and set up the click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
        .setTitle(R.string.title_units_dialog)
        .setIcon(res.getDrawable(R.drawable.ic_dialog_menu_generic))
        .setItems(R.array.arrayUnitsEntries, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		String[] values = res.getStringArray(R.array.arrayUnitsEntryValues);
                listener.onUnitsDialogResponse(id,Result.RESULT_SELECTED,values[which]);
        	}
        });
        
        // require user selection
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
                listener.onUnitsDialogResponse(id,Result.RESULT_CANCEL,null);
			}
		}); 

        // return the dialog
        return dialog;
    }

}
