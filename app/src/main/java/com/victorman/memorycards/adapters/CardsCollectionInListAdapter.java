package com.victorman.memorycards.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.victorman.memorycards.data.CardsCollection;
import com.victorman.memorycards.R;

import java.util.ArrayList;
import java.util.List;


public class CardsCollectionInListAdapter extends RecyclerView.Adapter<CardsCollectionInListAdapter.ViewHolder> implements Filterable {
    private OnItemClickListener onItemClickListener;

    private List<CardsCollection> cardsCollections;
    private List<CardsCollection> cardsCollectionsAll;


    public CardsCollectionInListAdapter() {
        this.cardsCollections = new ArrayList<>();
        this.cardsCollectionsAll = new ArrayList<>();
    }


    public CardsCollectionInListAdapter(List<CardsCollection> cardsCollections) {
        this.cardsCollections = cardsCollections;
        this.cardsCollectionsAll = new ArrayList<>(cardsCollections);
    }


    public void insertCardsCollection(int position, CardsCollection cardsCollection) {
        cardsCollections.add(position, cardsCollection);
        cardsCollectionsAll.add(position, cardsCollection);
        notifyItemInserted(position);
    }


    public void deleteCardsCollection(int position) {
        cardsCollectionsAll.remove(cardsCollections.get(position));
        cardsCollections.remove(position);
        notifyItemRemoved(position);
    }


    public void editCardsCollection(int position, CardsCollection cardsCollection) {
        cardsCollections.set(position, cardsCollection);
        cardsCollectionsAll.set(cardsCollections.indexOf(cardsCollection), cardsCollection);
        notifyItemChanged(position);
    }


    public List<CardsCollection> getCardsCollections() {
        return cardsCollections;
    }


    public void setCardsCollections(List<CardsCollection> cardsCollections) {
        this.cardsCollections = cardsCollections;
        this.cardsCollectionsAll = new ArrayList<>(cardsCollections);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cards_collection_in_list, parent, false),
                onItemClickListener
        );
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardsCollection currentCardsCollection = cardsCollections.get(position);

        holder.switchCardsCollectionSelected.setChecked(currentCardsCollection.isSelected());
        holder.textViewCardsCollectionName.setText(currentCardsCollection.getName());
    }


    @Override
    public int getItemCount() {
        return cardsCollections.size();
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
        void onSelectClick(int position, boolean selected);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewCardsCollectionName;
        public Switch switchCardsCollectionSelected;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);

            textViewCardsCollectionName = itemView.findViewById(R.id.itemCardsCollectionInListTextViewName);
            switchCardsCollectionSelected = itemView.findViewById(R.id.itemCardsCollectionInListSwitchSelected);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });

            switchCardsCollectionSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onSelectClick(position, isChecked);
                        }
                    }
                }
            });
        }
    }


    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CardsCollection> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(cardsCollectionsAll);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (CardsCollection cardsCollection : cardsCollectionsAll) {
                    if (cardsCollection.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(cardsCollection);
                    }
                }
            }


            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            cardsCollections.clear();
            cardsCollections.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
