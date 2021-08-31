import 'package:cloud_firestore/cloud_firestore.dart';
import 'dart:async';
import 'dart:convert';

import 'dart:io';
import 'package:device_info/device_info.dart';
import 'package:flutter_device_tracker/model/device.dart';

class Repo {
  final CollectionReference deviceCollection =
      Firestore.instance.collection('testDevice');

  final CollectionReference userCollection =
      Firestore.instance.collection('users');

  DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();

  Future<String> _getDeviceID() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosDeviceInfo = await deviceInfo.iosInfo;
      return iosDeviceInfo.identifierForVendor;
    } else {
      AndroidDeviceInfo androidDeviceInfo = await deviceInfo.androidInfo;
      return androidDeviceInfo.androidId;
    }
  }

  Future<String> _getDeviceVersion() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosDeviceInfo = await deviceInfo.iosInfo;
      return 'iOS ' + iosDeviceInfo.systemVersion;
    } else {
      AndroidDeviceInfo androidDeviceInfo = await deviceInfo.androidInfo;
      return 'Android ' + androidDeviceInfo.version.release;
    }
  }

  Future<String> _getDeviceManufacturer() async {
    if (Platform.isIOS) {
      return 'apple';
    } else {
      AndroidDeviceInfo androidDeviceInfo = await deviceInfo.androidInfo;
      return androidDeviceInfo.manufacturer;
    }
  }

  Future<String> _getDeviceName() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosDeviceInfo = await deviceInfo.iosInfo;
      return iosDeviceInfo.name;
    } else {
      return 'unknown android Name';
    }
  }

  void _writeDataToFirebase() async {
    deviceCollection.add({
      'deviceID': await _getDeviceID(),
      'name': await _getDeviceName(),
      'manufacturer': await _getDeviceManufacturer(),
      'version': await _getDeviceVersion(),
      'holder': null,
      'lastUpdated': 0,
      'batteryPercentage': 0,
      'batteryStatus': '',
      'date': '',
    }).then((value) => print(value.documentID));
  }

  void checkForExistence() async {
    final QuerySnapshot result = await deviceCollection
        .where('deviceID', isEqualTo: await _getDeviceID())
        .limit(1)
        .getDocuments();

    final List<DocumentSnapshot> documents = result.documents;

    if (documents.length == 1) {
      print('Device already exists');
    } else {
      _writeDataToFirebase();
    }
  }

  Stream<QuerySnapshot> getTestDevicesStream(
      String nameFilter, String osVersionFilter) {
    Stream<QuerySnapshot> stream;

    if (nameFilter != null) {
      stream =
          deviceCollection.where('name', isEqualTo: nameFilter).snapshots();
    } else if (osVersionFilter != null) {
      stream = deviceCollection
          .where('version', isEqualTo: osVersionFilter)
          .snapshots();
    } else if (nameFilter != null && osVersionFilter != null) {
      stream = deviceCollection
          .where('name', isEqualTo: nameFilter)
          .where('version', isEqualTo: osVersionFilter)
          .snapshots();
    } else if (nameFilter == null && osVersionFilter == null) {
      stream = deviceCollection.snapshots();
    } else {
      stream = null;
    }
    return stream;
  }

  Stream<QuerySnapshot> getTestDevicesStreamNoFilter() {
    return deviceCollection.snapshots();
  }

  Stream<QuerySnapshot> getUsersStream() {
    return userCollection.snapshots();
  }

  Future<Device> getTestDeviceInHand() async {
    Device deviceInMyHand = Device();

    final QuerySnapshot result = await deviceCollection
        .limit(1)
        .where('deviceID', isEqualTo: await _getDeviceID())
        .getDocuments();

    final List<DocumentSnapshot> documents = result.documents;

    documents.forEach((DocumentSnapshot nonJsonDocument) {
      Map<String, dynamic> document =
          jsonDecode(jsonEncode(nonJsonDocument.data));
      deviceInMyHand = Device.fromJson(document);
    });
    return deviceInMyHand;
  }

  Future<List<String>> getDeviceNames() async {
    List<String> deviceNames = List<String>();
    deviceNames.add('All');

    final QuerySnapshot result = await deviceCollection.getDocuments();

    final List<DocumentSnapshot> documents = result.documents;

    for (DocumentSnapshot document in documents) {
      String name = document.data['name'];

      if (!deviceNames.contains(name)) {
        deviceNames.add(name);
      }
    }

    deviceNames.sort();
    return deviceNames;
  }

  Future<List<String>> getDeviceVersions() async {
    List<String> deviceVersions = List<String>();
    deviceVersions.add('All');

    final QuerySnapshot result = await deviceCollection.getDocuments();

    final List<DocumentSnapshot> documents = result.documents;

    for (DocumentSnapshot document in documents) {
      String osVersion = document.data['version'];

      if (!deviceVersions.contains(osVersion)) {
        deviceVersions.add(osVersion);
      }
    }
    deviceVersions.sort();
    return deviceVersions;
  }

  Future rentDevice(Device testDevice, String renter) async {
    final QuerySnapshot result = await deviceCollection
        .where('deviceID', isEqualTo: testDevice.deviceID)
        .limit(1)
        .getDocuments();

    final List<DocumentSnapshot> documents = result.documents;

    for (DocumentSnapshot document in documents) {
      String documentID = document.documentID;
      deviceCollection.document(documentID).updateData({'holder': renter});
    }
  }

  Future returnDevice(Device testDevice) async {
    final QuerySnapshot result = await deviceCollection
        .where('deviceID', isEqualTo: testDevice.deviceID)
        .limit(1)
        .getDocuments();

    final List<DocumentSnapshot> documents = result.documents;

    for (DocumentSnapshot document in documents) {
      String documentID = document.documentID;
      deviceCollection.document(documentID).updateData({'holder': null});
    }
  }

  Future updateBatterStatus(Device testDevice, String batteryStatus,
      int batteryPercentage, String date, int timestamp) async {
    final QuerySnapshot result = await deviceCollection
        .where('deviceID', isEqualTo: testDevice.deviceID)
        .limit(1)
        .getDocuments();

    final List<DocumentSnapshot> documents = result.documents;

    for (DocumentSnapshot document in documents) {
      String documentID = document.documentID;
      deviceCollection.document(documentID).updateData({
        'batteryStatus': batteryStatus,
        'batteryPercentage': batteryPercentage,
        'date': date,
        'lastUpdated': timestamp
      });
    }
  }
}
