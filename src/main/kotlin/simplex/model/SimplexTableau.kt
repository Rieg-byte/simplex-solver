package simplex.model

import simplex.TableauResult

data class SimplexTableau(
    val tableau: List<List<Double>>,
    val basis: List<Int>,
    val pivotElement: PivotElement? = null,
    val tableauState: TableauResult? = null,
    val answer: Answer? = null
) {
    override fun toString() = buildString {
        append("Базис\t")
        for (i in 0..<tableau.first().size - 1) append("x${i + 1}\t\t")
        append("B\n")
        for (i in tableau.indices) {
            if (i < basis.size) append("x${basis[i]}\t\t") else append("F\t\t")
            tableau[i].forEach { append("%.2f\t".format(it)) }
            append("\n")
        }
        tableauState?.let {
            append(tableauState.text)
        }
        append("\n")
        pivotElement?.let {
            append("Разрещающий элемент: [${pivotElement.col}][${pivotElement.row}]")
        }
        answer?.let {
            it.vars.forEachIndexed { i, value -> append("x${i + 1} = %.2f\t".format(value))  }
            append("\n")
            append("%.2f".format(it.f))
        }
    }
}
