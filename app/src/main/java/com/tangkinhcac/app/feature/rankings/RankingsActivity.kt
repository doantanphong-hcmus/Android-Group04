package com.tangkinhcac.app.feature.rankings

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.tangkinhcac.app.R
import com.tangkinhcac.app.feature.library.LibrarySearchCatalog
import com.tangkinhcac.app.ui.applySystemBarPadding
import java.util.Locale

class RankingsActivity : AppCompatActivity() {
    private var selectedPeriod = RankingsPeriod.WEEK
    private var selectedMetric = RankingsMetric.READ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rankings)

        findViewById<View>(R.id.rankings_root).applySystemBarPadding()
        findViewById<View>(R.id.rankings_back_button).setOnClickListener { finish() }
        bindPeriodControls()
        bindMetricControls()
        renderRankings()
    }

    private fun bindPeriodControls() {
        val controls =
            mapOf(
                R.id.rankings_period_day to RankingsPeriod.DAY,
                R.id.rankings_period_week to RankingsPeriod.WEEK,
                R.id.rankings_period_month to RankingsPeriod.MONTH,
            )
        controls.forEach { (viewId, period) ->
            findViewById<View>(viewId).setOnClickListener {
                selectedPeriod = period
                controls.forEach { (id, value) -> findViewById<View>(id).isSelected = value == selectedPeriod }
                renderRankings()
            }
        }
        controls.forEach { (id, value) -> findViewById<View>(id).isSelected = value == selectedPeriod }
    }

    private fun bindMetricControls() {
        val controls =
            mapOf(
                R.id.rankings_metric_read to RankingsMetric.READ,
                R.id.rankings_metric_listen to RankingsMetric.LISTEN,
            )
        controls.forEach { (viewId, metric) ->
            findViewById<View>(viewId).setOnClickListener {
                selectedMetric = metric
                controls.forEach { (id, value) -> findViewById<View>(id).isSelected = value == selectedMetric }
                renderRankings()
            }
        }
        controls.forEach { (id, value) -> findViewById<View>(id).isSelected = value == selectedMetric }
    }

    private fun renderRankings() {
        val booksById = LibrarySearchCatalog.books.associateBy { book -> book.id }
        val container = findViewById<LinearLayout>(R.id.rankings_entries)
        container.removeAllViews()

        RankingsMockData.rankings(selectedPeriod, selectedMetric).forEachIndexed { index, entry ->
            val book = checkNotNull(booksById[entry.bookId])
            val row = layoutInflater.inflate(R.layout.item_ranking, container, false)

            row.findViewById<TextView>(R.id.ranking_position).text =
                getString(R.string.rankings_position, index + 1)
            row.findViewById<ImageView>(R.id.ranking_cover).apply {
                setImageResource(book.coverResId)
                contentDescription = book.title
            }
            row.findViewById<TextView>(R.id.ranking_title).apply {
                text = book.title
                setTypeface(typeface, Typeface.BOLD)
            }
            row.findViewById<TextView>(R.id.ranking_author).text = book.author
            row.findViewById<TextView>(R.id.ranking_count).text =
                getString(
                    if (selectedMetric == RankingsMetric.READ) {
                        R.string.rankings_read_count
                    } else {
                        R.string.rankings_listen_count
                    },
                    entry.count.toCompactCount(),
                )
            container.addView(row)
        }
    }

    private fun Int.toCompactCount(): String =
        if (this < 1_000) {
            toString()
        } else {
            String.format(Locale.US, "%.1fK", this / 1_000f)
        }
}
