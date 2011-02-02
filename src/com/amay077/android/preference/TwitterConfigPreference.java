package com.amay077.android.preference;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;

import com.amay077.android.hexringer.Const;
import com.amay077.android.hexringer.R;
import com.amay077.android.logging.Log;
import com.amay077.android.os.ZippyAsyncTask;
import com.amay077.android.twitter.AuthInfo;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TwitterConfigPreference extends DialogPreference {
    /**
     * The edit text shown in the dialog.
     */
    private EditText editUserId = null;
    private EditText editPassword = null;
    private Button buttonAuth = null;
    private Button buttonCancel = null;
    private Button buttonUnauth = null;
    private TextView textAnthorized = null;

    private AuthInfo info = AuthInfo.makeEmpty();

    public TwitterConfigPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    	setDialogLayoutResource(R.layout.twitter_login);
    }

    public TwitterConfigPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 1);
    }

    public TwitterConfigPreference(Context context) {
        this(context, null);
    }

    /**
     * Saves the text to the {@link SharedPreferences}.
     *
     * @param text The text to save
     */
    public void setAuthInfo(AuthInfo info) {
        final boolean wasBlocking = shouldDisableDependents();

        this.info = info;

        persistString(info.toString());

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    /**
     * Gets the text from the {@link SharedPreferences}.
     *
     * @return The current preference value.
     */
    public AuthInfo getAuthInfo() {
        return this.info;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        editUserId = (EditText)view.findViewById(R.id.editTwitterUserID);
        editPassword = (EditText)view.findViewById(R.id.editTwitterPassword);
        buttonAuth = (Button)view.findViewById(R.id.btnAuth);
        buttonUnauth = (Button)view.findViewById(R.id.btnUnauth);
        buttonCancel = (Button)view.findViewById(R.id.btnCancel);
        textAnthorized = (TextView)view.findViewById(R.id.textAuthorized);


        AuthInfo info = getAuthInfo();
        boolean isAuthorized = !TextUtils.isEmpty(info.consumerToken);
        if (isAuthorized) {
        	editUserId.setEnabled(false);
        	editPassword.setVisibility(View.GONE);
        	buttonAuth.setVisibility(View.GONE);
        	buttonUnauth.setVisibility(View.VISIBLE);
        	textAnthorized.setVisibility(View.VISIBLE);
        } else {
        	editUserId.setEnabled(true);
        	editPassword.setVisibility(View.VISIBLE);
        	buttonAuth.setVisibility(View.VISIBLE);
        	buttonUnauth.setVisibility(View.GONE);
        	textAnthorized.setVisibility(View.GONE);
        }

        editUserId.setText(info.userId);

        buttonAuth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// Twitter xAuth
				new ZippyAsyncTask<Void, Void, AccessToken>(TwitterConfigPreference.this.getContext()) {

					@Override
					protected AccessToken doInBackground(Void... params) {
				        try {
					        ConfigurationBuilder confbuilder = new ConfigurationBuilder();
					        confbuilder.setOAuthConsumerKey(Const.TWITTER_CONSUMER_TOKEN);
					        confbuilder.setOAuthConsumerSecret(Const.TWITTER_CONSUMER_SECRET);

					        TwitterFactory twitterfactory = new TwitterFactory(confbuilder.build());
					        Twitter twitter = twitterfactory.getInstance(
					        		editUserId.getText().toString(),
					        		editPassword.getText().toString());

				        	return twitter.getOAuthAccessToken();
				        } catch (Exception e) {
				        	Log.w("TwitterConfigPreference", "Twitter xAuth failed.", e);
				        }

						return null;
					}

					@Override
					protected void onPostExecute(AccessToken result) {
						super.onPostExecute(result);

						if (result != null) {
				        	TwitterConfigPreference.this.info.consumerToken = result.getToken();
				        	TwitterConfigPreference.this.info.consumerSecret = result.getTokenSecret();
				        	TwitterConfigPreference.this.info.userId = editUserId.getText().toString();

				        	TwitterConfigPreference.this.getDialog().dismiss();
							onDialogClosed(true);
						}
					}
				}.execute((Void)null);
			}
		});

        buttonUnauth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// clear xAuth token
				TwitterConfigPreference.this.info = AuthInfo.makeEmpty();
				TwitterConfigPreference.this.getDialog().dismiss();
				onDialogClosed(true);
			}
		});

        buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				TwitterConfigPreference.this.getDialog().dismiss();
				onDialogClosed(false);
			}
		});
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            if (callChangeListener(info.toString())) {
                setAuthInfo(info);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setAuthInfo(AuthInfo.fromString(restoreValue ?
        		getPersistedString(info.toString()) : (String) defaultValue));
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(info.consumerToken) || super.shouldDisableDependents();
    }

    /**
     * Returns the {@link EditText} widget that will be shown in the dialog.
     *
     * @return The {@link EditText} widget that will be shown in the dialog.
     */
    public EditText getEditText() {
        return editUserId;
    }

//    /** @hide */
//    @Override
//    protected boolean needInputMethod() {
//        // We want the input method to show, if possible, when dialog is displayed
//        return true;
//    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.text = getAuthInfo();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setAuthInfo(myState.text);
    }

    private static class SavedState extends BaseSavedState {
        AuthInfo text;

        public SavedState(Parcel source) {
            super(source);
            text = AuthInfo.fromString(source.readString());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text.toString());
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

	public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
		public SavedState createFromParcel(Parcel in) {
			return new SavedState(in);
		}

		public SavedState[] newArray(int size) {
			return new SavedState[size];
		}
	};

}
