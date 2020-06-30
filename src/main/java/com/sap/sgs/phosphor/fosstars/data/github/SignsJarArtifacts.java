package com.sap.sgs.phosphor.fosstars.data.github;

import static com.sap.sgs.phosphor.fosstars.maven.MavenUtils.readModel;
import static com.sap.sgs.phosphor.fosstars.model.feature.oss.OssFeatures.SIGNS_ARTIFACTS;

import com.sap.sgs.phosphor.fosstars.model.Feature;
import com.sap.sgs.phosphor.fosstars.model.Value;
import com.sap.sgs.phosphor.fosstars.model.feature.oss.OssFeatures;
import com.sap.sgs.phosphor.fosstars.tool.github.GitHubProject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;

/**
 * <p>This data provider checks if an open-source project signs its JAR artifacts. In particular,
 * the data provider checks if a project uses
 * <a href="http://maven.apache.org/plugins/maven-gpg-plugin/">Maven GPG plugin</a>.</p>
 * <p>The provider fills out the {@link OssFeatures#SIGNS_ARTIFACTS} feature.</p>
 */
public class SignsJarArtifacts extends CachedSingleFeatureGitHubDataProvider {

  /**
   * Initializes a data provider.
   *
   * @param fetcher An interface to GitHub.
   */
  public SignsJarArtifacts(GitHubDataFetcher fetcher) {
    super(fetcher);
  }

  @Override
  protected Feature supportedFeature() {
    return SIGNS_ARTIFACTS;
  }

  @Override
  protected Value fetchValueFor(GitHubProject project) throws IOException {
    logger.info("Figuring out if the project signs jar files ...");
    LocalRepository repository = fetcher.localRepositoryFor(project);
    boolean answer = checkMaven(repository);
    return SIGNS_ARTIFACTS.value(answer);
  }

  /**
   * Checks if a project uses Maven GPG plugin.
   *
   * @param repository The project's repository.
   * @return True if the project uses Maven GPG plugin, false otherwise.
   * @throws IOException If something went wrong.
   */
  private boolean checkMaven(LocalRepository repository) throws IOException {
    Optional<InputStream> content = repository.read("pom.xml");

    if (!content.isPresent()) {
      return false;
    }

    Model model = readModel(content.get());
    Build build = model.getBuild();

    return build != null && build.getPlugins().stream().anyMatch(SignsJarArtifacts::isMavenGpg);
  }

  /**
   * Checks a plugin is the Maven GPG plugin.
   *
   * @param plugin The plugin to be checked.
   * @return True if the plugin is the Maven GPG plugin, false otherwise.
   */
  private static boolean isMavenGpg(Plugin plugin) {
    return plugin != null
        && "org.apache.maven.plugins".equals(plugin.getGroupId())
        && "maven-gpg-plugin".equals(plugin.getArtifactId());
  }

}