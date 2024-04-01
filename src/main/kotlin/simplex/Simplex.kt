package simplex

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import simplex.model.*

class Simplex(
    val objectiveFunction: ObjectiveFunction,
    val constraints: List<Constraint>,
    val numVars: Int
) {
    private val tableau: MutableList<MutableList<Double>> = mutableListOf()
    private val basis: MutableList<Int> = MutableList(constraints.size) { 0 }
    private val c = objectiveFunction.coefficients
    private val goal = objectiveFunction.goal
    private val addVars = constraints.count { it.sign != Sign.EQ }
    private val countCol = numVars + addVars + 1
    private val countRow = basis.size + 1

    init {
        if (!constraints.all { it.coefficients.size == numVars }) {
            throw Exception()
        }
        formTableau()
    }

    private fun formTableau() {
        convertToCanonForm()
        formInitialBasis()
        addRow(objectiveFunction.coefficients.map { -it }, 0.0)
    }

    /*
    Формирование начального базиса
     */
    private fun formInitialBasis() {
        var countBasisVars = 0
        val maxCountBasisVars = basis.size
        for (i in 0..<countCol - 1) {
            if (countBasisVars == maxCountBasisVars) break
            val currentCol = tableau.map { it[i] }
            val countNonZero = currentCol.count { it != 0.0 }
            if (countNonZero == 1) {
                val indexRow = currentCol.indexOfFirst { it != 0.0 }
                if (currentCol[indexRow] == 1.0) {
                    basis[indexRow] = i + 1
                } else {
                    basis[indexRow] = i + 1
                    tableau[indexRow] = tableau[indexRow].map { it / currentCol.first() }.toMutableList()
                    println(tableau[indexRow])
                }
                countBasisVars++
            }
        }
        for (i in 0..<countCol - 1) {
            if (countBasisVars == maxCountBasisVars) break
            val currentCol = tableau.map { it[i] }
            val countNonZero = currentCol.count { it == 0.0 }
            if (countNonZero == 0) {
                val indexBasis = basis.indexOfFirst { it == 0 }
                basis[indexBasis] = i + 1
                tableau[indexBasis] = tableau[indexBasis].map { it / currentCol[indexBasis] }.toMutableList()
                tableau.forEachIndexed { row, _ ->
                    if (row != indexBasis) {
                        tableau[row] = tableau[row].mapIndexed{ col, it ->
                            it - tableau[indexBasis][col] * tableau[row][i] }.toMutableList()
                    }
                }
                countBasisVars++
            }
        }
    }

    /*
    Преобразование в каноническую форму
     */
    private fun convertToCanonForm() {
        var indexAddVar = numVars - 1
        var coefficients: List<Double>
        var b: Double
        for (constraint in constraints) {
            when(constraint.sign) {
                Sign.GE -> {
                    coefficients = constraint.coefficients.map { -it }
                    b = -constraint.b
                    indexAddVar++
                    addRow(coefficients, b, indexAddVar)
                }
                Sign.LE -> {
                    coefficients = constraint.coefficients
                    b = constraint.b
                    indexAddVar++
                    addRow(coefficients, b, indexAddVar)
                }
                Sign.EQ -> {
                    coefficients = constraint.coefficients
                    b = constraint.b
                    addRow(coefficients, b)
                }
            }
        }
    }

    /*
    Добавление строки в таблицу
     */
    private fun addRow(coefficients: List<Double>, b: Double, indexAddVar: Int? = null) {
        val row: MutableList<Double> = MutableList(countCol) { 0.0 }
        for (i in 0..<countCol) {
            if (i < coefficients.size) row[i] = coefficients[i]
            else if (indexAddVar != null && i == indexAddVar) row[i] = 1.0
            else if (i == countCol - 1) row[i] = b
            else continue
        }
        tableau.add(row)
    }

    /*
    Проверка на наличие отрицательных свободных коэффициентов (b)
     */
    private fun checkNegativeB(): Boolean = tableau.dropLast(1).any {it.last() < 0 }

    /*
    Перерасчёт таблицы
     */
    private fun recalculate(pivotElement: PivotElement) {
        val pivotRow = pivotElement.row
        val pivotCol = pivotElement.col
        basis[pivotElement.row] = pivotElement.col + 1
        for (i in 0..<countRow) {
            for (j in 0..<countCol) {
                if (i != pivotRow && j != pivotCol) {
                    tableau[i][j] = tableau[i][j] - tableau[i][pivotCol] * tableau[pivotRow][j] / tableau[pivotRow][pivotCol]
                }
            }
        }
        for (j in 0..<countCol) {
            if (j != pivotCol) {
                tableau[pivotRow][j] = tableau[pivotRow][j] / tableau[pivotRow][pivotCol]
            }
        }
        for (i in 0..<countRow) {
            if (i != pivotRow) {
                tableau[i][pivotCol] = 0.0
            } else {
                tableau[i][pivotCol] = 1.0
            }
        }
    }

    /*
    Поиск разрещающего элемента
     */
    private fun findPivotElement(containsNegativeB: Boolean = false): PivotElement? {
        return if (containsNegativeB) {
            val pivotRow: Int = findPivotRow()
            val pivotCol: Int? = findPivotCol(pivotRow)
            pivotCol?.let { PivotElement(row = pivotRow, col = pivotCol) }
        } else {
            val pivotCol: Int = findPivotCol()
            val pivotRow: Int? = findPivotRow(pivotCol)
            pivotRow?.let { PivotElement(row = pivotRow, col = pivotCol) }
        }
    }

    /*
    Поиск разрещающей строки
     */
    private fun findPivotRow(): Int {
        val b = tableau.dropLast(1).map { it.last() }
        val min = b.filter { it < 0 }.min()
        return b.indexOf(min)
    }

    private fun findPivotRow(pivotCol: Int): Int? {
        val b = tableau.dropLast(1).map { it.last() / it[pivotCol] }
        val min: Double? = b.filter { it > 0 }.minOrNull()
        return min?.let { b.indexOf(min) }
    }

    /*
    Поиск разрещающего столбца
     */
    private fun findPivotCol(): Int {
        return when (goal) {
            Goal.MAX -> {
                val min = tableau.last().dropLast(1).filter { it < 0 }.min()
                tableau.last().dropLast(1).indexOf(min)
            }
            Goal.MIN -> {
                val max = tableau.last().dropLast(1).filter { it > 0 }.max()
                tableau.last().dropLast(1).indexOf(max)
            }
        }
    }

    private fun findPivotCol(minRow: Int): Int? {
        val b = tableau[minRow].dropLast(1)
        val min = b.filter { it < 0 }.minOrNull()
        return min?.let { b.indexOf(min) }
    }

    /*
    Проверка на оптимальность
     */
    private fun checkOptimality(): Boolean {
        return when (goal) {
            Goal.MAX -> tableau.last().dropLast(1).all { it >= 0 }
            Goal.MIN -> tableau.last().dropLast(1).all { it <= 0 }
        }
    }

    /*
    Получение ответа
     */
    private fun getAnswer(): Answer {
        val vars: MutableList<Double> = mutableListOf()
        val f = tableau.last().last()
        for (i in c.indices) {
            val x = i + 1
            val value = if (x in basis) tableau[basis.indexOf(x)].last() else 0.0
            vars.add(value)
        }
        return Answer(vars, f)
    }

    /*
    Проверка таблицы
     */
    private fun checkTableau(): SimplexTableau {
        val simplexTableau = SimplexTableau(tableau, basis)
        if (checkNegativeB()) {
            val pivotElement = findPivotElement(checkNegativeB())
                ?: return simplexTableau.copy(tableauState = TableauResult.NO_SOLUTION)
            return simplexTableau.copy(pivotElement = pivotElement, tableauState = TableauResult.NEGATIVE_B)
        } else {
            if (checkOptimality()) {
                val answer = getAnswer()
                return simplexTableau.copy(tableauState = TableauResult.OPTIMAL, answer = answer)
            } else {
                val pivotElement = findPivotElement()
                    ?: return simplexTableau.copy(tableauState = TableauResult.UNBOUNDED)
                return simplexTableau.copy(pivotElement = pivotElement, tableauState = TableauResult.NOT_OPTIMAL)
            }
        }
    }

    fun solve(): Flow<SimplexTableau> = flow {
        var simplexTableau = checkTableau()
        var simplexResult = simplexTableau.tableauState
        emit(simplexTableau)
        while (simplexResult == TableauResult.NOT_OPTIMAL || simplexResult == TableauResult.NEGATIVE_B) {
            simplexTableau.pivotElement?.let { recalculate(it) }
            simplexTableau = checkTableau()
            simplexResult = simplexTableau.tableauState
            emit(simplexTableau)
        }
    }
}