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
package org.eclipse.qa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.qa.shapes.model.Shape;
import org.eclipse.qa.shapes.model.impl.Circle;
import org.eclipse.qa.shapes.model.impl.Rectangle;
import org.eclipse.qa.shapes.model.impl.Square;
import org.eclipse.qa.shapes.model.impl.Triangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
public class ShapeController {

    private static final Map<Long, Shape> shapes = new ConcurrentHashMap<>();

    @Autowired
    public ShapeController() {
        shapes.putAll(generateDefaultShapeList());
    }

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping(value = "shapes")
    public List<Shape> getShapes() {
        return new ArrayList<>(shapes.values());
    }

    @GetMapping(value = "shape/{id}")
    public ResponseEntity getShape(@PathVariable("id") String id) {
        long key;
        try {
            key = Long.valueOf(id);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to get shape by id:" + id + ". Cause:" + e.getMessage(),
                    BAD_REQUEST);
        }

        if (!shapes.containsKey(key)) {
            return new ResponseEntity<>("Shape was not found by id: " + id, NOT_FOUND);
        }

        return new ResponseEntity<>(shapes.get(key), OK);
    }

    @DeleteMapping(value = "shape/{id}")
    public ResponseEntity removeShape(@PathVariable("id") String id) {
        long idKey;
        try {
            idKey = Long.valueOf(id);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to remove shape by id:" + id + ". Cause:" + e.getMessage(),
                    BAD_REQUEST);
        }

        if (!shapes.containsKey(idKey)) {
            return new ResponseEntity<>("Shape was not found by id: " + id, NOT_FOUND);
        }

        shapes.remove(idKey);
        return new ResponseEntity(OK);
    }

    @PostMapping(value = "shape")
    public ResponseEntity createShape(@RequestBody Shape shape) {
        if (shape == null) {
            return new ResponseEntity<>("Shape should not be null", BAD_REQUEST);
        }

        long id = shape.getId();
        if (shapes.containsKey(id)) {
            return new ResponseEntity<>("Shape with id: " + id + " is already exist", CONFLICT);
        }

        shapes.put(id, shape);
        return new ResponseEntity(CREATED);
    }

    @PutMapping(value = "shape")
    public ResponseEntity updateShape(@RequestBody Shape shape) {
        if (shape == null) {
            return new ResponseEntity<>("Shape to update should not be null", BAD_REQUEST);
        }

        long id = shape.getId();
        if (!shapes.containsKey(id)) {
            return new ResponseEntity<>("Shape to update was not found by id: " + id, NOT_FOUND);
        }

        shapes.replace(id, shape);
        return new ResponseEntity(OK);
    }

    private Map<Long, Shape> generateDefaultShapeList() {
        Map<Long, Shape> shapes = new HashMap<>();

        Shape triangle = new Triangle(1, 2, 3);
        shapes.put(triangle.getId(), triangle);
        Shape rectangle = new Rectangle( 1, 2);
        shapes.put(rectangle.getId(), rectangle);
        Shape square = new Square(1);
        shapes.put(square.getId(), square);
        Shape circle = new Circle( 1);
        shapes.put(circle.getId(), circle);

        return shapes;
    }
}
