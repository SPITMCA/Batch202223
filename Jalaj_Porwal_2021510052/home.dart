import 'package:flutter/material.dart';
import 'package:car_app/car_card.dart';
import 'package:car_app/color_button.dart';
import 'package:car_app/radio_button.dart';

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  List<RadioModel> filterOptions = new List<RadioModel>();

  List<ColorModel> colorOptions = new List<ColorModel>();

  @override
  void initState() {
    super.initState();

    filterOptions.add(new RadioModel(true, 'Book', 'Book', 'assets/car1'));
    filterOptions.add(new RadioModel(false, 'For Sale', 'For Sale', 'assets/car1'));
    filterOptions.add(new RadioModel(false, 'Inventory', 'Inventory', 'assets/car2'));
    filterOptions.add(new RadioModel(false, 'Accounts', 'Accounts', 'assets/car3'));


    // Initializing color
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      body: Stack(
        children: <Widget>[
          new Container(
              width: 375,
              height: 812,
              child: SafeArea(
                child: Padding(
                  padding: const EdgeInsets.only(left: 10.0, top:10),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: <Widget>[
                      new Text("Apna Garage",
                          style: TextStyle(
                            fontFamily: 'Roboto',
                            color: Color(0xffffffff),
                            fontSize: 24,
                            fontWeight: FontWeight.w500,
                            fontStyle: FontStyle.normal,
                            letterSpacing: 0,
                          )),
                      SizedBox(
                        width: 50,
                      ),

                      // Avatar profile
                      new Container(
                          width: 36,
                          height: 36,
                          decoration: new BoxDecoration(
                            image: DecorationImage(
                                image: AssetImage('assets/image.png')),
                            boxShadow: [
                              BoxShadow(
                                  color: Color(0x28000000),
                                  offset: Offset(0, 2),
                                  blurRadius: 7,
                                  spreadRadius: 0)
                            ],
                          ))
                    ],
                  ),
                ),
              ),
              decoration: new BoxDecoration(
                  borderRadius: BorderRadius.circular(31),
                  gradient: LinearGradient(
                      colors: [Color(0xff3c80f7), Color(0xff1058d1)],
                      stops: [0, 1]))),
          Positioned(
            top: 100,
            child: new Container(
                width: 375,
                height: 729,
                child: Column(
                  children: <Widget>[
                    Padding(
                      padding: const EdgeInsets.only(top: 10.0),
                      child: new Container(
                          width: 343,
                          height: 44,
                          child: Padding(
                            padding: const EdgeInsets.all(7.0),
                            child: TextField(
                              decoration: InputDecoration(
                                  contentPadding: EdgeInsets.all(70),
                                  enabledBorder: InputBorder.none,
                                  labelText: 'get your car details here',
                                  labelStyle: TextStyle(
                                    fontFamily: 'AlegreyaSansSC',
                                    fontSize: 18,
                                    fontWeight: FontWeight.w400,
                                    fontStyle: FontStyle.normal,
                                    letterSpacing: 0,
                                  ),
                                  suffix: FlatButton(
                                      onPressed: () {},
                                      child: Icon(Icons.filter_list))),
                            ),
                          ),
                          decoration: new BoxDecoration(
                            color: Color(0xffffffff),
                            borderRadius: BorderRadius.circular(15),
                            boxShadow: [
                              BoxShadow(
                                  color: Color(0x16000000),
                                  offset: Offset(0, 2),
                                  blurRadius: 4,
                                  spreadRadius: 0)
                            ],
                          )),
                    ),

                    SizedBox(
                      height: 7,
                    ),
                    // Options
                    Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: filterOptions.map((document) {
                          return new InkWell(
                              onTap: () {
                                setState(() {
                                  filterOptions.forEach(
                                          (element) => element.isSelected = false);
                                  document.isSelected = true;
                                });
                              },
                              child: new RadioItem(document));
                        }).toList()),

                    // Color Options
                    Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: colorOptions.map((document) {
                          return new InkWell(
                              onTap: () {
                                setState(() {
                                  colorOptions.forEach(
                                          (element) => element.isSelected = false);
                                  document.isSelected = true;
                                });
                              },
                              child: new ColorItem(document));
                        }).toList()),

                    SizedBox(
                      height: 0,
                    ),

                    Flexible(
                      child: ListView(
                        scrollDirection: Axis.vertical,
                        children: <Widget>[
                          CarCard(
                            'Maruti Swift',
                            'Rs.5.92 - 8.85 Lakh*',
                            'assets/carlist1.png',
                            'assets/background_right.png',
                            true,
                          ),
                          CarCard(
                            'Maruti Baleno',
                            'Rs.6.42 - 9.60 Lakh*',
                            'assets/carlist2.png',
                            'assets/background_left.png',
                            false,
                          ),
                          CarCard(
                            'Maruti IGNIS',
                            'Rs.5.17 - 7.70 Lakh*',
                            'assets/carlist3.png',
                            'assets/background_right.png',
                            true,
                          ),
                          CarCard(
                            'Maruti Wagon R',
                            'Rs.5.47 - 7.19 Lakh*',
                            'assets/carlist4.png',
                            'assets/background_left.png',
                            false,
                          )
                        ],
                      ),
                    )
                  ],
                ),
                decoration: new BoxDecoration(
                    color: Color(0xffeff5ff),
                    borderRadius: BorderRadius.circular(32))),
          )
        ],
      ),
      floatingActionButton: new Container(
          width: 52,
          height: 52,
          child: Center(
            child: Image.asset('assets/phone.png'),
          ),
          decoration: new BoxDecoration(
              color: Color(0xffffffff),
              borderRadius: BorderRadius.circular(9.846153846153847))),
    );
  }
}


