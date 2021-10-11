void send_pop() {
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



int detect_popout(int leng,float range, PVector[] ini_pos, PVector[] blob_pos) {
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
