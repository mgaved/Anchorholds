package uk.ac.open.salsabeacons;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by rmg29 on 03/02/2015.
 */
public class BeaconArrayAdapter<Object> extends ArrayAdapter {

  private final int resource;
  private final BeaconReferenceApplication app;
  public static int count = 0;

  public BeaconArrayAdapter(Context context, int resource) {
    super(context, resource);
    app = (BeaconReferenceApplication) context;
    this.resource = resource;
  }

  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater)app.getSystemService
        (Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(resource, null, true);

    TextView iconView = (TextView) rowView.findViewById(R.id.beacon_type_icon);
    TextView title = (TextView) rowView.findViewById(R.id.beacon_name);
    TextView timestamp = (TextView) rowView.findViewById(R.id.beacon_timestamp);
    SalsaBeacon beacon = (SalsaBeacon) getItem(position);
    title.setText(beacon.toString());
    Integer iconResourceId = R.string.fa_globe;
    if(beacon.isResource()) {
      iconResourceId = R.string.fa_file_text;
    }
    iconView.setTypeface(app.getIconFont());
    iconView.setText(iconResourceId);
    Date now = new Date();
    BeaconArrayAdapter.count++;
    timestamp.setText(now.toString() + " count: " + count);
    return rowView;

  };
}
