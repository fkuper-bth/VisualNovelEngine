package data.model.scene

data class SceneRenderStateIds(
    val backgroundId: String? = null,
    val foregroundId: String? = null,
    val characterIds: List<String> = emptyList(),
    val activeCharacterId: String? = null,
    val textBoxIds: List<String> = emptyList()
)