package com.victorman.memorycards.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.GregorianCalendar;


public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CARDS_COLLECTIONS_LIST = "CARDS_COLLECTIONS_LIST";
    private static final String CARDS_LIST = "CARDS_LIST";

    private static DataBaseHelper dataBaseHelper;
    private static SQLiteDatabase database;


    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = this.getReadableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CARDS_COLLECTIONS_LIST + "("
                + CardsCollection.CardsCollectionContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CardsCollection.CardsCollectionContract.NAME + " TEXT, "
                + CardsCollection.CardsCollectionContract.SELECTED + " INTEGER, "
                + CardsCollection.CardsCollectionContract.LAST_SEEN + " INTEGER);");

        db.execSQL("CREATE TABLE " + CARDS_LIST + "("
                + Card.CardContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Card.CardContract.TERM + " TEXT, "
                + Card.CardContract.DEFINITION + " TEXT, "
                + Card.CardContract.POSITIVE_COUNT + " INTEGER, "
                + Card.CardContract.NEGATIVE_COUNT + " INTEGER, "
                + Card.CardContract.CARDS_COLLECTION_ID + " INTEGER);"
        );
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    public static void openDataBase(Context context) {
        dataBaseHelper = new DataBaseHelper(context);
    }


    public static void closeDataBase() {
        if (database != null)
            database.close();
        database = null;
    }


    public static CardsCollection getCardsCollection(long cardsCollectionId) {
        Cursor cursor = database.query(
                CARDS_COLLECTIONS_LIST,
                new String[]{
                        CardsCollection.CardsCollectionContract.ID,
                        CardsCollection.CardsCollectionContract.NAME,
                        CardsCollection.CardsCollectionContract.SELECTED,
                        CardsCollection.CardsCollectionContract.LAST_SEEN
                },
                CardsCollection.CardsCollectionContract.ID + " = ?",
                new String[] {String.valueOf(cardsCollectionId)},
                null,
                null,
                null
        );

        cursor.moveToNext();

        CardsCollection cardsCollection = new CardsCollection(
                cursor.getLong(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.ID)),
                cursor.getString(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.NAME)),
                cursor.getShort(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.SELECTED)),
                cursor.getLong(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.LAST_SEEN))
        );

        cursor.close();
        return cardsCollection;
    }


    public static ArrayList<CardsCollection> getCardsCollections() {
        Cursor cursor = database.query(
                CARDS_COLLECTIONS_LIST,
                new String[]{
                        CardsCollection.CardsCollectionContract.ID,
                        CardsCollection.CardsCollectionContract.NAME,
                        CardsCollection.CardsCollectionContract.SELECTED,
                        CardsCollection.CardsCollectionContract.LAST_SEEN
                },
                null,
                null,
                null,
                null,
                CardsCollection.CardsCollectionContract.LAST_SEEN + " DESC"
        );

        ArrayList<CardsCollection> cardsCollections = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            cardsCollections.add(new CardsCollection(
                    cursor.getLong(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.ID)),
                    cursor.getString(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.NAME)),
                    cursor.getShort(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.SELECTED)),
                    cursor.getLong(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.LAST_SEEN))
            ));
        }


        cursor.close();
        return cardsCollections;
    }


    public static long[] getSelectedCardsCollectionsIds() {
        Cursor cursor = database.query(
                CARDS_COLLECTIONS_LIST,
                new String[]{
                        CardsCollection.CardsCollectionContract.ID,
                },
                CardsCollection.CardsCollectionContract.SELECTED + " = ?",
                new String[] {String.valueOf(1)},
                null,
                null,
                CardsCollection.CardsCollectionContract.LAST_SEEN + " DESC"
        );

        long[] cardsCollectionsIds = new long[cursor.getCount()];
        while (cursor.moveToNext()) {
            cardsCollectionsIds[cursor.getPosition()] = cursor.getLong(cursor.getColumnIndex(CardsCollection.CardsCollectionContract.ID));
        }

        cursor.close();
        return cardsCollectionsIds;
    }


    public static long createCardsCollection(CardsCollection cardsCollection) {

        ContentValues values = new ContentValues();
        values.put(CardsCollection.CardsCollectionContract.NAME, cardsCollection.getName());
        values.put(CardsCollection.CardsCollectionContract.SELECTED, cardsCollection.getSelected());
        values.put(CardsCollection.CardsCollectionContract.LAST_SEEN, new GregorianCalendar().getTimeInMillis());

        return database.insert(CARDS_COLLECTIONS_LIST, null, values);
    }


    public static void deleteCardsCollection(long cardsCollectionId) {
        database.delete(
                CARDS_COLLECTIONS_LIST,
                CardsCollection.CardsCollectionContract.ID + " = ?",
                new String[]{String.valueOf(cardsCollectionId)}
        );

        database.delete(
                CARDS_LIST,
                Card.CardContract.CARDS_COLLECTION_ID + " = ?",
                new String[]{String.valueOf(cardsCollectionId)}
        );
    }


    public static long updateCardsCollectionSelected(CardsCollection cardsCollection) {
        ContentValues values = new ContentValues();
        values.put(CardsCollection.CardsCollectionContract.SELECTED, cardsCollection.getSelected());

        return database.update(
                CARDS_COLLECTIONS_LIST,
                values,
                CardsCollection.CardsCollectionContract.ID + " = ?",
                new String[] {String.valueOf(cardsCollection.getId())}
        );
    }


    public static long updateCardsCollection(CardsCollection cardsCollection) {
        ContentValues values = new ContentValues();
        values.put(CardsCollection.CardsCollectionContract.NAME, cardsCollection.getName());
        values.put(CardsCollection.CardsCollectionContract.SELECTED, cardsCollection.getSelected());
        values.put(CardsCollection.CardsCollectionContract.LAST_SEEN, new GregorianCalendar().getTimeInMillis());

        return database.update(
                CARDS_COLLECTIONS_LIST,
                values,
                CardsCollection.CardsCollectionContract.ID + " = ?",
                new String[] {String.valueOf(cardsCollection.getId())}
        );
    }


    public static Card getCard(long cardId) {
        Cursor cursor = database.query(
                CARDS_LIST,
                new String[]{
                        Card.CardContract.ID,
                        Card.CardContract.TERM,
                        Card.CardContract.DEFINITION,
                        Card.CardContract.POSITIVE_COUNT,
                        Card.CardContract.NEGATIVE_COUNT,
                        Card.CardContract.CARDS_COLLECTION_ID
                },
                Card.CardContract.ID + " = " + cardId,
                null,
                null,
                null,
                null
        );

        cursor.moveToNext();

        Card card = new Card(
                cursor.getLong(cursor.getColumnIndex(Card.CardContract.ID)),
                cursor.getString(cursor.getColumnIndex(Card.CardContract.TERM)),
                cursor.getString(cursor.getColumnIndex(Card.CardContract.DEFINITION)),
                cursor.getInt(cursor.getColumnIndex(Card.CardContract.POSITIVE_COUNT)),
                cursor.getInt(cursor.getColumnIndex(Card.CardContract.NEGATIVE_COUNT)),
                cursor.getLong(cursor.getColumnIndex(Card.CardContract.CARDS_COLLECTION_ID))
        );
        cursor.close();

        return card;
    }


    public static ArrayList<Card> getCards(long cardsCollectionId) {
        Cursor cursor = database.query(
                CARDS_LIST,
                new String[]{
                        Card.CardContract.ID,
                        Card.CardContract.TERM,
                        Card.CardContract.DEFINITION,
                        Card.CardContract.POSITIVE_COUNT,
                        Card.CardContract.NEGATIVE_COUNT,
                        Card.CardContract.CARDS_COLLECTION_ID
                },
                Card.CardContract.CARDS_COLLECTION_ID + " = " + cardsCollectionId,
                null,
                null,
                null,
                null
        );

        ArrayList<Card> cards = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            cards.add(new Card(
                    cursor.getLong(cursor.getColumnIndex(Card.CardContract.ID)),
                    cursor.getString(cursor.getColumnIndex(Card.CardContract.TERM)),
                    cursor.getString(cursor.getColumnIndex(Card.CardContract.DEFINITION)),
                    cursor.getInt(cursor.getColumnIndex(Card.CardContract.POSITIVE_COUNT)),
                    cursor.getInt(cursor.getColumnIndex(Card.CardContract.NEGATIVE_COUNT)),
                    cursor.getLong(cursor.getColumnIndex(Card.CardContract.CARDS_COLLECTION_ID))
            ));
        }

        cursor.close();
        return cards;
    }


    public static long addCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(Card.CardContract.TERM, card.getTerm());
        values.put(Card.CardContract.DEFINITION, card.getDefinition());
        values.put(Card.CardContract.POSITIVE_COUNT, card.getPositiveCount());
        values.put(Card.CardContract.NEGATIVE_COUNT, card.getNegativeCount());
        values.put(Card.CardContract.CARDS_COLLECTION_ID, card.getCardsCollectionId());

        return database.insert(CARDS_LIST, null, values);
    }

    public static long updateCardPositiveCount(Card card) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Card.CardContract.POSITIVE_COUNT, card.getPositiveCount());

        return database.update(
                CARDS_LIST,
                contentValues,
                Card.CardContract.ID + " = ?",
                new String[] {String.valueOf(card.getId())}
        );
    }

    public static long updateCardNegativeCount(Card card) {
        ContentValues values = new ContentValues();
        values.put(Card.CardContract.NEGATIVE_COUNT, card.getNegativeCount());

        return database.update(
                CARDS_LIST,
                values,
                Card.CardContract.ID + " = ?",
                new String[] {String.valueOf(card.getId())}
        );
    }

    public static long updateCardCounters(Card card) {
        ContentValues values = new ContentValues();
        values.put(Card.CardContract.POSITIVE_COUNT, card.getPositiveCount());
        values.put(Card.CardContract.NEGATIVE_COUNT, card.getNegativeCount());

        return database.update(
                CARDS_LIST,
                values,
                Card.CardContract.ID + " = ?",
                new String[] {String.valueOf(card.getId())}
        );
    }

    public static long updateCardSignature(Card card) {
        ContentValues values = new ContentValues();
        values.put(Card.CardContract.TERM, card.getTerm());
        values.put(Card.CardContract.DEFINITION, card.getDefinition());

        return database.update(
                CARDS_LIST,
                values,
                Card.CardContract.ID + " = ?",
                new String[] {String.valueOf(card.getId())}
        );
    }

    public static long updateCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(Card.CardContract.TERM, card.getTerm());
        values.put(Card.CardContract.DEFINITION, card.getDefinition());
        values.put(Card.CardContract.POSITIVE_COUNT, card.getPositiveCount());
        values.put(Card.CardContract.NEGATIVE_COUNT, card.getNegativeCount());
        values.put(Card.CardContract.CARDS_COLLECTION_ID, card.getCardsCollectionId());

        return database.update(
                CARDS_LIST,
                values,
                Card.CardContract.ID + " = ?",
                new String[] {String.valueOf(card.getId())}
        );
    }


    public static long deleteCard(long cardId) {
        return database.delete(
                CARDS_LIST,
                Card.CardContract.ID + " = ?",
                new String[] {String.valueOf(cardId)}
        );
    }
}
