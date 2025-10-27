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
import java.nio.file.StandardCopyOption;
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

public class SmgMetadados {
	/****************************************************************************************/
	static int count = 0;
	/* Configs */ // Diretorio tem que existir
	static String DIRETORIO = "C:\\\\temp\\\\metadadosLog3";
	static final String ARQUIVO_LOG = "logMetadados.txt";	          // cada execução produz um novo
	static final String ARQUIVO_LOG_TotalFiles = "logTotalFiles.txt"; // arquivo de Append
	public static String DATA_LOG=""; // usado para data arquivo de log
	//
	static String DIRETORIO_SMG = "E:\\Temp\\ArquivosGed\\SMG\\DGIDAC\\ARQUIVOS LANCADOS";
	//		
	static String DIRETORIO_DESTINO_SMG = "E:\\Projetos2024\\teste-metadados\\SMG\\DGIDAC\\ARQUIVOS LANCADOS";	
	//
	public static String caminho=""; 
	public static String palavrasChave=""; 
	public static String diretorioDestino="";
	//	1 =Local, 2 = Console
	public static int MODO = 2;
	public static boolean SavePdfCaminhoConfig = true;
	//	
	public static boolean LINUX = false; // windows valor contrario
	//
	public enum GrupoProtocolo {
	    PROTOCOLO_2005_1("2005/10/41344"),
	    PROTOCOLO_2005_2("2005/10/50760"),
	    PROTOCOLO_2012_1("2012/10/7844"),
	    PROTOCOLO_2012_2("2012/10/42276"),
	    PROTOCOLO_2012_3("2012/10/25229"),
	    PROTOCOLO_2012_4("2012/10/48800"),
	    PROTOCOLO_2021_1("2021/10/8353"),
	    NOT_DEFINED("Não PROCESSADO");

	    private final String descricao;

	    GrupoProtocolo(String descricao) {
	        this.descricao = descricao;
	    }

	    public String getDescricao() {
	        return descricao;
	    }
	}
	/****************************************************************************************/
	
