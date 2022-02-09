import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.StyledText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.text.TextViewer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class Console {

	protected Shell shell;
	public static ExecutorService poolThread = null; // Da sostituire con array di Thread
	private static File backupJson = null;
	private static File hostsJson = null; // File hostsJson
	private static File userJson = null;  // File userJson
	private static File logDir = null;
	private static File logFile = null;
	private static ObjectMapper objectMapper = null;  // objectMapper
	private static ArrayList<Address> hosts = null;   // hosts
	private static User user = null; 				  // Utente dove vengono salvate le informazioni mittente, password, destinatario
	private ReentrantLock lock = new ReentrantLock();
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		// Lista degli host gia' presenti
		hosts = new ArrayList<>();
		// backupJson
		backupJson = new File("Backup");
		// hostsJson
		hostsJson = new File("Backup/hostsJson.json");
		// userJson
		userJson = new File("Backup/userJson.json");
		// logDir
		logDir = new File("Logs");
		// logFile
		logFile = new File("Logs/log.log");
		// objectMapper
		objectMapper = new ObjectMapper();
		// PoolThread
		poolThread = Executors.newCachedThreadPool();
		try {
			Console window = new Console();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(974, 482);
		shell.setText("SWT Application");
		
		Button addHost = new Button(shell, SWT.NONE);
		addHost.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		addHost.setBounds(10, 408, 100, 33);
		addHost.setText("Aggiungi");
		
		Button rmvHost = new Button(shell, SWT.NONE);
		rmvHost.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		rmvHost.setText("Rimuovi");
		rmvHost.setBounds(194, 408, 100, 33);
		
		Button exit = new Button(shell, SWT.NONE);
		exit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("");
				System.out.println("Sto terminando i thread...");
				poolThread.shutdown();
				try {
					if(!poolThread.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
						poolThread.shutdownNow();
					}
				} catch(InterruptedException e1) {
					poolThread.shutdownNow();
				}
				sleep(3);
			}
		});
		exit.setText("Esci");
		exit.setBounds(848, 408, 100, 33);
		
		List list = new List(shell, SWT.BORDER);
		list.setBounds(10, 212, 938, 181);
		
		TextViewer textViewer = new TextViewer(shell, SWT.BORDER);
		StyledText consolePrint = textViewer.getTextWidget();
		consolePrint.setEditable(false);
		consolePrint.setTouchEnabled(true);
		consolePrint.setBounds(10, 30, 938, 127);
		// Verifico il backup
		manageBackup(consolePrint);
		
		Label lblListaImpianti = new Label(shell, SWT.NONE);
		lblListaImpianti.setBounds(10, 191, 100, 15);
		lblListaImpianti.setText("LISTA IMPIANTI");
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(10, 10, 55, 15);
		lblNewLabel.setText("CONSOLE");
	}
	
	private void manageBackup(StyledText consolePrint) {
		boolean checkDir = false;
		if(!backupJson.exists()) {
			checkDir = true;
			lock.lock();
			consolePrint.append("Cartella backup non trovata!\n");
			consolePrint.append("File userJson non trovato!\n");
			consolePrint.append("File hostsJson non trovato!\n");
			try {
				backupJson.mkdir();
				consolePrint.append("Cartella backup creata\n");
				userJson.createNewFile();
				consolePrint.append("File userJson creato\n");
				hostsJson.createNewFile();
				consolePrint.append("File hostsJson creato\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			lock.unlock();
		} else {
			consolePrint.append("Cartella backup trovata\n");
			if(!userJson.exists()) {
				lock.lock();
				consolePrint.append("File userJson non trovato!\n");
				try {
					userJson.createNewFile();
					consolePrint.append("File userJson creato\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				lock.unlock();
			} else {
				if(userJson.length() != 0) {
					// Prendo i dati salvati
					try {
						user = objectMapper.readValue(userJson, User.class);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					// Altrimenti li prendo dal config.txt
					
				}
			}
			if(!hostsJson.exists()) {
				lock.lock();
				consolePrint.append("File hostsJson non trovato!\n");
				try {
					userJson.createNewFile();
					consolePrint.append("File hostsJson creato\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				lock.unlock();
			} else {
				if(hostsJson.length() != 0) {
					try {
						hosts = new ArrayList<>(Arrays.asList(objectMapper.readValue(hostsJson, Address[].class)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					//
				}
			}
		}
		// Controllo cartella log
		if(!logDir.exists()) {
			lock.unlock();
			consolePrint.append("Cartella logs non trovata!\n");
			try {
				logDir.mkdir();
				consolePrint.append("Cartella logs creata\n");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void sleep(int TimeLongMillis) {
		for(int i = 0; i < TimeLongMillis; i++) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
				System.out.println("Errore di sleep (Main)");
			}
		}
		
	}
}
