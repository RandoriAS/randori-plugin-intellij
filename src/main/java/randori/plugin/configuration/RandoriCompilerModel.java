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

package randori.plugin.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author Frédéric THOMAS
 */

@State(name = RandoriCompilerModel.COMPONENT_NAME, storages = {
        @Storage(id = "default", file = "$PROJECT_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/randoriCompiler.xml", scheme = StorageScheme.DIRECTORY_BASED) })
public class RandoriCompilerModel implements PersistentStateComponent<RandoriCompilerModel>
{

    public static final String COMPONENT_NAME = "RandoriCompilerModel";

    private static final int CORES_COUNT = Runtime.getRuntime().availableProcessors();
    public static final boolean makeProjectOnSaveEnabled = CORES_COUNT > 2;

    private boolean makeOnSave;
    private boolean showDebugInfo;

    @NotNull
    public static RandoriCompilerModel getInstance(Project project)
    {
        return ServiceManager.getService(project, RandoriCompilerModel.class);
    }

    public RandoriCompilerModel()
    {
        makeOnSave = true;
    }

    //----------------------------------
    // makeOnSave
    //----------------------------------
    public boolean isMakeOnSave()
    {
        return makeOnSave && makeProjectOnSaveEnabled;
    }

    public void setMakeOnSave(boolean makeOnSave)
    {
        this.makeOnSave = makeOnSave;
    }

    //----------------------------------
    // showDebugInfo
    //----------------------------------
    public boolean isShowDebugInfo() {
        return showDebugInfo;
    }

    public void setShowDebugInfo(boolean showDebugInfo) {
        this.showDebugInfo = showDebugInfo;
    }

    //----------------------------------
    // webRoot
    //----------------------------------

    private String webRoot = null;

    /**
     * For the time being, the default webroot will be the project root.
     */
    public String getWebRoot()
    {
        return webRoot;
    }

    public void setWebRoot(String value)
    {
        webRoot = value;
    }

    //----------------------------------
    // port
    //----------------------------------

    private int port = -1;

    public int getPort()
    {
        return port;
    }

    public void setPort(int value)
    {
        port = value;
    }

    //----------------------------------
    // basePath
    //----------------------------------

    private String basePath = "generated";

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String value)
    {
        basePath = value;
    }

    //----------------------------------
    // libraryPath
    //----------------------------------

    private String libraryPath = "generated/lib";

    public String getLibraryPath()
    {
        return libraryPath;
    }

    public void setLibraryPath(String value)
    {
        libraryPath = value;
    }

    //----------------------------------
    // validateCSSClasses
    //----------------------------------

    private boolean validateCSSClasses = false;

    public boolean isValidateCSSClasses()
    {
        return validateCSSClasses;
    }

    public void setValidateCSSClasses(boolean value)
    {
        validateCSSClasses = value;
    }

    //----------------------------------
    // classesAsFile
    //----------------------------------

    private boolean classesAsFile = true;

    public boolean isClassesAsFile()
    {
        return classesAsFile;
    }

    public void setClassesAsFile(boolean value)
    {
        classesAsFile = value;
    }

    @Nullable
    @Override
    public RandoriCompilerModel getState()
    {
        return this;
    }

    @Override
    public void loadState(RandoriCompilerModel state)
    {
        XmlSerializerUtil.copyBean(state, this);
    }
}
