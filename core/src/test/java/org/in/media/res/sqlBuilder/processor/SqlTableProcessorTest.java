package org.in.media.res.sqlBuilder.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

class SqlTableProcessorTest {

	@Test
	void generatesColumnsArtifactsFromAnnotatedTable() throws Exception {
		Path outputDir = Files.createTempDirectory("processor-out");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertThat(compiler).as("JDK JavaCompiler availability").isNotNull();

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null,
				StandardCharsets.UTF_8)) {
			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(outputDir.toFile()));

			String source = """
					package org.in.media.res.sqlBuilder.processor.samples;

					import org.in.media.res.sqlBuilder.api.model.ColumnRef;
					import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
					import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

					@SqlTable(name = "ProcessorEmployee", alias = "PE")
					public final class ProcessorEmployee {

						@SqlColumn(name = "EMP_ID", alias = "ID", javaType = Integer.class)
						public static ColumnRef<Integer> ID;

						@SqlColumn(name = "EMP_NAME", alias = "NAME", javaType = String.class)
						public static ColumnRef<String> NAME;

						private ProcessorEmployee() {
						}
					}
					""";
			String classpath = System.getProperty("java.class.path");
			var compilationUnits = List.of(new InMemorySource(
					"org.in.media.res.sqlBuilder.processor.samples.ProcessorEmployee", source));
			List<String> options = List.of(
					"-classpath", classpath,
					"-processor", "org.in.media.res.sqlBuilder.processor.SqlTableProcessor");

			Boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
			assertThat(success).as(() -> diagnosticsToString(diagnostics)).isTrue();
		}

		Path interfaceFile = outputDir.resolve(
				"org/in/media/res/sqlBuilder/processor/samples/ProcessorEmployeeColumns.java");
		Path implFile = outputDir.resolve(
				"org/in/media/res/sqlBuilder/processor/samples/ProcessorEmployeeColumnsImpl.java");

		assertThat(interfaceFile).exists();
		assertThat(implFile).exists();

		String interfaceSource = Files.readString(interfaceFile);
		assertThat(interfaceSource)
				.contains("interface ProcessorEmployeeColumns")
				.contains("ColumnRef<java.lang.Integer> ID();")
				.contains("static ProcessorEmployeeColumns of(TableFacets.Facet facet)");

		String implSource = Files.readString(implFile);
		assertThat(implSource)
				.contains("implements ProcessorEmployeeColumns")
				.contains("facet.column(\"ID\")")
				.contains("facet.column(\"NAME\")");
	}

	private static String diagnosticsToString(DiagnosticCollector<JavaFileObject> diagnostics) {
		StringBuilder builder = new StringBuilder();
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
			builder.append(diagnostic.getKind())
					.append(": ")
					.append(diagnostic.getMessage(null))
					.append('\n');
		}
		return builder.toString();
	}

	private static String buildClasspath() {
		return System.getProperty("java.class.path");
	}

	private static final class InMemorySource extends SimpleJavaFileObject {
		private final String source;

		private InMemorySource(String className, String source) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.source = source;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return source;
		}
	}
}
