/*
 * Copyright (C) 2010 Pye Brook Company, Inc.
 *               http://www.pyebrook.com
 *               info@pyebrook.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This software uses information from the document
 *
 *     'Bluetooth HXM API Guide 2010-07-22'
 *
 * which is Copyright (C) Zephyr Technology, and used with the permission
 * of the company. Information on Zephyr Technology products and how to 
 * obtain the Bluetooth HXM API Guide can be found on the Zephyr
 * Technology Corporation website at
 * 
 *      http://www.zephyr-technology.com
 * 
 *
 */


package com.pyebrook.hxmDemo;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class hxmDemo extends Activity {

	/*
	 *  TAG for Debugging Log
	 */
    private static final String TAG = "hxmDemo";
        
    /*
     *  Layout Views
     */
    private TextView mTitle;
    private TextView mStatus;
    
    /*
     * Name of the connected device, and it's address
     */
    private String mHxMName = null;
    private String mHxMAddress = null;
    
    /*
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /*
     * Member object for the chat services
     */
    private HxmService mHxmService = null;
    

    
    
    /*
     * connectToHxm() sets up our service loops and starts the connection
     * logic to manage the HxM device data stream 
     */
    private void connectToHxm() {
    	/*
    	 * Update the status to connecting so the user can tell what's happening
    	 */
      	mStatus.setText(R.string.connecting);

	    /*
	     * Setup the service that will talk with the Hxm
	     */
	    if (mHxmService == null) 
	    	setupHrm();
	    
	    /*
	     * Look for an Hxm to connect to, if none is found tell the user
	     * about it
	     */
	    if ( getFirstConnectedHxm() ) {
	    	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mHxMAddress);
	    	mHxmService.connect(device); 	// Attempt to connect to the device
	    } else {
	      	mStatus.setText(R.string.nonePaired);	    	
	    }
    
    }
    
    
    /*
     * Loop through all the connected bluetooth devices, the first one that 
     * starts with HXM will be assumed to be our Zephyr HxM Heart Rate Monitor,
     * and this is the device we will connect to
     * 
     * returns true if a HxM is found and the global device address has been set 
     */
    private boolean getFirstConnectedHxm() {

    	/*
    	 * Initialize the global device address to null, that means we haven't 
    	 * found a HxM to connect to yet    	
    	 */
    	mHxMAddress = null;    	
    	mHxMName = null;
    	
    	
	    /*
	     * Get the local Bluetooth adapter
	     */
	    BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

	    /*
	     *  Get a set of currently paired devices to cycle through, the Zephyr HxM must
	     *  be paired to this Android device, and the bluetooth adapter must be enabled
	     */
	    Set<BluetoothDevice> bondedDevices = mBtAdapter.getBondedDevices();

	    /*
	     * For each device check to see if it starts with HXM, if it does assume it
	     * is the Zephyr HxM device we want to pair with      
	     */
	    if (bondedDevices.size() > 0) {
	        for (BluetoothDevice device : bondedDevices) {
	        	String deviceName = device.getName();
	        	if ( deviceName.startsWith("HXM") ) {
	        		/*
	        		 * we found an HxM to try to talk to!, let's remember its name and 
	        		 * stop looking for more
	        		 */
	        		mHxMAddress = device.getAddress();
	        		mHxMName = device.getName();
	        		Log.d(TAG,"getFirstConnectedHxm() found a device whose name starts with 'HXM', its name is "+mHxMName+" and its address is ++mHxMAddress");
	        		break;
	        	}
	        }
	    }
    
	    /*
	     * return true if we found an HxM and set the global device address
	     */
	    return (mHxMAddress != null);
   }

    
        
