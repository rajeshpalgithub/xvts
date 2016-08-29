package com.urcib.speedvts.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.urcib.speedvts.R;
import com.urcib.speedvts.model.SpeedVtsGeofence;

import java.util.List;

/**
 * @author URCIB
 * @version 1.0.0
 * @copyright URCIB TECHNOLOGIES PVT LTD
 * @created on 15/08/16
 *
 * Application class
 */
public class GeofenceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SpeedVtsGeofence> speedVtsGeofenceList;
    private View.OnClickListener listner;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    public GeofenceListAdapter(List<SpeedVtsGeofence> speedVtsGeofenceList, View.OnClickListener listner,
                               RecyclerView recVGeoFence) {
        this.speedVtsGeofenceList = speedVtsGeofenceList;
        this.listner = listner;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recVGeoFence.getLayoutManager();
        recVGeoFence.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });

    }

    private OnLoadMoreListener mOnLoadMoreListener;
    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void refreshLists(List<SpeedVtsGeofence> speedVtsGeofenceList){
        this.speedVtsGeofenceList = speedVtsGeofenceList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate
                    (R.layout.inflate_geofence_list, parent, false);
            return new GeoFenceViewHolder(v);
        } else if (viewType == VIEW_TYPE_LOADING) {
            ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate
                    (R.layout.load_more_item, parent, false);
            return new LoadingViewHolder(v);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return speedVtsGeofenceList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SpeedVtsGeofence geofence = speedVtsGeofenceList.get(position);

        if (holder instanceof GeoFenceViewHolder) {
            GeoFenceViewHolder geoFenceViewHolder = (GeoFenceViewHolder) holder;
            geoFenceViewHolder.lblName.setText(geofence.title);
            geoFenceViewHolder.lblLatitide.setText(String.valueOf(geofence.latitude) + geoFenceViewHolder.lblLatitide.getResources().
                    getString(R.string.degrees_unit));
            geoFenceViewHolder.lblLongitude.setText(String.valueOf(geofence.longitude) + geoFenceViewHolder.lblLongitude.getResources().
                    getString(R.string.degrees_unit));
            geoFenceViewHolder.lblRadius.setText(String.valueOf(geofence.radius / 1000.0) + " " +
                    geoFenceViewHolder.lblRadius.getResources().getString(R.string.kilometers_unit));

            geoFenceViewHolder.btnDelete.setTag(R.id.lblName, position);
            geoFenceViewHolder.btnDelete.setTag(R.id.lblRadius, speedVtsGeofenceList.get(position));
            geoFenceViewHolder.btnEdit.setTag(R.id.lblName, position);
            geoFenceViewHolder.btnEdit.setTag(R.id.lblRadius, speedVtsGeofenceList.get(position));

            geoFenceViewHolder.btnDelete.setOnClickListener(listner);
            geoFenceViewHolder.btnEdit.setOnClickListener(listner);
        }else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }


    }

    @Override
    public int getItemCount() {
        if (speedVtsGeofenceList!=null)
            return speedVtsGeofenceList.size();
        else
            return 0;
    }

    public void setLoaded() {
        isLoading = false;
    }

    static class GeoFenceViewHolder extends RecyclerView.ViewHolder {
        TextView lblName;
        TextView lblLatitide;
        TextView lblLongitude;
        TextView lblRadius;
        Button btnDelete;
        Button btnEdit;

        public GeoFenceViewHolder(ViewGroup v) {
            super(v);

            lblName = (TextView) v.findViewById(R.id.lblName);
            lblLatitide = (TextView) v.findViewById(R.id.lblLatitude);
            lblLongitude = (TextView) v.findViewById(R.id.lblLongitude);
            lblRadius = (TextView) v.findViewById(R.id.lblRadius);
            btnDelete = (Button) v.findViewById(R.id.btnDelete);
            btnEdit = (Button) v.findViewById(R.id.btnEdit);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }
}
