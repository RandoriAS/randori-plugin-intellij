/***
 * Copyright 2013 Teoti Graphix, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.plugin.utils;

import javax.swing.event.HyperlinkEvent;

import org.jetbrains.annotations.NotNull;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

/**
 * @author Michael Schmalle
 */
public class NotificationUtils
{
    public static final String RANDORI = "Randori";

    public static void sendRandoriInformation(String title, String message,
            final Project project)
    {
        sendRandoriNotification(title, message, project,
                NotificationType.INFORMATION);
    }

    public static void sendRandoriError(String title, String message,
            final Project project)
    {
        sendRandoriNotification(title, message, project, NotificationType.ERROR);
    }

    public static void sendRandoriWarning(String title, String message,
            final Project project)
    {
        sendRandoriNotification(title, message, project,
                NotificationType.WARNING);
    }

    public static void sendRandoriNotification(String title, String message,
            final Project project, NotificationType notificationType)
    {
        final Notification notification = new Notification(RANDORI, title,
                message, notificationType, new NotificationListener() {
                    @Override
                    public void hyperlinkUpdate(
                            @NotNull Notification notification,
                            @NotNull HyperlinkEvent event)
                    {
                        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                        {
                            openAssociatedWindow(event, project);
                        }
                    };
                });
        notification.hideBalloon();
        Notifications.Bus.notify(notification);
    }

    private static void openAssociatedWindow(HyperlinkEvent event,
            Project project)
    {
        String windowId = event.getDescription();
        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
                .getToolWindow(windowId);
        if (toolWindow != null)
        {
            toolWindow.show(new Runnable() {
                @Override
                public void run()
                {

                }
            });
        }
    }
}