/*
 * Our onCreate() needs to setup the main activity that we will use to        
 * @see android.app.Activity#onCreate(android.os.Bundle)
 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        /*
         * Set up the window layout, we can use a cutom title, the layout
         * from our resource file
         */
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.rawdata);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        /*
         *  Set up the title text view, if we can't do it something is wrong with
         *  how this application package was built, in that case display a message
         *  and give up.        
         */
        mTitle = (TextView) findViewById(R.id.title);
        if ( mTitle == null ) {
        	Toast.makeText(this, "Something went very wrong, missing resource, rebuild the application", Toast.LENGTH_LONG).show();
        	finish();
        }            

        /*
         *  Set up the status text view, if we can't do it something is wrong with
         *  how this application package was built, in that case display a message
         *  and give up.        
         */
        mStatus = (TextView) findViewById(R.id.status);
        if ( mStatus == null ) {
        	Toast.makeText(this, "Something went very wrong, missing resource, rebuild the application", Toast.LENGTH_LONG).show();
        	finish();
        }            
	    
        /*
         * Put some initial information into our display until we have 
         * something more interesting to tell the user about 
         */
       	mTitle.setText(R.string.hxmDemoAppName);
       	mStatus.setText(R.string.initializing);
        
        
	    /*
	     *  Get the default bluetooth adapter, if it fails there is not much we can do
	     *  so show the user a message and then close the application
	     */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /*
         * Start the activity that displays our welcome message
         */
        Intent intent = new Intent(this, WelcomeMessage.class);
    	startActivityForResult( intent , 1 );
        
        
        /*
         *  If the adapter is null, then Bluetooth is not supported
         */
        if (mBluetoothAdapter == null) {
	        /*
	         * Blutoooth needs to be available on this device, and also enabled.  
	         */
            Toast.makeText(this, "Bluetooth is not available or not enabled", Toast.LENGTH_LONG).show();
           	mStatus.setText(R.string.noBluetooth);
            
         } else {
	        /*
	         * Everything should be good to go so let's try to connect to the HxM
	         */
 	        if (!mBluetoothAdapter.isEnabled()) {
	          	mStatus.setText(R.string.btNotEnabled);
	        	Log.d(TAG, "onStart: Blueooth adapter detected, but it's not enabled");
	        } else {
	          	mStatus.setText(R.string.connecting);
		        connectToHxm();
	        }
         }        
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        /*
         * Check if there is a bluetooth adapter and if it's enabled,
         * error messages and status updates as appropriate
         */
        if (mBluetoothAdapter != null ) {
	        // If BT is not on, request that it be enabled.
	        // setupChat() will then be called during onActivityResult     
	        if (!mBluetoothAdapter.isEnabled()) {
	          	mStatus.setText(R.string.btNotEnabled);
	        	Log.d(TAG, "onStart: Blueooth adapter detected, but it's not enabled");
	        }
        } else {
          	mStatus.setText(R.string.noBluetooth);
        	Log.d(TAG, "onStart: No blueooth adapter detected, it needs to be present and enabled");
        }
	    
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mHxmService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mHxmService.getState() == R.string.HXM_SERVICE_RESTING) {
              // Start the Bluetooth scale services
              mHxmService.start();
            }
        }
    }

    private void setupHrm() {
    	Log.d(TAG, "setupScale:");

        // Initialize the service to perform bluetooth connections
        mHxmService = new HxmService(this, mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mHxmService != null) mHxmService.stop();
        Log.e(TAG, "--- ON DESTROY ---");
    }

     /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    // The Handler that gets information back from the hrm service
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case R.string.HXM_SERVICE_MSG_STATE: 
                Log.d(TAG, "handleMessage():  MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
	                case R.string.HXM_SERVICE_CONNECTED:
	                	if ((mStatus != null) && (mHxMName != null)) {
	                		mStatus.setText(R.string.connectedTo);
	                		mStatus.append(mHxMName);
	                	}
	                    break;

	                case R.string.HXM_SERVICE_CONNECTING:
	                    mStatus.setText(R.string.connecting);
	                    break;
	                    
	                case R.string.HXM_SERVICE_RESTING:
	                	if (mStatus != null ) {
	                		mStatus.setText(R.string.notConnected);
	                	}
	                    break;
                }
                break;

            case R.string.HXM_SERVICE_MSG_READ: {
            	/*
            	 * MESSAGE_READ will have the byte buffer in tow, we take it, build an instance
            	 * of a HrmReading object from the bytes, and then display it into our view
            	 */
                byte[] readBuf = (byte[]) msg.obj;
                HrmReading hrm = new HrmReading( readBuf );
                hrm.displayRaw();
                break;
            }
                
            case R.string.HXM_SERVICE_MSG_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(null),Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
	        case R.id.scan:
	        	connectToHxm();
	            return true;
	        
	        case R.id.quit:
	        	finish();
	            return true;
			
	        case R.id.about: {
	            /*
	             * Start the activity that displays our welcome message
	             */
	            Intent intent = new Intent(this, WelcomeMessage.class);
	        	startActivityForResult( intent , 1 );
	            break;
	        }
        }
        
        return false;
    }
 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + requestCode);
        
        switch(requestCode)
        {  
        
	        case 1:
	       	    break;

		}             
    }
    
    /*
     * HrmReading
     * 
     * This class holds the information corresponding to a single message from 
     * the Zephyr HxM Heart Rate Monitor
     * 
     * The constructor HrmReading(byte[]) will fill the member fields from the bytes presumably 
     * read from a connected Zephyr HxM Heart Rate Monitor.  Because Java does not support 
     * signed/unsigned variants of numbers, we sometimes put the fields extracted from the 
     * HxM message into fields larger than is necessary.
     * 
     *
     *  
     */
    public class HrmReading {
        public final int STX = 0x02;
        public final int MSGID = 0x26;
        public final int DLC = 55;
        public final int ETX = 0x03;
    	
    	private static final String TAG = "HrmReading";

    	int serial;
        byte stx;
        byte msgId;
        byte dlc;
        int firmwareId;
        int firmwareVersion;
        int hardWareId;
        int hardwareVersion;
        int batteryIndicator;
        int heartRate;
        int heartBeatNumber;
        long hbTime1;
        long hbTime2;
        long hbTime3;
        long hbTime4;
        long hbTime5;
        long hbTime6;
        long hbTime7;
        long hbTime8;
        long hbTime9;
        long hbTime10;
        long hbTime11;
        long hbTime12;
        long hbTime13;
        long hbTime14;
        long hbTime15;
        long reserved1;
        long reserved2;
        long reserved3;
        long distance;
        long speed;
        byte strides;    
        byte reserved4;
        long reserved5;
        byte crc;
        byte etx;

        public HrmReading (byte[] buffer) {
        	int bufferIndex = 0;

        	Log.d ( TAG, "HrmReading being built from byte buffer");
        	
            try {
    			stx 				= buffer[bufferIndex++];
    			msgId 				= buffer[bufferIndex++];
    			dlc 				= buffer[bufferIndex++];
    			firmwareId 			= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			firmwareVersion 	= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hardWareId 			= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hardwareVersion		= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			batteryIndicator  	= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
    			heartRate  			= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
    			heartBeatNumber  	= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
    			hbTime1				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime2				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime3				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime4				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime5				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime6				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime7				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime8				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime9				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime10			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime11			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime12			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime13			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime14			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			hbTime15			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			reserved1			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			reserved2			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			reserved3			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			distance			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			speed				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			strides 			= buffer[bufferIndex++];    
    			reserved4 			= buffer[bufferIndex++];
    			reserved5 			= (long)(int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
    			crc 				= buffer[bufferIndex++];
    			etx 				= buffer[bufferIndex];
    		} catch (Exception e) {
    			/*
    			 * An exception should only happen if the buffer is too short and we walk off the end of the bytes,
    			 * because of the way we read the bytes from the device this should never happen, but just in case
    			 * we'll catch the exception
    			 */
    	        Log.d(TAG, "Failure building HrmReading from byte buffer, probably an incopmplete or corrupted buffer");
    		}
                
    		
            Log.d(TAG, "Building HrmReading from byte buffer complete, consumed " + bufferIndex + " bytes in the process");
            
            /*
             * One simple check to see if we parsed the bytes properly is to check if the ETX 
             * character was found where we expected it,  a more robust implementation would be
             * to calculate the CRC from the message contents and compare it to the CRC from 
             * the packet.  
             */
            if ( etx != ETX )
            	 Log.e(TAG,"...ETX mismatch!  The HxM message was not parsed properly");
            
            /*
             * log the contents of the HrmReading, use logcat to watch the data as it arrives  
             */
            dump();
            }

        
        /*
         * Display the HRM reading into the layout     
         */
            private void displayRaw() {  	  
            	display	( R.id.stx,  stx );
            	display ( R.id.msgId,  msgId );
            	display ( R.id.dlc,  dlc );
            	display ( R.id.firmwareId,   firmwareId );
            	display ( R.id.firmwareVersion,   firmwareVersion );
            	display ( R.id.hardwareId,   hardWareId );
            	display ( R.id.hardwareVersion,   hardwareVersion );
            	display ( R.id.batteryChargeIndicator,  (int)batteryIndicator );
            	display ( R.id.heartRate, (int)heartRate );
            	display ( R.id.heartBeatNumber,  (int)heartBeatNumber );
            	display ( R.id.hbTimestamp1,   hbTime1 );
            	display ( R.id.hbTimestamp2,   hbTime2 );
            	display ( R.id.hbTimestamp3,   hbTime3 );
            	display ( R.id.hbTimestamp4,   hbTime4 );
            	display ( R.id.hbTimestamp5,   hbTime5 );
            	display ( R.id.hbTimestamp6,   hbTime6 );
            	display ( R.id.hbTimestamp7,   hbTime7 );
            	display ( R.id.hbTimestamp8,   hbTime8 );
            	display ( R.id.hbTimestamp9,   hbTime9 );
            	display ( R.id.hbTimestamp10,   hbTime10 );
            	display ( R.id.hbTimestamp11,   hbTime11 );
            	display ( R.id.hbTimestamp12,   hbTime12 );
            	display ( R.id.hbTimestamp13,   hbTime13 );
            	display ( R.id.hbTimestamp14,   hbTime14 );
            	display ( R.id.hbTimestamp15,   hbTime15 );
            	display ( R.id.reserved1,   reserved1 );
            	display ( R.id.reserved2,   reserved2 );
            	display ( R.id.reserved3,   reserved3 );
            	display ( R.id.distance,   distance );
            	display ( R.id.speed,   speed );
            	display ( R.id.strides,  (int)strides );
            	display ( R.id.reserved4,  reserved4 );
            	display ( R.id.reserved5,  reserved5 );
            	display ( R.id.crc,  crc );
            	display ( R.id.etx,  etx );    	    	    	
            	
            }    

        
        
        /*
         * dump() sends the contents of the HrmReading object to the log, use 'logcat' to view
         */    
        public void dump() {
        		Log.d(TAG,"HrmReading Dump");
        		Log.d(TAG,"...serial "+ ( serial ));
        		Log.d(TAG,"...stx "+ ( stx ));
        		Log.d(TAG,"...msgId "+( msgId ));
        		Log.d(TAG,"...dlc "+ ( dlc ));
        		Log.d(TAG,"...firmwareId "+ ( firmwareId ));
        		Log.d(TAG,"...sfirmwareVersiontx "+ (  firmwareVersion ));
        		Log.d(TAG,"...hardWareId "+ (  hardWareId ));
        		Log.d(TAG,"...hardwareVersion "+ (  hardwareVersion ));
        		Log.d(TAG,"...batteryIndicator "+ ( batteryIndicator ));
        		Log.d(TAG,"...heartRate "+ ( heartRate ));
        		Log.d(TAG,"...heartBeatNumber "+ ( heartBeatNumber ));
        		Log.d(TAG,"...shbTime1tx "+ (  hbTime1 ));
        		Log.d(TAG,"...hbTime2 "+ (  hbTime2 ));
        		Log.d(TAG,"...hbTime3 "+ (  hbTime3 ));
        		Log.d(TAG,"...hbTime4 "+ (  hbTime4 ));
        		Log.d(TAG,"...hbTime4 "+ (  hbTime5 ));
        		Log.d(TAG,"...hbTime6 "+ (  hbTime6 ));
        		Log.d(TAG,"...hbTime7 "+ (  hbTime7 ));
        		Log.d(TAG,"...hbTime8 "+ (  hbTime8 ));
        		Log.d(TAG,"...hbTime9 "+ (  hbTime9 ));
        		Log.d(TAG,"...hbTime10 "+ (  hbTime10 ));
        		Log.d(TAG,"...hbTime11 "+ (  hbTime11 ));
        		Log.d(TAG,"...hbTime12 "+ (  hbTime12 ));
        		Log.d(TAG,"...hbTime13 "+ (  hbTime13 ));
        		Log.d(TAG,"...hbTime14 "+ (  hbTime14 ));
        		Log.d(TAG,"...hbTime15 "+ (  hbTime15 ));
        		Log.d(TAG,"...reserved1 "+ (  reserved1 ));
        		Log.d(TAG,"...reserved2 "+ (  reserved2 ));
        		Log.d(TAG,"...reserved3 "+ (  reserved3 ));
        		Log.d(TAG,"...distance "+ (  distance ));
        		Log.d(TAG,"...speed "+ (  speed ));
        		Log.d(TAG,"...strides "+ ( strides ));
        		Log.d(TAG,"...reserved4 "+ ( reserved4 ));
        		Log.d(TAG,"...reserved5 "+ ( reserved5 ));
        		Log.d(TAG,"...crc "+ ( crc ));
        		Log.d(TAG,"...etx "+ ( etx ));    	    	    	
        }    

        
        
/****************************************************************************
 * Some utility functions to control the formatting of HxM fields into the 
 * activity's view
 ****************************************************************************/
        
        
        /*
         * display a byte value
         */
    	private void display  ( int nField, byte d ) {   
    		String INT_FORMAT = "%x";
    		
    		String s = String.format(INT_FORMAT, d);

    		display( nField, s  );
    	}

    	/*
    	 * display an integer value
    	 */
    	private void display  ( int nField, int d ) {   
    		String INT_FORMAT = "%d";
    		
    		String s = String.format(INT_FORMAT, d);

    		display( nField, s  );
    	}

    	/*
    	 * display a long integer value
    	 */
    	private void display  ( int nField, long d ) {   
    		String INT_FORMAT = "%d";
    		
    		String s = String.format(INT_FORMAT, d);

    		display( nField, s  );
    	}

    	/*
    	 * display a character string
    	 */
    	private void display ( int nField, CharSequence  str  ) {
        	TextView tvw = (TextView) findViewById(nField);
        	if ( tvw != null )
        		tvw.setText(str);
        }
    }    	   
}