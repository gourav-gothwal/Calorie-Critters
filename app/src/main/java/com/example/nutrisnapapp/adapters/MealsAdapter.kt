package com.example.nutrisnapapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutrisnapapp.R
import com.example.nutrisnapapp.data.models.RecipeItem


class MealAdapter(
    private val meals: List<RecipeItem>,
    private val onClick: (RecipeItem) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.imageViewMeal)
        val mealName: TextView = itemView.findViewById(R.id.textViewMealName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_card, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealName.text = meal.title
        Glide.with(holder.itemView.context)
            .load(meal.image)
            .into(holder.mealImage)

        holder.itemView.setOnClickListener {
            onClick(meal)
        }
    }

    override fun getItemCount(): Int = meals.size
}
