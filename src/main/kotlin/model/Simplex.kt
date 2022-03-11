package model

class Simplex(numOfConstraints: Int, numOfUnknowns: Int) {
    private val rows: Int
    // row and column
    private val cols: Int

    // returns the simplex tableau
    val table // simplex tableau
            : Array<FloatArray?>
    private var solutionIsUnbounded = false

    enum class ERROR {
        NOT_OPTIMAL, IS_OPTIMAL, UNBOUNDED
    }

    // prints out the simplex tableau
    @Deprecated(message = "old way to display table info", level = DeprecationLevel.HIDDEN)
    fun print() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val value = String.format("%.2f", table[i]!![j])
                print(value + "\t")
            }
            println()
        }
        println()
    }

    // fills the simplex tableau with coefficients
    fun fillTable(data: Array<FloatArray>) {
        for (i in table.indices) {
            table[i] = data[i].copyOf()
            //System.arraycopy(data[i], 0, table[i], 0, data[i].size)
        }
    }

    // computes the values of the simplex tableau
    // should be use in a loop to continously compute until
    // an optimal solution is found
    fun compute(): ERROR {
        // step 1
        if (checkOptimality()) {
            return ERROR.IS_OPTIMAL // solution is optimal
        }

        // step 2
        // find the entering column
        val pivotColumn = findEnteringColumn()
        println("Pivot Column: $pivotColumn")

        // step 3
        // find departing value
        val ratios = calculateRatios(pivotColumn)
        if (solutionIsUnbounded) return ERROR.UNBOUNDED
        val pivotRow = ratios.indexOfLast { it == ratios.filter { el -> el > 0 }.minOrNull() }

        // step 4
        // form the next tableau
        formNextTableau(pivotRow, pivotColumn)

        // since we formed a new table so return NOT_OPTIMAL
        return ERROR.NOT_OPTIMAL
    }

    // Forms a new tableau from precomuted values.
    private fun formNextTableau(pivotRow: Int, pivotColumn: Int) {
        val pivotValue = table[pivotRow]!![pivotColumn]
        val pivotRowVals = table[pivotRow]!!.copyOf()
        val pivotColumnVals = FloatArray(cols)
        val rowNew = FloatArray(cols)

        // divide all entries in pivot row by entry inpivot column
        // get entry in pivot row

        // get entry inpivot colum
        for (i in 0 until rows) pivotColumnVals[i] = table[i]!![pivotColumn]

        // divide values in pivot row by pivot value
        for (i in 0 until cols) rowNew[i] = pivotRowVals[i] / pivotValue

        // subtract from each of the other rows
        for (i in 0 until rows) {
            if (i != pivotRow) {
                for (j in 0 until cols) {
                    val c = pivotColumnVals[i]
                    table[i]!![j] = table[i]!![j] - c * rowNew[j]
                }
            }
        }

        // replace the row
        table[pivotRow] = rowNew.copyOf()
    }

    // calculates the pivot row ratios
    private fun calculateRatios(column: Int): FloatArray {
        val positiveEntries = FloatArray(rows)
        val res = FloatArray(rows)
        var allNegativeCount = 0
        for (i in 0 until rows) {
            if (table[i]!![column] > 0) {
                positiveEntries[i] = table[i]!![column]
            } else {
                positiveEntries[i] = 0F
                allNegativeCount++
            }
        }
        if (allNegativeCount == rows) {
            solutionIsUnbounded = true
        } else {
            for (i in 0 until rows) {
                val value = positiveEntries[i]
                if (value > 0) {
                    res[i] = table[i]!![cols - 1] / value
                }
            }
        }
        return res
    }

    // finds the next entering column
    private fun findEnteringColumn(): Int {
        val min = table.last()?.filter { it < 0 }?.minOrNull()
        if (min != null) {
            return table.last()?.indexOfLast { it == min }!!
        } else throw Exception()
    }

    // checks if the table is optimal
    private fun checkOptimality(): Boolean {
        return table.last()?.all {
            it >= 0
        } ?: false
    }

    init {
        rows = numOfConstraints + 1 // row number + 1
        cols = numOfUnknowns + 1 // column number + 1
        table = arrayOfNulls(rows) // create a 2d array

        // initialize references to arrays
        for (i in 0 until rows) {
            table[i] = FloatArray(cols)
        }
    }
}