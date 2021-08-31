import 'dart:async';
import 'package:battery/battery.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter_conditional_rendering/flutter_conditional_rendering.dart';

import 'package:flutter/material.dart';
import 'package:flutter_device_tracker/model/device.dart';
import 'package:flutter_device_tracker/service/device_battery_information.dart';
import 'package:flutter_device_tracker/database/repo.dart';
import 'dart:io';

class DeviceInfoScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return new DeviceInfoScreenState();
  }
}

class DeviceInfoScreenState extends State<DeviceInfoScreen> {
  Device _testDeviceInMyHand = Device();
  List<String> _deviceNames = List();
  List<String> _deviceVersions = List();
  final Battery _battery = Battery();
  String _selectedUser;
  String _nameFilter;
  String _osVersionFilter;

  Stream<QuerySnapshot> _devicesStream;
  Stream<QuerySnapshot> _userStream;

  Stream getDevicesStream() {
    return Repo().getTestDevicesStream(_nameFilter, _osVersionFilter);
  }

  Stream getUsersStream() {
    return Repo().getUsersStream();
  }

  Future<Device> getDeviceInMyHand() {
    return Repo().getTestDeviceInHand();
  }

  Future<List<String>> getDeviceNames() {
    return Repo().getDeviceNames();
  }

  Future<List<String>> getDeviceVersions() {
    return Repo().getDeviceVersions();
  }

  listenToBattery(Device testDevice) {
    _battery.onBatteryStateChanged.listen((BatteryState state) async {
      if (DeviceBatteryInfo().timeToUpdate(testDevice)) {
        String _batteryStatus = DeviceBatteryInfo().getBatteryStatus(state);
        DeviceBatteryInfo().updateBatteryInfo(testDevice, _batteryStatus);
        setState(() {});
      }
    });
  }

  @override
  void initState() {
    super.initState();
    _devicesStream = getDevicesStream();
    _userStream = getUsersStream();
  }

