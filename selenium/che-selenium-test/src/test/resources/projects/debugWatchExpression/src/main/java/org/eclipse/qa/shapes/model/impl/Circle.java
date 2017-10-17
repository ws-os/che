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
package org.eclipse.qa.shapes.model.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.eclipse.qa.shapes.model.Shape;
import org.eclipse.qa.util.ShapeUniqueIdGenerator;

@JsonTypeName("circle")
public class Circle implements Shape {

    private final long id;
    @JsonProperty("radius")
    private double radius;

    @JsonCreator
    public Circle(@JsonProperty("radius") double radius) {
        this.id = ShapeUniqueIdGenerator.generateUniqueId();
        this.radius = radius;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double evaluateSquare() {
        return Math.PI * Math.pow(radius, 2);
    }

    @Override
    public double evaluatePerimeter() {
        return 2 * Math.PI * radius;
    }
}
