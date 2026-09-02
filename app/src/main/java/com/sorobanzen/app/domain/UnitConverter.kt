package com.sorobanzen.app.domain

object UnitConverter {

    // Conversion Constants
    const val SHAKU_TO_METER = 10.0 / 33.0     // ~0.30303 m
    const val SUN_TO_METER = SHAKU_TO_METER / 10.0 // ~0.030303 m
    const val KEN_TO_METER = SHAKU_TO_METER * 6.0  // ~1.81818 m
    
    const val TSUBO_TO_SQM = 400.0 / 121.0       // ~3.30578 m²
    
    const val SHO_TO_LITER = 1.8039              // 1 sho = 1.8039 L
    const val GO_TO_LITER = SHO_TO_LITER / 10.0  // 1 go = 0.18039 L
    
    const val KAN_TO_KG = 3.75                   // 1 kan = 3.75 kg
    const val MOMME_TO_KG = KAN_TO_KG / 1000.0   // 1 momme = 3.75 g = 0.00375 kg

    /** One unit of a category, expressed as how many base (metric) units it is worth. */
    data class UnitSpec(val key: String, val suffix: String, val inBase: Double)

    /** Units available per category, base metric unit first. */
    val unitsByCategory: Map<String, List<UnitSpec>> = mapOf(
        "length" to listOf(
            UnitSpec("m", "m", 1.0),
            UnitSpec("shaku", "尺", SHAKU_TO_METER),
            UnitSpec("sun", "寸", SUN_TO_METER),
            UnitSpec("ken", "間", KEN_TO_METER)
        ),
        "area" to listOf(
            UnitSpec("sqm", "m²", 1.0),
            UnitSpec("tsubo", "坪", TSUBO_TO_SQM),
            UnitSpec("jo", "畳", TSUBO_TO_SQM / 2.0)
        ),
        "volume" to listOf(
            UnitSpec("l", "L", 1.0),
            UnitSpec("sho", "升", SHO_TO_LITER),
            UnitSpec("go", "合", GO_TO_LITER)
        ),
        "weight" to listOf(
            UnitSpec("kg", "kg", 1.0),
            UnitSpec("kan", "貫", KAN_TO_KG),
            UnitSpec("momme", "匁", MOMME_TO_KG)
        )
    )

    /** The base metric unit of [category], the one input defaults to. */
    fun baseUnit(category: String): UnitSpec? = unitsByCategory[category]?.firstOrNull()

    fun unitSpec(category: String, key: String): UnitSpec? =
        unitsByCategory[category]?.firstOrNull { it.key == key }

    /**
     * Converts [value], given in the unit [fromKey], into every other unit of [category].
     * Conversion routes through the category's base unit, so any pair works in either direction.
     */
    fun convertFrom(category: String, fromKey: String, value: Double): List<Pair<UnitSpec, Double>> {
        val units = unitsByCategory[category] ?: return emptyList()
        val from = units.firstOrNull { it.key == fromKey } ?: return emptyList()
        val base = value * from.inBase
        return units.filter { it.key != fromKey }.map { it to base / it.inBase }
    }
}
