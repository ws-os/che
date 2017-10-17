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

@JsonTypeName("triangle")
public class Triangle implements Shape {

    private final long id;
    @JsonProperty("a")
    private double a;
    @JsonProperty("b")
    private double b;
    @JsonProperty("c")
    private double c;

    public Triangle(@JsonProperty("a") double a,
                    @JsonProperty("b") double b,
                    @JsonProperty("c") double c) {
        this.id = ShapeUniqueIdGenerator.generateUniqueId();
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double evaluateSquare() {
        double hp = evaluatePerimeter()/2.0;
        return 1.0/4.0 * Math.pow(hp * (hp - a) * (hp - b) * (hp -c), 1.0/2.0);
    }

    @Override
    public double evaluatePerimeter() {
        return a + b + c;
    }
}
