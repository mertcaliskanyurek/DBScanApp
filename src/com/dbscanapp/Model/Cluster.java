package com.dbscanapp.Model;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    public static final String DEFAULT_CLUSTER_NAME = "Default_Cluster";
    public static final String CLUSTER_NAME_NOISE = "Noise";

    private String mClusterLabel;
    private List<Point> mPoints;

    public Cluster(String clusterLabel) {
        this.mClusterLabel = clusterLabel;
        mPoints = new ArrayList<>();
    }

    public void addPoint(Point p)
    {
        mPoints.add(p);
        p.setHasCluster(true);
    }

    public void addAll(List<Point> points)
    {
        mPoints = points;
    }

    public String getClusterLabel() {
        return mClusterLabel;
    }

    public List<Point> getPoints() {
        return mPoints;
    }

    public int getSize()
    {
        return mPoints.size();
    }
}
