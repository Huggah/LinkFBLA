package com.example.linkfbla;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A base class for adapters generating a list of competitive events.
 */
public abstract class CompetitiveEventAdapter extends BaseAdapter {

    private Context context;
    protected ArrayList<CompetitiveEvent> events;
    /**
     * Should be defined in subclass constructor.
     */
    protected int layout;

    /**
     * Extend off of ViewHolder class in child classes of CompetitiveEventAdapter to include more views.
     */
    static class ViewHolder {
        TextView name;
        TextView type;
    }

    /**
     * Should be called in subclass constructor.
     */
    public CompetitiveEventAdapter(Context context, ArrayList<CompetitiveEvent> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public final int getCount() {
        return events.size();
    }

    @Override
    public final Object getItem(int position) {
        return null;
    }

    @Override
    public final long getItemId(int position) {
        return 0;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = getViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(layout, parent, false);
            holder = setupViews(holder, convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertViews(holder, position);

        return convertView;
    }

    /**
     * Call the super method when overriding, then cast the return value to the subclass of ViewHolder to add views.
     */
    protected ViewHolder setupViews(ViewHolder holder, View view) {
        holder.name = view.findViewById(R.id.event_name);
        holder.type = view.findViewById(R.id.event_type);
        return holder;
    }

    /**
     * Call the super method when overriding, then cast the return value to the subclass of ViewHolder to modify views.
     */
    protected void convertViews(ViewHolder holder, int position) {
        holder.name.setText(events.get(position).name);
        holder.type.setText(events.get(position).team ? "Individual, Team" : "Individual");
    }

    /**
     * @return initialized subclass of ViewHolder including extra views included in the subclass of this.
     */
    protected abstract ViewHolder getViewHolder();
}
