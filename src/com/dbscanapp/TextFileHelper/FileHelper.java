package com.dbscanapp.TextFileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**FileHelper sınıfı herhangi bir metin dosyasından dosyaları okur ve dataparser yardımıyla
 * istenilen sınıfın listesi şeklinde verileri döndürür. Daqta parser nuol olabilir, null olursa
 * satırları içeren String listesi şeklinde döndürecektir
 *
 * Bu sınıf ayrıca dosyaya veri yazabilir, veriler istenilen bir sınıfın listesi de olabilir String
 * listesi de. Sınıf listesi ise verileri satır satır ayrıştırabilmek için yine data parser'a ihtiyaç duyar*/
public class FileHelper<T> {

    private File file;
    private ITextDataParser dataParser;

    public FileHelper(File file,ITextDataParser<T> dataParser)
    {
        this.file = file;
        this.dataParser = dataParser;
    }

    public void writeToTextFile(ArrayList<T> objects) throws IOException {

        if(!file.exists())
            file.createNewFile();

        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        List<String> lines = dataParser.parseFromObjects(objects);

        for (String s:lines) {
            bw.write(s);
            bw.newLine();
        }
        bw.flush();
        fw.close();
        bw.close();

    }
    
    public void writeToTextFile(String[] lines) throws IOException {

        if(!file.exists())
            file.createNewFile();

        FileWriter fw = new FileWriter(file.toString());
        BufferedWriter bw = new BufferedWriter(fw);

        for (String s:lines) {
            bw.write(s);
            bw.newLine();
        }
        bw.flush();
        fw.close();
        bw.close();

    }

    public List<T> readFromTextFile() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file),"ISO-8859-9"));

        ArrayList<String> lines = new ArrayList<>();

        String line = bufferedReader.readLine();

        while (line!=null)
        {
            lines.add(line);
            line = bufferedReader.readLine();
        }

        fileReader.close();
        bufferedReader.close();

        if(dataParser==null)
            return (ArrayList<T>) lines;
        else
            return dataParser.parseFromFile(lines);

    }

}
