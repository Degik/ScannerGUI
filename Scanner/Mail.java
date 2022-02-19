import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;
import com.sun.mail.util.MailConnectException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

public class Mail implements Runnable {
	private LinkedList<Address> goodHosts = null;
	private LinkedList<Address> badHosts = null;
	private String destinatario;
	private String username;
	private String password;
	private StyledText consolePrint;
	public static boolean finished = false;
	private ReentrantLock generalLock = new ReentrantLock();
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	public Mail(LinkedList<Address> goodHosts, LinkedList<Address> badHosts, String destinatario, String username, String password, StyledText consolePrint) {
		this.goodHosts = goodHosts;
		this.badHosts = badHosts;
		this.destinatario = destinatario;
		this.username = username;
		this.password = password;
		this.consolePrint = consolePrint;
	}
	
	@Override
	public synchronized void run() {
		sleep(7);
		running.set(true);
		while(running.get()) {
			//Console.writeLog("Sto impostando false\n", 2);
			finished = false;
			String subjectGood = "";
			String messaggioGood = "";
			if(Console.generalStatusGood) {
				generalLock.lock();
				if(goodHosts == null) {
					throw new NullPointerException();
				}
				if(goodHosts.size() > 1) {
					subjectGood = "Alcuni dei tuoi impianti sono tornati online ";
					messaggioGood = "Questi impianti sono tornati online:\n";
					Iterator<Address> it = new LinkedList<Address>(goodHosts).iterator();
					while(it.hasNext()) {
						Address h = it.next();
						subjectGood = subjectGood + "[" + h.getName() + "] ";
						messaggioGood = messaggioGood + h + "\n";
					}
					/*for(Address h : goodHosts) {
						subjectGood = subjectGood + "[" + h.getName() + "] ";
						messaggioGood = messaggioGood + h + "\n";
					}*/
				}else {
					subjectGood = "Uno dei tuoi impianti e' tornato online [";
					Iterator<Address> it = new LinkedList<Address>(goodHosts).iterator();
					while(it.hasNext()) {
						Address h = it.next();
						messaggioGood = "Questo tuo impianto e' di nuovo operativo: \n";
						subjectGood = subjectGood + h.getName();
						messaggioGood = messaggioGood + h + "\n";
					}
					/*for(Address h : goodHosts) {
						messaggioGood = "Questo tuo impianto e' di nuovo operativo: \n";
						subjectGood = subjectGood + h.getName();
						messaggioGood = messaggioGood + h + "\n";
					}*/
					subjectGood = subjectGood + "]";
				}
				goodHosts.removeAll(goodHosts); // rimuvo gli elementi
				generalLock.unlock();
				SendMail Mail = new SendMail(destinatario, username, subjectGood, messaggioGood, password);
				Mail.sendMail();
				generalLock.lock();
				Console.generalStatusGood = false;
				generalLock.unlock();
			}
			
			
			String subjectBad = "";
			String messaggioBad = "";
			if(Console.generalStatusBad) {
				generalLock.lock();
				String format = "Ecco la lista degli impianti non funzionanti:\n";
				if(badHosts == null) {
					throw new NullPointerException();
				}
				format = format + "/////////////////////////////////////////////////////////////////////////////////////////////\n";
				Iterator<Address> it = new LinkedList<Address>(badHosts).iterator();
				while(it.hasNext()) {
					Address address = it.next();
					format = format + "/// " + address + "\n";
				}
				/*for(Address address : badHosts) {
					format = format + "/// " + address + "\n";
				}*/
				format = format + "////////////////////////////////////////////////////////////////////////////////////////////\n";
				Console.writeConsole(consolePrint, format);
				Console.writeLog(format, 3);
				if(badHosts.size() > 1) {
					subjectBad = "Alcuni dei tuoi impianti sono offline ";
					messaggioBad = "Attenzione questi impianti risultano offline : \n";
					Iterator<Address> it1 = new LinkedList<Address>(badHosts).iterator();
					while(it1.hasNext()) {
						Address address = it1.next();
						subjectBad = subjectBad + "[" + address.getName() + "] ";
						messaggioBad = messaggioBad + address + "\n";
					}
					/*for(Address address : badHosts) {
						subjectBad = subjectBad + "[" + address.getName() + "] ";
						messaggioBad = messaggioBad + address + "\n";
					}*/
				}else {
					subjectBad = "Uno dei tuoi impianti e' offline ";
					Iterator<Address> it1 = new LinkedList<Address>(badHosts).iterator();
					while(it1.hasNext()) {
						Address address = it1.next();
						subjectBad = subjectBad + "[" + address.getName() + "] ";
						messaggioBad = "Attenzione questo impianto risulta offline : \n";
						messaggioBad = messaggioBad + address + "\n";
					}
					/*for(Address address : badHosts) {
						subjectBad = subjectBad + "[" + address.getName() + "] ";
						messaggioBad = "Attenzione questo impianto risulta offline : \n";
						messaggioBad = messaggioBad + address + "\n";
					}*/
				}
				badHosts.removeAll(badHosts); // rimuvo gli elementi
				//Iterator<Address> itRemove = badHosts.iterator();
				/*while(itRemove.hasNext()) {
					itRemove.remove();
				}*/
				generalLock.unlock();
				SendMail Mail = new SendMail(destinatario, username, subjectBad, messaggioBad, password);
				Mail.sendMail();
				generalLock.lock();
				Console.generalStatusBad = false;
				generalLock.unlock();
			}
			//Console.writeLog("Sto svegliando i thread\n", 2);
			//sleep(5);
			synchronized (Console.mutex) {
				Console.mutex.notifyAll();
				finished = true;
			}
			//sleep(1);
		}
	}
	
	public void setStop() {
		running.set(false);
	}
	
	public static void sleep(int TimeLongMillis) {
		for(int i = 0; i < TimeLongMillis; i++) {
			try {
				Thread.sleep(1000);
			}catch(InterruptedException e) {
				//
			}
		}
	}
}
