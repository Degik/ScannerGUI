import java.util.*;
import java.util.concurrent.locks.*;

public class Mail implements Runnable {
	private Set<Address> goodHosts = null;
	private Set<Address> badHosts = null;
	private String destinatario;
	private String username;
	private String password;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock writeLock = lock.writeLock();
	
	public Mail(Set<Address> goodHosts, Set<Address> badHosts, String destinatario, String username, String password) {
		this.goodHosts = goodHosts;
		this.badHosts = badHosts;
		this.destinatario = destinatario;
		this.username = username;
		this.password = password;
	}
	
	@Override
	public synchronized void run() {
		while(true) {
			String subjectGood = "";
			String messaggioGood = "";
			if(Main.generalStatusGood) {
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
					subjectGood = "Uno dei tuoi impianti è tornato online [";
					
					for(Address h : goodHosts) {
						messaggioGood = "Questo tuo impianto è di nuovo operativo: \n";
						subjectGood = subjectGood + h.getName();
						messaggioGood = messaggioGood + h + "\n";
					}
					subjectGood = subjectGood + "]";
				}
				goodHosts.removeAll(goodHosts); // rimuvo gli elementi
				SendMail Mail = new SendMail(destinatario, username, subjectGood, messaggioGood, password);
				Mail.sendMail();
				writeLock.lock();
				Main.generalStatusGood = false;
				writeLock.unlock();
			}
			
			
			String subjectBad = "";
			String messaggioBad = "";
			if(Main.generalStatusBad) {
				System.out.println("");
				System.out.println("");
				System.out.println("Ecco la lista degli impianti non funzionanti:");
				System.out.println("");
				if(badHosts == null) {
					throw new NullPointerException();
				}
				for(Address h : badHosts) {
					System.out.println(h);
				}
				System.out.println("");
				System.out.println("");
				System.out.print("Scegli: ");
				if(badHosts.size() > 1) {
					subjectBad = "Alcuni dei tuoi impianti sono offline ";
					messaggioBad = "Attenzione questi impianti risultano offline : \n";
					for(Address h : badHosts) {
						subjectBad = subjectBad + "[" + h.getName() + "] ";
						messaggioBad = messaggioBad + h + "\n";
					}
				}else {
					subjectBad = "Uno dei tuoi impianti è offline ";
					
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
				Main.generalStatusBad = false;
				writeLock.unlock();
			}
			sleep(3);
		}
	}
	
	public static void sleep(int TimeLongMillis) {
		for(int i = 0; i < TimeLongMillis; i++) {
			try {
				Thread.sleep(1000);
			}catch(InterruptedException e) {
				System.out.println("Il thread Mail è stato interrotto");
			}
		}
	}
	
}
