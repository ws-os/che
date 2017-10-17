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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.eclipse.qa.shapes.model.Shape;
import org.eclipse.qa.util.ShapeUniqueIdGenerator;

@JsonTypeName("square")
public class Square implements Shape {

    private final long id;
    @JsonProperty("side")
    private double side;

    public Square(@JsonProperty("side") double side) {
        this.id = ShapeUniqueIdGenerator.generateUniqueId();
        this.side = side;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double evaluateSquare() {
        return Math.pow(side, 2);
    }

    @Override
    public double evaluatePerimeter() {
        return 4 * side;
    }
}