	public static String getSeparator() {
		if(SmgMetadados.LINUX) {
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
		if(SmgMetadados.LINUX) {
			SmgMetadados.DIRETORIO = System.getProperty("java.io.tmpdir");
		}
		
		File dir = new File(SmgMetadados.DIRETORIO);
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
		if(SmgMetadados.LINUX) {
			SmgMetadados.DIRETORIO = System.getProperty("java.io.tmpdir");
		}
		
		File dir = new File(SmgMetadados.DIRETORIO);
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

	public static void main(String[] args) {				
		//Inicializa DATA_LOG 
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss_");  
		LocalDateTime now = LocalDateTime.now();				
		DATA_LOG = dtf.format(now);
		//		
		try {						
						
			if(SmgMetadados.MODO ==2) {
				SmgMetadados.LINUX = true;
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
				caminho = DIRETORIO_SMG;
				
				 //metadados palavra chaves
				palavrasChave = "";
				
				//diretorio Destino de escrita
				diretorioDestino = DIRETORIO_DESTINO_SMG;
			}	
			
			if(SmgMetadados.LINUX) { DIRETORIO = System.getProperty("java.io.tmpdir"); }			
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
			System.out.print("Total de arquivos: " + SmgMetadados.count);
			EscreveArquivo("Total de arquivos: " + SmgMetadados.count);
			//
			DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");  
			LocalDateTime now2 = LocalDateTime.now();			
			EscreveArquivoLogInfo("Execução " +caminho+ " - " +dtf2.format(now2)+ ": Total de arquivos: "+SmgMetadados.count);
						
		} catch (Exception e) {
			System.out.println("Utilize os parametros corretamente. ex: java -jar PdfMetadados.jar <diretório absoluto> <palavras-chave>");
			e.printStackTrace();
		}
	}
	
	/* Copiar arquivos PDF do GrupoProtocolo.NOT_DEFINED para destino para que possa ser feito a assinatura 
	 * */
	public static void CopiarFilePDF(File arquivoPDF) {			
	    // Novo destino para o arquivo (substitua pelo destino desejado)
	    //Path destino = Paths.get("C:\\caminho\\para\\o\\novo\\destino\\arquivo_copiado.pdf");
	
		Path origem = Paths.get(arquivoPDF.getAbsolutePath());
		//
		String pathAntigo = arquivoPDF.getAbsolutePath();
		String stringAux1 = pathAntigo.replace(caminho, "");
		String dirDestino = SmgMetadados.diretorioDestino + getSeparator() + stringAux1;		
		//
		Path destino = Paths.get(dirDestino);
			
	    // Copia o arquivo, sobrescrevendo se já existir
	    try {
			Files.copy(arquivoPDF.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Falhou na cópia de arquivo sem metadados: " +pathAntigo);
		}	
	    //System.out.println("Arquivo copiado com sucesso!");
	}
	
	/* Copia arquivos PDF da pasta 'Digitalizados em Março 2024' 
	 * */
	// @Deprecated: função deprecated 
	public static void CopiarPasta(File diretorio) {                
		Path origem = Paths.get(diretorio.getAbsolutePath());
		//
		String pathAntigo = diretorio.getAbsolutePath();
		String stringAux1 = pathAntigo.replace(caminho, "");
		String dirDestino = SmgMetadados.diretorioDestino + getSeparator() + stringAux1;		
		//
		Path destino = Paths.get(dirDestino);
			
		// Cria o diretório de destino se não existir				
        try {
			Files.createDirectories(destino);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Falhou criação da  pasta Digitalizados em Março 2024");
		}

        // Copia todos os arquivos da pasta de origem para a pasta de destino
        try {
			Files.walk(origem)
			        .filter(Files::isRegularFile)
			        .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
			        .forEach(source -> {
			            Path target = destino.resolve(origem.relativize(source));
			            try {
			                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			            } catch (IOException e) {
			            	System.err.println("Falhou copia da pasta Digitalizados em Março 2024");
			            }
			        });
		} catch (IOException e) {
			System.err.println("Falhou copia da pasta Digitalizados em Março 2024");
			//e.printStackTrace();
		}

        System.out.println("Cópia pdfs pasta Digitalizados em Março 2024 realizada com sucesso!");
        EscreveArquivo("Cópia pdfs pasta Digitalizados em Março 2024 realizada com sucesso!\n");        
	}
	
	/* Tratamento pasta 'Digitalizados em Março 2024' 
	 * */ 
	public static void TratamentoSubpastaDigitalizadosMarco(File diretorio, String palavrasChave) {                
		Path origem = Paths.get(diretorio.getAbsolutePath());
		//
		String pathAntigo = diretorio.getAbsolutePath();
		String stringAux1 = pathAntigo.replace(caminho, "");
		String dirDestino = SmgMetadados.diretorioDestino + getSeparator() + stringAux1;		
		//
		Path destino = Paths.get(dirDestino);
			
		// Cria o diretório de destino se não existir				
        try {
			Files.createDirectories(destino);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Falhou criação da  pasta Digitalizados em Março 2024");
		}

		FileFilter pdfFileFilter = (file1) -> {  return file1.getName().endsWith(".pdf");	};
		//
		File[] files = diretorio.listFiles(pdfFileFilter);
		SmgMetadados.count = files.length; // Total arquios PDFS
		System.out.println("Total Arquivos no Diretório Digitalizados Março: "+files.length);
		EscreveArquivo("Total Arquivos no Diretório Digitalizados Março: "+files.length);		
		EscreveArquivo("\n");
		
		if (files != null) {
            for (File pdfFile : files) {
                System.out.println("Arquivo PDF encontrado: " + pdfFile.getName());
                GrupoProtocolo grupo = ClassificarArquivo(pdfFile.getName());
                System.out.println("O arquivo " + pdfFile.getName() + " é do tipo: " + grupo.getDescricao());
                if(grupo == GrupoProtocolo.NOT_DEFINED) {
                	System.out.println("=========Arquivo " + pdfFile.getName() + " não processado.=========");
                	EscreveArquivo("=========Arquivo " + pdfFile.getName() + " não processado.=========\n");
                	//continue; // não faz nada
                	// Copia arquivo para destino
                	CopiarFilePDF(pdfFile);
                	System.out.println("=========Arquivo " + pdfFile.getName() + " copiado para destino =========");
                	EscreveArquivo("=========Arquivo sem metadados " + pdfFile.getName() + " copiado para destino =========\n");
                }
                else {
                	GravarMetadados(pdfFile, palavrasChave, grupo);
                }
            }
		}				

        //System.out.println("Cópia pdfs pasta Digitalizados em Março 2024 realizada com sucesso!");
        //EscreveArquivo("Cópia pdfs pasta Digitalizados em Março 2024 realizada com sucesso!\n");        
	}
	
	/* Classifica o protocoloco em grupos de acordo com o nome do arquivo
	 * */
    public static GrupoProtocolo ClassificarArquivo(String nomeArquivo) {
		if (nomeArquivo.toLowerCase().contains("2005-10-41344")) {
			return GrupoProtocolo.PROTOCOLO_2005_1;		    
		} else if (nomeArquivo.toLowerCase().contains("2005-10-50760")) {
			return GrupoProtocolo.PROTOCOLO_2005_2;
		} else if(nomeArquivo.toLowerCase().contains("2012-10-7844")) {
			return GrupoProtocolo.PROTOCOLO_2012_1;			
		} else if(nomeArquivo.toLowerCase().contains("2012-10-42276")) {
			return GrupoProtocolo.PROTOCOLO_2012_2;
		} else if(nomeArquivo.toLowerCase().contains("2012-10-25229")) {
			return GrupoProtocolo.PROTOCOLO_2012_3;
		} else if(nomeArquivo.toLowerCase().contains("2012-10-48800")) {
			return GrupoProtocolo.PROTOCOLO_2012_4;
		} else if(nomeArquivo.toLowerCase().contains("2021-10-8353")) {
			return GrupoProtocolo.PROTOCOLO_2021_1;			
		} else {
			return GrupoProtocolo.NOT_DEFINED;
		}
    }
		    
	public static void ProcessarArquivos(File diretorio, String palavrasChave) {				

		//		// Copia arquivos PDF da pasta 'Digitalizados em Março 2024'
		//		FileFilter directoryFilter = (file1) -> {  return file1.isDirectory();	};
		//		File[] todosOsElementos = diretorio.listFiles(directoryFilter); // lista os diretorios: 1 somente
		//		if (todosOsElementos != null) {
		//			for (File file : todosOsElementos) {                                 	                                
		//				CopiarPasta(file);
		//            }	
		//		}		
		//========================================================================
		// Pasta 'Digitalizados em Março 2024' deve ser processada
		FileFilter directoryFilter = (file1) -> {  return file1.isDirectory();	};
		File[] todosOsElementos = diretorio.listFiles(directoryFilter); // lista os diretorios: 1 somente
		if (todosOsElementos != null) {
			for (File diretorioDigitalizados : todosOsElementos) {                                 	                                
				TratamentoSubpastaDigitalizadosMarco(diretorioDigitalizados, palavrasChave);
            }
		}
		
		//========================================================================		
		FileFilter pdfFileFilter = (file1) -> {  return file1.getName().endsWith(".pdf");	};
		//
		File[] files = diretorio.listFiles(pdfFileFilter);
		SmgMetadados.count = files.length; // Total arquios PDFS
		System.out.println("Total Arquivos no Diretório: "+files.length);
		//EscreveArquivo("Total Arquivos no Diretório: "+files.length);
		///EscreveArquivo("\n");
		
		if (files != null) {
            for (File pdfFile : files) {
                System.out.println("Arquivo PDF encontrado: " + pdfFile.getName());
                GrupoProtocolo grupo = ClassificarArquivo(pdfFile.getName());
                System.out.println("O arquivo " + pdfFile.getName() + " é do tipo: " + grupo.getDescricao());
                if(grupo == GrupoProtocolo.NOT_DEFINED) {
                	System.out.println("=========Arquivo " + pdfFile.getName() + " não processado.=========");
                	EscreveArquivo("=========Arquivo " + pdfFile.getName() + " não processado.=========\n");
                	//continue; // não faz nada
                	// Copia arquivo para destino
                	CopiarFilePDF(pdfFile);
                	System.out.println("=========Arquivo " + pdfFile.getName() + " copiado para destino =========");
                	EscreveArquivo("=========Arquivo sem metadados " + pdfFile.getName() + " copiado para destino =========\n");
                }
                else {
                	GravarMetadados(pdfFile, palavrasChave, grupo);
                }
            }
		}				
	}
		
	@SuppressWarnings("deprecation")
	public static void GravarMetadados(File file, String palavrasChave, GrupoProtocolo grupo) {
		
		String pathAntigo = file.getAbsolutePath();
		String stringAux1 = pathAntigo.replace(caminho, "");
		String dirDestino = SmgMetadados.diretorioDestino + getSeparator() + stringAux1;		
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
			
			// setar INFO baseado do Grupo
			switch (grupo) {
            case PROTOCOLO_2005_1:                
            	System.out.println("Processando protocolo 2005/1");
            	PreencheMetadadosProtocolo2005_1(info, file, checksum);
                break;
            case PROTOCOLO_2005_2:                
            	System.out.println("Processando protocolo 2005/2");
            	PreencheMetadadosProtocolo2005_2(info, file, checksum);
                break;
            case PROTOCOLO_2012_1:                
            	System.out.println("Processando protocolo 2012/1");
            	PreencheMetadadosProtocolo2012_1(info, file, checksum);
                break;
            case PROTOCOLO_2012_2:               
            	System.out.println("Processando protocolo 2012/2");
            	PreencheMetadadosProtocolo2012_2(info, file, checksum);
                break;
            case PROTOCOLO_2012_3:
            	System.out.println("Processando protocolo 2012/3");
            	PreencheMetadadosProtocolo2012_3(info, file, checksum);
                break;
            case PROTOCOLO_2012_4:                
            	System.out.println("Processando protocolo 2012/4");
            	PreencheMetadadosProtocolo2012_4(info, file, checksum);
                break;
            case PROTOCOLO_2021_1:                
            	System.out.println("Processando protocolo 2021/1");
            	PreencheMetadadosProtocolo2021_1(info, file, checksum);
                break;                
            default:
                System.out.println("Protocolo não definido");
			}
			
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
    	
    /* Protocolo 2005/10/41344
     * */
	public static void PreencheMetadadosProtocolo2005_1(PDDocumentInformation info, File file, String checksum) {
		// Altera os metadados do documento
		info.setKeywords(SmgMetadados.palavrasChave); // ok - não pedido
		info.setSubject("Albano de Almeida Lima, 364, Guanabara"); //ok
		info.setAuthor("Secretaria Municipal de Assuntos Jurídicos"); // Ok
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
		//System.out.println(dataFormatada);
		//
		String dataLocal = "Campinas, " + dataFormatada;					
        //
		info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
		//			
		int indiceInicial = file.getName().length() - 5; // Começa 5 posições antes do final
		int indiceFinal = file.getName().length() - 4; // Termina 3 posições antes do final (exclusivo)
		//
		String versao = file.getName().substring(indiceInicial, indiceFinal);
		System.out.println("Número extraído: " + versao);
		//
		String identificadorAux = "2005_10_41344_X_5";
		String identificador = identificadorAux.replace("X", versao);
		//
		info.setCustomMetadataValue("Identificador do documento digital", identificador);
		info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
		//
		info.setCreator("Informática Municipios Associados");
		info.setProducer("Informática Municipios Associados");
		//			
		info.setTitle("Locação de imóvel para cartório da 275º Zona Eleitoral");
		info.setCustomMetadataValue("Título", "Locação de imóvel para cartório da 275º Zona Eleitoral");		
		//
		info.setCustomMetadataValue("Tipo Documental", "Processo de Licitação de Serviço Comum"); // ok
		info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
		info.setCustomMetadataValue("Classe", "Controle de compras, serviços e obras"); // OK
		info.setCustomMetadataValue("Data de produção do documento original", "2005-2013");			
		info.setCustomMetadataValue("Destinação Prevista", "Eliminação 12 anos após encerramento"); // ok
		info.setCustomMetadataValue("Gênero", "Textual"); // ok
		info.setCustomMetadataValue("Prazo de guarda", "2025"); // ok
	}

    /* Protocolo 2005/10/50760
     * */
	public static void PreencheMetadadosProtocolo2005_2(PDDocumentInformation info, File file, String checksum) {
		// Altera os metadados do documento
		info.setKeywords(SmgMetadados.palavrasChave); // ok - não pedido
		info.setSubject("Contrato da empresa Bags Tour- Viagens, Turismo e Câmbio Ltda."); //ok
		info.setAuthor("Secretaria de Chefia de Gabinete do Prefeito"); // Ok
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
		//System.out.println(dataFormatada);
		//
		String dataLocal = "Campinas, " + dataFormatada;					
        //
		info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
		//			
		int indiceInicial = file.getName().length() - 5; // Começa 5 posições antes do final
		int indiceFinal = file.getName().length() - 4; // Termina 3 posições antes do final (exclusivo)
		//
		String versao = file.getName().substring(indiceInicial, indiceFinal);
		System.out.println("Número extraído: " + versao);
		//
		String identificadorAux = "2005_10_50760_X_9";
		String identificador = identificadorAux.replace("X", versao);
		//
		info.setCustomMetadataValue("Identificador do documento digital", identificador);
		info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
		//
		info.setCreator("Informática Municipios Associados");
		info.setProducer("Informática Municipios Associados");
		//
		/*
		String titulo = "Contratação de empresa para prestação de serviço de fornecimento de passagens "
				+ "aéreas nacionais e internacionais, compreendendo a reserva, emissão, marcação, "
				+ "remarcação, endosso e entrega de bilhetes ou ordenas de passagens, bem como "
				+ "reserva de hospedagem em hotéis de categoria 4 estrelas ou superior.";
		*/		
		String titulo = "Contratação de empresa para fornecimento de passagens aéreas nacionais e internacionais, reserva, emissão, "
				+ "marcação, remarcação, endosso e entrega de bilhetes ou ordenas de passagens e reserva de hospedagem em hotéis "
				+ "de categoria 4 estrelas ou superior.";
					
		info.setTitle(titulo);
		info.setCustomMetadataValue("Título", titulo);
		//
		info.setCustomMetadataValue("Tipo Documental", "Processo de Licitação de Serviço Comum"); // ok
		info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
		info.setCustomMetadataValue("Classe", "Controle de compras, serviços e obras"); // OK
		info.setCustomMetadataValue("Data de produção do documento original", "2005-2008");			
		info.setCustomMetadataValue("Destinação Prevista", "Eliminação 12 anos após encerramento"); // ok
		info.setCustomMetadataValue("Gênero", "Textual"); // ok
		info.setCustomMetadataValue("Prazo de guarda", "2021"); // ok
	}
	 
    /* Protocolo 2012/10/7844
     * */
	public static void PreencheMetadadosProtocolo2012_1(PDDocumentInformation info, File file, String checksum) {
		// Altera os metadados do documento
		info.setKeywords(SmgMetadados.palavrasChave); // ok - não pedido
		info.setSubject("Contrato Gocil - Serviços de Vigilância e Segurança Ltda"); //ok
		info.setAuthor("Secretaria Municipal de Cooperação para Assuntos de Segurança"); // Ok
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
		//System.out.println(dataFormatada);
		//
		String dataLocal = "Campinas, " + dataFormatada;					
        //
		info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
		//			
		int indiceInicial = file.getName().length() - 6; // Começa 5 posições antes do final
		int indiceFinal = file.getName().length() - 4; // Termina 3 posições antes do final (exclusivo)
		//
		String versao = file.getName().substring(indiceInicial, indiceFinal);
		String versaoNumeroSemZeros = versao.replaceFirst("^0+", "");
		
		System.out.println("Número extraído: " + versaoNumeroSemZeros);
		//
		String identificadorAux = "2012_10_7844_X_41";
		String identificador = identificadorAux.replace("X", versaoNumeroSemZeros);
		//
		info.setCustomMetadataValue("Identificador do documento digital", identificador);
		info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
		//
		info.setCreator("Informática Municipios Associados");
		info.setProducer("Informática Municipios Associados");					
		//		
		String titulo = "Contratação de empresa para serviços de Segurança Patrimonial armada e desarmada";
		//
		info.setTitle(titulo);
		info.setCustomMetadataValue("Título", titulo);
		//
		info.setCustomMetadataValue("Tipo Documental", "Processo de Licitação de Serviço Comum"); // ok
		info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
		info.setCustomMetadataValue("Classe", "Controle de compras, serviços e obras"); // OK
		info.setCustomMetadataValue("Data de produção do documento original", "2012-2016");			
		info.setCustomMetadataValue("Destinação Prevista", "Eliminação 12 anos após encerramento"); // ok
		info.setCustomMetadataValue("Gênero", "Textual"); // ok
		info.setCustomMetadataValue("Prazo de guarda", "2029"); // ok		
	}
	
    /* Protocolo 2012/10/42276
     * */
	public static void PreencheMetadadosProtocolo2012_2(PDDocumentInformation info, File file, String checksum) {
		// Altera os metadados do documento
		info.setKeywords(SmgMetadados.palavrasChave); // ok - não pedido
		info.setSubject("Contrato Telefônica Brasil S.A."); //ok
		info.setAuthor("Gabinete do Prefeito"); // Ok
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
		//System.out.println(dataFormatada);
		//
		String dataLocal = "Campinas, " + dataFormatada;					
        //
		info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
		//			
		int indiceInicial = file.getName().length() - 6; // Começa 5 posições antes do final
		int indiceFinal = file.getName().length() - 4; // Termina 3 posições antes do final (exclusivo)
		//
		String versao = file.getName().substring(indiceInicial, indiceFinal);
		String versaoNumeroSemZeros = versao.replaceFirst("^0+", "");
		
		System.out.println("Número extraído: " + versaoNumeroSemZeros);
		//
		String identificadorAux = "2012_10_42276_X_10";
		String identificador = identificadorAux.replace("X", versaoNumeroSemZeros);
		//
		info.setCustomMetadataValue("Identificador do documento digital", identificador);
		info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
		//
		info.setCreator("Informática Municipios Associados");
		info.setProducer("Informática Municipios Associados");					
		//		
		String titulo = "Contratação de empresa para prestação de serviço telefônico fixo comutado (STFC)";
		//
		info.setTitle(titulo);
		info.setCustomMetadataValue("Título", titulo);
		//
		info.setCustomMetadataValue("Tipo Documental", "Processo de Licitação de Serviço Comum"); // ok
		info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
		info.setCustomMetadataValue("Classe", "Controle de compras, serviços e obras"); // OK
		info.setCustomMetadataValue("Data de produção do documento original", "2012-2017");			
		info.setCustomMetadataValue("Destinação Prevista", "Eliminação 12 anos após encerramento"); // ok
		info.setCustomMetadataValue("Gênero", "Textual"); // ok
		info.setCustomMetadataValue("Prazo de guarda", "2030"); // ok
	}
	
    /* Protocolo 2012/10/25229 
     * */ 
	public static void PreencheMetadadosProtocolo2012_3(PDDocumentInformation info, File file, String checksum) {
		// Altera os metadados do documento
		info.setKeywords(SmgMetadados.palavrasChave); // ok - não pedido
		info.setSubject("Contrato A. TELECAMP - COM. DE EQUIPAMENTOS DE TELEFONIA LTDA - EPP"); //ok
		info.setAuthor("Secretaria de Chefia de Gabinete do Prefeito"); // Ok
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
		//System.out.println(dataFormatada);
		//
		String dataLocal = "Campinas, " + dataFormatada;					
        //
		info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
		//			
		int indiceInicial = file.getName().length() - 5; // Começa 5 posições antes do final
		int indiceFinal = file.getName().length() - 4; // Termina 3 posições antes do final (exclusivo)
		//
		String versao = file.getName().substring(indiceInicial, indiceFinal);
		
		System.out.println("Número extraído: " + versao);
		//
		String identificadorAux = "2012_10_25229_X_6";
		String identificador = identificadorAux.replace("X", versao);
		//
		info.setCustomMetadataValue("Identificador do documento digital", identificador);
		info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
		//
		info.setCreator("Informática Municipios Associados");
		info.setProducer("Informática Municipios Associados");					
		//							
		/*
		String titulo = "Contratação de Empresa Especializada para a locação, instalação e manutenção "
				+ "de Central Telefônica Privada de Comutação CPCT, (tipo PABX), "
				+ "Tecnologia CPA - T para o Departamento de Proteção e Defesa do "
				+ "Consumidor - PROCON, Departamento de Defesa Civil e para o Centro "
				+ "Integrado de Monitoramento de Campinas - CIMCamp.";
		*/		
		String titulo = "Contratação de Empresa Especializada para a locação, instalação e manutenção de Central Telefônica "
				+ "Privada de Comutação,(tipo PABX), Tecnologia CPA - T para o PROCON, o Depto de Defesa Civil e para o Centro "
				+ "Integrado de Mon1toramento de Campinas - CIMCamp.";								
		//
		info.setTitle(titulo);
		info.setCustomMetadataValue("Título", titulo);
		//
		info.setCustomMetadataValue("Tipo Documental", "Processo de Licitação de Serviço Comum"); // ok
		info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
		info.setCustomMetadataValue("Classe", "Controle de compras, serviços e obras"); // OK
		info.setCustomMetadataValue("Data de produção do documento original", "2012-2019");			
		info.setCustomMetadataValue("Destinação Prevista", "Eliminação 12 anos após encerramento"); // ok
		info.setCustomMetadataValue("Gênero", "Textual"); // ok
		info.setCustomMetadataValue("Prazo de guarda", "2032"); // ok
	}
	
    /* Protocolo 2012/10/48800
     * */
	public static void PreencheMetadadosProtocolo2012_4(PDDocumentInformation info, File file, String checksum) {
		// Altera os metadados do documento
		info.setKeywords(SmgMetadados.palavrasChave); // ok - não pedido
		info.setSubject("Contrato Empresa Brasileira de Correios e Telégrafos"); //ok
		info.setAuthor("Secretaria de Chefia de Gabinete do Prefeito"); // Ok
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
		//System.out.println(dataFormatada);
		//
		String dataLocal = "Campinas, " + dataFormatada;					
        //
		info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
		//			
		int indiceInicial = file.getName().length() - 5; // Começa 5 posições antes do final
		int indiceFinal = file.getName().length() - 4; // Termina 3 posições antes do final (exclusivo)
		//
		String versao = file.getName().substring(indiceInicial, indiceFinal);
		
		System.out.println("Número extraído: " + versao);
		//
		String identificadorAux = "2012_10 48800_X_5";
		String identificador = identificadorAux.replace("X", versao);
		//
		info.setCustomMetadataValue("Identificador do documento digital", identificador);
		info.setCustomMetadataValue("Responsável pela digitalização", "Informática de Municípios Associados");
		//
		info.setCreator("Informática Municipios Associados");
		info.setProducer("Informática Municipios Associados");					
		//				
		String titulo = "Contratação direta para prestação de serviços postais e "
				+ "telemáticos convencionais, adicionais, nas modalidades nacional e "
				+ "internacional, bem como a compra de produtos postais, disponibilizados em "
				+ "unidades de atendimento em âmbito nacional.";
		//
		info.setTitle(titulo);
		info.setCustomMetadataValue("Título", titulo);
		//
		info.setCustomMetadataValue("Tipo Documental", "Processo de Licitação de Serviço Comum"); // ok
		info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
		info.setCustomMetadataValue("Classe", "Controle de compras, serviços e obras"); // OK
		info.setCustomMetadataValue("Data de produção do documento original", "2012-2016");			
		info.setCustomMetadataValue("Destinação Prevista", "Eliminação 12 anos após encerramento"); // ok
		info.setCustomMetadataValue("Gênero", "Textual"); // ok
		info.setCustomMetadataValue("Prazo de guarda", "2028"); // ok
	}		
	
    /* Protocolo 2021/10/8353
     * */
	public static void PreencheMetadadosProtocolo2021_1(PDDocumentInformation info, File file, String checksum) {
		// Altera os metadados do documento
		info.setKeywords(SmgMetadados.palavrasChave); // ok - não pedido
		info.setSubject("Contratação Direta nº 931/2021 - Contrato nº 112/2021 - Informática Municípios Associados"); //ok
		info.setAuthor("Secretaria Municipal de Governo"); // Ok
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
		//System.out.println(dataFormatada);
		//
		String dataLocal = "Campinas, " + dataFormatada;					
        //
		info.setCustomMetadataValue("Data e local da digitalização", dataLocal); //ok
		//			
		int indiceInicial = file.getName().length() - 5; // Começa 5 posições antes do final
		int indiceFinal = file.getName().length() - 4; // Termina 3 posições antes do final (exclusivo)
		//
		String versao = file.getName().substring(indiceInicial, indiceFinal);
		
		System.out.println("Número extraído: " + versao);
		//
		String identificadorAux = "2021_10_8353_X_8";
		String identificador = identificadorAux.replace("X", versao);
		//
		info.setCustomMetadataValue("Identificador do documento digital", identificador);
		info.setCustomMetadataValue("Responsável pela digitalização", "Informática Municípios Associados");
		//
		info.setCreator("Informática Municipios Associados");
		info.setProducer("Informática Municipios Associados");					
		//
		/*
		String titulo = "Contratação, sob demanda, de horas ou fração de horas de serviço de "
				+ "atendimento prestado à população em geral, referentes à recepção, "
				+ "informações, esclarecimentos, atividades administrativas decorrentes do atendimento à população, reclamações e "
				+ "solicitações de serviços aos órgãos da Administração Pública Direta e Indireta, de forma direta "
				+ "(presencial) ou indireta (atendimento remoto).";
		*/			
		String titulo = "Contratação, sob demanda, de serviço de atendimento à população em geral, referentes à recepção, "
				+ "informações, esclarecimentos, reclamações e solicitações de serviços à Administração Pública Direta e "
				+ "Indireta, de forma presencial ou por atendimento remoto.";
					
		info.setTitle(titulo);
		info.setCustomMetadataValue("Título", titulo);
		//
		info.setCustomMetadataValue("Tipo Documental", "Processo de Licitação de Serviço Comum"); // ok
		info.setCustomMetadataValue("Hash (checksum) da imagem", checksum);// Hash (checksum) da imagem // ok
		info.setCustomMetadataValue("Classe", "Execução de Compras, serviços e obras"); // OK
		info.setCustomMetadataValue("Data de produção do documento original", "25/06/2021");			
		info.setCustomMetadataValue("Destinação Prevista", "Eliminação 12 anos após encerramento"); // ok
		info.setCustomMetadataValue("Gênero", "Textual"); // ok
		info.setCustomMetadataValue("Prazo de guarda", "2035"); // ok
	}
}