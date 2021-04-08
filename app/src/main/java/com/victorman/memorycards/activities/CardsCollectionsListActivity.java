package com.victorman.memorycards.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.victorman.memorycards.data.Card;
import com.victorman.memorycards.data.CardsCollection;
import com.victorman.memorycards.data.DataBaseHelper;
import com.victorman.memorycards.R;
import com.victorman.memorycards.Utils;
import com.victorman.memorycards.adapters.CardsCollectionInListAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class CardsCollectionsListActivity extends AppCompatActivity {
    public static final String EXTRA_CARDS_COLLECTION = "CARDS_COLLECTION";

    public static final int REQUEST_CODE_CARDS_LIST = 1;
    public static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_CODE_GET_FILE_FROM_STORAGE = 3;

    private int selectedCardsCollectionPosition;

    private CardsCollectionInListAdapter cardsCollectionInListAdapter;

    private ConstraintLayout rootLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        }
        else {
            setTheme(R.style.LightTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_collections_list);

        setToolbar();
        setRecyclerViewCardsCollections();
        setOtherViews();

        (new AsyncTaskExecutor()).execute(AsyncTaskExecutor.TASK_LOAD_CARDS_COLLECTIONS);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;


        if (requestCode == REQUEST_CODE_CARDS_LIST) {
            if (resultCode == CardsListActivity.RESULT_CODE_COLLECTION_CHANGED_AND_CREATED) {
                cardsCollectionInListAdapter.editCardsCollection(selectedCardsCollectionPosition,
                        (CardsCollection) data.getParcelableExtra(CardsListActivity.EXTRA_CARDS_COLLECTION)
                );

                cardsCollectionInListAdapter.insertCardsCollection(cardsCollectionInListAdapter.getItemCount(),
                        (CardsCollection) data.getParcelableExtra(CardsListActivity.EXTRA_CREATED_CARDS_COLLECTION));
            }
            else if (resultCode == CardsListActivity.RESULT_CODE_COLLECTION_EDITED) {
                cardsCollectionInListAdapter.editCardsCollection(selectedCardsCollectionPosition,
                        (CardsCollection) data.getParcelableExtra(CardsListActivity.EXTRA_CARDS_COLLECTION)
                );
            }
            else if (resultCode == CardsListActivity.RESULT_CODE_COLLECTION_CREATED) {
                cardsCollectionInListAdapter.insertCardsCollection(cardsCollectionInListAdapter.getItemCount(),
                        (CardsCollection) data.getParcelableExtra(CardsListActivity.EXTRA_CREATED_CARDS_COLLECTION));
            }
        }
        else if (requestCode == REQUEST_CODE_GET_FILE_FROM_STORAGE) {
            if (resultCode == Activity.RESULT_OK) {
                (new AsyncTaskExecutor()).execute(AsyncTaskExecutor.TASK_DOWNLOAD_COLLECTION_FROM_FILE, data.getData());
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cards_collections_list_toolbar, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.menuActionCardsCollectionSearch).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cardsCollectionInListAdapter.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onClose() {
                searchView.setLayoutParams(new MaterialToolbar.LayoutParams(Gravity.END));
                searchView.clearFocus();
                Objects.requireNonNull(getSupportActionBar()).collapseActionView();
                return false;
            }
        });

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openPickTxtFileActivity();
            }
        }
    }


    public void openPickTxtFileActivity() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_GET_FILE_FROM_STORAGE);
    }


    protected void openCardsListActivity(int requestCode) {
        Intent intent = new Intent(CardsCollectionsListActivity.this, CardsListActivity.class);
        CardsCollection cardsCollection = cardsCollectionInListAdapter.getCardsCollections().get(selectedCardsCollectionPosition);
        cardsCollection.setLastSeen(System.currentTimeMillis());
        DataBaseHelper.updateCardsCollection(cardsCollection);
        intent.putExtra(EXTRA_CARDS_COLLECTION, cardsCollectionInListAdapter.getCardsCollections().get(selectedCardsCollectionPosition));
        startActivityForResult(intent, requestCode);
    }


    protected void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.activityCardsCollectionsListToolbar);

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuActionCardsCollectionSearch) {
                    SearchView searchView = (SearchView) item.getActionView();
                    searchView.setIconifiedByDefault(true);
                    searchView.setFocusable(true);
                    searchView.setIconified(false);
                    searchView.requestFocusFromTouch();
                    return true;
                }
                if (item.getItemId() == R.id.menuActionCardsCollectionDownload) {
                    // check permissions
                    if (ContextCompat.checkSelfPermission(CardsCollectionsListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openPickTxtFileActivity();
                    } else {
                        ActivityCompat.requestPermissions(
                                CardsCollectionsListActivity.this,
                                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
                    }

                    return true;
                }

                return false;
            }
        });
    }


    protected void setRecyclerViewCardsCollections() {
        RecyclerView recyclerView = findViewById(R.id.activityCardsCollectionsListRecyclerViewCardsCollections);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardsCollectionInListAdapter = new CardsCollectionInListAdapter();
        recyclerView.setAdapter(cardsCollectionInListAdapter);
        cardsCollectionInListAdapter.setOnItemClickListener(new CardsCollectionInListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                selectedCardsCollectionPosition = position;
                openCardsListActivity(REQUEST_CODE_CARDS_LIST);
            }

            @Override
            public void onSelectClick(int position, boolean selected) {
                CardsCollection cardsCollection = cardsCollectionInListAdapter.getCardsCollections().get(position);
                cardsCollection.setSelected(selected);
                DataBaseHelper.updateCardsCollectionSelected(cardsCollection);
            }
        });

        (new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int currentPosition = viewHolder.getAdapterPosition();
                final CardsCollection cardsCollection = cardsCollectionInListAdapter.getCardsCollections().get(currentPosition);

                // delete
                cardsCollectionInListAdapter.deleteCardsCollection(currentPosition);

                new MaterialAlertDialogBuilder(CardsCollectionsListActivity.this)
                        .setTitle("Delete collection?")
                        .setMessage("All linked cards will be deleted.")
                        .setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cardsCollectionInListAdapter.insertCardsCollection(currentPosition, cardsCollection);
                            }
                        })
                        .setPositiveButton("delete", new DialogPositiveButtonOnClickListener(cardsCollection.getId()))
                        .setCancelable(false)
                        .show();

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(CardsCollectionsListActivity.this, R.color.colorDelete))
                        .addActionIcon(R.drawable.ic_delete_white_24dp)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        })).attachToRecyclerView(recyclerView);
    }


    protected void setOtherViews() {
        findViewById(R.id.activityCardsCollectionListButtonInsertCardsCollection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardsCollection cardsCollection = new CardsCollection(
                        "New collection", false, System.currentTimeMillis()
                );
                cardsCollection.setId(DataBaseHelper.createCardsCollection(cardsCollection));
                cardsCollectionInListAdapter.insertCardsCollection(0, cardsCollection);
            }
        });

        rootLayout = findViewById(R.id.activityCardsCollectionsListRootLayout);
        progressBar = findViewById(R.id.activityCardsCollectionsListProgressBar);
    }





    // class for delete cards collection dialog
    protected class DialogPositiveButtonOnClickListener implements DialogInterface.OnClickListener {
        private long cardsCollectionId;

        DialogPositiveButtonOnClickListener(long cardsCollectionId) {
            this.cardsCollectionId = cardsCollectionId;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            DataBaseHelper.deleteCardsCollection(cardsCollectionId);
        }
    }


    @SuppressLint("StaticFieldLeak")
    protected class AsyncTaskExecutor extends android.os.AsyncTask<Object, Integer, Void> {
        public static final int TASK_LOAD_CARDS_COLLECTIONS = 0;
        public static final int TASK_DOWNLOAD_COLLECTION_FROM_FILE = 1;

        public static final int ERROR_DOWNLOAD_COLLECTION_FROM_FILE = 0;

        private int currentTask;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rootLayout.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Object... objects) {
            // objects[0] - task id
            currentTask = (Integer) objects[0];

            if (currentTask == TASK_LOAD_CARDS_COLLECTIONS) {
                cardsCollectionInListAdapter.setCardsCollections(DataBaseHelper.getCardsCollections());
            }
            else if (currentTask == TASK_DOWNLOAD_COLLECTION_FROM_FILE) {
                try {
                    List<String> lines = Utils.linesFromStream(
                            getContentResolver().openInputStream((Uri) objects[1]));

                    CardsCollection cardsCollection = new CardsCollection(lines.get(0), false, System.currentTimeMillis());
                    cardsCollection.setId(DataBaseHelper.createCardsCollection(cardsCollection));

                    // .get(0) (first line) - name of collection
                    String[] term_definition;
                    for (int l = 1; l < lines.size() - 1; l++) {
                        term_definition = lines.get(l).split(" - ", 2);
                        if (term_definition.length > 1)
                            DataBaseHelper.addCard(new Card(term_definition[0], term_definition[1], cardsCollection.getId()));
                        else
                            publishProgress(ERROR_DOWNLOAD_COLLECTION_FROM_FILE);
                    }

                    cardsCollectionInListAdapter.insertCardsCollection(0, cardsCollection);

                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress(ERROR_DOWNLOAD_COLLECTION_FROM_FILE);
                }
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... integers) {
            super.onProgressUpdate(integers);


            if (currentTask == TASK_DOWNLOAD_COLLECTION_FROM_FILE && integers[0] == ERROR_DOWNLOAD_COLLECTION_FROM_FILE) {
                Toast.makeText(CardsCollectionsListActivity.this, "Error while downloading collection.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            rootLayout.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
