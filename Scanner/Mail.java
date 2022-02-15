import java.util.*;
import java.util.concurrent.locks.*;

import org.eclipse.swt.custom.StyledText;

public class Mail implements Runnable {
	private Set<Address> goodHosts = null;
	private Set<Address> badHosts = null;
	private String destinatario;
	private String username;
	private String password;
	private StyledText consolePrint;
	public static boolean finished = false;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock writeLock = lock.writeLock();
	private ReentrantLock generalLock = new ReentrantLock();
	private ArrayList<Thread> threadList;
	
	public Mail(Set<Address> goodHosts, Set<Address> badHosts, String destinatario, String username, String password, StyledText consolePrint, ArrayList<Thread> threadList) {
		this.goodHosts = goodHosts;
		this.badHosts = badHosts;
		this.destinatario = destinatario;
		this.username = username;
		this.password = password;
		this.consolePrint = consolePrint;
		this.threadList = threadList;
	}
	
	@Override
	public synchronized void run() {
		sleep(1);
		while(true) {
			Console.writeLog("Sto impostando false\n");
			finished = false;
			String subjectGood = "";
			String messaggioGood = "";
			if(Console.generalStatusGood) {
				if(goodHosts == null) {
					throw new NullPointerException();
				}
				if(goodHosts.size() > 1) {
					subjectGood = "Alcuni dei tuoi impianti sono tornati online ";
					
					messaggioGood = "Questi impianti sono tornati online:\n";
					for(Address h : goodHosts) {
						subjectGood = subjectGood + "[" + h.getName() + "] ";
						messaggioGood = messaggioGood + h + "\n";
					}
				}else {
					subjectGood = "Uno dei tuoi impianti e' tornato online [";
					
					for(Address h : goodHosts) {
						messaggioGood = "Questo tuo impianto e' di nuovo operativo: \n";
						subjectGood = subjectGood + h.getName();
						messaggioGood = messaggioGood + h + "\n";
					}
					subjectGood = subjectGood + "]";
				}
				goodHosts.removeAll(goodHosts); // rimuvo gli elementi
				SendMail Mail = new SendMail(destinatario, username, subjectGood, messaggioGood, password);
				Mail.sendMail();
				writeLock.lock();
				Console.generalStatusGood = false;
				writeLock.unlock();
			}
			
			
			String subjectBad = "";
			String messaggioBad = "";
			if(Console.generalStatusBad) {
				String format = "Ecco la lista degli impianti non funzionanti:\n";
				if(badHosts == null) {
					throw new NullPointerException();
				}
				format = format + "/////////////////////////////////////////////////////////////////////////////////////////////\n";
				
				for(Address h : badHosts) {
					format = format + "/// " + h + "\n"; 
				}
				format = format + "////////////////////////////////////////////////////////////////////////////////////////////\n";
				Console.writeConsole(consolePrint, format);
				Console.writeLog(format);
				if(badHosts.size() > 1) {
					subjectBad = "Alcuni dei tuoi impianti sono offline ";
					messaggioBad = "Attenzione questi impianti risultano offline : \n";
					for(Address h : badHosts) {
						subjectBad = subjectBad + "[" + h.getName() + "] ";
						messaggioBad = messaggioBad + h + "\n";
					}
				}else {
					subjectBad = "Uno dei tuoi impianti e' offline ";
					
					for(Address h : badHosts) {
						subjectBad = subjectBad + "[" + h.getName() + "] ";
						messaggioBad = "Attenzione questo impianto risulta offline : \n";
						messaggioBad = messaggioBad + h + "\n";
					}
				}
				badHosts.removeAll(badHosts); // rimuvo gli elementi
				SendMail Mail = new SendMail(destinatario, username, subjectBad, messaggioBad, password);
				Mail.sendMail();
				writeLock.lock();
				Console.generalStatusBad = false;
				writeLock.unlock();
			}
			Console.writeLog("Sto svegliando i thread\n");
			//sleep(5);
			synchronized (Console.mutex) {
				Console.mutex.notifyAll();
			}
			finished = true;
			sleep(1);
		}
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
