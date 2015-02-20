package uk.ac.open.salsabeacons;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by rmg29 on 03/02/2015.
 */
public class BeaconArrayAdapter<Object> extends ArrayAdapter {

  private final int resource;
  private final BeaconReferenceApplication app;

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
    TextView proximityText = (TextView) rowView.findViewById(R.id.beacon_proximity);
    SalsaBeacon beacon = (SalsaBeacon) getItem(position);
    title.setText(beacon.toString());
    Integer iconResourceId = R.string.fa_globe;
    if(beacon.isUnassigned()) {
      iconResourceId = R.string.fa_question;
    } else if(beacon.isResource()) {
      iconResourceId = R.string.fa_file_text;
    }
    iconView.setTypeface(app.getIconFont());
    iconView.setText(iconResourceId);
    Resources resources =  BeaconReferenceApplication.getContext().getResources();
    java.text.DateFormat time = DateFormat.getTimeFormat(BeaconReferenceApplication.getContext());
    java.text.DateFormat date = DateFormat.getDateFormat(BeaconReferenceApplication.getContext());
    double proximity = (double) Math.round(beacon.getLastProximity() * 100) / 100;
    String viewedDetails = "";
    if(beacon.viewed()) {
      int grey = Color.parseColor("#9C9C9C");
      title.setTextColor(grey);
      iconView.setTextColor(grey);
      timestamp.setTextColor(grey);
      proximityText.setTextColor(grey);
      viewedDetails += " viewed at: "+ date.format(beacon.getViewedAt()) + " "
          + time.format(beacon.getViewedAt());
    }
    timestamp.setText(
        date.format(beacon.getLastLogged()) + " "
        + time.format(beacon.getLastLogged())
        + viewedDetails
    );
    proximityText.setText(resources.getString(R.string.estimated_proximity)
        + ": " + proximity);
    return rowView;

  };
}
