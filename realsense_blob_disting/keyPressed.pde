void keyPressed() {
  if (key == '1') {
    page  = 1;
  }
  else if(key == '2') {
    page = 2;
  }
    
}

void mouseDragged(){
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
