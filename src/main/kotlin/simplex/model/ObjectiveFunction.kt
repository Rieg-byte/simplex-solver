package simplex.model

data class ObjectiveFunction(
    val coefficients: List<Double>,
    val goal: Goal
)

enum class Goal {
    MAX, MIN
}
