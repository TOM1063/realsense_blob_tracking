void setUI() {
  slider = new ControlP5(this);
  color myColor = color(255);
  slider.addSlider("sliderValue")
  //.setLabel("bbb")
  .setRange(0, 1)//0~100の間
  .setValue(0.26)//初期値
  .setPosition(20, 40)//位置
  .setSize(200, 20)//大きさ
  .setNumberOfTickMarks(20);//Rangeを(引数の数-1)で割った値が1メモリの値
  
  slider.addSlider("threshold")
  //.setLabel("bbb")
  .setRange(0, 255)//0~100の間
  .setValue(36)//初期値
  .setPosition(20, 80)//位置
  .setSize(200, 20);//大きさ
  //.setNumberOfTickMarks(20);//Rangeを(引数の数-1)で割った値が1メモリの値
  
  slider.addSlider("blurValue")
  //.setLabel("bbb")
  .setRange(0, 100)//0~100の間
  .setValue(6)//初期値
  .setPosition(20, 120)//位置
  .setSize(200, 20);//大きさ
  //.setNumberOfTickMarks(20);//Rangeを(引数の数-1)で割った値が1メモリの値
  
   slider.addTextfield("ip_address")
   .setPosition(20,160)
   .setSize(200,20)
   .setFocus(true)
   .setColor(myColor);
   
   
  button = new ControlP5(this);
  
  button.addButton("loadCsv")
    .setLabel("LOAD_CSV")//テキスト
    .setPosition(20, 200)
    .setSize(60, 20);
  button.addButton("saveAsCsv")
    .setLabel("SAVE_AS_CSV")//テキスト
    .setPosition(100, 200)
    .setSize(80, 20);
  
}
