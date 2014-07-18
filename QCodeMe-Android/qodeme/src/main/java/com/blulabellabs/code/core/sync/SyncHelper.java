/*
 * Copyright 2012 Google Inc.
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
 */

package com.blulabellabs.code.core.sync;

import static com.blulabellabs.code.utils.LogUtils.LOGE;
import static com.blulabellabs.code.utils.LogUtils.LOGI;
import static com.blulabellabs.code.utils.LogUtils.makeLogTag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.blulabellabs.code.core.accounts.GenericAccountService;
import com.blulabellabs.code.core.data.entities.ChatEntity;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestSyncHelper;
import com.blulabellabs.code.core.io.hendler.AccountContactsHandler;
import com.blulabellabs.code.core.io.hendler.ChatFavoriteHandler;
import com.blulabellabs.code.core.io.hendler.ChatImageUploadHandler;
import com.blulabellabs.code.core.io.hendler.ChatLoadAddMemberHandler;
import com.blulabellabs.code.core.io.hendler.ChatLoadHandler;
import com.blulabellabs.code.core.io.hendler.ChatMessageHandler;
import com.blulabellabs.code.core.io.hendler.ContactAcceptHandler;
import com.blulabellabs.code.core.io.hendler.ContactAddHandler;
import com.blulabellabs.code.core.io.hendler.ContactBlockHandler;
import com.blulabellabs.code.core.io.hendler.ContactRejectHandler;
import com.blulabellabs.code.core.io.hendler.ContactSetInfoHandler;
import com.blulabellabs.code.core.io.hendler.MessageDeleteHandler;
import com.blulabellabs.code.core.io.hendler.MessageFlaggedHandler;
import com.blulabellabs.code.core.io.hendler.MessageReadHandler;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.responses.AccountContactsResponse;
import com.blulabellabs.code.core.io.responses.ChatLoadResponse;
import com.blulabellabs.code.core.io.responses.ChatMessageResponse;
import com.blulabellabs.code.core.io.responses.ContactAddResponse;
import com.blulabellabs.code.core.io.responses.DeleteMessageResponse;
import com.blulabellabs.code.core.io.responses.SetFavoriteResponse;
import com.blulabellabs.code.core.io.responses.SetFlaggedResponse;
import com.blulabellabs.code.core.io.responses.UploadImageResponse1;
import com.blulabellabs.code.core.io.responses.UserSettingsResponse;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.provider.QodemeContract.Sync;
import com.blulabellabs.code.utils.Converter;
import com.bugsense.trace.BugSenseHandler;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * A helper class for dealing with sync and other remote persistence operations.
 * All operations occur on the thread they're called from, so it's best to wrap
 * calls in an {@link android.os.AsyncTask}, or better yet, a
 * {@link android.app.Service}.
 */
public class SyncHelper {
	private static final String TAG = makeLogTag(SyncHelper.class);

	public static final String SYNC_EXTRAS_AFTER_LOGIN = "sync_extras_after_login";

	public static final int FLAG_SYNC_LOCAL = 0x1;
	public static final int FLAG_SYNC_REMOTE = 0x2;

	private static final int LOCAL_VERSION_CURRENT = 25;
	private static final String LOCAL_MAPVERSION_CURRENT = "\"vlh7Ig\"";

	private Context mContext;

	public SyncHelper(Context context) {
		mContext = context;
	}

	public static void requestManualSync() {
		Account account = GenericAccountService.GetAccount();
		Bundle b = new Bundle();
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(account, QodemeContract.CONTENT_AUTHORITY, b);
	}

	public static void requestAfterLoginSync() {
		Account account = GenericAccountService.GetAccount();
		Bundle b = new Bundle();
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(SYNC_EXTRAS_AFTER_LOGIN, true);
		ContentResolver.requestSync(account, QodemeContract.CONTENT_AUTHORITY, b);
	}

	/**
	 * Loads conference information (sessions, rooms, tracks, speakers, etc.)
	 * from a local static cache data and then syncs down data from the
	 * Conference API.
	 * 
	 * @param syncResult
	 *            Optional {@link android.content.SyncResult} object to
	 *            populate.
	 * @throws java.io.IOException
	 */
	public void performSync(SyncResult syncResult, int flags) throws IOException {

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		final int localVersion = prefs.getInt("local_data_version", 0);

		// Bulk of sync work, performed by executing several fetches from
		// local and online sources.
		final ContentResolver resolver = mContext.getContentResolver();
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

		LOGI(TAG, "Performing sync");
	}

