package com.urcib.speedvts.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
public class GeofenceListViewAdapter extends BaseAdapter {

    private List<SpeedVtsGeofence> speedVtsGeofenceList;
    private View.OnClickListener listner;

    private LayoutInflater layoutInflater;

    public GeofenceListViewAdapter(List<SpeedVtsGeofence> speedVtsGeofenceList, View.OnClickListener listner,
                                   Context ctx) {
        this.speedVtsGeofenceList = speedVtsGeofenceList;
        this.listner = listner;

        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return speedVtsGeofenceList.size();
    }

    @Override
    public Object getItem(int i) {
        return speedVtsGeofenceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder geoFenceViewHolder;
        if (view == null){
            geoFenceViewHolder = new ViewHolder();
            view = layoutInflater.inflate(R.layout.inflate_geofence_list, viewGroup, false);

            geoFenceViewHolder.lblName = (TextView) view.findViewById(R.id.lblName);
            geoFenceViewHolder.lblLatitide = (TextView) view.findViewById(R.id.lblLatitude);
            geoFenceViewHolder.lblLongitude = (TextView) view.findViewById(R.id.lblLongitude);
            geoFenceViewHolder.lblRadius = (TextView) view.findViewById(R.id.lblRadius);
            geoFenceViewHolder.btnDelete = (Button) view.findViewById(R.id.btnDelete);
            geoFenceViewHolder.btnEdit = (Button) view.findViewById(R.id.btnEdit);
            geoFenceViewHolder.lnrGeofenceList = (LinearLayout) view.findViewById(R.id.lnrGeofenceList);
            view.setTag(geoFenceViewHolder);
        }else {
            geoFenceViewHolder = (ViewHolder) view.getTag();
        }
        SpeedVtsGeofence geofence = speedVtsGeofenceList.get(position);

        Log.d("getview", "getView: "+position);

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
        geoFenceViewHolder.lnrGeofenceList.setTag(speedVtsGeofenceList.get(position));
        geoFenceViewHolder.lnrGeofenceList.setOnClickListener(listner);

        return view;
    }

    static class ViewHolder{
        TextView lblName;
        TextView lblLatitide;
        TextView lblLongitude;
        TextView lblRadius;
        Button btnDelete;
        Button btnEdit;
        LinearLayout lnrGeofenceList;
    }
}
