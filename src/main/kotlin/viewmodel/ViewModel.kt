package viewmodel

import androidx.compose.runtime.collection.mutableVectorOf
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.flow
import model.Simplex

class ViewModel {
    private var simplex: Simplex? = null

    fun initSimplex(count: Int, constraints: Int, standardized: Array<FloatArray>) {
        simplex = Simplex(count, constraints)
        simplex?.fillTable(standardized)
    }

    fun calculate() = flow {
        var quit = false
        simplex?.let { it ->
            emit(it.table)
            while (!quit) {
                val err = it.compute()
                emit(it.table)
                if (err == Simplex.ERROR.IS_OPTIMAL) {
                    quit = true
                } else if (err == Simplex.ERROR.UNBOUNDED) {
                    quit = true
                }
            }
        }
    }

}