	public static void doInitialSync(Context context, ContentResolver contentResolver) {
		LOGI(TAG, "Initial sync");
		ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		RestSyncHelper rest = RestSyncHelper.getInstance(context);
		contentResolver.delete(QodemeContract.BASE_CONTENT_URI, null, null);
		try {
			AccountContactsResponse accountContactsResponse = rest.accountContacts();
			AccountContactsHandler accountContactHandler = new AccountContactsHandler(context);
			accountContactHandler.parseAndApply(accountContactsResponse);
			UserSettingsResponse userSettingsResponse = rest.getUserSettings();
			if (userSettingsResponse.getSettings() != null) {
				QodemePreferences.getInstance().setUserSettingsResponse(userSettingsResponse);
			} else {
				QodemePreferences pref = QodemePreferences.getInstance();
				pref.initUserSettings();
				rest.setUserSettings();
				pref.setUserSettingsUpToDate(true);
			}
			loadAllChats(context, batch, rest, accountContactsResponse);
			QodemeContract.applyBatch(context, batch);
		} catch (RestError e) {
			LOGE(TAG, e.toString(context), e);
		} catch (InterruptedException e) {
			LOGE(TAG, "doInitialSync", e);
		} catch (ExecutionException e) {
			LOGE(TAG, "doInitialSync", e);
		} catch (JSONException e) {
			LOGE(TAG, "doInitialSync", e);
		}
	}

	private static void loadAllChats(Context context, ArrayList<ContentProviderOperation> batch,
			RestSyncHelper rest, AccountContactsResponse accountContactsResponse)
			throws InterruptedException, ExecutionException, JSONException, RestError {
		// For One2One chats
		for (Contact c : accountContactsResponse.getContactList()) {
			long chatId = c.chatId;
			ChatLoadResponse chatLoadResponse = rest.chatLoad(chatId, 0, 1000);
			new ChatLoadHandler(context).parse(chatLoadResponse, batch);
		}
		// For group chats
		for (ChatEntity c : accountContactsResponse.getChatList()) {
			long chatId = c.getId();
			ChatLoadResponse chatLoadResponse = rest.chatLoad(chatId, 0, 1000);
			new ChatLoadHandler(context).parse(chatLoadResponse, batch);
		}
	}

