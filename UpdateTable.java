import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class UpdateTable implements Runnable {
	
	private TableViewer tableViewer;	// Tabella da modificare
	private TableColumn[] columns;		// Colonne da modificare
	private ArrayList<Address> hosts;	// Lista degli host
	private Display display;
	private ReentrantLock lock = new ReentrantLock();
	
	public UpdateTable(TableViewer tableViewer, TableColumn[] columns, ArrayList<Address> hosts, Display display) {
		this.tableViewer = tableViewer;
		this.columns = columns;
		this.hosts = hosts;
		this.display = display;
	}
	
	@SuppressWarnings("static-access")
	public void run() {
		Table table = tableViewer.getTable();
		sleep(5);
		while(true) {
			display.getDefault().asyncExec(new Runnable() {
				public void run() {
					lock.lock();
					TableItem[] items = table.getItems();
					for(int i = 0; i < hosts.size(); i++) {
						Address address = hosts.get(i);
						items[i].setText(3, address.statusString());
					}
					for(int i = 0; i < columns.length; i++) {
						columns[i].pack();
					}
					lock.unlock();
				}
			});
			sleep(15);
		}
	}
	
	public static void sleep(int timeLongMillis) {
		for(int i = 0; i < timeLongMillis; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
