void keyPressed() {
  if (key == '1') {
    page  = 1;
  }
  else if(key == '2') {
    page = 2;
  }
  else if(key == '3') {
    page = 3;
    send_pop_setup();
  }
    
}

void mouseDragged(){
if(page == 1) {
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
  if(page == 3) {
    println(send_blob_num);
    float range = 10;
    for(int i = 0; i < send_blob_num; i ++) {
      float converted_x = set_pos[i].x;
      float converted_y = set_pos[i].y;
      if(mouseX > converted_x - range && mouseX < converted_x  + range && mouseY > converted_y - range && mouseY < converted_y + range){
        set_pos[i].x = mouseX;
        set_pos[i].y = mouseY;
      }
    }
  }
}
