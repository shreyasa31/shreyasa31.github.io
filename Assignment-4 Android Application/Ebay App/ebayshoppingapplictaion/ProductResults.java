package com.example.ebayshoppingapplictaion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ProductResults extends AppCompatActivity  {
    private TextView noResultsTextView;

    private RecyclerView recyclerView;
    private  ProgressBar progressBar;
    private SearchAdapter adapter;
    private ImageView cart;
    private TextView searchProductsText;
    private List<SearchItem> searchItemList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_results);
        noResultsTextView= findViewById(R.id.noResultsTextView);
        ImageView img = findViewById(R.id.backArrow);
        progressBar = findViewById(R.id.progressBar);
        searchProductsText = findViewById(R.id.searchProductsText);
        // Initially, show the progress bar and hide the text view
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        String keyword = intent.getStringExtra("keyword");
        searchProductsText.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        adapter = new SearchAdapter(searchItemList, new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SearchItem item) {
                Intent intent = new Intent(ProductResults.this, DetailActivity.class);
                intent.putExtra("title", item.getTitle()); // Pass the title
                // Add any other product details you want to pass to the detail activity

                intent.putExtra("keyword", keyword);
                intent.putExtra("productUrl", item.getProductUrl());
                intent.putExtra("itemId",item.getItemId());
                intent.putExtra("ShippingType",item.getShippingType());
                intent.putExtra("zipcode",item.getZipcode());
                intent.putExtra("price",item.getPrice());
                intent.putExtra("image",item.getImage());
                intent.putExtra("condition",item.getCondition());


                startActivity(intent);
            }

            @Override
            public void onWishlistClick(SearchItem item) {
                if (item.isInWishlist()) {
                    // Call API to add item to wishlist

                    addToWishlist(item);
                } else {
                    // Call API to remove item from wishlist
                    removeFromWishlist(item);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        String category = intent.getStringExtra("category");
        String conditions = intent.getStringExtra("condition");
        String[] condition = conditions.split(",");
        boolean localpickuponly = intent.getBooleanExtra("localpickuponly", false);
        boolean freeshipping = intent.getBooleanExtra("freeshipping", false);
        String distance = intent.getStringExtra("distance");
        String buyerPostalCode = intent.getStringExtra("buyerPostalCode");

        String url = constructURL(keyword, category, condition, localpickuponly, freeshipping, Integer.parseInt(distance), buyerPostalCode);
        fetchSearchResults(url);
        // Handler to delay the display of the text
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Hide the progress bar and show the text
//                progressBar.setVisibility(View.GONE);
//                searchProductsText.setVisibility(View.GONE);
////                textView.setText("Hi");
////                textView.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.VISIBLE);
//            }
//        }, 2000); // 2000 ms delay
      img.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              finish();

          }
      });

    }

    private String trimmedTitle(String title) {
        int maxLength = 20; // Define the maximum length of the title
        if (title.length() > maxLength) {
            return title.substring(0, maxLength) + "...";
        } else {
            return title;
        }
    }

    private void addToWishlist(SearchItem item) {
        String baseUrl = "http://10.0.2.2:8080/addToWishlist?"; // Replace with your actual API URL

        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("ItemID", item.getItemId());
        builder.appendQueryParameter("image", item.getImage());
        builder.appendQueryParameter("title", item.getTitle());
        builder.appendQueryParameter("location", item.getZipcode());
        builder.appendQueryParameter("shipping", item.getShippingType());
        builder.appendQueryParameter("condition", item.getCondition());
        builder.appendQueryParameter("price", item.getPrice());


        String urlWithParams = builder.build().toString();
        Log.d("wishlistttttttttttttttttttttt", "URL: " + urlWithParams);
        Toast.makeText(this, trimmedTitle(item.getTitle())+"was added to wishlist", Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlWithParams, null,
                response -> {
                    // Handle response

                    Log.d("ProductResults", "Item added to wishlist");
                    item.setInWishlist(true);

                    adapter.notifyItemChanged(searchItemList.indexOf(item));
                },
                error -> {
                    // Handle error
                    Log.e("ProductResults", "Error: " + error.getMessage());
                });

        queue.add(jsonObjectRequest);

    }

    private void removeFromWishlist(SearchItem item) {
        String removeFromWishlistUrl = "http://10.0.2.2:8080/deleteWishlist?"; // Replace with your actual API endpoint

        // Construct the request URL with query parameters
        Uri.Builder builder = Uri.parse(removeFromWishlistUrl).buildUpon();
        builder.appendQueryParameter("itemID", item.getItemId());

        String urlWithParams = builder.build().toString();
        Log.d("deletewishlistttttttt", "URL: " + urlWithParams);
        RequestQueue queue = Volley.newRequestQueue(this);

        // Using StringRequest instead of JsonObjectRequest
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, urlWithParams,
                response -> {
                    // Handle successful response
                    item.setInWishlist(false);

                    int position = searchItemList.indexOf(item);
                    Toast.makeText(this, trimmedTitle(item.getTitle())+"was removed from wishlist", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                    Log.d("ProductResults", "Item removed from wishlist");
                },
                error -> {
                    // Handle error
                    Log.e("ProductResults", "Error removing item from wishlist: " + error.getMessage());
                });

        queue.add(stringRequest);
    }



    private String constructURL(String keyword, String category, String[] condition, boolean localpickuponly, boolean freeshipping, int distance, String buyerPostalCode) {
        // Construct the URL with user input
        String baseUrl = "http://10.0.2.2:8080/search";
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("keyword", keyword)
                .appendQueryParameter("category", category);
//        for (String c : condition) {
//            builder.appendQueryParameter("condition", c);
//        }
        StringBuilder conditionValue = new StringBuilder();
        for (String c : condition) {
            if (conditionValue.length() > 0) {
                conditionValue.append(",");
            }
            conditionValue.append(c);
        }
        builder.appendQueryParameter("condition", conditionValue.toString());
                builder.appendQueryParameter("localpickuponly", Boolean.toString(localpickuponly))
                .appendQueryParameter("freeshipping", Boolean.toString(freeshipping))
                .appendQueryParameter("distance", Integer.toString(distance))

               .appendQueryParameter("buyerPostalCode", buyerPostalCode);
        Log.d("thissss url productsss","prodyctshwhgw"+builder);
        return builder.build().toString();

    }

    private void fetchSearchResults(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
//                response -> {
//                    parseJSONAndPopulateList(response);
//                    progressBar.setVisibility(View.GONE);
//                    searchProductsText.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//                }, error -> {
                response -> {
                    Log.d("APIIIIIIIIIIIIIII Response", response.toString());
                    parseJSONAndPopulateList(response);
                    progressBar.setVisibility(View.GONE); // Hide progress bar when data is loaded
                    searchProductsText.setVisibility(View.GONE);
                    if (searchItemList.isEmpty()) {
                        noResultsTextView.setVisibility(View.VISIBLE);

                    } else {
                        noResultsTextView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }, error -> {
            progressBar.setVisibility(View.GONE);
            searchProductsText.setText("Failed to load data.");
            searchProductsText.setVisibility(View.VISIBLE);
            Log.e("ProductResults", "Error fetching data: " + error.getMessage());

            // Handle error
        });

        queue.add(jsonObjectRequest);
    }

    private void parseJSONAndPopulateList(JSONObject response) {
        // Clear the current list
        searchItemList.clear();

        try {
            // Parse JSON response and create SearchItem objects
            JSONObject message = response.getJSONObject("message");
            JSONArray items = message.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String image = item.getString("image");
                String title = item.getString("title");
                String zipcode = item.getString("zipcode");
                String shippingType = item.getString("shippingType");
                String condition = item.getString("condition");
                String price = item.getString("price");
                Log.d("ProductResultssssssssss", "Item " + i + ": " + title + ", " + zipcode + ", " + price);
                String productUrl = item.getString("viewItemURL");
                String itemId=item.getString("itemId");
                searchItemList.add(new SearchItem(image,title, zipcode, shippingType, condition, price,productUrl,itemId));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle the error
        }

        adapter.notifyDataSetChanged();
    }


}