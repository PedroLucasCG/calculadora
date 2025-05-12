package com.example.calculadora

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal
import kotlin.math.*
import androidx.core.graphics.toColorInt

@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity() {
    private var isScientificMode = false
    private var readOnlyDisplay = false
    private var equation = ""
    private var isAltColumnOn = false
    private var isRootY = false
    private var isLogXBaseY = false
    private var bufferLogXBaseY = 1.0;
    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        var isExpOperation = false
        var lastEntrySize = 0;
        var memory: Double = 0.0
        val e = 2.7182818284590452353602874713527
        val pi = 3.1415926535897932384626433832795
        val display = findViewById<TextView>(R.id.textView)
        val displaySmall = findViewById<TextView>(R.id.textViewSmall)
        val digitButtons = listOf(
            R.id.digit0,
            R.id.digit1,
            R.id.digit2,
            R.id.digit3,
            R.id.digit4,
            R.id.digit5,
            R.id.digit6,
            R.id.digit7,
            R.id.digit8,
            R.id.digit9,
            R.id.decimal,
            R.id.pi,
            R.id.e,
            R.id.openParen,
            R.id.closeParen,
        ).map { findViewById<Button>(it) }
        val operationButtons = listOf(
            R.id.divide,
            R.id.multiply,
            R.id.plus,
            R.id.minus,
        ).map { findViewById<Button>(it) }
        val operators: Map<String, String> = mapOf(
            "+" to "+",
            "-" to "-",
            "×" to "*",
            "÷" to "/",
        )

        val actionButtonIds: Map<String, Int> = mapOf(
            "equals" to R.id.equals,
            "plusMinus" to R.id.plusMinus,
            "factorial" to R.id.factorial,
            "sqrt2" to R.id.sqrt2,
            "powerXY" to R.id.powerXY,
            "tenPowerX" to R.id.tenPowerX,
            "log" to R.id.log,
            "ln" to R.id.ln,
            "xSquared" to R.id.xSquared,
            "oneOverX" to R.id.oneOverX,
            "moduleOfX" to R.id.moduleOfX,
            "exp" to R.id.exp,
            "mod" to R.id.mod,
            "2nd" to R.id.nd,
            "c" to R.id.c,
            "backspace" to R.id.backspace,
            "MS" to R.id.MS,
            "MR" to R.id.MR,
            "MC" to R.id.MC,
            "M-" to R.id.Mminus,
            "M+" to R.id.Mplus,
            "DEG" to R.id.deg,
            "F-E" to R.id.FE,
        )

        val actionButtons: Map<String, Button> = actionButtonIds
            .mapValues { (_, id) -> findViewById<Button>(id) }

        try {
            actionButtons["equals"]?.setOnClickListener {
                if (!equation.isEmpty() && equation.last() == '.') {
                    equation += "0"
                }
                val openCount = equation.count { it == '(' }
                val closeCount = equation.count { it == ')' }
                if (openCount > closeCount) {
                    equation += ")".repeat(openCount - closeCount)
                }
                if (equation.endsWith("^")) {
                    equation += display.text.toString()
                }
                equation = equation.replace(oldValue = "e", newValue = e.toString())
                equation = equation.replace(oldValue = "π", newValue = pi.toString())
                if (isRootY) {
                    isRootY = false
                }
                if(isLogXBaseY) {
                    equation = equation.dropLast(display.text.length)
                    var number = display.text.toString()
                    number = number.replace(oldValue = "e", newValue = e.toString())
                    number = number.replace(oldValue = "π", newValue = pi.toString())
                    val result = log(bufferLogXBaseY, number.toDouble())
                    equation += "(" + formatResult(result) + ")"
                    displaySmall.text = equation
                    isLogXBaseY = false
                    bufferLogXBaseY = 1.0
                }
                val result = eval(equation)
                val roundedString = String.format("%.2f", result)
                val rounded = roundedString.toDouble()
                equation = rounded.toString()
                displaySmall.text = equation
                display.text = formatResult(rounded)
                readOnlyDisplay = true
            }

            actionButtons["2nd"]?.setOnClickListener {
                isAltColumnOn = !isAltColumnOn

                if (isAltColumnOn) {
                    actionButtons["xSquared"]?.text = "x³"
                    actionButtons["sqrt2"]?.text = "³√x"
                    actionButtons["powerXY"]?.text = "ⁿ√x"
                    actionButtons["tenPowerX"]?.text = "2ˣ"
                    actionButtons["log"]?.text = "logᵧ x"
                    actionButtons["ln"]?.text = "eˣ"
                    actionButtons["2nd"]?.setBackgroundColor("#8f281a".toColorInt())
                } else {
                    actionButtons["xSquared"]?.text = "x²"
                    actionButtons["sqrt2"]?.text = "²√x"
                    actionButtons["powerXY"]?.text = "xʸ"
                    actionButtons["tenPowerX"]?.text = "10ˣ"
                    actionButtons["log"]?.text = "log"
                    actionButtons["ln"]?.text = "ln"
                    actionButtons["2nd"]?.setBackgroundColor("#3d3d3d".toColorInt())
                }
            }

            actionButtons["plusMinus"]?.setOnClickListener {
                val number = display.text
                if (number[0] == '-') {
                    display.text = number.drop(1)
                    return@setOnClickListener
                }
                display.text = "-$number"
                if (readOnlyDisplay) {
                    equation = "-($equation)"
                    displaySmall.text = equation
                }
            }

            actionButtons["factorial"]?.setOnClickListener {
                val count = display.text.toString().toIntOrNull() ?: 0
                if (count < 0) {
                    equation = equation.dropLast(display.text.length)
                    display.text = "0"
                    displaySmall.text = equation
                    return@setOnClickListener
                }
                var total = 1
                for (c in 1 until count + 1) {
                    total *= c
                }
                equation = equation.dropLast(display.text.length)
                displaySmall.text = displaySmall.text.dropLast(display.text.length)
                displaySmall.text = displaySmall.text.toString() + total.toString()
                equation += total.toString()
                display.text = "0"
                readOnlyDisplay = true
            }

            actionButtons["sqrt2"]?.setOnClickListener {
                var number = display.text.toString()
                number = number.replace(oldValue = "e", newValue = e.toString())
                number = number.replace(oldValue = "π", newValue = pi.toString())
                if(actionButtons["sqrt2"]?.text == "²√x") {
                    display.text = sqrt(number.toDouble()).toString()
                } else {
                    display.text = cbrt(number.toDouble()).toString()
                }

            }

            actionButtons["powerXY"]?.setOnClickListener {
                if(actionButtons["powerXY"]?.text == "xʸ") {
                    equation += "^"
                } else {
                    equation += "^(1/"
                    isRootY = true
                }

                displaySmall.text = equation
                display.text = "0"
            }

            actionButtons["tenPowerX"]?.setOnClickListener {
                equation = equation.dropLast(display.text.length)
                var number = display.text.toString()
                number = number.replace(oldValue = "e", newValue = e.toString())
                number = number.replace(oldValue = "π", newValue = pi.toString())
                val result = if (actionButtons["tenPowerX"]?.text == "10ˣ") {
                    10.0.pow(number.toDouble())
                } else {
                    2.0.pow(number.toDouble())
                }
                equation += "(" + formatResult(result) + ")"
                displaySmall.text = equation
                display.text = result.toString()
                readOnlyDisplay = true
            }

            actionButtons["log"]?.setOnClickListener {
                equation = equation.dropLast(display.text.length)
                var number = display.text.toString()
                number = number.replace(oldValue = "e", newValue = e.toString())
                number = number.replace(oldValue = "π", newValue = pi.toString())
                if (actionButtons["log"]?.text == "log") {
                    val result = log(number.toDouble(), 10.0)
                    equation += "(" + formatResult(result) + ")"
                    display.text = "0"
                    displaySmall.text = equation
                } else {
                    bufferLogXBaseY = number.toDouble()
                    display.text = "0"
                    displaySmall.text = equation
                    isLogXBaseY = true
                }
            }

            actionButtons["ln"]?.setOnClickListener {
                equation = equation.dropLast(display.text.length)
                var input = display.text.toString()
                input = input.replace(oldValue = "e", newValue = e.toString())
                input = input.replace(oldValue = "π", newValue = pi.toString())
                val number = BigDecimal(input).toPlainString()
                val result = if (actionButtons["ln"]?.text == "ln") {
                    eval("log($number) / log($e)")
                } else {
                    equation = equation.dropLast(display.text.length)
                    e.pow(display.text.toString().toDouble())
                }

                equation += "(" + formatResult(result) + ")"
                display.text = "0"
                displaySmall.text = equation
                readOnlyDisplay = true
            }

            actionButtons["xSquared"]?.setOnClickListener {
                equation = equation.dropLast(display.text.length)
                var number = display.text.toString()
                number = number.replace(oldValue = "e", newValue = e.toString())
                number = number.replace(oldValue = "π", newValue = pi.toString())

                val result = if (actionButtons["xSquared"]?.text == "x²") {
                    number.toDouble().pow(2).toString()
                } else {
                    number.toDouble().pow(3).toString()
                }

                equation += result
                display.text = equation
                displaySmall.text = equation
                readOnlyDisplay = true
            }

            actionButtons["oneOverX"]?.setOnClickListener {
                equation = equation.dropLast(display.text.length)
                var number = display.text.toString()
                number = number.replace(oldValue = "e", newValue = e.toString())
                number = number.replace(oldValue = "π", newValue = pi.toString())
                val result = 1 / number.toDouble()
                equation += result.toString()
                display.text = equation
                displaySmall.text = equation
                readOnlyDisplay = true
            }

            actionButtons["moduleOfX"]?.setOnClickListener {
                equation = equation.dropLast(display.text.length)
                var number = display.text.toString()
                number = number.replace(oldValue = "e", newValue = e.toString())
                number = number.replace(oldValue = "π", newValue = pi.toString())
                var value = number.toDouble()
                if (value < 0) {
                    value *= -1
                }
                equation += value.toString()
                display.text = equation
                displaySmall.text = equation
                readOnlyDisplay = true
            }

            actionButtons["exp"]?.setOnClickListener {
                equation = equation.dropLast(display.text.length)
                val number = display.text.toString().toDouble()
                equation += "($number * e + 0)"
                display.text = "0"
                displaySmall.text = equation
                readOnlyDisplay = true
                isExpOperation = true
            }

            actionButtons["mod"]?.setOnClickListener {
                if (!equation.last().isDigit() && equation.last() != ')') {
                    return@setOnClickListener
                }
                equation = "$equation%"
                display.text = "0"
                displaySmall.text = equation
            }

            actionButtons["c"]?.setOnClickListener {
                if (actionButtons["c"]?.text == "CE") {
                    display.text = "0"
                    actionButtons["c"]?.text = "C"
                } else {
                    displaySmall.text = "0"
                    equation = ""
                }

                if (display.text.toString() != "0") {
                    actionButtons["c"]?.text = "CE"
                } else {
                    actionButtons["c"]?.text = "C"
                }

            }

            actionButtons["backspace"]?.setOnClickListener {
                display.text = display.text.dropLast(1)
                if (display.text.isEmpty()) {
                    display.text = "0"
                } else {
                    equation = equation.dropLast(1)
                }
            }

            actionButtons["MS"]?.setOnClickListener {
                memory = display.text.toString().toDouble()
            }

            actionButtons["MC"]?.setOnClickListener {
                memory = 0.0
            }

            actionButtons["MR"]?.setOnClickListener {
                if (memory != 0.0) {
                    display.text = "$memory"
                    equation = "$memory"
                    readOnlyDisplay = true
                }
            }

            actionButtons["M+"]?.setOnClickListener {
                memory += display.text.toString().toDouble()
            }

            actionButtons["M-"]?.setOnClickListener {
                memory -= display.text.toString().toDouble()
            }

            actionButtons["DEG"]?.setOnClickListener {
                if (actionButtons["DEG"]?.text == "DEG") {
                    actionButtons["DEG"]?.text = "GRAD"
                } else {
                    actionButtons["DEG"]?.text = "DEG"
                }
            }

            actionButtons["F-E"]?.setOnClickListener {
                isScientificMode = !isScientificMode
                if (isScientificMode) {
                    actionButtons["F-E"]?.setBackgroundColor("#8f281a".toColorInt())
                } else {
                    actionButtons["F-E"]?.setBackgroundColor("#3d3d3d".toColorInt())
                }
            }

            operationButtons.forEach { btn ->
                btn.setOnClickListener {
                    lastEntrySize = display.text.length
                    readOnlyDisplay = false
                    isExpOperation = false
                    if (equation.isNotEmpty() && equation.last() == '.') {
                        equation += "0"
                    }
                    if (
                         equation.isNotEmpty() &&
                        !equation.last().isDigit() &&
                         equation.last() != ')'    &&
                         equation.last() != 'e'    &&
                         equation.last() != 'π') {
                        return@setOnClickListener
                    }
                    if(isLogXBaseY) {
                        var number = display.text.toString()
                        number = number.replace(oldValue = "e", newValue = e.toString())
                        number = number.replace(oldValue = "π", newValue = pi.toString())
                        val result = log(bufferLogXBaseY, number.toDouble())
                        equation = equation.dropLast(display.text.length)
                        equation += "(" + formatResult(result) + ")" + operators[btn.text.toString()]
                        displaySmall.text = equation
                        display.text = "0"
                        isLogXBaseY = false
                        bufferLogXBaseY = 1.0
                        return@setOnClickListener
                    }
                    if (isRootY) {
                        equation += ")" + operators[btn.text.toString()]
                        isRootY = false
                        display.text = "0"
                        displaySmall.text = equation
                        return@setOnClickListener
                    }
                    equation += operators[btn.text.toString()]
                    display.text = "0"
                    displaySmall.text = equation
                }
            }

            digitButtons.forEach { btn ->
                btn.setOnClickListener {
                    try {
                        if (isExpOperation) {
                            equation = equation.dropLast(2)
                            equation += btn.text.toString() + ")"
                            displaySmall.text = equation
                            return@setOnClickListener
                        }

                        if (btn.text.toString() == ")" || btn.text.toString() == "(") {
                            val openCount = equation.count { it == '(' }
                            val closeCount = equation.count { it == ')' }

                            if (display.text.toString() == "0" && btn.text.toString() != ")") {
                                equation += "("
                                displaySmall.text = equation
                                return@setOnClickListener
                            }
                            if (openCount == closeCount && btn.text.toString() == ")") {
                                return@setOnClickListener
                            } else {
                                if (equation.last() == '.') {
                                    equation += "0"
                                }
                                equation += btn.text.toString()
                                displaySmall.text = equation
                                display.text = "0"
                                return@setOnClickListener
                            }
                        }

                        if (readOnlyDisplay &&
                            equation.isNotEmpty() &&
                            equation.last().isDigit()) {
                            equation = if (btn.text.toString() == ".") {
                                "0" + btn.text.toString()
                            } else {
                                btn.text.toString()
                            }
                            display.text = equation
                            displaySmall.text = "0"
                            readOnlyDisplay = false
                            return@setOnClickListener
                        } else if (readOnlyDisplay) {
                            readOnlyDisplay = false
                        }
                        if (display.text.toString() == "0" && btn.text.toString() != ".") {
                            display.text = btn.text
                            equation += btn.text.toString()
                            return@setOnClickListener
                        }

                        if (btn.text.toString() == ".") {
                            if (display.text.toString().contains('.')) {
                                return@setOnClickListener
                            }
                        }
                        equation += if (btn.text.toString() == "." && !equation.last().isDigit()) {
                            "0" + btn.text.toString()
                        } else {
                            btn.text.toString()
                        }
                        display.text = display.text.toString() + btn.text.toString()
                    } catch (e: Exception) {
                        Log.d("digit", e.toString())
                        readOnlyDisplay = true
                    } finally {
                        if (display.text.toString() != "0") {
                            actionButtons["c"]?.text = "CE"
                        } else {
                            actionButtons["c"]?.text = "C"
                        }
                    }

                }
            }

            val btnTrig: View = findViewById(R.id.btnTrig)
            btnTrig.setOnClickListener { view ->
                PopupMenu(this, view).apply {
                    menuInflater.inflate(R.menu.menu_trig, menu)
                    setOnMenuItemClickListener { item ->
                        var input = display.text.toString().toDoubleOrNull()
                            ?: return@setOnMenuItemClickListener false

                        if (actionButtons["DEG"]?.text.toString() == "DEG") {
                            input = Math.toRadians(input)
                        }

                        val result: Double? = when (item.itemId) {
                            R.id.sin -> sin(input)
                            R.id.cos -> cos(input)
                            R.id.tan -> tan(input)
                            R.id.sec -> 1.0 / cos(input)
                            R.id.csc -> 1.0 / sin(input)
                            R.id.cot -> 1.0 / tan(input)

                            R.id.asin -> asin(input)
                            R.id.acos -> acos(input)
                            R.id.atan -> atan(input)
                            R.id.asec -> acos(1.0 / input)
                            R.id.acsc -> asin(1.0 / input)
                            R.id.acot -> atan(1.0 / input)

                            else -> null
                        }

                        return@setOnMenuItemClickListener result?.let { value ->
                            val text = formatResult(value)
                            equation = equation.dropLast(display.text.length) + text
                            display.text = text
                            displaySmall.text = equation
                            readOnlyDisplay = true
                            true
                        } ?: false
                    }
                }.show()
            }

            val btnFunc: View = findViewById(R.id.btnFunc)
            btnFunc.setOnClickListener { view ->
                PopupMenu(this, view).apply {
                    menuInflater.inflate(R.menu.menu_function, menu)
                    setOnMenuItemClickListener { item ->
                        var input = display.text.toString().toDoubleOrNull()
                            ?: return@setOnMenuItemClickListener false

                        val result: Double? = when (item.itemId) {
                            R.id.module -> abs(input)
                            R.id.floor -> floor(input)
                            R.id.ceiling -> ceil(input)

                            R.id.rand -> Math.random()
                            R.id.dms -> {
                                val totalDeg = input
                                val d = totalDeg.toInt()
                                val mTotal = (totalDeg - d) * 60.0
                                val m = mTotal.toInt()
                                val s = ((mTotal - m) * 60.0)

                                d + m / 100.0 + s / 10000.0
                            }

                            R.id.deg -> Math.toDegrees(input)
                            else -> null
                        }
                        return@setOnMenuItemClickListener result?.let { value ->
                            val text = formatResult(value)
                            equation = equation.dropLast(display.text.length) + text
                            display.text = text
                            displaySmall.text = equation
                            readOnlyDisplay = true
                            true
                        } ?: false
                    }
                }.show()
            }
        } catch (_: Exception) {
            equation = ""
            displaySmall.text = "0"
            display.text = "0"
        }
    }
    private fun eval(expr: String): Double {
        try {
            val builder = ExpressionBuilder(expr)
            val expression = builder.build()
            return expression.evaluate()
        }catch (e: Exception) {
            Log.e("Calculo", e.toString())
            readOnlyDisplay = true
            equation = ""
            return 0.0
        }

    }
    private fun formatResult(value: Double): String {
        return if (isScientificMode) {
            String.format("%.2E", value)
        } else {
            value.toString()
        }
    }

}