import 'dart:io';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_device_tracker/database/repo.dart';
import 'package:flutter_device_tracker/screens/testdevice_info_recyclerview.dart';

class RegisterScreen extends StatefulWidget {
  @override
  _RegisterScreenState createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  Stream<QuerySnapshot> _devicesStream;

  @override
  void initState() {
    super.initState();
    _devicesStream = Repo().getTestDevicesStreamNoFilter();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: StreamBuilder(
          stream: _devicesStream,
          builder: (context, snapshot) {
            switch (snapshot.connectionState) {
              case ConnectionState.waiting:
                return _registeringDevice();

              case ConnectionState.active:
                return _registeredDevice(context);

              default:
                return _registeringDeviceNoConnection();
            }
          }),
    );
  }

  Future navigateToDeviceInfoScreen(context) async {
    Navigator.push(
        context, MaterialPageRoute(builder: (context) => DeviceInfoScreen()));
  }

  Widget _registeringDevice() {
    return Stack(children: <Widget>[
      Container(
        alignment: Alignment.topCenter,
        margin: EdgeInsets.symmetric(vertical: 250.0),
        child: Text(
          'Registering this device',
          style: TextStyle(
              fontFamily: Platform.isIOS ? 'courier' : 'monospace',
              fontSize: 20.0,
              fontWeight: FontWeight.normal),
          textAlign: TextAlign.center,
        ),
      ),
      Container(
        alignment: Alignment.center,
        child: CircularProgressIndicator(
            valueColor: AlwaysStoppedAnimation<Color>(Colors.red[900])),
      ),
    ]);
  }

  Widget _registeringDeviceNoConnection() {
    return Stack(children: <Widget>[
      Container(
        alignment: Alignment.topCenter,
        margin: EdgeInsets.symmetric(vertical: 250.0),
        child: Text(
          'Registering this device',
          style: TextStyle(
              fontFamily: Platform.isIOS ? 'courier' : 'monospace',
              fontSize: 20.0,
              fontWeight: FontWeight.normal),
          textAlign: TextAlign.center,
        ),
      ),
      Container(
        alignment: Alignment.center,
        child: CircularProgressIndicator(
            valueColor: AlwaysStoppedAnimation<Color>(Colors.red[900])),
      ),
      Container(
        alignment: Alignment.bottomCenter,
        margin: EdgeInsets.symmetric(vertical: 80.0),
        child: Text(
          'No internet connection',
          style: TextStyle(
              fontFamily: Platform.isIOS ? 'courier' : 'monospace',
              fontSize: 15.0,
              fontWeight: FontWeight.normal),
          textAlign: TextAlign.center,
        ),
      ),
    ]);
  }

  Widget _registeredDevice(context) {
    return Stack(
      children: <Widget>[
        Container(
          alignment: Alignment.topCenter,
          margin: EdgeInsets.symmetric(vertical: 250.0),
          child: Text(
            'Device has been registered',
            style: TextStyle(
                fontFamily: Platform.isIOS ? 'courier' : 'monospace',
                fontSize: 20.0,
                fontWeight: FontWeight.normal),
            textAlign: TextAlign.center,
          ),
        ),
        Container(
          alignment: Alignment.bottomRight,
          padding: EdgeInsets.symmetric(vertical: 100.0, horizontal: 50.0),
          child: ButtonTheme(
            minWidth: 100.0,
            child: RaisedButton(
              child: new Text(
                'OK',
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                ),
              ),
              color: Colors.red[900],
              textColor: Colors.white,
              elevation: 5.0,
              onPressed: () {
                navigateToDeviceInfoScreen(context);
              },
              padding: EdgeInsets.all(5.0),
            ),
          ),
        ),
      ],
    );
  }
}
