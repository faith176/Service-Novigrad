package com.uottawa.servicenovigrad1.activities.admin.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.uottawa.servicenovigrad1.R;
import com.uottawa.servicenovigrad1.service.Service;

import java.util.List;

public class AdminServicesListAdapter extends ArrayAdapter<Service> {
    private Activity context;
    List<Service> services;

    public AdminServicesListAdapter(@NonNull Activity context, @NonNull List<Service> services) {
        super(context, R.layout.layout_admin_services_listitem, services);
        this.context = context;
        this.services = services;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_admin_services_listitem, null, true);

        TextView textViewName = listViewItem.findViewById(R.id.service_name);
        TextView textViewDesc = listViewItem.findViewById(R.id.service_desc);
        TextView textViewPrice = listViewItem.findViewById(R.id.service_price);

        Service service = services.get(position);

        textViewName.setText(service.getName());
        textViewDesc.setText(service.getDesc());
        textViewPrice.setText("Price: $" + service.getPrice());

        return listViewItem;
    }
}
