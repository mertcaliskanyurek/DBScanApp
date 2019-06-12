package com.dbscanapp.TextFileHelper;

import com.dbscanapp.Model.Point;

import java.util.ArrayList;
import java.util.List;

/**FileHelper sınıfına csv dosyasını ayrıştırması için yardım eder.*/
public class DataParser implements ITextDataParser<Point> {

    @Override
    public List<Point> parseFromFile(List<String> lines) {
        List<Point> points= new ArrayList<>();
        String[] attrNames = lines.get(0).split(";");

        for(int i=1;i<lines.size();i++)
        {
            String[] attrValues = lines.get(i).split(";");
            Point temp = new Point();
            for(int j=0;j<attrNames.length;j++) {
                try {
                    temp.addValue(attrNames[j], Double.parseDouble(attrValues[j]));
                }
                catch (NumberFormatException e){
                    e.printStackTrace();
                    System.out.println(j+"inci deger sayi degil");
                }
            }

            points.add(temp);
        }

        return points;
    }

    @Override
    public List<String> parseFromObjects(List<Point> objects) {
        return null;
    }
}
