PrintWriter file; 

void csvSetup() {
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




void loadCsv() {
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
    pickPoints[i].x = float(csv[i][0]);
    pickPoints[i].y = float(csv[i][1]);
  }
}
