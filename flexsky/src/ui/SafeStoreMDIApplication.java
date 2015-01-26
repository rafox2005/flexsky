/*
 * Copyright 2014 rlibardi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui;

import data.StorageOptions;
import data.DataAccount;
import data.DataFile;
import dispersal.IEncoderIDA;
import experimentPlanner.Scenario;
import experimentPlanner.ScenarioExecutor;
import experimentPlanner.ScenarioParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import management.StoreSafeManager;
import mssf.selector.DispersalSelection;
import mssf.selector.LPSelector;
import org.xml.sax.SAXException;
import storage.IDriver;

/**
 *
 * @author rlibardi
 */
public class SafeStoreMDIApplication extends javax.swing.JFrame {

    private StoreSafeManager ssm;
    private String db_path;
    private String logDB_path;

    /**
     * Creates new form SafeStoreMDIApplication
     */
    public SafeStoreMDIApplication() {
        initComponents();
        populateComponents();

    }

    private void getDBPaths() {
        int returnVal = jFileChooserDB.showOpenDialog(desktopPane);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooserDB.getSelectedFile();
            this.db_path = file.getAbsolutePath();
        }

        jFileChooserDB.setDialogTitle("Choose the SQLite LOG Database File");
        returnVal = jFileChooserDB.showOpenDialog(desktopPane);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooserDB.getSelectedFile();
            this.logDB_path = file.getAbsolutePath();
        }
    }

    private void populateComponentsUploadEasy() {

        //Create the label table
        Hashtable labelTable = new Hashtable();

        //Availability
        int nProv = ssm.getAccounts().size();
        labelTable.put(new Integer(0), new JLabel("Minimum Space"));
        labelTable.put(new Integer(nProv - 1), new JLabel("Maximum Availability"));
        jSliderAvailability.setLabelTable(labelTable);
        jSliderAvailability.setMinorTickSpacing(1);
        jSliderAvailability.setPaintTicks(true);
        jSliderAvailability.setPaintLabels(true);
        jSliderAvailability.setSnapToTicks(true);
        jSliderAvailability.setMaximum(nProv - 1);

        jSliderAvailability.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateFileSize();
            }
        });

        //Security
        labelTable = new Hashtable();
        labelTable.put(new Integer(0), new JLabel("I dont mind"));
        labelTable.put(new Integer(50), new JLabel("Confidential"));
        labelTable.put(new Integer(100), new JLabel("Very Confidential"));
        jSliderSecurity.setLabelTable(labelTable);
        jSliderSecurity.setMinorTickSpacing(50);
        jSliderSecurity.setPaintTicks(true);
        jSliderSecurity.setPaintLabels(true);
        jSliderSecurity.setSnapToTicks(true);
        jSliderSecurity.setMaximum(100);

        jSliderSecurity.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {

            }
        });

        //Access
        labelTable = new Hashtable();
        labelTable.put(new Integer(0), new JLabel("Almost never"));
        labelTable.put(new Integer(50), new JLabel("Sometimes"));
        labelTable.put(new Integer(100), new JLabel("Lots of times"));
        jSliderAccessPattern.setLabelTable(labelTable);
        jSliderAccessPattern.setMinorTickSpacing(50);
        jSliderAccessPattern.setPaintTicks(true);
        jSliderAccessPattern.setPaintLabels(true);
        jSliderAccessPattern.setSnapToTicks(true);
        jSliderAccessPattern.setMaximum(100);

        jSliderAccessPattern.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {

            }
        });

    }

    private void updateFileSize() {
        int nProv = ssm.getAccounts().size();
        long file_size = jFileChooser1.getSelectedFile().length() * nProv / (nProv - jSliderAvailability.getValue());
        jLabelFinalSize.setText(Long.toString(file_size));
    }

    private void populateComponents() {
        try {
            this.getDBPaths();

            ssm = StoreSafeManager.getInstance(this.db_path, this.logDB_path);

            //Retrieve the lists
            Set listIDA = ssm.getIDAList();
            listIDA.add(null);
            Set listPipe = ssm.getPipeList();
            listPipe.add(null);

            JList aux = new JList(listIDA.toArray());
            this.IDAList.setModel(aux.getModel());

        //aux = new JList(listPipe.toArray());
            //this.filePipelinejTable.setModel(aux.getModel());       
            this.updateAccounts();

            JComboBox comboBox = new JComboBox(listPipe.toArray());

            TableColumn filePipeColumn = this.filePipelinejTable.getColumnModel().getColumn(0);
            filePipeColumn.setCellEditor(new DefaultCellEditor(comboBox));

            //Set up tool tips for the sport cells.
            DefaultTableCellRenderer renderer
                    = new DefaultTableCellRenderer();
            renderer.setToolTipText("Click for combo box");
            filePipeColumn.setCellRenderer(renderer);

            TableColumn slicePipeColumn = this.slicePipelinejTable.getColumnModel().getColumn(0);
            slicePipeColumn.setCellEditor(new DefaultCellEditor(comboBox));
            slicePipeColumn.setCellRenderer(renderer);

            //DOWNLOAD
            List listFiles = ssm.listFiles();
            aux = new JList(listFiles.toArray());

            this.filesToDownloadJList.setModel(aux.getModel());

            //Accounts
            Set listDriver = ssm.getDriverList();
            JComboBox aux2 = new JComboBox(listDriver.toArray());

            this.driverTypejComboBox.setModel(aux2.getModel());

            //Upload Easy
            this.populateComponentsUploadEasy();
        } catch (Exception ex) {
            Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, "Critical Error: Closing application!", ex);
            System.exit(-1);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        jFileChooser1 = new javax.swing.JFileChooser();
        jDialogDB = new javax.swing.JDialog();
        jFileChooserDB = new javax.swing.JFileChooser();
        jLabelChooseDB = new javax.swing.JLabel();
        desktopPane = new javax.swing.JDesktopPane();
        mainPanel = new javax.swing.JTabbedPane();
        jScrollPaneUpload = new javax.swing.JScrollPane();
        uploadPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        IDAList = new javax.swing.JList();
        IDALabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ProviderList = new javax.swing.JList();
        ProviderList1 = new javax.swing.JList();
        ProviderLabel = new javax.swing.JLabel();
        totalPartsLabel = new javax.swing.JLabel();
        totalPartsTextField = new javax.swing.JTextField();
        revTextBox = new javax.swing.JTextField();
        reqPartsLabel = new javax.swing.JLabel();
        revLabel = new javax.swing.JLabel();
        reqPartsTextField = new javax.swing.JTextField();
        filePipelineLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        filePipelinejTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        slicePipelinejTable = new javax.swing.JTable();
        slicePipelineLabel = new javax.swing.JLabel();
        parametersLabel = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        parametersjTable = new javax.swing.JTable();
        uploadJButton = new javax.swing.JButton();
        pathToFileLabel = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();
        typeTextField = new javax.swing.JTextField();
        delProviderjButton = new javax.swing.JButton();
        jPanelDownload = new javax.swing.JPanel();
        pathToDownloadFolderjLabel = new javax.swing.JLabel();
        downloadJButton = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        filesToDownloadJList = new javax.swing.JList();
        setDownloadPathjButton = new javax.swing.JButton();
        jDownloadRefreshListButton = new javax.swing.JButton();
        deleteJButton = new javax.swing.JButton();
        jScrollPaneAccount = new javax.swing.JScrollPane();
        jPanelAccount = new javax.swing.JPanel();
        providerNameAddjLabel = new javax.swing.JLabel();
        providerNameAddjTextField = new javax.swing.JTextField();
        providerTypeAddjLabel = new javax.swing.JLabel();
        providerPathAddjLabel = new javax.swing.JLabel();
        providerPathAddjTextField = new javax.swing.JTextField();
        parametersProviderLabel = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        parametersProviderjTable = new javax.swing.JTable();
        providerAddjButton = new javax.swing.JButton();
        driverTypejComboBox = new javax.swing.JComboBox();
        jScrollPaneUploadEasy = new javax.swing.JScrollPane();
        jPanelUPloadEasy = new javax.swing.JPanel();
        jButtonChooseFile = new javax.swing.JButton();
        pathToFileLabel1 = new javax.swing.JLabel();
        uploadJButtonEasy = new javax.swing.JButton();
        jSliderAvailability = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jSliderSecurity = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSliderAccessPattern = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jLabelFinalSize = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        jDialog1.setTitle("Choose the file");
        jDialog1.setMinimumSize(jFileChooser1.getMinimumSize());

        jPanel2.setMinimumSize(jFileChooser1.getMinimumSize());

        jFileChooser1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileChooser1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 459, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 440, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 459, Short.MAX_VALUE)
            .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jDialog1Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 440, Short.MAX_VALUE)
            .addGroup(jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jDialog1Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jFileChooserDB.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        jFileChooserDB.setDialogTitle("Select the SQLite File Database");
        jFileChooserDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileChooserDBActionPerformed(evt);
            }
        });

        jLabelChooseDB.setText("Select the location of the SQLite Database");

        javax.swing.GroupLayout jDialogDBLayout = new javax.swing.GroupLayout(jDialogDB.getContentPane());
        jDialogDB.getContentPane().setLayout(jDialogDBLayout);
        jDialogDBLayout.setHorizontalGroup(
            jDialogDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogDBLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jFileChooserDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jDialogDBLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabelChooseDB))
        );
        jDialogDBLayout.setVerticalGroup(
            jDialogDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogDBLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelChooseDB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFileChooserDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FlexSky v. 0.1a - Testing Purposes Only");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        desktopPane.setLayout(new javax.swing.OverlayLayout(desktopPane));

        jScrollPaneUpload.setPreferredSize(new java.awt.Dimension(1600, 534));
        jScrollPaneUpload.setRequestFocusEnabled(false);

        jButton1.setText("Choose the file");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane1.setMaximumSize(new java.awt.Dimension(54, 226));

        IDAList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        IDAList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        IDAList.setName(""); // NOI18N
        jScrollPane1.setViewportView(IDAList);

        IDALabel.setText("Dispersal Algorithm");

        jScrollPane2.setMaximumSize(new java.awt.Dimension(145, 256));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(22, 22));

        ProviderList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        ProviderList.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ProviderList.setMaximumSize(new java.awt.Dimension(145, 256));
        ProviderList.setName(""); // NOI18N
        ProviderList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ProviderListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(ProviderList);

        ProviderList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        ProviderList1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ProviderList1.setMaximumSize(new java.awt.Dimension(145, 256));
        ProviderList1.setName(""); // NOI18N
        ProviderList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ProviderListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(ProviderList1);

        ProviderLabel.setText("Providers");

        totalPartsLabel.setText("Total Parts:");

        totalPartsTextField.setText("0");

        revTextBox.setText("0");

        reqPartsLabel.setText("Req. Parts");

        revLabel.setText("Revision");

        reqPartsTextField.setText("0");

        filePipelineLabel.setText("File Pipeline");

        filePipelinejTable.setFont(new java.awt.Font("Ubuntu", 0, 10)); // NOI18N
        filePipelinejTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                ""
            }
        ));
        filePipelinejTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        filePipelinejTable.setFocusCycleRoot(true);
        jScrollPane3.setViewportView(filePipelinejTable);

        slicePipelinejTable.setFont(new java.awt.Font("Ubuntu", 0, 10)); // NOI18N
        slicePipelinejTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                ""
            }
        ));
        jScrollPane4.setViewportView(slicePipelinejTable);

        slicePipelineLabel.setText("Slice Pipeline");

        parametersLabel.setText("Additional Parameters");

        parametersjTable.setFont(new java.awt.Font("Ubuntu", 0, 10)); // NOI18N
        parametersjTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        parametersjTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(parametersjTable);
        if (parametersjTable.getColumnModel().getColumnCount() > 0) {
            parametersjTable.getColumnModel().getColumn(0).setResizable(false);
            parametersjTable.getColumnModel().getColumn(1).setResizable(false);
        }

        uploadJButton.setText("Upload");
        uploadJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                uploadJButtonMouseClicked(evt);
            }
        });
        uploadJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadJButtonActionPerformed(evt);
            }
        });

        typeLabel.setText("Type");

        delProviderjButton.setText("Del");
        delProviderjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delProviderjButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout uploadPanelLayout = new javax.swing.GroupLayout(uploadPanel);
        uploadPanel.setLayout(uploadPanelLayout);
        uploadPanelLayout.setHorizontalGroup(
            uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(uploadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(uploadPanelLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(pathToFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(uploadPanelLayout.createSequentialGroup()
                        .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                            .addGroup(uploadPanelLayout.createSequentialGroup()
                                .addComponent(revLabel)
                                .addGap(26, 26, 26)
                                .addComponent(revTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(IDALabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 109, Short.MAX_VALUE)
                        .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ProviderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(uploadPanelLayout.createSequentialGroup()
                                .addComponent(typeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(typeTextField))
                            .addGroup(uploadPanelLayout.createSequentialGroup()
                                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(delProviderjButton)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 98, Short.MAX_VALUE)))))
                .addGap(31, 31, 31)
                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(uploadJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(uploadPanelLayout.createSequentialGroup()
                        .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(slicePipelineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(uploadPanelLayout.createSequentialGroup()
                                .addComponent(totalPartsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(totalPartsTextField))
                            .addComponent(filePipelineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(68, 68, 68)
                        .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(uploadPanelLayout.createSequentialGroup()
                                .addComponent(reqPartsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reqPartsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(parametersLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(70, 70, 70))
        );
        uploadPanelLayout.setVerticalGroup(
            uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(uploadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(pathToFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(uploadPanelLayout.createSequentialGroup()
                        .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(typeLabel)
                            .addComponent(typeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ProviderLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(uploadPanelLayout.createSequentialGroup()
                        .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(totalPartsLabel)
                                .addComponent(totalPartsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(reqPartsLabel)
                                .addComponent(reqPartsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(revTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(revLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(uploadPanelLayout.createSequentialGroup()
                                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(filePipelineLabel)
                                    .addComponent(parametersLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(uploadPanelLayout.createSequentialGroup()
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(9, 9, 9)
                                        .addComponent(slicePipelineLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                            .addGroup(uploadPanelLayout.createSequentialGroup()
                                .addComponent(IDALabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(uploadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(uploadJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delProviderjButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPaneUpload.setViewportView(uploadPanel);

        mainPanel.addTab("Upload-Advanced", jScrollPaneUpload);

        downloadJButton.setText("Download");
        downloadJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                downloadJButtonMouseClicked(evt);
            }
        });

        filesToDownloadJList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane6.setViewportView(filesToDownloadJList);

        setDownloadPathjButton.setText("Set Download Path");
        setDownloadPathjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDownloadPathjButtonActionPerformed(evt);
            }
        });

        jDownloadRefreshListButton.setText("Refresh");
        jDownloadRefreshListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDownloadRefreshListButtonActionPerformed(evt);
            }
        });

        deleteJButton.setText("Delete");
        deleteJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteJButtonMouseClicked(evt);
            }
        });
        deleteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelDownloadLayout = new javax.swing.GroupLayout(jPanelDownload);
        jPanelDownload.setLayout(jPanelDownloadLayout);
        jPanelDownloadLayout.setHorizontalGroup(
            jPanelDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDownloadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6)
                    .addGroup(jPanelDownloadLayout.createSequentialGroup()
                        .addGroup(jPanelDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelDownloadLayout.createSequentialGroup()
                                .addComponent(pathToDownloadFolderjLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanelDownloadLayout.createSequentialGroup()
                                .addComponent(setDownloadPathjButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 591, Short.MAX_VALUE)
                                .addComponent(jDownloadRefreshListButton)
                                .addGap(21, 21, 21)))
                        .addComponent(downloadJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelDownloadLayout.setVerticalGroup(
            jPanelDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDownloadLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(downloadJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(deleteJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelDownloadLayout.createSequentialGroup()
                        .addGroup(jPanelDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(setDownloadPathjButton)
                            .addComponent(jDownloadRefreshListButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pathToDownloadFolderjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(139, Short.MAX_VALUE))
        );

        mainPanel.addTab("Files Stored", jPanelDownload);

        providerNameAddjLabel.setText("Name");

        providerTypeAddjLabel.setText("Type");

        providerPathAddjLabel.setText("Path");

        parametersProviderLabel.setText("Additional Parameters");

        parametersProviderjTable.setFont(new java.awt.Font("Ubuntu", 0, 10)); // NOI18N
        parametersProviderjTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        parametersProviderjTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane8.setViewportView(parametersProviderjTable);
        if (parametersProviderjTable.getColumnModel().getColumnCount() > 0) {
            parametersProviderjTable.getColumnModel().getColumn(0).setResizable(false);
            parametersProviderjTable.getColumnModel().getColumn(1).setResizable(false);
        }

        providerAddjButton.setText("Add");
        providerAddjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                providerAddjButtonActionPerformed(evt);
            }
        });

        driverTypejComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanelAccountLayout = new javax.swing.GroupLayout(jPanelAccount);
        jPanelAccount.setLayout(jPanelAccountLayout);
        jPanelAccountLayout.setHorizontalGroup(
            jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAccountLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAccountLayout.createSequentialGroup()
                        .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelAccountLayout.createSequentialGroup()
                                .addComponent(providerPathAddjLabel)
                                .addGap(18, 18, 18)
                                .addComponent(providerPathAddjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelAccountLayout.createSequentialGroup()
                                .addComponent(providerNameAddjLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(providerNameAddjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelAccountLayout.createSequentialGroup()
                                .addComponent(providerTypeAddjLabel)
                                .addGap(18, 18, 18)
                                .addComponent(driverTypejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelAccountLayout.createSequentialGroup()
                        .addComponent(providerAddjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37)))
                .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parametersProviderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelAccountLayout.setVerticalGroup(
            jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAccountLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelAccountLayout.createSequentialGroup()
                        .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(providerNameAddjLabel)
                            .addComponent(providerNameAddjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(providerPathAddjLabel)
                            .addComponent(providerPathAddjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelAccountLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelAccountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(providerTypeAddjLabel)
                                    .addComponent(driverTypejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelAccountLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(providerAddjButton))))
                    .addGroup(jPanelAccountLayout.createSequentialGroup()
                        .addComponent(parametersProviderLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        jScrollPaneAccount.setViewportView(jPanelAccount);

        mainPanel.addTab("Account", jScrollPaneAccount);

        jScrollPaneUploadEasy.setEnabled(false);
        jScrollPaneUploadEasy.setFocusable(false);

        jPanelUPloadEasy.setEnabled(false);
        jPanelUPloadEasy.setFocusable(false);

        jButtonChooseFile.setText("Choose the file");
        jButtonChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseFileActionPerformed(evt);
            }
        });

        uploadJButtonEasy.setText("Upload");
        uploadJButtonEasy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                uploadJButtonEasyMouseClicked(evt);
            }
        });
        uploadJButtonEasy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadJButtonEasyActionPerformed(evt);
            }
        });

        jLabel1.setText("Availability x Storage Space Overhead");

        jLabel2.setText("Security");

        jLabel3.setText("How often will you read/write this file?");

        jLabel4.setText("Final Dispersed File Size: ");

        javax.swing.GroupLayout jPanelUPloadEasyLayout = new javax.swing.GroupLayout(jPanelUPloadEasy);
        jPanelUPloadEasy.setLayout(jPanelUPloadEasyLayout);
        jPanelUPloadEasyLayout.setHorizontalGroup(
            jPanelUPloadEasyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelUPloadEasyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelUPloadEasyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelUPloadEasyLayout.createSequentialGroup()
                        .addComponent(jSliderAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelFinalSize))
                    .addComponent(jSliderSecurity, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jSliderAccessPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jButtonChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(339, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelUPloadEasyLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(uploadJButtonEasy, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(413, 413, 413))
            .addGroup(jPanelUPloadEasyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelUPloadEasyLayout.createSequentialGroup()
                    .addGap(203, 203, 203)
                    .addComponent(pathToFileLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 705, Short.MAX_VALUE)
                    .addGap(175, 175, 175)))
        );
        jPanelUPloadEasyLayout.setVerticalGroup(
            jPanelUPloadEasyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelUPloadEasyLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jButtonChooseFile)
                .addGap(37, 37, 37)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelUPloadEasyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSliderAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelUPloadEasyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(jLabelFinalSize)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderSecurity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderAccessPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 164, Short.MAX_VALUE)
                .addComponent(uploadJButtonEasy, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(115, 115, 115))
            .addGroup(jPanelUPloadEasyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelUPloadEasyLayout.createSequentialGroup()
                    .addGap(21, 21, 21)
                    .addComponent(pathToFileLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(401, 401, 401)))
        );

        jScrollPaneUploadEasy.setViewportView(jPanelUPloadEasy);

        mainPanel.addTab("Upload-Easy (Not Working)", jScrollPaneUploadEasy);

        desktopPane.add(mainPanel);

        getContentPane().add(desktopPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1090, 580));

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('d');
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentMenuItem.setMnemonic('c');
        contentMenuItem.setText("Contents");
        helpMenu.add(contentMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void jFileChooser1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooser1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFileChooser1ActionPerformed

    private void jFileChooserDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooserDBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFileChooserDBActionPerformed

    private void jDownloadRefreshListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDownloadRefreshListButtonActionPerformed
        // TODO add your handling code here:
        List listFiles = ssm.listFiles();
        JList aux = new JList(listFiles.toArray());

        this.filesToDownloadJList.setModel(aux.getModel());

    }//GEN-LAST:event_jDownloadRefreshListButtonActionPerformed

    private void setDownloadPathjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDownloadPathjButtonActionPerformed
        // TODO add your handling code here:
        int returnVal = jFileChooser1.showSaveDialog(jPanel2);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser1.getSelectedFile();
            this.pathToDownloadFolderjLabel.setText(file.getAbsolutePath());
            jDialog1.setVisible(false);
        } else {
            jDialog1.setVisible(false);
        }
    }//GEN-LAST:event_setDownloadPathjButtonActionPerformed

    private void downloadJButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadJButtonMouseClicked
        List<DataFile> ssfList = this.filesToDownloadJList.getSelectedValuesList();

        for (DataFile storeSafeFile : ssfList) {
            String path = this.pathToDownloadFolderjLabel.getText();
            ssm.downloadFile(path, storeSafeFile);
        }
    }//GEN-LAST:event_downloadJButtonMouseClicked

    private void deleteJButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteJButtonMouseClicked

        List<DataFile> ssfList = this.filesToDownloadJList.getSelectedValuesList();
        for (DataFile storeSafeFile : ssfList) {
            ssm.deleteFile(storeSafeFile);
        }
    }//GEN-LAST:event_deleteJButtonMouseClicked

    private void deleteJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteJButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteJButtonActionPerformed

    private void uploadJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadJButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_uploadJButtonActionPerformed

    private void uploadJButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadJButtonMouseClicked

        //Get the Dispersal algorithm
        String dispersalMethod = this.IDAList.getSelectedValue().toString().substring(31);

        //Get the file path
        String path = this.pathToFileLabel.getText();

        //Get the type
        String type = this.typeTextField.getText();

        //Get the parts and revision
        int totalParts = Integer.parseInt(this.totalPartsTextField.getText());
        int reqParts = Integer.parseInt(this.reqPartsTextField.getText());
        int revision = Integer.parseInt(this.revTextBox.getText());

        StoreSafeManager instance = this.ssm;
        ArrayList listAccounts = new ArrayList(this.ProviderList1.getSelectedValuesList());

        //Get file Pipeline
        ArrayList filePipeline = new ArrayList();
        TableModel fileTM = this.filePipelinejTable.getModel();

        for (int i = 0; i < this.filePipelinejTable.getModel().getRowCount(); i++) {
            if (fileTM.getValueAt(i, 0) != null) {
                try {
                    Class aux = (Class) fileTM.getValueAt(i, 0);

                    filePipeline.add(aux.getConstructor().newInstance());
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        //Get slice Pipeline
        ArrayList slicePipeline = new ArrayList();
        TableModel sliceTM = this.slicePipelinejTable.getModel();

        for (int i = 0; i < sliceTM.getRowCount(); i++) {
            if (sliceTM.getValueAt(i, 0) != null) {
                try {
                    Class aux = (Class) sliceTM.getValueAt(i, 0);

                    slicePipeline.add(aux.getConstructor().newInstance());
                } catch (InstantiationException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        //Get additionalk paramteres
        HashMap<String, String> param = new HashMap<String, String>();

        TableModel paramTM = this.parametersjTable.getModel();

        for (int i = 0; i < paramTM.getRowCount(); i++) {
            if (paramTM.getValueAt(i, 0) != null && paramTM.getValueAt(i, 1) != null) {
                param.put(paramTM.getValueAt(i, 0).toString(), paramTM.getValueAt(i, 1).toString());
            }
        }

        StorageOptions options = new StorageOptions(filePipeline, slicePipeline, param);

        boolean result = instance.storeFile(path, type, dispersalMethod, totalParts, reqParts, revision, listAccounts, options);

        if (result == true) {
            JOptionPane.showMessageDialog(this.desktopPane, "Upload success!");
        }
    }//GEN-LAST:event_uploadJButtonMouseClicked

    private void ProviderListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ProviderListMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_ProviderListMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        jDialog1.setVisible(true);
        jDialog1.toFront();

        int returnVal = jFileChooser1.showOpenDialog(jPanel2);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser1.getSelectedFile();
            this.pathToFileLabel.setText(file.getAbsolutePath());
            jDialog1.setVisible(false);
        } else {
            jDialog1.setVisible(false);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void delProviderjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delProviderjButtonActionPerformed
        List<DataAccount> accounts = this.ProviderList1.getSelectedValuesList();

        for (DataAccount account : accounts) {
            this.ssm.delAccount(account);
        }

        this.updateAccounts();

    }//GEN-LAST:event_delProviderjButtonActionPerformed

    private void jButtonChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseFileActionPerformed
        // TODO add your handling code here:
        jDialog1.setVisible(true);
        jDialog1.toFront();

        int returnVal = jFileChooser1.showOpenDialog(jPanel2);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser1.getSelectedFile();
            this.pathToFileLabel1.setText(file.getAbsolutePath());
            jDialog1.setVisible(false);
        } else {
            jDialog1.setVisible(false);
        }
    }//GEN-LAST:event_jButtonChooseFileActionPerformed

    private void uploadJButtonEasyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadJButtonEasyMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_uploadJButtonEasyMouseClicked

    private void uploadJButtonEasyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadJButtonEasyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_uploadJButtonEasyActionPerformed

    private void providerAddjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_providerAddjButtonActionPerformed
        String name = providerNameAddjTextField.getText();
        String path = providerPathAddjTextField.getText();
        String type = driverTypejComboBox.getSelectedItem().toString().substring(6);

        //Get additionalk paramteres
        HashMap<String, String> param = new HashMap<String, String>();

        TableModel paramTM = this.parametersProviderjTable.getModel();

        for (int i = 0; i < paramTM.getRowCount(); i++) {
            if (paramTM.getValueAt(i, 0) != null && paramTM.getValueAt(i, 1) != null) {
                param.put(paramTM.getValueAt(i, 0).toString(), paramTM.getValueAt(i, 1).toString());
            }
        }

        DataAccount account = new DataAccount(name, type, path);
        account.setAdditionalParameters(param);

        this.ssm.addAccount(account);

        this.updateAccounts();
    }//GEN-LAST:event_providerAddjButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        //Check if UI, SelectorOnly or Experiment Planner to be used.
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("SELECTONLY")) {
                StoreSafeManager ssm = StoreSafeManager.getInstance(args[1], args[2]);

                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put("model_path", "C:\\Users\\Rafox\\Documents\\NetBeansProjects\\flexsky\\flexsky\\optimization.mod");

                    //User parameters - TODO
                    HashMap<String, Number> userParam = new HashMap<>();
                    userParam.put("MIN_SEC", -20);
                    userParam.put("MIN_PERF", -20);
                    userParam.put("MIN_STO", -20);

                    userParam.put("WEIGHT_SEC", 0.8);
                    userParam.put("WEIGHT_PERF", 0);
                    userParam.put("WEIGHT_STO", 0);
                    userParam.put("WEIGHT_STOCOST", 0.1);
                    userParam.put("WEIGHT_BWCOST", 0.1);
                    userParam.put("WEIGHT_AVAIL", 0);
                    userParam.put("WEIGHT_DUR", 0);

                    userParam.put("PROV_REQ", Integer.parseInt(args[3]));

                    LPSelector lps = new LPSelector();
                    DispersalSelection ds = lps.select(new ArrayList(ssm.getAccounts()), ssm.listModules(), userParam, parameters);

            }
            else if (args[0].equalsIgnoreCase("SCENARIO")){
            try {
                Scenario test = ScenarioParser.parse(args[1]);
                ScenarioExecutor ex = ScenarioExecutor.getInstance();
                ex.execute(test);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        } else {

            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
             */
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(SafeStoreMDIApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>

            /* Create and display the form */
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new SafeStoreMDIApplication().setVisible(true);
                }
            });
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel IDALabel;
    private javax.swing.JList IDAList;
    private javax.swing.JLabel ProviderLabel;
    private javax.swing.JList ProviderList;
    private javax.swing.JList ProviderList1;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JButton delProviderjButton;
    private javax.swing.JButton deleteJButton;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JButton downloadJButton;
    private javax.swing.JComboBox driverTypejComboBox;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel filePipelineLabel;
    private javax.swing.JTable filePipelinejTable;
    private javax.swing.JList filesToDownloadJList;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonChooseFile;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialogDB;
    private javax.swing.JButton jDownloadRefreshListButton;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFileChooser jFileChooserDB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelChooseDB;
    private javax.swing.JLabel jLabelFinalSize;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelAccount;
    private javax.swing.JPanel jPanelDownload;
    private javax.swing.JPanel jPanelUPloadEasy;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPaneAccount;
    private javax.swing.JScrollPane jScrollPaneUpload;
    private javax.swing.JScrollPane jScrollPaneUploadEasy;
    private javax.swing.JSlider jSliderAccessPattern;
    private javax.swing.JSlider jSliderAvailability;
    private javax.swing.JSlider jSliderSecurity;
    private javax.swing.JTabbedPane mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JLabel parametersProviderLabel;
    private javax.swing.JTable parametersProviderjTable;
    private javax.swing.JTable parametersjTable;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JLabel pathToDownloadFolderjLabel;
    private javax.swing.JLabel pathToFileLabel;
    private javax.swing.JLabel pathToFileLabel1;
    private javax.swing.JButton providerAddjButton;
    private javax.swing.JLabel providerNameAddjLabel;
    private javax.swing.JTextField providerNameAddjTextField;
    private javax.swing.JLabel providerPathAddjLabel;
    private javax.swing.JTextField providerPathAddjTextField;
    private javax.swing.JLabel providerTypeAddjLabel;
    private javax.swing.JLabel reqPartsLabel;
    private javax.swing.JTextField reqPartsTextField;
    private javax.swing.JLabel revLabel;
    private javax.swing.JTextField revTextBox;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton setDownloadPathjButton;
    private javax.swing.JLabel slicePipelineLabel;
    private javax.swing.JTable slicePipelinejTable;
    private javax.swing.JLabel totalPartsLabel;
    private javax.swing.JTextField totalPartsTextField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField typeTextField;
    private javax.swing.JButton uploadJButton;
    private javax.swing.JButton uploadJButtonEasy;
    private javax.swing.JPanel uploadPanel;
    // End of variables declaration//GEN-END:variables

    private void updateAccounts() {
        //Easy bind for providers
        JList aux = new JList(ssm.getAccounts().toArray());
        this.ProviderList1.setModel(aux.getModel());

    }

}
