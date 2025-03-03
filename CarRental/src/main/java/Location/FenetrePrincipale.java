package Location;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class FenetrePrincipale extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5094469871654926439L;

	private BdD _bdd = null;
	private JTable _tableau = null;
	private DefaultTableModel _model = null;

	public FenetrePrincipale() throws HeadlessException {
		// initialisation des parametres de la fenetre
		super();
		setTitle("Location Voiture");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
		
		//Les titres des colonnes de la JTable
		String  title[] = {"id", "license_plate", "brand", "model", "status"};

        // Modèle de données pour la JTable
        _model = new DefaultTableModel(title, 0) {
			private static final long serialVersionUID = -127357999526842396L;

			@Override
            public Class<?> getColumnClass(int columnIndex) {
                // Retourne le type de la colonne
                if (columnIndex == 4) {
                    return JButton.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        
        // connexion a la base de données
		try {
	        _bdd = new BdD(_model);
			_bdd.majTable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
		// instanciation de la JTable
		_tableau = new JTable(_model);
		
        // Ajouter un renderiseur de cellule pour la colonne 4 qui contient des boutons
		_tableau.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());

        // Ajouter un éditeur de cellule pour la colonne 4 pour afficher le bouton
		_tableau.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

		//Nous ajoutons notre tableau à notre contentPane dans un scroll
		//Sinon les titres des colonnes ne s'afficheront pas !
		this.getContentPane().add(new JScrollPane(_tableau));

	}



    // Renderer pour afficher le JButton dans la JTable
    class ButtonRenderer extends JButton implements TableCellRenderer {
		private static final long serialVersionUID = 8210188608232217845L;

		public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
            	setText("");
            } else {
            	if (value.getClass() == JButton.class) {
            		setText(((JButton)value).getText());

            	} else {
            		setText(value.toString());
            	}
            }
            return this;
        }
    }

    // Editeur pour gérer l'action du JButton dans la JTable
    class ButtonEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1219566654147830204L;
		private JButton _button = null;
        private String _label = null;
        private int _row = 0;
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            _button = new JButton();
            _button.setOpaque(true);

            // Ajouter un écouteur d'événements pour le bouton
            _button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    try {
                    	
                    	// update de la table
						_bdd.update(_model.getValueAt(_row, 1).toString(), e.getActionCommand());
					} catch (SQLException e1) {
	                    JOptionPane.showMessageDialog(_button, "erreur SQL !");
					}
                    
                }
            });
        }

        // maj du label du bouton
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value == null) {
            	_label = ("");
            } else {
            	if (value.getClass() == JButton.class) {
            		_label = (((JButton)value).getText());

            	} else {
            		_label = (value.toString());
            	}
            }
            _button.setText(_label);
            _row = row;
            return _button;
        }

        @Override
        public Object getCellEditorValue() {
            return _label;
        }
    }
}
