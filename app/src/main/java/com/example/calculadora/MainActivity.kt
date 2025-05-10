package com.example.calculadora

import android.annotation.SuppressLint
import android.os.Bundle
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
import kotlin.math.sqrt

@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity() {
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

        var readOnlyDisplay = false
        var equation = ""
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
            "−" to "-",
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
        )

        val actionButtons: Map<String, Button> = actionButtonIds
            .mapValues { (_, id) -> findViewById<Button>(id) }

        actionButtons["equals"]?.setOnClickListener {
            val openCount  = equation.count { it == '(' }
            val closeCount = equation.count { it == ')' }
            if(openCount > closeCount){
                equation += ")".repeat(openCount - closeCount)
            }
            if (equation.endsWith("^")) {
                equation += display.text.toString()
            }
            val result = eval(equation)
            val roundedString = String.format("%.2f", result)
            val rounded = roundedString.toDouble()
            equation = rounded.toString()
            displaySmall.text = equation
            display.text = rounded.toString()
            readOnlyDisplay = true
        }

        actionButtons["plusMinus"]?.setOnClickListener {
            val number = display.text
            if (number[0] == '-') {
                display.text = number.drop(1)
                return@setOnClickListener
            }
            display.text = "-$number"
            equation = "-($equation)"
        }

        actionButtons["factorial"]?.setOnClickListener {
            val count = display.text.toString().toIntOrNull() ?: 0
            var total = 1
            for (c in 1 until count+1) {
                total *= c
            }
            equation = equation.dropLast(1)
            displaySmall.text = displaySmall.text.dropLast(1)
            displaySmall.text = displaySmall.text.toString() + total.toString()
            equation += total.toString()
            display.text = "0"
        }

        actionButtons["sqrt2"]?.setOnClickListener {
            val number = display.text.toString()
            display.text = sqrt(number.toDouble()).toString();
        }

        actionButtons["powerXY"]?.setOnClickListener {
            equation += "^"
            displaySmall.text = equation
            display.text = "0"
            readOnlyDisplay = true
        }

        actionButtons["tenPowerX"]?.setOnClickListener {
            equation = equation.dropLast(1)
            equation += "10^" + display.text.toString()
            displaySmall.text = equation
            display.text=  "0"
            readOnlyDisplay = true
        }

        actionButtons["log"]?.setOnClickListener {
            equation = equation.dropLast(1)
            val number = display.text.toString()
            equation += "log($number)"
            displaySmall.text = equation
            display.text=  "0"
            readOnlyDisplay = true
        }

        actionButtons["ln"]?.setOnClickListener {
            val input = display.text.toString()
            val number = BigDecimal(input).toPlainString()
            val result = eval("log($number) / log(2.7182818284590452353602874713527)")
            equation = result.toString()
            display.text = equation
            displaySmall.text = equation
            readOnlyDisplay = true
        }

        operationButtons.forEach  { btn ->
            btn.setOnClickListener {
                readOnlyDisplay = false
                display.text = "0"
                equation += operators[btn.text.toString()]
                displaySmall.text = equation
            }
        }

        digitButtons.forEach { btn ->
            btn.setOnClickListener {
                if (btn.text.toString() == ")" || btn.text.toString() == "(") {
                    val openCount  = equation.count { it == '(' }
                    val closeCount = equation.count { it == ')' }

                    if (display.text.toString() == "0") {
                        equation += "("
                        displaySmall.text = equation
                        return@setOnClickListener
                    }
                    if (openCount == closeCount && btn.text.toString() == ")") {
                        return@setOnClickListener
                    } else {
                        equation += btn.text.toString()
                        displaySmall.text = equation
                        return@setOnClickListener
                    }
                }

                if (readOnlyDisplay) {
                    equation = btn.text.toString()
                    display.text = equation
                    displaySmall.text = ""
                    readOnlyDisplay = false
                    return@setOnClickListener
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

                equation += btn.text.toString()
                display.text = display.text.toString() + btn.text.toString()
            }
        }

        val btnTrig: View = findViewById(R.id.btnTrig)
        btnTrig.setOnClickListener { view ->
            PopupMenu(this, view).apply {
                menuInflater.inflate(R.menu.menu_trig, menu)
                setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.deg  -> println()
                        R.id.rad  -> println()
                        R.id.grad -> println()
                    }
                    true
                }
            }.show()
        }

        val btnFunc: View = findViewById(R.id.btnFunc)
        btnFunc.setOnClickListener { view ->
            PopupMenu(this, view).apply {
                menuInflater.inflate(R.menu.menu_function, menu)
                setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.module  -> println()
                        R.id.floor  -> println()
                        R.id.ceiling -> println()

                        R.id.rand  -> println()
                        R.id.dms  -> println()
                        R.id.deg -> println()
                    }
                    true
                }
            }.show()
        }
    }
    private fun eval(expr: String, vars: Map<String, Double> = emptyMap()): Double {
        val builder = ExpressionBuilder(expr)
        vars.forEach { (name, value) -> builder.variable(name) }
        val expression = builder.build().apply { setVariables(vars) }
        return expression.evaluate()
    }

}