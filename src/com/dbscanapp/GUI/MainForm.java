package com.dbscanapp.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Set;

import com.dbscanapp.Model.Cluster;
import com.dbscanapp.Model.Point;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**Girdi ve çıktı işlemlerinin gerçekleştiği UI sınıf. */
public class MainForm extends JFrame{

    private JTextField textFieldMinPoints;
    private JTextField textFieldEpsilon;
    private JPanel main_panel;
    private JButton runButton;
    private JButton openFileButton;
    private JPanel Components;
    private JPanel VisualField;
    private JPanel Buttons;
    private JComboBox comboBoxXAttr;
    private JComboBox comboBoxYAttr;
    private JButton buttonReset;
    private JLabel labelProgress;
    private JTextArea outputTextArea;
    private JButton buttonSaveFile;
    private JPanel panelFile;
    private JPanel panelAttrs;

    private JFreeChart mChart;
    private MainFormEventListener mListener;

    private XYSeriesCollection mDataset = new XYSeriesCollection();

    private List<Cluster> mClusters;
    private Cluster mClusterNoise;
    private boolean mChartUpdateLock;

    /**@param listener Formdaki butonlara tıklandığında eventlerin tetiklenmesi için gereken listener.
     * Tetikleme işlemini ele alacak sınıf {@link MainFormEventListener} listenerını implement etmelidir.*/
    public MainForm(MainFormEventListener listener)
    {
        add(main_panel);
        Dimension d = new Dimension(1280,768);
        setSize(d);
        setMinimumSize(d);
        this.mListener = listener;

        initComponents();
        initChart();
    }

    /**Formadki view elemanları hazırlanır.*/
    private void initComponents() {
        labelProgress.setVisible(false);
        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Open File Button Tıklandı");
                JFileChooser fileChooser = new JFileChooser();
                if(fileChooser.showOpenDialog(main_panel)==JFileChooser.APPROVE_OPTION)
                    mListener.onFileOpened(fileChooser.getSelectedFile());
            }
        });

        buttonSaveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Save File Button Tıklandı");
                JFileChooser fileChooser = new JFileChooser();
                if(fileChooser.showSaveDialog(main_panel)==JFileChooser.APPROVE_OPTION)
                    mListener.onOutputFileOpenedToSave(fileChooser.getSelectedFile());
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int minPoints = Integer.parseInt(textFieldMinPoints.getText());
                    double epsilon = Double.parseDouble(textFieldEpsilon.getText());
                    mListener.onDBScanRunPressed(minPoints,epsilon);
                }catch (NumberFormatException exception)
                {
                    JOptionPane.showMessageDialog(main_panel,"Doğru veriler girmediniz. Lütfen Verileri Kontrol Ediniz");
                }
            }
        });

        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mListener.onResetPressed();
            }
        });

        comboBoxXAttr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!mChartUpdateLock) {
                    updateChart();
                }
            }
        });

        comboBoxYAttr.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!mChartUpdateLock) {
                    updateChart();
                }
            }
        });
    }

    /**@param clusterNoise formda Chart üzerinde görsel olarak kırmızı şekilde gösterilmek istenen,
     *             XY Koordinatlı noktalar içeren küme*/
    public void setClusterNoise(Cluster clusterNoise) {
        this.mClusterNoise = clusterNoise;
    }

    /**Formda Chart üzerinde görsel olarak gösterilmek istenen XY Koordinatlı noktalar içeren kümeler*/
    public void setClusters(List<Cluster> clusters) {
        mClusters = clusters;

        comboBoxXAttr.setSelectedIndex(0);
        if(comboBoxYAttr.getItemCount()>1)
            comboBoxYAttr.setSelectedIndex(1);
    }

    /**@param message form ekranında dialog olarak gösterilmek istenen mesaj*/
    public void showMessage(String message)
    {
        JOptionPane.showMessageDialog(main_panel,message);
    }

    /**Basit bir işleniyor yazısı animasyonu gösterir.
     *
     * @param show işleniyor yazısını göster veya gizle*/
    public void showProgress(boolean show)
    {
        labelProgress.setVisible(show);
        runButton.setEnabled(!show);
        //animate progress bar
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (show) {
                    short i=5;
                    String text = "Processing";
                    labelProgress.setText(text);
                    try {
                        while (i!=0) {
                            text+='.';
                            labelProgress.setText(text);
                            Thread.sleep(1000);
                            i--;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**Comboboxlara veri eklenirken seçim eventi tetiklenmesini önlemek adına kilit konmuştur.
     *
     * @param attrNames Formda chart üzerinde x ve y düzleminde
     *                  gösterilmek üzere seçilecek özellik isimleri.*/
    public void setAttributes(Set<String> attrNames) {
        mChartUpdateLock = true;
        comboBoxXAttr.removeAllItems();
        comboBoxYAttr.removeAllItems();
        for(String s:attrNames)
        {
            comboBoxXAttr.addItem(s);
            comboBoxYAttr.addItem(s);
        }
        mChartUpdateLock = false;
    }

    /**Chart ı comboboxta seçilen x ve y özelliklerine göre yeniler.*/
    public void updateChart() {
        String xAttr = (String) comboBoxXAttr.getSelectedItem();
        String yAttr = (String) comboBoxYAttr.getSelectedItem();

        mDataset.removeAllSeries();

        if(mClusterNoise!=null) {
            XYSeries tempSeries = new XYSeries(mClusterNoise.getClusterLabel());
            for (Point p : mClusterNoise.getPoints())
                tempSeries.add(p.getValues().get(xAttr), p.getValues().get(yAttr));

            mDataset.addSeries(tempSeries);
        }

        for(Cluster cluster:mClusters)
        {
            XYSeries tempSeries = new XYSeries(cluster.getClusterLabel());
            for (Point p:cluster.getPoints())
                tempSeries.add(p.getValues().get(xAttr),p.getValues().get(yAttr));

            mDataset.addSeries(tempSeries);
        }

    }

    /**Formda Bir Scatter Plot hazırlar.*/
    private void initChart()
    {
        mChart = ChartFactory.createScatterPlot(null,"X Axis","Y Axis",mDataset);
        ChartPanel chartPanel = new ChartPanel(mChart);
        VisualField.add(chartPanel,BorderLayout.CENTER);
        VisualField.validate();
    }

    /**@param output Formda gösterilecek log verileri*/
    public void setOutput(String output)
    {
        outputTextArea.append('\n'+output);
    }

    /**@return formda gösterilen log verileri*/
    public String getOutput()
    {
        return outputTextArea.getText();
    }

    public interface MainFormEventListener
    {
        void onFileOpened(File file);
        void onOutputFileOpenedToSave(File file);
        void onDBScanRunPressed(int minPoints, double epsilon);
        void onResetPressed();
    }
}
