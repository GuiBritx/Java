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

public class SinfraMetadados {
	/****************************************************************************************/
	static int count = 0;
	/* Configs */ // Diretorio tem que existir
	static String DIRETORIO = "C:\\Seinfra";
	static final String ARQUIVO_LOG = "logMetadados.txt";	          // cada execução produz um novo
	static final String ARQUIVO_LOG_TotalFiles = "logTotalFiles.txt"; // arquivo de Append
	public static String DATA_LOG=""; // usado para data arquivo de log
	//
	static String DIRETORIO_Sinfra = "C:\\Seinfra";
	//
	static String DIRETORIO_DESTINO_Sinfra = "C:\\Seinfra\\Destino";	
	//	
	public static String caminho=""; 
	public static String palavrasChave=""; 
	public static String diretorioDestino="";
	//	1 =Local, 2 = Console
	public static int MODO = 1;
	public static boolean SavePdfCaminhoConfig = true;
	//	
	public static boolean LINUX = false; // windows valor contrario	
	
	/* Armazena nome_arquivo / num_protocolo / data_cadastro / assunto / data_recebimento */
	public static List<List<String>> dataSinfra = new ArrayList<List<String>>();	
	/****************************************************************************************/
	
	public static String getSeparator() {
		if(SinfraMetadados.LINUX) {
			return "/";
		}
		else {		
			String separator = File.separator;
			if(separator.equals("\\")) {
				separator += "\\";
			}
			return separator;
			//return "/";
		}
	}
	
	private static void EscreveArquivoLogInfo(String texto) {
		if(SinfraMetadados.LINUX) {
			SinfraMetadados.DIRETORIO = System.getProperty("java.io.tmpdir");
		}
		
		File dir = new File(SinfraMetadados.DIRETORIO);
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
		if(SinfraMetadados.LINUX) {
			SinfraMetadados.DIRETORIO = System.getProperty("java.io.tmpdir");
		}
		
		File dir = new File(SinfraMetadados.DIRETORIO);
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

	/* Ler arquivo SInfora  
	 *  [0] = nome_arquivo;
	 *  [1] = protocolo;
	 *  [2] = data_cadastro;
     *  [3] = descricao_assunto;
     *  [4] = data_recebimento;
	 * */
	private static void LerCsv() {
		try {			
	        String caminhoAtual = System.getProperty("user.dir");
	        System.out.println("Caminho de execução: " + caminhoAtual);

	        // Construindo o caminho completo para o arquivo CSV
	        String caminhoArquivo = caminhoAtual + getSeparator() + "src" + getSeparator() + "dadosSinfra.csv";
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
                try {
                	SinfraMetadados.dataSinfra.add(row);
				} catch (Exception e) {
					System.err.println("Arquivo CSV error: " + e.getMessage() + row.toString());
				}
                
            }
            System.out.println("Num Linhas csv: "+SinfraMetadados.dataSinfra.size());
            
            // Process data
            for (List<String> row : SinfraMetadados.dataSinfra) {
            	System.out.println( "Arquivo: " + row.get(0) + ", numero protocolado: " + row.get(1) + 
    					", dataCadastro: " + row.get(2) + ", assunto: " + row.get(3) +
    					", dataRecebimento: " + row.get(4) );
            }

            br.close();

        } catch (Exception e) {
            //e.printStackTrace();
        	System.err.println("Arquivo CSV error: " + e.getMessage());
        }
	}
	
