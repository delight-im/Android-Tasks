package im.delight.android.tasks;

/*
 * Copyright (c) delight.im <info@delight.im>
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

import android.os.Build;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Base class for a recurring IntentService components:
 *
 * The service may be started manually at any time via startService(context, ServiceClass.class))
 *
 * However, this class ensures that subclassed IntentServices will always adhere to the given time preferences
 *
 * Thus the service will not execute more frequently than allowed, no matter how often you start it manually
 *
 * For all automatically scheduled executions, the service will adhere to the minimum hour and maximum hour allowed
 *
 * In addition to that, the service will automatically schedule its next regular execution whenever it runs
 *
 * You may, for example, call startService(context, ServiceClass.class)) in every onCreate(...) of your Activity
 *
 * This class makes sure that your service will not run more often than you want in any case.
 */
abstract public class RegularIntentService extends IntentService {

	public RegularIntentService() {
		super("RegularIntentService");
	}

	@SuppressLint("NewApi")
	@Override
	protected final void onHandleIntent(Intent intent) {
		// get access to the preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// get the time of the service's last execution
		long lastExecutionTime = prefs.getLong(getLastExecutionPreference(), 0);

		// if we may run the service again (enough time passed since last execution)
		if ((System.currentTimeMillis() - lastExecutionTime) > getInterval()) {
			// change the time of the last execution
			lastExecutionTime = System.currentTimeMillis();

			// update the time of the last execution in the preferences
			SharedPreferences.Editor editor = prefs.edit();
			editor.putLong(getLastExecutionPreference(), lastExecutionTime);
			if (Build.VERSION.SDK_INT >= 9) {
				editor.apply();
			}
			else {
				editor.commit();
			}

			// perform the actual tasks
			run(intent);
		}

		// automatically schedule the next execution
		scheduleNextExecution(lastExecutionTime + getInterval());
	}

	protected void scheduleNextExecution(long nextMinimumTime) {
		// set up the AlarmManager
		final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		// create the Intent that will be started for the next execution
		final Intent scheduledIntent = new Intent(this, this.getClass());
		final PendingIntent scheduledPendingIntent = PendingIntent.getService(this, getServiceID(), scheduledIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// cancel any previous executions that may have been planned
		alarmManager.cancel(scheduledPendingIntent);

		// calculate the time for the next execution (taking the minimum and maximum hour into consideration)
		Calendar scheduledTime = new GregorianCalendar();
		// start with the earliest time that is possible for the next execution
		scheduledTime.setTimeInMillis(nextMinimumTime);

		// if the service is to be scheduled at a random point in time
		if (getRandomInterval() > 0) {
			// calculate a random number of milliseconds to postpone the execution by
			Random random = new Random();
			int postponeByMillis = random.nextInt(getRandomInterval());

			// add the number of milliseconds to the scheduled time
			scheduledTime.setTimeInMillis(scheduledTime.getTimeInMillis()+postponeByMillis);
		}

		// make sure the service will not run before the minimum hour or after the maximum hour on a day
		// if the next execution time would be earlier on a day than the minimum hour
		if (scheduledTime.get(Calendar.HOUR_OF_DAY) < getHourMin()) {
			// postpone the execution to the minimum hour on the same day
			scheduledTime.set(Calendar.HOUR_OF_DAY, getHourMin());
			scheduledTime.set(Calendar.MINUTE, 0);
		}
		// if the next execution time would be later on a day than the maximum hour
		else if (scheduledTime.get(Calendar.HOUR_OF_DAY) > getHourMax()) {
			// schedule the execution for the minimum hour on the next day instead
			scheduledTime.set(Calendar.HOUR_OF_DAY, getHourMin());
			scheduledTime.set(Calendar.MINUTE, 0);
			scheduledTime.add(Calendar.DAY_OF_YEAR, 1);
		}

		// schedule the next execution
		alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledTime.getTimeInMillis(), scheduledPendingIntent);
	}

	/** Must return the name for the preference that saves the last execution time for this service */
	abstract protected String getLastExecutionPreference();

	/** Must return the minimum interval (in milliseconds) that has to pass between two executions */
	abstract protected long getInterval();

	/** Must return a unique ID for this service (services with the same IDs overwrite each other's execution times) */
	abstract protected int getServiceID();

	/** Must return the earliest hour on a day (0-23) that the service may run at (will run at HH:00 or later) */
	abstract protected int getHourMin();

	/** Must return the latest hour on a day (1-24) that the service may run at (will run at HH:59 or earlier) */
	abstract protected int getHourMax();

	/** Must return a positive number of milliseconds if the service does not need to run as early as possible but at a random time (within that time range) */
	abstract protected int getRandomInterval();

	/** Must be the implementation of the tasks to run (Intent parameter from onHandleIntent(...) will be passed) */
	abstract protected void run(Intent intent);

}
