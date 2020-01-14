# react-native-get-sms-android

Module that supports interaction with the Messaging API on Android

The package allows you to:

- get messages
- send messages
- delete messages

> Decided to start this package because [_react-native-android-sms_](https://github.com/msmakhlouf/react-native-android-sms) wasn't maintained at the time.

---

## Getting started

#### Yarn

`$ yarn add react-native-get-sms-android`

#### Npm

`$ npm install react-native-get-sms-android --save`

### Mostly automatic installation

`$ react-native link react-native-get-sms-android`

#### Manual installation

_android/settings.gradle_

    include ':react-native-get-sms-android'
    project(':react-native-get-sms-android').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-get-sms-android/android')

_android/app/build.gradle_

    dependencies{
        compile project(':react-native-get-sms-android')
     }

_MainApplication.java_

    import com.react.SmsPackage;

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new SmsPackage()
        // (...)
      );
    }

#### Android Permissions

**Note**: This has changed from 2.x. See `Upgrading to 2.x` section if using <=2.x

Add permissions to your `android/app/src/main/AndroidManifest.xml` file.

```xml
...
  <uses-permission android:name="android.permission.READ_SMS" />
  <uses-permission android:name="android.permission.WRITE_SMS" />
  <uses-permission android:name="android.permission.SEND_SMS" />
...
```

## Upgrading to 2.x

You need to add permissions manually. `react-native-get-sms-android` does not automatically require permissions from 2.x. Refer to [this](https://github.com/briankabiro/react-native-get-sms-android/issues/34) issue.

You need to require permissions in your `AndroidManifest.xml` file's `application` element based on what functions you plan to use like [the official documentation](https://developer.android.com/guide/topics/permissions/overview) describes:

| Function            | Permission needed            |
| ------------------- | ---------------------------- |
| SmsAndroid.list     | android.permission.READ_SMS  |
| SmsAndroid.delete   | android.permission.WRITE_SMS |
| SmsAndroid.autoSend | android.permission.SEND_SMS  |

## Usage

### List SMS Messages

```javascript
import SmsAndroid from 'react-native-get-sms-android';

/* List SMS messages matching the filter */
var filter = {
  box: 'inbox', // 'inbox' (default), 'sent', 'draft', 'outbox', 'failed', 'queued', and '' for all

  /**
   *  the next 3 filters can work together, they are AND-ed
   *  
   *  minDate, maxDate filters work like this:
   *    - If and only if you set a maxDate, it's like executing this SQL query:
   *    "SELECT * from messages WHERE (other filters) AND date <= maxDate"
   *    - Same for minDate but with "date >= minDate"
   */
  minDate: 1554636310165, // timestamp (in milliseconds since UNIX epoch)
  maxDate: 1556277910456, // timestamp (in milliseconds since UNIX epoch)
  bodyRegex: '(.*)How are you(.*)', // content regex to match

  /** the next 5 filters should NOT be used together, they are OR-ed so pick one **/
  read: 0, // 0 for unread SMS, 1 for SMS already read
  _id: 1234, // specify the msg id
  thread_id: 12 // specify the conversation thread_id
  address: '+1888------', // sender's phone number
  body: 'How are you', // content to match
  /** the next 2 filters can be used for pagination **/
  indexFrom: 0, // start from index 0
  maxCount: 10, // count of SMS to return each time
};

SmsAndroid.list(
  JSON.stringify(filter),
  (fail) => {
    console.log('Failed with this error: ' + fail);
  },
  (count, smsList) => {
    console.log('Count: ', count);
    console.log('List: ', smsList);
    var arr = JSON.parse(smsList);

    arr.forEach(function(object) {
      console.log('Object: ' + object);
      console.log('-->' + object.date);
      console.log('-->' + object.body);
    });
  },
);

/*
Each sms will be represents by a JSON object represented below

{
  "_id": 1234,
  "thread_id": 3,
  "address": "2900",
  "person": -1,
  "date": 1365053816196,
  "date_sent": 0,
  "protocol": 0,
  "read": 1,
  "status": -1,
  "type": 1,
  "body": "Hello There, I am an SMS",
  "service_center": "+60162999922",
  "locked": 0,
  "error_code": -1,
  "sub_id": -1,
  "seen": 1,
  "deletable": 0,
  "sim_slot": 0,
  "hidden": 0,
  "app_id": 0,
  "msg_id": 0,
  "reserved": 0,
  "pri": 0,
  "teleservice_id": 0,
  "svc_cmd": 0,
  "roam_pending": 0,
  "spam_report": 0,
  "secret_mode": 0,
  "safe_message": 0,
  "favorite": 0
}

*/
```

### Delete SMS Message

Delete an sms with id. If the message with the specified id does not exist it will fail with error: `SMS not found`

```javascript
import SmsAndroid from 'react-native-get-sms-android';

SmsAndroid.delete(
  _id,
  (fail) => {
    console.log('Failed with this error: ' + fail);
  },
  (success) => {
    console.log('SMS deleted successfully');
  },
);
```

### Send SMS Message (automatically)

Send an sms directly with React without user interaction.

```javascript
import SmsAndroid from 'react-native-get-sms-android';

SmsAndroid.autoSend(
  phoneNumber,
  message,
  (fail) => {
    console.log('Failed with this error: ' + fail);
  },
  (success) => {
    console.log('SMS sent successfully');
  },
);
```

#### Event listeners

An event will be thrown when the sms has been delivered. If the sms was delivered successfully the message will be "SMS delivered" otherwise the message will be "SMS not delivered"

```js
import { DeviceEventEmitter } from 'react-native';

DeviceEventEmitter.addListener('sms_onDelivery', (msg) => {
  console.log(msg);
});
```

## Note

- Does not work with Expo as it's not possible to include custom native modules beyond the React Native APIs and components that are available in the Expo client app. The information [here](https://github.com/react-community/create-react-native-app/blob/master/react-native-scripts/template/README.md#ejecting-from-create-react-native-app) might help with integrating the module while still using Expo.

## Contributions welcome!

Feel free to open an issue or a Pull Request.

## Thanks

- [react-native-android-sms](https://github.com/msmakhlouf/react-native-android-sms)
