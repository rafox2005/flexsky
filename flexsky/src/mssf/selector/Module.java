/*
 * The MIT License
 *
 * Copyright 2014 rlibardi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mssf.selector;

import java.util.HashMap;

/**
 *
 * @author rlibardi
 */
public class Module {
    private String name;
    private String pipe_name;
    private String type;
    private HashMap<String, String> parameters;
    private HashMap<String, Integer> selectionParameters;

    public Module(String name, String type, String pipe_name, HashMap<String, String> parameters, HashMap<String, Integer> selectionParameters) {
        this.name = name;
        this.pipe_name = pipe_name;
        this.type = type;
        this.parameters = parameters;
        this.selectionParameters = selectionParameters;
    }

    public String getName() {
        return name;
    }

    public String getPipe_name() {
        return pipe_name;
    }

    public String getType() {
        return type;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public HashMap<String, Integer> getSelectionParameters() {
        return selectionParameters;
    }
    
    
    
    
}
