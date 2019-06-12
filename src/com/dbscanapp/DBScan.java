package com.dbscanapp;

import com.dbscanapp.Model.Cluster;
import com.dbscanapp.Model.Point;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**Bu sınıf DBScan ile ilgili işlemleri içerir. parametre olarak Point listesi tipinde veriler,
 * epsilon komşuluğundaki içermesini istenilen minimum nokta sayısı, ve epsilon sayısı alır.
 *
 * runDbScan çalıştırıldığında Cluster listesi döndürür. Bu liste boş ta olabilir. Tüm veriler
 * sapan değer(noise) olarak atanmış olabilir. Sapan değerler ayrı olarak tutulmuştur {@link getNoiseCluster()}
 * metoduyla dışarıdan çağırılabilir. */
public class DBScan {

    private static final String CLUSTER_LABEL = "Cluster ";

    private List<Point> mPoints;
    private int mMinPoints;
    private double mEpsilon;
    private List<Cluster> mClusters;
    private Cluster mNoiseCluster =null;
    private StringBuilder logBuilder;

    /**@param points DbScan ile kümelere ayrılacak veriler
     * @param minPoints kümeleme işlemi için tercih edilen minimum komşu nokta
     * @param epsilon kümeleme işlemi için tercih edilen maksimum komşu nokta uzaklığı*/
    public DBScan(List<Point> points, int minPoints, double epsilon) {
        this.mPoints = points;
        this.mMinPoints = minPoints;
        this.mEpsilon = epsilon;

        mClusters = new ArrayList<>();
        logBuilder = new StringBuilder();
    }

    /**DB Scan algoritmasını çalıştırır. Küme numaraları 1 den başlar.
     * Tüm gidilmemiş noktalar için epsilon komşuluğu noktalarını bulur ve eğer bunlar
     * minPoints ten küçükse sapan değer olarak işaretler. Şayet büyükse yeni bir küme oluşturulup
     * küme genişletilir.
     *
     * @return finalde oluşan kümeleri döndürür.Tüm değerler sapan değer ise bu liste boş ta olabilir.
     *  Sapan değerler kümesini getirmek için {@link getNoiseCluster()} metodunu çağırın*/
    public List<Cluster> runDbScan()
    {
        int currClustNum=0;
        //for all Points
        for(int i=0;i<mPoints.size();i++) {
            Point curr = mPoints.get(i);
            if(curr.isVisited())
                continue;

            curr.setVisited(true);
            List<Point> neighPoints = findNeighPoints(curr);
            if(neighPoints.size()<mMinPoints){
                if(mNoiseCluster ==null)
                    mNoiseCluster = new Cluster(Cluster.CLUSTER_NAME_NOISE);
                mNoiseCluster.addPoint(curr);
                logBuilder.append(i+" inci kayıt: Sapan Değer\n");
            }
            else {
                currClustNum++;
                Cluster nextCluster = new Cluster(CLUSTER_LABEL+currClustNum);
                mClusters.add(nextCluster);
                expandCluster(curr,neighPoints,nextCluster);
            }
        }

        return mClusters;
    }

    /**Verilen kümete verilen noktayı dahil eder ve epsilon komşuluğunda minPoints ten fazla,
     * daha önce gidilmemiş nokta buldukça komşular listesine dahil etmeye ve kümeye eklemeye devam eder.
     * Komşular listesine dahil edecek komşuları minPointsten küçük nokta olana veya noktalar bitene kadar
     * genişletme işlemi devam eder.
     *
     * Bu sırada loglar da tutar.
     *
     * @param p kümeye dahil edilecek nokta
     * @param pNeighs kümeye dahil edilecek noktanın o anki komşuları. Daha sonra bu liste komşu bulduça genişleyecektir.
     * @param cluster genişletilecek küme
     * */
    private void expandCluster(Point p,List<Point> pNeighs,Cluster cluster)
    {
        cluster.addPoint(p);
        logBuilder.append(mPoints.indexOf(p)+" inci kayıt: "+cluster.getClusterLabel()+'\n');
        int neighSize = pNeighs.size();
        //neigh size is dynamic
        for(int i=0;i<neighSize;i++)
        {
            Point currNeigh = pNeighs.get(i);
            if(!currNeigh.isVisited())
            {
                currNeigh.setVisited(true);
                List<Point> neighs2 = findNeighPoints(currNeigh);
                if(neighs2.size()>=mMinPoints) {
                    pNeighs.addAll(neighs2);
                    neighSize=pNeighs.size();
                }

                if(!currNeigh.hasCluster()) {
                    cluster.addPoint(currNeigh);
                    logBuilder.append(mPoints.indexOf(currNeigh)+" inci kayıt: "+cluster.getClusterLabel()+'\n');
                }
            }
        }//for all neighs
    }

    /**Verilen noktanın epsilon komşuluğundaki noktaları bulur.
     *  Brute force yöntem kullanır, yani listedeki tüm noktalar ile uzaklığına bakılır.
     *  KD Tree yöntemi ile daha hızlı sonuç alınabilir.
     *
     * @param p komşuları aranacak nokta
     * @return epsilon komşuluğundaki Pointlerin listesi*/
    private List<Point> findNeighPoints(Point p)
    {
        List<Point> neighPoints = new ArrayList<>();
        for(Point temp:mPoints)
        {
            //if point is p
            if(Point.comparator.compare(p,temp)==0)
                continue;

            if(calcDistance(p,temp)<=mEpsilon)
                neighPoints.add(temp);
        }
        return neighPoints;
    }

    /**Verilen iki noktanın öklid uzaklığı hesaplanır.
     * Noktaların kaç boyutta olduğu önemsizdir.
     *
     * @param p1 p2 uzaklığı ölçülecek noktalar
     * @return iki noktanın uzaklığı*/
    private double calcDistance(Point p1,Point p2)
    {
        Enumeration<Double> p1Values = p1.getValues().elements();
        Enumeration<Double> p2Values = p2.getValues().elements();
        double result=0;
        while (true) {
            try {
                result += Math.pow(p1Values.nextElement() - p2Values.nextElement(), 2);
            }catch (NoSuchElementException e) {
                break;
            }
        }
        return Math.sqrt(result);
    }

    public Cluster getNoiseCluster() {
        return mNoiseCluster;
    }

    /**Hangi verinin hangi kümeye atandığı (sapan değerler dahil)
     * , hangi kümede kaç değer olduğu bilgisini içeren bir yazı döndürür*/
    public String getLog()
    {
        for(Cluster c:mClusters)
            logBuilder.append(c.getClusterLabel()+": "+c.getSize()+" kayıt\n");

        if(mNoiseCluster !=null)
            logBuilder.append("Kümeye Atanmayan (Sapan Değer) : "+ mNoiseCluster.getSize()+'\n');

        return logBuilder.toString();
    }
}
