package com.dbscanapp;

import com.dbscanapp.GUI.MainForm;
import com.dbscanapp.Model.Cluster;
import com.dbscanapp.Model.Point;
import com.dbscanapp.TextFileHelper.DataParser;
import com.dbscanapp.TextFileHelper.FileHelper;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**Programın çalışacağı ana sınıf. Dosya okuma yazma, çıktı gösterme alma işlemleri bu sınıfta olur.
 * Form sınıfı sadece aracıdır. Çıktı alma ve gösterme işlemleri için {@link MainForm} sınıfını kullanır.*/
public class Main extends Application implements MainForm.MainFormEventListener{

    private MainForm mForm;
    private List<Point> mPoints;
    private Thread mDBScanThread;
    private FileHelper<Point> mFileHelper;
    boolean reset=true;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mForm = new MainForm(this);
        mForm.setVisible(true);
        mForm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**Verilerin okunacağı dosya açıldığında tetiklenen metod.
     * Veriler dosyadan okunur {@link mPoints} e atanır. Form default
     * küme ile hazırlanır.
     *
     * @param file açılan dosya*/
    @Override
    public void onFileOpened(File file) {
        mFileHelper = new FileHelper<>(file,new DataParser());
        try {
            mPoints = mFileHelper.readFromTextFile();
            initFormWithDefaultCluster();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Dosya Acilirken Hata Olustu : "+e.getMessage());
        }
    }

    /**Logların kaydedileceği dosya açılınca tetiklenen metod.
     * Formda gösterilen loglar alınır ve bir text dosyası olarak yazdırılır.
     *
     * @param file logların kaydedileceği dosya*/
    @Override
    public void onOutputFileOpenedToSave(File file) {
        String[] log = mForm.getOutput().split("\n");
        try {
            new FileHelper<String>(file,null).writeToTextFile(log);
            mForm.showMessage("Dosyaya Yazma Başarılı.");
        } catch (IOException e) {
            e.printStackTrace();
            mForm.showMessage("Dosyaya Yazarken Hata Olustu : "+e.getMessage());
        }
    }

    /**Run düğmesine basınca tetiklenen metod. Eğer veriler daha önceden kümelenmişse resetlenir.
     * Sonrasında {@link DBScan} sınıfı yardımıyla DBScan algoritması ayrı bir Thread üzerince çalıştırılır.
     * Algoritma işini bitirdiğinde oluşan kümler ve sapan değer kümesi gösterilmesi için forma gönderilir.*/
    @Override
    public void onDBScanRunPressed(int minPoints, double epsilon) {
        System.out.println("OnDBSCanRun");
        if(!reset)
            onResetPressed();

        DBScan dbScan = new DBScan(mPoints,minPoints,epsilon);
        mDBScanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mForm.showProgress(true);
                List<Cluster> clusters = dbScan.runDbScan();
                mForm.setClusterNoise(dbScan.getNoiseCluster());
                mForm.setClusters(clusters);
                mForm.setOutput(dbScan.getLog());
                mForm.showProgress(false);
                reset = false;
            }
        });
        mDBScanThread.start();
    }

    /**Reset düğmesine basıldığında tetiklenen metod. Eğer DBScan çalıştırılıyorsa çalışması kesilir.
     * Veriler baştan dosyadan okunur ve default cluster olarak forma gönderilir.*/
    @Override
    public void onResetPressed() {
        try {
            if(mDBScanThread.isAlive())
                mDBScanThread.interrupt();
            mPoints = mFileHelper.readFromTextFile();
            initFormWithDefaultCluster();
            mForm.showProgress(false);
            reset = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Bir default cluster oluşturur ve verileri içine atar. Göstermesi için forma gönderir.*/
    private void initFormWithDefaultCluster()
    {
        mForm.setAttributes(mPoints.get(0).getValues().keySet());
        Cluster defaultCluster = new Cluster(Cluster.DEFAULT_CLUSTER_NAME);
        defaultCluster.addAll(mPoints);
        List<Cluster> tempList = new ArrayList<>();
        tempList.add(defaultCluster);
        mForm.setClusters(tempList);
    }
}