  @override
  Widget build(
    BuildContext context,
  ) {
    return new SafeArea(
      child: Scaffold(
        body: FutureBuilder(
          future: Future.wait(
              [getDeviceNames(), getDeviceVersions(), getDeviceInMyHand()]),
          builder: (_, futureSnapshot) {
            if (futureSnapshot.connectionState == ConnectionState.waiting) {
              return Center(
                child: Text('Loading...'),
              );
            } else {
              _deviceNames = futureSnapshot.data[0];
              _deviceVersions = futureSnapshot.data[1];
              _testDeviceInMyHand = futureSnapshot.data[2];
              return Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: <Widget>[
                      buildDropDownButtonNameFilter(context, _deviceNames),
                      buildDropDownButtonVersionFilter(
                          context, _deviceVersions),
                    ],
                  ),
                  Expanded(
                    child: _buildTestDevices(_testDeviceInMyHand),
                  ),
                ],
              );
            }
          },
        ),
        extendBodyBehindAppBar: true,
      ),
    );
  }

  Widget _buildTestDevices(Device testDeviceInMyHand) {
    DocumentSnapshot _testDeviceSnapshot;
    List<DocumentSnapshot> _listOfUserSnapshot;

    return StreamBuilder(
        stream: _devicesStream,
        builder: (context, deviceStreamSnapshot) {
          return StreamBuilder(
            stream: _userStream,
            builder: (context, userStreamSnapshot) {
              if (deviceStreamSnapshot.hasData && userStreamSnapshot.hasData) {
                return new ListView.separated(
                    separatorBuilder: (context, index) => Divider(
                          color: _testDeviceSnapshot['holder'] == null
                              ? Colors.black
                              : Colors.red[900],
                          thickness: 1.5,
                        ),
                    padding: const EdgeInsets.all(16.0),
                    itemCount: deviceStreamSnapshot.data.documents.length,
                    itemBuilder: (context, index) {
                      _testDeviceSnapshot =
                          deviceStreamSnapshot.data.documents[index];
                      _listOfUserSnapshot = userStreamSnapshot.data.documents;

                      return _buildRow(testDeviceInMyHand, _testDeviceSnapshot,
                          _listOfUserSnapshot);
                    });
              } else {
                return Center(
                  child: CircularProgressIndicator(
                    valueColor:
                        new AlwaysStoppedAnimation<Color>(Colors.red[900]),
                  ),
                );
              }
            },
          );
        });
  }

  Widget _buildRow(Device testDevice, DocumentSnapshot testDeviceSnapshot,
      List<DocumentSnapshot> listOfUserSnapshot) {
    List<String> users = List();

    for (DocumentSnapshot userSnapshot in listOfUserSnapshot) {
      users.add(userSnapshot['name']);
    }
    users.sort();

    listenToBattery(testDevice);

    return new ListTile(
      title: rowItems(testDeviceSnapshot),
      trailing: FittedBox(
        alignment: Alignment.bottomRight,
        fit: BoxFit.fill,
        child: Column(
          children: <Widget>[
            Row(
              children: [
                Text(
                  testDeviceSnapshot['batteryStatus'] == "Charging"
                      ? "Charging"
                      : "",
                  style: TextStyle(
                    fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                    fontSize: 12.0,
                    fontWeight: FontWeight.normal,
                    color: testDeviceSnapshot['holder'] == null
                        ? Colors.black
                        : Colors.red[900],
                  ),
                ),
                SizedBox(
                  width: 10,
                ),
                Text(
                  testDeviceSnapshot['batteryPercentage'].toString() + '%',
                  style: TextStyle(
                    fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                    fontSize: 12.0,
                    fontWeight: FontWeight.normal,
                    color: testDeviceSnapshot['holder'] == null
                        ? Colors.black
                        : Colors.red[900],
                  ),
                ),
                SizedBox(
                  width: 10,
                ),
                ConditionalSwitch.single<bool>(
                  context: context,
                  valueBuilder: (BuildContext context) => true,
                  caseBuilders: {
                    testDeviceSnapshot['batteryPercentage'] > 75:
                        (BuildContext context) => buildImage('images/b100.png'),
                    testDeviceSnapshot['batteryPercentage'] <= 75 &&
                            testDeviceSnapshot['batteryPercentage'] > 50:
                        (BuildContext context) => buildImage('images/b75.png'),
                    testDeviceSnapshot['batteryPercentage'] <= 50 &&
                            testDeviceSnapshot['batteryPercentage'] > 25:
                        (BuildContext context) => buildImage('images/b50.png'),
                    testDeviceSnapshot['batteryPercentage'] <= 25 &&
                            testDeviceSnapshot['batteryPercentage'] > 15:
                        (BuildContext context) => buildImage('images/b25.png'),
                    testDeviceSnapshot['batteryPercentage'] <= 15:
                        (BuildContext context) => buildImage('images/b0.png'),
                  },
                  fallbackBuilder: (BuildContext context) => Image.asset(
                    'images/b0.png',
                    height: 15,
                    width: 30,
                  ),
                ),
              ],
            ),
            SizedBox(height: 20),
            Text(
              (_testDeviceInMyHand.deviceID == testDeviceSnapshot['deviceID'])
                  ? (testDeviceSnapshot['holder'] == null) ? 'Rent' : 'Return'
                  : (testDeviceSnapshot['holder'] == null)
                      ? ""
                      : testDeviceSnapshot['holder'],
              style: TextStyle(
                fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                fontSize: 14.0,
                fontWeight: FontWeight.normal,
                color: testDeviceSnapshot['holder'] == null
                    ? Colors.black
                    : Colors.red[900],
              ),
            ),
          ],
        ),
      ),
      onTap: () {
        buildDialog(context, testDevice, testDeviceSnapshot, users);
      },
    );
  }

  Widget rowItems(DocumentSnapshot testDeviceSnapshot) {
    return new Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          testDeviceSnapshot['name'] != null
              ? testDeviceSnapshot['name']
              : 'error reading name',
          style: TextStyle(
            fontFamily: Platform.isIOS ? 'courier' : 'monospace',
            fontSize: 16.0,
            fontWeight: FontWeight.normal,
            color: testDeviceSnapshot['holder'] == null
                ? Colors.black
                : Colors.red[900],
          ),
        ),
        SizedBox(height: 20),
        Text(
          testDeviceSnapshot['version'] != null
              ? testDeviceSnapshot['version']
              : 'error reading os version',
          style: TextStyle(
            fontFamily: Platform.isIOS ? 'courier' : 'monospace',
            fontSize: 14.0,
            fontWeight: FontWeight.normal,
            color: testDeviceSnapshot['holder'] == null
                ? Colors.black
                : Colors.red[900],
          ),
        ),
      ],
    );
  }

  buildDialog(BuildContext context, Device testDevice,
      DocumentSnapshot testDeviceSnapshot, List<String> users) {
    showDialog(
        context: context,
        builder: (context) {
          return StatefulBuilder(builder: (context, setState) {
            return Dialog(
              child: Container(
                height: 320,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.fromLTRB(30.0, 20.0, 0, 0),
                      child: Text(
                        testDeviceSnapshot.data['name'],
                        style: TextStyle(
                          fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                          fontSize: 16.0,
                          fontWeight: FontWeight.normal,
                        ),
                        textAlign: TextAlign.start,
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(30.0, 20.0, 0, 0),
                      child: Text(
                        testDeviceSnapshot.data['version'],
                        style: TextStyle(
                          fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                          fontSize: 16.0,
                          fontWeight: FontWeight.normal,
                        ),
                        textAlign: TextAlign.start,
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(30.0, 20.0, 0, 0),
                      child: Text(
                        testDeviceSnapshot.data['date'],
                        style: TextStyle(
                          fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                          fontSize: 16.0,
                          fontWeight: FontWeight.normal,
                        ),
                        textAlign: TextAlign.start,
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(45.0, 40.0, 45.0, 0),
                      child: buildDropDownButtonUser(
                          context, users, testDeviceSnapshot, testDevice),
                    ),
                    SizedBox(height: 50),
                    buildButtons(context, testDevice, testDeviceSnapshot),
                  ],
                ),
              ),
            );
          });
        });
  }

  Widget buildDropDownButtonNameFilter(
      BuildContext context, List<String> deviceNames) {
    return Container(
      alignment: Alignment.center,
      height: 60,
      decoration: BoxDecoration(
          color: Colors.white60, borderRadius: BorderRadius.circular(5)),
      child: DropdownButton<String>(
        hint: Text(
          "Name",
          style: TextStyle(
            fontFamily: Platform.isIOS ? 'courier' : 'monospace',
            fontSize: 14.0,
            fontWeight: FontWeight.normal,
            color: Colors.black,
          ),
        ),
        items: deviceNames.map((String name) {
          return new DropdownMenuItem<String>(
            value: name,
            child: new Text(
              name,
              style: TextStyle(
                fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                fontSize: 14.0,
                fontWeight: FontWeight.normal,
              ),
            ),
          );
        }).toList(),
        onChanged: (String newName) {
          setState(() {
            _nameFilter = newName == 'All' ? null : newName;
            _devicesStream = getDevicesStream();
          });
        },
        value: _nameFilter,
        elevation: 16,
      ),
    );
  }

  Widget buildDropDownButtonVersionFilter(
      BuildContext context, List<String> deviceVersions) {
    return Container(
      height: 60,
      alignment: Alignment.center,
      decoration: BoxDecoration(
          color: Colors.white60, borderRadius: BorderRadius.circular(5)),
      child: DropdownButton<String>(
        hint: Text(
          "Version",
          style: TextStyle(
            fontFamily: Platform.isIOS ? 'courier' : 'monospace',
            fontSize: 14.0,
            fontWeight: FontWeight.normal,
            color: Colors.black,
          ),
        ),
        items: deviceVersions.map((String name) {
          return new DropdownMenuItem<String>(
            value: name,
            child: new Text(
              name,
              style: TextStyle(
                fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                fontSize: 14.0,
                fontWeight: FontWeight.normal,
              ),
            ),
          );
        }).toList(),
        onChanged: (String newVersion) {
          setState(() {
            _osVersionFilter = newVersion == 'All' ? null : newVersion;
            _devicesStream = getDevicesStream();
          });
        },
        value: _osVersionFilter,
        elevation: 16,
      ),
    );
  }

  Widget buildDropDownButtonUser(BuildContext context, List<String> users,
      DocumentSnapshot testDeviceSnapshot, Device testDevice) {
    return StatefulBuilder(builder: (context, setState) {
      return Visibility(
        visible: testDeviceSnapshot['deviceID'] == testDevice.deviceID
            ? testDeviceSnapshot['holder'] != null ? false : true
            : false,
        maintainSize: true,
        maintainState: true,
        maintainAnimation: true,
        child: Container(
          alignment: Alignment.bottomLeft,
          padding: const EdgeInsets.only(left: 10.0),
          decoration: BoxDecoration(
            color: Colors.grey[500],
          ),
          child: DropdownButton<String>(
            hint: Text(
              "User             ",
              style: TextStyle(
                  fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                  fontSize: 16.0,
                  fontWeight: FontWeight.normal,
                  color: Colors.black),
            ),
            items: users.map((String name) {
              return new DropdownMenuItem<String>(
                value: name,
                child: new Text(
                  name,
                  style: TextStyle(
                    fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                    fontSize: 14.0,
                    fontWeight: FontWeight.normal,
                  ),
                ),
              );
            }).toList(),
            onChanged: (String newUser) {
              setState(() {
                _selectedUser = newUser;
              });
            },
            value: _selectedUser,
            elevation: 16,
          ),
        ),
      );
    });
  }

  Widget buildButtons(BuildContext context, Device testDevice,
      DocumentSnapshot testDeviceSnapshot) {
    return StatefulBuilder(builder: (context, setState) {
      return Row(
        mainAxisAlignment: MainAxisAlignment.end,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.fromLTRB(0, 0, 20.0, 0),
            child: Visibility(
              child: ButtonTheme(
                child: FlatButton(
                  child: new Text(
                    testDevice.deviceID == testDeviceSnapshot['deviceID']
                        ? testDeviceSnapshot['holder'] == null
                            ? 'Rent'
                            : 'Return'
                        : "",
                    style: TextStyle(
                      fontWeight: FontWeight.normal,
                      fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                      fontSize: 14.0,
                    ),
                  ),
                  color: Colors.white,
                  textColor: Colors.red[900],
                  onPressed: () {
                    testDeviceSnapshot['holder'] == null
                        ? Repo().rentDevice(testDevice, _selectedUser)
                        : Repo().returnDevice(testDevice);

                    _selectedUser = null;
                    Navigator.of(context).pop();
                  },
                  padding: EdgeInsets.all(5.0),
                ),
              ),
              visible: testDevice.deviceID == testDeviceSnapshot['deviceID']
                  ? true
                  : false,
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(0, 0, 20.0, 0),
            child: ButtonTheme(
              child: FlatButton(
                child: new Text(
                  'Cancel',
                  style: TextStyle(
                    fontWeight: FontWeight.normal,
                    fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                    fontSize: 14.0,
                  ),
                ),
                color: Colors.white,
                textColor: Colors.red[900],
                onPressed: () {
                  Navigator.of(context).pop();
                },
                padding: EdgeInsets.all(5.0),
              ),
            ),
          )
        ],
      );
    });
  }

  buildImage(String image) {
    return Image.asset(
      image,
      height: 15,
      width: 30,
    );
  }
}
