import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.*;

import org.eclipse.swt.custom.StyledText;

public class Checker implements Runnable {

	private int checkId;
	private Address host = null;
	private final int timeSleep = 6;
	private Set<Address> badHosts = null;
	private Set<Address> goodHosts = null;
	private boolean itWasOffline = false;
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock writeLock = lock.writeLock();
	private StyledText consolePrint;
	
	public Checker(int checkId, Address host, Set<Address> badHosts, Set<Address> goodHosts, StyledText consolePrint) {
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
		while(true) {
			writeLock.lock();
			Console.checkLog(consolePrint);
			writeLock.unlock();
			try {
				InetAddress Address = InetAddress.getByName(host.getHost());
				check = Address.isReachable(1000);
				// Attenzione questo metodo provera' a stabilire una connesione di tipo TCP
				// Sulla porta 7 (Echo port)
				if(check) { // Prima verifica
					host.setStatus(true);
					host.setTypeErr(3);
					if(itWasOffline) {
						itWasOffline = false;
						writeLock.lock();
						goodHosts.add(host);
						Console.writeConsole(consolePrint, "[" + host.getHost() + "] e' connesso\n");
						Console.writeLog("[" + host.getHost() + "] e' connesso\n");
						if(!Console.generalStatusGood) {
							Console.generalStatusGood = true;
						}
						writeLock.unlock();
					}
				}else { // Se la prima verifica dovesse fallire
					InetAddress a1 = InetAddress.getByName(host.getHost());
					check = a1.isReachable(1000);
					if(check) { // Seconda verifica
						host.setStatus(true);
						host.setTypeErr(3);
						if(itWasOffline) {
							itWasOffline = false;
							writeLock.lock();
							goodHosts.add(host);
							Console.writeConsole(consolePrint, "[" + host.getHost() + "] e' connesso\n");
							Console.writeLog("[" + host.getHost() + "] e' connesso\n");
							if(!Console.generalStatusGood) {
								Console.generalStatusGood = true;
							}
							writeLock.unlock();
						}
					}else { // Se la seconda verifica dovesse fallire
						InetAddress a2 = InetAddress.getByName(host.getHost());
						check = a2.isReachable(1000);
						if(check) { // Terza verifica
							host.setStatus(true);
							host.setTypeErr(3);
							if(itWasOffline) {
								itWasOffline = false;
								writeLock.lock();
								goodHosts.add(host);
								Console.writeConsole(consolePrint, "[" + host.getHost() + "] e' connesso\n");
								Console.writeLog("[" + host.getHost() + "] e' connesso\n");
								if(!Console.generalStatusGood) {
									Console.generalStatusGood = true;
								}
								writeLock.unlock();
							}
						}else { // Se la terza verifica dovesse fallire
							InetAddress a3 = InetAddress.getByName(host.getHost());
							check = a3.isReachable(1000);
							if(check) { // Quarta verifica
								host.setStatus(true);
								host.setTypeErr(3);
								if(itWasOffline) {
									itWasOffline = false;
									writeLock.lock();
									goodHosts.add(host);
									Console.writeConsole(consolePrint, "[" + host.getHost() + "] e' connesso\n");
									Console.writeLog("[" + host.getHost() + "] e' connesso\n");
									if(!Console.generalStatusGood) {
										Console.generalStatusGood = true;
									}
									writeLock.unlock();
								}
							} else {
								InetAddress a4 = InetAddress.getByName(host.getHost());
								check = a4.isReachable(1000);
								if(check) { // Quinta verifica
									host.setStatus(true);
									host.setTypeErr(3);
									if(itWasOffline) {
										itWasOffline = false;
										writeLock.lock();
										goodHosts.add(host);
										Console.writeConsole(consolePrint, "[" + host.getHost() + "] e' connesso\n");
										Console.writeLog("[" + host.getHost() + "] e' connesso\n");
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
										badHosts.add(host);
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
							}
						}
					}
				}
			} catch(IOException e) {
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
				}else {
					//System.out.println("Errore di connessione (Time out!) (" + host.getHost() + "), ho gi� notificato!");
				}
			}
			//sleep(timeSleep);
			//Console.writeLog("Mi sto addormentando " + Thread.currentThread().getName() + "\n");
			try {
				synchronized (Console.mutex) {
					while(!Mail.finished) {
						Console.writeLog("Mi sto addormentando " + Thread.currentThread().getName() + "\n");
						Console.mutex.wait();
						//this.wait();
					}
				}
				//this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Console.writeLog("Mi sto svegliando " + Thread.currentThread().getName() + "\n");
			/*
			if(Console.generalStatusBad || Console.generalStatusGood) {
				
			}
			try {
				threadMail.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	
	public int getCheckId() {
		return checkId;
	}
	
	public static void sleep(int timeLongMillis) {
		for(int i = 0; i < timeLongMillis; i++) {
			try {
				Thread.sleep(1000);
			}catch(InterruptedException e) {
				//
			}
		}
	}
}
