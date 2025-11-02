package org.in.media.res.sqlBuilder.core.model;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class ClasspathScanner {

	private ClasspathScanner() {
	}

	static Set<Class<?>> findClasses(String basePackage, ClassLoader classLoader) {
		Set<Class<?>> classes = new HashSet<>();
		String path = basePackage.replace('.', '/');
		try {
			Enumeration<URL> resources = classLoader.getResources(path);
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				String protocol = resource.getProtocol();
				if ("file".equals(protocol)) {
					scanFileSystem(basePackage, resource, classLoader, classes);
				} else if ("jar".equals(protocol)) {
					scanJar(basePackage, resource, classLoader, classes);
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to scan package " + basePackage, ex);
		}
		return classes;
	}

	private static void scanFileSystem(String basePackage, URL resource, ClassLoader classLoader, Set<Class<?>> classes)
			throws IOException {
			String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
			Path basePath = Paths.get(filePath);
			Files.walk(basePath).filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".class"))
					.forEach(path -> {
						String className = toClassName(basePackage, basePath, path);
						loadClass(className, classLoader, classes);
					});
	}

	private static String toClassName(String basePackage, Path basePath, Path classFile) {
		Path relative = basePath.relativize(classFile);
		String className = relative.toString().replace(File.separatorChar, '.');
		if (className.endsWith(".class")) {
			className = className.substring(0, className.length() - 6);
		}
		return basePackage + '.' + className;
	}

	private static void scanJar(String basePackage, URL resource, ClassLoader classLoader, Set<Class<?>> classes)
			throws IOException {
			JarURLConnection connection = (JarURLConnection) resource.openConnection();
			try (JarFile jarFile = connection.getJarFile()) {
				String path = basePackage.replace('.', '/');
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
						String className = name.replace('/', '.').substring(0, name.length() - 6);
						loadClass(className, classLoader, classes);
					}
				}
			}
	}

	private static void loadClass(String className, ClassLoader classLoader, Set<Class<?>> classes) {
		try {
			classes.add(Class.forName(className, false, classLoader));
		} catch (ClassNotFoundException | NoClassDefFoundError ignored) {
			// ignore classes that cannot be loaded
		}
	}
}
