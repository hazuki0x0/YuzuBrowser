/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.reader.snacktory;

import org.jsoup.nodes.Element;

/**
 * Class which encapsulates the data from an image found under an element
 *
 * @author Chris Alexander, chris@chris-alexander.co.uk
 */
public class ImageResult {

    public String src;
    public Integer weight;
    public String title;
    public int height;
    public int width;
    public String alt;
    public boolean noFollow;
    public Element element;

    public ImageResult(String src, Integer weight, String title, int height, int width, String alt, boolean noFollow) {
        this.src = src;
        this.weight = weight;
        this.title = title;
        this.height = height;
        this.width = width;
        this.alt = alt;
        this.noFollow = noFollow;
    }
}
