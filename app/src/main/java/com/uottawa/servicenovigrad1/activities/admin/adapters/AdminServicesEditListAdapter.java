package com.uottawa.servicenovigrad1.activities.admin.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.uottawa.servicenovigrad1.R;
import com.uottawa.servicenovigrad1.utils.Function;

import java.util.List;

public class AdminServicesEditListAdapter extends ArrayAdapter<String> {
    private Activity context;
    List<String> objects;
    Function edit, delete;

    public AdminServicesEditListAdapter(@NonNull Activity context, @NonNull List<String> objects, Function edit, Function delete) {
        super(context, R.layout.layout_admin_services_listitem, objects);
        this.context = context;
        this.objects = objects;
        this.edit = edit;
        this.delete = delete;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_admin_services_edit_listitem, null, true);

        TextView name = listViewItem.findViewById(R.id.service_edit_listitem);
        name.setText(objects.get(position));

        ImageButton editButton = listViewItem.findViewById(R.id.edit_service_listitem);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.f(position);
            }
        });

        ImageButton deleteButton = (ImageButton) listViewItem.findViewById(R.id.delete_service_listitem);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete.f(position);
            }
        });

        return listViewItem;
    }
}
