import kotlinx.coroutines.runBlocking
import simplex.*
import simplex.model.Constraint
import simplex.model.Goal
import simplex.model.ObjectiveFunction
import simplex.model.Sign

fun main(args: Array<String>) = runBlocking {
    val objectiveFunction = ObjectiveFunction(listOf(6.0, 2.0), Goal.MIN)
    val constraints = listOf(
        Constraint(listOf(6.0, 2.0), Sign.LE, 8.0),
        Constraint(listOf(1.0, 3.0), Sign.LE, 6.0),
        Constraint(listOf(1.0, 1.0), Sign.GE, 16.0)
    )
    val simplex = Simplex(
        objectiveFunction = objectiveFunction,
        constraints = constraints,
        numVars = 2
    )
    simplexSolve(simplex)
}


suspend fun simplexSolve(simplex: Simplex) {
    simplex.solve().collect{ println(it) }
}