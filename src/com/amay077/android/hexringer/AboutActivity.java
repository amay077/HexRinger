package com.amay077.android.hexringer;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WebView webview = new WebView(this);
		setContentView(webview);

		webview.loadUrl(getString(R.string.pref_about_url));
		webview.setWebViewClient(new WebViewClient() {});
		webview.getSettings().setJavaScriptEnabled(true);
	}

}
