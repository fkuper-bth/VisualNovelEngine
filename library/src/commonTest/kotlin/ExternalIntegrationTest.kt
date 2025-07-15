import api.engine.VisualNovelEngine
import api.engine.VisualNovelEngineImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExternalIntegrationTest : KoinTest {
    interface SomeDependency
    class SomeDependencyImpl : SomeDependency

    @BeforeTest
    fun setup() {
        stopKoin()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `VisualNovelEngine - check uninitialized state`() {
        assertNull(VisualNovelEngine.koinApp)
    }

    @Test
    fun `VisualNovelEngine init - should return VisualNovelEngineImpl`() {
        // Arrange
        val applicationScope = CoroutineScope(Dispatchers.Default)

        // Act
        val engine = VisualNovelEngine.init(applicationScope)

        // Assert
        assertNotNull(engine)
        assertTrue(engine is VisualNovelEngineImpl)
    }

    @Test
    fun `VisualNovelEngine init in other Koin module - should return VisualNovelEngineImpl`() {
        // Arrange
        val consumerModule = module {
            single<SomeDependency> { SomeDependencyImpl() }
            single<VisualNovelEngine> {
                VisualNovelEngine.init(
                    applicationScope = CoroutineScope(Dispatchers.Default)
                )
            }
        }

        // Act
        startKoin {
            modules(consumerModule)
        }
        val engine = get<VisualNovelEngine>()

        // Assert
        assertNotNull(engine)
        assertTrue(engine is VisualNovelEngineImpl)
    }

    @Test
    fun `VisualNovelEngine dispose - should dispose KoinApplication`() {
        // Arrange
        val applicationScope = CoroutineScope(Dispatchers.Default)

        // Act
        VisualNovelEngine.init(applicationScope)
        VisualNovelEngine.dispose()

        // Assert
        assertNull(VisualNovelEngine.koinApp)
    }
}