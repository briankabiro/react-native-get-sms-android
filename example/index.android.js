/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  ScrollView,
  TextInput,
  Alert
} from 'react-native';
import SmsAndroid from 'react-native-get-sms-android';

export default class example extends Component {

  constructor() {
    super();
    this.state = {
      sendTo: '',
      sendBody: '',
      smsList: []
    };

    this.listSMS();
  }

  listSMS() {
    var filter = {
      box: 'inbox',
      maxCount: 10,
    };

    SmsAndroid.list(JSON.stringify(filter), (fail) => {
      console.log("Failed with this error: " + fail)
    }, (count, smsList) => {
      var arr = JSON.parse(smsList);
      console.log(arr);
      this.setState({ smsList: arr });
    });
  }

  deleteSMS(id) {
    console.log(id);
    SmsAndroid.delete(id, (err) => {
      Alert.alert("Failed to deleted SMS. Check console");
      console.log("SMS DELETE ERROR", err);
    }, (success) => {
      Alert.alert("SMS deleted successfully");
      this.listSMS();
    });
  }

  showSMS() {
    return this.state.smsList.map(sms => {
      return <View style={{ borderColor: '#bbb', borderWidth: 1 }} key={sms._id}>
        <Text>From: {sms.address}</Text>
        <Text>Body: {sms.body}</Text>
        <Text>Id: {sms._id}</Text>
        <TouchableOpacity onPress={() => this.deleteSMS(sms._id)}
          style={{ width: 90, margin: 5, borderColor: '#bbb', borderWidth: 1 }} >
          <Text>DELETE SMS</Text>
        </TouchableOpacity>
      </View >
    })
  }

  sendSMS() {
    SmsAndroid.autoSend(this.state.sendTo, this.state.sendBody, (err) => {
      Alert.alert("Failed to send SMS. Check console");
      console.log("SMS SEND ERROR", err);
    }, (success) => {
      Alert.alert("SMS sent successfully");
    });
  }

  render() {
    return (
      <View style={styles.container}>
        <View style={{ flex: 5 }}>
          <Text style={styles.welcome}>
            Latest Messages
        </Text>
          <ScrollView>
            {this.showSMS()}
          </ScrollView>
        </View>

        <View style={{ flex: 5 }}>
          <Text style={styles.welcome}>
            Send SMS
          </Text>

          <Text>To</Text>
          <TextInput
            style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
            onChangeText={(text) => this.setState({ sendTo: text })}
            value={this.state.sendTo}
            keyboardType={'numeric'}
          />

          <Text>Message</Text>
          <TextInput
            style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
            onChangeText={(text) => this.setState({ sendBody: text })}
            value={this.state.sendBody}
          />

          <TouchableOpacity
            onPress={() => this.sendSMS()}
            style={{ marginTop: 10, borderColor: '#bbb', borderWidth: 1 }} >
            <Text style={{ textAlign: 'center' }}>SEND SMS</Text>
          </TouchableOpacity>

        </View>

      </View >
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('example', () => example);
