package eu.florianbecker.baureihensammler.data

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Bahngesellschaft / Datenherkunft der Baureihe.
 * Aktuell nur [DB] in den Stammdaten; weitere Werte für geplante Kataloge (z. B. SBB, ÖBB).
 */
enum class TrainSeriesOrigin {
    /** Deutsche Bahn (inkl. historischer DR-/DB-Baureihen in diesem Katalog). */
    DB,

    /** Schweizerische Bundesbahnen — reserviert für zukünftige Erweiterungen. */
    SBB,

    /** Österreichische Bundesbahnen — reserviert für zukünftige Erweiterungen. */
    OBB,
    ;

    companion object {
        fun fromName(raw: String?): TrainSeriesOrigin =
            entries.find { it.name == raw } ?: DB
    }
}

/** Kurztext im Kennzeichenfeld (links neben „BR“). */
fun TrainSeriesOrigin.plateAbbrev(): String =
    when (this) {
        TrainSeriesOrigin.DB -> "DB"
        TrainSeriesOrigin.SBB -> "SBB"
        TrainSeriesOrigin.OBB -> "ÖBB"
    }

/** Bezeichnung im Auswahlmenü. */
fun TrainSeriesOrigin.menuLabel(): String =
    when (this) {
        TrainSeriesOrigin.DB -> "Deutsche Bahn (DB)"
        TrainSeriesOrigin.SBB -> "Schweizerische Bundesbahnen (SBB)"
        TrainSeriesOrigin.OBB -> "Österreichische Bundesbahnen (ÖBB)"
    }

data class TrainSeries(
    val baureihe: String,
    val name: String,
    val category: String,
    val vmaxKmh: Int,
    val fleetEstimate: Int,
    val wikiArticleTitle: String,
    val origin: TrainSeriesOrigin = TrainSeriesOrigin.DB,
) {
    val wikiArticleUrl: String
        get() = "https://de.wikipedia.org/wiki/${encodeForWiki(wikiArticleTitle)}"

    val wikiSummaryApiUrl: String
        get() = "https://de.wikipedia.org/api/rest_v1/page/summary/${encodeForWiki(wikiArticleTitle)}"
}

private fun encodeForWiki(value: String): String {
    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
}

