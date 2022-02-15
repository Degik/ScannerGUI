import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Label;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AddHost {

	protected Shell shell;
	private Text textAddress;
	private Text textHost;
	private ArrayList<Address> hosts = null; // Lista degli impianti
	private StyledText consolePrint = null;  // consolePrint
	private ObjectMapper objectMapper = null;// La usiamo per aggiornare i backup
	private File hostsJson;
	private TableViewer tableViewer;
	private ArrayList<Thread> threadList;
	private Set<Address> goodHosts;
	private Set<Address> badHosts;
	private ReentrantLock lock = new ReentrantLock();
	
	public AddHost() {
		// Vuoto
	}
	
	public AddHost(ArrayList<Address> hosts, StyledText consolePrint, ObjectMapper objectMapper, TableViewer tableViewer, ArrayList<Thread> threadList, Set<Address> goodHosts, Set<Address> badHosts) {
		this.hosts = hosts;
		this.consolePrint = consolePrint;
		this.objectMapper = objectMapper;
		hostsJson = new File("Backup/hostsJson.json");
		this.tableViewer = tableViewer;
		this.threadList = threadList;
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AddHost window = new AddHost();
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
		shell.setSize(683, 222);
		shell.setText("Scanner (Aggiungi)");
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(324, 31, 42, 15);
		lblNewLabel.setText("HOST");
		
		Label lblLocalita = new Label(shell, SWT.NONE);
		lblLocalita.setText("LOCALITA");
		lblLocalita.setBounds(311, 75, 75, 15);
		
		Label lblInserisciHost = new Label(shell, SWT.NONE);
		lblInserisciHost.setText("AGGIUNGI NUOVO IMPIANTO");
		lblInserisciHost.setBounds(246, 10, 202, 15);
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(textHost.getText().isEmpty() || textAddress.getText().isEmpty()) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Errore", "Devi inserire degli argomenti!");
				} else {
					//String name, String host, int addressId, boolean status
					Address host = new Address(textAddress.getText(), textHost.getText(), hosts.size() + 1, false);
					hosts.add(host);
					lock.lock();
					try {
						objectMapper.writeValue(hostsJson, hosts);
					} catch(IOException e1) {
						consolePrint.append(Console.dateReturn() + "Errore aggiornamento backup\n");
						Console.writeLog("Errore aggiornamento backup\n");
					}
					Table table = tableViewer.getTable();
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, Integer.toString(host.getAddressId()));
					item.setText(1, host.getName());
					item.setText(2, host.getHost());
					item.setText(3, host.statusString());
					// Creo il checker
					Checker check = new Checker(host.getAddressId(), host, badHosts, goodHosts, consolePrint);
					// Creo il thread
					Thread th = new Thread(check);
					// Aggiungo il thread alla lista
					threadList.add(th);
					// Avvio il thread
					th.start();
					consolePrint.append(Console.dateReturn() + "Impianto aggiunto con successo {" + textHost.getText() + "} {" + textAddress.getText() + "}");
					Console.writeLog("Impianto aggiunto con successo {" + textHost.getText() + "}" + "{" + textAddress.getText() + "}\n");
					lock.unlock();
					MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Successo", "Impianto aggiunto");
					shell.close();
				}
			}
		});
		btnNewButton.setBounds(86, 135, 114, 38);
		btnNewButton.setText("Conferma");
		
		Button btnCancella = new Button(shell, SWT.NONE);
		btnCancella.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		btnCancella.setText("Cancella");
		btnCancella.setBounds(480, 135, 114, 38);
		
		textAddress = new Text(shell, SWT.BORDER | SWT.CENTER);
		textAddress.setBounds(86, 96, 508, 21);
		
		textHost = new Text(shell, SWT.BORDER | SWT.CENTER);
		textHost.setBounds(180, 48, 321, 21);

	}
}
