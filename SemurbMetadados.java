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

public class SemurbMetadados {
	/****************************************************************************************/
	static int count = 0;
	/* Configs */ // Diretorio tem que existir
	static String DIRETORIO = "C:\\\\temp\\\\metadadosLog2";
	static final String ARQUIVO_LOG = "logMetadados.txt";	          // cada execução produz um novo
	static final String ARQUIVO_LOG_TotalFiles = "logTotalFiles.txt"; // arquivo de Append
	public static String DATA_LOG=""; // usado para data arquivo de log
	//
	static String DIRETORIO_SEMURB = "E:\\Temp\\ArquivosGed\\SEMURB";
	//
	static String DIRETORIO_DESTINO_SEMURB = "E:\\Projetos2024\\teste-metadados\\SEMURB";	
	//
	public static String caminho=""; 
	public static String palavrasChave=""; 
	public static String diretorioDestino="";
	public static String rodarMetaCatolog=""; // "1"=SIM / "2" NÂO
	//	1 =SIM, 2 = NÂO
	//public static int RODAR_DUBLIN = 2;
	//	1 =Local, 2 = Console		
	public static int MODO = 1; 
	public static boolean SavePdfCaminhoConfig = true;
	//	
	public static boolean LINUX = false; // windows valor contrario	
	
	/* Armazena nome_arquivo / num_protocolo / data_cadastro / assunto / data_recebimento */
	public static List<List<String>> dataSermurb = new ArrayList<List<String>>();
	//
	public static String ultimoNivelCaminhoOrigem = "";
	public static String destinoAux = ""; // Subnivel a partir do qual quero gravar
	
	/****************************************************************************************/

