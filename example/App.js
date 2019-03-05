/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 * @lint-ignore-every XPLATJSCOPYRIGHT1
 */

import React, { Component } from "react";
import {
  Alert,
  Platform,
  StyleSheet,
  Text,
  View,
  ScrollView,
  TextInput,
  TouchableOpacity,
  PermissionsAndroid
} from "react-native";
import SmsAndroid from "react-native-get-sms-android";

const instructions = Platform.select({
  ios: "Press Cmd+R to reload,\n" + "Cmd+D or shake for dev menu",
  android:
    "Double tap R on your keyboard to reload,\n" +
    "Shake or press menu button for dev menu"
});

type Props = {};
export default class App extends Component<Props> {
  constructor() {
    super();
    this.state = {
      sendTo: "",
      sendBody: "",
      smsList: []
    };
  }

  async componentDidMount() {
    if (Platform.OS === "android") {
      try {
        if (!(await this.checkPermissions())) {
          await this.requestPermissions();
        }

        if (await this.checkPermissions()) {
          this.listSMS();
        }
      } catch (e) {
        console.error(e);
      }
    }
  }

  async checkPermissions() {
    console.log("checking SMS permissions");
    let hasPermissions = false;
    try {
      hasPermissions = await PermissionsAndroid.check(
        PermissionsAndroid.PERMISSIONS.READ_SMS
      );
      if (!hasPermissions) return false;
      hasPermissions = await PermissionsAndroid.check(
        PermissionsAndroid.PERMISSIONS.SEND_SMS
      );
      if (!hasPermissions) return false;
    } catch (e) {
      console.error(e);
    }
    return hasPermissions;
  }

  async requestPermissions() {
    let granted = {};
    try {
      console.log("requesting SMS permissions");
      granted = await PermissionsAndroid.requestMultiple(
        [
          PermissionsAndroid.PERMISSIONS.READ_SMS,
          PermissionsAndroid.PERMISSIONS.SEND_SMS
        ],
        {
          title: "Example App SMS Features",
          message: "Example SMS App needs access to demonstrate SMS features",
          buttonNeutral: "Ask Me Later",
          buttonNegative: "Cancel",
          buttonPositive: "OK"
        }
      );
      console.log(granted);
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log("You can use SMS features");
      } else {
        console.log("SMS permission denied");
      }
    } catch (err) {
      console.warn(err);
    }
  }

  listSMS() {
    var filter = {
      box: "inbox",
      maxCount: 10
    };

    SmsAndroid.list(
      JSON.stringify(filter),
      fail => {
        console.log("Failed with this error: " + fail);
      },
      (count, smsList) => {
        var arr = JSON.parse(smsList);
        console.log(arr);
        this.setState({ smsList: arr });
      }
    );
  }

  showSMS() {
    return this.state.smsList.map(sms => {
      return (
        <View style={{ borderColor: "#bbb", borderWidth: 1 }} key={sms._id}>
          <Text>From: {sms.address}</Text>
          <Text>Body: {sms.body}</Text>
          <Text>Id: {sms._id}</Text>
        </View>
      );
    });
  }

  sendSMS() {
    SmsAndroid.autoSend(
      this.state.sendTo,
      this.state.sendBody,
      err => {
        Alert.alert("Failed to send SMS. Check console");
        console.log("SMS SEND ERROR", err);
      },
      success => {
        Alert.alert("SMS sent successfully");
      }
    );
  }

  render() {
    // The default 'react-native init' output is used if not android platform
    if (Platform.OS !== "android") {
      return (
        <View style={styles.container}>
          <Text style={styles.welcome}>Welcome to React Native!</Text>
          <Text style={styles.instructions}>To get started, edit App.js</Text>
          <Text style={styles.instructions}>{instructions}</Text>
        </View>
      );
    }

    return (
      <View style={styles.container}>
        <View style={{ flex: 5 }}>
          <Text style={styles.welcome}>Latest Messages</Text>
          <ScrollView>{this.showSMS()}</ScrollView>
        </View>

        <View style={{ flex: 5 }}>
          <Text style={styles.welcome}>Send SMS</Text>

          <Text>To</Text>
          <TextInput
            style={{ height: 40, borderColor: "gray", borderWidth: 1 }}
            onChangeText={text => this.setState({ sendTo: text })}
            value={this.state.sendTo}
            keyboardType={"numeric"}
          />

          <Text>Message</Text>
          <TextInput
            style={{ height: 40, borderColor: "gray", borderWidth: 1 }}
            onChangeText={text => this.setState({ sendBody: text })}
            value={this.state.sendBody}
          />

          <TouchableOpacity
            onPress={() => this.sendSMS()}
            style={{ marginTop: 10, borderColor: "#bbb", borderWidth: 1 }}
          >
            <Text style={{ textAlign: "center" }}>SEND SMS</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: {
    fontSize: 20,
    textAlign: "center",
    margin: 10
  },
  instructions: {
    textAlign: "center",
    color: "#333333",
    marginBottom: 5
  }
});