	public static void doFromPreference(Context context, ContentResolver contentResolver) {
		ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		RestSyncHelper rest = RestSyncHelper.getInstance(context);
		try {
			loadFromPreference(context, batch, rest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (RestError e) {
			e.printStackTrace();
		}
	}

	public static void loadFromPreference(Context context,
			ArrayList<ContentProviderOperation> batch, RestSyncHelper rest)
			throws InterruptedException, ExecutionException, JSONException, RestError {
		String id = QodemePreferences.getInstance().get("AddMemberId", "");
		if (id.trim().equals("")) {
		} else {
			QodemePreferences.getInstance().set("AddMemberId", "");
			String chatIdList[] = id.split(",");
			for (String c : chatIdList) {
				long chatId = Long.parseLong(c);
				context.getContentResolver().delete(QodemeContract.Messages.CONTENT_URI, QodemeContract.Messages.MESSAGE_CHAT_ID+"="+chatId, null);
				ChatLoadResponse chatLoadResponse = rest.chatLoad(chatId, 0, 1000);
				new ChatLoadAddMemberHandler(context).parseAndApply(chatLoadResponse);// ,

				if (chatLoadResponse.getChatLoad().is_favorite != 1)
					context.getContentResolver().update(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.updateFavorite(1,
									chatLoadResponse.getChatLoad().number_of_likes + 1),
							QodemeContract.Chats.CHAT_ID + " = " + chatId, null);
				// rest.setFavorite(date, is_favorite, chat_id)
			}
		}
	}

	public static void doContactsSync(Context context, ContentResolver contentResolver) {
		LOGI(TAG, "Contacts sync");
		ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		RestSyncHelper rest = RestSyncHelper.getInstance(context);
		Cursor cursor = contentResolver.query(QodemeContract.Contacts.CONTENT_URI,
				QodemeContract.Contacts.ContactQuery.PROJECTION, QodemeContract.Contacts.UPDATED
						+ " != " + QodemeContract.Contacts.Sync.DONE, null, null);

		if (cursor.moveToFirst())
			do {
				int update = cursor.getInt(QodemeContract.Contacts.ContactQuery.UPDATED);
				long id = cursor.getLong(QodemeContract.Contacts.ContactQuery._ID);
				String qrCode = cursor
						.getString(QodemeContract.Contacts.ContactQuery.CONTACT_QRCODE);
				if ((update & QodemeContract.Contacts.Sync.NEW) == QodemeContract.Contacts.Sync.NEW) {
					try {
						String publicName = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_PUBLIC_NAME);
						String location = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_LOCATION);
						String message = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_MESSAGE);
						ContactAddResponse contactAddResponse = rest.contactAdd(qrCode, publicName,
								message, location);
						new ContactAddHandler(context, id).parse(contactAddResponse, batch);
						long chatId = contactAddResponse.getContact().chatId;
						String title = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_TITLE);
						int color = cursor
								.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_COLOR);

						Cursor cursorChat = contentResolver.query(QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.ChatQuery.PROJECTION,
								QodemeContract.Chats.CHAT_ID + " = " + chatId, null, null);

						String desc = "";
						int is_locked = 0;
						String status = "";
						String tags = "";
						int chat_color = -1;
						String latitude = "0";
						String longitude = "0";
						if (cursorChat != null && cursorChat.getCount() > 0) {
							cursorChat.moveToFirst();
							desc = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_DESCRIPTION);
							is_locked = cursorChat
									.getInt(QodemeContract.Chats.ChatQuery.CHAT_IS_LOCKED);
							status = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_STATUS);
							tags = cursorChat.getString(QodemeContract.Chats.ChatQuery.CHAT_TAGS);
							chat_color = cursorChat
									.getInt(QodemeContract.Chats.ChatQuery.CHAT_COLOR);
							latitude = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_LATITUDE);
							longitude = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_LONGITUDE);
							// int type =
							// cursor.getInt(QodemeContract.Chats.ChatQuery.CHAT_TYPE);
							// if (type == 0)
							// chat_color = color;
						}

						VoidResponse response = rest.chatSetInfo(chatId, title, color, null, desc,
								is_locked, status, tags, chat_color, latitude, longitude);
						new ContactSetInfoHandler(context, id).parse(response, batch);
					} catch (RestError e) {
						BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
						LOGE(TAG, e.toString(context), e);
					} catch (InterruptedException e) {
						LOGE(TAG, "doInitialSync", e);
					} catch (ExecutionException e) {
						LOGE(TAG, "doInitialSync", e);
					} catch (JSONException e) {
						LOGE(TAG, "doInitialSync", e);
					}
				} else {
					if ((update & QodemeContract.Contacts.Sync.STATE_UPDATED) == QodemeContract.Contacts.Sync.STATE_UPDATED) {
						try {
							int state = cursor
									.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_STATE);
							switch (state) {
							case QodemeContract.Contacts.State.APPRUVED: {
								VoidResponse response = rest.contactAccept(qrCode);
								new ContactAcceptHandler(context, id).parse(response, batch);
								break;
							}
							case QodemeContract.Contacts.State.REJECTED: {
								VoidResponse response = rest.contactReject(qrCode);
								new ContactRejectHandler(context, id).parse(response, batch);
								update = QodemeContract.Contacts.Sync.DONE;
								break;
							}
							case QodemeContract.Contacts.State.BLOCKED_BY: {
								VoidResponse response = rest.contactBlock(qrCode);
								new ContactBlockHandler(context, id).parse(response, batch);
								update = QodemeContract.Contacts.Sync.DONE;
								break;
							}
							}
						} catch (RestError e) {
							BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
							LOGE(TAG, e.toString(context), e);
						} catch (InterruptedException e) {
							LOGE(TAG, "doInitialSync", e);
						} catch (ExecutionException e) {
							LOGE(TAG, "doInitialSync", e);
						} catch (JSONException e) {
							LOGE(TAG, "doInitialSync", e);
						}
					}
					if ((update & QodemeContract.Contacts.Sync.UPDATED) == QodemeContract.Contacts.Sync.UPDATED) {
						// update & (QodemeContract.Contacts.Sync.UPDATED ^
						// 0xff)
						long chatId = cursor
								.getLong(QodemeContract.Contacts.ContactQuery.CONTACT_CHAT_ID);
						String title = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_TITLE);
						int color = cursor
								.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_COLOR);
						String desc = "";
						int is_locked = 0;
						String status = "";
						String tags = "";
						int chat_color = -1;
						String latitude = "0";
						String longitude = "0";
						Cursor cursorChat = contentResolver.query(QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.ChatQuery.PROJECTION,
								QodemeContract.Chats.CHAT_ID + " = " + chatId, null, null);
						if (cursorChat != null && cursorChat.getCount() > 0) {
							cursorChat.moveToFirst();
							desc = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_DESCRIPTION);
							is_locked = cursorChat
									.getInt(QodemeContract.Chats.ChatQuery.CHAT_IS_LOCKED);
							status = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_STATUS);
							tags = cursorChat.getString(QodemeContract.Chats.ChatQuery.CHAT_TAGS);
							chat_color = cursorChat
									.getInt(QodemeContract.Chats.ChatQuery.CHAT_COLOR);
							latitude = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_LATITUDE);
							longitude = cursorChat
									.getString(QodemeContract.Chats.ChatQuery.CHAT_LONGITUDE);
							// int type =
							// cursor.getInt(QodemeContract.Chats.ChatQuery.CHAT_TYPE);
							// if (type == 0)
							// chat_color = color;
						}
						try {
							VoidResponse response = rest.chatSetInfo(chatId, title, color, null,
									desc, is_locked, status, tags, chat_color, latitude, longitude);
							new ContactSetInfoHandler(context, id).parse(response, batch);
						} catch (RestError e) {
							BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
							LOGE(TAG, e.toString(context), e);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

			} while (cursor.moveToNext());

		QodemeContract.applyBatch(context, batch);
	}

	// public static void doChatSync123(Context context, ContentResolver
	// contentResolver) {
	// LOGI(TAG, "Chat sync");
	// ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
	// RestSyncHelper rest = RestSyncHelper.getInstance(context);
	// // Cursor cursor =
	// // contentResolver.query(QodemeContract.Contacts.CONTENT_URI,
	// // QodemeContract.Contacts.ContactQuery.PROJECTION,
	// // QodemeContract.Contacts.UPDATED
	// // + " != " + QodemeContract.Contacts.Sync.DONE, null, null);
	// Cursor cursorChat =
	// contentResolver.query(QodemeContract.Chats.CONTENT_URI,
	// QodemeContract.Chats.ChatQuery.PROJECTION,
	// QodemeContract.Chats.CHAT_TYPE + " != 0", null, null);
	//
	// if (cursorChat.moveToFirst())
	// do {
	// try {
	//
	// long chatId = cursorChat.getLong(QodemeContract.Chats.ChatQuery.CHAT_ID);
	// String title =
	// cursorChat.getString(QodemeContract.Chats.ChatQuery.CHAT_TITLE);
	//
	// String desc = "";
	// int is_locked = 0;
	// String status = "";
	// String tags = "";
	// Integer chat_color;
	// desc =
	// cursorChat.getString(QodemeContract.Chats.ChatQuery.CHAT_DESCRIPTION);
	// is_locked =
	// cursorChat.getInt(QodemeContract.Chats.ChatQuery.CHAT_IS_LOCKED);
	// status =
	// cursorChat.getString(QodemeContract.Chats.ChatQuery.CHAT_STATUS);
	// tags = cursorChat.getString(QodemeContract.Chats.ChatQuery.CHAT_TAGS);
	// chat_color =
	// cursorChat.getInt(QodemeContract.Chats.ChatQuery.CHAT_COLOR);
	// // int type =
	// // cursor.getInt(QodemeContract.Chats.ChatQuery.CHAT_TYPE);
	// // if (type == 0)
	//
	// VoidResponse response = rest.chatSetInfo(chatId, null, null, null, desc,
	// is_locked, status, tags, chat_color);
	// } catch (RestError e) {
	// BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
	// LOGE(TAG, e.toString(context), e);
	// } catch (InterruptedException e) {
	// LOGE(TAG, "doInitialSync", e);
	// } catch (ExecutionException e) {
	// LOGE(TAG, "doInitialSync", e);
	// } catch (JSONException e) {
	// LOGE(TAG, "doInitialSync", e);
	// }
	//
	// } while (cursorChat.moveToNext());
	//
	// // QodemeContract.applyBatch(context, batch);
	// }

	public static void doChatFavoriteSync(Context context, ContentResolver contentResolver) {
		LOGI(TAG, "Chat sync");
		// ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		RestSyncHelper rest = RestSyncHelper.getInstance(context);
		// Cursor cursor =
		// contentResolver.query(QodemeContract.Contacts.CONTENT_URI,
		// QodemeContract.Contacts.ContactQuery.PROJECTION,
		// QodemeContract.Contacts.UPDATED
		// + " != " + QodemeContract.Contacts.Sync.DONE, null, null);
		Cursor cursorChat = contentResolver.query(QodemeContract.Chats.CONTENT_URI,
				QodemeContract.Chats.ChatQuery.PROJECTION, QodemeContract.Chats.CHAT_IS_FAVORITE
						+ " != 0", null, null);

		if (cursorChat.moveToFirst())
			do {
				try {
					int updated = cursorChat.getInt(QodemeContract.Chats.ChatQuery.UPDATED);
					if (updated == QodemeContract.Sync.UPDATED) {

						long _id = cursorChat.getLong(QodemeContract.Chats.ChatQuery._ID);
						long chatId = cursorChat.getLong(QodemeContract.Chats.ChatQuery.CHAT_ID);
						int is_favorite = cursorChat
								.getInt(QodemeContract.Chats.ChatQuery.CHAT_IS_FAVORITE);

						String date = Converter.getCurrentGtmTimestampString();

						ContentValues values = new ContentValues();
						values.put(QodemeContract.Chats.UPDATED, Sync.DONE);

						contentResolver.update(QodemeContract.Chats.CONTENT_URI, values,
								QodemeContract.Chats.CHAT_ID + "=" + chatId, null);
						if (is_favorite == 2)
							is_favorite = 0;
						SetFavoriteResponse favoriteResponse = rest.setFavorite(date, is_favorite,
								chatId);

						new ChatFavoriteHandler(context, _id, is_favorite)
								.parseAndApply(favoriteResponse);
					}
				} catch (RestError e) {
					BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
					LOGE(TAG, e.toString(context), e);
				} catch (InterruptedException e) {
					LOGE(TAG, "doInitialSync", e);
				} catch (ExecutionException e) {
					LOGE(TAG, "doInitialSync", e);
				} catch (JSONException e) {
					LOGE(TAG, "doInitialSync", e);
				}

			} while (cursorChat.moveToNext());

		// QodemeContract.applyBatch(context, batch);
	}

	public static void doSettingsSync(Context context) {
		LOGI(TAG, "User settings sync");
		QodemePreferences pref = QodemePreferences.getInstance();
		RestSyncHelper rest = RestSyncHelper.getInstance(context);
		if (!QodemePreferences.getInstance().isUserSettingsUpToDate()) {
			try {
				rest.setUserSettings();
				pref.setUserSettingsUpToDate(true);
			} catch (RestError e) {
				BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
				LOGE(TAG, e.toString(context), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public static void doMessageSync(Context context, ContentResolver contentResolver) {
		RestSyncHelper rest = RestSyncHelper.getInstance(context);
		Cursor cursor = contentResolver.query(QodemeContract.Messages.CONTENT_URI,
				QodemeContract.Messages.Query.PROJECTION, QodemeContract.Messages.UPDATED + " != "
						+ QodemeContract.Messages.Sync.DONE, null, null);
		if (cursor.moveToFirst())
			do {
				long id = cursor.getLong(QodemeContract.Messages.Query._ID);
				int state = cursor.getInt(QodemeContract.Messages.Query.MESSAGE_STATE);
				int is_flagged = cursor.getInt(QodemeContract.Messages.Query.MESSAGE_HAS_FLAGGED);
				int is_deleted = cursor.getInt(QodemeContract.Messages.Query.MESSAGE_IS_DELETED);
				int update = cursor.getInt(QodemeContract.Messages.Query.UPDATED);
				if (update == (update & QodemeContract.Sync.NEW)
						&& state == QodemeContract.Messages.State.LOCAL) {
					try {
						long chatId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_CHAT_ID);
						String message = cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_TEXT);
						long created = (long) (Converter.getCrurentTimeFromTimestamp(cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_CREATED)) / 1E3);

						int is_search = cursor
								.getInt(QodemeContract.Messages.Query.MESSAGE_IS_SEARCH);

						String dateString = cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_CREATED);

						String photoUrl = cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_PHOTO_URL);
						int hashPhoto = cursor
								.getInt(QodemeContract.Messages.Query.MESSAGE_HAS_PHOTO);
						long replyTo_id = cursor
								.getInt(QodemeContract.Messages.Query.MESSAGE_REPLY_TO_ID);
						String latitude = cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_LATITUDE);
						String longitude = cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_LONGITUDE);
						String senderName = cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_SENDERNAME);
						String imageLocal = cursor
								.getString(QodemeContract.Messages.Query.MESSAGE_PHOTO_URL_LOCAL);

						if (hashPhoto == 1 && photoUrl.trim().equals("")) {
							String mProfileImageBase64 = null;
							try {
								File file = new File(imageLocal);
								// Bitmap resizedBitmap = decodeFile(file);
								//
								// Matrix matrix = new Matrix();
								// matrix.postRotate(getImageOrientation(file.getAbsolutePath()
								// .toString().trim()));
								// Bitmap rotatedBitmap =
								// Bitmap.createBitmap(resizedBitmap, 0, 0,
								// resizedBitmap.getWidth(),
								// resizedBitmap.getHeight(),
								// matrix, true);
								//
								// ByteArrayOutputStream stream = new
								// ByteArrayOutputStream();
								// rotatedBitmap.compress(Bitmap.CompressFormat.PNG,
								// 100, stream);
								// byte[] byteArray = stream.toByteArray();
								byte[] byteArray = convertFileToByteArray(file);

								mProfileImageBase64 = Base64.encodeToString(byteArray,
										Base64.NO_WRAP);

							} catch (OutOfMemoryError e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (mProfileImageBase64 != null) {
								UploadImageResponse1 imageResponse = rest.chatImage(id,
										mProfileImageBase64);
								new ChatImageUploadHandler(context, id)
										.parseAndApply(imageResponse);
								ChatMessageResponse response = rest.chatMessage(chatId, message,
										created, imageResponse.getUrl(), hashPhoto, replyTo_id,
										latitude, longitude, senderName, dateString, is_search);
								new ChatMessageHandler(context, id).parseAndApply(response);
							}
							// Intent intent = new Intent(context,
							// UploadImageService.class);
							// intent.putExtra(UploadImageService.LOCAL_PATH,
							// imageLocal);
							// intent.putExtra(UploadImageService.MESSAGE_ID,
							// id);
							// context.startService(intent);
						} else {
							ChatMessageResponse response = rest.chatMessage(chatId, message,
									created, photoUrl, hashPhoto, replyTo_id, latitude, longitude,
									senderName, dateString, is_search);
							new ChatMessageHandler(context, id).parseAndApply(response);
						}
					} catch (RestError e) {
						BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
						LOGE(TAG, e.toString(context), e);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if (update == (update & QodemeContract.Sync.UPDATED) && is_deleted == 1) {

					try {
						long messageId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_ID);
						long chat_id = cursor
								.getLong(QodemeContract.Messages.Query.MESSAGE_CHAT_ID);
						DeleteMessageResponse response = rest.deleteMessage(messageId, chat_id);
						new MessageDeleteHandler(context, id).parseAndApply(response);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RestError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (update == (update & QodemeContract.Sync.UPDATED)
						&& state == QodemeContract.Messages.State.READ_LOCAL) {
					try {
						long messageId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_ID);

						VoidResponse response = rest.messageRead(messageId);
						new MessageReadHandler(context, id).parseAndApply(response);
					} catch (RestError e) {
						BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
						LOGE(TAG, e.toString(context), e);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (update == (update & QodemeContract.Sync.UPDATED) && is_flagged != 0) {
					try {
						long messageId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_ID);
						long chatId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_CHAT_ID);

						if (is_flagged != 1)
							is_flagged = 0;
						SetFlaggedResponse response = rest
								.setFlagged(messageId, is_flagged, chatId);
						new MessageFlaggedHandler(context, id).parseAndApply(response);
					} catch (RestError e) {
						BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
						LOGE(TAG, e.toString(context), e);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} while (cursor.moveToNext());
	}

	public static byte[] convertFileToByteArray(File f) {
		byte[] byteArray = null;
		try {
			InputStream inputStream = new FileInputStream(f);

			byteArray = ByteStreams.toByteArray(inputStream);
			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// byte[] b = new byte[1024 * 8];
			// int bytesRead = 0;
			//
			// while ((bytesRead = inputStream.read(b)) != -1) {
			// bos.write(b, 0, bytesRead);
			// }
			//
			// byteArray = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	private static Bitmap decodeFile(File f) {
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 70;

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public static int getImageOrientation(String imagePath) {
		int rotate = 0;
		try {

			File imageFile = new File(imagePath);
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			// Log.d(TAG,"orientation : "+orientation);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rotate;
	}
}
