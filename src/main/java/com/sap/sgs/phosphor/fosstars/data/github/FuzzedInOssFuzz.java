package com.sap.sgs.phosphor.fosstars.data.github;

import static com.sap.sgs.phosphor.fosstars.model.feature.oss.OssFeatures.FUZZED_IN_OSS_FUZZ;

import com.sap.sgs.phosphor.fosstars.model.Feature;
import com.sap.sgs.phosphor.fosstars.model.Value;
import com.sap.sgs.phosphor.fosstars.model.feature.oss.OssFeatures;
import com.sap.sgs.phosphor.fosstars.tool.github.GitHubProject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;

/**
 * This data provider check if an open-source project is included to the OSS-Fuzz project.
 * It fills out the {@link OssFeatures#FUZZED_IN_OSS_FUZZ} feature.
 */
public class FuzzedInOssFuzz extends CachedSingleFeatureGitHubDataProvider {

  /**
   * OSS-Fuzz project on GitHub.
   */
  static final GitHubProject OSS_FUZZ_PROJECT = new GitHubProject("google", "oss-fuzz");

  /**
   * Initializes a data provider.
   *
   * @param fetcher An interface to GitHub.
   */
  public FuzzedInOssFuzz(GitHubDataFetcher fetcher) {
    super(fetcher);
  }

  @Override
  protected Feature supportedFeature() {
    return FUZZED_IN_OSS_FUZZ;
  }

  @Override
  protected Value<Boolean> fetchValueFor(GitHubProject project) throws IOException {
    logger.info("Figuring out if the project is fuzzed in OSS-Fuzz ...");

    LocalRepository ossFuzzRepository = fetcher.localRepositoryFor(OSS_FUZZ_PROJECT);

    List<Path> dockerFiles = Files.walk(ossFuzzRepository.info().path())
        .filter(Files::isRegularFile)
        .filter(path -> "Dockerfile".equals(path.getFileName().toString()))
        .collect(Collectors.toList());

    String url = project.url().toString();
    for (Path dockerFile : dockerFiles) {
      String content = IOUtils.toString(Files.newInputStream(dockerFile));
      if (content.contains(url)) {
        return FUZZED_IN_OSS_FUZZ.value(true);
      }
    }

    return FUZZED_IN_OSS_FUZZ.value(false);
  }
}
