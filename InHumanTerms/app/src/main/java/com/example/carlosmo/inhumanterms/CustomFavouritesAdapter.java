package com.example.carlosmo.inhumanterms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Carlos Mo on 20/11/2015.
 */
public class CustomFavouritesAdapter extends ArrayAdapter<TermListItem> {
    Context context;
    List<TermListItem> favouritesItemList;
    int layoutResID;
    DatabaseHelper db;

    public CustomFavouritesAdapter(Context context, int layoutResourceID, List<TermListItem> listItems) {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.favouritesItemList = listItems;
        this.layoutResID = layoutResourceID;
        db = new DatabaseHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FavouritesItemHolder favouritesHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            favouritesHolder = new FavouritesItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            favouritesHolder.term = (TextView) view.findViewById(R.id.favouritedTerm);
            favouritesHolder.favouritedStatusButton = (ImageButton) view.findViewById(R.id.favouritedStatusButton);

            favouritesHolder.favouritedStatusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the position of the row
                    // Source: http://jmsliu.com/2444/click-button-in-listview-and-get-item-position.html
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    int position = listView.getPositionForView(parentRow);

                    // Get the term list item object
                    TermListItem termListItem = (TermListItem) listView.getItemAtPosition(position);

                    // Get term from favourites table
                    db.deleteTermFromFavourites(termListItem.getId());

                    // Display message
                    Toast.makeText(context, "Removed from favourites.", Toast.LENGTH_SHORT).show();

                    // Notify that the dataset has changed
                    favouritesItemList.remove(position);
                    notifyDataSetChanged();
                }
            });

            view.setTag(favouritesHolder);
        } else {
            favouritesHolder = (FavouritesItemHolder) view.getTag();
        }

        TermListItem dItem = (TermListItem) this.favouritesItemList.get(position);

        favouritesHolder.term.setText(dItem.getTerm());
        favouritesHolder.favouritedStatusButton.setBackgroundResource(R.drawable.fa_star);

        return view;
    }

    private static class FavouritesItemHolder {
        TextView term;
        ImageButton favouritedStatusButton;
    }
}
