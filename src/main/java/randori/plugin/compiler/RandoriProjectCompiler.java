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

package randori.plugin.compiler;

import org.apache.flex.compiler.internal.workspaces.Workspace;
import randori.compiler.internal.projects.RandoriApplicationProject;

/**
 * @author Frédéric THOMAS
 * Date: 09/04/13
 * Time: 02:16
 */
class RandoriProjectCompiler extends RandoriApplicationProject {
    public RandoriProjectCompiler(Workspace workspace) {
        super(workspace);
    }
}
