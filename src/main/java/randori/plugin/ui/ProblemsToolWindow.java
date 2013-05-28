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

package randori.plugin.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.annotations.DefaultSeverity;

import randori.plugin.components.RandoriProjectComponent;
import randori.plugin.service.ProblemsService;
import randori.plugin.service.ProblemsService.OnProblemServiceListener;
import randori.plugin.util.ProjectUtils;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.table.JBTable;
import icons.RandoriIcons;

// TODO added an error log to the ProblemsService, the collection should not
//      contain null File instances on CompilerProblems, left null checks in

/**
 * @author Michael Schmalle
 */
public class ProblemsToolWindow
{
    private static final String[] COLUMN_TITLES = new String[] { "Description",
            "Resource", "Path", "Location", "Type" };
    private static ProblemsToolWindow instance;
    private final List<Integer> columnSizes;
    @SuppressWarnings("unused")
    private final ProblemsService service;
    private final ContentManager contentManager;
    @SuppressWarnings("unused")
    private ToolWindow window;
    private JBTable table;
    private JPanel jPanel;

    public ProblemsToolWindow(ToolWindow window, ProblemsService service,
            List<Integer> columnSizes)
    {
        instance = this;

        this.window = window;
        this.service = service;
        this.columnSizes = columnSizes;

        service.addListener(new OnProblemServiceListener() {
            @Override
            public void onReset()
            {
                refreshTree(new HashSet<ICompilerProblem>());
            }

            @Override
            public void onChange(Set<ICompilerProblem> problems)
            {
                refreshTree(problems);
            }
        });

        contentManager = window.getContentManager();

        create();

        refreshTree(service.getProblems());
    }

    public static ProblemsToolWindow getInstance()
    {
        return instance;
    }

    private ProblemsTableModel getModel()
    {
        return (ProblemsTableModel) table.getModel();
    }

