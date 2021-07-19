package com.example.petsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import android.widget.Toast;


import com.example.petsapp.data.PetContract.PetEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    PetCursorAdapter mPetCursorAdapter;
    public static final int PET_LOADER = 1;
    private Cursor tmpCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("on create called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        ListView petListView = findViewById(R.id.list_view);
        View emptyView = (RelativeLayout)findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        mPetCursorAdapter = new PetCursorAdapter(this, null);

        petListView.setAdapter(mPetCursorAdapter);
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(PET_LOADER, null, this);

    }
    //Example method for using of selection and selection args
    private void updatePet() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Milo");
        values.put(PetEntry.COLUMN_PET_BREED, "French Bulldog");
        values.put(PetEntry.COLUMN_PET_WEIGHT, 20);
        String selection = PetEntry.COLUMN_PET_NAME + "=?";
        String[] selectionArgs = new String[] {"Toto"};
        int rows = getContentResolver().update(PetEntry.CONTENT_URI,  values, selection, selectionArgs);
        Toast.makeText(this, rows + " rows effected", Toast.LENGTH_SHORT).show();
    }


    private void insertPet() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        String[] columns = {PetEntry._ID, PetEntry.COLUMN_PET_WEIGHT};
        MenuItem menuItem = menu.findItem(R.id.action_delete_all_entries);

        if(tmpCursor.getCount() == 0)
            menuItem.setVisible(false);
        else menuItem.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllPets() {
        getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete all pets?");
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable  Bundle args) {
        System.out.println("On create loader called " + CatalogActivity.class.getSimpleName() + ": " + id);
        String[] columns = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        CursorLoader cursorLoader = new CursorLoader(this, PetEntry.CONTENT_URI, columns, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        System.out.println("on load finished called " + data.toString() + CatalogActivity.class.getSimpleName());
        tmpCursor = data;
        mPetCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        System.out.println("on loader reset  called");
        //this method called when the data of current cursor is deleted or invalid
        mPetCursorAdapter.swapCursor(null);
    }
}