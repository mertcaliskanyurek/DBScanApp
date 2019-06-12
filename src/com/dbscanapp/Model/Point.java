package com.dbscanapp.Model;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

public class Point {

    private Hashtable<String,Double> mValues;
    //private String mClusterLabel;
    private boolean hasCluster;
    private boolean mVisited=false;

    public Point() {
        mValues = new Hashtable<>();
        hasCluster=false;
    }

    public Hashtable<String, Double> getValues() {
        return mValues;
    }
/*
    public String getClusterLabel() {
        return mClusterLabel;
    } */

    public boolean hasCluster() {
        return hasCluster;
    }

    public boolean isVisited() {
        return mVisited;
    }

    public void setVisited(boolean visited) {
        this.mVisited = visited;
    }

    public void setHasCluster(boolean hasCluster) {
        this.hasCluster = hasCluster;
    }
    /*
    public void setClusterLabel(String clusterLabel) {
        this.mClusterLabel = clusterLabel;
    } */

    public void addValue(String attrName, Double value)
    {
        mValues.put(attrName,value);
    }

    /**Compares two points*/
    public static Comparator<Point> comparator = new Comparator<Point>() {
        /**@return 1 if point 1 bigger than point 2
         * -1 if p2 bigger than p1
         * 0 if p1 equal p2*/
        @Override
        public int compare(Point p1, Point p2) {
            Enumeration<Double> elements= p1.getValues().elements();
            Enumeration<Double> elements2= p2.getValues().elements();
            //distance from origin
            double distance1=0;
            double distance2=0;
            while (true)
            {
                try {
                    distance1+=Math.pow(elements.nextElement(),2);
                    distance2+=Math.pow(elements2.nextElement(),2);
                }catch (NoSuchElementException e)
                {
                    break;
                }
            }

            distance1 = Math.sqrt(distance1);
            distance2 = Math.sqrt(distance2);

            if(distance1-distance2>0)
                return 1;
            else if(distance1-distance2<0)
                return -1;
            else
                return 0;
        }
    };

}
