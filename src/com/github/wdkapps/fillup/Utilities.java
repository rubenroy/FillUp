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

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.Toast;

/**
 * Implements various methods for general purpose use.
 */
public class Utilities {
	
	/**
     * Display an Android "toast" dialog box.
     * @param context The context to use. Usually an Application or Activity object.
     * @param text The text to display in the toast.
     */
    public static void toast(Context context, String text)
    {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }
    
    /**
     * Convert pixels to device independent pixels.
     * @param px Pixels
     * @return Device independent pixels.
     */
    public static float convertPixelsToDp(int px){
        DisplayMetrics metrics = App.getContext().getResources().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
    
    /**
     * Convert device independent pixels to pixels. 
     * @param dp Device independent pixels.
     */
    public static int convertDpToPixel(float dp){
        DisplayMetrics metrics = App.getContext().getResources().getDisplayMetrics();
        int px = (int) (dp * (metrics.densityDpi / 160f));
        return px;
    }
    
}
