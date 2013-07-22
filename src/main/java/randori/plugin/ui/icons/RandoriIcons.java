/*
 * Copyright 2013 original Randori IntelliJ Plugin authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package randori.plugin.ui.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriIcons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, RandoriIcons.class);
    }

    public static final Icon JumpToArrow = load("/icons/jumpTo.png"); // 16x16
    public static final Icon Randori16 = load("/icons/randori16x16.png"); // 16x16
    public static final Icon Randori13 = load("/icons/randori13x13.png"); // 13x13
    public static final Icon Randori24 = load("/icons/randori24x24.png"); // 24x24
    public static final Icon RandoriLibModule16 = load("/icons/randoriLibModule16x16.png"); // 16x16
    public static final Icon RandoriLibModule24 = load("/icons/randoriLibModule24x24.png"); // 24x24
}
