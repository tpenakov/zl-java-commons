package green.zerolabs.gqgen;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@QuarkusTest
@Slf4j
public class GraphQlSchemaGenerateIT {

  public static final Function<InputStream, String> READ_TEXT_FILE_FN =
      Unchecked.<InputStream, String>function(s -> IOUtils.toString(s, StandardCharsets.UTF_8));

  public static final Function<String, String> READ_TEXT_FILE_FROM_PATH_FN =
      Unchecked.<String, String>function(
          s -> FileUtils.readFileToString(new File(s), StandardCharsets.UTF_8));
  public static final String OUTPUT_DIR =
      "../core/src/main/java/green/zerolabs/commons/core/model/graphql/generated";
  public static final String OPUTPUT_PACKAGE =
      "green.zerolabs.commons.core.model.graphql.generated";

  @Test
  public void generateFilesFromGraphQlSchema() throws Exception {
    // you test your lambas by invoking on http://localhost:8081
    // this works in dev mode too

    final String property = System.getenv("SHEMA_GRAPHQL_GENERATE");
    if (!Boolean.parseBoolean(property)) {
      log.info("no need to generate schema. will exit");
      return;
    }

    FileUtils.deleteDirectory(new File(OUTPUT_DIR));
    FileUtils.createParentDirectories(new File(OUTPUT_DIR, "touch.txt"));

    final String schemaGraphqlPath = System.getenv("SHEMA_GRAPHQL_PATH");
    final String queryAndMutationFiles = System.getenv("GRAPHQL_QUERY_MUTATIONS");
    final List<String> gqRows =
        Arrays.stream(StringUtils.split(queryAndMutationFiles, ","))
            .sorted()
            .map(s -> READ_TEXT_FILE_FROM_PATH_FN.apply(s))
            .flatMap(s -> readGqRows(s).stream())
            .collect(Collectors.toList());

    final String schema =
        StringUtils.isNotBlank(schemaGraphqlPath)
            ? READ_TEXT_FILE_FROM_PATH_FN.apply(schemaGraphqlPath)
            : READ_TEXT_FILE_FN.apply(
                getClass().getClassLoader().getResourceAsStream("schema.graphql"));

    final Generator generator = new Generator(OPUTPUT_PACKAGE, "GraphQlRepository", schema, gqRows);
    final Map<String, String> result = generator.generateSourceFiles();

    log.info("result: {}", result);

    result.forEach(
        (s, s2) -> {
          final int i = StringUtils.lastIndexOf(s, '.');
          final String filename = MessageFormat.format("{0}.java", StringUtils.substring(s, i + 1));
          Unchecked.<String, String, Boolean>function(
                  (o, o2) -> {
                    final File file = new File(OUTPUT_DIR, o);
                    FileUtils.write(file, o2, StandardCharsets.UTF_8);
                    return true;
                  })
              .apply(filename, s2);
          log.info("Filename: {}", filename);
        });
  }

  private List<String> readGqRows(final String s) {
    final AtomicInteger i = new AtomicInteger(0);
    return Arrays.stream(StringUtils.split(s, "`"))
        .sequential()
        .filter(s1 -> i.getAndIncrement() % 2 > 0)
        .collect(Collectors.toList());
  }
}
