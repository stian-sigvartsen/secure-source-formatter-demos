/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.liferay.tools.security.ClassLoader;
import com.liferay.tools.security.ExpressionVisitor;
import com.liferay.tools.security.LiferayCodeAnalytics;
import com.liferay.tools.security.LiferayMethodVisitor;
import com.liferay.tools.security.reference.ClassOrInterfaceReferenceTable;
import com.liferay.tools.security.reference.MethodReferenceTable;
import com.liferay.tools.security.reference.ReferenceTable;
import com.liferay.tools.security.resolution.ResolvableType;
import com.liferay.tools.security.symbol.ClassOrInterfaceSymbolTable;
import com.liferay.tools.security.symbol.MethodSymbolTable;
import com.liferay.tools.security.utils.StaticAnalysisUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Stian Sigvartsen
 */
public class Demo2 {

	public static MethodSymbolTable getEntryMethodST(
		String className, String methodName, ClassLoader classLoader) {

		ClassOrInterfaceReferenceTable typeRT =
			ClassOrInterfaceReferenceTable.getReferenceTable(
				(ClassOrInterfaceDeclaration)
					classLoader.getRequiredDefinition(className),
				classLoader);

		ClassOrInterfaceSymbolTable typeST =
			ClassOrInterfaceSymbolTable.getSymbolTable(typeRT);

		List<MethodReferenceTable> methodRT = typeRT.getMethodEntries(
			methodName);

		MethodSymbolTable methodST = MethodSymbolTable.getSymbolTable(
			methodRT.get(0), null, typeST);

		return methodST;
	}

	public static void main(String[] args) {
		ClassLoader classLoader = new ClassLoader(
			ClassLoader.class.getClassLoader());

		try {
			classLoader.addClasspathDirectory(
				new File("/liferay/SecureSourceFormatter/javaSrcDir"));

			classLoader.addClasspathDirectory(new File("./src/main/java"));
		}
		catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.exit(0);
		}

		final List<MethodReferenceTable> visitedMethodRTs = new LinkedList<>();

		LiferayCodeAnalytics analytics = new LiferayCodeAnalytics() {

			public void checkIsSignificantMethod(
				MethodReferenceTable methodRT) {

				if (methodRT.getNode().getName().equals("hasPermission")) {
					visitedMethodRTs.add(methodRT);
				}
			}

		};

		MethodSymbolTable methodST = getEntryMethodST(
			"demo2.DummyService", "getUser", classLoader);

		LiferayMethodVisitor visitor = new ExpressionVisitor();

		visitor.visitMethod(null, methodST, analytics);

		writeReport(visitedMethodRTs, classLoader);
	}

	private static Set<ResolvableType> getSources(
		ResolvableType resolvableType) {

			Set<ResolvableType> derivedSources = new HashSet<>();
			getSources(resolvableType, derivedSources);

			if (derivedSources.size() == 0) {
				derivedSources.add(resolvableType);
			}

			return derivedSources;
	}

	private static void getSources(
		ResolvableType resolvableType, Set<ResolvableType> alreadyLogged) {

			if (resolvableType.getSources().size() == 0) {
				alreadyLogged.add(resolvableType);
			}
			else {
					for (ResolvableType innerSource :
							resolvableType.getSources()) {

							if (!alreadyLogged.contains(innerSource)) {
								getSources(innerSource, alreadyLogged);
							}
					}
			}
	}

	private static void writeReport(
		List<MethodReferenceTable> visitedMethodRTs, ClassLoader classLoader) {

		PrintWriter w = new PrintWriter(System.out);

		w.println("==========");

		for (MethodReferenceTable visitedMethodRT : visitedMethodRTs) {
			w.println("Visited: " + visitedMethodRT.getKey().toString());

			for (MethodReferenceTable.ParameterEntry param :
					visitedMethodRT.getParameterEntries()) {

				w.println();
				w.println(param.getQualifiedTypeReference());

				try {
					writeSourcesReport(w, param, classLoader);
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		w.flush();
	}

	private static void writeSourcesReport(
			PrintWriter w, ResolvableType resolvableType,
			ClassLoader classLoader)
		throws IOException {

		for (ResolvableType source : getSources(resolvableType)) {
			if (source.getType().getParentNode() instanceof MethodDeclaration) {

				// A method return type derived from an interface
				// because no impl is known

				w.println(" Unvisited method source: " +
						MethodReferenceTable.getReferenceTable(
								(MethodDeclaration)source.getType().getParentNode(),
								classLoader).getKey().serialize());
			}
			else if (source instanceof ReferenceTable.Entry) {
				ReferenceTable.Entry entrySource = (ReferenceTable.Entry)source;

				if (entrySource instanceof ClassOrInterfaceReferenceTable.
						FieldEntry) {

					w.println(" Field source: " +
							entrySource.getName() +
							" at line " +
							source.getType().getBeginLine() +
							" in " +
							entrySource.getReferenceTable().getKey().serialize());
				}
				else if (entrySource instanceof MethodReferenceTable.ParameterEntry) {
					w.println(" Parameter source: " +
							entrySource.getName() +
							" passed to " +
							entrySource.getReferenceTable().getKey().serialize());
				}
				else {
					w.println(
						" Unsupported SymbolTable.Entry source type: " 
							+ source.getType().getClass().getName());
				}
			}
			else if (source instanceof ResolvableType) {
				w.println(" Literal expr: " +
						source.getType().getParentNode() +
						" at line " +
						source.getType().getBeginLine() +
						" in " +
						ClassOrInterfaceReferenceTable.getReferenceTable(
								StaticAnalysisUtils.getClassDeclaration(
										source.getType()), classLoader).getKey().serialize());
			}
			else {
				w.println(
						" Unsupported source type: " + source.getType().getClass().getName());
			}
		}
	}

}