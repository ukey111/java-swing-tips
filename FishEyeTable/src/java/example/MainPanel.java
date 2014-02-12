package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        final FishEyeTable table = new FishEyeTable(makeTestModel());
        table.setRowSelectionInterval(0,0);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(320, 240));
        add(scroll, BorderLayout.NORTH);
    }
    private static TableModel makeTestModel() {
        TestModel m = new TestModel();
        for(int i=0;i<20;i++) {
            m.addTest(new Test("Name:"+i, i%2==0?"Comment":""));
        }
        return m;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class FishEyeRowContext {
    public final int height;
    public final Font font;
    public final Color color;
    public FishEyeRowContext(int height, Font font, Color color) {
        this.height = height;
        this.font   = font;
        this.color  = color;
    }
}

class FishEyeTable extends JTable {
    private final List<FishEyeRowContext> fishEyeRowList;
    private final Font minFont;
    private int prevRow = -1;
    private int prevHeight;
    private FishEyeTableHandler handler;

    public FishEyeTable(TableModel m) {
        super(m);
        Font font = getFont();
        minFont = font.deriveFont(8.0f);
        Font font12 = font.deriveFont(10.0f);
        Font font18 = font.deriveFont(16.0f);
        Font font24 = font.deriveFont(22.0f);
        Font font32 = font.deriveFont(30.0f);
        Color color12 = new Color(250,250,250);
        Color color18 = new Color(245,245,245);
        Color color24 = new Color(240,240,240);
        Color color32 = new Color(230,230,250);

        fishEyeRowList = Arrays.asList(
            new FishEyeRowContext(12,font12,color12),
            new FishEyeRowContext(18,font18,color18),
            new FishEyeRowContext(24,font24,color24),
            new FishEyeRowContext(32,font32,color32),
            new FishEyeRowContext(24,font24,color24),
            new FishEyeRowContext(18,font18,color18),
            new FishEyeRowContext(12,font12,color12)
        );
    }
    @Override public void updateUI() {
        if(handler!=null) {
            removeMouseListener(handler);
            removeMouseMotionListener(handler);
            getSelectionModel().removeListSelectionListener(handler);
        }
        super.updateUI();
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);

        if(handler==null) {
            handler = new FishEyeTableHandler();
        }
        addMouseListener(handler);
        addMouseMotionListener(handler);
        getSelectionModel().addListSelectionListener(handler);
    }

    private class FishEyeTableHandler extends MouseAdapter implements ListSelectionListener {
        @Override public void mouseMoved(MouseEvent e) {
            int row = rowAtPoint(e.getPoint());
            if(prevRow==row) {
                return;
            }
            initRowHeigth(prevHeight, row);
            prevRow = row;
        }
        @Override public void mouseDragged(MouseEvent e) {
            int row = rowAtPoint(e.getPoint());
            if(prevRow==row) {
                return;
            }
            initRowHeigth(prevHeight, row);
            prevRow = row;
        }
        @Override public void mousePressed(MouseEvent e) {
            repaint();
        }
        @Override public void valueChanged(ListSelectionEvent e) {
            if(e.getValueIsAdjusting()) { return; }
            int row = getSelectedRow();
            if(prevRow==row) {
                return;
            }
            initRowHeigth(prevHeight, row);
            prevRow = row;
        }
    }

    @Override public void doLayout() {
        super.doLayout();
        Container p = SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if(p==null) {
            return;
        }
        int h = ((JViewport)p).getExtentSize().height;
        if(h==prevHeight) {
            return;
        }
        initRowHeigth(h, getSelectedRow());
        prevHeight = h;
    }

    @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        int rowCount = getModel().getRowCount();
        Color color = Color.WHITE;
        Font font   = minFont;
        int ccRow   = prevRow;
        int index   = 0;
        int rd2     = (fishEyeRowList.size()-1)/2;
        for(int i=-rd2;i<rowCount;i++) {
            if(ccRow-rd2<=i && i<=ccRow+rd2) {
                if(i==row) {
                    color = fishEyeRowList.get(index).color;
                    font  = fishEyeRowList.get(index).font;
                    break;
                }
                index++;
            }
        }
        c.setBackground(color);
        c.setFont(font);
        if(isRowSelected(row)) {
            c.setBackground(getSelectionBackground());
        }
        return c;
    }

    private int getViewableColoredRowCount(int ridx) {
        int rd2 = (fishEyeRowList.size()-1)/2;
        int rc  = getModel().getRowCount();
        if(rd2-ridx>0 && ridx<rd2) {
            return rd2 + 1 + ridx;
        }else if(ridx>rc-1-rd2 && ridx<rc-1+rd2) {
            return rc - ridx + rd2;
        }
        return fishEyeRowList.size();
    }

    private void initRowHeigth(int height, int ccRow) {
        int rd2      = (fishEyeRowList.size()-1)/2;
        int rowCount = getModel().getRowCount();
        int viewRc   = getViewableColoredRowCount(ccRow);
        int viewH    = 0;
        for(int i=0;i<viewRc;i++) {
            viewH += fishEyeRowList.get(i).height;
        }
        int restRc   = rowCount - viewRc;
        int restH    = height - viewH;
        int restRh   = restH/restRc; restRh = restRh>0 ? restRh : 1;
        int restGap  = restH - restRh*restRc;
        //System.out.println(String.format("%d-%d=%dx%d+%d=%d", height, viewH, restRc, restRh, restGap, restH));
        int index = -1;
        for(int i=-rd2;i<rowCount;i++) {
            int crh;
            if(ccRow-rd2<=i && i<=ccRow+rd2) {
                index++;
                if(i<0) {
                    continue;
                }
                crh = fishEyeRowList.get(index).height;
            }else{
                if(i<0) {
                    continue;
                }
                crh = restRh + (restGap>0 ? 1 : 0);
                restGap--;
            }
            setRowHeight(i, crh);
        }
    }
}

class TestModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  true),
        new ColumnContext("Comment", String.class,  true)
    };
    private int number;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getComment()};
        super.addRow(obj);
        number++;
    }
    @Override public boolean isCellEditable(int row, int col) {
        return COLUMN_ARRAY[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_ARRAY.length;
    }
    @Override public String getColumnName(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnName;
    }
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class Test {
    private String name, comment;
    public Test(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }
    public void setName(String str) {
        name = str;
    }
    public void setComment(String str) {
        comment = str;
    }
    public String getName() {
        return name;
    }
    public String getComment() {
        return comment;
    }
}
