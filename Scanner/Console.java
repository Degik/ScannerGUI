import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class Console {

	public static boolean generalStatusGood = false;
	public static boolean generalStatusBad = false;
	
	protected Shell shell;
	public static ExecutorService poolThread = null; // Da sostituire con array di Thread
	private static File backupJson = null;
	private static File hostsJson = null; // File hostsJson
	private static File userJson = null;  // File userJson
	private static File logDir = null;
	private static File dayLog = null;
	private static ObjectMapper objectMapper = null;  // objectMapper
	private static ArrayList<Address> hosts = null;   // hosts
	private static User user = null; 				  // Utente dove vengono salvate le informazioni mittente, password, destinatario
	private static Set<Address> badHosts = new HashSet<>();
	private static Set<Address> goodHosts = new HashSet<>();
	private ReentrantLock lock = new ReentrantLock();
	private Table table;
	private static String logPath = "Logs/" + getDay() + ".txt";
	private static ArrayList<Thread> threadList;
	
	//private Table table;
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
		dayLog = new File(logPath);
		// objectMapper
		objectMapper = new ObjectMapper();
		// Setting objectMapper
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.enable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
		
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
	 * @throws IOException 
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(974, 482);
		shell.setText("Scanner (Menu)");
		
		Button rmvHost = new Button(shell, SWT.NONE);
		rmvHost.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		rmvHost.setText("Rimuovi");
		rmvHost.setBounds(194, 408, 100, 33);
		
		TextViewer textViewer = new TextViewer(shell, SWT.BORDER);
		StyledText consolePrint = textViewer.getTextWidget();
		consolePrint.setEditable(false);
		consolePrint.setTouchEnabled(true);
		consolePrint.setBounds(10, 30, 938, 127);
		
		// Verifico il backup
		if(!manageBackup(consolePrint)) {
			// Prendo i dati
			Properties prop = new Properties();
			try {
				FileInputStream input = new FileInputStream("C:\\Users\\Davide Bulotta\\eclipse-workspace\\ScannerGUI\\src\\UserSetting.properties");
				prop.load(input);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("IOException");
				e.printStackTrace();
			}
			
			String sender, password, recipient;
			sender = prop.getProperty("MITTENTE");
			password = prop.getProperty("PASSWORD");
			recipient = prop.getProperty("DESTINATARIO");
			
			user = new User(sender,password,recipient);
			
			// Recupero le informazioni host
			ArrayList<String> inputSettings = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Davide Bulotta\\eclipse-workspace\\ScannerGUI\\src\\config.txt"))) {
				String line;
				while((line = br.readLine()) != null) {
					inputSettings.add(line);
				}
			} catch (FileNotFoundException e) {
				consolePrint.append(dateReturn() + "Non ho trovato nessun file config.txt\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int id = 1;
			int count = -1;
			if(!inputSettings.isEmpty()) {
				String name, host;
				while(true) {
					if((count + 1) >= inputSettings.size()) {
						break;
					} else {
						count++; // Mi sposto nella lista
						name = inputSettings.get(count);
						count++;
						host = inputSettings.get(count);
						Address a = new Address(name, host, id, false);
						hosts.add(a);
						id++;
					}
				}
			}
		}
		
		try {
			objectMapper.writeValue(userJson, user);
			objectMapper.writeValue(hostsJson, hosts);
		} catch(IOException e) {
			consolePrint.append(dateReturn() + "Errore creazione backup\n");
		}
		
		// Gestisco ed avvio tutti i thread
		threadList = new ArrayList<>();
		
		Mail mail = new Mail(goodHosts, badHosts, user.getSender(), user.getPassword(), user.getRecipient(), consolePrint);
		Thread threadMail = new Thread(mail);
		threadMail.start();
		
		for(Address h : hosts) {
			Checker check = new Checker(h.getAddressId(), h, badHosts, goodHosts, consolePrint, threadMail);
			Thread th = new Thread(check);
			threadList.add(th);
			th.start();
		}
		
		Label lblListaImpianti = new Label(shell, SWT.NONE);
		lblListaImpianti.setBounds(10, 191, 100, 15);
		lblListaImpianti.setText("LISTA IMPIANTI");
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(10, 10, 55, 15);
		lblNewLabel.setText("CONSOLE");
		
		TableViewer tableViewer = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setBounds(10, 221, 938, 181);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tcIdV = new TableColumn(table, SWT.NONE);
		tcIdV.setText("         Id");
		
		TableColumn tcAddressV = new TableColumn(table, SWT.NONE);
		tcAddressV.setText("         Impianto");
		
		TableColumn tcHostV = new TableColumn(table, SWT.NONE);
		tcHostV.setText("         Host");
		
		TableColumn tcStatusV = new TableColumn(table, SWT.NONE);
		tcStatusV.setText("         Status");
		
		TableColumn[] columnsV = table.getColumns();
		
		for(int i = 0; i < hosts.size(); i++) {
			Address address = hosts.get(i);
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, Integer.toString(address.getAddressId()));
			item.setText(1, address.getName());
			item.setText(2, address.getHost());
			item.setText(3, address.statusString());
		}
		for(int i = 0; i < columnsV.length; i++) {
			columnsV[i].pack();
		}
		
		UpdateTable updateTable = new UpdateTable(tableViewer, columnsV, hosts, Display.getDefault()); // Creo l'oggetto per gestire i dati
		Thread tableThread = new Thread(updateTable); // Creo il thread
		tableThread.start();
		
		Button addHost = new Button(shell, SWT.NONE);
		addHost.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddHost addHost = new AddHost(hosts, consolePrint, objectMapper, tableViewer, threadList, goodHosts, badHosts, threadMail);
				addHost.open();
			}
		});
		addHost.setBounds(10, 408, 100, 33);
		addHost.setText("Aggiungi");
		
		Button exit = new Button(shell, SWT.NONE);
		exit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				consolePrint.append(dateReturn() + "Sto terminando i thread");
				for(Thread th : threadList) {
					th.interrupt();
				}
				threadMail.interrupt();
				sleep(3);
				shell.close();
			}
		});
		exit.setText("Esci");
		exit.setBounds(848, 408, 100, 33);
		
		writeLog("Programma avviato con successo\n");
		writeLog("Impianti caricati con successo\n");
		writeLog("Tabelle caricate con successo\n");
		writeLog("Utente caricato con successo\n");
	}
	
	private boolean manageBackup(StyledText consolePrint) {
		boolean checkDir = false;
		boolean res = true; // Se vero prelevo i dati backup
		if(!backupJson.exists()) {
			checkDir = true;
			res = false;
			lock.lock();
			consolePrint.append(dateReturn() + "Cartella backup non trovata!\n");
			consolePrint.append(dateReturn() + "File userJson non trovato!\n");
			consolePrint.append(dateReturn() + "File hostsJson non trovato!\n");
			try {
				backupJson.mkdir();
				consolePrint.append(dateReturn() + "Cartella backup creata\n");
				userJson.createNewFile();
				consolePrint.append(dateReturn() + "File userJson creato\n");
				hostsJson.createNewFile();
				consolePrint.append(dateReturn() + "File hostsJson creato\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			lock.unlock();
		} else {
			consolePrint.append(dateReturn() + "Cartella backup trovata\n");
			if(!userJson.exists()) {
				res = false;
				lock.lock();
				consolePrint.append(dateReturn() + "File userJson non trovato!\n");
				try {
					userJson.createNewFile();
					consolePrint.append(dateReturn() + "File userJson creato\n");
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
					// Altrimenti li prendo da UserSetting.properties
					res = false;
				}
			}
			if(!hostsJson.exists()) {
				res = false;
				lock.lock();
				consolePrint.append(dateReturn() + "File hostsJson non trovato!\n");
				try {
					userJson.createNewFile();
					consolePrint.append(dateReturn() + "File hostsJson creato\n");
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
					res = false;
				}
			}
		}
		// Controllo cartella log
		if(!logDir.exists()) {
			lock.lock();
			consolePrint.append(dateReturn() + "Cartella logs non trovata!\n");
			try {
				logDir.mkdir();
				consolePrint.append(dateReturn() + "Cartella logs creata\n");
			} catch(Exception e) {
				e.printStackTrace();
			}
			lock.unlock();
			try {
				dayLog.createNewFile();
				consolePrint.append(dateReturn() + "File log\n");
				writeLog("Log avviato (" + logPath + ")\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;
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
	
	// Questo metodo controlla periodicamente se viene aggiornata la data dei log creando un nuovo file per ogni nuovo giorno
	public synchronized static void checkLog(StyledText consolePrint) {
		String newLogPath = "Logs/" + getDay() + ".txt";
		logPath = newLogPath;
		dayLog = new File(logPath);
		if(!dayLog.exists()) {
			try {
				dayLog.createNewFile();
				consolePrint.append(dateReturn() + "Nuovo file log creato");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized static String dateReturn() {
		String dateStr = "";
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter myFormatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		dateStr = date.format(myFormatDate);
		return "[" + dateStr + "] ";
	}
	
	public synchronized static String getDay() {
		String dateStr = "";
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter myFormatDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		dateStr = date.format(myFormatDate);
		return dateStr;
	}
	
	public synchronized static void writeLog(String text) {
		try {
			FileWriter writeLog = new FileWriter(logPath, true);
			writeLog.write(dateReturn() + text);
			writeLog.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void writeConsole(StyledText consolePrint, String text) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				consolePrint.append(dateReturn() + text);
			}
		});
	}
}
