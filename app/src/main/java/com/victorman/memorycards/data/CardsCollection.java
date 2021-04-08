package com.victorman.memorycards.data;


import android.os.Parcel;
import android.os.Parcelable;

public class CardsCollection implements Parcelable {
    private long id;
    private String name;
    private boolean selected;
    private long lastSeen;


    public CardsCollection(long id, String name, boolean selected, long lastSeen) {
        this.id = id;
        this.name = name;
        this.selected = selected;
        this.lastSeen = lastSeen;
    }

    public CardsCollection(long id, String name, short selected, long lastSeen) {
        this.id = id;
        this.name = name;
        this.selected = selected != 0;
        this.lastSeen = lastSeen;
    }


    public CardsCollection(String name, boolean selected, long lastSeen) {
        this.name = name;
        this.selected = selected;
        this.lastSeen = lastSeen;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public short getSelected() {
        if (selected)
            return 1;
        return 0;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public static final Creator<CardsCollection> CREATOR = new Creator<CardsCollection>() {
        @Override
        public CardsCollection createFromParcel(Parcel dest) {
            return new CardsCollection(
                    dest.readLong(),
                    dest.readString(),
                    (dest.readInt() == 1),
                    dest.readLong()
            );
        }

        @Override
        public CardsCollection[] newArray(int size) {
            return new CardsCollection[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(selected? 1 : 0);
        dest.writeLong(lastSeen);
    }

    public static class CardsCollectionContract {
        public static final String ID = "id";
        public static final String NAME = "NAME";
        public static final String SELECTED = "SELECTED";
        public static final String LAST_SEEN = "LAST_SEEN";
    }
}
