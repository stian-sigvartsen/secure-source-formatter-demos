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
import com.github.javaparser.ast.body.FieldDeclaration;

import com.liferay.tools.security.ClassLoader;
import com.liferay.tools.security.reference.ClassOrInterfaceReferenceTable;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.List;

/**
 * @author Stian Sigvartsen
 */
public class Demo1 {

	public static void main(String[] args) throws FileNotFoundException {
		ClassLoader classLoader = new ClassLoader(
			ClassLoader.class.getClassLoader());

		classLoader.addClasspathDirectory(
			new File("/liferay/SecureSourceFormatter/javaSrcDir"));

		ClassOrInterfaceReferenceTable typeRT =
			ClassOrInterfaceReferenceTable.getReferenceTable(
				(ClassOrInterfaceDeclaration)
					classLoader.getRequiredDefinition("java.lang.Integer"),
				classLoader);

		ClassOrInterfaceReferenceTable.FieldEntry fieldRT =
			typeRT.getFieldEntry("digits");

		// Retrieve JavaParser AST representation

		FieldDeclaration fieldDeclaration = fieldRT.getFieldDeclaration();

		// Let’s print the declaration statement of the digits field

		System.out.println(fieldDeclaration.toString());

		// Let’s print the implemented Comparable interface’s
		// reference table (fields, methods, class hierarchy etc.)

		List<ClassOrInterfaceReferenceTable> interfaceRTs =
			typeRT.getInterfacesReferenceTables();

		System.out.println(interfaceRTs.get(0).toString());
	}

}