package org.example.project.calculator.CalculatorApp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorMainScreen() {

    var input = remember { mutableStateOf("") }

    val listOfButtons = listOf(
        "C", "(", ")", "/",
        "7", "8", "9", "*",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "%", "0", ".", "="
    )
    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {

                TextField(
                    value = input.value,
                    onValueChange = { input.value = it },
                    modifier = Modifier.fillMaxWidth()
                )

            }

            Divider(modifier = Modifier.fillMaxWidth(), color = Color.Gray, thickness = 1.dp)

            Box(modifier = Modifier.weight(2f).fillMaxSize()) {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    items(listOfButtons) {

                        Button(
                            modifier = Modifier.fillMaxWidth().padding(5.dp),
                            shape = RectangleShape,
                            onClick = {
                                if(it == "C"){
                                    input.value = ""
                                }
                                else if(it == "="){
                                    input.value = evaluate(input.value).toString()
                                }

                                else{
                                input.value += it.toString()
                            }
                            },
                        ) {
                            Text(text = it)
                        }

                    }
                }

            }

        }
    }

}

fun evaluate(str: String): Double {

    data class Data(val rest: List<Char>, val value: Double)

    return object : Any() {

        fun parse(chars: List<Char>): Double {
            return getExpression(chars.filter { it != ' ' })
                .also { if (it.rest.isNotEmpty()) throw RuntimeException("Unexpected character: ${it.rest.first()}") }
                .value
        }

        private fun getExpression(chars: List<Char>): Data {
            var (rest, carry) = getTerm(chars)
            while (true) {
                when {
                    rest.firstOrNull() == '+' -> rest = getTerm(rest.drop(1)).also { carry += it.value }.rest
                    rest.firstOrNull() == '-' -> rest = getTerm(rest.drop(1)).also { carry -= it.value }.rest
                    else                      -> return Data(rest, carry)
                }
            }
        }

        private fun getTerm(chars: List<Char>): Data {
            var (rest, carry) = getFactor(chars)
            while (true) {
                when {
                    rest.firstOrNull() == '*' -> rest = getTerm(rest.drop(1)).also { carry *= it.value }.rest
                    rest.firstOrNull() == '/' -> rest = getTerm(rest.drop(1)).also { carry /= it.value }.rest
                    else                      -> return Data(rest, carry)
                }
            }
        }

        private fun getFactor(chars: List<Char>): Data {
            return when (val char = chars.firstOrNull()) {
                '+'              -> getFactor(chars.drop(1)).let { Data(it.rest, +it.value) }
                '-'              -> getFactor(chars.drop(1)).let { Data(it.rest, -it.value) }
                '('              -> getParenthesizedExpression(chars.drop(1))
                in '0'..'9', '.' -> getNumber(chars) // valid first characters of a number
                else             -> throw RuntimeException("Unexpected character: $char")
            }
        }

        private fun getParenthesizedExpression(chars: List<Char>): Data {
            return getExpression(chars)
                .also { if (it.rest.firstOrNull() != ')') throw RuntimeException("Missing closing parenthesis") }
                .let { Data(it.rest.drop(1), it.value) }
        }

        private fun getNumber(chars: List<Char>): Data {
            val s = chars.takeWhile { it.isDigit() || it == '.' }.joinToString("")
            return Data(chars.drop(s.length), s.toDouble())
        }

    }.parse(str.toList())

}