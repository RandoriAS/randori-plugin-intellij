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

package randori.plugin.util;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import randori.plugin.configuration.RandoriCompilerModel;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public final class LogUtils
{

    public static String dumpStackTrace(StackTraceElement[] elements)
    {
        String dump = "";
        for (StackTraceElement element : elements)
        {
            dump += element.toString() + "\n";
        }
        return dump;
    }

    public static void dumpCompilationInfo(Logger log, @NotNull Project project, @NotNull String configuration) {
        System.out.println();
        System.out.println("----------------- Dumping compilation info ----------------");
        System.out.println(configuration);
        System.out.println("-------------- Ends Dumping compilation info --------------");
        System.out.println();

        log.info(project.getName() + ": " + configuration);

        final RandoriCompilerModel state = RandoriCompilerModel.getInstance(project).getState();

        if (state != null && state.isShowDebugInfo())
            NotificationUtils.sendRandoriNotification(NotificationUtils.COMPILER, "Dumping compilation info",
                    configuration, project, NotificationType.WARNING);
    }
}
