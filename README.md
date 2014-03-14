# Android-Tasks

Helper classes for recurring and automatic tasks in Android

## Installation

 * Copy the Java package to your project's source folder
 * or
 * Create a new library project from this repository and reference it in your project

## Usage

### RegularIntentService

#### AndroidManifest.xml

```
<service android:name=".MyRegularService" />
```

#### Any application component

Paste the following code into any Activity, Service, IntentService, BroadcastReceiver, etc. in order to start the RegularIntentService for the first time and later ensure that it is still set up. You can call this as many times as you want, the RegularIntentService will never run more frequently than specified.

```
startService(new Intent(this, MyRegularService.class));
```

#### Your new custom RegularIntentService class

```
public class MyRegularService extends RegularIntentService {

	@Override
	protected String getLastExecutionPreference() {
		// return the name of the preference where to save the last execution time in
		return "last_my_regular_service";
	}

	@Override
	protected long getInterval() {
		// return the interval between executions in milliseconds, e.g. <43200000> for 12 hours
		return 43200000;
	}

	@Override
	protected int getServiceID() {
		// return a service ID (integer) that is unique within your project
		return 1;
	}

	@Override
	protected int getHourMin() {
		// return the earliest hour that this task may run at
		return 0;
	}

	@Override
	protected int getHourMax() {
		// return the latest hour that this task may run at
		return 24;
	}

	@Override
	protected int getRandomInterval() {
		// return <0> to always run on time or return a positive integer to specify a random interval in milliseconds
		return 0;
	}

	@Override
	protected void run(Intent intent) {
		// put the actual code here that you want to run regularly
	}

}
```

## License

```
Copyright 2014 www.delight.im <info@delight.im>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```