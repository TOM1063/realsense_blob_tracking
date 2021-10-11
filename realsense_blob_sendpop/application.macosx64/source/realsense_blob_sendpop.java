import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ch.bildspur.realsense.*; 
import ch.bildspur.realsense.type.*; 
import org.intel.rs.types.Option; 
import blobDetection.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import gab.opencv.*; 
import org.opencv.core.Mat; 
import org.opencv.core.CvType; 
import org.opencv.core.Point; 
import org.opencv.core.Size; 
import org.opencv.core.MatOfPoint2f; 
import org.opencv.imgproc.Imgproc; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class realsense_blob_sendpop extends PApplet {


// realsense blob 2021/09/12

//refference of rs lib >> https://github.com/cansik/realsense-processing
//refference of cv lib >> https://github.com/atduskgreg/opencv-processing
//refference of warp perspecrive >> https://github.com/atduskgreg/opencv-processing/blob/master/examples/WarpPerspective/WarpPerspective.pde#L49

//realsense lib




//blob lib


//UI


//OSC



//cv








OscP5 oscP5;
NetAddress myRemoteLocation;
String ip_address;
String prev_ip_addres = "0";

RealSenseCamera camera = new RealSenseCamera(this);
PImage img, thresh, projectedImg;
int rsWidth = 640;
int rsHeight = 480;

BlobDetection theBlobDetection;
PFont font;

ControlP5 slider ,button;
float sliderValue;
int threshold;
int blurValue;

OpenCV cv;

PVector[] pickPoints = new PVector[4];

int page;



public void setup(){
  //general
  
  background(0);
  strokeWeight(10);
  page = 1;
  
  
  //UI
  setUI();
    
  //realsense camera
  camera.enableDepthStream(rsWidth, rsHeight, 30);
  camera.enableIRStream(rsWidth, rsHeight, 30);
  camera.start();
  camera.getDepthSensor().setOption(Option.EnableAutoExposure, 0.0f);
  
  //blob detection
  img = new PImage(rsWidth, rsHeight); 
  theBlobDetection = new BlobDetection(rsWidth, rsHeight);
  theBlobDetection.setPosDiscrimination(true);
  //theBlobDetection.setBlobDimensionMin(200, 100);
  font = createFont("Arial", 28);
  textFont(font, 28);
  textAlign(CENTER);
  
  //OSC
  oscP5 = new OscP5(this, 12000);
  
  //projection
  projectedImg = new PImage(rsWidth, rsHeight);
  pickPoints[0] = new PVector(rsWidth -100,0);
  pickPoints[1] = new PVector(100,0);
  pickPoints[2] = new PVector(0,rsHeight);
  pickPoints[3] = new PVector(rsWidth,rsHeight);
  
  //csv
  csvSetup();
  
  
}



public void draw() {
  background(0);
  
  //realsense
  camera.readFrames();
  img = camera.getIRImage();
  
  //blur
  fastblur(img,blurValue);
  
  //cv process
  cv = new OpenCV(this,img);
  //projection
  cv.toPImage(warpPerspective(pickPoints, 640, 480), projectedImg);
  //thresh
  cv = new OpenCV(this,projectedImg);
  cv.threshold(threshold);
  thresh = cv.getOutput();
  
  //blob
  theBlobDetection.setThreshold(sliderValue); 
  theBlobDetection.computeBlobs(thresh.pixels);
  int blob_num = theBlobDetection.getBlobNb();
  
  if(page ==1) {
    image(img, 0, 0,width,height);
    for(int i = 0; i < 4; i ++) {
      noFill();
      stroke(255,0,0);
      float posx = width * pickPoints[i].x/rsWidth;
      float posy = height * pickPoints[i].y/rsHeight;
      int prevID = i - 1;
      if(prevID == -1) {
        prevID = 3;
      }
      float prev_posx = width * pickPoints[prevID].x/rsWidth;
      float prev_posy = height * pickPoints[prevID].y/rsHeight;
      ellipse(posx,posy,20,20);
      stroke(255,255,0);
      line(posx,posy,prev_posx, prev_posy);
    }
  }
  else if(page == 2) {
    image(thresh, 0, 0,width,height);
    drawBlobsAndEdges(true,true); 
  }
  else if(page == 3) {
     image(thresh, 0, 0,width,height);
     send_pop();
  }
  
  //debug
  fill(255);
  textSize(10);
  textAlign(LEFT);
  text("blob_num = " + str(blob_num),20,height/2);
  fill(255,255,0);
  text("fps = " + frameRate,20,height/2  +40);
  
  //IP update
  if(prev_ip_addres != ip_address) {
    myRemoteLocation = new NetAddress(ip_address,12000);
    prev_ip_addres = ip_address;
    println("ip_was_set_to:" + ip_address);
  }
  
  //saveCSV
  
}


public void drawBlobsAndEdges(boolean drawBlobs, boolean drawEdges){
  noFill();
  Blob b;    //オブジェクト用の変数
  EdgeVertex eA,eB;    //エッジの点
 
  //nはオブジェクトの数
  for (int n=0; n<theBlobDetection .getBlobNb(); n++){
    b=theBlobDetection.getBlob(n);
    if (b!=null){  //もしオブジェクトがあったら、
      // Edges
      //if (drawEdges){
      //  strokeWeight(3);
      //  stroke(0,0,255);
      //  for (int m=0;m<b.getEdgeNb();m++){
      //    eA = b.getEdgeVertexA(m);  //エッジの点をそれぞれeA,eBに代入
      //    eB = b.getEdgeVertexB(m);
      //    if (eA !=null && eB !=null)
      //      //線を描く
      //      line(eA.x*width, eA.y*height, eB.x*width, eB.y*height);
      //  }
      //}
 
      // Blobs
      if (drawBlobs){
        noFill();
        strokeWeight(1);
        stroke(255);
        //オブジェクトを囲む矩形を描く
        rect(b.xMin*width, b.yMin*height, b.w*width, b.h*height);
        fill(0,255, 0);
        //オブジェクト番号を表示
        textSize(20);
        text(n, b.xMin*width + b.w*width/2, b.yMin*height+ b.h*height/2);
      }
    }
    //send osc
    sendData(b, n);
  }
}

public void sendData(Blob b, int id){
  if(b != null) {
    OscMessage msg = new OscMessage("/id_" + id + "/pos/");
    msg.add(b.xMin);
    msg.add(b.yMin);
    myRemoteLocation = new NetAddress(ip_address,12000);
    oscP5.send(msg, myRemoteLocation);
    //println("pos sent");
  }
}

public void saveAsCsv() {
  PVector[] points = pickPoints;
  file = createWriter("pickPoints.csv");
  for (int i = 0; i < 4; i ++) {
    float x = points[i].x;
    float y = points[i].y;
    file.print(x);
    file.print(",");
    file.println(y);
  }
 file.flush();
 file.close();
}
public void setUI() {
  slider = new ControlP5(this);
  int myColor = color(255);
  slider.addSlider("sliderValue")
  //.setLabel("bbb")
  .setRange(0, 1)//0~100の間
  .setValue(0.26f)//初期値
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
PrintWriter file; 

public void csvSetup() {
  //file = createWriter("pickPoints.csv");
}

//void saveAsCsv(PVector[] points) {
//  file = createWriter("pickPoints.csv");
//  for (int i = 0; i < 4; i ++) {
//    float x = points[i].x;
//    float y = points[i].y;
//    file.print(x);
//    file.print(",");
//    file.println(y);
//  }
// file.flush();
// file.close();
//}




public void loadCsv() {
  String lines[] = loadStrings("pickPoints.csv");
  String [][] csv;
  int csvWidth=0;

//calculate max width of csv file
  for (int i=0; i < lines.length; i++) {
    String [] chars=split(lines[i],',');
    if (chars.length>csvWidth){
      csvWidth=chars.length;
    }
  }

//create csv array based on # of rows and columns in csv file
  csv = new String [lines.length][csvWidth];

//parse values into 2d array
  for (int i=0; i < lines.length; i++) {
    String [] temp = new String [lines.length];
    temp= split(lines[i], ',');
    for (int j=0; j < temp.length; j++){
     csv[i][j]=temp[j];
    }
  }
  
  for(int i = 0; i <4; i++) {
    pickPoints[i].x = PApplet.parseFloat(csv[i][0]);
    pickPoints[i].y = PApplet.parseFloat(csv[i][1]);
  }
}
public void fastblur(PImage img,int radius)
{
 if (radius<1){
    return;
  }
  int w=img.width;
  int h=img.height;
  int wm=w-1;
  int hm=h-1;
  int wh=w*h;
  int div=radius+radius+1;
  int r[]=new int[wh];
  int g[]=new int[wh];
  int b[]=new int[wh];
  int rsum,gsum,bsum,x,y,i,p,p1,p2,yp,yi,yw;
  int vmin[] = new int[max(w,h)];
  int vmax[] = new int[max(w,h)];
  int[] pix=img.pixels;
  int dv[]=new int[256*div];
  for (i=0;i<256*div;i++){
    dv[i]=(i/div);
  }

  yw=yi=0;

  for (y=0;y<h;y++){
    rsum=gsum=bsum=0;
    for(i=-radius;i<=radius;i++){
      p=pix[yi+min(wm,max(i,0))];
      rsum+=(p & 0xff0000)>>16;
      gsum+=(p & 0x00ff00)>>8;
      bsum+= p & 0x0000ff;
    }
    for (x=0;x<w;x++){

      r[yi]=dv[rsum];
      g[yi]=dv[gsum];
      b[yi]=dv[bsum];

      if(y==0){
        vmin[x]=min(x+radius+1,wm);
        vmax[x]=max(x-radius,0);
      }
      p1=pix[yw+vmin[x]];
      p2=pix[yw+vmax[x]];

      rsum+=((p1 & 0xff0000)-(p2 & 0xff0000))>>16;
      gsum+=((p1 & 0x00ff00)-(p2 & 0x00ff00))>>8;
      bsum+= (p1 & 0x0000ff)-(p2 & 0x0000ff);
      yi++;
    }
    yw+=w;
  }

  for (x=0;x<w;x++){
    rsum=gsum=bsum=0;
    yp=-radius*w;
    for(i=-radius;i<=radius;i++){
      yi=max(0,yp)+x;
      rsum+=r[yi];
      gsum+=g[yi];
      bsum+=b[yi];
      yp+=w;
    }
    yi=x;
    for (y=0;y<h;y++){
      pix[yi]=0xff000000 | (dv[rsum]<<16) | (dv[gsum]<<8) | dv[bsum];
      if(x==0){
        vmin[y]=min(y+radius+1,hm)*w;
        vmax[y]=max(y-radius,0)*w;
      }
      p1=x+vmin[y];
      p2=x+vmax[y];

      rsum+=r[p1]-r[p2];
      gsum+=g[p1]-g[p2];
      bsum+=b[p1]-b[p2];

      yi+=w;
    }
  }

}
public void keyPressed() {
  if (key == '1') {
    page  = 1;
  }
  else if(key == '2') {
    page = 2;
  }
  else if(key == '3') {
    page = 3;
  }
    
}

public void mouseDragged(){
  float range = 50;
  for(int i = 0; i < 4; i ++) {
    float converted_x = width * pickPoints[i].x/rsWidth;
    float converted_y = height * pickPoints[i].y/rsHeight;
    if(mouseX > converted_x - range && mouseX < converted_x  + range && mouseY > converted_y - range && mouseY < converted_y + range){
      pickPoints[i].x = rsWidth * mouseX / width;
      pickPoints[i].y = rsHeight * mouseY / height;
    }
  }
}
public void send_pop() {
  background(0);
  int send_blob_num = 4;
  int blob_num = theBlobDetection .getBlobNb();
  float border  = 50;
  Blob b;
  PVector[] blobs_pos = new PVector[4];
  PVector[] set_pos = new PVector[4];
  set_pos[0] = new PVector(width/2 -200,height/2);
  set_pos[1] = new PVector(width/2 - 400,height/2);
  set_pos[2] = new PVector(width/2+200,height/2);
  set_pos[3] = new PVector(width/2  +400,height/2);
  blobs_pos[0] = new PVector(width/2 -200,height/2);
  blobs_pos[1] = new PVector(width/2 - 400,height/2);
  blobs_pos[2] = new PVector(width/2+200,height/2);
  blobs_pos[3] = new PVector(width/2  +400,height/2);
  if(send_blob_num > blob_num) {
    println("NOT ENOUGH");
   return;
  }
  else{
  for(int i = 0; i < send_blob_num; i ++) {
    b=theBlobDetection.getBlob(i);
    if (b!=null){
    blobs_pos[i].x = b.xMin;
    blobs_pos[i].y = b.yMin;
    //send blob positions
    OscMessage msg = new OscMessage("/id_" + i + "/pos/");
    msg.add(b.xMin);
    msg.add(b.yMin);
    myRemoteLocation = new NetAddress(ip_address,12000);
    oscP5.send(msg, myRemoteLocation);
    //println("pos sent");
    fill(255);
    noStroke();
    ellipse(b.xMin*width, height -b.yMin*height,border,border);
    noFill();
    stroke(255);
    ellipse(set_pos[i].x,set_pos[i].y,100,100);
    text(str(i),set_pos[i].x,set_pos[i].y - border - 10);
    }
  }
  int result = detect_popout(send_blob_num,border,set_pos,blobs_pos);
  println(result);
  println(frameRate);
  //send result
  OscMessage msg = new OscMessage("/pop_detection/");
    msg.add(result);
    myRemoteLocation = new NetAddress(ip_address,12000);
  oscP5.send(msg, myRemoteLocation);
  }
}



public int detect_popout(int leng,float range, PVector[] ini_pos, PVector[] blob_pos) {
  boolean[] state = new boolean[leng];
  int set_num = 0;
  int pop_blob = 0;
  for (int i = 0; i < leng; i++) {
    PVector ini  = ini_pos[i];
    for(int j = 0; j < leng; j++) {
      PVector blob  = blob_pos[j];
      boolean x_set = (ini.x - range < blob.x) && (ini.x + range > blob.x);
      boolean y_set = (ini.y - range < blob.y) && (ini.y + range > blob.y);
      boolean set = x_set && y_set;
      if(set) {
        state[i] = true;
        set_num += 1;
        break;
      }
      else {
        state[i] = false;
        if(j == leng -1) {
          pop_blob = i;
        }
      }
    }
  }
  int pop_num = leng - set_num;
  if(pop_num  == 0) {
    return 1000;
  }
  else if(pop_num == 1) {
    return  pop_blob;
  }
  else{
    return 2000;
  }
}


public Mat warpPerspective(PVector[] inputPoints, int w, int h) {
  Mat transform = getPerspectiveTransformation(inputPoints, w, h);
  Mat unWarpedMarker = new Mat(w, h, CvType.CV_8UC1);    
  Imgproc.warpPerspective(cv.getColor(), unWarpedMarker, transform, new Size(w, h));
  return unWarpedMarker;
}

public Mat getPerspectiveTransformation(PVector[]inputPoints, int w, int h) {
  Point[] canonicalPoints = new Point[4];
  canonicalPoints[0] = new Point(w, 0);
  canonicalPoints[1] = new Point(0, 0);
  canonicalPoints[2] = new Point(0, h);
  canonicalPoints[3] = new Point(w, h);

  MatOfPoint2f canonicalMarker = new MatOfPoint2f();
  canonicalMarker.fromArray(canonicalPoints);

  Point[] points = new Point[4];
  for (int i = 0; i < 4; i++) {
    points[i] = new Point(inputPoints[i].x, inputPoints[i].y);
  }
  MatOfPoint2f marker = new MatOfPoint2f(points);
  return Imgproc.getPerspectiveTransform(marker, canonicalMarker);
}
  public void settings() {  size(1280, 720, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "realsense_blob_sendpop" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
