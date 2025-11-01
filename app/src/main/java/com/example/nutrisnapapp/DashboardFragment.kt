package com.example.nutrisnapapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nutrisnapapp.viewmodel.UserStatsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


class DashboardFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var tvConsumed: TextView
    private lateinit var tvBurned: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvGoalStatus: TextView
    private val statsViewModel: UserStatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        barChart = view.findViewById(R.id.barChartWeekly)
        pieChart = view.findViewById(R.id.pieChartMacros)
        tvConsumed = view.findViewById(R.id.tvConsumed)
        tvBurned = view.findViewById(R.id.tvBurned)
        tvRemaining = view.findViewById(R.id.tvRemaining)
        tvGoalStatus = view.findViewById(R.id.tvGoalStatus)

        setupBarChart()
        setupPieChart()

        return view
    }

    private fun setupBarChart() {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 1800f))
        entries.add(BarEntry(2f, 1500f))
        entries.add(BarEntry(3f, 2000f))
        entries.add(BarEntry(4f, 1700f))
        entries.add(BarEntry(5f, 1900f))
        entries.add(BarEntry(6f, 1600f))
        entries.add(BarEntry(7f, 2100f))

        val dataSet = BarDataSet(entries, "Calories per day")
        dataSet.color = Color.parseColor("#FF9800")
        val data = BarData(dataSet)
        data.barWidth = 0.5f

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun setupPieChart() {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(25f, "Protein"))
        entries.add(PieEntry(50f, "Carbs"))
        entries.add(PieEntry(25f, "Fats"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#FF9800")
        )

        val data = PieData(dataSet)
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
