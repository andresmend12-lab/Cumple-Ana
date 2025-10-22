import java.util.Collections;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation;
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin;
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact;
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption;

public class LocalComposeCompilerGradlePlugin implements KotlinCompilerPluginSupportPlugin {
    private static final String COMPOSE_PLUGIN_ID = "androidx.compose.compiler.plugins.kotlin";
    private static final String COMPOSE_COMPILER_GROUP = "androidx.compose.compiler";
    private static final String COMPOSE_COMPILER_ARTIFACT = "compiler";
    private static final String COMPOSE_COMPILER_VERSION = "1.8.8";

    @Override
    public void apply(Project project) {
        // No-op.
    }

    @Override
    public boolean isApplicable(KotlinCompilation<?> kotlinCompilation) {
        return true;
    }

    @Override
    public Provider<List<SubpluginOption>> applyToCompilation(KotlinCompilation<?> kotlinCompilation) {
        return kotlinCompilation.getTarget().getProject().provider(Collections::emptyList);
    }

    @Override
    public String getCompilerPluginId() {
        return COMPOSE_PLUGIN_ID;
    }

    @Override
    public SubpluginArtifact getPluginArtifact() {
        return new SubpluginArtifact(
                COMPOSE_COMPILER_GROUP,
                COMPOSE_COMPILER_ARTIFACT,
                COMPOSE_COMPILER_VERSION
        );
    }

    @Override
    public SubpluginArtifact getPluginArtifactForNative() {
        return getPluginArtifact();
    }
}