	public static void main(String[] args) {			
		//Inicializa DATA_LOG 
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss_");  
		LocalDateTime now = LocalDateTime.now();				
		DATA_LOG = dtf.format(now);
		//		
		try {						
						
			if(SinfraMetadados.MODO ==2) {
				SinfraMetadados.LINUX = true;
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
				caminho = SinfraMetadados.DIRETORIO_Sinfra;
				
				 //metadados palavra chaves
				palavrasChave = "";
				
				//diretorio Destino de escrita
				diretorioDestino = SinfraMetadados.DIRETORIO_DESTINO_Sinfra;
			}	
			
			if(SinfraMetadados.LINUX) { SinfraMetadados.DIRETORIO = System.getProperty("java.io.tmpdir"); }
			
			// Infos SInfra
			LerCsv();
			//
			//Cria diretorio de Log
			Files.createDirectories(Paths.get(SinfraMetadados.DIRETORIO));			
			//
			// Deleta Arquivo de log			
			Files.deleteIfExists(Paths.get(SinfraMetadados.DIRETORIO + getSeparator() + DATA_LOG+ARQUIVO_LOG));						
			//
			DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
			LocalDateTime now1 = LocalDateTime.now();			
			EscreveArquivo("Execução - "+dtf1.format(now1));
			EscreveArquivo("\n");			
			//			
			File diretorio = new File(caminho);
			ProcessarArquivos(diretorio, palavrasChave);
			System.out.print("Total de arquivos: " + SinfraMetadados.count);
			EscreveArquivo("Total de arquivos: " + SinfraMetadados.count);
			//
			DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
			LocalDateTime now2 = LocalDateTime.now();			
			EscreveArquivoLogInfo("Execução " +caminho+ " - " +dtf2.format(now2)+ ": Total de arquivos: "+SinfraMetadados.count);
						
		} catch (Exception e) {
			System.out.println("Utilize os parametros corretamente. ex: java -jar PdfMetadados.jar <diretório absoluto> <palavras-chave>");
			e.printStackTrace();
		}
	}
		
