package com.example.myapplication.wifi

/*

class CarLocatorViewModel : ViewModel() {
    private val _referenceAccessPoint = MutableLiveData<Pair<String, Int>?>()
    //val referenceAccessPoint: LiveData<Pair<String, Int>?> = _referenceAccessPoint

    private val _currentDistanceInfo = MutableLiveData<String>()
    val currentDistanceInfo: LiveData<String> = _currentDistanceInfo

    private val rssiHistory = mutableListOf<Int>() // Pour le graphe
    private val _rssiHistoryLiveData = MutableLiveData<List<Int>>()
    val rssiHistoryLiveData: LiveData<List<Int>> = _rssiHistoryLiveData

    fun saveClosestAccessPoint(results: MutableList<ScanResult>) {
        val closest = results.maxByOrNull { it.level } // Meilleur RSSI = plus proche
        closest?.let {
            _referenceAccessPoint.value = Pair(it.BSSID, it.level)
        }
    }
    fun compareToReference(results: MutableList<ScanResult>) {
        val ref = _referenceAccessPoint.value ?: return
        val current = results.find { it.BSSID == ref.first }

        if (current != null) {
            val refRssi = ref.second
            val diff = current.level - refRssi

            val message = when {
                diff >= -5 -> "Vous êtes très proche"
                diff in -10..-6 -> "Vous êtes à moyenne distance"
                else -> "Vous vous éloignez"
            }

            _currentDistanceInfo.value = message
            // Ajout au graphe
            rssiHistory.add(current.level)
            _rssiHistoryLiveData.value = rssiHistory.toList()
        } else {
            _currentDistanceInfo.value = "Point d'accès non détecté"
        }
    }


}
*/