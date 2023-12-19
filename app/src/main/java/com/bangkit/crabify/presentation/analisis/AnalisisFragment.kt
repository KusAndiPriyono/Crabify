package com.bangkit.crabify.presentation.analisis

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bangkit.crabify.R
import com.bangkit.crabify.data.model.Crab
import com.bangkit.crabify.databinding.FragmentAnalisisBinding
import com.bangkit.crabify.presentation.auth.login.LoginViewModel
import com.bangkit.crabify.presentation.upload.ClassificationViewModel
import com.bangkit.crabify.utils.UiState
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AnalisisFragment : Fragment() {

    private var _binding: FragmentAnalisisBinding? = null
    private val binding get() = _binding!!

    private lateinit var pieChart: PieChart

    //    private var tfRegular: Typeface? = null
//    private var tfLight: Typeface? = null
//
    private val authViewModel: LoginViewModel by viewModels()
    private val analisisViewModel: ClassificationViewModel by viewModels()
//
//    private val calendar: Calendar = Calendar.getInstance()
//    private val year = calendar.get(Calendar.YEAR)
//
//    private val statsTitle = arrayOf(
//        binding.tvCrabSoka,
//        binding.tvCrabBiasa
//    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalisisBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pieChart = binding.pieChart

        authViewModel.getSession { user ->
            analisisViewModel.getCrabs(user)
        }
        observerAnalisis()
    }

    private fun observerAnalisis() {
        analisisViewModel.crab.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    val crab = state.data
                    setPieChartData(crab)
                    configurePieChart()
                }

                is UiState.Error -> {
                }
            }
        }
    }


    private fun setPieChartData(crabs: List<Crab>) {
        val entries: ArrayList<PieEntry> = ArrayList()

        //adding data to it to display in pie chart
        for (crab in crabs) {
            if (crab.label.isNotEmpty() && crab.score.isNotEmpty()) {
                for (i in 0 until crab.label.size) {
                    entries.add(PieEntry(crab.score[i], crab.label[i]))
                }
            }
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        val colors: ArrayList<Int> = ArrayList()
        colors.add(ColorTemplate.rgb(R.color.md_green_600.toString()))
        colors.add(ColorTemplate.rgb(R.color.md_deep_orange_600.toString()))

        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        pieChart.data = data

        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun configurePieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.dragDecelerationFrictionCoef = 0.95f

        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)

        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        pieChart.setDrawCenterText(true)

        pieChart.rotationAngle = 0f

        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        pieChart.animateY(1400, Easing.EaseInOutQuad)

        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}