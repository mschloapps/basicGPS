package com.mschloapps.basicgps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class EditLocationsAdapter extends RecyclerView.Adapter<EditLocationsAdapter.ViewHolder> {

    private List<GPSLoc> values;
    private Context context;
    private SQLiteAdapter db;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView lineone;
        public TextView linetwo;
        public LinearLayout rv_row;
        public View layout;

        private ViewHolder(View v) {
            super(v);
            layout = v;
            lineone = v.findViewById(R.id.lineone);
            linetwo = v.findViewById(R.id.linetwo);
            rv_row = v.findViewById(R.id.rv_row);
        }

    }

    public EditLocationsAdapter(List<GPSLoc> data, Context ctx) {
        values = data;
        context = ctx;
        db = new SQLiteAdapter(ctx);
    }

    public void updateData(List<GPSLoc> data) {
        values.clear();
        values = data;
        notifyDataSetChanged();
    }

    @Override
    public EditLocationsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.recycler_view_row, null);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String name = values.get(holder.getAdapterPosition()).getName();
        holder.lineone.setText(name);
        String linetwoStr = "Lat: " + values.get(holder.getAdapterPosition()).getLat() + ", Long: " + values.get(holder.getAdapterPosition()).getLong() + ", Alt: " + values.get(holder.getAdapterPosition()).getAlt();
        holder.linetwo.setText(linetwoStr);
        holder.rv_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialoglayout = inflater.inflate(R.layout.edit_location, null);
                builder.setTitle("Edit: " + name);
                final EditText latitude = dialoglayout.findViewById(R.id.editText8);
                latitude.setText(String.valueOf(values.get(holder.getAdapterPosition()).getLat()));
                final EditText longitude = dialoglayout.findViewById(R.id.editText9);
                longitude.setText(String.valueOf(values.get(holder.getAdapterPosition()).getLong()));
                final EditText altitude = dialoglayout.findViewById(R.id.editText10);
                altitude.setText(String.valueOf(values.get(holder.getAdapterPosition()).getAlt()));

                builder.setView(dialoglayout);
                builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String emptyStr = "";
                        double lat = 0.0000;
                        if (emptyStr.contentEquals(latitude.getText())) {
                            Toast.makeText(context, "Enter Latitude.", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            lat = Double.parseDouble(latitude.getText().toString());
                        }

                        double lng = 0.0000;
                        if (emptyStr.contentEquals(longitude.getText())) {
                            Toast.makeText(context, "Enter Longitude.", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            lng = Double.parseDouble(longitude.getText().toString());
                        }

                        double alt = 0.0000;
                        if (emptyStr.contentEquals(altitude.getText())) {
                            Toast.makeText(context, "Enter Altitude.", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            alt = Double.parseDouble(altitude.getText().toString());
                        }

                        GPSLoc loc = new GPSLoc(name, lat, lng, alt);
                        db.updateLoc(loc);
                        values = db.getAllLocations();
                        notifyDataSetChanged();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

}
