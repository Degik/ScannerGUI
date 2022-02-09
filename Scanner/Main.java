package Scanner;

import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {
	public static boolean generalStatusGood = false;
	public static boolean generalStatusBad = false;
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		ArrayList<String> inputSettings = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {
			String line;
			while((line = br.readLine()) != null) {
				//System.out.println(line);
				inputSettings.add(line);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Non ho trovato nessun file config.txt");
		}
		// Da questo punto si prendono i dati
		@SuppressWarnings("resource") // Soppressione del warining sc is not close
		Scanner sc = new Scanner(System.in);
		//System.out.print("Inserisci username: "); // e-mail del mittente per il servizio di messagistica
		//String username = sc.nextLine();
		int count = 0; // Serve per mandare avanti gli elementi della lista
		String username = inputSettings.get(count);
		username = username + "@gmail.com";
		//System.out.print("Inserisci password: "); // password dell'e-mail
		//String password = sc.nextLine();
		count++;
		String password = inputSettings.get(count);
		//System.out.print("Inserisci il destinatario: "); // e-meil destinataria dei messaggi
		//String destinatario = sc.nextLine();
		count++;
		String destinatario = inputSettings.get(count);
		//System.out.println("Inserire l'intervallo di tempo (secondi) per ogni check");
		//System.out.println("Considerare che ogni tot secondi invia un email nel caso si presentasse un problema!");
		//System.out.println("Tempo check (secondi): ");		// intervallo di tempo tra ogni check
		//int timeCheck = sc.nextInt();
		//System.out.println("Tempo e-mail (secondi): ");
		//int timeEmail = sc.nextInt();
		
		System.out.println("");
		System.out.println("");
		
		//System.out.println("Inserisci gli host di cui tenere traccia:"); // Qui dobbiamo inserire la lista degli host
		//System.out.println("Scrivere 0 per proseguire");
		
		Set<Address> hosts = new HashSet<>();
		String host = "";
		String name = "";
		int numAddress = 1; // Serve per mandare avanti gli Id
		while(true) {
			//System.out.print("Inserisci l'host: ");
			if(inputSettings.size() <= (count + 1)) {
				break;
			} else {
				count++; // Mi sposto nella lista
				name = inputSettings.get(count);
				count++;
				host = inputSettings.get(count);
				Address a = new Address(name, host, numAddress, false);
				hosts.add(a);
				numAddress++;
			}
			//System.out.println("L'host inserito � " + "(" + host + ")");
			//System.out.println("� corretto? Y/N");	// Se non lo � richiede nuovamente l'host
			//String answear = sc.nextLine();
			/*if(answear.toLowerCase().equals("y")) {
				Address a = new Address(host, numAddress, false);
				hosts.add(a);
				numAddress++;
				System.out.println("Scrivere 0 per proseguire");
				continue;
			}else {
				System.out.println("Reinserisci host");
			}*/
		}
		// Fine inserimento dei dati
		
		ExecutorService poolThread = Executors.newCachedThreadPool(); // Questa � la mia threadPool
		// Ogni thread avr� il compito di verificare lo status 
		// di ciascun address in maniera concorrente
		
		Set<Address> badHosts = new HashSet<>();
		Set<Address> goodHosts = new HashSet<>();
		
		System.out.println("");
		System.out.println("Ecco la tua lista:");
		System.out.println("");
		
		for(Address h : hosts) {
			System.out.println(h.getName() + " " + h.getHost());	
		}
		System.out.println("");
		
		int i = 1;
		
		// Ogni singolo checker controlla ogni singolo indirizzo
		for(Address h : hosts) {
			poolThread.execute(new Checker(i, h, badHosts, goodHosts));
			i++;
		}
		
		// Il thread Mail si occupa di tutte le richiese di notifica simultaneamente
		poolThread.execute(new Mail(goodHosts, badHosts, destinatario, username, password));
		
		System.out.println("");
		safePrintln("Sto avviando il servizio...");
		sleep(6);
		System.out.println("");
		boolean running = true; // check while (true)
		while(running) {
			System.out.println("");
			System.out.println("");
			safePrintln("1. Lista dei server");
			safePrintln("2. Aggiungi un server");
			safePrintln("3. Termina");
			System.out.println("");
			System.out.println("");
			safePrint("Scegli: ");
			String choose = sc.nextLine();
			switch(choose) {
			case "1":
				//Lista dei servizi
				safePrintln("      Lista dei server");
				System.out.println("");
				for(Address h : hosts) {
					System.out.println(h);
				}
				break;
			case "2":
				// Aggiungi un servizio
				String address = "";
				while(!address.equals("0")) {
					System.out.println("");
					safePrintln("Scrivi 0 per annullare");
					safePrint("Inserisci l'indirizzo: ");
					address = sc.nextLine();
					if(address.equals("0")) {
						break;
					}
					System.out.println("");
					safePrintln("Host = " + "[" + address + "]");
					safePrintln("È corretto? Y/N");	// Se non lo � richiede nuovamente l'host
					String answear = sc.nextLine();
					if(answear.toLowerCase().equals("y")) {
						String name1 = "";
						while(!name1.equals("0")) {
							System.out.println("");
							System.out.print("Inserisci località: ");
							name1 = sc.nextLine();
							if(name1.equals("0")) {
								break;
							}
							System.out.println("");
							safePrintln("Località = " + "[" + name1 + "]");
							safePrintln("È corretto? Y/N");
							String answear1 = sc.nextLine();
							if(answear1.toLowerCase().equals("y")) {
								Address a = new Address(name, address, numAddress, false);
								hosts.add(a);
								numAddress++;
								poolThread.execute(new Checker(i, a, badHosts, goodHosts));
								i++;
								System.out.println("");
								safePrintln("Servizio aggiunto con successo!");
								safePrintln("Sto aggiornando il sistema...");
								sleep(12);
								break;
							}
							System.out.println("");
						}
						break;
					}else {
						System.out.println("");
						safePrintln("Reinserisci host");
					}
				}
				break;
			case "3":
				System.out.println("");
				System.out.println("Sto terminando i thread...");
				poolThread.shutdown();
				try {
					if(!poolThread.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
						poolThread.shutdownNow();
					}
				} catch(InterruptedException e) {
					poolThread.shutdownNow();
				}
				sleep(3);
				running = false;
				break;
			default:
				// Errore inserimento;
				safePrintln("Operazione non valida!");
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

	public static void safePrintln(String s) {
		synchronized (System.out) {
			System.out.println(s);
		}
	}
	
	public static void safePrint(String s) {
		  synchronized (System.out) {
		    System.out.print(s);
		  }
	}
}
