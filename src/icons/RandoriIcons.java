package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public class RandoriIcons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, RandoriIcons.class);
    }

    public static final Icon JumpToArrow = load("/icons/jumpto.png"); // 12x12
    public static final Icon Randori16 = load("/icons/randori.png"); // 16x16
    public static final Icon Randori13 = load("/icons/randori13x13.png"); // 16x16
    public static final Icon Randori24 = load("/icons/randorix2.png"); // 16x16
}
