package service

import model.assets.Asset
import kotlinx.coroutines.flow.StateFlow

internal interface AssetStore {
    val assets: StateFlow<Map<String, Asset>>

    fun addOrUpdateAsset(asset: Asset)

    fun addOrUpdateAsset(assetId: String, asset: Asset)

    fun addOrUpdateAssets(assets: List<Asset>) = assets.forEach(::addOrUpdateAsset)

    fun removeAsset(assetId: String)
}

internal inline fun <reified T : Asset> AssetStore.getNow(id: String): T? {
    return assets.value[id] as? T
}

internal inline fun <reified T : Asset> AssetStore.getNow(ids: List<String>): List<T>? {
    return ids.mapNotNull { getNow(it) }
}