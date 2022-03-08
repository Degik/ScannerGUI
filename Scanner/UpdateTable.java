import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
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
	private StyledText consolePrint;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	public UpdateTable(TableViewer tableViewer, TableColumn[] columns, ArrayList<Address> hosts, Display display, StyledText consolePrint) {
		this.tableViewer = tableViewer;
		this.columns = columns;
		this.hosts = hosts;
		this.display = display;
		this.consolePrint = consolePrint;
	}
	
	@SuppressWarnings("static-access")
	public void run() {
		sleep(7);
		running.set(true);
		while(running.get()) {
			Table table = tableViewer.getTable();
			display.getDefault().asyncExec(new Runnable() {
				public void run() {
					lock.lock();
					TableItem[] items = table.getItems();
					for(int i = 0; i < hosts.size(); i++) {
						Address address = hosts.get(i);
						if(address.getStatus()) {
							FileInputStream iconGreen = null;
							try {
								iconGreen = new FileInputStream("icons\\online.png");
							} catch(FileNotFoundException e1) {
								Console.writeConsole(consolePrint, "Icona online.png non trovata\n");
								Console.writeLog("Icona online.png non trovata! cercare nella cartella icons\n", 1);
								e1.printStackTrace();
							}
							Image statusIconOnline = new Image(display.getDefault(), iconGreen);
							items[i].setImage(3, statusIconOnline);
						} else {
							FileInputStream iconRed = null;
							try {
								iconRed = new FileInputStream("icons\\offline.png");
							} catch(FileNotFoundException e1) {
								Console.writeConsole(consolePrint, "Icona offline.png non trovata\n");
								Console.writeLog("Icona onffline.png non trovata! cercare nella cartella icons\n", 1);
								e1.printStackTrace();
							}
							Image statusIconOffline = new Image(display.getDefault(), iconRed);
							items[i].setImage(3, statusIconOffline);
						}
						//items[i].setText(3, address.statusString());
					}
					for(int i = 0; i < columns.length; i++) {
						columns[i].pack();
					}
					lock.unlock();
				}
			});
			sleep(10);
		}
	}
	
	public void setStop() {
		running.set(false);
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
