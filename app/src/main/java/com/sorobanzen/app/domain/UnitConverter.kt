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

    // --- Length conversions ---
    fun metersToShaku(m: Double) = m / SHAKU_TO_METER
    fun shakuToMeters(shaku: Double) = shaku * SHAKU_TO_METER

    fun metersToSun(m: Double) = m / SUN_TO_METER
    fun sunToMeters(sun: Double) = sun * SUN_TO_METER

    fun metersToKen(m: Double) = m / KEN_TO_METER
    fun kenToMeters(ken: Double) = ken * KEN_TO_METER

    // --- Area conversions ---
    fun sqmToTsubo(sqm: Double) = sqm / TSUBO_TO_SQM
    fun tsuboToSqm(tsubo: Double) = tsubo * TSUBO_TO_SQM

    // --- Volume conversions ---
    fun litersToSho(liters: Double) = liters / SHO_TO_LITER
    fun shoToLiters(sho: Double) = sho * SHO_TO_LITER

    fun litersToGo(liters: Double) = liters / GO_TO_LITER
    fun goToLiters(go: Double) = go * GO_TO_LITER

    // --- Weight conversions ---
    fun kgToKan(kg: Double) = kg / KAN_TO_KG
    fun kanToKg(kan: Double) = kan * KAN_TO_KG

    fun kgToMomme(kg: Double) = kg / MOMME_TO_KG
    fun mommeToKg(momme: Double) = momme * MOMME_TO_KG
}
