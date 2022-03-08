import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;

import org.eclipse.swt.custom.StyledText;

public class Checker implements Runnable {

	private int checkId;
	private Address host = null;
	private LinkedList<Address> badHosts = null;
	private LinkedList<Address> goodHosts = null;
	private boolean itWasOffline = false;
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock writeLock = lock.writeLock();
	private Lock readLock = lock.readLock();
	private StyledText consolePrint;
	private final AtomicBoolean running = new AtomicBoolean(false);
	public Checker(int checkId, Address host, LinkedList<Address> badHosts, LinkedList<Address> goodHosts, StyledText consolePrint) {
		this.checkId = checkId;
		if(host == null) {
			throw new NullPointerException();
		}
		this.host = host;
		this.badHosts = badHosts;
		this.goodHosts = goodHosts;
		this.consolePrint = consolePrint;
	}
	
	public synchronized void run() {
		boolean check = false;
		sleep(1,consolePrint);
		running.set(true);
		while(running.get()) {
			writeLock.lock();
			Console.checkLog(consolePrint);
			writeLock.unlock();
			check = checkNetwork(5); // Faccio 5 tenativi
			if(check) {
				host.setStatus(true);
				host.setTypeErr(3);
				if(itWasOffline) {
					itWasOffline = false;
					writeLock.lock();
					readLock.lock();
					if(!goodHosts.contains(host)) {
						goodHosts.add(host);
					}
					readLock.unlock();
					Console.writeConsole(consolePrint, host.getName() + " connesso\n");
					Console.writeLog(host.getName() + " connesso\n", 3);
					if(!Console.generalStatusGood) {
						Console.generalStatusGood = true;
					}
					writeLock.unlock();
				}
			} else {
				// Non riesce a connettersi
				host.setStatus(false);
				host.setTypeErr(1); // case 1
				if(!itWasOffline) {
					writeLock.lock();
					readLock.lock();
					if(!badHosts.contains(host)) {
						badHosts.add(host);
					}
					readLock.unlock();
					//System.out.println("Connessione non riuscita (" + host.getHost() + "), sto notificando...");
					itWasOffline = true;
					if(!Console.generalStatusBad) {
						Console.generalStatusBad = true;
					}
					writeLock.unlock();
				}else {
					//System.out.println("Connessione non riuscita (" + host.getHost() + "), ho gia' notificato!");
				}
			}
			//sleep(timeSleep);
			//Console.writeLog("Mi sto addormentando " + Thread.currentThread().getName() + "\n");
			//boolean sleep = false;
			try {
				synchronized (Console.mutex) {
					while(!Mail.finished) {
						//Console.writeLog("Mi sto addormentando " + Thread.currentThread().getName() + "\n", 2);
						Console.mutex.wait();
						//sleep = true;
					}
				}
				//this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Console.writeConsole(consolePrint, "Sono stato eliminato " + Thread.currentThread().getName() + "\n");
				Console.writeLog("Sono stato eliminato " + Thread.currentThread().getName() + "\n", 2);
			}
			/*if(sleep) {
				Console.writeLog("Mi sto svegliando " + Thread.currentThread().getName() + "\n", 2);
				Console.writeConsole(consolePrint, "Mi sto svegliando " + Thread.currentThread().getName() + "\n");
			}*/
		}
		Console.writeLog("Sono stato terminato (quindi sono uscito dal while) " + Thread.currentThread().getName() + "\n", 3);
	}
	
	public int getCheckId() {
		return checkId;
	}
	
	public boolean checkNetwork(int count) {
		boolean check = false;
		try {
			for(int i = 0; i < count && !check; i++) {
				InetAddress Address = InetAddress.getByName(host.getHost());
				check = Address.isReachable(1000);
				// Attenzione questo metodo provera' a stabilire una connesione di tipo TCP
				// Sulla porta 7 (Echo port)
			}
		} catch (IOException e) {
			host.setStatus(false);
			host.setTypeErr(2); // case 2
			if(!itWasOffline) {
				writeLock.lock();
				badHosts.add(host);
				//System.out.println("Errore di connessione (Time out!) (" + host.getHost() + "), sto notificando...");
				itWasOffline = true;
				if(!Console.generalStatusBad) {
					Console.generalStatusBad = true;
				}
				writeLock.unlock();
			}
		}
		return check;
	}
	
	public void setStop() {
		running.set(false);
	}
	
	public static void sleep(int timeLongMillis, StyledText consolePrint) {
		for(int i = 0; i < timeLongMillis; i++) {
			try {
				Thread.sleep(1000);
			}catch(InterruptedException e) {
				//
				Console.writeConsole(consolePrint, "Sono stato eliminato " + Thread.currentThread().getName() + "\n");
				Console.writeLog("Sono stato eliminato " + Thread.currentThread().getName() + "\n", 2);
			}
		}
	}
}
