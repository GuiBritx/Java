import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
//import java.util.TimeZone;
//import java.util.Calendar;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.xml.bind.*;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceNAttributes;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializer;

public class DevisaMetadados {
	/****************************************************************************************/
	static int count = 0;
	/* Configs */ // Diretorio tem que existir
	static String DIRETORIO = "C:\\\\temp\\\\metadadosLog1";
	static final String ARQUIVO_LOG = "logMetadados.txt";	          // cada execução produz um novo
	static final String ARQUIVO_LOG_TotalFiles = "logTotalFiles.txt"; // arquivo de Append
	public static String DATA_LOG=""; // usado para data arquivo de log
	//
	static String DIRETORIO_LOTE_1 = "E:\\Temp\\ArquivosGed\\SMS-DEVISA\\Lançamentos\\Lotes 1_12";
	static String DIRETORIO_LOTE_2 = "E:\\Temp\\ArquivosGed\\SMS-DEVISA\\Lançamentos\\Lotes 8_25_38";
	static String DIRETORIO_LOTE_3 = "E:\\Temp\\ArquivosGed\\SMS-DEVISA\\Lançamentos\\Lotes_2a3_11a24_26a37_39";
	//
	static String DIRETORIO_DESTINO = "E:\\Projetos2024\\teste-metadados";
	//
	static String DIRETORIO_DESTINO_LOTE1 = "E:\\Projetos2024\\teste-metadados\\SMS-DEVISA\\Lançamentos\\Lotes 1_12";	
	static String DIRETORIO_DESTINO_LOTE2 = "E:\\Projetos2024\\teste-metadados\\SMS-DEVISA\\Lançamentos\\Lotes 8_25_38";
	static String DIRETORIO_DESTINO_LOTE3 = "E:\\Projetos2024\\teste-metadados\\SMS-DEVISA\\Lançamentos\\Lotes_2a3_11a24_26a37_39";
	//
	public static String caminho=""; 
	public static String palavrasChave=""; 
	public static String diretorioDestino="";
	//	1 =Local, 2 = Console
	public static int MODO = 2;
	public static boolean SavePdfCaminhoConfig = true;
	//	
	public static boolean LINUX = false; // windows valor contrario	
	
	/* Armazena nome arquivo / numeroProtocolado */
	public static List<List<String>> dataDevisa = new ArrayList<List<String>>();
	/* Armazena numeroProtocolado / dataProtocolado */
	public static List<List<String>> dataDevisa1 = new ArrayList<List<String>>();
	
	/****************************************************************************************/
	
	public static String getSeparator() {
		String separator = File.separator;
		if(separator.equals("\\")) {
			separator += "\\";
		}
		return separator;
		//return "/";
	}
	
