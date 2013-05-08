package com.tec.spreadsheettogoogledrive;

import java.io.IOException;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class MainActivity extends Activity {
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;

	public static final String FILE_PATH = "file_path";

	static final String PREFS_NAME = "SPREADSHEET_PREFERENCES";
	static final String ACCOUNT_NAME = "SPREADSHEET_ACCOUNT_NAME";

	private static Drive service;
	private GoogleAccountCredential credential;

	private SharedPreferences _appPreferences;

	private java.io.File _fileToSend;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		_fileToSend = Util.convertToFile(Util.getTestLines());

		credential = GoogleAccountCredential.usingOAuth2(this,
				DriveScopes.DRIVE);

		_appPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		String savedAccountName = _appPreferences.getString(ACCOUNT_NAME, "");
		if (savedAccountName.isEmpty()) {
			startActivityForResult(credential.newChooseAccountIntent(),
					REQUEST_ACCOUNT_PICKER);
		} else {
			credential.setSelectedAccountName(savedAccountName);
			service = getDriveService(credential);
			saveFileToDrive();
		}
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null
					&& data.getExtras() != null) {
				String accountName = data
						.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {

					SharedPreferences.Editor edit = _appPreferences.edit();
					edit.putString(ACCOUNT_NAME, accountName);
					edit.commit();

					credential.setSelectedAccountName(accountName);
					service = getDriveService(credential);
					saveFileToDrive();
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				saveFileToDrive();
			} else {
				startActivityForResult(credential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}

	private void saveFileToDrive() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileContent mediaContent = new FileContent("text/csv",
							_fileToSend);

					// File's metadata.
					File body = new File();
					body.setTitle(_fileToSend.getName());
					body.setMimeType("text/csv");

					File file = service.files().insert(body, mediaContent)
							.setConvert(true).execute();
					if (file != null) {
						showToast("File uploaded: " + file.getTitle());
					}

				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	private Drive getDriveService(final GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast,
						Toast.LENGTH_SHORT).show();
			}
		});
	}
}