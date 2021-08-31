import 'package:flutter/material.dart';
import 'package:flutter_device_tracker/screens/register_screen.dart';
import 'package:flutter_device_tracker/database/repo.dart';

void main() => runApp(DeviceTracker());

class DeviceTracker extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    Repo().checkForExistence();
    return MaterialApp(
      title: 'Device Tracker',
      theme: ThemeData(
        primaryColor: Colors.red[900],
      ),
      home: RegisterScreen(),
    );
  }
}
