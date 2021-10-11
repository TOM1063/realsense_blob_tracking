void send_pop_setup() {
   send_blob_num = send_blob_num_ui;
   //pop_set_pos
}


void send_pop() {
  background(0);
  int blob_num = theBlobDetection .getBlobNb();
  float border  = 50;
  Blob b;
  if(send_blob_num > blob_num) {
    println("NOT ENOUGH");
   return;
  }
  else {
    PVector[] blobs_pos = new PVector[send_blob_num];
    println("send" + send_blob_num + "blob" + blob_num);
    for(int i = 0; i < send_blob_num; i ++) {
      b=theBlobDetection.getBlob(i);
      if (b!=null){
      blobs_pos[i] = new PVector(b.xMin+ b.w/2, b.yMin+ b.h/2);
      //send blob positions
      OscMessage msg = new OscMessage("/id_" + i + "/pos/");
      msg.add(b.xMin + b.w/2);
      msg.add(b.yMin + b.h/2);
      myRemoteLocation = new NetAddress(ip_address,12000);
      oscP5.send(msg, myRemoteLocation);
      //println("pos sent");
      fill(255);
      noStroke();
      ellipse((b.xMin + b.w/2)*width,(b.yMin + b.h/2)*height,50,50);
      noFill();
      stroke(255,0,0); //draw border box
      strokeWeight(1);
      rect(set_pos[i].x - border/2,set_pos[i].y - border/2,border,border);
      text(str(i),set_pos[i].x,set_pos[i].y - border - 10);
    }
  }
  int result = detect_popout(send_blob_num,border,set_pos,blobs_pos);
  //println(result);
  text("pop_detection_result = " + result, 20, height/2  + 60 );

  //send result
  OscMessage msg = new OscMessage("/pop_detection/");
    msg.add(result);
    myRemoteLocation = new NetAddress(ip_address,12000);
  oscP5.send(msg, myRemoteLocation);
  }
}


int detect_popout(int leng,float range, PVector[] ini_pos, PVector[] blob_pos) {
  boolean[] state = new boolean[leng];
  int set_num = 0;
  int pop_blob = 0;
  for (int i = 0; i < leng; i++) {
    PVector ini  = ini_pos[i];
    for(int j = 0; j < leng; j++) {
      PVector blob  = blob_pos[j];
      boolean x_set = (ini.x- range < blob.x * width) && (ini.x+ range > blob.x * width);
      boolean y_set = (ini.y- range < blob.y * height) && (ini.y + range > blob.y * height);
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
  text("pop_num = " + pop_num + " leng = " + leng + " set_num = " + set_num, 20, height/2  + 80 );
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


void show_debug() {
  float size_y = 200;
  float size_x = width;
  rect(0,height - size_y,size_x,size_y) ;

}
