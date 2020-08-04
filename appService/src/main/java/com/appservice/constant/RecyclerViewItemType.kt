package com.appservice.constant

import androidx.annotation.LayoutRes
import com.appservice.R

enum class RecyclerViewItemType {
  PAGINATION_LOADER,
  SPECIFICATION_ITEM,
  IMAGE_PREVIEW,
  ADDITIONAL_FILE_VIEW;

  @LayoutRes
  fun getLayout(): Int {
    return when (this) {
      PAGINATION_LOADER -> R.layout.pagination_loader
      SPECIFICATION_ITEM -> R.layout.row_layout_added_specs
      IMAGE_PREVIEW -> R.layout.item_preview_image
      ADDITIONAL_FILE_VIEW -> R.layout.item_pdf_file
    }
  }
}
