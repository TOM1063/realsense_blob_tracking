
// realsense blob 2021/09/12

//refference of rs lib >> https://github.com/cansik/realsense-processing
//refference of cv lib >> https://github.com/atduskgreg/opencv-processing
//refference of warp perspecrive >> https://github.com/atduskgreg/opencv-processing/blob/master/examples/WarpPerspective/WarpPerspective.pde#L49

//realsense lib
import ch.bildspur.realsense.*;
import ch.bildspur.realsense.type.*;
import org.intel.rs.types.Option;

//blob lib
import blobDetection.*;

//UI
import controlP5.*;

//OSC
import netP5.*;
import oscP5.*;

//cv
import gab.opencv.*;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

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



void setup(){
  //general
  size(1280, 720, P3D);
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



void draw() {
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


void drawBlobsAndEdges(boolean drawBlobs, boolean drawEdges){
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

void sendData(Blob b, int id){
  if(b != null) {
    OscMessage msg = new OscMessage("/id_" + id + "/pos/");
    msg.add(b.xMin);
    msg.add(b.yMin);
    myRemoteLocation = new NetAddress(ip_address,12000);
    oscP5.send(msg, myRemoteLocation);
    //println("pos sent");
  }
}

void saveAsCsv() {
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
