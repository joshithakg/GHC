package com.mapbox.navigation.examples.androidauto.app.search

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView

class AppSearchBottomSheet(
    val searchBottomSheetView: SearchBottomSheetView,
    val searchPlaceView: SearchPlaceBottomSheetView,
    val searchCategoriesView: SearchCategoriesBottomSheetView,
    val view: View,
    val savedInstanceState: Bundle?
) {
    private val configuration = if (view.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        SearchBottomSheetView.Configuration(
            collapsedStateAnchor = SearchBottomSheetView.CollapsedStateAnchor.SEARCH_BAR
        )
    } else {
        SearchBottomSheetView.Configuration()
    }

    private val cardsMediator = SearchViewBottomSheetsMediator(
        searchBottomSheetView,
        searchPlaceView,
        searchCategoriesView
    )

    private var placeClickListener: ((SearchPlace) -> Unit) = {}
    private var navigateClickListener: ((SearchPlace) -> Unit) = {}

    init {
        searchBottomSheetView.initializeSearch(savedInstanceState, configuration)
        searchBottomSheetView.isHideableByDrag = true
        savedInstanceState?.let {
            cardsMediator.onRestoreInstanceState(it)
        }
        cardsMediator.addSearchBottomSheetsEventsListener(
            object : SearchViewBottomSheetsMediator.SearchBottomSheetsEventsListener {
                override fun onOpenPlaceBottomSheet(place: SearchPlace) {
                    placeClickListener(place)
                }

                override fun onOpenCategoriesBottomSheet(category: Category) {}

                override fun onBackToMainBottomSheet() {}
            }
        )
    }

    fun handleOnBackPressed(): Boolean = cardsMediator.handleOnBackPressed()
    fun placeClickListener(placeClickListener: (SearchPlace) -> Unit) = apply {
        this.placeClickListener = placeClickListener
    }
    fun navigateClickListener(navigateClickListener: (SearchPlace) -> Unit) = apply {
        searchPlaceView.removeOnNavigateClickListener(this.navigateClickListener)
        searchPlaceView.removeOnShareClickListener(this.navigateClickListener)
        searchPlaceView.addOnNavigateClickListener { navigateClickListener(it) }
        searchPlaceView.addOnShareClickListener { navigateClickListener(it) }
        this.navigateClickListener = navigateClickListener
    }
    fun onSaveInstanceState(outState: Bundle) {
        cardsMediator.onSaveInstanceState(outState)
    }
    fun toggleVisibility(): Boolean {
        if (searchPlaceView.isHidden() && searchCategoriesView.isHidden()) {
            if (searchBottomSheetView.isHidden()) {
                searchBottomSheetView.open()
            } else {
                searchBottomSheetView.hide()
            }
            return true
        }
        return false
    }
}
