/*
 * This file is part of Salsa Beacons
 *
 * Salsa Beacons is a Bluetooth LE aware Android app that enables location dependant learning
 * author:  Richard Greenwood <richard.greenwood@open.ac.uk>
 * Copyright (C) 2015 The Open University
 *
 * Salsa Beacons is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Salsa Beacons is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Salsa Beacons.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.open.salsabeacons;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

/**
 * Created by rmg29 on 13/04/2015.
 */
public class BeaconListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private OnListFragmentInteractionListener mListener;
  private static final String TAG = "BeaconListFragment";
  // This is the Adapter being used to display the list's data.
  SalsaBeaconCursorAdapter mAdapter;
  BeaconReferenceApplication app;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    app = (BeaconReferenceApplication) getActivity().getApplicationContext();
    // Give some text to display if there is no data.  In a real
    // application this would come from a resource.
    Resources resources = getResources();
    setEmptyText(resources.getString(R.string.no_salsa_beacons_present));

    // Context menu delete functionality
    // commented out for the purposes of data collection on this trial
    /*ListView listView = getListView();
    listView.setSelector(R.drawable.list_item_selector);
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    listView.setItemsCanFocus(false);
    listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
      @Override
      public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Resources res = getResources();
        mode.setTitle(getListView().getCheckedItemCount() + " " + res.getString(R.string.selected));
      }

      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        Log.d(TAG, "onCreateActionMode: " + menu);
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.list_items_context, menu);
        return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId()) {
          case R.id.delete_selected_items:
            deleteSelectedItems();
            mode.finish(); // Action picked, so close the CAB
            return true;
          default:
            return false;
        }
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {

      }
    });*/

    // Create an empty adapter we will use to display the loaded data.
    mAdapter = new SalsaBeaconCursorAdapter(getActivity(), R.layout.beacon_list, null, 0);

    setListAdapter(mAdapter);

    // Start out with a progress indicator.
    setListShown(false);

    // Prepare the loader.  Either re-connect with an existing one,
    // or start a new one.
    getLoaderManager().initLoader(0, null, this);
  }

  private void deleteSelectedItems() {
    ListView listView = getListView();
    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
    if (checkedItems != null) {
      for (int i=0; i<checkedItems.size(); i++) {
        if (checkedItems.valueAt(i)) {
          Cursor listItemCursor = (Cursor) listView.getItemAtPosition(checkedItems.keyAt(i));
          SalsaBeacon beacon;
          try {
            String beaconName = listItemCursor.getString(
                listItemCursor.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME)
            );
            beacon = SalsaBeacon.getInstance(beaconName);
            beacon.setDeletedFlag();
          } catch (IllegalArgumentException e) {
            continue;
          }
        }
      }
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if (mListener != null) {
      mListener.onListFragmentInteraction(position);
    }
  }

  // These are the Contacts rows that we will retrieve.
  static final String[] BEACONS_SUMMARY_PROJECTION = new String[] {
      Salsa.BeaconOccurrence._ID,
      Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME,
      Salsa.BeaconOccurrence.AGG_COLUMN_NAME_OCCURRENCE_COUNT
  };

  public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {

    // Create and return a CursorLoader that will take care of
    // creating a Cursor for the data being displayed.
    return new CursorLoader(
        getActivity(),
        Salsa.BeaconOccurrence.CONTENT_VIEWABLE_LIST_URI,
        BEACONS_SUMMARY_PROJECTION,
        null,
        null,
        Salsa.BeaconOccurrence._ID + " DESC"
    );
  }

  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    // Swap the new cursor in.  (The framework will take care of closing the
    // old cursor once we return.)
    mAdapter.swapCursor(data);

    // The list should now be shown.
    if (isResumed()) {
      setListShown(true);
    } else {
      setListShownNoAnimation(true);
    }
  }

  public void onLoaderReset(Loader<Cursor> loader) {
    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.  We need to make sure we are no
    // longer using it.
    mAdapter.swapCursor(null);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnListFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnListFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onListFragmentInteraction(int position);
    public int getLastClicked();
  }
}