
# react-native-get-sms-android

Module that gets all the messages from android

> Decided to start this package because *react-native-android-sms* isn't maintained. It doesn't work with newer React native versions
----

## Getting started

#### Yarn

`$ yarn add react-native-get-sms-android`

#### Npm

`$ npm install react-native-get-sms-android --save`

### Mostly automatic installation

`$ react-native link react-native-get-sms-android`

#### Manual installation
*android/settings.gradle*
    
    include ':react-native-get-sms-android'
    project('react-native-get-sms-android').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-get-sms-android/android')

*android/app/build.gradle*
       
    dependencies{
        compile project(':react-native-get-sms-android')
     }

*MainApplication.java*

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
Add permissions to your `android/app/src/main/AndroidManifest.xml` file.

```xml
...
  <uses-permission android:name="android.permission.READ_SMS" />
  <uses-permission android:name="android.permission.WRITE_SMS" />
...
```




## Usage

### List SMS Messages

```javascript
import SmsAndroid  from 'react-native-get-sms-android';

/* List SMS messages matching the filter */
var filter = {
    box: 'inbox', // 'inbox' (default), 'sent', 'draft', 'outbox', 'failed', 'queued', and '' for all
    // the next 4 filters should NOT be used together, they are OR-ed so pick one
    read: 0, // 0 for unread SMS, 1 for SMS already read
    _id: 1234, // specify the msg id
    address: '+1888------', // sender's phone number
    body: 'How are you', // content to match
    // the next 2 filters can be used for pagination
    indexFrom: 0, // start from index 0
    maxCount: 10, // count of SMS to return each time
};

SmsAndroid.list(JSON.stringify(filter), (fail) => {
        console.log("Failed with this error: " + fail)
    },
    (count, smsList) => {
        console.log('Count: ', count);
        console.log('List: ', smsList);
        var arr = JSON.parse(smsList);

        arr.forEach(function(index){
          var obj = arr[index];
            console.log("Index: " + index);
            console.log("-->" + obj.date);
            console.log("-->" + obj.body);
        })
    });

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
Delete an sms with id. If the message with the specified id does not exist it will return success

```javascript
import SmsAndroid  from 'react-native-get-sms-android';

SmsAndroid.delete(_id, (fail) => {
    console.log("Failed with this error: " + fail)
}, (success) => {
    console.log("SMS deleted successfully");
});
```

## thanks
* react-native-android-sms
