package simplex

enum class TableauResult(val text: String) {
    OPTIMAL("Решение оптимальное"),
    NOT_OPTIMAL("Решение не оптимальное"),
    UNBOUNDED("Функция неограничена"),
    NO_SOLUTION("Система не имеет решения"),
    NEGATIVE_B("В таблице есть отрицательные свободные коэффициенты"),
}
