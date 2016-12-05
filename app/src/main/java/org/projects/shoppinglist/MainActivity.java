package org.projects.shoppinglist;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("items");
    FirebaseListAdapter<Product> mAdapter;
    Product lastDeletedProduct;
    int lastDeletedPosition;

    public void saveLastDeletedProduct(){
        lastDeletedPosition = listView.getCheckedItemPosition();
        lastDeletedProduct = getItem(lastDeletedPosition);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.list);

        Button addButton = (Button) findViewById(R.id.addButton);
        final EditText basketItemField = (EditText) findViewById(R.id.basketItemField);

        final EditText quantity = (EditText) findViewById(R.id.quantityField);

        mAdapter = new FirebaseListAdapter<Product>(this, Product.class, android.R.layout.simple_list_item_checked, ref){

            @Override
            protected void populateView(View v, Product product, int position) {
                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(product.toString());
            }
        };

        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Product product = new Product();
                product.quantity = Integer.parseInt(quantity.getText().toString());
                product.name = basketItemField.getText().toString();
                ref.push().setValue(product);

                getMyAdapter().notifyDataSetChanged();
                basketItemField.setText("");
                quantity.setText("");
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton fab = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.deleteSelected);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(listView.getCheckedItemCount() > 0) {
                    saveLastDeletedProduct();
                    int index = listView.getCheckedItemPosition();

                    getMyAdapter().getRef(index).setValue(null);
                    getMyAdapter().notifyDataSetChanged();

                    Snackbar snackbar = Snackbar.make(listView, "Item Deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener(){
                               @Override
                                public void onClick(View view){
                                   ref.push().setValue(lastDeletedProduct);
                                   getMyAdapter().notifyDataSetChanged();
                                   Snackbar snackbar = Snackbar.make(listView, "Item restored!", Snackbar.LENGTH_SHORT);
                                   snackbar.show();
                               }
                            });
                    snackbar.show();
                    listView.clearChoices();
                }
            }
        });

        com.getbase.floatingactionbutton.FloatingActionButton searchFab = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.search);
        searchFab.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v) {
               if (listView.getCheckedItemCount() > 0) {
                   Product selectedProduct = (Product) mAdapter.getItem(listView.getCheckedItemPosition());

                   Uri uri = Uri.parse("http://www.google.com/#q=" + "where to buy " +selectedProduct.getName());
                   Intent searchIntent = new Intent(Intent.ACTION_VIEW, uri);
                   startActivity(searchIntent);
               }
           }
        });


        if(MyPreferenceFragment.getName(this) != "" && MyPreferenceFragment.getName(this) != null){
            Toast toast = Toast.makeText(this, "Welcome, "+ MyPreferenceFragment.getName(this), Toast.LENGTH_LONG);
            toast.show();
        }

    }

    public FirebaseListAdapter getMyAdapter(){
        return mAdapter;
    }

    public Product getItem(int index){
        return (Product) getMyAdapter().getItem(index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        if(requestCode==1){
            String name = MyPreferenceFragment.getName(this);
            String message = "Welcome, "+name;
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            toast.show();
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()){
            case R.id.delete_all:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                for (int i = 0; i< getMyAdapter().getCount(); i++){
                                    getMyAdapter().getRef(i).setValue(null);
                                }
                                getMyAdapter().notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "List is now reset!", Toast.LENGTH_SHORT).show();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                Toast.makeText(getApplicationContext(), "Nothing happened!", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you want to clear the entire list?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                return true;
            case R.id.send_list:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, convertListToString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
        };

        return super.onOptionsItemSelected(item);
    }

    public String convertListToString(){
        String res = "";

        for(int i =0; i<mAdapter.getCount(); i++){
            Product p = mAdapter.getItem(i);
            res = res +"- " + p.getQuantity() + " " + p.getName() + "\n";
        }
        return res;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mAdapter.cleanup();
    }
}
