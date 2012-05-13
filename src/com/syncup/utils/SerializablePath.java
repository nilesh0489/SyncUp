package com.syncup.utils;

/**
 * Copyright (c) 2012, aditya
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <p/>
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import android.graphics.Path;

import java.util.ArrayList;

import java.io.Serializable;

public class SerializablePath extends Path implements Serializable {

    private ArrayList<float[]> pathPoints;

    public SerializablePath() {
        super();
        pathPoints = new ArrayList<float[]>();
    }

    public SerializablePath(SerializablePath p) {
        super(p);
        pathPoints = p.pathPoints;
    }

    public void addPathPoints(float[] points) {
        this.pathPoints.add(points);
    }

    public void loadPathPointsAsQuadTo() {
        float[] initPoints = pathPoints.remove(0);
        this.moveTo(initPoints[0], initPoints[1]);
        for (float[] pointSet : pathPoints) {
            this.quadTo(pointSet[0], pointSet[1], pointSet[2], pointSet[3]);
        }
    }
}