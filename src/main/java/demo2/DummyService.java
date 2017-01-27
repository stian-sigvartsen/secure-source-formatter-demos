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

package demo2;

/**
 * @author Stian Sigvartsen
 */
public class DummyService {

	public void checkUserPermission(long userId, String action) {
		final String model = "USER";
		_permissionChecker.hasPermission(0, model, userId, action);
	}

	public String getUser(long userId) {
		checkUserPermission(userId, action);
		return null;
	}

	private static final String action = "VIEW";

	private PermissionChecker _permissionChecker;

}