	public static String getSeparator() {
		if(SemurbMetadados.LINUX) {
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
		if(SemurbMetadados.LINUX) {
			SemurbMetadados.DIRETORIO = System.getProperty("java.io.tmpdir");
		}
		
		File dir = new File(SemurbMetadados.DIRETORIO);
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
		if(SemurbMetadados.LINUX) {
			SemurbMetadados.DIRETORIO = System.getProperty("java.io.tmpdir");
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
	
	private static boolean isPdfExtension(File arquivo) {
		String fileName = arquivo.toString();

		int index = fileName.lastIndexOf('.');
		if(index > 0) {
			String extension = fileName.substring(index + 1);
			return extension.toLowerCase().equals("pdf");
		}
		else {
			return false;
		}
	}
	
	/* Ler arquivo Semurb  
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
	        String caminhoArquivo = caminhoAtual + getSeparator() + "src" + getSeparator() + "semurb_19092024.csv";
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
                SemurbMetadados.dataSermurb.add(row);
            }

            // Process data
            for (List<String> row : SemurbMetadados.dataSermurb) {
                //if(row.get(0) == null || row.get(1) == null || row.get(2) == null || row.get(3) == null || row.get(4) == null) {
                //	System.out.println( "Dado arquivo invalido");
                //}
                //else {
                	System.out.println( "Arquivo: " + row.get(0) + ", numero protocolado: " + row.get(1) + 
                					", dataCadastro: " + row.get(2) + ", assunto: " + row.get(3) +
                					", dataRecebimento: " + row.get(4) );
                //}
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
				
		try {						

			if(SemurbMetadados.MODO ==2) {
				SemurbMetadados.LINUX = true;
				if (args == null || (args.length < 3 || args.length > 4)) {
					System.err.println("Número de argumentos inválido. Esperados: 3 ou 4.");
				    System.err.println("<diretório absoluto PDFS originais> <palavra-chave> <diretório Saida PDFS metadados> <rodarcatalog>");				     
				    return;
				}
				else {
					
					if (args.length == 3) {										
						caminho = args[0]; 
						palavrasChave = args[1];
						diretorioDestino = args[2];
						rodarMetaCatolog = "1";
					}
					else if (args.length == 4) {
						caminho = args[0]; 
						palavrasChave = args[1];
						diretorioDestino = args[2];
						rodarMetaCatolog = args[3];			
					}					
				}	
			}
			
			else { // Modo Local
				//diretorio Origem de leitura
				caminho = SemurbMetadados.DIRETORIO_SEMURB;
				
				 //metadados palavra chaves
				palavrasChave = "";
				
				//diretorio Destino de escrita
				diretorioDestino = SemurbMetadados.DIRETORIO_DESTINO_SEMURB;
				
				rodarMetaCatolog = "1";
			}	
			
			if(SemurbMetadados.LINUX) { SemurbMetadados.DIRETORIO = System.getProperty("java.io.tmpdir"); }
												
			String separador =  getSeparator();
			int dirPaiAux = caminho.lastIndexOf(separador);
			ultimoNivelCaminhoOrigem = caminho.substring(dirPaiAux + 1);

			destinoAux = diretorioDestino + getSeparator();	// + ultimoNivelCaminhoOrigem;
			//
			// Cria diretorio de Log
			Files.createDirectories(Paths.get(SemurbMetadados.DIRETORIO));			
			//
			// Deleta Arquivo de log
			Files.deleteIfExists(Paths.get(SemurbMetadados.DIRETORIO + "/"+ DATA_LOG+ARQUIVO_LOG));									
			//
			DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
			LocalDateTime now1 = LocalDateTime.now();			
			EscreveArquivo("Execução - "+dtf1.format(now1));
			EscreveArquivo("\n");			
			//			
			File diretorio = new File(caminho);
			//
			// nome_arquivo / num_protocolo / data_cadastro / assunto / data_recebimento
			LerCsv();
			//					
			listarArquivosDiretorio(diretorio, palavrasChave);
			//
			System.out.print("Total de arquivos: " + SemurbMetadados.count);
			EscreveArquivo("Total de arquivos: " + SemurbMetadados.count);
			//
			DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
			LocalDateTime now2 = LocalDateTime.now();			
			EscreveArquivoLogInfo("Execução " +caminho+ " - " +dtf2.format(now2)+ ": Total de arquivos: "+SemurbMetadados.count);			
						
		} catch (Exception e) {
			System.out.println("Utilize os parametros corretamente. ex: java -jar PdfMetadados.jar <diretório absoluto> <palavras-chave>");
			e.printStackTrace();
		}
	}
	
			
	public static void listarArquivosDiretorio(File diretorio, String palavrasChave) {
				
		// Percorre todos os diretórios recursivamente listando todos os arquivos
		for (File file : diretorio.listFiles()) {
			if (!file.isDirectory()) {
				if(!isPdfExtension(file)) {
					continue;
				} 				
				gravarMetadados(file, palavrasChave);				
				SemurbMetadados.count++;
			} else { // se for Diretorio
				
				if(file.getName().equals("NOVA REMESSA")) { // se for diretório NOVA REMESSA por enquanto não processa
					//System.out.println(file.getName() + "Diretorio Pulado Configuração");
					//EscreveArquivo("Diretorio Pulado Configuração: "+file.getName());
					//EscreveArquivo("\n");
					//continue;
				}
								
				// Reproduzir a estrutura de Diretorio destino se MODO = 2 e save ligado
//				if(SavePdfCaminhoConfig) {
//					String diretorioDestinoCriar = destinoAux + "\\" + file.getName();									
//					try {
//						Files.createDirectories(Paths.get(diretorioDestinoCriar));
//					} catch (IOException e) {
//						System.out.println("Erro Criar Diretorio Destino");
//						e.printStackTrace();
//					}						
//				}
											
				EscreveArquivo("-------------------- Novo Diretório -----------------------------------------------------------------");
				EscreveArquivo("\n");
				System.out.println("-------------------- Novo Diretório -----------------------------------------------------------------");
				listarArquivosDiretorio(file, palavrasChave);
								
				FileFilter pdfFileFilter = (file1) -> {  return file1.getName().endsWith(".pdf");	};
				File[] files = file.listFiles(pdfFileFilter);
				System.out.println("Total Arquivos no Diretório: "+files.length);
				EscreveArquivo("Total Arquivos no Diretório: "+files.length);
				EscreveArquivo("\n");				
			}
		}
	}
	
		
	public static void gravarMetadados(File file, String palavrasChave) {
		//EscreveArquivo("Entrou function GravarMetadados");
		//System.out.println("Entrou function GravarMetadados");
		EscreveArquivo("Processando Arquivo: " +file.getName());
		System.out.println("Processando Arquivo: " + file.getName());
		
		String pathAntigo = file.getAbsolutePath();
		String stringAux1 = pathAntigo.replace(SemurbMetadados.caminho, "");
		//
		String dirDestino = SemurbMetadados.diretorioDestino + getSeparator() + stringAux1;
		Path path = Paths.get(dirDestino);

		EscreveArquivo("Origem: " +pathAntigo);
		System.out.println("Origem: " + pathAntigo);

		EscreveArquivo("DirDestino: " +dirDestino);
		System.out.println("DirDestino: " + dirDestino);

//		if (Files.exists(path)){
//			EscreveArquivo("Arquivo " + pathAntigo + " já processado enteriormente!\n");
//			System.out.println("Arquivo " + pathAntigo + " já processado enteriormente!");
//			return;
//		}
		
		try {
			
			String numProtocolo = "";
			String dataCadastro = "";
			String assunto= "";
		    String dataRecebimento = "";
		    List<String> dadosSemurb = new ArrayList<String>();
		    EscreveArquivo("Arquivo PDF: " +file.getName());
		    try {
		    	dadosSemurb = EncontrarDadosPorNomeArquivo(SemurbMetadados.dataSermurb, file.getName() );
			} catch (Exception e) {
				EscreveArquivo("Exception: Dados Arquivo CSV não encontrado: " +file.getName() + " - " +e.getMessage());
			}		    							
			if (dadosSemurb != null) {
				numProtocolo = dadosSemurb.get(0);
				dataCadastro = dadosSemurb.get(1);
				assunto = dadosSemurb.get(2);
				dataRecebimento = dadosSemurb.get(3);
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
			info.setKeywords(SemurbMetadados.palavrasChave); // ok - não pedido			
			info.setSubject(assunto); // ok
			//
			//info.setAuthor("Secretaria Municipal de Urbanismo"); // Ok
						
			//info.setAuthor("Teste urbano"); // Ok
//			if(info.getAuthor().contains("; ")) {
//				System.out.println("Entrou no replace info.getAuthor() ");
//				
//				String a = "Secretaria Municipal de Urbanismo".replace("; ", "");				
//				info.setAuthor(a);
//			}					
			//================================================================
			String author = "Secretaria Municipal de Urbanismo".trim().replaceAll("^\\p{P}+", "");											
			byte[] bytes = author.getBytes(StandardCharsets.UTF_8);
			String encodedAuthor = new String(bytes, StandardCharsets.UTF_8);
			info.setAuthor(encodedAuthor);							
			//================================================================
			//
			System.out.println(" info.getAuthor(): " + info.getAuthor());
			EscreveArquivo("\n info.getAuthor(): " + info.getAuthor());
			//
			
			//
			info.setCreator("Informática Municipios Associados");
			info.setProducer("Informática Municipios Associados");
			//info.setTitle( file.getName().substring(0, file.getName().length()-4) ); // Numero do processo - nome do arquivo
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
			System.out.println(" - dataCriação:" +dataFormatada);
			//
			String dataLocal = "(" + dataFormatada + "), Campinas";				
	        //
			info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
			info.setCustomMetadataValue("Identificador do documento digital", file.getName().substring(0, file.getName().length()-4));//Identificador do documento digital - id único atribuido ao digitalizar
			info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
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
			info.setCustomMetadataValue("Tipo Documental", "Ficha de Informação de Zoneamento e Uso do Solo"); // ok
			info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
			info.setCustomMetadataValue("Classe", "Território e Desenvolvimento Urbano"); // OK
			//
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
			info.setCustomMetadataValue("Destinação Prevista", "Eliminação após 10 (dez) anos de custódia."); // ok
			info.setCustomMetadataValue("Gênero", "Textual"); // ok
			//							
			String anoDataRecebimento = (dataRecebimento!=null && !dataRecebimento.isEmpty()) ?  dataRecebimento.substring(dataRecebimento.length() - 4) : "00";
			if(anoDataRecebimento.equals("00")) {							
				EscreveArquivo("\nNão encontrado DataRecebimento.... "+file.getName());
			}					
			else {
				anoDataRecebimento = Integer.toString ( Integer.parseInt(anoDataRecebimento) + 10 );
			}			
			info.setCustomMetadataValue("Prazo de guarda", anoDataRecebimento); // se não achar vai colocar "00" na guarda					
			
			// Só roda catalog se for ""1
			if(SemurbMetadados.rodarMetaCatolog == "1") {
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
						//xmpParser.setStrictParsing(true);
						metadata = xmpParser.parse(meta.toByteArray());					
					} catch (XmpParsingException e) {
						System.out.println(" Erro XMPMetadata do arquivo: " + file.getAbsolutePath());
						EscreveArquivo("\n");
						EscreveArquivo(" Erro XMPMetadata do arquivo: " + file.getAbsolutePath());
						e.printStackTrace();
						//metadata = XMPMetadata.createXMPMetadata();
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
				//
				System.out.println(" DublinCoreSchema author: " + info.getAuthor());
				EscreveArquivo("\nDublinCoreSchema author: " + info.getAuthor());
				//
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
			}
			
			// Salva o documento com os novos metadados
			String infoGravado = "";
			if(SavePdfCaminhoConfig) {
				try {					
//					String pathAntigo = file.getAbsolutePath();					
//					int aux = pathAntigo.indexOf(ultimoNivelCaminhoOrigem);
//					String stringAux1 = pathAntigo.substring(aux);
//					String dirDestino = diretorioDestino + "\\" + stringAux1;		
//					infoGravado = "Gravado em " +dirDestino;							
//					document.save(dirDestino);
										
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
	
	/* Dado o nome do arquivo pdf retorna uma lista com
	 *  [0] = numero protocolo;
	 *  [1] = data_cadastro;
     *  [2] = descricao_assunto;
     *  [3] = data_recebimento;
	 * */
	public static List<String> EncontrarDadosPorNomeArquivo(List<List<String>> dataSemurb, String nomeArquivoProcurado) {
	    return dataSemurb.stream()
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