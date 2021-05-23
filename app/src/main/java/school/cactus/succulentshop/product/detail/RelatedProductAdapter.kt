package school.cactus.succulentshop.product.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import school.cactus.succulentshop.databinding.RelatedProductsBinding
import school.cactus.succulentshop.product.ProductItem

class RelatedProductAdapter : ListAdapter<ProductItem, RelatedProductAdapter.ProductHolder>(
    DIFF_CALLBACK
) {

    var itemClickListener: (ProductItem) -> Unit = {}

    class ProductHolder(
        private val binding: RelatedProductsBinding,
        private val itemClickListener: (ProductItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductItem) {
            binding.titleText.text = product.title
            binding.priceText.text = product.price

            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .override(512)
                .into(binding.imageView)

            binding.root.setOnClickListener {
                itemClickListener(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val binding = RelatedProductsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProductItem>() {
            override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem) =
                oldItem == newItem
        }
    }
}