	private static void EscreveArquivoLogInfo(String texto) {
		if(LINUX) {
			DIRETORIO = System.getProperty("java.io.tmpdir");
		}
		
		File dir = new File(DIRETORIO);
        File arq = new File(dir, DATA_LOG+ARQUIVO_LOG_TotalFiles);

        try {
            FileWriter fileWriter = new FileWriter(arq, true); // Append
            PrintWriter printWriter = new PrintWriter(fileWriter);
            
            printWriter.println(texto);

            printWriter.flush();
            printWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	private static void EscreveArquivo(String texto) {
		if(LINUX) {
			DIRETORIO = System.getProperty("java.io.tmpdir");
		}
		
		File dir = new File(DIRETORIO);
        File arq = new File(dir, DATA_LOG+ARQUIVO_LOG);

        try {
            FileWriter fileWriter = new FileWriter(arq, true); // Append
            PrintWriter printWriter = new PrintWriter(fileWriter);
            
            printWriter.print(texto);

            printWriter.flush();
            printWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/* Ler arquivo dadosDevisa */
	private static void LerCsv() {
		try {			

			//			ClassLoader classLoader = DevisaMetadados.class.getClassLoader();
			//	        URL resource = classLoader.getResource("dadosDevisa.csv");
			//
			//	        if (resource != null) {
			//	            String caminho = resource.getPath();
			//	            System.out.println("Caminho do arquivo: " + caminho);
			//	            // ... ler o arquivo ...
			//	        } else {
			//	            System.out.println("Arquivo não encontrado.");
			//	        }

	        String caminhoAtual = System.getProperty("user.dir");
	        System.out.println("Caminho de execução: " + caminhoAtual);

	        // Construindo o caminho completo para o arquivo CSV
	        String caminhoArquivo = caminhoAtual + getSeparator() + "src" + getSeparator() + "dadosDevisa.csv";
	        System.out.println("Caminho completo do arquivo: " + caminhoArquivo);
			
            String csvFile = caminhoArquivo;
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line;
                        		
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                List<String> row = new ArrayList<String>();
                for (String field : fields) {
                    row.add(field);
                }
                DevisaMetadados.dataDevisa.add(row);
            }

            // Process data
            for (List<String> row : DevisaMetadados.dataDevisa) {
                System.out.println("nomearquivo: " + row.get(0) + ", protocolado: " + row.get(1) + ", lote: " + row.get(2) );
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/* Ler arquivo dadosDataProtocolado */
	private static void LerCsv1() {
		try {			
	        String caminhoAtual = System.getProperty("user.dir");
	        System.out.println("Caminho de execução: " + caminhoAtual);

	        // Construindo o caminho completo para o arquivo CSV
	        String caminhoArquivo = caminhoAtual + getSeparator() + "src" + getSeparator() + "dadosDataProtocolado.csv";
	        System.out.println("Caminho completo do arquivo: " + caminhoArquivo);
			
            String csvFile = caminhoArquivo;
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line;
                        		
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                List<String> row = new ArrayList<String>();
                for (String field : fields) {
                    row.add(field);
                }
                DevisaMetadados.dataDevisa1.add(row);
            }

            // Process data
            for (List<String> row : DevisaMetadados.dataDevisa1) {
                System.out.println("numeroProtocolado: " + row.get(0) + ", data protocolado: " + row.get(1) );
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public static void main(String[] args) {			
		//Inicializa DATA_LOG 
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss_");  
		LocalDateTime now = LocalDateTime.now();				
		DATA_LOG = dtf.format(now);
		//		
		try {						
						
			if(DevisaMetadados.MODO ==2) {
				DevisaMetadados.LINUX = true;
				if (args == null || args.length != 3) {
				    System.err.println("Not enough arguments received.");
				    System.err.println("<diretório absoluto PDFS originais> <palavra-chave> <diretório Saida PDFS metadados>");				     
				    return;
				}
				else {
					caminho = args[0]; 
					palavrasChave = args[1];
					diretorioDestino = args[2];
				}	
			}	
			else { // Modo Local
				//diretorio Origem de leitura
				caminho = DIRETORIO_LOTE_1;
				
				 //metadados palavra chaves
				palavrasChave = "";
				
				//diretorio Destino de escrita
				diretorioDestino = DIRETORIO_DESTINO_LOTE1;
			}	
			
			if(DevisaMetadados.LINUX) { DIRETORIO = System.getProperty("java.io.tmpdir"); }
			
			// Arquivo / Protocolado
			LerCsv();
			//
			// Número Protocolado / Data Protocolado
			LerCsv1();					
			//
			//Cria diretorio de Log
			Files.createDirectories(Paths.get(DIRETORIO));			
			//
			// Deleta Arquivo de log			
			Files.deleteIfExists(Paths.get(DIRETORIO + getSeparator() + DATA_LOG+ARQUIVO_LOG));						
			//
			DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
			LocalDateTime now1 = LocalDateTime.now();			
			EscreveArquivo("Execução - "+dtf1.format(now1));
			EscreveArquivo("\n");			
			//			
			File diretorio = new File(caminho);
			ProcessarArquivos(diretorio, palavrasChave);
			System.out.print("Total de arquivos: " + DevisaMetadados.count);
			EscreveArquivo("Total de arquivos: " + DevisaMetadados.count);
			//
			DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
			LocalDateTime now2 = LocalDateTime.now();			
			EscreveArquivoLogInfo("Execução " +caminho+ " - " +dtf2.format(now2)+ ": Total de arquivos: "+DevisaMetadados.count);
						
		} catch (Exception e) {
			System.out.println("Utilize os parametros corretamente. ex: java -jar PdfMetadados.jar <diretório absoluto> <palavras-chave>");
			e.printStackTrace();
		}
	}
		
	public static void ProcessarArquivos(File diretorio, String palavrasChave) {
		FileFilter pdfFileFilter = (file1) -> {  return file1.getName().endsWith(".pdf");	};
		//
		File[] files = diretorio.listFiles(pdfFileFilter);
		DevisaMetadados.count = files.length; // Total arquios PDFS
		System.out.println("Total Arquivos no Diretório: "+files.length);
		//EscreveArquivo("Total Arquivos no Diretório: "+files.length);
		///EscreveArquivo("\n");
		
		if (files != null) {
            for (File pdfFile : files) {
                System.out.println("Arquivo PDF encontrado: " + pdfFile.getName());
                GravarMetadados(pdfFile, palavrasChave);
            }
		}				
	}
		
	@SuppressWarnings("deprecation")
	public static void GravarMetadados(File file, String palavrasChave) {
		
		String pathAntigo = file.getAbsolutePath();
		String stringAux1 = pathAntigo.replace(caminho, "");
		String dirDestino = DevisaMetadados.diretorioDestino + getSeparator() + stringAux1;		
		Path path = Paths.get(dirDestino);
		/*
		if (Files.exists(path)){
			EscreveArquivo("Arquivo " + pathAntigo + " já processado enteriormente!\n");
			System.out.println("Arquivo " + pathAntigo + " já processado enteriormente!");
			return;
		}*/
				
		//
		try {
			// Gera o hash (checksum) do arquivo
			byte[] dados = Files.readAllBytes(file.toPath());
			byte[] hash = MessageDigest.getInstance("MD5").digest(dados);
			String checksum = new BigInteger(1, hash).toString(16);
			
			System.out.print("Gravando " + file.getAbsolutePath());
			EscreveArquivo("Gravando " + file.getAbsolutePath());
			
			PDDocument document = PDDocument.load(file);
			// Obtem as informações do documento
			PDDocumentInformation info = document.getDocumentInformation();
			
			// Altera os metadados do documento
			info.setKeywords(DevisaMetadados.palavrasChave); // ok - não pedido
			info.setSubject("Inscrição em Dívida Corrente"); //ok
			info.setAuthor("Departamento de Vigilância em Saúde"); // Ok
			//							
			String dataString = info.getCreationDate().getTime().toString();
			//
			DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
			Instant instant = Instant.from(formatterInput.parse(dataString));			
			//Instant instant = Instant.parse(dataString); // Assumindo que a string está em formato ISO-8601
			ZoneId zonaHoraria = ZoneId.of("America/Sao_Paulo");
			ZonedDateTime zonedDateTime = instant.atZone(zonaHoraria);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String dataFormatada = zonedDateTime.format(formatter);
			System.out.println(dataFormatada);
			//
			String dataLocal = "Campinas, " + dataFormatada;					
	        //
			info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
			info.setCustomMetadataValue("Identificador do documento digital", file.getName().substring(0, file.getName().length()-4));//Identificador do documento digital - id único atribuido ao digitalizar
			info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
			//
			info.setCreator("Informática Municipios Associados");
			info.setProducer("Informática Municipios Associados");
			//			
			//String arquivo = file.getName().substring(0, file.getName().length()-4);
			String arquivo = file.getName();
			String numeroProtocolado = EncontrarProtocoloPorNomeArquivo(DevisaMetadados.dataDevisa, arquivo);			
			if(numeroProtocolado!=null && !numeroProtocolado.isEmpty()) { // Se null não achou no arquivo csv
				info.setTitle(numeroProtocolado);
				info.setCustomMetadataValue("Título", numeroProtocolado);
			}
			else { // se não encontrou numeroProtocolado insere branco 
				info.setTitle("");
				info.setCustomMetadataValue("Título", "");
				EscreveArquivo("\nNão encontrado Número Protocolado.... "+arquivo);
			}
			//
			info.setCustomMetadataValue("Tipo Documental", "Processo de Auto de Fiscalização"); // ok
			info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
			info.setCustomMetadataValue("Classe", "Vigilância em Saúde"); // OK
			//
			String anoDataProtocolado = (numeroProtocolado!=null && !numeroProtocolado.isEmpty()) ? numeroProtocolado.substring(0, 4) : "0000";
			//
			String dataProtocolado = (numeroProtocolado!=null && !numeroProtocolado.isEmpty()) 
									 ?  EncontrarDataProtocoloPorNumeroProtocolado(DevisaMetadados.dataDevisa1, numeroProtocolado)
								     : "00000000";
			
			if(dataProtocolado==null || dataProtocolado.equals("00000000")) {							
				EscreveArquivo("\nNão encontrado DataProtocolado.... "+arquivo);
			}					
			/* Log de mensagens */
			/* ==================================== */
			System.out.print(" - NumeroProtocolado: ");
			if(numeroProtocolado!=null && !numeroProtocolado.isEmpty()) {	System.out.println(numeroProtocolado); }
			System.out.print(" - DataProtocolado: ");
			if(dataProtocolado!=null && !dataProtocolado.isEmpty()) {	System.out.print(dataProtocolado); }
			//							
			EscreveArquivo(" - NumeroProtocolado: ");
			if(numeroProtocolado!=null && !numeroProtocolado.isEmpty()) {	EscreveArquivo(numeroProtocolado); }
			EscreveArquivo(" - DataProtocolado: ");
			if(dataProtocolado!=null && !dataProtocolado.isEmpty()) {	EscreveArquivo(dataProtocolado); }
			/* ==================================== */
			//			
			if(dataProtocolado!=null) {							
				info.setCustomMetadataValue("Data de produção do documento original", dataProtocolado);
			}	
			else {
				String dataProtocoladoPadronizada = "31/12/" + anoDataProtocolado;
				info.setCustomMetadataValue("Data de produção do documento original", dataProtocoladoPadronizada);
				System.out.print(" - Usado DataProtocoladoPadrão: " +dataProtocoladoPadronizada);
				EscreveArquivo(" - Usado DataProtocoladoPadrão: " +dataProtocoladoPadronizada);
			}					
			info.setCustomMetadataValue("Destinação Prevista", "Eliminação em 5 (cinco) anos após a quitação ou baixa do débito"); // ok
			info.setCustomMetadataValue("Gênero", "Textual"); // ok
			info.setCustomMetadataValue("Prazo de guarda", "2030"); // ok
			//
			//String  filename = file.getName().replaceAll("[^\\w\\s]","");
			//info.setCustomMetadataValue("Data original", "Campinas, " + filename.substring(0, 4));// Data de produção do documento original - Campinas, <ano do documento original>
			//
			// https://stackoverflow.com/questions/73550933/changing-author-title-info-of-an-existing-pdf-not-working
			/* Para setar o autor e keyword precisa disso */
		
			PDDocumentCatalog catalog = document.getDocumentCatalog();
			PDMetadata meta = catalog.getMetadata();
			XMPMetadata metadata = null;
			//
			
			if (meta != null) {
			    DomXmpParser xmpParser;
				try {
					xmpParser = new DomXmpParser();
					metadata = xmpParser.parse(meta.toByteArray());
				} catch (XmpParsingException e) {
					System.out.println(" Erro XMPMetadata do arquivo: " + file.getAbsolutePath());
					EscreveArquivo("\n");
					EscreveArquivo(" Erro XMPMetadata do arquivo: " + file.getAbsolutePath());
					metadata = XMPMetadata.createXMPMetadata();			    
				}			    
			}
			else {
			    meta = new PDMetadata(document);
			    catalog.setMetadata(meta);
			    metadata = XMPMetadata.createXMPMetadata();
			}
			/* --------------------------------------------------*/
			//
		
			DublinCoreSchema dcSchema = metadata.getDublinCoreSchema();
			if (dcSchema == null) {
			    dcSchema = metadata.createAndAddDublinCoreSchema();
			}
			dcSchema.setTitle(info.getTitle());
			dcSchema.addCreator(info.getAuthor()); // you may want to check whether the author is already there
			/* --------------------------------------------------*/
			//
			//List<XMPSchema> lista =  metadata.getAllSchemas();
		
			AdobePDFSchema pdfSchema = metadata.getAdobePDFSchema();
			if(pdfSchema == null ){
				pdfSchema = metadata.createAndAddAdobePDFSchema();
			}
            pdfSchema.setKeywords(info.getKeywords());
            pdfSchema.setProducer(info.getProducer());
			//
	        /* --------------------------------------------------*/
		
			XmpSerializer serializer = new XmpSerializer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {				
				serializer.serialize(metadata, baos, true);
			} catch (TransformerException e) {
				System.out.println("Erro ao serializar o arquivo: " + file.getAbsolutePath());
				EscreveArquivo("\n");
				EscreveArquivo("Erro ao serializar o arquivo: " + file.getAbsolutePath()); 
				e.printStackTrace();
			}
			meta.importXMPMetadata(baos.toByteArray());
											
			// Salva o documento com os novos metadados
			String infoGravado = "";
			if(SavePdfCaminhoConfig) {
				try {
					infoGravado = "Gravado em " + dirDestino;
					
					if(SavePdfCaminhoConfig) {
  						
						if (!Files.exists(path.getParent())){
							try {
								Files.createDirectories(path.getParent());
							} catch (IOException e) {
								System.out.println("Erro Criar Diretorio Destino");
								e.printStackTrace();
							}						
						}
					}						
					document.save(dirDestino);
				} catch (IOException e) {
					System.out.println("Erro Escrita diretorio Saida: ");
					e.printStackTrace();		
				}						
			}
			else {
				document.save(file.getAbsolutePath());
			}
				
			document.close();
			
			System.out.println(" -> [OK] -" + infoGravado);
			EscreveArquivo(" -> [OK] - "  + infoGravado);
			EscreveArquivo("\n");
			
		} catch (IOException e) {
			System.out.println("Erro ao abrir o arquivo: " + file.getAbsolutePath());
			EscreveArquivo("Erro ao abrir o arquivo: " + file.getAbsolutePath());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			EscreveArquivo("Erro ao gerar o hash para o arquivo: " + file.getAbsolutePath());
			System.out.println("Erro ao gerar o hash para o arquivo: " + file.getAbsolutePath());			
			e.printStackTrace();
		} catch (Exception e){
			System.out.println("Erro ao processar o arquivo: " + file.getAbsolutePath());
			EscreveArquivo("Erro ao processar o arquivo: " + file.getAbsolutePath());
			e.printStackTrace();
		}
		
//		finally {
//		   if( document != null ) {
//			   document.close();
//		   }
//		}
	}
	
	public static String EncontrarProtocoloPorNomeArquivo(List<List<String>> dataDevisa, String nomeArquivoProcurado) {
	    return dataDevisa.stream()
	        .filter(row -> row.get(0).equals(nomeArquivoProcurado))
	        .map(row -> row.get(1))
	        .findFirst()
	        .orElse(null);
	}
	
	public static String EncontrarDataProtocoloPorNumeroProtocolado(List<List<String>> dataDevisa1, String numeroProtocolado) {
	    return dataDevisa1.stream()
	    	.filter(row -> row.get(0).trim().equalsIgnoreCase(numeroProtocolado.trim()))
	        .map(row -> row.get(1))
	        .findFirst()
	        .orElse(null);
	}
}
