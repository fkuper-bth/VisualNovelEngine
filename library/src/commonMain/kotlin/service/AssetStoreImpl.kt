package service

import model.assets.Asset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class AssetStoreImpl : AssetStore {
    private val _assets = MutableStateFlow<Map<String, Asset>>(emptyMap())
    override val assets: StateFlow<Map<String, Asset>> = _assets.asStateFlow()

    override fun addOrUpdateAsset(asset: Asset) {
        _assets.update { currentAssets ->
            currentAssets + (asset.id to asset)
        }
    }

    // TODO: does this mix the data state with the UI state?
    // Should AssetStore just be a storage of assets or also provide some kind of UI state?
    override fun addOrUpdateAsset(assetId: String, asset: Asset) {
        _assets.update { currentAssets ->
            currentAssets + (assetId to asset)
        }
    }

    override fun removeAsset(assetId: String) {
        _assets.update { currentAssets ->
            currentAssets - assetId
        }
    }
}