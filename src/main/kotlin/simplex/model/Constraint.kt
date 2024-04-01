package simplex.model

data class Constraint(
    val coefficients: List<Double>,
    val sign: Sign,
    val b: Double
)

enum class Sign {
    EQ, LE, GE
}
