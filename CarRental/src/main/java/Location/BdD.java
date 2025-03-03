package Location;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.table.DefaultTableModel;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

public class BdD extends TimerTask {

	private Connection _conn = null;
	private PGConnection _pgconn = null;
	private Statement _stmt = null;
	private DefaultTableModel _tableModel = null;

	public BdD(DefaultTableModel tableModel) throws SQLException {
		// connexion a la base de données
		super();
		_tableModel = tableModel;
		String url = "jdbc:postgresql://localhost:5432/db_location";
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "HCvSkMSfGs9JFLU6neHi");
		props.setProperty("ssl", "false");
		_conn = DriverManager.getConnection(url, props);
		_conn.setAutoCommit(true);

		// connexion a la notification du trigger sur update de la table location
		_pgconn = _conn.unwrap(PGConnection.class);
		_stmt = _conn.createStatement();
		_stmt.execute("LISTEN update_location_channel");
		
		// timer pour lecture de presence de notification
		Timer timer = new Timer();
		timer.schedule(this, 0, 5000);

	}
	
    public void run() {
		try {
			PGNotification[] notifications = _pgconn.getNotifications();
			if (notifications != null) {
				
				// en ca s de notification misa a jour de la JTable
				majTable();
				
				// @ TODO : maj uniquement la ligne de la JTable concernée
				//for (PGNotification notification : notifications) {
				//	JsonParser parser = Json.createParser(notification.getParameter()));
				//}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
     }

	public void majTable() throws SQLException {

		// suppression de lignes existantes
		_tableModel.setRowCount(0);
		
		// La requête SQL de lecture de la table location classée par ordre de ID
		String query = "SELECT id, license_plate, brand, model, status FROM location ORDER BY id ASC";
		Statement stmt;
		stmt = _conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		// Traiter les résultats de la requête
		while (rs.next()) {
			int id = rs.getInt("id");
			String license_plate = rs.getString("license_plate");
			String brand = rs.getString("brand");
			String model = rs.getString("model");
			String status = rs.getString("status");
			_tableModel.addRow(new Object[]{id, license_plate, brand, model, status});
		}
		stmt.close();
	}

	public void close() throws SQLException {
		// fermeture de la connexion à la base de données
		_conn.close();
	}

	public void update(String license_plate, String status) throws SQLException {
		String newStatus = "RENTED";
		if (status.compareTo("RENTED") == 0) {
			newStatus = "AVAILABLE";
		}
		
		// requete de update de la table location en fonction de license_plate et du status courant
		PreparedStatement ps = _conn.prepareStatement("UPDATE location SET status = ? WHERE status = ? AND license_plate = ?;");
		ps.setString(1, newStatus);
		ps.setString(2, status);
		ps.setString(3, license_plate);
		ps.executeUpdate();
		ps.close();
	}

}