object AlphaTrainSeriesRepository {
    val items: List<TrainSeries> = listOf(
        TrainSeries("003 204", "ex DR 03", "Dampflokomotive", 130, 1, "DR_03"),
        TrainSeries("041 231", "ex DR 41", "Dampflokomotive", 90, 1, "DR-Baureihe_41"),
        TrainSeries("044", "ex DR 44", "Dampflokomotive", 80, 2, "DR-Baureihe_44"),
        TrainSeries("050", "ex DR 50", "Dampflokomotive", 80, 11, "DR-Baureihe_50"),
        TrainSeries("052", "ex DR 52", "Dampflokomotive", 80, 21, "DR-Baureihe_52"),
        TrainSeries("065 008", "ex DR 65.10", "Dampflokomotive", 90, 1, "DR-Baureihe_65.10"),
        TrainSeries("099 701", "Sachsische IV K", "Dampflokomotive", 30, 5, "DR-Baureihe_99.51%E2%80%9360"),
        TrainSeries("099 720", "Sachsische VI K", "Dampflokomotive", 30, 1, "DR-Baureihe_99.67%E2%80%9371"),
        TrainSeries("099 722", "ex DR 99.73-76", "Dampflokomotive", 30, 14, "DR-Baureihe_99.73%E2%80%9376"),
        TrainSeries("099 736", "ex DR 99.77-79", "Dampflokomotive", 30, 21, "DR-Baureihe_99.77%E2%80%9379"),
        TrainSeries("099 760", "Trusetal", "Dampflokomotive", 25, 1, "DR_99_4532"),
        TrainSeries("099 770", "ex RukB Mh", "Dampflokomotive", 30, 2, "R%C3%BCgensche_B%C3%A4derbahn"),
        TrainSeries("099 780", "ex KJI 20/21", "Dampflokomotive", 45, 2, "Baureihe_099"),
        TrainSeries("099 901", "ex DR 99.32", "Dampflokomotive", 50, 3, "DR-Baureihe_99.32"),
        TrainSeries("099 904", "LKM 225 PS Schmalspur", "Dampflokomotive", 35, 2, "DR-Baureihe_99.33"),
        TrainSeries("101", "InterCity Ellok", "Lokomotive", 220, 145, "DB-Baureihe_101"),
        TrainSeries("102", "Skoda 109E", "Elektrolokomotive", 200, 20, "DB-Baureihe_102"),
        TrainSeries("105", "Talgo Travca", "Elektrolokomotive", 230, 23, "DB-Baureihe_105"),
        TrainSeries("111", "ex DB E 41", "Elektrolokomotive", 160, 160, "DB-Baureihe_111"),
        TrainSeries("112", "ex DR/DB 112.1", "Elektrolokomotive", 160, 90, "DB-Baureihe_112"),
        TrainSeries("114", "ex DR 212.0", "Elektrolokomotive", 160, 40, "DB-Baureihe_114"),
        TrainSeries("143", "ex DR 243", "Elektrolokomotive", 120, 300, "DB-Baureihe_143"),
        TrainSeries("145", "Bombardier Traxx", "Elektrolokomotive", 140, 80, "DB-Baureihe_145"),
        TrainSeries("146.0", "Bombardier Traxx", "Elektrolokomotive", 160, 30, "DB-Baureihe_146"),
        TrainSeries("146.1", "Bombardier Traxx P160 AC1", "Elektrolokomotive", 160, 30, "DB-Baureihe_146"),
        TrainSeries("146.2", "Bombardier Traxx 2 P160 AC2", "Elektrolokomotive", 160, 130, "DB-Baureihe_146.2"),
        TrainSeries("146.5", "Bombardier Traxx 2 IC2", "Elektrolokomotive", 160, 30, "DB-Baureihe_146.5"),
        TrainSeries("147.0", "Bombardier Traxx 3 P160 AC3", "Elektrolokomotive", 160, 40, "DB-Baureihe_147"),
        TrainSeries("147.5", "Bombardier Traxx 3 IC2", "Elektrolokomotive", 160, 20, "DB-Baureihe_147.5"),
        TrainSeries("146", "TRAXX P160 AC2", "Lokomotive", 160, 220, "DB-Baureihe_146"),
        TrainSeries("147", "TRAXX P160 AC3", "Lokomotive", 160, 70, "DB-Baureihe_147"),
        TrainSeries("152", "Güterzug-Ellok", "Lokomotive", 140, 195, "DB-Baureihe_152"),
        TrainSeries("182", "Siemens ES64U2 Taurus", "Elektrolokomotive", 230, 30, "DB-Baureihe_182"),
        TrainSeries("185.0", "Bombardier Traxx F140 AC1", "Elektrolokomotive", 140, 80, "DB-Baureihe_185"),
        TrainSeries("185.2", "Bombardier Traxx 2 F140 AC2", "Elektrolokomotive", 140, 190, "DB-Baureihe_185.2"),
        TrainSeries("186.1", "Bombardier Traxx 2E F140 MS", "Elektrolokomotive", 160, 40, "DB-Baureihe_186"),
        TrainSeries("187.0", "Bombardier Traxx 3 F140 AC3", "Elektrolokomotive", 140, 150, "DB-Baureihe_187"),
        TrainSeries("189", "Siemens ES64F4", "Elektrolokomotive", 140, 100, "DB-Baureihe_189"),
        TrainSeries("185", "TRAXX F140 AC", "Lokomotive", 140, 370, "DB-Baureihe_185"),
        TrainSeries("187", "TRAXX F140 AC3", "Lokomotive", 140, 200, "DB-Baureihe_187"),
        TrainSeries("193", "Vectron", "Lokomotive", 160, 160, "DB-Baureihe_193"),
        TrainSeries("203", "ex DR V100 modernisiert", "Diesellokomotive", 100, 20, "DB-Baureihe_203"),
        TrainSeries("212", "ex DB V 100.20", "Diesellokomotive", 100, 40, "DB-Baureihe_212"),
        TrainSeries("218", "Diesellok", "Lokomotive", 140, 215, "DB-Baureihe_218"),
        TrainSeries("225", "ex DB 215", "Diesellokomotive", 140, 40, "DB-Baureihe_225"),
        TrainSeries("232", "ex DR 132", "Diesellokomotive", 120, 70, "DB-Baureihe_232"),
        TrainSeries("232.9", "Umbauvariante 234", "Diesellokomotive", 120, 20, "DB-Baureihe_232.9"),
        TrainSeries("233", "remotorisierte 232", "Diesellokomotive", 120, 60, "DB-Baureihe_233"),
        TrainSeries("245", "TRAXX DE ME", "Lokomotive", 160, 80, "DB-Baureihe_245"),
        TrainSeries("246", "Bombardier Traxx DE", "Diesellokomotive", 160, 20, "DB-Baureihe_246"),
        TrainSeries("261", "Gravita 10 BB", "Lokomotive", 100, 130, "DB-Baureihe_261"),
        TrainSeries("265", "Voith Gravita 15L BB", "Diesellokomotive", 100, 60, "DB-Baureihe_265"),
        TrainSeries("266.4", "Class 77", "Diesellokomotive", 100, 40, "DB-Baureihe_266"),
        TrainSeries("290", "ex DB V 90", "Diesellokomotive", 80, 180, "DB-Baureihe_290"),
        TrainSeries("291", "ex DB V 90", "Diesellokomotive", 80, 60, "DB-Baureihe_291"),
        TrainSeries("293", "ex DR V 100", "Diesellokomotive", 80, 60, "DB-Baureihe_293"),
        TrainSeries("294", "Umbau aus 290", "Diesellokomotive", 80, 120, "DB-Baureihe_294"),
        TrainSeries("295", "Umbau aus 291", "Diesellokomotive", 80, 80, "DB-Baureihe_295"),
        TrainSeries("296", "Umbau aus 290", "Diesellokomotive", 80, 40, "DB-Baureihe_296"),
        TrainSeries("298", "ex DR V 100", "Diesellokomotive", 80, 50, "DB-Baureihe_298"),
        TrainSeries("4125", "Vossloh DE 12", "Diesellokomotive", 120, 20, "DB-Baureihe_4125"),
        TrainSeries("4185", "Vossloh DE 18", "Diesellokomotive", 120, 20, "DB-Baureihe_4185"),
        TrainSeries("1001", "Hybridlok", "Hybridlokomotive", 60, 10, "DB-Baureihe_1001"),
        TrainSeries("1002", "Alstom Prima H3", "Hybridlokomotive", 60, 30, "DB-Baureihe_1002"),
        TrainSeries("1004", "CRRC Rangierlok", "Hybridlokomotive", 100, 20, "DB-Baureihe_1004"),
        TrainSeries("2159", "Stadler Eurodual", "Zweikraftlokomotive", 120, 20, "DB-Baureihe_2159"),
        TrainSeries("2248", "Vectron Dual Mode", "Zweikraftlokomotive", 160, 40, "DB-Baureihe_2248"),
        TrainSeries("2249", "Vectron Dual Mode light", "Zweikraftlokomotive", 120, 30, "DB-Baureihe_2249"),
        TrainSeries("310", "Ko II", "Kleinlok", 30, 40, "DB-Baureihe_310"),
        TrainSeries("311", "ex DR V 15", "Kleinlok", 35, 40, "DB-Baureihe_311"),
        TrainSeries("312.0", "ex DR V 23", "Kleinlok", 42, 20, "DB-Baureihe_312"),
        TrainSeries("312.1", "ex DR 102.1", "Kleinlok", 40, 20, "DB-Baureihe_312"),
        TrainSeries("323", "Kof II", "Kleinlok", 45, 30, "DB-Baureihe_323"),
        TrainSeries("324", "Kof II", "Kleinlok", 45, 20, "DB-Baureihe_324"),
        TrainSeries("332", "Kof III", "Kleinlok", 45, 60, "DB-Baureihe_332"),
        TrainSeries("333", "Kof III", "Kleinlok", 45, 40, "DB-Baureihe_333"),
        TrainSeries("335", "Kof III", "Kleinlok", 45, 30, "DB-Baureihe_335"),
        TrainSeries("345", "ex DR V 60", "Kleinlok", 60, 40, "DB-Baureihe_345"),
        TrainSeries("346", "ex DR V 60", "Kleinlok", 60, 60, "DB-Baureihe_346"),
        TrainSeries("347", "ex DR V 60", "Kleinlok", 60, 20, "DB-Baureihe_347"),
        TrainSeries("351", "VPS Mietloks", "Kleinlok", 70, 10, "DB-Baureihe_351"),
        TrainSeries("352", "MaK G322 / Vossloh G400", "Kleinlok", 70, 10, "DB-Baureihe_352"),
        TrainSeries("362", "Umbau aus 364", "Kleinlok", 60, 40, "DB-Baureihe_362"),
        TrainSeries("363", "Umbau aus 365", "Kleinlok", 60, 50, "DB-Baureihe_363"),
        TrainSeries("364", "Umbau aus 360", "Kleinlok", 60, 90, "DB-Baureihe_364"),
        TrainSeries("365", "Umbau aus 361", "Kleinlok", 60, 70, "DB-Baureihe_365"),
        TrainSeries("383", "ex LEW EL 16", "Kleinlok", 6, 20, "DB-Baureihe_383"),
        TrainSeries("399 105", "Wangerooge L18H", "Kleinlok", 20, 2, "DB-Baureihe_399"),
        TrainSeries("399 107", "Wangerooge Schoma", "Kleinlok", 20, 2, "DB-Baureihe_399"),
        TrainSeries("650", "Vossloh G6", "Kleinlok", 80, 50, "DB-Baureihe_650"),
        TrainSeries("401", "ICE 1", "Triebzug", 280, 59, "DB-Baureihe_401"),
        TrainSeries("402", "ICE 2", "Triebzug", 280, 44, "DB-Baureihe_402"),
        TrainSeries("403", "ICE 3", "Triebzug", 330, 67, "DB-Baureihe_403_(1997)"),
        TrainSeries("406", "ICE 3M", "Triebzug", 330, 17, "DB-Baureihe_406"),
        TrainSeries("407", "Velaro D", "Triebzug", 320, 17, "DB-Baureihe_407"),
        TrainSeries("408", "ICE 3neo", "Triebzug", 320, 90, "DB-Baureihe_408"),
        TrainSeries("411", "ICE T (7-teilig)", "Triebzug", 230, 60, "DB-Baureihe_411"),
        TrainSeries("412", "ICE 4", "Triebzug", 250, 130, "DB-Baureihe_412"),
        TrainSeries("415", "ICE T (5-teilig)", "Triebzug", 230, 11, "DB-Baureihe_415"),
        TrainSeries("420", "S-Bahn ET 420", "Triebzug", 120, 80, "DB-Baureihe_420"),
        TrainSeries("421", "S-Bahn ET 421", "Triebzug", 120, 30, "DB-Baureihe_420"),
        TrainSeries("422", "S-Bahn Rhein-Ruhr", "Triebzug", 140, 80, "DB-Baureihe_422"),
        TrainSeries("432", "S-Bahn Rhein-Ruhr", "Triebzug", 140, 80, "DB-Baureihe_422"),
        TrainSeries("423", "S-Bahn ET 423", "Triebzug", 140, 240, "DB-Baureihe_423"),
        TrainSeries("433", "S-Bahn ET 433", "Triebzug", 140, 120, "DB-Baureihe_423"),
        TrainSeries("424", "S-Bahn ET 424", "Triebzug", 140, 40, "DB-Baureihe_424"),
        TrainSeries("434", "S-Bahn ET 434", "Triebzug", 140, 30, "DB-Baureihe_424"),
        TrainSeries("425", "S-Bahn Triebzug", "Triebzug", 140, 250, "DB-Baureihe_425"),
        TrainSeries("435", "S-Bahn ET 435", "Triebzug", 160, 120, "DB-Baureihe_425"),
        TrainSeries("426", "Regional ET 426", "Triebzug", 160, 60, "DB-Baureihe_426"),
        TrainSeries("3427", "FLIRT 3XL", "Triebzug", 160, 25, "DB-Baureihe_3427"),
        TrainSeries("1428", "FLIRT 3", "Triebzug", 160, 40, "DB-Baureihe_1428"),
        TrainSeries("3428", "FLIRT 3XL", "Triebzug", 160, 40, "DB-Baureihe_3428"),
        TrainSeries("429", "FLIRT", "Triebzug", 160, 40, "DB-Baureihe_429"),
        TrainSeries("429.1", "FLIRT 3", "Triebzug", 160, 25, "DB-Baureihe_429.1"),
        TrainSeries("3429", "FLIRT 3XL", "Triebzug", 160, 25, "DB-Baureihe_3429"),
        TrainSeries("430", "S-Bahn Rhein-Main", "Triebzug", 140, 190, "DB-Baureihe_430"),
        TrainSeries("440", "Coradia Continental", "Triebzug", 160, 110, "DB-Baureihe_440"),
        TrainSeries("441", "Coradia Continental", "Triebzug", 160, 110, "DB-Baureihe_440"),
        TrainSeries("1440", "Coradia Continental", "Triebzug", 160, 80, "DB-Baureihe_1440"),
        TrainSeries("1441", "Coradia Continental", "Triebzug", 160, 80, "DB-Baureihe_1441"),
        TrainSeries("442", "Talent 2", "Triebzug", 160, 250, "DB-Baureihe_442"),
        TrainSeries("443", "Talent 2", "Triebzug", 160, 250, "DB-Baureihe_443"),
        TrainSeries("1442", "Talent 2", "Triebzug", 160, 150, "DB-Baureihe_1442"),
        TrainSeries("1443", "Talent 2", "Triebzug", 160, 150, "DB-Baureihe_1443"),
        TrainSeries("2442", "Talent 2", "Triebzug", 160, 100, "DB-Baureihe_2442"),
        TrainSeries("2443", "Talent 2", "Triebzug", 160, 100, "DB-Baureihe_2443"),
        TrainSeries("3442", "Talent 2", "Triebzug", 160, 100, "DB-Baureihe_3442"),
        TrainSeries("3443", "Talent 2", "Triebzug", 160, 100, "DB-Baureihe_3443"),
        TrainSeries("8442", "Talent 2", "Triebzug", 160, 40, "DB-Baureihe_8442"),
        TrainSeries("8443", "Talent 2", "Triebzug", 160, 40, "DB-Baureihe_8443"),
        TrainSeries("6442", "Talent 3+", "Triebzug", 160, 40, "DB-Baureihe_6442"),
        TrainSeries("6443", "Talent 3+", "Triebzug", 160, 40, "DB-Baureihe_6443"),
        TrainSeries("445", "Stadler KISS", "Triebzug", 160, 20, "DB-Baureihe_445"),
        TrainSeries("446", "Bombardier Twindexx Vario", "Triebzug", 160, 20, "DB-Baureihe_446"),
        TrainSeries("447", "Alstom Coradia Max", "Triebzug", 160, 20, "DB-Baureihe_447"),
        TrainSeries("1462", "Siemens Desiro HC", "Triebzug", 190, 30, "DB-Baureihe_1462"),
        TrainSeries("4462", "Siemens Desiro HC", "Triebzug", 190, 30, "DB-Baureihe_4462"),
        TrainSeries("463", "Mireo", "Triebzug", 160, 90, "DB-Baureihe_463")
    )
}