	public static void ProcessarArquivos(File diretorio, String palavrasChave) {
		FileFilter pdfFileFilter = (file1) -> {  return file1.getName().endsWith(".pdf") || file1.getName().endsWith(".PDF") ;	};
		//
		File[] files = diretorio.listFiles(pdfFileFilter);
		SinfraMetadados.count = files.length; // Total arquivos PDFS
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
		String dirDestino = SinfraMetadados.diretorioDestino + getSeparator() + stringAux1;		
		Path path = Paths.get(dirDestino);
		/*
		if (Files.exists(path)){
			EscreveArquivo("Arquivo " + pathAntigo + " já processado enteriormente!\n");
			System.out.println("Arquivo " + pathAntigo + " já processado enteriormente!");
			return;
		}*/
				
		//
		try {
			
			String numProtocolo = "";
			String dataCadastro = "";
			String assunto= "";
		    String dataRecebimento = "";
		    List<String> dadosSinfra = new ArrayList<String>();
		    EscreveArquivo("Arquivo PDF: " +file.getName());
		    try {
		    	dadosSinfra = EncontrarDadosPorNomeArquivo(SinfraMetadados.dataSinfra, file.getName() );
			} catch (Exception e) {
				EscreveArquivo("Exception: Dados Arquivo CSV não encontrado: " +file.getName() + " - " +e.getMessage());
			}		    							
			if (dadosSinfra != null) {
				numProtocolo = dadosSinfra.get(0);
				dataCadastro = dadosSinfra.get(1);
				assunto = dadosSinfra.get(2);
				dataRecebimento = dadosSinfra.get(3);
			}
			else {
				EscreveArquivo("Dados Arquivo CSV não encontrado: " +file.getName());
				EscreveArquivo("\n");
			}
			
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
			info.setKeywords(SinfraMetadados.palavrasChave); // ok - não pedido
			info.setSubject(assunto);
			info.setAuthor("Secretaria Municipal de Infraestrutura");
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
			String dataLocal = dataFormatada + ", Campinas";					
	        //
			info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
			//
			info.setCustomMetadataValue("Identificador do documento digital", file.getName().substring(0, file.getName().length()-4));//Identificador do documento digital - id único atribuido ao digitalizar
			//		
			info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
			//
			info.setCreator("Informática Municipios Associados");
			info.setProducer("Informática Municipios Associados");
			//			
			if(numProtocolo!=null && !numProtocolo.isEmpty()) { // Se null não achou no arquivo csv
				info.setTitle(numProtocolo);
				info.setCustomMetadataValue("Título", numProtocolo);
			}
			else { // se não encontrou numeroProtocolado insere branco 
				info.setTitle("");
				info.setCustomMetadataValue("Título", "");
				EscreveArquivo("\nNão encontrado Número Protocolado.... "+file.getName());
			}
			//
			
			String tipoDocumental = "Processo de solicitação de utilização do solo público, nos termos da Lei n° 10.639, de 5 de outubro de 2000";
			info.setCustomMetadataValue("Tipo Documental", tipoDocumental); // ok
			info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
			info.setCustomMetadataValue("Classe", "Território e Desenvolvimento Urbano"); // OK
			//
			if(dataCadastro==null || dataCadastro.isEmpty()) {	
				EscreveArquivo("\nNão encontrado DataProtocolado.... "+file.getName());
			}					
			/* Log de mensagens */
			/* ==================================== */
			System.out.print(" - NumeroProtocolado: ");
			if(numProtocolo!=null && !numProtocolo.isEmpty()) {	System.out.println(numProtocolo); }
			System.out.print(" - DataProtocolado: ");
			if(dataCadastro!=null && !dataCadastro.isEmpty()) {	System.out.print(dataCadastro); }
			//							
			EscreveArquivo(" - NumeroProtocolado: ");
			if(numProtocolo!=null && !numProtocolo.isEmpty()) {	EscreveArquivo(numProtocolo); }
			EscreveArquivo(" - DataProtocolado: ");
			if(dataCadastro!=null && !dataCadastro.isEmpty()) {	EscreveArquivo(dataCadastro); }
			/* ==================================== */
			//			
			if(dataCadastro!=null && !dataCadastro.isEmpty()) {							
				info.setCustomMetadataValue("Data de produção do documento original", "Campinas, " +dataCadastro);
			}	
			else { // regra 31/12
				//String dataProtocoladoPadronizada = "31/12/" + anoDataProtocolado;
				//info.setCustomMetadataValue("Data de produção do documento original", "Campinas, " + dataProtocoladoPadronizada);
				//System.out.print(" - Usado DataProtocoladoPadrão: " +dataProtocoladoPadronizada);
				//EscreveArquivo(" - Usado DataProtocoladoPadrão: " +dataProtocoladoPadronizada);
			}					
					
			info.setCustomMetadataValue("Destinação Prevista", "Eliminação 40 (quarenta) anos, após a emissão do Termo de Recebimento Definitivo de Obra."); // ok
			info.setCustomMetadataValue("Gênero", "Textual e Cartográfico"); // ok
			
			String anoDataRecebimento = (dataRecebimento!=null && !dataRecebimento.isEmpty()) ?  dataRecebimento.substring(dataRecebimento.length() - 4) : "00";
			if(anoDataRecebimento.equals("00")) {							
				EscreveArquivo("\nNão encontrado DataRecebimento.... "+file.getName());
			}					
			else {
				anoDataRecebimento = Integer.toString ( Integer.parseInt(anoDataRecebimento) + 40 );
			}			
			info.setCustomMetadataValue("Prazo de guarda", anoDataRecebimento); // se não achar vai colocar "00" na guarda	
			
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
	}
		
	public static List<String> EncontrarDadosPorNomeArquivo(List<List<String>> dataSinfra, String nomeArquivoProcurado) {
	    return dataSinfra.stream()
	        .filter(row -> row.get(0).equals(nomeArquivoProcurado))
	        .map(row ->   {
	        	// Para Java 8 ou inferior
                List<String> dados = new ArrayList<>();
                dados.add(row.get(1));
                dados.add(row.get(2));
                dados.add(row.get(3));
                dados.add(row.get(4));
                return dados;
	        })
	        .findFirst()
	        .orElse(null);
	}		
}