/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.qa.shapes.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.qa.shapes.model.impl.Circle;
import org.eclipse.qa.shapes.model.impl.Rectangle;
import org.eclipse.qa.shapes.model.impl.Square;
import org.eclipse.qa.shapes.model.impl.Triangle;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = Circle.class, name = "circle"),
               @JsonSubTypes.Type(value = Rectangle.class, name = "rectangle"),
               @JsonSubTypes.Type(value = Square.class, name = "square"),
               @JsonSubTypes.Type(value = Triangle.class, name = "triangle")})
public interface Shape {
    long getId();

    double evaluateSquare();

    double evaluatePerimeter();
}
