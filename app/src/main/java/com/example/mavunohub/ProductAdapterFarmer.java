package com.example.mavunohub;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapterFarmer extends RecyclerView.Adapter<ProductAdapterFarmer.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private OnProductClickListener onProductClickListener;

    public interface OnProductClickListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    // Constructor with OnProductClickListener parameter
    public ProductAdapterFarmer(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.onProductClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_farmer, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (productList == null || productList.isEmpty()) {
            Log.e("ProductAdapterFarmer", "Product list is empty or null!");
            return;
        }

        Product product = productList.get(position);
        if (product == null) {
            Log.e("ProductAdapterFarmer", "Product at position " + position + " is null!");
            return;
        }

        // Ensure views are not null before setting data
        if (holder.nameTv != null) {
            holder.nameTv.setText(product.getName());
        } else {
            Log.e("ProductAdapterFarmer", "nameTv is NULL in ViewHolder!");
        }

        if (holder.pricePerUnitTv != null) {
            holder.pricePerUnitTv.setText("KES " + product.getPricePerUnit() + " per " + product.getUnit());
        }

        if (holder.qtyTv != null) {
            holder.qtyTv.setText("Qty: " + product.getQuantity() + " " + product.getUnit());
        }

        // Load product image safely with Glide
        if (holder.productImageView != null) {
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context).load(product.getImageUrl()).into(holder.productImageView);
            } else {
                holder.productImageView.setImageResource(R.drawable.ic_imageplaceholder);
            }
        }

        // Prevent crashes from null buttons
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> onProductClickListener.onEditClick(product));
        }

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> onProductClickListener.onDeleteClick(product));
        }
    }

    @Override
    public int getItemCount() {
        return (productList != null) ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView, btnEdit, btnDelete;
        TextView nameTv, pricePerUnitTv, qtyTv;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.product_image_view);
            nameTv = itemView.findViewById(R.id.name_tv);
            pricePerUnitTv = itemView.findViewById(R.id.tvPricePerUnit);
            qtyTv = itemView.findViewById(R.id.qty_tv);
            btnEdit = itemView.findViewById(R.id.btn_edit_product);
            btnDelete = itemView.findViewById(R.id.btn_delete_product);

            // Log if any view is null (useful for debugging)
            if (productImageView == null) Log.e("ProductAdapterFarmer", "productImageView is NULL");
            if (nameTv == null) Log.e("ProductAdapterFarmer", "nameTv is NULL");
            if (pricePerUnitTv == null) Log.e("ProductAdapterFarmer", "pricePerUnitTv is NULL");
            if (qtyTv == null) Log.e("ProductAdapterFarmer", "qtyTv is NULL");
            if (btnEdit == null) Log.e("ProductAdapterFarmer", "btnEdit is NULL");
            if (btnDelete == null) Log.e("ProductAdapterFarmer", "btnDelete is NULL");
        }
    }
}
