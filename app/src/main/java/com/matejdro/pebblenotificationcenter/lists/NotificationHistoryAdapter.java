package com.matejdro.pebblenotificationcenter.lists;

import android.content.Context;
import android.content.Intent;
import com.matejdro.pebblenotificationcenter.NotificationHistoryStorage;
import com.matejdro.pebblenotificationcenter.NotificationKey;
import com.matejdro.pebblenotificationcenter.PebbleNotification;
import com.matejdro.pebblenotificationcenter.pebble.modules.ListModule;
import com.matejdro.pebblenotificationcenter.pebble.modules.NotificationSendingModule;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class NotificationHistoryAdapter implements NotificationListAdapter {
	private List<PebbleNotification> notifications;
	private NotificationHistoryStorage storage;
	private Context context;

	public NotificationHistoryAdapter(Context context, NotificationHistoryStorage storage) {
		this.storage = storage;
		this.context = context;
		
		loadNotifications(context);
	}
	
	public void loadNotifications(Context context)
	{
		Cursor cursor = storage.getReadableDatabase().rawQuery("SELECT PostTime, Title, Subtitle, Text, Icon FROM notifications ORDER BY PostTime DESC LIMIT 150", null);
		notifications = new ArrayList<PebbleNotification>(cursor.getCount());
		
		while (cursor.moveToNext())
		{
            long sendingDate = cursor.getLong(0);
            String title = cursor.getString(1);
            String text = cursor.getString(3) + "\n\nSent on " + ListModule.getFormattedDate(context, sendingDate);
            NotificationKey key = new NotificationKey(null, null, null);
			byte[] iconData = cursor.getBlob(4);
			Bitmap icon = null;
			if (iconData != null)
				icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);

            PebbleNotification notification = new PebbleNotification(title, text, key);
			notification.setSubtitle(cursor.getString(2));
			notification.setPostTime(sendingDate);
            notification.setListNotification(true);
			notification.setDismissable(true);
			notification.setNotificationIcon(icon);

			notifications.add(notification);
		}

		cursor.close();
	}

	@Override
	public PebbleNotification getNotificationAt(int index) {
		return notifications.get(index);
	}

	@Override
	public int getNumOfNotifications() {
		return notifications.size();
	}

	@Override
	public void forceRefresh() {
		loadNotifications(context);
	}
}
