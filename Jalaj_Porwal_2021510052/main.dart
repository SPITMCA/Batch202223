import 'package:car_app/home.dart';
import 'package:car_app/splash.dart';
import 'package:flutter/material.dart';
import 'package:flutter/animation.dart';
import 'package:animated_splash_screen/animated_splash_screen.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Garage Management System App',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: AnimatedSplashScreen(
        splash: 'assets/image.png',
        duration: 2000,
        splashTransition: SplashTransition.rotationTransition,
        backgroundColor: Colors.white,
        nextScreen: MyHomePage(title: 'Apna garage'),
      ),
    );
  }
}

