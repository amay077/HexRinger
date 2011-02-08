package com.amay077.android.os;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Handler;

public abstract class ZippyAsyncTask<Params, Progress, Result>
	extends AsyncTask<Params, Progress, Result> {

	private ProgressDialog dlg = null;
	private Timer startTimer = new Timer();
	private Context context = null;
	private String progressMessage = "認証中...";
	private int    progressDelay = 200;

	public int getProgressDelay() {
		return progressDelay;
	}

	public void setProgressDelay(int progressDelay) {
		this.progressDelay = progressDelay;
	}

	public ZippyAsyncTask(Context context) {
		this.context = context;
	}

	public String getProgressMessage() {
		return progressMessage;
	}

	public void setProgressMessage(String progressMessage) {
		this.progressMessage = progressMessage;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		final Handler handler = new Handler();

		startTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						openProgressDialog();
					}
				});
			}
		}, progressDelay);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		closeProgressDialog();
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		closeProgressDialog();
	}

	protected void openProgressDialog() {
		dlg = new ProgressDialog(context);
		dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dlg.setMessage(progressMessage);
		dlg.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ZippyAsyncTask.this.cancel(true);
			}
		});

		dlg.show();
	}

	protected void closeProgressDialog() {
		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
			dlg = null;
		}
	}

}
