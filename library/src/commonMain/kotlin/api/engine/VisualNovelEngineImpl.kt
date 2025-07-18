package api.engine

import model.assets.Asset
import service.AssetStore

internal class VisualNovelEngineImpl(
    override val storyPlayer: VisualNovelStoryPlayer,
    private val assetStore: AssetStore
) : VisualNovelEngine {
    override fun loadAssets(assets: List<Asset>) {
        assetStore.addOrUpdateAssets(assets)
    }
}