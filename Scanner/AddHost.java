import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.dialogs.MessageDialog;
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
	private ReentrantLock lock = new ReentrantLock();
	
	public AddHost() {
		// Vuoto
	}
	
	public AddHost(ArrayList<Address> hosts, StyledText consolePrint, ObjectMapper objectMapper) {
		this.hosts = hosts;
		this.consolePrint = consolePrint;
		this.objectMapper = objectMapper;
		hostsJson = new File("Backup/hostsJson.json");
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
					Address host = new Address(textAddress.getMessage(), textHost.getMessage(), hosts.size() + 1, false);
					hosts.add(host);
					lock.lock();
					try {
						objectMapper.writeValue(hostsJson, hosts);
					} catch(IOException e1) {
						consolePrint.append(dateReturn() + "Errore aggiornamento backup\n");
					}
					consolePrint.append(dateReturn() + "Impianto aggiunto con successo {" + textHost.getMessage() + "} {" + textAddress.getMessage() + "}");
					lock.unlock();
					MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Successo", "Impianto aggiunto");
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
	
	public static String dateReturn() {
		String dateStr = "";
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		dateStr += "[" + date + "--" + time + "]: ";
		return dateStr;
	}
}