    private void create()
    {
        contentManager.removeAllContents(true);

        String tableName = "Randori Project";

        jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        // create a table with columns and rows
        table = new JBTable();
        table.setCellSelectionEnabled(false);
        table.setAutoCreateRowSorter(true);

        // the scrollpane holds the table which is layed out FULL and will
        // not fit inside the parent Panel without scroll
        JBScrollPane jbScrollPane = new JBScrollPane(table);
        jbScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        jPanel.add(jbScrollPane, BorderLayout.CENTER);

        Content content = ContentFactory.SERVICE.getInstance().createContent(
                jPanel, tableName, false);
        contentManager.addContent(content);

        ListSelectionModel listMod = table.getSelectionModel();
        listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listMod.addListSelectionListener(new TableMouseListener());

        table.setEnableAntialiasing(true);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.getTableHeader().setReorderingAllowed(false);

        table.setModel(new ProblemsTableModel());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    handleDoubleClick(e);
                }
            }
        });
        table.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e)
            {
                if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED
                        && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
                {
                    saveColumnWidths(table, columnSizes);
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                int r = table.rowAtPoint(e.getPoint());
                if (r >= 0 && r < table.getRowCount())
                {
                    table.setRowSelectionInterval(r, r);
                }
                else
                {
                    table.clearSelection();
                }

                int rowIndex = table.getSelectedRow();
                if (rowIndex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JBTable)
                {
                    JPopupMenu popupMenu = createPopupMenu(rowIndex);
                    if (popupMenu != null)
                    {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private JPopupMenu createPopupMenu(final int rowIndex)
    {
        String menuTitle = generateMenuTitle(rowIndex);
        if (menuTitle != null)
        {
            JPopupMenu popupMenu = new JPopupMenu();
            ActionListener menuListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    jumpToProblemInFile(rowIndex);
                }
            };
            JMenuItem item;
            popupMenu.add(item = new JMenuItem(menuTitle,
                    RandoriIcons.JumpToArrow));
            item.setHorizontalTextPosition(SwingConstants.RIGHT);
            item.addActionListener(menuListener);
            return popupMenu;
        }
        return null;
    }

    private String generateMenuTitle(int rowIndex)
    {
        rowIndex = table.convertRowIndexToModel(rowIndex);
        ProblemsTableModel model = (ProblemsTableModel) table.getModel();
        ICompilerProblem problem = model.getProblemAt(rowIndex);
        if (problem != null)
        {
            return "Jump to "
                    + model.getSeverity(problem).toLowerCase() + " on line "
                    + problem.getLine() + " in " + model.getName(problem);
        }
        return null;
    }

    private void saveColumnWidths(JBTable table, List<Integer> columnSizes)
    {
        for (int i = 0; i < columnSizes.size(); i++)
        {
            TableColumn column = table.getColumnModel().getColumn(i);
            columnSizes.set(i, column.getWidth());
        }
    }

    private void setColumnWidths(JBTable table, List<Integer> columnSizes)
    {
        for (int i = 0; i < columnSizes.size(); i++)
        {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnSizes.get(i));
        }
        table.doLayout();
    }

    void refreshTree(Set<ICompilerProblem> problems)
    {
        saveColumnWidths(table, columnSizes);

        if (columnSizes.size() != 0 && columnSizes.get(0) > -1)
        {
            setColumnWidths(table, columnSizes);
        }

        getModel().setProblems(new ArrayList<ICompilerProblem>(problems));
        // allows the table to be updated without reseting the columns
        getModel().fireTableDataChanged();
    }

    private void handleDoubleClick(MouseEvent e)
    {
        int rowIndex = table.rowAtPoint(e.getPoint());
        if (rowIndex > -1)
        {
            jumpToProblemInFile(rowIndex);
        }
    }

    private void jumpToProblemInFile(int rowIndex)
    {
        rowIndex = table.convertRowIndexToModel(rowIndex);
        ProblemsTableModel model = (ProblemsTableModel) table.getModel();
        ICompilerProblem problem = model.getProblemAt(rowIndex);
        if (!isValid(problem))
            return;

        RandoriProjectComponent component = ProjectUtils.findProjectComponent(
                jPanel, RandoriProjectComponent.class);
        component.openFileForProblem(problem);
    }

    private boolean isValid(ICompilerProblem problem)
    {
        return problem.getLine() != -1;
    }

    @SuppressWarnings("serial")
    private static class IconRenderer extends DefaultTableCellRenderer
    {
        @Override
        public void setValue(Object value)
        {
            if (value == null)
            {
                setText("");
            }
            else
            {
                setIcon((Icon) value);
            }
        }
    }

    @SuppressWarnings("serial")
    public static class ProblemsTableModel extends AbstractTableModel
    {
        private List<ICompilerProblem> problems;

        public ProblemsTableModel()
        {
        }

        void setProblems(List<ICompilerProblem> problems)
        {
            this.problems = problems;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Integer.class;
            case 4:
                return String.class;
            }
            return Object.class;
        }

        @Override
        public int getRowCount()
        {
            if (problems == null)
                return 0;
            return problems.size();
        }

        @Override
        public String getColumnName(int column)
        {
            return COLUMN_TITLES[column];
        }

        @Override
        public int getColumnCount()
        {
            return COLUMN_TITLES.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return getProblemValue(columnIndex, problems.get(rowIndex));
        }

        private String getProblemValue(int index, ICompilerProblem problem)
        {
            if (problem != null)
            {
                switch (index)
                {
                case 0:
                    return problem.toString();
                case 1:
                    return getName(problem);
                case 2:
                    return getPath(problem);
                case 3:
                    return Integer.toString(problem.getLine());
                case 4:
                    return getSeverity(problem);
                }
            }
            return "Not found";
        }

        public String getSeverity(ICompilerProblem problem)
        {
            if (problem == null)
                return "";

            DefaultSeverity defaultSeverity = problem.getClass().getAnnotation(
                    DefaultSeverity.class);
            return defaultSeverity.value().toString();
        }

        private String getPath(ICompilerProblem problem)
        {
            String result = "";

            if (problem != null)
            {
                File file = new File(problem.getSourcePath());

                if (file.exists())
                    result = file.getParent();
            }

            return result;
        }

        public String getName(ICompilerProblem problem)
        {
            String result = "";

            if (problem != null)
            {
                File file = new File(problem.getSourcePath());

                if (file.exists())
                    result = file.getName();
            }

            return result;
        }

        public ICompilerProblem getProblemAt(int row)
        {
            // TODO the problems should NEVER be null, fix this
            return (problems != null) ? problems.get(row) : null;
        }
    }

    private class TableMouseListener implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
        }
    }
}
