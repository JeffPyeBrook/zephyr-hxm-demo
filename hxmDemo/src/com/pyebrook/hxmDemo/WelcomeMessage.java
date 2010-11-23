/**
 * 
 */
package com.pyebrook.hxmDemo;

import android.os.Bundle;
import android.webkit.WebView;

/**
 * @author jeff
 *
 */
public class WelcomeMessage extends android.app.Activity {
	WebView mWebView;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.webview);

	    mWebView = (WebView) findViewById(R.id.webview);
	    mWebView.getSettings().setJavaScriptEnabled(true);
	    mWebView.loadUrl("file:///android_asset/hxmdemo.html");
	}
	
}
