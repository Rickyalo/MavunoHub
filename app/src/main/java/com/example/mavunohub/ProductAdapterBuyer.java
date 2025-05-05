package com.example.mavunohub;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapterBuyer extends RecyclerView.Adapter<ProductAdapterBuyer.ProductViewHolder> {

    private final Context context;
    private List<Product> productList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
    }

    public ProductAdapterBuyer(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = new ArrayList<>(productList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_buyer, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.nameTv.setText(product.getName());
        holder.pricePerUnitTv.setText("KES " + product.getPricePerUnit() + " per " + product.getUnit());
        holder.qtyTv.setText("Qty: " + product.getQuantity() + " " + product.getUnit());
        holder.totalPriceTv.setText("Total: KES " + String.format("%.2f", product.getTotalPrice()));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context).load(product.getImageUrl()).into(holder.productImageView);
        } else {
            holder.productImageView.setImageResource(R.drawable.ic_imageplaceholder);
        }

        holder.phoneTv.setText("Phone: " + (product.getPhone() != null ? product.getPhone() : "Not Available"));
        holder.locationTv.setText("Location: " +
                (product.getCounty() != null ? product.getCounty() : "Unknown") + ", " +
                (product.getSubCounty() != null ? product.getSubCounty() : ""));

        if (product.getQuantity() <= 0) {
            holder.qtyTv.setText("Out of Stock");
            holder.qtyTv.setTextColor(Color.RED);
            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setImageResource(R.drawable.ic_out_of_stock);
            holder.btnAddToCart.setBackgroundColor(Color.GRAY);
        } else {
            holder.qtyTv.setText("Qty: " + product.getQuantity() + " " + product.getUnit());
            holder.qtyTv.setTextColor(Color.BLACK);
            holder.btnAddToCart.setEnabled(true);
            holder.btnAddToCart.setImageResource(R.drawable.ic_add_to_cart);
            holder.btnAddToCart.setBackgroundColor(Color.GREEN);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null && product.getQuantity() > 0) {
                listener.onAddToCartClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        this.productList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView, btnAddToCart;
        TextView nameTv, pricePerUnitTv, qtyTv, totalPriceTv, phoneTv, locationTv;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.product_image_view);
            nameTv = itemView.findViewById(R.id.name_tv);
            pricePerUnitTv = itemView.findViewById(R.id.tvPricePerUnit);
            qtyTv = itemView.findViewById(R.id.qty_tv);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            totalPriceTv = itemView.findViewById(R.id.tvTotalPrice);
            phoneTv = itemView.findViewById(R.id.tvPhone);
            locationTv = itemView.findViewById(R.id.location_tv);
        }
    }
}